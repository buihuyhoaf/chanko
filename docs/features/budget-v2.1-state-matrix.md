# State matrix
Các trục độc lập: cycle current/past; network online/offline; load loading/ready/error/no-cache; spend unset/zero/within/over; mutation idle/submitting/error/conflict. canWrite = online && currentCycle && ready && revisionValid && !submitting, kiểm tra lại ở command/fake repository. Không dùng enum loại trừ làm mất tổ hợp.

| Trạng thái | Overview / settings summary | Cycle / history / methods | Mutation |
|---|---|---|---|
| Current online | Số liệu đúng cycle | Đọc, toggle history, Pay Now cố định | Cho theo validation |
| Over | Nhãn Vượt ngân sách + số vượt, tỷ lệ thật >100%; bar cap 100% | Không thay scope | Vẫn sửa được khi current online |
| Zero | Budget 5m/spent 0/remaining 5m/0% | Empty chi tiêu; history vẫn có nếu có adjustment; items vẫn có | Theo quyền |
| Past | Badge Đã kết thúc · Chỉ xem | Cycle/history/item navigation còn dùng | Khóa budget/add/rename/delete |
| Offline cached | Banner cache + thời gian cập nhật | Chỉ cache, history toggle và Back còn dùng | Khóa entry và command, không queue |
| Offline no-cache | Chưa có dữ liệu để xem ngoại tuyến; không số giả | Pay Now là định nghĩa hệ thống, không giả list credit/history; chỉ cycle cached | Khóa |
| Loading | Skeleton, không biến thành 0 | Loading section nếu có | Khóa |
| Load error | Retry; có cache giữ snapshot kèm độ mới/lỗi | Section error độc lập nếu chỉ phần đó lỗi | Khóa khi snapshot/revision không hợp lệ |
| Empty credit/history | Pay Now vẫn hiện; text empty riêng | Không empty toàn màn | Add theo quyền |
| Submitting | Giữ draft, pending rõ | Khóa submit/dismiss modal | Chống double tap |
| Save error/conflict | Giữ draft; error/reload rõ | Không overwrite im lặng | Retry/revalidate |

Ưu tiên hiển thị: offline banner → past badge → over warning; zero không xóa banner. Offline+past+zero và offline+past+over bắt buộc fixture. Mất mạng/cycle kết thúc khi form mở: giữ draft, khóa Lưu/Xóa, hủy còn dùng nếu không pending. Network trở lại phải revalidate.
Không cache cycle mới: không giữ các số của cycle cũ dưới tiêu đề mới. Back/restoration giữ selectedCycleId, scroll, historyExpanded; không replay pending write.

## C06 bổ sung (ưu tiên DIALOG-ADD-CARD.md)
| State | Hiển thị / thao tác |
|---|---|
| Initial | Tên rỗng, 0/30, tím selected và preview tím/Tên thẻ; CTA disabled, chưa lỗi |
| Editing valid | Preview tên/màu realtime, CTA bật theo quyền |
| Empty/duplicate/>30/reserved | Lỗi tương ứng, CTA khóa, giữ tên/màu |
| Color only changed | Dirty=true, preview đổi; tên rỗng vẫn không submit |
| Pending | Đang thêm…, khóa edit/màu/dismiss, chống double tap |
| Failure | Inline error, draft giữ, retry cùng ý định không thêm trùng |
| Offline/past mid-form | Read-only + lý do, Hủy/đóng còn dùng; reconnect revalidate |
| Success | Đóng về C03, card mới đúng tên/màu, Pay Now vẫn mặc định, spent không đổi |
Fixtures thêm: length 0/1/30/31; whitespace/NFC/dấu Việt/emoji ghép; duplicate khác hoa/thường, duplicate khác màu, archived cùng tên, reserved Pay Now; mỗi màu; chỉ đổi màu rồi cancel; pending/error/retry; offline/past khi mở và giữa submit. Không thêm màn test/debug launcher.


## C07 — ưu tiên DIALOG-RENAME-CARD.md
| State | UI và hành vi |
|---|---|
| Initial / unchanged | Prefill, n/30, màu read-only, preview; Lưu khóa |
| Valid changed | Preview realtime; Lưu theo canSave |
| Empty / >30 / duplicate / reserved | Draft giữ, lỗi cụ thể, Lưu khóa |
| Legacy 31–40 | Không cắt prefill; helper rút gọn, chỉ lưu tên mới ≤30 |
| Source loading/error | Không edit dữ liệu giả, khóa Lưu; retry/đóng |
| Pending | Đang lưu…, khóa edit/dismiss/submit, một request |
| Save error | Draft/màu giữ, inline lỗi, retry sau revalidate |
| Conflict / archived | Không overwrite; reload có xác nhận / khóa Lưu và đóng |
| Offline/past | Read-only có lý do, giữ draft, cancel còn dùng ngoài pending |
| Success | C03 cùng ID/màu, tên mới; snapshot cũ và tiền không đổi |
Fixtures: 0/1/30/31/40; whitespace/NFC/emoji; self và other duplicate, case-only; legacy dài; unchanged; pending/double tap/error/retry/conflict; offline trước mở/giữa edit/submit/reconnect; past; archive; rotate/process restore; expense trước/sau rename.
