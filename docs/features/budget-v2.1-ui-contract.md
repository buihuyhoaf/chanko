# Screen/component inventory và tương tác
Đây là contract đầy đủ cho fake UI. Giá trị kích thước và policy input là fallback kế thừa v2.0, không phải pixel measurements hoặc nghiệp vụ mới đã được duyệt.

| ID | Surface v2.1 | Thành phần / hành vi |
|---|---|---|
| C01 | Screen/tab Budget Overview | Header Ngân sách + Thiết lập ví; selector cycle; banners; summary; mascot slot; breakdown; món đáng chú ý; preview biến động |
| C02 | Bottom sheet Chọn chu kỳ | Radio range/current/past/selected; tap chọn ngay, không Save/create/future/editor |
| C03 | Screen Thiết lập ví | Back, title/subtitle; 3 khu vực ngân sách/phương thức/lịch sử |
| C04 | Bottom sheet Sửa ngân sách | Range áp dụng, field tiền, live preview, warning dưới spent, Hủy/Lưu |
| C05 | Section trong C03 | Pay Now + active credit names + Thêm thẻ; không route/sheet riêng |
| C06 | Dialog Thêm thẻ | Tên thẻ bắt buộc, counter /30, 6 màu Candy, preview tên/màu, đóng, Hủy/Thêm thẻ |
| C07 | Dialog Đổi tên thẻ | Tên prefill, n/30, màu hiện tại chỉ đọc, preview realtime, chú thích snapshot, X/Hủy/Lưu thay đổi; xem DIALOG-RENAME-CARD.md |
| C08 | Dialog Xóa thẻ? | Tên + giải thích giữ khoản chi, Hủy/Xóa thẻ |
| C09 | Expandable trong C03 | Header toggle; list chỉ đọc mới nhất trước, empty/error/cache |
| C10–C13 | State C01, phản ánh ở C03 | Vượt / offline / past / zero-spend; không route |

## C01/C02 giữ chức năng
Overview thứ tự như inventory. Summary luôn có budget/spent/remaining hoặc over, progress, tỷ lệ thật và cycle. Payment breakdown chỉ đọc theo cycle, có Pay Now và Credit, nhóm chưa gán nếu có; zero-spend không giả giao dịch. Món đáng chú ý tối đa 3, item hợp lệ mở shared detail; kỳ cũ dùng snapshot đúng kỳ, thiếu thì báo “Không có dữ liệu món đáng chú ý cho chu kỳ này”. Activity tối đa 3 mới nhất, tie-break eventId, chỉ tác động tiền/hạn mức; item event mở shared detail, adjustment mở C03/C09. Không Xem tất cả dẫn Timeline.
C02 loading/error/empty có lý do; offline row chưa cache disabled. Sau chọn, dữ liệu mới phải cùng selectedCycleId, lỗi không trộn summary kỳ cũ dưới nhãn kỳ mới.

## C03 bố cục
Header Back nhãn “Quay lại”, “Thiết lập ví”, subtitle như ảnh. Candy Pop 3D: nền pastel, thẻ bo mềm, tím chủ đạo, minh họa tùy chọn; không cắt ảnh screenshot làm asset. Dùng token app, không hard-code theo khung iPhone.
1. “Ngân sách mua sắm”; “Ngân sách: 5.000.000đ”; “Chu kỳ 15/10/2026 – 14/11/2026”; “Ngày bắt đầu chu kỳ: 15 · Chỉ xem”; Sửa ngân sách mở C04. Đã chi 3.200.000đ, Còn lại 1.800.000đ, 64%. Số lớn wrap/reflow, không che/cắt.
2. “Phương thức thanh toán”; helper “Pay Now và thẻ tín dụng đều tính vào ngân sách khi xác nhận mua.”; Thêm thẻ. Pay Now đứng đầu, nhãn “Mặc định”, không edit/delete. Credit rows chỉ tên, icon trang trí, Đổi tên/Xóa có label accessible; không số thẻ hay default badge. Không có credit: “Chưa có thẻ tín dụng” nhưng vẫn có Pay Now và Thêm thẻ.
3. “Lịch sử điều chỉnh ngân sách”; mặc định thu gọn khi vào từ settings, mở khi vào từ activity; toggle có expanded semantics. Row: ngày/giờ địa phương, Tăng/Giảm ngân sách, ngân sách trước → sau, chu kỳ áp dụng đầy đủ. Có thể có delta; không row chevron/click/edit/revert. Chỉ header toggle có chevron. Empty “Chưa có điều chỉnh ngân sách trong chu kỳ”. Không tạo event khởi tạo từ null chỉ vì ảnh có minh họa; fake list chỉ các adjustment có before/after xác định.

## Modal lifecycle
C04 là ModalBottomSheet; C06/C07 là Dialog nhỏ gọn, C08 AlertDialog. Không biến add/rename thành sheet. Mỗi modal có title, nội dung cuộn và nút không bị keyboard che. Back/scrim/swipe/hủy khi chưa đổi draft đóng ngay; draft đổi thì xác nhận bỏ thay đổi bằng confirmation dùng chung của app (utility, không tính là surface nghiệp vụ thứ tư). Không thêm route cho confirmation. Giữ lựa chọn “Tiếp tục chỉnh sửa” / “Bỏ thay đổi”. Submitting khóa dismiss/submit, fake request phải kết thúc hữu hạn. Chỉ một modal nghiệp vụ hiện tại; không mở sheet quản lý thẻ cũ phía sau dialog.
Success đóng về đúng parent, snackbar và refresh source of truth. Lỗi giữ draft, inline retry qua nút submit; không success giả. Offline/cycle hết hạn trong modal giữ draft, khóa ghi kèm lý do; online lại revalidate, không tự submit.
C04 success cập nhật summary và đúng một adjustment/activity, không sửa kỳ sau. C06 success thêm tên, không tự gán expense. C07 success chỉ đổi metadata hiện hành; C08 archive giữ snapshots và spent. Card đã archive khi rename: khóa Lưu, “Thẻ không còn khả dụng”, Quay lại; đã archive khi delete: refresh/đóng/báo “Thẻ đã được xóa”.

## Layout và accessibility fallback
Padding/gap section 16dp, gap field 12dp; touch ≥48dp, icon 24dp; LazyColumn/scroll theo content, không fixed height cắt font. Text Việt qua resources, label độc lập placeholder, money đọc “đồng”; summary theo budget → spent → remaining/over → tỷ lệ → cycle. Focus modal và lỗi đầu; expanded/selected/disabled có semantics và lý do, không chỉ màu. Mascot trang trí không đọc. Contrast mục tiêu 4.5:1 text thường, 3:1 text lớn/thành phần quan trọng; kiểm tra font 100/150/200%, màn hẹp, insets/IME, reduced motion. Không chép status bar/Dynamic Island/khung máy từ ảnh vào UI Android.
