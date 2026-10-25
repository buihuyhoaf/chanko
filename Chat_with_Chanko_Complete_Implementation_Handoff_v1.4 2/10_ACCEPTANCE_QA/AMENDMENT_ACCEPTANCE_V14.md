# A2/A4 amendment acceptance — runtime plan, NOT RUN

User approvals được ghi riêng trong14; tests này không phải yêu cầu approve lại. Tất cả rows hiện NOT RUN vì nhiệm vụ chỉ sửa documents/assets, không application code. Dùng backend/app revision1.4 khi implementation và lưu actual evidence.

| Case | Setup/action | Expected invariant / evidence |
|---|---|---|
| AM14-01 | FREE có nhiều messages/draft →select existing item→confirm→ack | Same threadID và mọi message ID/text/role/sequence/timestamp, draft unchanged; context ITEM/itemID đúng, context_revision+1, no new ShoppingItem/thread/official record |
| AM14-02 | Select row rồi cancel/Back; hoặc item owner/version invalid | Không context mutation khi chưa confirm; cancel FREE; invalid owner404/stale409, không chọn item khác hoặc auto create |
| AM14-03 | Double tap confirm; timeout/lost ack; replay same key/payload | Một bind operation/transition/revision increment; lookup/replay canonical result, no clone/duplicate messages |
| AM14-04 | Same bind key, different item/item version/payload |409 IDEMPOTENCY_CONFLICT, no second effect; không đổi payload để retry key cũ |
| AM14-05 | Devices A/B bind hai items với cùng FREE thread version | Một winner bằng DB/version, loser409/refetch sameID; no rebind/clock winner, old page/response không overwrite current context |
| AM14-06 | Pending FREE provider request/UnknownOutcome, user tries bind | Disabled/rejected409, reconcile trước confirm mới; không cancel/reinterpret old response thành ITEM advice |
| AM14-07 | Bind xong; old FREE RETRYABLE_FAILURE/late finalization/SUCCEEDED replay arrives | Retry old context409, late output fence context_revision/epoch; historical success retained rev0, no new post-bind evaluation or duplicate bubble |
| AM14-08 | Cached transcript48h+ cùng account, offline, no known tombstone/logout | Read full retained data with STALE/cached_at; no read denial/purge merely freshnessTTL/JWT age; no writes/AI/bind/outbox |
| AM14-09 | Freshness24h expired while online; refresh fails then offline | Attempt revalidate when online; failed fetch preserves stale readable cache, không fake404/no-cache/account-empty |
| AM14-10 | Cursor snapshot15m/retry window24h expire with cached messages | Reset online cursor/disable unsupported retry appropriately, cached messages vẫn đọc offline, không retention coupling |
| AM14-11 | Device B deletes/archive/binds; A offline rồi reconnect | Before reconnect retained stale read. On current validation: delete purge chat+durable tombstone/no resurrect; archive readonly; bind update sameID ITEM projection; no auto resend/restore |
| AM14-12 | Capacity pressure/open transcript/logout/account switch | No age-only eviction; LRU capacity bounds, protect open conversation; true eviction no-cache honest; owner separation/logout purge, item manual drafts preserved |
| AM14-13 | Bind→valid explicit post-bind evaluate→delete chat; compare official histories | No backfill from free prose; official Decision/Evaluation/Purchase IDs/item links/values unchanged by delete; independent item readers still succeed, spent/status/cooling-off unchanged |
| AM14-14 | Bind race with delete/archive, both commit orders | Lifecycle/version serialization; no resurrect/no implicit restore, replay redacted when deleted; result resource availability/current lifecycle correctly reported |
| AM14-15 | Filter FREE/ITEM snapshots +message cursor issued before remote bind | Current membership rechecked, no duplicate history row; message CURSOR_CONTEXT_CHANGED resets cursor without losing existing transcript; owner+thread cache stable |
| AM14-16 | Original successful bind replay after thread delete/archive | Same operation outcome/ref IDs, no new bind. current_conversation absent/redacted when deleted; current lifecycle/version wins old projections, no raw transcript exposure |

Record IDs/counts before/after and safe error/state evidence, not secrets/raw private transcripts in logs. UI reference-file SHA/contrast checks do not substitute these runtime results. Compare34 legacy IDs only when original body exists; current approval does not create that missing evidence.
