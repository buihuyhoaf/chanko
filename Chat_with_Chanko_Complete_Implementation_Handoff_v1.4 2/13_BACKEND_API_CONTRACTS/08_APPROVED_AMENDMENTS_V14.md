# Approved amendment addendum — hiệu lực v1.4

**APPROVED BY DIRECT USER INSTRUCTION.** [Source approval](../14_APPROVALS_AND_RECOVERY/USER_TECHNICAL_APPROVAL_V14.md). A2/A4 amendment thắng các câu immutable-context/new-thread và offline-read-TTL cutoff trong đề xuấtv1.3. A1/A3/A5 giữ nguyên các invariant đã approve. Không sửa SPEC/TECH source files hoặc thêm tính năng streaming.

## 1. Free→Item cùng thread, có xác nhận

- User chọn **item đã tồn tại** trong owner scope, xem tên/item identity và xác nhận rõ “Chuyển cuộc trò chuyện này sang món đồ này; giữ toàn bộ tin nhắn”. Tap row mới là selection, chưa mutate. Cancel không có effect.
- Logical action `BindExistingItem` trên threadID hiện tại. Reference API: POST `/api/v1/ai/conversations/{thread_id}/bind-item`. Body gồm item_id, item_version, confirmed=true; If-Match thread version +Idempotency-Key +X-Client-Operation-Id. confirmed là dấu explicit intent, không thay auth/owner/capability validation.
- Chỉ ACTIVE FREE có thể bind. Server kiểm owner thread/item, item read/context permission, availability/capability/version (không yêu cầu item-write hoặc tạo item); item archived/read-only xử lý theo item policy, không auto restore. Không tạo hoặc sửa ShoppingItem.
- Transaction ngắn: replay idempotency trước so current version; lock thread →check If-Match/context/capabilities/no active AI op →validate item/version →set context_kind ITEM/item_id →tăng thread version/context_revision →ghi bound_at/bound_after_sequence và operation result atomically. Không gọi LLM trong transaction hoặc do bind.
- Giữ **cùng conversation/thread ID**, mọi message ID/role/text/sequence/timestamp, operation/evaluation IDs đã có và draft theo owner+thread. Không copy/clone/merge sang thread item khác, kể cả item đó có active thread khác. Direct Item Detail entry ngoài conversion vẫn có reuse/new policy đã approve.
- FREE→ITEM là transition một chiều trong scope hiện tại. Item→item khác hoặc unbind không được thực hiện ngầm; không được dùng amendment này để reassign official history. Unsupported action trả conflict, không lựa chọn thay user.
- Lưu context provenance: context_revision bắt đầu0 cho FREE, lên1 khi bind; messages/operations giữ context revision lúc tạo (logical backfill metadata0 cho lịch sử FREE, không sửa nội dung). bound_after_sequence là frontier trước bind. Các tin trước bind vẫn đọc trong transcript đầy đủ nhưng không bị diễn giải thành purchase/official decision/evaluation cho item.
- Chỉ một evaluate intent hợp lệ **sau bind** mới có thể tạo EvaluationRecord cho item. Không backfill official records từ câu “đã mua”, “skip” hoặc verdict prose trong lịch sử FREE. Transcript cũ được giữ như dialogue history, không tự coi là facts của item mới; structured snapshot phải là facts có nguồn của context đánh giá.
- Empty chưa có server thread: lựa chọn/xác nhận existing item đặt unsent ITEM context; first send tạo ITEM thread theo create contract, không tạo FREE thread giả chỉ để bind. Với thread FREE đã có ID thì bắt buộc giữ đúng ID.

## 2. Idempotency, concurrency và multi-device cho bind

| Case | Kết quả có thẩm quyền |
|---|---|
| Double tap /response mất | Replay cùng bind key+payload/client ID; một context transition, một context revision increment; lookup trước key mới |
| Same key, khác item/version/confirmation payload |409 IDEMPOTENCY_CONFLICT; không rebind |
| Hai devices bind hai items trên cùng version FREE | Một commit thắng; loser409 VERSION_CONFLICT/CONTEXT_CHANGED, reload cùng thread; không client-clock winner |
| Đã ITEM, command mới khác key |409 ALREADY_ITEM_BOUND; original command replay vẫn canonical success, không làm transition lại |
| FREE AI op RUNNING/PENDING hoặc UNKNOWN chưa reconcile |409 OPERATION_IN_PROGRESS/khóa confirm; đợi/reconcile rồi user xác nhận lại với version mới. Không cancel hoặc reinterpret output để bind ngầm |
| Send/evaluate từ máy đang giữ FREE revision cũ sau bind | Reject trước provider/admission với version/context conflict; refresh projection cùng thread, không auto resend |
| Retryable FREE operation cũ sau bind | Không resume ở ITEM context; CONTEXT_CHANGED. Historical SUCCEEDED operation có thể lookup/replay như history rev0, không gọi AI hoặc tạo evaluation mới |
| Delete/archive commit trước bind |404/409 theo owner/lifecycle; không resurrect hoặc auto restore |
| Bind commit trước delete/archive | Version/lifecycle guards của action tiếp theo; chat deletion vẫn giữ official item records độc lập |

Provider finalization và retry kiểm cả lifecycle_epoch **và context_revision**, không chỉ timestamp. Message ordering do sequence server; context bind không append assistant reply giả. Operation key/hash gốc không bị sửa để “fit” context mới.

GET Conversation/cache-validation/OperationView trả context_kind, item_id khi có quyền, context_revision, server version và capabilities hiện hành. Client giữ route/cache/draft identity owner+thread; cập nhật projection/header/context chip sau ack/reconcile, không tạo entry ID mới.

Global filter FREE/ITEM snapshot phải recheck current context membership mỗi page. Thread chuyển ITEM không tiếp tục xuất hiện như FREE ở page mới trả về; next cursor dựa consumed snapshot IDs. Refresh query generation và dedup threadID cho list mới, không copy hai records. Message cursor bound thread+context_revision; cursor cũ sau bind trả400 CURSOR_CONTEXT_CHANGED và reset page cursor, vẫn giữ/merge transcript đã đọc theo stable message IDs. Không mất transcript khi reset pagination.

## 3. Cache freshness không phải offline retention

| Clock/policy | Hiệu lực | Không có hiệu lực gì |
|---|---|---|
| Freshness TTL24h từ last successful fetch | Khi quá hạn, mark STALE và ưu tiên revalidate khi online; hiển thị cached_at | Không xóa/ẩn/chặn transcript offline |
| Offline retention | Không có age-based expiry cho chat data còn nằm trong cache đúng account và chưa known-deleted/unavailable | Không cam kết toàn bộ server history đã download hoặc cache không thể bị user/OS/capacity eviction |
| Server pagination snapshot15 phút | Cursor hết hạn thì reset khi fetch page online | Không làm cached messages đã tải mất quyền đọc offline |
| Operation retry/result window24h | Giới hạn retry/replay có bảo đảm theo A3 | Không phải tuổi thọ transcript/cache |
| Server transcript purge24h/backup30 ngày sau delete | A1 server/storage retention | Không hứa remote offline device nhận deletion tức thì |

Cached retained transcript/list vẫn đọc được offline sau24h,48h hoặc lâu hơn; giữ nhãn dữ liệu đã lưu/stale và timestamp. Chỉ cache chưa có, đã bị clear/evict thật hoặc known-deleted/unavailable mới hiện no-cache/unavailable. Không “cần internet để đọc” chỉ do freshness TTL.

Capacity limits A4 vẫn giữ200 threads/10.000 messages/20MiB với LRU khi cần nạp dữ liệu mới, không TTL cleanup. Protect currently open/read conversation để không tự evict giữa phiên đọc offline; không eviction chỉ vì dữ liệu cũ. User clear cache, OS/app data removal, logout, confirmed lifecycle/owner invalidation và capacity eviction thật là retention boundaries, không thời hạn theo tuổi. Existing Offline Manual Item Draft không bị policy chat xóa.

## 4. Remote deletion, offline reads và server authority

Thiết bị hoàn toàn offline có thể tiếp tục đọc dữ liệu cache cũ cho tới khi biết remote deletion hoặc một retention boundary thực xảy ra; **không còn guarantee chặn đọc sau24h**. A4 amendment supersedes đúng guarantee cũ này, không nới server read permissions/purge của A1.

Khi online/open/foreground/reconnect: validate current thread/context/lifecycle và pending operation IDs trước khi enable actions; known delete/unavailable purge transcript/summary/draft, lưu owner-scoped minimal tombstone để response/page cũ không resurrect cache. Archive→read-only, bind→same thread ITEM projection. Network failure không được coi là404/deleted; giữ stale read access, mutations vẫn guard theo connectivity/current server capability.

Offline vẫn không send/bind/evaluate/archive/delete/restore, không outbox/auto replay. Session draft và minimal pending identity journal theo A4/A5; process death không hứa raw chat draft durable. Local account isolation/known tombstones luôn thắng cached content. Không dùng TTL/cursor hết hạn để fake business mutation hoặc làm thay official DecisionRecords.

## 5. Thẩm quyền và acceptance

A2/A4 semantics ở đây là amendment được user approve; path/DTO là reference implementation contract cho backend/Android mapping, không tuyên bố API đã deploy. A1/A3/A5 approved, REST baseline giữ, optional SSE vẫn excluded. [Amendment acceptance](../10_ACCEPTANCE_QA/AMENDMENT_ACCEPTANCE_V14.md) có positive/negative/race/offline cases cần chạy sau implementation; không có test runtime được giả PASS trong đợt tài liệu này.

## 6. Implementation clarifications của các approved invariants

context_revision khởi tạo0 cho cả direct ITEM và FREE; lưu initial_context_kind để không gọi mọi revision0 là FREE. Bind FREE→ITEM tăng revision, bound_at/frontier xác định provenance; direct ITEM không có bind marker. Message/operation context-kind/revision-at-creation có thể là metadata sidecar/backfill từ known initial context, không sửa text/role/sequence hoặc official records. Không suy context từ nội dung LLM.

Auth/read-only idempotency lookup có thể thực hiện trước; với existing thread, thứ tự **row locks** thống nhất thread→operation/command records (stable order)→short item validation khi cần. Replay decision vẫn xảy ra trước current version/context rejection. Không giữ operation row lock rồi chờ thread theo hướng ngược ở một route khác; không lock DB trong provider call. Create chưa có thread dùng durable unique owner/scope/key admission rồi owner-scoped canonical replay. Đây là cụ thể hóa A3 version/idempotency safety, không thêm side effect.

Known-deletion tombstones của local owner không expire theo freshness TTL; stale response/page từ generation cũ không được resurrect. Khi purge toàn cache/logout, cancel subscriptions/inflight generations trước; response owner/session-generation cũ luôn bị bỏ. Server read authorization/redaction vẫn thắng replay sau delete.

## 7. BindItemResult và offline owner distinction

200 BindItemResult gồm operation_id, thread_id, context_revision_at_bind, resource_available, current_lifecycle và current_conversation khi owner hiện được đọc. Lần bind thành công trả current ITEM conversation cùng threadID. Replay sau archive trả current archived projection; replay sau delete vẫn xác nhận outcome của intent nhưng current_conversation=null/resource_available=false và không raw transcript/title; không restore old cached ACTIVE state. Cross-owner request404. GET Conversation/deletion authorization không bị idempotency replay bypass.

JWT/API token expiration tự nó không xóa retained cache hoặc biến freshness thành retention. Local read của identified same owner khi offline tuân MVP local security; không hứa token đó còn hợp lệ để gọi server. Explicit logout/known owner/session revocation/account switch được xử lý theo security policy và cache isolation; offline đọc cache không tạo auth session/API write mới.
