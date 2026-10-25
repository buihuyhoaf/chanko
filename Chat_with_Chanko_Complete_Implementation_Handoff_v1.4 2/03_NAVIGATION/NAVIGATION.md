> v1.4: official visual baselinev1.2; [approved context/API](../13_BACKEND_API_CONTRACTS/01_CONTEXT_AND_API.md), [approved lifecycle](../13_BACKEND_API_CONTRACTS/04_LIFECYCLE_AND_MULTI_DEVICE.md). UI fixture routes dùng được ngay; production adapter chỉ theo deployed capabilities đúng approved revision.

# Navigation Flow
Tất cả tên route sau là P logical identifiers, không xác nhận routes đã có trong repo.

## Entry points
| Nguồn | Đích | Contract |
|---|---|---|
| Home — CTA Chat (dùng entry đã có của Home, không thêm tab) | ChatEntry → Free Chat/Empty | Không thêm tab; giữ 4 tabs và global + |
| Item Detail — Cân nhắc/Xem lại cùng Chanko | ItemChat(itemId) | Kiểm tra owner/item hiện tại; reuse/new theo approved A2 cho direct ITEM entry, không áp thay thế same-thread bind, không đổi context |
| Ready — Still Want It | ItemChat(itemId, recheck intent) | Cho bỏ qua; mở chat không clear Ready/ghi decision |
| Chat app bar | ChatHistory | Giữ route nguồn, query/scroll |
| Free Chat — chọn existing item để xác nhận chuyển | ItemPicker | Cancel không đổi context |
| Free Chat picker chọn existing item +confirm/ack | ChatThread(same threadId), context projection ITEM | A2 amendment giữ transcript/draft; no clone/reuse other thread/no item create; cancel giữ FREE |
| Item chip/verdict link | ItemDetail(itemId) | Không business mutation |
| History row | ChatThread(threadId) | Đọc mode/context từ repo, không tin object cũ |
| Thread More | ThreadOptions → DeleteConfirmation/Archive | Không mở hai modal cùng lúc |
| History — Archived | ArchivedThreads → ArchivedThread | Khác ArchivedItems |
| Restore thành công | Thread view/History refreshed | Không restore Shopping Item ngầm |
| Delete thành công | History hoặc Empty | Không quay lại deleted route |

## Luồng chính
Home → Empty → gửi hợp lệ → Free Chat.
Item Detail → Item Chat → Quick Check → structured verdict → xem Item Detail → user tự chọn action nghiệp vụ riêng.
History → thread → options → delete confirmation → pending → ack → History.
History → thread → options → archive → ack → Archived → restore rõ ràng.
Bất kỳ view có cache → mất mạng → Offline read-only → có mạng → refetch/reconcile → online state; không tự gửi.

## Back stack và lifecycle (P)
Back ưu tiên đóng keyboard theo hành vi Android, dialog/sheet/picker, rồi route. Cancel delete giữ thread. Back từ Chat về đúng Home/Item Detail và giữ list filters/scroll. Không đẩy Home mới vào stack.
Routes truyền ID và entry intent tối thiểu; repository lấy dữ liệu mới. Không nhét toàn bộ transcript/PII vào navigation args.
Composer draft keyed theo account+thread/context; không rò draft giữa Free/Item/thread. Rotation không gửi lại operation. Process death nếu mất operation identity thì đọc messages/reconcile trước khi cho resend, không đoán chưa commit.
Deep link thread thiếu/không thuộc owner: màn không tồn tại chung, không leak title/preview; Back an toàn. Account switch hủy subscription và xóa view/cache account cũ khỏi màn.
A4/A5 đã approved session draft; existing-thread draft keyed owner+threadID và giữ qua confirmed FREE→ITEM. Không hứa durable raw draft/cloud.

## Early verdict và chuyển thiết bị (v0.3)
Item Detail → ItemChat → Quick Check chưa verdict → user RequestEarlyVerdict (logical P event; quyền từ SPEC-01) → pending → structured result hoặc error/UnknownOutcome. Thành công tạo immutable evaluation qua server; không purchase hoặc resolve Ready. User có thể rời chat và mua trực tiếp qua Item Detail flow đã có.

History/foreground/reconnect → đọc server theo contract đang có → reconcile pending nếu có cơ chế được chốt → render capability/lifecycle hiện hành. Không tự post lại intent/restore thread; bind FREE→ITEM chỉ qua explicit user confirmation, giữ cùng transcript không backfill official records. Nếu thiết bị khác delete/archive, route cần unavailable/read-only theo lifecycle contract; approved conflict/context revision policy ở13/04+08; không last-write-wins bằng clock client.

## Confirmed conversion navigation
S02→S05 selection→confirmation trong cùng picker surface→BindExistingItem pending→same ChatThread ID với S03 pre-verdict sau ack/reconcile. Không push một ItemChat entity/thread thay thế, không merge với existing item thread khác. Back/cancel trước submit giữ FREE; request đã submit nhưng ack mất thì reconcile cùng key trước đổi UI. Không tạo auto verdict/call AI khi bind. Khi chưa có server thread ID ở Empty, chọn/xác nhận item là unsent ITEM context; first send tạo ITEM thread.

Offline retained transcript luôn đọc được kể cả freshness hết24h; route state nêu stale context/cache timestamp, no writes. Khi remote bind/delete/archive được xác nhận online, cùng entity projection/context version hoặc known tombstone thắng old async responses.
