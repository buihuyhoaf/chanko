# TC-R07 — xóa chat, bảo toàn official Decision Records

**APPROVED A1-A, revision1.4; A3 approved và A4 amendment áp dụng cho offline cache.** R07 contract-approval gate đã đóng bởi user; runtime conformance/release gate kiểm sau implementation. Invariant baseline: xóa chat không xóa official decision/evaluation/purchase history hoặc thay budget/item/cooling-off.

## Phương án A đã được phê duyệt

Logical deletion có hiệu lực ngay khi commit; purge transcript/chat summaries trong24 giờ; dữ liệu backup được luân chuyển hết trong tối đa30 ngày. Các mốc này nay là yêu cầu contract được user approve; Data/Backend owners phải hiện thực hóa và chứng minh trước release, không phải SLA đã đo hoặc tuyên bố pháp lý.

Không có Restore cho DELETED; chỉ Restore ARCHIVED. Hạn chế giữ nội dung chat sau khi user xóa, trong khi tombstone tối thiểu đủ chặn phục hồi nhầm/race/replay. AlternativeB v1.3 không được chọn; không có restore DELETED.

| Nhóm dữ liệu | Sau logical delete | Sau purge |
|---|---|---|
| Raw user/assistant messages, chat draft server nếu từng có, generic chat/session summaries | Ngay lập tức không đọc/list/search/provider context | Xóa trong24h, xóa khỏi derived index/cache backend cùng scope |
| Minimal conversation tombstone | Giữ owner, opaque ID, lifecycle/version/epoch, deletion/purge metadata; không title/snippet/raw text | Giữ để bảo vệ referential/race behavior theo account/item retention; không dùng để mở lại chat |
| Official DecisionRecords | Giữ ID, item link, confirmed action và lịch sử đúng nguồn | Không cascade từ chat; theo retention item/account hiện có |
| Official EvaluationRecords | Giữ ID/item/verdict/rationale và snapshot tối thiểu đã dùng giải thích kết quả | Bảo toàn history; generic chat summary không được trá hình thành official evaluation |
| Purchases, corrections, budgets, cooling-off | Không đổi | Không đổi |
| Operation/idempotency payload | Response chứa chat text bị redaction ngay, kể cả replay; giữ trạng thái/resource IDs/hash không có transcript | Safe replay metadata24h; key digest/tombstone30 ngày theo approved03; không làm sống lại nội dung chat |
| Android cache | Thiết bị kết nối purge khi validate/refresh nhận UNAVAILABLE | Offline chưa biết remote deletion vẫn đọc retained cache, không age cutoff. Khi nhận known deletion/owner invalidation thì purge ngay; freshness24h không phải retention theo05+08 |

Không giữ toàn transcript trong evaluation.input_snapshot. Allowlist official snapshot gồm item facts/version, budget facts/version/as_of cần thiết, mã câu trả lời/tóm tắt nhu cầu tối thiểu, engine/prompt version; không raw tokens hoặc hồ sơ tâm lý. Rationale có thể chứa dữ kiện user đã cung cấp ở mức cần giải thích official assessment. Phải thông báo rõ xóa chat vẫn giữ official item history, không hứa xóa mọi dữ kiện từng được đánh giá. Review/backfill dữ liệu cũ nếu snapshot hiện đang chép raw transcript; không xóa evaluation/DecisionRecord để né vấn đề.

## Quan hệ dữ liệu và đọc history độc lập

Approved design giữ tombstone conversation shell và FK evaluation→conversation với RESTRICT/no physical parent removal. Không ON DELETE CASCADE từ conversation tới official records. DecisionRecord.shopping_item_id và evaluation_record_id vẫn giữ nguyên. Các field/FK này là approved implementation design trong Chat addendum, không migration đã chạy và không sửa TECH snapshot. Nếu triển khai sau cần hard-delete shell, phải ADR riêng cho nullable link/backfill, không tự SET NULL trong migration.

Official history reader bắt đầu từ owner+item và đọc Decision/Evaluation IDs trực tiếp; conversation link là optional presentation link. UI history giữ card dữ kiện chính thức, thay lối chat bằng nhãn “Cuộc trò chuyện đã xóa”; không yêu cầu JOIN còn sống để đọc record. Delete không sửa spent/remaining hoặc item.status, không tạo correction purchase hoặc decision mới.

## Luồng transaction và quyền

1. Parse/auth/owner, validate header và lookup idempotency theo03. Replay cùng key đã thành công trả204, không kiểm version hiện tại để làm replay thất bại; vẫn kiểm auth và redaction.
2. Transaction ngắn lock conversation/active operation theo thứ tự thống nhất. Kiểm If-Match, quyền delete và current lifecycle. Owner khác404; version cũ409; offline không tạo request.
3. Đặt DELETED, tăng version và lifecycle_epoch, revoke read/send/evaluate/archive/restore; đánh dấu operation đang chạy INTERRUPTED_LIFECYCLE. Ghi tombstone, purge deadline và kết quả idempotency atomically.
4. Commit rồi204. Không đợi provider hoặc purge job trong transaction; purge failure không cho phép đọc lại chat. Không JSON body trong204. Operation lookup theo client ID xác nhận commit nếu response mất.
5. Purge job là maintenance function idempotent trong hạ tầng backend hiện có; không bắt buộc thêm queue platform. Mark purge-complete chỉ sau kiểm tra transcript/summaries/index đã được loại. Failure retry nội bộ có cảnh báo vận hành; production acceptance cần chứng minh các mốc24h/30d được thực hiện.

## Race với send/evaluate

| Thứ tự commit | Kết quả có thẩm quyền |
|---|---|
| Evaluation/message commit trước delete | Result đã tồn tại; delete ẩn/purge transcript, official evaluation giữ nguyên |
| Delete commit trước AI finalization | lifecycle_epoch mismatch; discard provider output, không append assistant/evaluation, không tái tạo thread |
| Provider đang chạy khi delete | Có thể best-effort stop call nhưng không hứa cancel phía provider; epoch fence ngăn commit |
| Delete response mất | Operation lookup/replay cùng intent; không báo success từ timeout, không key mới |
| Hai thiết bị delete/archive cùng version | Transaction/version xác định winner; request còn lại conflict/reload, không device-clock winner |
| Restore sau delete hoặc stale create |404/409 chung phù hợp owner scope; không upsert/clone/restore ngầm |

## Backup, purge và kiểm chứng

Backup phục hồi phải áp deletion ledger trước khi mở traffic; restore backup không được làm chat đã xóa đọc được lại. Raw text có thể còn trong backup tới30 ngày theo phương án đã được user approve; không hứa xóa vật lý ngay khỏi backup/provider. Chính sách lưu dữ liệu tại provider/TECH-04 vận hành được owner xác nhận trước release, không mặc nhiên đóng bởi việc chấp thuận UI.

A1-A đã chốt scope/retention/khả năng khôi phục và phương án referential này. Runtime tests IT01–IT06/IT16 trong10 chứng minh official IDs/counts/values/history readers không đổi, purge/redaction đúng, timeout/race an toàn. Không dùng việc chưa có test sau implementation để đánh dấu approved contract này thiếu nội dung.

## Official-history retention boundary

Không đặt bất kỳ TTL xóa nào cho official Decision/Evaluation/Purchase history hoặc minimal conversation shell do hành động Delete Chat. Chúng tiếp tục tồn tại trong account/item history theo baseline; chỉ một chính sách/action độc lập được phê duyệt cho account/item mới có thể thay đổi retention đó. Đây không phải quyền restore transcript hoặc triển khai account deletion mới trong Chat. Transcript/chat summary purge24h vẫn áp dụng độc lập.

## Same-thread binding và independent official history

FREE→ITEM confirmed bind không chuyển/chỉnh official DecisionRecords cũ, không tạo hoặc backfill Evaluation/Purchase. Các official evaluations chỉ được tạo qua valid post-bind evaluation intent, gắn owner/item và giữ độc lập transcript. Delete sau bind dùng cùng tombstone shell/independent item readers, không cascade. A4 amendment cho retained offline read không nới server read permissions, purge hoặc replay redaction của A1.
