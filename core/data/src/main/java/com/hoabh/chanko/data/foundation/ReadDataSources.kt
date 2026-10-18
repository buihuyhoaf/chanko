package com.hoabh.chanko.data.foundation

import com.hoabh.chanko.core.network.NetworkResult
import com.hoabh.chanko.domain.repository.foundation.AccountScope

/** DTO ownership stays in the transport/data boundary, never in domain. */
fun interface RemoteReadSource<Dto> {
    suspend fun fetch(scope: AccountScope): NetworkResult<Dto>
}

data class CacheRecord<Entity>(val scope: AccountScope, val value: Entity,
                               val cachedAtEpochMillis: Long)

interface ReadCacheSource<Entity> {
    /** Query must include account and the resource/filter key captured by this source. */
    suspend fun read(scope: AccountScope): CacheRecord<Entity>?
    /** Atomically replace this snapshot, including an empty server list. Never merge drafts.
     * Implementations must serialize refreshes or reject older server versions; no stale overwrite.
     */
    suspend fun replace(record: CacheRecord<Entity>)
}

/** Separate local-only storage: intentionally no remote dependency or submit method.
 * A future durable adapter must preserve drafts and private image files across restart/migration.
 * Reads/writes/deletes must include account scope. Retention/logout policy requires review.
 */
interface ManualDraftSource<Draft> {
    suspend fun read(scope: AccountScope, localDraftId: String): Draft?
    suspend fun save(scope: AccountScope, localDraftId: String, draft: Draft)
    suspend fun delete(scope: AccountScope, localDraftId: String)
}
