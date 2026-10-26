# TECH boundary, edge cases và provenance
Không sửa baseline TECH-02/03 trên Drive. Handoff v1.4 có approved extension riêng; dùng addendum v1.4 trước các câu context/cache cũ. Không lấy mockup làm schema/API contract.

## Ranh giới domain
PostgreSQL/backend là authority. Room là cache đọc; chat không tạo general-purpose outbox. Android ChatRepository không gọi LLM provider trực tiếp, không chứa secret/provider key. Transport REST theo TECH-03; không thêm SSE vì mockup có chat. Fake repository và production adapter cùng logical seam, demo luôn có label “Bản mẫu · Phản hồi dựng sẵn”. Fake success không được trình bày là cloud success.

AI verdict BUY/WAIT/SKIP, EvaluationRecord, user DecisionRecord, ItemStatus và Purchase là các khái niệm riêng. Structured evaluate hoàn tất hợp lệ có thể lưu EvaluationRecord/message qua server. Render/tap card không tạo evaluation mới; prose “tôi mua rồi” không set PURCHASED, không decision/purchase/budget/cooling-off mutation. Purchase/status chỉ qua flow user confirm ở Item Detail hiện hữu. Xóa/archive chat không cascade hoặc đổi official decisions/evaluations/purchases; history reader của item độc lập chat route.

## Free → Item: same thread sau confirmation
Chỉ ACTIVE FREE có quyền mới bind existing owner-scoped item. Item selection không tạo/sửa ShoppingItem. Confirm command giữ threadId, message IDs/text/roles/sequence/timestamps, evaluation/operation history và draft keyed owner+thread. Không clone/merge/reuse thread khác kể cả item đã có thread. Direct Item Detail entry có reuse/new policy riêng, không áp vào conversion.

Giữ initial_context_kind và context_revision_at_creation, bound_at/bound_after_sequence. Rev0 không mặc định FREE vì direct ITEM cũng rev0. Bind tăng context_revision; pre-bind FREE text chỉ là transcript cũ, không official item facts/records. Không tự evaluate khi bind. ITEM→ITEM khác/unbind ngoài scope; không thay ngầm theo tên. Chưa có server thread ở CD unsent thì selected ITEM là context local, first send create ITEM; không tạo FREE giả để bind.

Hai devices bind hai items cùng version: một thắng, bên kia conflict→reload same thread, user xem lại; không client-clock last-write-wins. Operation FREE đang pending/unknown chặn bind. Old FREE retry sau bind không được chạy ở ITEM; context conflict không auto resend. Cursor cũ sau bind reset theo contract nhưng merge transcript theo stable IDs, không xóa transcript hoặc clone. Replay bind sau archive/delete phải trả outcome + current availability, không resurrect ACTIVE projection cũ.

## Retry/idempotency/preconditions
Mỗi intent tạo clientOperationId + idempotency key ổn định và payload hash. Double-tap Send chỉ một bubble/operation. Retry cùng intent dùng cùng key/payload; khác payload cùng key là conflict. Timeout/network drop sau submit là UnknownOutcome; lookup/reconcile trước, không key mới để “thử lại”. Stable server message IDs/sequence dedup, không dựa text/timestamp. Completion một lần, không card từ partial. Idempotency result window24h của v1.4 không phải offline retention; hết window không giả failed để post lại.

If-Match/current thread version/context/lifecycle/capability theo approved contract. 403 báo không quyền; 428 xử lý thiếu precondition, không bỏ header để retry mù; 409 context/version conflict refresh và user xem lại; 404 owner-safe unavailable, network fail không coi404. Reconnect/foreground validate current state và reconcile pending trước enable action. DB transaction ngắn; không giữ transaction/row lock khi gọi LLM. UI không tự định nghĩa endpoint lookup/rename hoặc migration; đối chiếu contract deployed trước production adapter.

## Offline/cache/lifecycle
Offline read-only: xem retained cache đúng owner, cả sau24/48h; freshness24h chỉ mark stale/cachedAt và revalidate online, không TTL khóa đọc/xóa. Không có cache thật mới no-cache. Cache capacity v1.4:200 threads/10.000 messages/20MiB, LRU khi cần nạp dữ liệu mới; protect thread đang đọc, không eviction chỉ do cũ. Không cam kết toàn bộ history đã download.

Không send/bind/evaluate/rename/archive/delete/restore hoặc auto replay khi reconnect. Session draft có thể edit/giữ trong rotation, không hứa durable raw chat draft sau process death. Minimal pending journal theo approved contract phục vụ reconcile, không là outbox. Log out/account switch tách owner/session generation, hủy inflight/subscriptions; response cũ không leak/resurrect dữ liệu. Known deletion purge đúng owner và tombstone; device đang offline chưa biết delete có thể còn đọc cache theo v1.4. Archive thread read-only, khác archive ShoppingItem. Restore chat không restore item.

## Edge cases phải triển khai/kiểm thử
| Case | Expected UX / guard |
|---|---|
| Item bị xóa/permission đổi khi NC đang chọn | Giữ thông báo, disable Start, refresh; không tạo dummy item |
| Item archived/read-only | Theo item capability; không tự restore; Chat lifecycle độc lập |
| Search đổi khi page cũ trả về | Generation/cursor bound query; bỏ old response, dedup threadId |
| Thread bind chuyển FREE→ITEM trong lúc CL filter FREE | Refresh membership, thread không xuất hiện như FREE lần nữa; không duplicate |
| Empty/emoji/combining marks/long Vietnamese input | Trim whitespace; count theo contract; giữ text, không silently truncate; 200% font wrap |
| Rotation/process death sau submit | Không POST lại do recomposition; reconcile journal/current messages trước resend |
| Draft đang có + quick reply | Giữ draft, inline explanation; không overwrite |
| Partial response/failure | Không official card; giữ trạng thái chưa hoàn tất, retry/reconcile theo op |
| Provider output có raw HTML/instructions/link lạ | Text renderer an toàn; không execute commands hoặc business intent từ output |
| User rời detail khi response đến | Notification/badge chỉ nếu supported; không đổi back stack/focus ngoài ý muốn |
| Delete ack đến sau bind response cũ | Tombstone/current lifecycle thắng; không restore title/transcript |
| Giá/budget thiếu, stale, estimate khác actual | Nêu thiếu/dữ liệu cũ; không claim đủ tiền; không dùng estimate ghi chi |

## 34-decision provenance
Namespace gốc: CHAT-01–20, GAP-01–08, FINAL-01–06. Approval vẫn được giữ như ledger cũ. Body verification = 0 VERIFIED / 34 PARTIAL vì không có nguyên bản body/option cho từng ID. Không ghép GAP của My Items vào GAP Chat. `decision_provenance_34.csv` liệt kê đủ34 rows với missing body/option/impact/AC mapping; revision này không tự điền rule vào ID. Current UX có mapping riêng REV01-01…12 và readable-source links, dùng để triển khai mà không giả source recovery đã xong.

## Gaps còn lại
RENAME-API: yêu cầu UX mới có dialog nhưng không có endpoint/field/version/capability/limit đã xác minh. Cần backend owner xác nhận contract trước wire production, không tự sửa TECH. Runtime readiness của approved APIs vẫn cần repo/deployment evidence; scope này không kiểm app. Không thiếu nguồn mockup/mascot nữa sau user bổ sung ảnh; exact `/mnt/data` byte-parity vẫn không xác minh vì path không tồn tại ở host.
