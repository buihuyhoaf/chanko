# Chanko Home — bản chốt v5 tiếng Việt

Ngày bàn giao: 20/10/2026. Đây là hướng dẫn để tự code UI, không phải mã Android. Nguồn hình: ../chanko-home-v5/home-full-scroll-vi.png.

Preview là ảnh raster sinh bằng AI, không có layer/Figma hay thông số gốc. Ngoài màu thương hiệu và Nunito đã có trong Design System, HEX, kích thước và shadow bên dưới là bộ thông số triển khai đề xuất để tái hiện bản đã chốt; không phải thông số đã trích từ file thiết kế vector. Không dùng ảnh toàn màn hình làm UI thực tế.

## 1. Màu sắc

| Token | HEX | Dùng cho |
|---|---|---|
| brandPurple | #8965F6 | Nút +, ngày hôm nay, progress, avatar |
| brandPink | #FF85B3 | Viền ngày chọn, dấu ngày mua |
| background | #FAF8F5 | Nền cream chính |
| backgroundLavender | #F0E7FF | Quầng tím rất nhạt ở góc nền |
| backgroundPink | #FCEAF4 | Quầng hồng rất nhạt cuối trang |
| surface | #FFFFFF | Bottom bar và bề mặt trung tính |
| calendarSurface | #F4EEFF | Container lịch |
| calendarInnerSurface | #FAF7FF | Mặt lưới lịch, không cần viền riêng |
| primarySoft | #EBE1FF | Nút Hôm nay, tab được chọn |
| primaryInk | #5731C6 | Chữ/icon tím trên nền sáng |
| textPrimary | #151329 | Tiêu đề, giá tiền |
| textSecondary | #585C82 | Mô tả, thứ, nhãn phụ |
| divider | #E4DDF2 | Phân cách nhẹ trong lịch |
| progressTrack | #E3DEEC | Rãnh progress |
| readySurface | #D9FAE6 | Item Sẵn sàng xem lại |
| readySurfaceEnd | #CFF6DF | Điểm cuối gradient xanh mint |
| readyAction | #00634F | Nút Xem lại |
| onReadyAction | #FFFFFF | Chữ nút Xem lại |
| readyInk | #075C48 | Chữ trạng thái ready |
| readyChip | #E6FFF0 | Chip đã hết thời gian chờ |
| coolingBlue | #E0EBFF | Item cooling-off thứ nhất |
| coolingLavender | #E7E1FF | Item cooling-off thứ hai |
| coolingChip | #D3D9FF | Nền timer |
| speechSurface | #FFECE5 | Bong bóng lời mèo |

Gradient chỉ dùng nhẹ: background cream + radial lavender/hồng opacity khoảng 25–40%; ready xanh mint; cooling gần như màu đặc; progress có thể #8965F6 → #A184FF. Không cần gradient mọi component. Màu tiền còn lại dùng primaryInk để đọc rõ hơn màu tím nhạt trong raster. Nút + dùng textPrimary trên brandPurple; không mặc định chữ trắng nhỏ trên màu tím thương hiệu. Cần đo tương phản trên nền thực tế trước phát hành.

Kiểm tra các cặp màu đặc đề xuất: #151329 trên #8965F6 ≈ 4,54:1; #5731C6 trên #F4EEFF ≈ 6,96:1; trắng trên #00634F ≈ 7,25:1; #585C82 trên #FAF8F5 ≈ 6,06:1; #075C48 trên #D9FAE6 ≈ 7,13:1. Đây là phép tính trên màu đặc, chưa xác nhận gradient, alpha, pressed/disabled hoặc toàn bộ UI đạt WCAG.

## 2. Typography

Nunito toàn bộ UI, gồm chữ số. Dùng file font có đầy đủ tiếng Việt; nhúng cùng app để không phụ thuộc tải mạng. Bảng dưới là scale đề xuất riêng cho màn này.

| Vai trò | Size / line-height | Weight |
|---|---|---|
| Greeting | 20sp / 28sp | 800 |
| Tiêu đề lịch | 22sp / 28sp | 800 |
| Tiêu đề section | 20sp / 28sp | 800 |
| Tháng của lịch | 18sp / 24sp | 800 |
| Ngày / thứ | 14sp / 20sp | 600 / 500 |
| Tên item | 16sp / 22sp | 800 |
| Giá item | 16sp / 22sp | 600 |
| Số tiền budget | 18sp / 24sp | 800 |
| Số tiền ngày được chọn | 22sp / 28sp | 800 |
| Body, nhãn budget | 14sp / 20sp | 500–600 |
| Timer, status, legend | 12sp / 18sp | 600 |
| Nút | 14sp / 20sp | 800 |
| Bottom nav | 11–12sp / 16sp | 600–700 |

Giữ định dạng 3.000.000 ₫; không ellipsis số tiền. Nunito ExtraBold dùng cho heading, không tô mọi dòng cùng độ đậm. Đừng lấy kích thước chữ theo pixel ảnh PNG rồi dùng trực tiếp làm sp.

## 3. Layout và hình học

Khung thiết kế mục tiêu 393dp; chiều dài nội dung linh hoạt, không cố nhét full-scroll preview vào một viewport. Spacing scale: 4 / 8 / 12 / 16 / 20 / 24 / 32dp. Gutter mục tiêu 16–20dp. Nền full màn; nội dung cuộn; bottom bar cố định với system insets.

| Thành phần | Thông số đề xuất |
|---|---|
| Header | cao tối thiểu 56dp; greeting trái, gear rồi avatar bên phải |
| Settings, avatar | hình 36–40dp, hit target 48dp; icon gear 22–24dp |
| Header → lịch | 12–16dp; không hero / khoảng trống |
| Lịch container | radius 24dp; padding 16dp; cao theo nội dung |
| Nút Hôm nay | pill radius full; cao hiển thị 36dp, vùng chạm 48dp |
| Ô ngày | lưới 7 cột chia đều; height 40–48dp; nền/viền chọn radius 12dp |
| Purchase dot | 4–5dp, dưới số ngày |
| Progress | cao 10dp, radius full |
| Khoảng cách section | 20–24dp |
| Icon cửa / tuyết | 32–36dp, chỉ trang trí |
| Item | radius 20dp; padding 12dp; min-height 88dp, cho phép cao hơn |
| Khoảng cách hai item | 8–12dp |
| Thumbnail | 60–64dp, radius 14dp; ảnh contain/crop phù hợp |
| Nút Xem lại | cao tối thiểu 48dp; min-width 80dp; pill |
| Status/timer chip | radius full; padding ngang 8dp, dọc 4dp |
| Chevron | 20dp; toàn item có thể là vùng mở chi tiết |
| Bottom bar | cao nội dung 72dp + system bottom inset; radius 24dp |
| Nút + | 56dp; icon 28dp; giữa chính xác theo chiều ngang |

Shadow ít: calendar/card có thể 0–2dp, bar 4dp, + 6dp. Đây là đề xuất, không phải elevation approved. Dùng shadow mềm ngả tím, tránh shadow đen hoặc nhiều lớp card lồng nhau.

## 4. Cấu trúc UI để tự code

Scaffold
→ nền cream + các gradient vẽ bằng Brush
→ content LazyColumn
  1. HomeHeader(greeting, onSettings, onProfile)
  2. SpendingCalendar(cycle, selectedDate, today, purchasesByDate)
     - header + nút Hôm nay
     - cycle range + month label
     - weekday row + date grid
     - legend
     - chi tiết ngày đang chọn
     - CatBudgetMessage: ảnh mèo + bong bóng text thật
     - BudgetSummary + progress + renewal
  3. SectionHeader(cửa mint, Sẵn sàng xem lại, item count)
  4. ReadyReviewItem
  5. SectionHeader(bông tuyết, Đang cân nhắc, item count)
  6. CoolingOffItem × 2
→ BottomBar(Trang chủ, Món đồ, add action, Ngân sách, Phân tích)

Chỉ mèo/cửa/tuyết là ảnh trang trí pre-render. Lịch, speech bubble, chữ, nút, nền card, chip, progress, avatar, gear và nav phải là component thật. Gear, calendar, chevron, clock, plus, home, inventory/clipboard, pie và chart dùng một bộ Material Symbols Rounded thống nhất; glyph trong raster chỉ là tham chiếu, không cần cắt icon UI ra PNG.

Bottom bar có bốn tab, nút + là action thêm món đồ ở trung tâm. Mỗi tab hai bên nhận cùng phần chiều rộng, chừa khoảng trung tâm 64–72dp. Giữ selected Trang chủ bằng cả icon/label/semantics. Không coi + là tab, không đổi màn selected chỉ vì mở thêm món.

## 5. Lịch và dữ liệu minh họa

Ảnh chốt giữ ngày minh họa hôm nay 19/10/2026 dù ngày bàn giao là 20/10. Khi code, lấy ngày thực tế và dữ liệu từ state, không hardcode 19.

Chu kỳ 15/10/2026 → hết 14/11/2026; kỳ mới 15/11/2026. Ngày neo 15 là ví dụ đã có setup, không áp đặt quy tắc chu kỳ đầu. Ngày mua 16 và 18/10; đang chọn 18/10 có 1 giao dịch 1.400.000 ₫. Tổng đã tiêu 2.400.000 ₫, ngân sách 3.000.000 ₫, còn lại 600.000 ₫. Ngày mua/giao dịch/item là dữ liệu mock; không phải lịch sử tài khoản.

Lưới này là lưới theo chu kỳ bắt đầu T5, KHÔNG phải lưới tháng tiêu chuẩn bắt đầu T2. Nhãn thứ tương ứng phải được tính từ cycle start, không copy sang mọi chu kỳ. Ngày ngoài kỳ không nằm trong lưới; renewal hiển thị riêng.

| State ngày | Hiển thị |
|---|---|
| Bình thường | chữ dark, nền trong |
| Cycle start | outline tím + nhãn trong semantics |
| Today | nền tím; foreground đạt tương phản |
| Selected | outline hồng; thêm selected semantics; khi trùng Today giữ nền tím và vòng chọn ngoài |
| Có purchase | dot hồng; semantics đọc số giao dịch |

Chạm ngày cập nhật selected date và dòng giao dịch; nút Hôm nay chọn ngày hiện tại khi thuộc kỳ đang xem. Xử lý trường hợp ngày hiện tại ngoài kỳ theo ngữ cảnh điều hướng đã có, không tự thêm tính năng mới. Không coi selected date là đổi cycle. 80% là spent/budget, không phải tiến độ thời gian. Hết cooling-off không tự mua; UI bám trạng thái server. Remaining âm phải hiện đúng, không cắt về 0; budget 0/null không chia cho 0.

## 6. Responsive — điều chỉnh cần thiết khi code

Ảnh AI cho cảm giác đúng nhưng lịch + sidebar mèo sẽ quá chật nếu bê y nguyên sang 393dp và vẫn yêu cầu ô chạm 48dp. Để dùng thực tế: cho lưới lịch chiếm toàn chiều ngang; chuyển legend thành một dòng dưới lưới, mèo + speech thành hàng hỗ trợ dưới chi tiết ngày. Đây là cách reflow bảo toàn chức năng và visual identity, không phải thêm feature. Ở 393dp với gutter 16dp và padding lịch 12dp, bảy ô rộng khoảng 48dp. Ở 320dp, có thể cần lịch edge-to-edge trong vùng content và hit target tối thiểu phù hợp, ưu tiên tránh các target chồng nhau.

Header ở font lớn cho greeting xuống dòng hoặc đứng trên nhóm gear/avatar. Budget ba cột có thể đổi thành 1–2 cột để không cắt tiền. Item tên dài xuống dòng, status và nút không ép cùng hàng khi thiếu chỗ. Icon trang trí có thể nhỏ/ẩn; không giảm chữ quan trọng. Đo ở 320/393dp và font scale 100/150/200%. Bottom bar cần phương án label reflow, không ellipsis nhãn quan trọng.

## 7. Accessibility / state

Mọi action ≥48dp khi có thể; nút + có nhãn “Thêm món đồ”, gear “Cài đặt”, avatar “Hồ sơ”. Icon cửa/tuyết trang trí không đọc lặp TalkBack. Đọc lịch theo ngày thật, gồm tháng/năm, selected, today, số giao dịch. Màu đi kèm text/icon; xanh mint không có nghĩa AI cho phép mua. Cooling-off vẫn có thể mở/chạm, không style như disabled. Reduced motion dùng ảnh tĩnh. Chỉ dùng haptic nhẹ khi có chủ đích.

Loading: skeleton ngắn trong vùng dữ liệu, giữ header/nav. Empty ready/cooling: câu ngắn trung tính thay item, không bịa dữ liệu. Offline: chỉ cache và draft theo SPEC; deadline về 0 không tự ghi trạng thái. Error: thông báo + retry tại vùng lỗi. Các trạng thái này là hướng dẫn triển khai, chưa có mockup chi tiết riêng.

## 8. Asset bàn giao

Các PNG trong assets/ được ImageGen tách/tái tạo từ ảnh composite nên có thể khác vài chi tiết; không phải layer gốc pixel-perfect. Không dùng ảnh sản phẩm mẫu làm catalog thật; thay bằng ảnh item của user. PNG giữ alpha; import nguyên file vào drawable-nodpi để không bị suy diễn density. Render với kích thước dp như bảng; nếu cần tối ưu, xuất WebP alpha sau khi kiểm tra viền, không upscale ảnh để tìm thêm chi tiết.

| File | Vai trò | Kích thước hiển thị gợi ý |
|---|---|---|
| mascot_crossed_arms.png | Mèo khoanh tay, không chữ | 88–112dp cao |
| ready_open_door.png | Icon cửa mở xanh mint | 32–36dp |
| cooling_snowflake.png | Icon bông tuyết | 32–36dp |
| sample_jacket.png | Ảnh mock áo khoác | contain trong 64dp |
| sample_headphones.png | Ảnh mock tai nghe | contain trong 64dp |
| sample_keyboard.png | Ảnh mock bàn phím | contain trong 64dp |

Bong bóng lời mèo phải vẽ bằng UI và text thật; không gắn chữ vào asset mèo. Yellow reaction marks là trang trí, có thể nằm trong ảnh mèo. Cần xem ảnh ở nền cream, mint, lavender để kiểm tra alpha; không có nền checkerboard được vẽ vào PNG.
