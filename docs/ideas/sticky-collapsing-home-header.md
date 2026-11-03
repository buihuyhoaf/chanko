# Collapsing & Sticky Home Header

## Problem Statement
Làm thế nào để giữ tiện ích điều hướng Profile, thông tin trạng thái kết nối (Offline) và ngữ cảnh lời chào luôn sẵn sàng khi người dùng cuộn xem nội dung Home mà không chiếm dụng không gian hiển thị thẻ dữ liệu?

## Recommended Direction: Collapsing Frosted Header
- Tách `HomeHeader` khỏi danh sách cuộn nội dung `LazyColumn`, đặt cố định ở lớp trên cùng (`Box` layout neo tại `Alignment.TopCenter`).
- Sử dụng hiệu ứng chuyển đổi trạng thái mượt mà (Interpolation) dựa trên khoảng cách cuộn (`scrollOffset`):
  + **Expanded (Khi ở đỉnh trang):** Hiển thị đầy đủ lời chào buổi (`greetingText` + emoji), dòng phụ đề (`subtitleText`), Avatar viền tím cỡ chuẩn ($52\text{ dp}$) và thanh/badge thông báo trạng thái ngoại tuyến (`lastSyncedAtText` khi offline). Nền tiệp với gradient đỉnh màn hình.
  + **Collapsed (Khi cuộn xuống):** 
    - Dòng phụ đề mờ dần (`alpha` về 0) và ẩn đi.
    - Lời chào co gọn thành 1 dòng tiêu đề ngắn gọn.
    - Avatar thu gọn nhẹ (~$38\text{ dp}$).
    - **Trạng thái Ngoại tuyến:** Thu gọn thành badge mini hoặc chấm cảnh báo nhỏ gọn kèm nhãn "Ngoại tuyến" cạnh Avatar để người dùng luôn nhận biết được dữ liệu chưa đồng bộ mà không tốn diện tích.
    - Chiều cao thanh header cố định ở mức mỏng (~$56\text{ dp} + \text{status bar insets}$).
  + **Xử lý nền (Surface):** Sử dụng nền kính mờ (Frosted glass) với màu `CandyColors.homeScreenBackgroundTop` có độ đục vừa đủ, kèm hiệu ứng đổ bóng siêu nhẹ (`elevation`) khi bắt đầu cuộn để ngăn chữ và thẻ trôi bên dưới bị chồng chéo thị giác.

## Key Assumptions to Validate
- [ ] **Hiệu năng Recomposition:** Việc quan sát `listState.firstVisibleItemScrollOffset` phải được bọc trong `derivedStateOf` hoặc điều khiển qua modifier lambda `graphicsLayer` để không gây hiện tượng tụt khung hình (drop FPS) khi cuộn nhanh trên các thiết bị cấu hình trung bình/thấp.
- [ ] **Độ tương phản & Khả năng đọc (Legibility):** Lớp nền kính mờ phải đủ độ đục để khi thẻ có màu cam đậm rực rỡ (`BudgetCreditCard`) trượt ngầm bên dưới, chữ và huy hiệu offline trên Header vẫn rõ ràng, đạt chuẩn tương phản tiếp cận (Accessibility).
- [ ] **Độ an toàn Edge-to-Edge:** Phần đệm `WindowInsets.statusBars` được neo chuẩn xác ở Header mà không làm hụt lề danh sách khi cuộn hết lên đỉnh.

## MVP Scope
- **In Scope:**
  + Tách biệt `HomeHeader` ra khỏi các items của `LazyColumn`.
  + Tái cấu trúc layout lớp: `Box` chứa `LazyColumn` (có padding top bù đắp bằng chiều cao Header) và `HomeHeader` nổi ở lớp trên.
  + Tính toán chiều cao co giãn, cỡ chữ và độ mờ nền theo khoảng cách cuộn từ $0\text{ px} \to 120\text{ px}$.
  + Duy trì đầy đủ khả năng bấm Avatar để mở Profile & Settings ở cả hai trạng thái.
  + Hiển thị chỉ báo ngoại tuyến (Offline Indicator) ở cả trạng thái mở rộng lẫn thu gọn.
- **Out of Scope (Chưa làm ở MVP):**
  + Các hiệu ứng vật lý nảy lò xo (overscroll spring/bounce animation) khi chạm đáy/đỉnh.
  + Ẩn hoàn toàn thanh header khi cuộn xuống và chỉ trượt xuống lại khi cuộn ngược lên (Quick Return).

## Not Doing (and Why)
- **Không ghim nguyên khối Header to lớn hiện tại:** Chiều cao cố định ~88–100dp sẽ chiếm tới gần 20% màn hình, gây chật chội và che khuất nội dung quan trọng của danh sách thẻ bên dưới.
- **Không dùng nền Header trong suốt:** Sẽ gây lỗi chồng chéo chữ (text overlapping) nghiêm trọng khi các thẻ nội dung bên dưới trượt lên.
- **Không ẩn badge ngoại tuyến:** Người dùng cần luôn biết tình trạng dữ liệu offline để tránh nhầm lẫn khi số dư ngân sách hoặc hành trình món đồ chưa được cập nhật mới nhất.

## Open Questions
- Khi click vào badge/chấm ngoại tuyến ở trạng thái Collapsed, có cần bật tooltip/snackbar thông báo chi tiết thời gian đồng bộ lần cuối (`lastSyncedAtText`) hay không?
