package com.hoabh.chanko.presentation.foundation

import com.hoabh.chanko.domain.auth.SessionState
import com.hoabh.chanko.domain.repository.foundation.DataFailure
import com.hoabh.chanko.domain.repository.foundation.ReadRepository
import com.hoabh.chanko.domain.repository.foundation.ReadState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import kotlin.coroutines.cancellation.CancellationException

/** Presentation categories only. Never expose exception text, server messages or request metadata. */
enum class ReadError {
    CONNECTION, TIMEOUT, NOT_CONFIGURED, INVALID_DATA, STORAGE,
    SIGN_IN_REQUIRED, FORBIDDEN, CONFLICT, RATE_LIMITED, SERVER, UNKNOWN,
}

fun DataFailure.toReadError(): ReadError = when (this) {
    DataFailure.Connection -> ReadError.CONNECTION
    DataFailure.Timeout -> ReadError.TIMEOUT
    DataFailure.NotConfigured -> ReadError.NOT_CONFIGURED
    DataFailure.InvalidResponse -> ReadError.INVALID_DATA
    DataFailure.LocalStorage -> ReadError.STORAGE
    is DataFailure.Http -> when (status) {
        401 -> ReadError.SIGN_IN_REQUIRED
        403 -> ReadError.FORBIDDEN
        409 -> ReadError.CONFLICT
        429 -> ReadError.RATE_LIMITED
        in 500..599 -> ReadError.SERVER
        else -> ReadError.UNKNOWN
    }
}

enum class ReadSource { CACHE, SERVER }
sealed interface ReadUiState<out T> {
    data object SignedOut : ReadUiState<Nothing>
    data object Loading : ReadUiState<Nothing>
    data class Content<T>(
        val value: T,
        val source: ReadSource,
        val cachedAtEpochMillis: Long? = null,
        val isRefreshing: Boolean = false,
        val error: ReadError? = null,
    ) : ReadUiState<T>
    data class Failed(val error: ReadError) : ReadUiState<Nothing>
}

/**
 * Cold, read-only presentation adapter for a ViewModel-owned collection.
 * Starts one read per account and per explicit refresh; latest refresh cancels the prior read.
 * Same-account content survives refresh failures; account changes discard it before reading.
 * No timers, connectivity retries, automatic draft submission, saved credentials or mutations.
 * Collect once in viewModelScope; expose via StateFlow and collect with lifecycle in Compose.
 * Empty collections remain Content: only a feature can decide what "empty" means.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> ReadRepository<T>.presentationStates(
    sessions: Flow<SessionState>,
    refreshRequests: Flow<Unit>,
): Flow<ReadUiState<T>> = sessions
    .map { (it as? SessionState.Available)?.account }
    .distinctUntilChanged()
    .flatMapLatest { account ->
        if (account == null) flowOf<ReadUiState<T>>(ReadUiState.SignedOut)
        else flow<ReadUiState<T>> {
            var content: ReadUiState.Content<T>? = null
            emit(ReadUiState.Loading)
            refreshRequests.onStart { emit(Unit) }.transformLatest<Unit, ReadUiState<T>> {
                emit(content?.copy(isRefreshing = true, error = null) ?: ReadUiState.Loading)
                var terminal = false
                suspend fun fail(error: ReadError) {
                    content = content?.copy(isRefreshing = false, error = error)
                    emit(content ?: ReadUiState.Failed(error))
                }
                try {
                    read(account).collect { result ->
                        // A cancelled/non-cooperative source must not publish late data.
                        currentCoroutineContext().ensureActive()
                        when (result) {
                            is ReadState.Cached -> {
                                val newContent = ReadUiState.Content(result.value, ReadSource.CACHE,
                                    result.cachedAtEpochMillis, isRefreshing = true)
                                content = newContent
                                emit(newContent)
                            }
                            is ReadState.Fresh -> {
                                terminal = true
                                val newContent = ReadUiState.Content(result.value, ReadSource.SERVER,
                                    error = result.cacheFailure?.toReadError())
                                content = newContent
                                emit(newContent)
                            }
                            is ReadState.Failed -> { terminal = true; fail(result.failure.toReadError()) }
                        }
                    }
                    if (!terminal) fail(ReadError.INVALID_DATA)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    fail(ReadError.UNKNOWN)
                }
            }.collect { emit(it) }
        }
    }
