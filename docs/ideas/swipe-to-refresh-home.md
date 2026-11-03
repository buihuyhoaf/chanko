# [Ý tưởng] Swipe-to-Refresh trên Home Screen (Chanko v2.0)

## 1. Problem Statement
> Làm thế nào để người dùng có thể chủ động vuốt kéo xuống trên Trang chủ để cập nhật tức thì ngân sách, trạng thái món đồ (hết hạn chờ Cooling-off) và góc nhìn mới từ Chanko một cách mượt mà, đúng chuẩn Material 3 với bảng màu thương hiệu Candy mà vẫn giữ nguyên vị trí cuộn đang đọc?

## 2. Recommended Direction
- **Tầng UI (Jetpack Compose Material 3):**
  - Tích hợp `PullToRefreshBox` chuẩn của Compose Material 3 bao bọc `HomeScreen`.
  - Indicator hiển thị hình tròn nổi nền trắng bo tròn mềm mại (`shape = CircleShape`), mũi tên và vòng xoay sử dụng màu tím thương hiệu `CandyColors.brandPurple` (#7C4DFF).
- **Tầng ViewModel & Logic:**
  - Kết nối với `HomeUiEvent.RefreshHome` và state `isRefreshing` trong `HomeUiState`.
  - Làm mới toàn bộ 3 khu vực: Thẻ ngân sách, Hành trình món đồ (đối soát Cooling-off), và Góc nhìn từ Chanko.
  - Bảo toàn `scrollAnchorItemId` để giữ nguyên vị trí cuộn sau khi làm mới (`AC-19`, `AC-30`).
- **Trường hợp ngoại tuyến (Offline):**
  - Dừng xoay spinner ngay lập tức và phát `HomeUiEffect.ShowOfflineBlockedMessage` hiển thị Snackbar: *"Bạn đang ngoại tuyến. Dữ liệu được giữ từ lần đồng bộ gần nhất."*

## 3. Key Assumptions to Validate
- [ ] `PullToRefreshBox` tương thích tốt với `LazyColumn` lồng bên trong trên tất cả các phiên bản Android hỗ trợ.
- [ ] Thao tác vuốt không can thiệp vào cử chỉ giữ tay trên node (`JourneyNodeGestureStarted` / snapshot lock).

## 4. MVP Scope
- Tích hợp `PullToRefreshBox` vào `HomeRoute` / `HomeScreen`.
- Kết nối `isRefreshing` và sự kiện `HomeUiEvent.RefreshHome` trong `HomeViewModel`.
- Bổ sung unit tests kiểm tra logic `RefreshHome` cả khi Online lẫn Offline.

## 5. Not Doing (and Why)
- Không làm hoạt ảnh linh vật kéo giãn lò xo phức tạp để tránh over-engineering và giật khung hình.
- Không phục hồi các insight mà người dùng đã bấm ẩn (`hiddenInsightIds`).
