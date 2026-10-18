package com.hoabh.chanko

import com.hoabh.chanko.domain.auth.SessionState
import com.hoabh.chanko.domain.repository.foundation.*
import com.hoabh.chanko.presentation.foundation.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReadPresentationTest {
    private val accountA = AccountScope("a")
    private val accountB = AccountScope("b")

    @Test fun signedOutDoesNotRead() = runTest {
        val repo = FakeRead()
        assertEquals(listOf(ReadUiState.SignedOut), repo.presentationStates(
            flowOf(SessionState.SignedOut), emptyFlow()).toList())
        assertTrue(repo.accounts.isEmpty())
    }

    @Test fun cacheThenFreshKeepsProvenance() = runTest {
        val repo = FakeRead { flow { emit(ReadState.Cached(listOf("old"), 123)); emit(ReadState.Fresh(listOf("new"))) } }
        val states = repo.presentationStates(flowOf(SessionState.Available(accountA)), emptyFlow()).toList()
        assertEquals(ReadUiState.Loading, states.first())
        assertTrue(states.contains(ReadUiState.Content(listOf("old"), ReadSource.CACHE, 123, true)))
        assertEquals(ReadUiState.Content(listOf("new"), ReadSource.SERVER), states.last())
        assertEquals(listOf(accountA), repo.accounts)
    }

    @Test fun failureKeepsCachedValueAndTimestamp() = runTest {
        val repo = FakeRead { flow { emit(ReadState.Cached(listOf("old"), 123)); emit(ReadState.Failed(DataFailure.Connection)) } }
        assertEquals(ReadUiState.Content(listOf("old"), ReadSource.CACHE, 123, error = ReadError.CONNECTION),
            repo.presentationStates(flowOf(SessionState.Available(accountA)), emptyFlow()).last())
    }

    @Test fun emptyServerCollectionIsContentAndCacheFailureIsNonfatal() = runTest {
        val repo = FakeRead { flowOf(ReadState.Fresh(emptyList(), DataFailure.LocalStorage)) }
        assertEquals(ReadUiState.Content(emptyList<String>(), ReadSource.SERVER, error = ReadError.STORAGE),
            repo.presentationStates(flowOf(SessionState.Available(accountA)), emptyFlow()).last())
    }

    @Test fun noCacheFailureIsNotEmptySuccess() = runTest {
        val repo = FakeRead { flowOf(ReadState.Failed(DataFailure.NotConfigured)) }
        assertEquals(ReadUiState.Failed(ReadError.NOT_CONFIGURED),
            repo.presentationStates(flowOf(SessionState.Available(accountA)), emptyFlow()).last())
    }

    @Test fun explicitRefreshRetainsContentWithoutAutomaticRetry() = runTest {
        val refresh = MutableSharedFlow<Unit>()
        var calls = 0
        val repo = FakeRead {
            calls++
            if (calls == 1) flowOf(ReadState.Fresh(listOf("saved")))
            else flow { delay(100); emit(ReadState.Failed(DataFailure.Timeout)) }
        }
        val states = mutableListOf<ReadUiState<List<String>>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.presentationStates(flowOf(SessionState.Available(accountA)), refresh).toList(states)
        }
        runCurrent(); refresh.emit(Unit); runCurrent()
        assertEquals(ReadUiState.Content(listOf("saved"), ReadSource.SERVER, isRefreshing = true), states.last())
        advanceTimeBy(100); runCurrent()
        assertEquals(ReadUiState.Content(listOf("saved"), ReadSource.SERVER, error = ReadError.TIMEOUT), states.last())
        advanceTimeBy(60_000); runCurrent(); assertEquals(2, calls)
    }

    @Test fun accountChangeClearsOldContentAndCancelsOldRead() = runTest {
        val sessions = MutableStateFlow<SessionState>(SessionState.Available(accountA))
        var cancelled = false
        val repo = FakeRead { account -> flow {
            if (account == accountA) {
                emit(ReadState.Cached(listOf("private-a"), 1))
                try { awaitCancellation() } finally { cancelled = true }
            } else { emit(ReadState.Failed(DataFailure.Connection)) }
        } }
        val states = mutableListOf<ReadUiState<List<String>>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.presentationStates(sessions, emptyFlow()).toList(states)
        }
        runCurrent(); val boundary = states.size
        sessions.value = SessionState.Available(accountB); runCurrent()
        assertTrue(cancelled)
        assertEquals(ReadUiState.Loading, states[boundary])
        assertEquals(ReadUiState.Failed(ReadError.CONNECTION), states.last())
        assertFalse(states.drop(boundary).any { it is ReadUiState.Content })
        assertEquals(listOf(accountA, accountB), repo.accounts)
        sessions.value = SessionState.SignedOut; runCurrent()
        assertEquals(ReadUiState.SignedOut, states.last())
    }

    @Test fun logoutCancelsAndReentryStartsWithoutPriorContent() = runTest {
        val sessions = MutableStateFlow<SessionState>(SessionState.Available(accountA))
        var attempts = 0
        var cancelled = false
        val repo = FakeRead { flow {
            attempts++
            if (attempts == 1) {
                emit(ReadState.Fresh(listOf("private")))
                try { awaitCancellation() } finally { cancelled = true }
            } else emit(ReadState.Failed(DataFailure.Connection))
        } }
        val states = mutableListOf<ReadUiState<List<String>>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.presentationStates(sessions, emptyFlow()).toList(states)
        }
        runCurrent(); sessions.value = SessionState.SignedOut; runCurrent()
        assertTrue(cancelled); assertEquals(ReadUiState.SignedOut, states.last())
        sessions.value = SessionState.Available(accountA); runCurrent()
        assertEquals(ReadUiState.Failed(ReadError.CONNECTION), states.last())
        assertEquals(2, attempts)
    }

    @Test fun latestExplicitRefreshCancelsPreviousRead() = runTest {
        val refresh = MutableSharedFlow<Unit>()
        var cancelled = false
        var calls = 0
        val repo = FakeRead { flow {
            calls++
            if (calls == 1) {
                try { awaitCancellation() } finally { cancelled = true }
            } else emit(ReadState.Fresh(listOf("latest")))
        } }
        val states = mutableListOf<ReadUiState<List<String>>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.presentationStates(flowOf(SessionState.Available(accountA)), refresh).toList(states)
        }
        runCurrent(); refresh.emit(Unit); runCurrent()
        assertTrue(cancelled)
        assertEquals(ReadUiState.Content(listOf("latest"), ReadSource.SERVER), states.last())
    }

    @Test fun cancellationIsNotMappedToErrorAndExceptionsAreSanitized() = runTest {
        val repo = FakeRead { flow { throw CancellationException("cancel") } }
        val cancelledStates = repo.presentationStates(
            flowOf(SessionState.Available(accountA)), emptyFlow()).toList()
        assertTrue(cancelledStates.all { it == ReadUiState.Loading })
        val broken = FakeRead { throw IllegalStateException("sensitive-token") }
        val result = broken.presentationStates(flowOf(SessionState.Available(accountA)), emptyFlow()).last()
        assertEquals(ReadUiState.Failed(ReadError.UNKNOWN), result)
        assertFalse(result.toString().contains("sensitive-token"))
    }

    @Test fun incompleteReadDoesNotLeaveInfiniteSpinner() = runTest {
        val empty = FakeRead { emptyFlow() }
        assertEquals(ReadUiState.Failed(ReadError.INVALID_DATA),
            empty.presentationStates(flowOf(SessionState.Available(accountA)), emptyFlow()).last())
        val cachedOnly = FakeRead { flowOf(ReadState.Cached(listOf("old"), 1)) }
        assertEquals(ReadUiState.Content(listOf("old"), ReadSource.CACHE, 1, error = ReadError.INVALID_DATA),
            cachedOnly.presentationStates(flowOf(SessionState.Available(accountA)), emptyFlow()).last())
    }

    @Test fun allFailuresMapWithoutServerMetadataOrImplicitRetry() {
        assertEquals(ReadError.CONNECTION, DataFailure.Connection.toReadError())
        assertEquals(ReadError.TIMEOUT, DataFailure.Timeout.toReadError())
        assertEquals(ReadError.STORAGE, DataFailure.LocalStorage.toReadError())
        assertEquals(ReadError.INVALID_DATA, DataFailure.InvalidResponse.toReadError())
        assertEquals(ReadError.NOT_CONFIGURED, DataFailure.NotConfigured.toReadError())
        mapOf(401 to ReadError.SIGN_IN_REQUIRED, 403 to ReadError.FORBIDDEN,
            409 to ReadError.CONFLICT, 429 to ReadError.RATE_LIMITED,
            500 to ReadError.SERVER, 599 to ReadError.SERVER, 400 to ReadError.UNKNOWN,
            404 to ReadError.UNKNOWN).forEach { (status, expected) ->
            assertEquals(expected, DataFailure.Http(status, "secret", "secret", "secret").toReadError())
        }
    }
}

private class FakeRead(
    private val source: (AccountScope) -> Flow<ReadState<List<String>>> = { emptyFlow() },
) : ReadRepository<List<String>> {
    val accounts = mutableListOf<AccountScope>()
    override fun read(scope: AccountScope): Flow<ReadState<List<String>>> {
        accounts += scope
        return source(scope)
    }
}
