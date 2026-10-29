# Acceptance criteria — tất cả CHƯA CHẠY trên app
READY là độ đầy đủ đặc tả, không phải test PASS. Ghi evidence/commit/device khi triển khai.

| AC | Given / When / Then |
|---|---|
| 01 | Shell → Home/My Items/+/Budget/Insights; + gọi global action, Thiết lập ví không là tab |
| 02 | Overview → Thiết lập ví → màn riêng có Back, đúng 3 section; Back giữ cycle/scroll |
| 03 | Inventory → đúng 2 screen/2 sheet/3 business dialog; không manage/history route/sheet, add/rename không sheet |
| 04 | Settings within fixture → budget 5m trực tiếp, range đầy đủ, spent 3.2m/remaining 1.8m/64%, anchor chỉ đọc |
| 05 | Edit từ cả 2 parent → live preview, save/hủy quay đúng parent; 1 save = 1 adjustment, không đổi kỳ sau |
| 06 | Input invalid/rỗng/âm/thập phân/overflow/zero → lỗi, không ghi; zero fixture không chia 0; giảm dưới spent cảnh báo nhưng được lưu fake |
| 07 | Payment section → Pay Now luôn có, default chỉ system method, không rename/delete UI hay command; credit chỉ tên |
| 08 | Add/rename dialogs → trim/NFC/length/duplicate/reserved name, unchanged disabled; error giữ draft; success về settings |
| 09 | Archive last credit → vẫn có Pay Now, empty credit; spent và historical name snapshots không đổi |
| 10 | Xác nhận mua Pay Now và Credit fixture → mỗi khoản chỉ trừ budget một lần; save item/add card không trừ tiền |
| 11 | History header toggle → expanded semantics; rows có time/before/after/cycle range, mới nhất trước, không click/edit/revert |
| 12 | Activity adjustment → settings history mở đúng cycle/event; Back đúng Overview, không history sheet |
| 13 | Past → read-only Budget, shared Item Detail hồi tố theo module đích; quay lại refresh đúng cycle |
| 14 | Offline cached/uncached → lý do và độ mới rõ, mọi write bị chặn ở UI/repo; chỉ cached cycles; không fake số 0 |
| 15 | Form mở rồi offline/cycle kết thúc → draft giữ, save khóa; online lại revalidate, không tự commit |
| 16 | Over 5m/5.5m → vượt 500k/110%, bar cap; zero 5m/0 → 0%, empty độc lập history/items |
| 17 | Loading/error/empty/conflict/double tap/process recreation → không stale cycle, không write lặp, không overwrite draft im lặng |
| 18 | Overview → tối đa 3 budget-affecting events; WAIT/SKIP/save không tiền bị loại; item dùng shared detail |
| 19 | Visual corrections → không số thẻ che/default credit, có Pay Now/budget trực tiếp/range history, không row chevron |
| 20 | TalkBack/font 100/150/200%/narrow/IME → focus/labels/expanded/48dp/contrast đúng, không che nút hay cắt số |
| 21 | Thiếu art → UI text vẫn đủ, placeholder được phép; không phát sinh màn/flow mới |

Test plan: unit cho tiền/parser/name/reducer; fake repository tests cho Pay Now guard, purchase attribution, rename snapshot/archive và idempotency; Compose tests cho navigation/dialog/history và offline/past combinations. Preview/render các fixtures trong VALIDATION.md rồi review ảnh; Android runtime/TalkBack trên thiết bị theo policy repo đích. Hiện chưa build/lint/test/runtime vì đây là bàn giao tài liệu, không sửa implementation.

## C06 bổ sung — chưa chạy trên app
| AC | Given / When / Then |
|---|---|
| 22 | Settings +Thêm thẻ → dialog căn giữa có dim, không sheet/route mới, nền không tương tác |
| 23 | Initial → tên rỗng/0/30, tím selected/preview tím, mô tả/đóng/Hủy/Thêm thẻ đủ |
| 24 | Đổi tên và từng màu → preview phản ánh cùng draft; không VISA/chip/số thẻ; chữ Visa do user nhập chỉ là tên |
| 25 | Tên 1/30 hợp lệ, 0/31 invalid; trim/NFC/duplicate/reserved đúng; counter tính thật, không copy 17 từ ảnh |
| 26 | Hủy/X/Back/scrim → cùng discard policy; chỉ đổi màu cũng dirty; không ghi; parent scroll/history giữ |
| 27 | Double tap pending → một request/card; lỗi giữ tên/màu và retry; success đúng tên/màu trong settings, Pay Now còn mặc định, spent/history không đổi |
| 28 | Offline/past trước mở hoặc giữa form/submit → UI và command chặn write; không queue/tự submit khi reconnect |
| 29 | Font 200%/màn hẹp/IME/TalkBack → body cuộn, footer khả dụng, focus modal, palette có tên/selected, restore focus khi đóng |
Validation evidence cần ghi riêng policy UI30 và domain40, emoji ghép/dấu tổ hợp; không coi kiểm tra source là test pass.


## C07 — AC30–39, tất cả chưa chạy trên app
| AC | Given / When / Then |
|---|---|
| 30 | C03 chọn Đổi tên → modal giữa nền dim, không sheet/route; prefill đúng cardId, X/Hủy/Lưu thay đổi |
| 31 | Sửa tên → preview realtime; màu/ID giữ, màu không tương tác; không VISA/chip hay field dữ liệu ngân hàng |
| 32 | Tên trống/space/31 → lỗi và khóa; 1/30 hợp lệ; counter grapheme đúng NFC/emoji; Techcombank=11 |
| 33 | No-change/trim/NFC-only → khóa Lưu; case-only self được đổi; duplicate other case-insensitive/reserved khóa |
| 34 | Tên cũ 31–40 → prefill đủ, helper/counter, không truncate; chỉ lưu draft mới ≤30 |
| 35 | Hủy/X/Back/scrim → cùng discard policy, không ghi; giữ cycle/scroll/history và restore focus |
| 36 | Pending/double tap → một mutation, khóa dismiss; failure giữ draft/màu và retry; conflict không overwrite |
| 37 | Offline/past/loading/archive → khóa UI và command đúng lý do; reconnect không tự submit, không queue |
| 38 | Rename success → chỉ tên hiện hành đổi, snackbar; expense cũ giữ tên cũ, expense mới dùng tên mới, spent/budget/C09 không đổi |
| 39 | IME/font200/màn hẹp/TalkBack/rotate/process restore → nút còn dùng, focus/labels đúng, không replay write |
Bằng chứng bàn giao: review chéo tài liệu và ảnh reference; không phải bằng chứng test app. Validation plan tương lai phải kiểm tra reducer, guard/retry/idempotency/snapshot và Compose states trên thiết bị thật theo repo policy.
