package com.hoabh.chanko.data.foundation

import com.hoabh.chanko.core.network.NetworkResult
import com.hoabh.chanko.domain.repository.foundation.AccountScope
import com.hoabh.chanko.domain.repository.foundation.DataFailure
import com.hoabh.chanko.domain.repository.foundation.ReadRepository
import com.hoabh.chanko.domain.repository.foundation.ReadState
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Read-only policy primitive, not a sync engine. Not bound to legacy ItemRepository.
 * Sources/mappers are resource-specific. Mapping must be pure and validate wire values.
 * Null success bodies are invalid for this snapshot contract (including 204).
 */
class OnlineReadRepository<Dto : Any, Entity : Any, Model : Any>(
    private val remote: RemoteReadSource<Dto>,
    private val cache: ReadCacheSource<Entity>,
    private val dtoToDomain: (Dto) -> Model,
    private val domainToEntity: (Model) -> Entity,
    private val entityToDomain: (Entity) -> Model,
    private val nowEpochMillis: () -> Long,
) : ReadRepository<Model> {
    override fun read(scope: AccountScope): Flow<ReadState<Model>> = flow {
        val cached = try { cache.read(scope) } catch (_: IOException) { null }
        if (cached != null && cached.scope == scope) {
            val model = try { entityToDomain(cached.value) }
                catch (_: IllegalArgumentException) { null }
            if (model != null) emit(ReadState.Cached(model, cached.cachedAtEpochMillis))
        }
        when (val result = remote.fetch(scope)) {
            is NetworkResult.Success -> {
                val model = try { result.body?.let(dtoToDomain) }
                    catch (_: IllegalArgumentException) { null }
                if (model == null) {
                    emit(ReadState.Failed(DataFailure.InvalidResponse))
                } else {
                    val cacheFailure = try {
                        cache.replace(CacheRecord(scope, domainToEntity(model), nowEpochMillis()))
                        null
                    } catch (_: IOException) { DataFailure.LocalStorage }
                      catch (_: IllegalArgumentException) { DataFailure.LocalStorage }
                    // A local cache failure cannot undo a successful server read.
                    emit(ReadState.Fresh(model, cacheFailure))
                }
            }
            else -> emit(ReadState.Failed(result.toDataFailure()))
        }
    }
}

internal fun NetworkResult<*>.toDataFailure(): DataFailure = when (this) {
    NetworkResult.ConnectionFailure -> DataFailure.Connection
    NetworkResult.Timeout -> DataFailure.Timeout
    NetworkResult.NotConfigured -> DataFailure.NotConfigured
    NetworkResult.InvalidResponse -> DataFailure.InvalidResponse
    is NetworkResult.HttpFailure -> DataFailure.Http(status, error?.code, requestId, retryAfter)
    is NetworkResult.Success -> error("Success has no failure mapping")
}
