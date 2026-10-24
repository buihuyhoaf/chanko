# Prompt cho coding agent • Chanko My Items & Item Detail v1.0

Đọc README,01–05,07–09, manifest, Recovery Baseline và snapshots Functional/TECH-02/Design System; đọc AGENTS.md và quy ước repo đích. Đây là bộ bàn giao tài liệu; chỉ implement khi được giao nhiệm vụ riêng. Không PR/deploy ngoài scope được giao. Không suy ra package/routes/API/schema đã tồn tại vì có logical fields trong tài liệu.

## Approved UX phải giữ

-4 routes MyItemsScreen, ItemDetailScreen, TransactionDetailScreen (view/edit cùng route), ArchivedItemsScreen.3 sheets Filter & Sort, More Options, Record a Purchase; Purchase Confirmation là cùng sheet, không thêm màn/sheet. Journey/Purchase History là sections.
- MyItems default list card ngang thumbnail trái, compact read-only, tap detail, không More/inline purchase trên card. Compact3D Chanko Hero,3 tiles All Items/Cooling Off/Ready tap lọc. Hai cấp All/Active/Purchased/Skipped và Active→Saved/Considering/Cooling Off/Ready/Postponed; Search/Sort có sẵn. Active/Saved/Cooling Off/Ready/Postponed là UI mapping ở03, không thêm enums.4 tabs+global+, không FAB hoặc Add trùng.
- Empty state canonical Chanko+hộp kẹo; Save an Item hoặc Clear Filters. Archived Items trong menu, Restore giữ history/budget.
- Detail Pastel Candy Product Stage dummy-only, summary/expandable details, Edit trong More. Contextual content/CTA, bubble+small Chanko dưới status, collapsible Journey, sticky primary+More. Countdown nổi bật; Adjust/Skip Waiting trong More. Purchased/Skipped ưu tiên read-only, Purchase History section riêng; tap transaction mở TransactionDetail xem/sửa/xóa nhập sai.
- Ready mindful review ngắn có thể bỏ qua; Ready giữ đến action xử lý rõ, không clear vì mở/Back/dismiss. Need More Time2 nhánh: new wait hoặc Considering+Postponed event, next review khi có. Still Want It mời AI1–2 câu, có thể bỏ qua; không bắt mua.
- Direct Mark as Purchased và Buy Again trong More mở Record a Purchase; không Quick Check bắt buộc; phải xác nhận transaction, không gán Purchased thiếu purchase bằng shortcut.

## Business contract

5 ItemStatus WISHLIST/CONSIDERING/WAITING/PURCHASED/SKIPPED; verdict BUY/WAIT/SKIP độc lập. ACTIVE completion do server→COMPLETED/CONSIDERING; Need More Time sau COMPLETED tạo ACTIVE mới, giữ phiên cũ. Skip Waiting kết thúc ACTIVE qua cancel theo phụ lục→Considering và mở lựa chọn, không tự Skip item; Skip item rõ ràng mới RESOLVED/SKIPPED. Mua ACTIVE tạo purchase và RESOLVED/PURCHASED atomically; không khóa quyền mua.

Postponed không ItemStatus/timer giả, Considering+event. Ready temporary offline theo EDGE01 không mutation chính thức: cache statuses giữ nguyên, label chưa đồng bộ, reconnect refetch. Persist/clear marker sau Still Want It/Postponed, next-review scheduling và archive ACTIVE/restore lifecycle còn Pending contract; không invent schema/enum/endpoints hoặc thay UX đã Approved.

Một item N purchases, expected fields chuẩn estimated_unit_price/actual_unit_price; không DTO mới bằng tên cũ. VND nguyên exact, quantity INTEGER≥1 default1, unit≥0, server tính unit×qty. Retry cùng intent giữ idempotency key; Buy Again mới key mới; key cũ khác payload conflict; stale version không overwrite. Edit purchase cùng purchaseId và revision, delete correction không refund. Xóa cuối giữ status hiện tại bất kỳ, evaluations/cooling history; không hồi sinh timer. Counts khác quantity, tổng history không tính từ1 page.

PAY_NOW=CREDIT tại purchased_at/account timezone [start,end), Credit card optional; không debt/refund/discovery/rollover. Cycle anchor1–31 cố định, clamp giữ anchor, remaining có thể âm. Budget amount ngay current cycle setting+snapshot cùng transaction; closed snapshots giữ nguyên; correction recompute đúng cycles và recap khi xem. Không dùng budget threshold UI để chặn mua.

Archive/restore giữ ID/status/history/accounting. Không tự hard-delete/cascade, cancel/pause ACTIVE hay reset Wishlist. Backend policy ACTIVE/archived write chưa chốt: theo capability thật hoặc báo gap; không áp guard chặn/restore-trước-edit tự chọn làm Approved.

## Offline, state và feedback

Room read cache và manual item drafts, không general business outbox. Offline giữ form mua theo EDGE07, xác nhận cần online, không auto-submit reconnect. Manual draft item chưa là item/purchase, không budget; ảnh draft bền vững. Server ack mới success; timeout UnknownOutcome giữ key, reconcile không key mới. Account ownership/caches tách biệt. Form/filters/search/sort/scroll qua lifecycle theo repo; result query cũ không ghi đè, sort/count toàn bộ không giả từ từng page.

## Visual/assets

Candy Pop3D light mode, Nunito, palette/radii/spacing/adaptive của01/Design System; controls2D thật, ≥48dp, primary56 grow, font200%, TalkBack/reduced motion/insets. Exact dimensions/copy/default sort chưa có nguồn là Reference, không đòi phê duyệt lại tokens Approved.

Chỉ canonical white Chanko có provenance, không chó hoặc mèo khác. Hero MyItems/Empty/bubble được phép; cấm trong/cạnh/chồng Product Hero/media. Bundle không có canonical+hộp kẹo: cần asset, tạm giữ text/slot trống có báo gap. Product Hero/fixtures/preview chỉ dummy từ manifest, không túi/tai nghe/giày; không tự tìm ảnh mạng. Không confetti/celebration mua/shaming/upsell; sound không tự bật.

## Delivery khi có nhiệm vụ implement

Audit repo, map read models/capabilities/API/image/theme; giữ user changes. Implement theo kiến trúc Kotlin/Compose/M3 hiện có, không tự dựng backend thay policy còn thiếu. Wire mutation version/idempotency/ack và invalidate đúng list/detail/history/budget/recap. Chạy AC/EC ở04 cùng repo checks, đặc biệt Ready provisional/persistence,2 nhánh Need More Time, Skip Waiting khác Skip item, purchase retry/cycle correction/xóa cuối/archive. Review screenshots layouts/accessibility. Báo rõ phần pass/chưa test, visual asset thiếu và contract gaps; không gọi bundle QA là runtime pass.29 mã đã có Recovery Baseline và mapping, không tiếp tục báo thiếu nội dung29 mã.
