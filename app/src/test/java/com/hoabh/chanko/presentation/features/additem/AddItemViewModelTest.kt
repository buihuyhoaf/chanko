package com.hoabh.chanko.presentation.features.additem

import com.hoabh.chanko.domain.additem.*
import com.hoabh.chanko.domain.auth.*
import com.hoabh.chanko.domain.repository.foundation.AccountScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AddItemViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private class Auth : AuthenticationRepository {
        override val session = MutableStateFlow<SessionState>(SessionState.Available(AccountScope("a")))
        override suspend fun restore() = AuthResult.Success(Unit)
        override suspend fun signIn(provider: GoogleTokenProvider) = AuthResult.Success(Unit)
        override suspend fun refresh() = AuthResult.Success(Unit)
        override suspend fun logout() = AuthResult.Success(Revocation.CONFIRMED)
    }
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }
    private fun vm(service: UrlExtractionService = UnavailableUrlExtractionService(),
        engine: QuickCheckEvaluationContract = UnavailableQuickCheckEngine(), auth: Auth = Auth()) =
        AddItemViewModel(service, engine, auth)
    private fun AddItemViewModel.manual() {
        open(); editUrl("https://example.com/item"); extract()
    }
    private fun AddItemViewModel.validForm() {
        edit { it.editProduct { form -> form.copy(title = "Tai nghe", rawPrice = "1000", category = "Điện tử", reason = "Làm việc") } }
        edit { it.copy(confirmedPrice = it.price) }
    }
    @Test fun plusOpensEmptyUrlDialog() = runTest(dispatcher) {
        val vm = vm(); vm.open(); runCurrent()
        assertTrue(vm.state.value.active)
        assertEquals(AddItemStep.URL_DIALOG, vm.state.value.step)
        assertEquals("", vm.state.value.form.url)
    }
    @Test fun invalidUrlOffersManualInputWithoutCallingExtraction() = runTest(dispatcher) {
        var calls = 0
        val vm = vm(UrlExtractionService { calls++; ExtractionResult.Failed }); vm.open()
        vm.editUrl("javascript:alert(1)"); vm.extract(); runCurrent()
        assertEquals(0, calls)
        assertTrue(vm.state.value.extractionFailed)
    }
    @Test fun emptyUrlCanOpenManualInputFromTheExtractButton() = runTest(dispatcher) {
        var calls = 0
        val vm = vm(UrlExtractionService { calls++; ExtractionResult.Failed })
        vm.open(); vm.extract(); runCurrent()
        assertTrue(vm.state.value.extractionFailed)
        assertEquals(0, calls)
        vm.confirmManualInput()
        assertEquals(AddItemStep.MANUAL_INPUT, vm.state.value.step)
        assertEquals("", vm.state.value.form.url)
    }
    @Test fun unavailableServiceOpensFailureAndCancelPreservesUrl() = runTest(dispatcher) {
        val vm = vm(); vm.manual(); runCurrent()
        assertTrue(vm.state.value.extractionFailed)
        vm.dismissExtractionFailure()
        assertFalse(vm.state.value.extractionFailed)
        assertEquals(AddItemStep.URL_DIALOG, vm.state.value.step)
        assertEquals("https://example.com/item", vm.state.value.form.url)
    }
    @Test fun failureConfirmationOpensManualWithoutExtractionsOrConfirmedPrice() = runTest(dispatcher) {
        val vm = vm(); vm.manual(); runCurrent(); vm.confirmManualInput()
        assertEquals(AddItemStep.MANUAL_INPUT, vm.state.value.step)
        assertFalse(vm.state.value.extractionFailed)
        assertNull(vm.state.value.form.confirmedPrice)
        assertEquals("https://example.com/item", vm.state.value.form.url)
    }
    @Test fun successOpensVerifyAndRequiresPriceConfirmationAndUserReason() = runTest(dispatcher) {
        val vm = vm(UrlExtractionService { ExtractionResult.Success(ExtractedItemInfo("Tai nghe", 2490000, "Điện tử")) })
        vm.open(); vm.editUrl("https://example.com/item"); vm.extract(); runCurrent()
        assertEquals(AddItemStep.VERIFY_INFO, vm.state.value.step)
        assertEquals("Tai nghe", vm.state.value.form.title)
        assertEquals(2490000L, vm.state.value.form.price)
        assertNull(vm.state.value.form.confirmedPrice)
        assertEquals("", vm.state.value.form.reason)
        assertFalse(vm.state.value.form.canContinue)
    }
    @Test fun partialSuccessLeavesMissingFieldsEditable() = runTest(dispatcher) {
        val vm = vm(UrlExtractionService { ExtractionResult.Success(ExtractedItemInfo(title = "Tai nghe")) })
        vm.open(); vm.editUrl("https://example.com/item"); vm.extract(); runCurrent()
        assertEquals(AddItemStep.VERIFY_INFO, vm.state.value.step)
        assertNull(vm.state.value.form.price)
        assertFalse(vm.state.value.form.canContinue)
        vm.validForm()
        assertTrue(vm.state.value.form.canContinue)
    }
    @Test fun cancelAndReopenDiscardsAllUnsavedInformation() = runTest(dispatcher) {
        val vm = vm(); vm.manual(); runCurrent(); vm.confirmManualInput(); vm.validForm()
        val key = vm.state.value.form.saveIdempotencyKey
        vm.cancel()
        assertEquals(AddItemUiState(), vm.state.value)
        vm.open()
        assertEquals("", vm.state.value.form.title)
        assertEquals("", vm.state.value.form.url)
        assertNull(vm.state.value.form.confirmedPrice)
        assertNotEquals(key, vm.state.value.form.saveIdempotencyKey)
    }
    @Test fun newViewModelDoesNotRestorePreviousSession() = runTest(dispatcher) {
        val first = vm(); first.manual(); runCurrent(); first.confirmManualInput(); first.validForm()
        val recreated = vm(); runCurrent(); recreated.open()
        assertEquals("", recreated.state.value.form.title)
        assertEquals(AddItemStep.URL_DIALOG, recreated.state.value.step)
    }
    @Test fun repeatedExtractIsSingleFlight() = runTest(dispatcher) {
        var calls = 0
        val gate = CompletableDeferred<ExtractionResult>()
        val vm = vm(UrlExtractionService { calls++; gate.await() })
        vm.open(); vm.editUrl("https://example.com/item"); vm.extract(); vm.extract(); runCurrent()
        assertEquals(1, calls)
        assertTrue(vm.state.value.extracting)
        gate.complete(ExtractionResult.Failed); runCurrent()
        assertTrue(vm.state.value.extractionFailed)
    }
    @Test fun lateSuccessAfterCancelCannotReopenOrOverwriteNewSession() = runTest(dispatcher) {
        val gate = CompletableDeferred<ExtractionResult>()
        val vm = vm(UrlExtractionService { withContext(NonCancellable) { gate.await() } })
        vm.open(); vm.editUrl("https://example.com/old"); vm.extract(); runCurrent()
        vm.cancel(); vm.open(); vm.editUrl("https://example.com/new")
        gate.complete(ExtractionResult.Success(ExtractedItemInfo(title = "Stale"))); runCurrent()
        assertEquals("https://example.com/new", vm.state.value.form.url)
        assertEquals("", vm.state.value.form.title)
        assertEquals(AddItemStep.URL_DIALOG, vm.state.value.step)
    }
    @Test fun extractionExceptionBecomesRecoverableFailure() = runTest(dispatcher) {
        val vm = vm(UrlExtractionService { throw java.io.IOException() })
        vm.open(); vm.editUrl("https://example.com/item"); vm.extract(); runCurrent()
        assertFalse(vm.state.value.extracting)
        assertTrue(vm.state.value.extractionFailed)
    }
    @Test fun missingEngineDoesNotGenerateVerdictOrPersistItem() = runTest(dispatcher) {
        val vm = vm(); vm.manual(); runCurrent(); vm.confirmManualInput(); vm.validForm()
        vm.continueForm(); runCurrent()
        assertEquals(AddItemStep.QUICK_CHECK, vm.state.value.step)
        assertEquals(EvaluationState.Unavailable, vm.state.value.evaluation)
        assertNull(vm.state.value.form.assessment)
        vm.cancel()
        assertEquals(AddItemUiState(), vm.state.value)
    }
    @Test fun earlyAssessmentPassesMissingAnswersWithoutInventingThem() = runTest(dispatcher) {
        val received = mutableListOf<AddItemFormState>()
        val engine = QuickCheckEvaluationContract { form -> received += form
            EvaluationState.Question(QuickCheckQuestion("q1", "Câu hỏi", listOf("Có", "Chưa chắc")), 0) }
        val vm = vm(engine = engine); vm.manual(); runCurrent(); vm.confirmManualInput(); vm.validForm(); vm.continueForm(); runCurrent()
        vm.evaluate(early = true); runCurrent()
        assertEquals(2, received.size)
        assertTrue(received.last().earlyAssessment)
        assertTrue(received.last().answers.isEmpty())
        assertNull(vm.state.value.form.assessment)
    }
    @Test fun accountChangeDiscardsTheInMemorySession() = runTest(dispatcher) {
        val auth = Auth(); val vm = vm(auth = auth); vm.manual(); runCurrent(); vm.confirmManualInput(); vm.validForm()
        auth.session.value = SessionState.Available(AccountScope("b")); runCurrent()
        assertEquals(AddItemUiState(), vm.state.value)
    }
    @Test fun matchingFixtureAssessmentOpensResultWithoutMarkingPurchased() = runTest(dispatcher) {
        val engine = QuickCheckEvaluationContract { form ->
            EvaluationState.Result(AssessmentSnapshot(Verdict.WAIT, listOf("Fixture"), emptyList(),
                form.productRevision, form.answerRevision, "test-only"))
        }
        val vm = vm(engine = engine); vm.manual(); runCurrent(); vm.confirmManualInput(); vm.validForm(); vm.continueForm(); runCurrent()
        assertEquals(AddItemStep.DECISION_RESULT, vm.state.value.step)
        assertEquals(CoolingChoice.APPLIED, vm.state.value.form.coolingChoice)
        assertEquals(java.util.concurrent.TimeUnit.DAYS.toMillis(1), vm.state.value.form.coolingDurationMillis)
    }

    @Test fun saveItemInsertsCooldownItemWithMandatoryDuration() = runTest(dispatcher) {
        val inserted = mutableListOf<com.hoabh.chanko.domain.model.CooldownItem>()
        val repo = object : com.hoabh.chanko.domain.repository.ItemRepository {
            override fun getActiveItems() = flowOf(emptyList<com.hoabh.chanko.domain.model.CooldownItem>())
            override fun getItemsByStatus(status: com.hoabh.chanko.domain.model.ItemStatus) = flowOf(emptyList<com.hoabh.chanko.domain.model.CooldownItem>())
            override fun getArchivedItems() = flowOf(emptyList<com.hoabh.chanko.domain.model.CooldownItem>())
            override fun getItemById(id: Int) = flowOf(null)
            override suspend fun insertItem(item: com.hoabh.chanko.domain.model.CooldownItem) { inserted += item }
            override suspend fun updateItem(item: com.hoabh.chanko.domain.model.CooldownItem) {}
            override suspend fun updateItemStatus(id: Int, status: com.hoabh.chanko.domain.model.ItemStatus) {}
            override suspend fun setArchived(id: Int, isArchived: Boolean) {}
            override suspend fun deleteItem(item: com.hoabh.chanko.domain.model.CooldownItem) {}
            override fun getTotalSavedAmount() = flowOf(0L)
        }
        val engine = QuickCheckEvaluationContract { form ->
            EvaluationState.Result(AssessmentSnapshot(Verdict.WAIT, listOf("Fixture"), emptyList(),
                form.productRevision, form.answerRevision, "test-only"))
        }
        val vm = AddItemViewModel(UnavailableUrlExtractionService(), engine, Auth(), repo)
        vm.manual(); runCurrent(); vm.confirmManualInput(); vm.validForm(); vm.continueForm(); runCurrent()
        assertEquals(AddItemStep.DECISION_RESULT, vm.state.value.step)
        vm.saveItem(); runCurrent()
        assertEquals(1, inserted.size)
        assertEquals("Tai nghe", inserted.first().name)
        assertEquals(1000L, inserted.first().price)
        assertEquals(com.hoabh.chanko.domain.model.ItemStatus.COOLING_DOWN, inserted.first().status)
        assertEquals(java.util.concurrent.TimeUnit.DAYS.toMillis(1), inserted.first().cooldownDurationMillis)
        assertFalse(vm.state.value.active)
    }
    @Test fun staleAssessmentCannotOpenResult() = runTest(dispatcher) {
        val engine = MockQuickCheckEngine(EvaluationState.Result(
            AssessmentSnapshot(Verdict.BUY, listOf("Stale fixture"), emptyList(), -1, -1, "test-only")))
        val vm = vm(engine = engine); vm.manual(); runCurrent(); vm.confirmManualInput(); vm.validForm(); vm.continueForm(); runCurrent()
        assertEquals(AddItemStep.QUICK_CHECK, vm.state.value.step)
        assertEquals(EvaluationState.Unavailable, vm.state.value.evaluation)
        assertNull(vm.state.value.form.assessment)
    }
    @Test fun engineCannotRenderAQuestionBeyondTheThreeQuestionLimit() = runTest(dispatcher) {
        val engine = MockQuickCheckEngine(EvaluationState.Question(QuickCheckQuestion("q4", "Fixture", listOf("A")), 3))
        val vm = vm(engine = engine); vm.manual(); runCurrent(); vm.confirmManualInput(); vm.validForm(); vm.continueForm(); runCurrent()
        assertEquals(EvaluationState.Unavailable, vm.state.value.evaluation)
        assertNull(vm.state.value.form.assessment)
    }

}
