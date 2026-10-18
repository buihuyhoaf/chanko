package com.hoabh.chanko.data.foundation

import com.hoabh.chanko.core.network.ApiError
import com.hoabh.chanko.core.network.NetworkResult
import com.hoabh.chanko.domain.repository.foundation.AccountScope
import com.hoabh.chanko.domain.repository.foundation.DataFailure
import com.hoabh.chanko.domain.repository.foundation.ReadState
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class OnlineReadRepositoryTest {
    private val account = AccountScope("a")
    private class Cache : ReadCacheSource<List<Int>> {
        var record: CacheRecord<List<Int>>? = null
        var readFails = false
        var writeFails = false
        var writes = 0
        override suspend fun read(scope: AccountScope): CacheRecord<List<Int>>? {
            if (readFails) throw IOException()
            return record
        }
        override suspend fun replace(record: CacheRecord<List<Int>>) {
            if (writeFails) throw IOException()
            writes++
            this.record = record
        }
    }
    private fun repo(cache: Cache, fetch: suspend () -> NetworkResult<List<String>>) =
        OnlineReadRepository(
            RemoteReadSource<List<String>> { fetch() }, cache,
            dtoToDomain = { dto -> dto.map { it.toInt().also { n -> require(n >= 0) } } },
            domainToEntity = { it: List<Int> -> it },
            entityToDomain = { it: List<Int> -> it.also { require(it.all { n -> n >= 0 }) } },
            nowEpochMillis = { 123L },
        )

    @Test fun `cache emits before remote and fresh snapshot replaces it`() = runTest {
        val cache = Cache().apply { record = CacheRecord(account, listOf(1), 10) }
        val states = repo(cache) { NetworkResult.Success(listOf("2"), 200) }.read(account).toList()
        assertEquals(listOf(ReadState.Cached(listOf(1), 10), ReadState.Fresh(listOf(2))), states)
        assertEquals(CacheRecord(account, listOf(2), 123), cache.record)
        assertEquals(1, cache.writes)
    }
    @Test fun `empty server list replaces stale cache`() = runTest {
        val cache = Cache().apply { record = CacheRecord(account, listOf(1), 10) }
        repo(cache) { NetworkResult.Success(emptyList(), 200) }.read(account).toList()
        assertEquals(emptyList<Int>(), cache.record?.value)
    }
    @Test fun `all network failures preserve cached snapshot and never write or retry`() = runTest {
        val cases = listOf(
            NetworkResult.ConnectionFailure to DataFailure.Connection,
            NetworkResult.Timeout to DataFailure.Timeout,
            NetworkResult.NotConfigured to DataFailure.NotConfigured,
            NetworkResult.InvalidResponse to DataFailure.InvalidResponse,
            NetworkResult.HttpFailure(409, ApiError("VERSION_CONFLICT", "untrusted"), "r", "30") to
                DataFailure.Http(409, "VERSION_CONFLICT", "r", "30"),
            NetworkResult.HttpFailure(401, null, null, null) to DataFailure.Http(401, null, null, null),
        )
        for ((network, failure) in cases) {
            val cache = Cache().apply { record = CacheRecord(account, listOf(1), 10) }
            var calls = 0
            val states = repo(cache) { calls++; network }.read(account).toList()
            assertEquals(listOf(ReadState.Cached(listOf(1), 10), ReadState.Failed(failure)), states)
            assertEquals(0, cache.writes)
            assertEquals(1, calls)
        }
    }
    @Test fun `cache miss and offline is failure rather than empty success`() = runTest {
        assertEquals(listOf(ReadState.Failed(DataFailure.Connection)),
            repo(Cache()) { NetworkResult.ConnectionFailure }.read(account).toList())
    }
    @Test fun `mismatched account cache never escapes`() = runTest {
        val cache = Cache().apply { record = CacheRecord(AccountScope("b"), listOf(9), 10) }
        val states = repo(cache) { NetworkResult.Timeout }.read(account).toList()
        assertEquals(listOf(ReadState.Failed(DataFailure.Timeout)), states)
    }
    @Test fun `invalid wire data and absent bodies never overwrite cache`() = runTest {
        for (response in listOf(NetworkResult.Success(listOf("bad"), 200),
            NetworkResult.Success(listOf("-1"), 200), NetworkResult.Success(null, 204))) {
            val cache = Cache()
            assertEquals(listOf(ReadState.Failed(DataFailure.InvalidResponse)),
                repo(cache) { response }.read(account).toList())
            assertEquals(0, cache.writes)
        }
    }
    @Test fun `cache IO or invalid entity does not prevent server read`() = runTest {
        for (cache in listOf(Cache().apply { readFails = true },
            Cache().apply { record = CacheRecord(account, listOf(-1), 10) })) {
            assertEquals(listOf(ReadState.Fresh(listOf(2))),
                repo(cache) { NetworkResult.Success(listOf("2"), 200) }.read(account).toList())
        }
    }
    @Test fun `cache write failure retains fresh server result with diagnostic`() = runTest {
        val cache = Cache().apply { writeFails = true }
        assertEquals(listOf(ReadState.Fresh(listOf(2), DataFailure.LocalStorage)),
            repo(cache) { NetworkResult.Success(listOf("2"), 200) }.read(account).toList())
    }
    @Test fun `cancellation propagates without retry or cache write`() = runTest {
        val cache = Cache()
        val cancelled = CancellationException("stop")
        try {
            repo(cache) { throw cancelled }.read(account).toList()
            fail("Cancellation swallowed")
        } catch (e: CancellationException) { assertSame(cancelled, e) }
        assertEquals(0, cache.writes)
    }
    @Test fun `repository is cold and each explicit collection performs one read`() = runTest {
        val cache = Cache()
        var calls = 0
        val flow = repo(cache) { calls++; NetworkResult.Success(listOf("1"), 200) }.read(account)
        assertEquals(0, calls)
        flow.toList()
        flow.toList()
        assertEquals(2, calls)
    }
}
