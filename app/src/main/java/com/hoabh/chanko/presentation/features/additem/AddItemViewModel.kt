package com.hoabh.chanko.presentation.features.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.additem.*
import com.hoabh.chanko.domain.additem.UrlExtractionService
import com.hoabh.chanko.domain.additem.UnavailableUrlExtractionService
import com.hoabh.chanko.domain.auth.AuthenticationRepository
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.policy.FreezeDurationPolicy
import com.hoabh.chanko.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddItemUiState(
    val active: Boolean = false,
    val form: AddItemFormState = AddItemFormState(),
    val step: AddItemStep = AddItemStep.URL_DIALOG,
    val extracting: Boolean = false,
    val extractionFailed: Boolean = false,
    val assessing: Boolean = false,
    val photoError: Boolean = false,
    val evaluation: EvaluationState = EvaluationState.Unavailable
)
sealed interface AddItemEffect {
    data class Navigate(val step: AddItemStep) : AddItemEffect
    data object Exit : AddItemEffect
}

/** Session-only state. No SavedStateHandle, Room, auto-save or restoration. */
@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val extraction: UrlExtractionService,
    private val engine: QuickCheckEvaluationContract,
    auth: AuthenticationRepository,
    private val itemRepository: ItemRepository? = null
) : ViewModel() {
    private val mutableState = MutableStateFlow(AddItemUiState())
    val state = mutableState.asStateFlow()
    private val effectsChannel = Channel<AddItemEffect>(Channel.BUFFERED)
    val effects = effectsChannel.receiveAsFlow()
    private var operation: Job? = null
    private var generation = 0L
    private var owner = auth.session.value

    init {
        viewModelScope.launch {
            auth.session.collect { identity ->
                if (identity != owner) {
                    owner = identity
                    if (state.value.active) cancel()
                }
            }
        }
    }
    fun open() {
        if (state.value.active) return
        generation++
        while (effectsChannel.tryReceive().isSuccess) { }
        mutableState.value = AddItemUiState(active = true,
            form = AddItemFormState(saveIdempotencyKey = UUID.randomUUID().toString()))
    }
    fun editUrl(url: String) {
        if (!state.value.active || state.value.extracting || state.value.extractionFailed) return
        edit { it.editProduct { form -> form.copy(url = url) } }
    }
    fun edit(transform: (AddItemFormState) -> AddItemFormState) {
        val current = state.value
        if (!current.active || current.extracting || current.assessing) return
        mutableState.value = current.copy(form = transform(current.form), photoError = false)
    }
    fun photoFailed() { mutableState.update { it.copy(photoError = true) } }
    fun extract() {
        val current = state.value
        if (!current.active || current.step != AddItemStep.URL_DIALOG || current.extracting ||
            current.extractionFailed) return
        if (!isProductUrl(current.form.url)) {
            mutableState.update { it.copy(extractionFailed = true) }
            return
        }
        val request = ++generation
        mutableState.value = current.copy(extracting = true)
        operation = viewModelScope.launch {
            val result = try { extraction.extract(current.form.url.trim()) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { ExtractionResult.Failed }
            if (request != generation || !state.value.active) return@launch
            when (result) {
                ExtractionResult.Failed -> mutableState.update { it.copy(extracting = false, extractionFailed = true) }
                is ExtractionResult.Success -> {
                    val item = result.item
                    val form = current.form.editProduct { it.copy(title = item.title,
                        rawPrice = item.priceVnd?.takeIf { value -> value > 0 }?.toString().orEmpty(),
                        category = item.category, photoUri = item.photoUri) }
                    mutableState.value = current.copy(form = form, step = AddItemStep.VERIFY_INFO)
                    effectsChannel.send(AddItemEffect.Navigate(AddItemStep.VERIFY_INFO))
                }
            }
        }
    }
    fun dismissExtractionFailure() {
        mutableState.update { it.copy(extractionFailed = false) }
    }
    fun confirmManualInput() {
        if (!state.value.active || !state.value.extractionFailed) return
        mutableState.update { it.copy(extractionFailed = false, step = AddItemStep.MANUAL_INPUT) }
        effectsChannel.trySend(AddItemEffect.Navigate(AddItemStep.MANUAL_INPUT))
    }
    fun continueForm() {
        if (state.value.step !in setOf(AddItemStep.VERIFY_INFO, AddItemStep.MANUAL_INPUT) || !state.value.form.canContinue) return
        if (state.value.assessing) return
        mutableState.update { it.copy(step = AddItemStep.QUICK_CHECK) }
        effectsChannel.trySend(AddItemEffect.Navigate(AddItemStep.QUICK_CHECK))
        evaluate()
    }
    fun answer(questionId: String, choice: String) {
        val current = state.value
        val question = (current.evaluation as? EvaluationState.Question)?.question ?: return
        if (current.assessing || questionId != question.id || choice !in question.choices) return
        mutableState.update { it.copy(form = it.form.copy(answers = it.form.answers + (questionId to choice),
            answerRevision = it.form.answerRevision + 1, assessment = null)) }
    }
    fun evaluate(early: Boolean = false) {
        val current = state.value
        if (!current.active || current.step != AddItemStep.QUICK_CHECK || current.assessing) return
        val question = current.evaluation as? EvaluationState.Question
        if (!early && question != null && current.form.answers[question.question.id] == null) return
        val request = ++generation
        val form = current.form.copy(earlyAssessment = early)
        mutableState.update { it.copy(assessing = true, form = form) }
        operation = viewModelScope.launch {
            val result = try { engine.evaluate(form) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { EvaluationState.Unavailable }
            if (request != generation || !state.value.active) return@launch
            when (result) {
                is EvaluationState.Question -> mutableState.update { it.copy(assessing = false,
                    evaluation = if (result.index in 0..2) result else EvaluationState.Unavailable) }
                is EvaluationState.Result -> {
                    val snapshot = result.snapshot
                    if (snapshot.productRevision != form.productRevision || snapshot.answerRevision != form.answerRevision) {
                        mutableState.update { it.copy(assessing = false, evaluation = EvaluationState.Unavailable) }
                    } else {
                        val duration = FreezeDurationPolicy.calculateDurationMillis(form.price ?: 0L)
                        val updatedForm = form.copy(
                            assessment = snapshot,
                            coolingChoice = CoolingChoice.APPLIED,
                            coolingDurationMillis = duration
                        )
                        mutableState.update { it.copy(assessing = false, evaluation = result,
                            form = updatedForm, step = AddItemStep.DECISION_RESULT) }
                        effectsChannel.send(AddItemEffect.Navigate(AddItemStep.DECISION_RESULT))
                    }
                }
                EvaluationState.Unavailable -> mutableState.update { it.copy(assessing = false, evaluation = result) }
            }
        }
    }
    fun saveItem() {
        val current = state.value
        if (!current.active || current.assessing) return
        val price = current.form.price ?: return
        val duration = current.form.coolingDurationMillis ?: FreezeDurationPolicy.calculateDurationMillis(price)
        val now = System.currentTimeMillis()
        val item = CooldownItem(
            name = current.form.title,
            price = price,
            imageUri = current.form.photoUri.takeIf { it.isNotBlank() },
            addedTimestamp = now,
            cooldownDurationMillis = duration,
            reason = current.form.reason,
            status = ItemStatus.COOLING_DOWN
        )
        viewModelScope.launch {
            itemRepository?.insertItem(item)
            cancel()
        }
    }
    fun cancel() {
        generation++
        operation?.cancel()
        // Drain old navigation effects so a canceled extraction cannot reopen a discarded flow.
        while (effectsChannel.tryReceive().isSuccess) { }
        mutableState.value = AddItemUiState()
        effectsChannel.trySend(AddItemEffect.Exit)
    }
}
