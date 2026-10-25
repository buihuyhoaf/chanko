# TC-PAGING/CACHE — pagination và Room

**APPROVED WITH AMENDMENT A4, revision1.4.** Baseline TECH-03 §2.1: stable order +ID tie-breaker, owner/filter-bound cursor, capped limit; TECH-02 §8 và TECH-03 §4.3: Room read cache, không auto outbox, owner isolation/foreground refresh. User đã phê duyệt A4-A với amendment tách freshness khỏi offline retention; [normative addendum](08_APPROVED_AMENDMENTS_V14.md) áp dụng. Không thay TECH source files hoặc tuyên bố implementation đã chạy.

## Conversation/history pagination

- Sort membership/order snapshot: updated_at DESC, id DESC tại request page1. Filter gồm owner từ session, context kind, item ID nếu có, lifecycle và search query chuẩn hóa. Không gọi loaded-count là total.
- Approved snapshot ID-list trong Redis hỗ trợ tạm thời, TTL15 phút; không long-running DB transaction. Freeze ordered IDs +sort key +filter hash +owner +created/expiry. PostgreSQL vẫn nguồn record/quyền hiện hành; snapshot không là business truth. Redis unavailable→503 LIST_SNAPSHOT_UNAVAILABLE; snapshot bị evict/hết hạn→400 CURSOR_EXPIRED, client reset page1. Không fallback offset âm thầm.
- Cursor opaque, signed/authenticated, chứa snapshot ref/index và filter/owner binding; client không chế tạo hoặc đổi filter dưới cursor cũ. Default limit20, max100; limit đổi không đổi snapshot order. Invalid signature/owner/filter→400 CURSOR_INVALID, không leak data.
- Mỗi page lấy next slice of frozen IDs, re-authorize/current lifecycle trước trả nội dung. Deleted/khác owner hoặc current context không còn khớp filter sau bind bị bỏ, không lộ title. Rows có thể cập nhật nội dung/version mới nhưng `sort_updated_at_snapshot` giữ vị trí snapshot; client không tự sort lại từng page. New/updated membership xuất hiện khi refresh page1.
- Khi skip deleted rows, page có thể ít hơn limit hoặc rỗng nhưng has_more vẫn true nếu snapshot còn IDs. next_cursor dựa index đã tiêu thụ, không dựa length response. Không lặp cursor. UI tiếp tục tải theo cursor hoặc user refresh, không giả account empty từ một empty page.
- `total_snapshot` chỉ trả nếu backend tính count chính xác theo snapshot, không total hiện hành; khi có visibility/lifecycle changes thì omit count hoặc báo count_as_of rõ. UI không cần total để hoạt động. Never infer from page length.
- Approved resource bound: tối đa3 snapshots/user, TTL15 phút, tối đa1MiB metadata/snapshot; khi vượt trả503 SNAPSHOT_LIMIT với hướng dẫn thu hẹp query/retry. Không silently truncate hoặc mất history; tune sau load test và giữ contract revision. Đây là giới hạn vận hành cần Backend review, không giới hạn tổng số cuộc trò chuyện trên server.

## Message và official history pagination

Messages có server sequence_no unique, được cấp trong transaction. Default page gần nhất50, max100, API trả data theo sequence ASC để render; cursor tải older theo before_sequence và snapshot upper sequence tại page1. Append sau upper bound nằm ngoài snapshot; thread refresh lấy latest và merge stable IDs. Bind giữ cùng transcript nhưng tăng context_revision; message cursor bound context revision cũ trả400 CURSOR_CONTEXT_CHANGED, reset cursor và merge ID, không xóa transcript đã đọc. Delete thread invalidates mọi cursor/read. Sequence gap do redaction không được hiểu là thiếu record để bịa message.

Official evaluations/DecisionRecords query theo owner+item, order created_at DESC,id DESC, cursor bound item/filter. Không join vào conversation còn sống. Delete chat không invalidates quyền item history. Concurrent new evaluations xuất hiện khi refresh, không overwrite records cũ.

## Client query generations

Mỗi owner+query+filter+sort+snapshot/context_revision có query generation riêng, nhưng entity cache/route/draft của existing chat keyed owner+threadID ổn định qua bind. Search/filter/refresh/account switch tăng generation và reset cursors; response cũ không được merge vào generation mới. Upsert theo server resource ID; preserve scroll/anchor khi tải older. Cursor expired/invalid→clear page cursor và tải page1, không tự submit operation. Stale response không xóa record được source mới xác nhận.

## Cache freshness, offline retention và Room

Các quy tắc [A4 amendment](08_APPROVED_AMENDMENTS_V14.md) có hiệu lực. **freshness TTL24h không phải display TTL hoặc offline retention expiry**. Data còn cache đúng account và chưa known-unavailable vẫn đọc offline dù freshness/cursor đã hết hạn. Server remains authority; offline không mutations/AI/bind/outbox.

| Dữ liệu / policy | Approved contract v1.4 |
|---|---|
| Conversation/messages/evaluation Chat cache | owner+server ID+server version+context_revision+fetched_at.24h chỉ đánh dấu STALE/revalidate khi online, không xóa hoặc chặn đọc |
| Offline retention | Không có age cutoff. Giữ đọc cached data còn tồn tại cho tới explicit clear/logout/known lifecycle invalidation hoặc actual capacity/OS eviction; không giả toàn bộ server history đã tải |
| Capacity |200 threads,10.000 messages hoặc20MiB; LRU chỉ khi cần capacity cho dữ liệu mới, không purge vì tuổi. Protect thread đang mở/đọc để không mất nội dung giữa phiên offline |
| Official item-history cache | Không purge official Decision/Evaluation/Purchase records do delete chat; chỉ transcript/link cache chat bị invalidated |
| Unsent chat draft | owner+threadID cho thread đã có; giữ cùng draft qua FREE→ITEM ack. Unsent context trước create có logical session key; rotation/session theoA5, không hứa raw draft durable sau process death |
| Pending journal | Owner-scoped IDs/key/hash/context revision/version/deadline/status tối thiểu sau user send; không raw outbox, không tự replay |
| Offline Manual Item Draft | Theo baseline riêng, không bị chat retention/freshness hoặc bind làm mất draft/ảnh |
| Logout/account switch | Clear render owner cũ; logout purge chat cache/draft/journal. Không dùng key context mới để hiện dữ liệu owner khác; item manual-draft policy giữ nguyên |

Bind là update projection của cùng cached entity: giữ threadID/message IDs, đổi kind/item/context revision/version, giữ draft. Query generations cũ bị discard; không clone cache FREE và ITEM thành hai thread records. Old FREE response arriving late không overwrite context_revision mới. Retain known delete tombstones để stale page/response không resurrect nội dung.

## Phân biệt các hạn thời gian

- Freshness24h: mark old, trigger refresh khi online; offline read không bị chặn.
- Cursor snapshot15m: hết hạn chỉ làm reset page fetch online, không xóa cache đã tải.
- Retry/result24h/key digest30d: giới hạn operation safety, không transcript retention.
- Server purge24h/backup30d sau delete: A1 áp phía server/storage, không guarantee máy offline đã biết deletion.

## Revalidation, owner và failure handling

Open/foreground/reconnect/user refresh/mutation success kiểm thread/context/lifecycle/capabilities hiện hành trước enable actions. Network lỗi giữ stale cached read access; không diễn giải thành404/deleted hoặc bảo user phải có mạng chỉ vì dữ liệu quá24h. Known unavailable/deleted từ server hoặc local confirmed delete mới purge transcript/draft/summary và chặn resurrect; official item history không bị xóa.

Thiết bị offline không nhận remote delete/bind tức thì; có thể đọc snapshot cũ với nhãn “Dữ liệu đã lưu, ngữ cảnh có thể đã thay đổi”. Khi kết nối lại, reconcile current context/version/operations trước writes. Cache no longer retains a24h confidentiality cutoff; đây là amendment user đã phê duyệt, không được giữ claim cũ trong copy/QA.

No-cache chỉ khi dữ liệu chưa tải hoặc thật sự đã clear/evict/known unavailable. Offline read không renew fetched_at thành “mới”. Migration/recovery không discard retained cache chỉ vì freshness/cursor expiration, không phá item manual drafts hoặc pending identity cần reconcile. Storage failure/LRU thật được thông báo đúng; không báo server send success cục bộ.

Process death với pending identity: lookup theo03; mất raw payload không tự tạo lại message/key. Token không nằm Room; cơ chế private storage/encryption theo target repo security, không tuyên bố Room tự mã hóa. Runtime kiểm freshness/retention độc lập trong IT12/IT15 và AM14-08–12; chưa chạy không làm contract thành pending approval.
