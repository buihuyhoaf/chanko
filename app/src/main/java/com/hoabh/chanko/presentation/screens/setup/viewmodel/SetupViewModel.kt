package com.hoabh.chanko.presentation.screens.setup.viewmodel

import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.presentation.base.BaseViewModel
import com.hoabh.chanko.presentation.screens.setup.components.SetupDraft
import com.hoabh.chanko.presentation.screens.setup.components.SetupGateway
import com.hoabh.chanko.presentation.screens.setup.components.SetupStatus
import com.hoabh.chanko.presentation.screens.setup.components.SetupSubmission
import com.hoabh.chanko.presentation.screens.setup.components.UnconfiguredSetupGateway
import com.hoabh.chanko.presentation.screens.setup.data.SetupPage
import com.hoabh.chanko.presentation.screens.setup.data.SetupProblem
import com.hoabh.chanko.presentation.screens.setup.data.SetupUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.TimeZone

private val DOT_NUMBER_PATTERN = Regex("[0-9]{1,3}(\\.[0-9]{3})+")
private val STANDARD_FALLBACK_ZONES = setOf("UTC", "GMT")

class SetupViewModel(
    private val gateway: SetupGateway = UnconfiguredSetupGateway,
    private val deviceTimeZone: () -> String = { TimeZone.getDefault().id },
) : BaseViewModel<SetupUiState, Nothing>(SetupUiState()) {

    /** Backward-compatible alias for existing tests and call sites. */
    val state: StateFlow<SetupUiState> get() = uiState

    private var operation: Job? = null

    fun dismissErrorDialog() {
        updateState { it.copy(showErrorDialog = false) }
    }

    fun amountChanged(value: String) {
        if (currentState.busy) return
        val raw = if (value.matches(DOT_NUMBER_PATTERN)) value.replace(".", "") else value
        updateState {
            it.copy(
                amount = raw,
                amountError = false,
                problem = null,
            )
        }
    }

    fun dayChanged(day: Int) {
        if (currentState.busy || day !in 1..31) return
        updateState {
            it.copy(
                day = day,
                dayError = false,
                problem = null,
            )
        }
    }

    fun continueToDay() {
        if (currentState.busy) return
        val valid = validAmount(currentState.amount)
        updateState {
            it.copy(
                amountError = !valid,
                step = if (valid) 2 else 1,
            )
        }
    }

    fun back() {
        if (currentState.busy) return
        updateState {
            it.copy(
                step = 1,
                problem = null,
            )
        }
    }

    fun checkStatus() {
        if (operation?.isActive == true) return
        updateState {
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
        val current = currentState
        if (current.busy || current.page != SetupPage.FORM) return

        if (current.needsReconciliation) {
            updateState { it.copy(busy = true, problem = null) }
            operation = viewModelScope.launch {
                applyStatus(safeStatus())
            }
            return
        }

        val isAmountValid = validAmount(current.amount)
        if (!isAmountValid || current.day == null) {
            updateState {
                it.copy(
                    amountError = !isAmountValid,
                    dayError = it.day == null,
                    step = if (!isAmountValid) 1 else 2,
                )
            }
            return
        }

        val zone = deviceTimeZone()
        if (zone !in TimeZone.getAvailableIDs() || (!zone.contains('/') && zone !in STANDARD_FALLBACK_ZONES)) {
            updateState { it.copy(problem = SetupProblem.TIME_ZONE, showErrorDialog = true) }
            return
        }

        val draft = SetupDraft(current.amount.trimStart('0'), current.day, zone)
        updateState { it.copy(busy = true, problem = null) }
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
                    updateState { it.copy(needsReconciliation = true) }
                    applyStatus(safeStatus())
                }
                SetupSubmission.OFFLINE -> handleSaveProblem(SetupProblem.OFFLINE)
                SetupSubmission.ERROR -> handleSaveProblem(SetupProblem.SAVE_ERROR)
                SetupSubmission.UNAVAILABLE -> handleSaveProblem(SetupProblem.UNAVAILABLE)
            }
        }
    }

    private fun handleSaveProblem(problem: SetupProblem) {
        updateState { it.copy(busy = false, problem = problem, showErrorDialog = true) }
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
        updateState { old ->
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
                    problem = status.toProblem(),
                    showErrorDialog = old.page == SetupPage.FORM,
                )
            }
        }
    }

    private fun SetupStatus.toProblem(): SetupProblem = when (this) {
        SetupStatus.OFFLINE -> SetupProblem.OFFLINE
        SetupStatus.UNAVAILABLE -> SetupProblem.UNAVAILABLE
        else -> SetupProblem.UNKNOWN
    }

    /** Called when the account subtree leaves; drafts are never shared between accounts. */
    fun discard() {
        operation?.cancel()
        updateState { SetupUiState() }
    }

    companion object {
        fun validAmount(raw: String): Boolean =
            raw.isNotEmpty() && raw.all { it in '0'..'9' } && raw.any { it != '0' }
    }
}
