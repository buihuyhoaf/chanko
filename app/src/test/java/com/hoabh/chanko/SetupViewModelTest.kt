package com.hoabh.chanko

import com.hoabh.chanko.presentation.screens.setup.components.SetupDraft
import com.hoabh.chanko.presentation.screens.setup.components.SetupGateway
import com.hoabh.chanko.presentation.screens.setup.components.SetupStatus
import com.hoabh.chanko.presentation.screens.setup.components.SetupSubmission
import com.hoabh.chanko.presentation.screens.setup.data.SetupPage
import com.hoabh.chanko.presentation.screens.setup.data.SetupProblem
import com.hoabh.chanko.presentation.screens.setup.viewmodel.SetupViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SetupViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun start() { Dispatchers.setMain(dispatcher) }
    @After fun stop() { Dispatchers.resetMain() }
    private class Gateway : SetupGateway {
        var status = SetupStatus.INCOMPLETE
        var response = SetupSubmission.ACCEPTED
        var submitted = mutableListOf<SetupDraft>()
        var beforeSave: suspend () -> Unit = {}
        override suspend fun checkStatus() = status
        override suspend fun submit(draft: SetupDraft): SetupSubmission { submitted += draft; beforeSave(); return response }
    }
    @Test fun noDefaultsNoArbitraryAmountLimitAndBackKeepsDraft() = runTest(dispatcher) {
        val vm = SetupViewModel(Gateway()); runCurrent()
        assertEquals(SetupPage.FORM, vm.state.value.page)
        assertEquals("", vm.state.value.amount); assertNull(vm.state.value.day)
        vm.continueToDay(); assertTrue(vm.state.value.amountError)
        listOf("0", "000", "-1", "1.5", "abc", "1", "99999", "099999").forEach { vm.amountChanged(it); vm.continueToDay(); assertTrue(vm.state.value.amountError) }
        vm.amountChanged("100000"); vm.continueToDay(); assertFalse(vm.state.value.amountError); vm.back()
        val large = "9".repeat(80)
        vm.amountChanged(large); vm.continueToDay(); vm.dayChanged(31); vm.back()
        assertEquals(large, vm.state.value.amount); assertEquals(31, vm.state.value.day)
        vm.dayChanged(32); assertEquals(31, vm.state.value.day)
    }
    @Test fun minimumBudgetEnforcedAt100000Vnd() = runTest(dispatcher) {
        val vm = SetupViewModel(Gateway()); runCurrent()
        listOf("0", "99999", "0099999", "1", "50000").forEach { amount ->
            vm.amountChanged(amount)
            vm.continueToDay()
            assertTrue("Expected amountError for $amount", vm.state.value.amountError)
            assertEquals(1, vm.state.value.step)
        }
        vm.amountChanged("100000")
        vm.continueToDay()
        assertFalse(vm.state.value.amountError)
        assertEquals(2, vm.state.value.step)
        vm.back()

        vm.amountChanged("100.000")
        vm.continueToDay()
        assertFalse(vm.state.value.amountError)
        assertEquals(2, vm.state.value.step)
    }
    @Test fun requiresExplicitDayAndUsesIanaDeviceZone() = runTest(dispatcher) {
        val gw = Gateway(); val vm = SetupViewModel(gw) { "Asia/Ho_Chi_Minh" }; runCurrent()
        vm.amountChanged("3.000.000"); vm.continueToDay(); vm.save(); runCurrent()
        assertTrue(vm.state.value.dayError); assertTrue(gw.submitted.isEmpty())
        vm.dayChanged(31); vm.save(); runCurrent()
        assertEquals(SetupDraft("3000000", 31, "Asia/Ho_Chi_Minh"), gw.submitted.single())
        assertEquals(SetupPage.FORM, vm.state.value.page) // accepted alone never grants Home
        assertFalse(vm.state.value.needsReconciliation)
    }
    @Test fun onlyServerConfirmationGrantsHomeAndDoubleSubmitIsBlocked() = runTest(dispatcher) {
        val gw = Gateway(); val blocker = CompletableDeferred<Unit>(); gw.beforeSave = { blocker.await() }
        val vm = SetupViewModel(gw) { "Asia/Ho_Chi_Minh" }; runCurrent()
        vm.amountChanged("100000"); vm.dayChanged(1); vm.save(); vm.save(); runCurrent()
        assertEquals(1,gw.submitted.size); assertTrue(vm.state.value.busy)
        vm.amountChanged("200000"); vm.dayChanged(5); vm.back()
        assertEquals("100000",vm.state.value.amount); assertEquals(1,vm.state.value.day)
        gw.status = SetupStatus.COMPLETE; blocker.complete(Unit); runCurrent()
        assertEquals(SetupPage.COMPLETE,vm.state.value.page)
    }
    @Test fun uncertainSaveReconcilesBeforeAnotherSubmission() = runTest(dispatcher) {
        val gw = Gateway(); val vm = SetupViewModel(gw) { "Asia/Ho_Chi_Minh" }; runCurrent()
        vm.amountChanged("500000"); vm.dayChanged(3); gw.status = SetupStatus.UNKNOWN
        vm.save(); runCurrent(); assertTrue(vm.state.value.needsReconciliation)
        vm.save(); runCurrent(); assertEquals(1, gw.submitted.size)
        gw.status = SetupStatus.INCOMPLETE; vm.save(); runCurrent()
        assertFalse(vm.state.value.needsReconciliation); assertEquals(1,gw.submitted.size)
        vm.save(); runCurrent(); assertEquals(2,gw.submitted.size)
    }
    @Test fun unconfiguredOfflineAndExceptionsNeverCompleteOrEraseDraft() = runTest(dispatcher) {
        val closed = SetupViewModel()
        assertEquals(SetupPage.FORM, closed.state.value.page)
        closed.checkStatus(); runCurrent(); assertEquals(SetupProblem.UNAVAILABLE,closed.state.value.problem)
        val gw = Gateway(); val vm = SetupViewModel(gw) { "Asia/Ho_Chi_Minh" }; runCurrent()
        vm.amountChanged("500000"); vm.dayChanged(30); gw.response = SetupSubmission.OFFLINE
        vm.save(); runCurrent(); assertEquals(SetupProblem.OFFLINE,vm.state.value.problem)
        assertTrue(vm.state.value.showErrorDialog)
        vm.dismissErrorDialog()
        assertFalse(vm.state.value.showErrorDialog)
        assertEquals("500000",vm.state.value.amount); assertEquals(30,vm.state.value.day)
        gw.beforeSave = { throw IllegalStateException("private diagnostic") }; gw.status=
        SetupStatus.UNKNOWN
        vm.save(); runCurrent(); assertEquals(SetupProblem.UNKNOWN,vm.state.value.problem)
        assertTrue(vm.state.value.showErrorDialog)
        assertTrue(vm.state.value.needsReconciliation)
        vm.discard(); assertEquals("",vm.state.value.amount); assertNull(vm.state.value.day)
    }
    @Test fun cancelledAccountCannotAcceptALateCompleteStatus() = runTest(dispatcher) {
        val result = CompletableDeferred<SetupStatus>()
        val gw = object : SetupGateway {
            override suspend fun checkStatus() = withContext(NonCancellable) { result.await() }
            override suspend fun submit(draft: SetupDraft) = SetupSubmission.UNKNOWN
        }
        val vm = SetupViewModel(gw); runCurrent(); vm.checkStatus(); vm.discard()
        result.complete(SetupStatus.COMPLETE); runCurrent()
        assertEquals(SetupPage.FORM, vm.state.value.page)
        assertEquals("", vm.state.value.amount)
    }

}
