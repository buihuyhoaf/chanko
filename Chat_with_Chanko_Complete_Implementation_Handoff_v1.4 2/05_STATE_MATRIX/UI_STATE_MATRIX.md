> v1.4:11 visual baseline chính thức. Production seams trong matrix đã được user approve A1–A5, gồm A2/A4 amendments normative13/08; không còn yêu cầu tìmv0.2. Runtime tests chạy sau implementation.

# UI State Matrix — đúng 11 trạng thái
Đây là 11 trạng thái UX, không phải enum database. Các overlay có thể kết hợp: Offline + Item Chat, Error + History, Loading + Archived. Thứ tự render P: xác thực/context → modal → offline capability → nội dung → loading/error tại vùng liên quan. Không thay cả timeline bằng spinner khi còn cache.

| # | State | Điều kiện / nội dung | Actions | Exit / guard |
|---|---|---|---|---|
| S01 | Empty | Online, ngữ cảnh mới chưa có message; Chanko canonical nếu có, lời mời ngắn, demo badge | Nhập/gửi, chọn item, History | Gửi tạo pending operation; Empty không chứng minh account chưa có history |
| S02 | Free Chat | Thread không item; messages mẫu có sender và thời gian | Send nếu online/idle; chọn item; History/More | Không official Decision Record; chọn item không gắn ngầm transcript cũ |
| S03 | Item Chat with Verdict | Item context + messages; khi evaluation hoàn tất hợp lệ thì có Verdict Card; trước verdict vẫn có Quick Check timeline/composer | Yêu cầu verdict sớm theo quyền; re-evaluate khi phù hợp; xem item; next step qua flow hiện có | Render verdict không đổi item/decision/purchase/budget/cooling-off; stale version có label lịch sử |
| S04 | History | Danh sách active threads; context/free label; empty/partial/page error riêng | Open row, Archived; tải thêm theo cursor | Query cũ không ghi đè; count không giả từ page |
| S05 | Item Picker | Item rows đọc từ repository; query, empty, loading và retry riêng | Select item hoặc Cancel | Confirmed FREE→ITEM cùng thread; Item biến mất/stale: refetch, không chọn item khác thay thế; offline chỉ đọc cache, không mở AI |
| S06 | Offline | Banner cache + freshness; timeline/history cached (freshness không khóa offline read) hoặc no-cache explanation | Back/đọc cache; retry kết nối | Send/evaluate và mutations disabled; reconnect không replay |
| S07 | Loading | Initial skeleton hoặc refresh indicator; send pending gắn đúng turn; partial chỉ trong demo scenario | Back/đọc nội dung; cancel UI subscription nếu supported | Không hứa cancel backend; không tạo verdict từ partial; một operation active/thread (P) |
| S08 | Error | Inline section/message error; UnknownOutcome khác confirmed failure | Retry phù hợp, xem lại dữ liệu, Back | Giữ draft/key; validation/conflict không retry mù; no default BUY |
| S09 | Thread Options | Sheet có context thread và quyền hiện tại | P Archive, Delete, xem item; đóng | Online/capabilities guard; không mutate lúc mở menu |
| S10 | Delete Confirmation | Tên/context chat + cảnh báo giữ official history; pending/error trong dialog | Giữ lại / Xóa chat | Double tap một operation; ack mới dismiss success; offline disable; cancel không effect |
| S11 | Archived | List/detail có nhãn archived và message lịch sử | P đọc/Restore/Delete theo capability | Không tự reopen/send; archive chat không archive item; restore không reset history |

## Biến state bổ trợ (P contracts)
content = none/cache/fresh; context = free/item; lifecycle = active/archived/deleted; connectivity = online/offline; request = idle/pending/partial/unknown/failed; modal = none/options/delete/picker; dataSource = fake/cache/server.
Không lưu cả 11 state thành 11 booleans mâu thuẫn. “Deleted” là lifecycle nội bộ để loại route, không thêm trạng thái UX thứ 12. Pending/partial/unknown là request flags, không mở rộng danh sách 11.

## Demo fixtures bắt buộc
FX01 Empty; FX02 Free Chat ≥2 turns; FX03 Item BUY neutral; FX04 cùng item re-evaluation WAIT/SKIP giữ lịch sử; FX05 History nhiều trang; FX06 Picker có/không kết quả; FX07 Offline cache/no-cache; FX08 initial/refresh/send loading; FX09 confirmed error/UnknownOutcome/partial interrupted; FX10 options+delete cancel/ack/failure; FX11 archived+restore; FX12 account switch/stale item/version conflict.
Tất cả là synthetic, không dữ liệu người thật. Fixture money ví dụ 250.000 ₫ chỉ là giá dự kiến, không giả đã chi.

## Coverage pre-verdict (R02)
Giữ nguyên tên S03 “Item Chat with Verdict” theo danh sách user. Nhóm nội dung item có subcase P `evaluation=none/pending/completed/failed/stale`; không thêm state thứ12 hoặc enum database. Idle Quick Check chưa verdict không render card và không bị giữ spinner. S07/S08 là pending/error overlays, không thay nội dung item thread. FX13/AC21 kiểm subcase này. Đây là coverage từ SPEC-01, không phục dựng body CHAT nào.

FX13–18 và AC21–26 tại ../10_ACCEPTANCE_QA/AC01_29.md; FX19–21 và AC27–29 kiểm multi-device/cache/pagination. Tất cả là test plan/fixture design, chưa triển khai fixture code.

## A2/A4 approved semantics
FREE→ITEM là context transition có xác nhận của cùng thread, không UX state thứ12. S03 sau bind có thể chưa verdict; không auto evaluate. Freshness TTL24h và snapshot cursor expiry không đổi quyền đọc retained cache offline, không thêm no-cache chỉ vì thời gian. Current context_revision cùng lifecycle_epoch/owner/version bảo vệ stale responses và multi-device race;13/08 normative.
