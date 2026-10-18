# Data Repository Foundation — Phase 3.2

## Boundaries

`domain` owns `ReadRepository`, `ReadState`, `DataFailure`, and the opaque local
`AccountScope`. It remains Kotlin-only. `data` owns source contracts, coordination,
and mapping functions. `network` owns transport results/envelopes. `database` owns
Room entities/DAOs; the existing `database -> domain` enum dependency is preserved.
The only new compile edge is `data -> network` (implementation).
App remains the composition root; no new bindings replace the legacy repository.

`OnlineReadRepository` is a small read-only policy primitive. Each explicit collection
reads one cache snapshot, emits it as potentially stale with its cached timestamp,
then performs one remote fetch. It emits a fresh server value or a typed failure.
Empty lists are valid server snapshots and replace old lists. Null/204 is invalid
for this body-required read contract. There is no polling or reconnect behavior.
A fresh emission means freshly fetched, not guaranteed current forever.

## Source and mapping conventions

Each source instance represents one resource/query/filter key. Never share the same
snapshot slot for different filters/pages. Always include account scope in every
cache query/write. `AccountScope` is a partition key supplied by future session
composition, never a credential or client-provided backend authorization field.
The repository rejects cache records with mismatched scope as defense in depth.
Remote implementations must use the server-authenticated account and verify response
identity/ownership before mapping; no arbitrary owner parameter grants server access.

Mappings are pure named resource functions supplied to the policy:
DTO -> domain -> entity for server refresh; entity -> domain for cache display.
Do not put Room/Retrofit types in domain or presentation contracts. Never map legacy
`CooldownItem` Int IDs/statuses into official server records or purchases implicitly.
Future wire prices use `estimated_unit_price` and `actual_unit_price`, exact integer
VND, no float or rounding; validate numeric range and reject unknown business enum
values instead of defaulting to a real decision. No business DTO/schema is invented here.
Mapping validation uses `IllegalArgumentException`; unexpected programming errors propagate.

Cache adapters normalize expected storage failures to `IOException`; cancellation
must propagate. A broken/unreadable cache does not block a server read. Writes are
atomic snapshot replacement, including timestamp, in the supplied account partition.
Before production wiring, adapters must serialize refreshes or compare server versions
atomically to prevent older concurrent responses overwriting newer state. This primitive
neither invents a version contract nor makes unverified concurrency guarantees.
A cache write failure is exposed alongside fresh data, never as a remote failure.
Downstream Flow exceptions/cancellation are not swallowed. HTTP status, unknown error
code, request ID and Retry-After survive mapping; raw server messages are not UI copy.
No error schedules retry or authorizes a mutation. Failed reads do not replace cache.

## Authority and manual drafts

Official mutations must be separate online operations, successful only after server
confirmation; a timeout can mean an unknown server outcome, never local success.
409/428 require re-read/user review; do not blindly retry. Idempotency and related
cache invalidation require resource-specific reviewed contracts before wiring writes.
No official write API is introduced by this foundation.

`ManualDraftSource` is deliberately a separate local-only contract with scoped
read/save/delete. It has no submit, queue or remote operation. Reconnecting or reading
cache cannot call it. A future durable adapter must preserve drafts and private image
files across process restart/migration; an ephemeral URI is insufficient. Only a
separate user-reviewed and confirmed operation may submit, retaining its idempotency
identity when outcome is uncertain. No draft becomes an official item/budget/purchase.

## Explicit blockers / deferred integration

Current Room v2 `cooldown_items` has legacy IDs/statuses and lacks owner, server ID,
version, cache timestamp and separate drafts. Reusing it as official cache/draft
would violate account isolation and authority. Production Room cache/draft adapters,
image persistence, logout/retention and legacy migration are blocked pending reviewed
schema and data policy. This phase stops that integration; no schema/version/migration
or storage fallback is added. Fake caches in focused tests prove policy, not durability.

No deployed OpenAPI, approved environment, authentication, persistence acceptance,
server version or TTL is claimed. Snapshot TECH-01/02/03 v1.1, final amendments and
ADR-09 are reference evidence, not live backend verification. Existing legacy behavior
continues unchanged until its separately authorized product migration.
