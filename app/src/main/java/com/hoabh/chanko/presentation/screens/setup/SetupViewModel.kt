package com.hoabh.chanko.presentation.screens.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.TimeZone

/** Presentation integration seam, not an endpoint or an approved API payload. */
interface SetupGateway {
    suspend fun checkStatus(): SetupStatus
    suspend fun submit(draft: SetupDraft): SetupSubmission
}

enum class SetupStatus { COMPLETE, INCOMPLETE, UNKNOWN, OFFLINE, ERROR, UNAVAILABLE }

enum class SetupSubmission { ACCEPTED, UNKNOWN, OFFLINE, ERROR, UNAVAILABLE }

data class SetupDraft(val amountVnd: String, val anchorDay: Int, val deviceTimeZone: String)

/** Fails closed until a reviewed backend adapter is available. Never returns fake completion. */
object UnconfiguredSetupGateway : SetupGateway {
    override suspend fun checkStatus(): SetupStatus = SetupStatus.UNAVAILABLE
    override suspend fun submit(draft: SetupDraft): SetupSubmission = SetupSubmission.UNAVAILABLE
}

enum class SetupPage { CHECKING, STATUS_ERROR, FORM, COMPLETE }

enum class SetupProblem { UNKNOWN, OFFLINE, SAVE_ERROR, UNAVAILABLE, TIME_ZONE }

data class SetupUiState(
    val page: SetupPage = SetupPage.CHECKING,
    val step: Int = 1,
    val amount: String = "",
    val day: Int? = null,
    val amountError: Boolean = false,
    val dayError: Boolean = false,
    val busy: Boolean = false,
    val problem: SetupProblem? = null,
    // A possibly committed submission must be reconciled before any new submission.
    val needsReconciliation: Boolean = false,
)

class SetupViewModel(
    private val gateway: SetupGateway = UnconfiguredSetupGateway,
    private val deviceTimeZone: () -> String = { TimeZone.getDefault().id },
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    /** Backward-compatible alias for existing tests and call sites. */
    val state: StateFlow<SetupUiState> get() = uiState

    private var operation: Job? = null

    init {
        checkStatus()
    }

    fun amountChanged(value: String) {
        if (_uiState.value.busy) return
        val raw = if (value.matches(Regex("[0-9]{1,3}(\\.[0-9]{3})+"))) {
            value.replace(".", "")
        } else {
            value
        }
        _uiState.update {
            it.copy(
                amount = raw,
                amountError = false,
                problem = null,
            )
        }
    }

    fun dayChanged(day: Int) {
        if (_uiState.value.busy || day !in 1..31) return
        _uiState.update {
            it.copy(
                day = day,
                dayError = false,
                problem = null,
            )
        }
    }

    fun continueToDay() {
        if (_uiState.value.busy) return
        val valid = validAmount(_uiState.value.amount)
        _uiState.update {
            it.copy(
                amountError = !valid,
                step = if (valid) 2 else 1,
            )
        }
    }

    fun back() {
        if (!_uiState.value.busy) {
            _uiState.update {
                it.copy(
                    step = 1,
                    problem = null,
                )
            }
        }
    }

    fun checkStatus() {
        if (operation?.isActive == true) return
        _uiState.update {
            it.copy(
                page = SetupPage.CHECKING,
                problem = null,
                busy = true,
            )
        }
        operation = viewModelScope.launch {
            val result = safeStatus()
            applyStatus(result)
        }
    }

    fun save() {
        val current = _uiState.value
        if (current.busy || current.page != SetupPage.FORM) return

        if (current.needsReconciliation) {
            _uiState.update { it.copy(busy = true, problem = null) }
            operation = viewModelScope.launch {
                applyStatus(safeStatus())
                // Incomplete restores the editable form; a fresh tap explicitly submits.
            }
            return
        }

        if (!validAmount(current.amount) || current.day == null) {
            _uiState.update {
                it.copy(
                    amountError = !validAmount(it.amount),
                    dayError = it.day == null,
                    step = if (!validAmount(it.amount)) 1 else 2,
                )
            }
            return
        }

        val zone = deviceTimeZone()
        // Offset aliases must not silently stand in for an IANA device region.
        if (zone !in TimeZone.getAvailableIDs() || (!zone.contains('/') && zone !in setOf("UTC", "GMT"))) {
            _uiState.update { it.copy(problem = SetupProblem.TIME_ZONE) }
            return
        }

        val draft = SetupDraft(current.amount.trimStart('0'), current.day, zone)
        _uiState.update { it.copy(busy = true, problem = null) }
        operation = viewModelScope.launch {
            val response = try {
                gateway.submit(draft)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                SetupSubmission.UNKNOWN
            }
            currentCoroutineContext().ensureActive()
            when (response) {
                SetupSubmission.ACCEPTED, SetupSubmission.UNKNOWN -> {
                    _uiState.update { it.copy(needsReconciliation = true) }
                    applyStatus(safeStatus())
                }
                SetupSubmission.OFFLINE -> {
                    _uiState.update { it.copy(busy = false, problem = SetupProblem.OFFLINE) }
                }
                SetupSubmission.ERROR -> {
                    _uiState.update { it.copy(busy = false, problem = SetupProblem.SAVE_ERROR) }
                }
                SetupSubmission.UNAVAILABLE -> {
                    _uiState.update { it.copy(busy = false, problem = SetupProblem.UNAVAILABLE) }
                }
            }
        }
    }

    private suspend fun safeStatus(): SetupStatus = try {
        val status = gateway.checkStatus()
        currentCoroutineContext().ensureActive()
        status
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        SetupStatus.UNKNOWN
    }

    private fun applyStatus(status: SetupStatus) {
        _uiState.update { old ->
            when (status) {
                SetupStatus.COMPLETE -> old.copy(
                    page = SetupPage.COMPLETE,
                    busy = false,
                    problem = null,
                )
                SetupStatus.INCOMPLETE -> old.copy(
                    page = SetupPage.FORM,
                    busy = false,
                    problem = null,
                    needsReconciliation = false,
                )
                else -> old.copy(
                    page = if (old.page == SetupPage.FORM) SetupPage.FORM else SetupPage.STATUS_ERROR,
                    busy = false,
                    problem = when (status) {
                        SetupStatus.OFFLINE -> SetupProblem.OFFLINE
                        SetupStatus.UNAVAILABLE -> SetupProblem.UNAVAILABLE
                        else -> SetupProblem.UNKNOWN
                    },
                )
            }
        }
    }

    /** Called when the account subtree leaves; drafts are never shared between accounts. */
    fun discard() {
        operation?.cancel()
        _uiState.value = SetupUiState()
    }

    companion object {
        fun validAmount(raw: String): Boolean =
            raw.isNotEmpty() && raw.all { it in '0'..'9' } && raw.any { it != '0' }
    }
}
