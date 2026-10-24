# UX baseline

## Phạm vi

4 màn: MyItems, ItemDetail, TransactionDetail, ArchivedItems. 3 modal bottom sheets: Filter & Sort, More Options, Record Purchase. Dialog xác nhận, date picker, section mở rộng và trạng thái loading/error không tính thành màn độc lập. Edit transaction là chế độ trong TransactionDetail; Record Purchase chỉ tạo lần mua mới. Các route Add/Edit Item, AI conversation và cooling-off editor là điểm tích hợp ngoài 4 màn này; dùng luồng hiện có nếu có, không tự phát triển thêm toàn bộ ứng dụng.

## Bất biến đã xác minh

- Đúng 5 Item Status: WISHLIST, CONSIDERING, WAITING, PURCHASED, SKIPPED. AI BUY/WAIT/SKIP là dữ liệu khác.
- Ready for Review là trạng thái trình bày dẫn xuất; Postponed là tín hiệu trình bày, không thêm enum hoặc tab status thứ sáu.
- Một item có nhiều purchases; purchase count không phải quantity. Đơn giá dự kiến không thay đơn giá thực. Tạo purchase đưa item về PURCHASED.
- Chỉ giao dịch server xác nhận mới tác động chi tiêu. Đổi nhãn status không ngầm tạo hoặc xóa giao dịch. WAITING → PURCHASED phải qua ghi purchase.
- Sửa/xóa purchase nhập sai có revision và tính lại đúng chu kỳ; không phải refund. Xóa purchase cuối không đổi status, không hồi sinh cooling-off và không xóa evaluation.
- Online-first; Room đọc cache, manual draft riêng. Không có business outbox, không tự gửi draft khi mạng trở lại.
- Archive/restore là chiều độc lập với status trong thiết kế bàn giao; bảo toàn purchase và lịch sử. Không dùng hard delete thay archive. Schema/API archive vẫn cần căn chỉnh với contract thật.

## Phân lớp nội dung

MyItems trả lời “Tôi đang cân nhắc những gì?”. ItemDetail trả lời “Món này đang ở đâu trong quyết định của tôi?”. Purchase history trả lời “Tôi đã ghi nhận những lần mua nào?”. TransactionDetail trả lời “Lần mua này có thông tin gì và có nhập sai không?”. Không dùng một chip cho cả verdict, item status và payment type.

Price text luôn ghi rõ “Giá dự kiến / đơn vị”, “Đơn giá thực tế”, “Số lượng”, “Tổng giao dịch”. Count ghi “3 giao dịch”, không ghi “3 món” khi quantity có thể >1. Không tổng giá skipped thành tiền tiết kiệm. Không có sale, mua ngay, đề xuất sản phẩm hay lời mèo cổ vũ mua lại.

## Visual Candy Pop 3D

Design System nguồn là Approved; bố cục đã nêu trong Recovery Baseline là Approved; kích thước/microcopy chưa có nguồn cụ thể vẫn Reference. UI 2D sạch, chỉ hero và mascot dùng chiều sâu soft clay. Không raster hóa chữ, nút hoặc cả màn hình.

| Vai trò | Token nguồn |
|---|---|
| Nền / surface | #FAF8F5 / #FFFFFF |
| Purple / Pink | #8965F6 / #FF85B3 |
| Yellow / Mint / Blue / Orange | #FFD75E / #78DCC3 / #8DCDF8 / #FFAA61 |
| Hero gradient | #8965F6 → #BA85F5 → #FFB4CF |
| Cooling / Ready / Purchased / Skipped | #8DCDF8 / #FFD75E / #6888C9 / #78DCC3 |
| Postponed / Error | #FFAA61 / #D94B57 |
| Corner radius | Hero 32dp, card 24dp, small card 20dp, thumbnail 16dp |
| Controls | Primary radius 20dp, secondary/input 16dp; sheet top 28dp; chip capsule |
| Typography | Nunito only; title 20/28sp 700, body 16/24sp 400, metadata 12/16sp 500, price 20sp 700, button 16/24sp 700 |

Nguồn chưa chốt tất cả foreground/on-colors. Reference trong mockup tài liệu: text #211E2B trên surface cream/trắng; CTA chữ #15131D trên Purple (xấp xỉ 4.60:1 theo tài liệu nguồn). Khi hiện thực phải đo từng cặp, kể cả disabled/pressed/focus. Không mặc định chữ trắng trên Purple, không dùng semantic bright làm chữ nhỏ trên trắng. Màu status dùng như container đi kèm nhãn và icon.

Approved UI-08/09: spacing 4/8/12/16/24/32/48dp, horizontal screen padding 20dp, section spacing 24dp; primary 56dp (cho grow khi font lớn), touch target ≥48×48dp. Insets hệ thống và IME bắt buộc. Compact một cột + Bottom Navigation; Medium có thể hai cột; Expanded dùng Navigation Rail + multi-column. Không ép một cột cho mọi thiết bị. Ngưỡng breakpoint cụ thể theo repo/contract là Reference.

Reference riêng cho màn này: card padding 16dp, spacing list 12dp, khoảng 20dp ngoài screen padding nếu cần. Không thêm 20dp vào thang spacing Approved. Foreground, gradient angle, hero ratio và kích thước MyItems mascot vẫn Reference.

## Mascot và product media

Chanko phải là mèo trắng canonical, soft clay 3D, mắt đen tròn tối giản. Không dùng chó; không đổi hình mèo giữa các màn. Không có asset canonical trong nguồn truy xuất nên bundle không tạo một mascot giả. Dùng registry asset có sẵn trong repo đích; nếu chưa có thì ẩn slot hình và giữ lời thoại.

Được dùng mèo ở hero MyItems, empty states, speech bubble độc lập dưới status ItemDetail. **Trong bản bàn giao/preview v1.0, Product Hero chỉ dùng dummy placeholder trung tính; không đưa ảnh sản phẩm mẫu vào. Thumbnail và TransactionDetail cũng dùng dummy assets của bundle. Luồng ảnh thật do user nhập trong sản phẩm tương lai là khả năng theo SPEC, không phải asset được cung cấp hoặc nội dung preview v1.0. Tuyệt đối không ghép mèo vào, không nằm cạnh ảnh sản phẩm; không dùng túi/tai nghe/giày minh họa cũ.** Trong ItemDetail cần tách rõ media container với status/speech section, không overlay mascot lên ảnh.

Reference kích thước MyItems mascot 140–180dp nếu có nguồn, empty 140–180dp, supporting speech 48–72dp. Không ép mascot vào mọi card hoặc form. Hình trang trí không được TalkBack đọc; lời thoại là text thật. Không Happy/confetti khi ghi purchase; dùng Neutral.

## Copy và tương tác

| Ngữ cảnh | Copy Reference |
|---|---|
| Ghi mua | “Ghi nhận mua hàng”; success “Đã ghi nhận giao dịch” |
| Ready | “Đến lúc xem lại” / “Bạn còn muốn món này không?” |
| Offline | “Đang xem dữ liệu đã lưu trên thiết bị” |
| Countdown hết khi offline | “Đã đến thời điểm xem lại. Kết nối mạng để cập nhật trạng thái.” |
| Archive | “Lưu trữ món đồ”; “Lịch sử mua và chi tiêu vẫn được giữ.” |
| Delete correction | “Xóa giao dịch nhập sai?”; giải thích chi tiêu chu kỳ sẽ tính lại |
| Không purchase | “Chưa có giao dịch được ghi nhận” — kể cả status PURCHASED |

Mọi mutation giữ form khi lỗi, hiển thị pending và chống double tap. Sau timeout có thể đã commit: “Chưa xác nhận được kết quả”, đối chiếu/retry cùng ý định, không báo thất bại chắc chắn. Danh sách có thể phản ánh mutation sau server ack, không cập nhật chi tiêu giả trước ack.

Nunito chưa có trong bundle; không lấy font từ web không rõ quyền. Dùng font và OFL hợp lệ từ repo/nguồn được cấp. Material Symbols Rounded weight 400, icon thường 24dp, selected có thể fill. Reduce Motion tắt bounce/trang trí; tài chính chuyển động tối thiểu; sound là tùy chọn, không tự bật starter pack âm thanh.

## Approved UX theo Recovery Baseline

MyItems: default list card ngang, thumbnail trái; Compact3D Chanko Hero;3 tiles All Items/Cooling Off/Ready nhấn lọc. Filter hai cấp: All/Active/Purchased/Skipped; Active gồm Saved/Considering/Cooling Off/Ready/Postponed. Search/Sort có sẵn, option/default sort cụ thể Reference. Cards compact read-only, tap detail; không inline More hoặc mutation. Giữ4 navigation tabs và nút+ toàn cục, không thêm FAB/Add trùng. Archived Items trong menu. Empty state Chanko + hộp kẹo, Save an Item hoặc Clear Filters theo ngữ cảnh.

ItemDetail: Pastel Candy Product Stage dummy-only, summary + expandable facts, Edit trong More. Chanko nhỏ bên contextual bubble dưới status, tách khỏi product media. Sticky primary CTA + More Options; contextual nội dung/CTA theo state. Purchased/Skipped ưu tiên read-only; Decision Journey collapsible từ lịch sử thật, Purchase History là section riêng. Countdown nổi bật, Adjust/Skip Wait trong More. Ready review ngắn có thể bỏ qua; bỏ qua review UI không tự xử lý Ready.

Need More Time có2 nhánh: thời gian mới→ACTIVE mới sau COMPLETED; hoặc chuyển/giữ Considering + Postponed event, next review time khi có. Direct Mark as Purchased/Buy Again trong More đều mở sheet ghi purchase, không bắt buộc Quick Check, không đặt nhãn thay transaction.

Offline đến deadline: hiển thị Ready tạm thời, nêu chưa đồng bộ; cache ItemStatus/CoolingOffStatus giữ nguyên, không tự ghi review hay purchase. Khi có mạng refetch; không auto-replay form mua.

## Phân loại hiệu lực

Approved: toàn bộ29 mã tại07,5 status và các bất biến từ Drive, tokens Design System. Reference: exact dimensions không có nguồn, copy, ratio, sort defaults, logic layout chi tiết ngoài baseline. Pending contract: persisted Ready resolution, postponed metadata/clear, archive ACTIVE/restore lifecycle và archived write permissions. Không hạ cấp UX đã duyệt chỉ vì backend chưa có field.

Budget UI Normal<80%, Warning80–100% gồm đúng80/100, Over>100%; là tín hiệu thiết kế đã duyệt, không chặn mua; backend threshold pairing cần contract. Budget0/missing không NaN hoặc chia0. Amount sửa hiệu lực ngay cycle hiện hành; cycle đóng giữ snapshot; recap tính lại khi xem.
