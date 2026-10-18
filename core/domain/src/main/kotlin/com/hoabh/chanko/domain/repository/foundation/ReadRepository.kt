package com.hoabh.chanko.domain.repository.foundation

import kotlinx.coroutines.flow.Flow

/** Local partition key, supplied by the future session composition root; never authorization. */
@JvmInline
value class AccountScope(val value: String) {
    init { require(value.isNotBlank()) }
}

sealed interface DataFailure {
    data object Connection : DataFailure
    data object Timeout : DataFailure
    data object NotConfigured : DataFailure
    data object InvalidResponse : DataFailure
    data object LocalStorage : DataFailure
    /** Diagnostic metadata only: no server message is exposed as user-facing copy. */
    data class Http(val status: Int, val code: String?, val requestId: String?,
                    val retryAfter: String?) : DataFailure
}

/** Cache is always potentially stale; no invented TTL or offline business authority. */
sealed interface ReadState<out T> {
    data class Cached<T>(val value: T, val cachedAtEpochMillis: Long) : ReadState<T>
    data class Fresh<T>(val value: T, val cacheFailure: DataFailure? = null) : ReadState<T>
    /** A previous Cached emission remains displayable; failure is never an empty success. */
    data class Failed(val failure: DataFailure) : ReadState<Nothing>
}

interface ReadRepository<T> {
    /** One explicit refresh per collection. No timer, reconnect listener, retry or mutation. */
    fun read(scope: AccountScope): Flow<ReadState<T>>
}
