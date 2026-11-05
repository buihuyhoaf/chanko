# Vòng quỹ đạo tách rời & Animation xoay Ambient cho Node Zigzag (Item Journey)

## Problem Statement
Làm thế nào để các node vật phẩm trên hành trình Zigzag có khoảng thở thị giác thanh thoát và thể hiện trực quan trạng thái "đang suy nghĩ / đang đếm ngược" mà không làm rối mắt hay hao pin?

## Recommended Direction
Triển khai mô hình **"The Magic Orbit Ring"**:
- **Cấu trúc 3 lớp đồng tâm:**
  1. *Lớp lõi (Core Item):* Ảnh sản phẩm tròn (hoặc icon placeholder) lùi vào trong, có đổ bóng nhẹ.
  2. *Khoảng thở (Breathing Gap):* Khoảng cách hở 4-5dp giữa ảnh sản phẩm và vòng viền ngoài, để lộ nền canvas giúp vật phẩm nổi khối như viên ngọc lơ lửng.
  3. *Vòng quỹ đạo ngoài (Orbit Ring):*
     - Với các trạng thái tĩnh (`Saved`, `Ready`, `Purchased`, `Skipped`): Vòng viền liền thanh mảnh (2dp) ôm bên ngoài khoảng thở.
     - Với các trạng thái động (`Waiting` - Đang chờ / Cooling-off, `Considering` - Đang cân nhắc): Vòng nét đứt (`dash = 8dp, gap = 6dp`), tự động xoay tròn 360° với nhịp độ thư thái (8 giây/vòng, `LinearEasing`).
  4. *Huy hiệu trạng thái (Badge):* Nằm ở góc trên phải, hoàn toàn đứng yên trên lớp trên cùng để đảm bảo khả năng nhận diện ký hiệu hình học.
- **Trợ năng (Accessibility):** Tự động tắt hiệu ứng xoay khi người dùng bật `isReducedMotionEnabled`.

## Key Assumptions to Validate
- [x] Hiệu ứng xoay 8s/vòng bằng `graphicsLayer` / Canvas `rotate` đảm bảo mượt mà 60/120fps, không kích hoạt recomposition ảnh món đồ.
- [x] Chỉ 2 trạng thái `Waiting` và `Considering` xoay giúp màn hình giữ được sự cân bằng, không bị quá nhiều chuyển động cạnh tranh sự chú ý.
- [x] Khoảng hở 4-5dp tạo cảm giác thoáng đãng, cao cấp hơn hẳn viền dính sát trước đây.

## MVP Scope
- Cập nhật [`ItemJourneySection.kt`](file:///Users/buihoa/HocTap/Android/chanko/app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/ItemJourneySection.kt):
  - Nhận tham số `isReducedMotionEnabled: Boolean` vào `ItemJourneySection`, `ItemJourneyZigZagCanvas`, `JourneyProductNode`.
  - Tách vòng viền thành Canvas Orbit Ring riêng biệt bao ngoài với khoảng hở 5dp.
  - Sử dụng `rememberInfiniteTransition` tạo góc xoay cho `Waiting` và `Considering`.
- Cập nhật [`HomeScreen.kt`](file:///Users/buihoa/HocTap/Android/chanko/app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt) truyền `state.isReducedMotionEnabled`.
- Kiểm thử unit tests và build packaging.

## Not Doing (and Why)
- **Không xoay toàn bộ node (bao gồm ảnh và badge):** Ảnh sản phẩm và ký hiệu badge cần giữ thẳng đứng để dễ nhìn.
- **Không cho tất cả item cùng xoay:** Tránh gây rối mắt và làm giảm ý nghĩa biểu đạt trạng thái.
- **Không xoay nhanh:** Giữ nhịp ambient 8s/vòng chậm rãi, thư thái.
