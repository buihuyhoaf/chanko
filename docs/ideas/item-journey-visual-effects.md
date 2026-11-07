# Đề xuất Nâng cấp Hiệu ứng & Màu viền Item Journey (Cooling-off Visuals)

**Ngày cập nhật:** 07/11/2026  
**Trạng thái:** Refined (Sau quá trình `/idea-refine`)  
**Mục tiêu:** Tinh chỉnh hệ thống màu viền và hiệu ứng hình ảnh của các node món đồ trên Item Journey (Home Screen) nhằm phản ánh trực quan tiến độ hạ nhiệt (Cooling-off Freeze) và hướng sự chú ý của người dùng vào các món đã "Sẵn sàng xem xét".

---

## 1. Problem Statement (Vấn đề cần giải quyết)
- Hiện tại, vòng tròn bên ngoài (Orbit Ring) của các node món đồ đang sử dụng hiệu ứng xoay 360 độ liên tục (`rotationAngle`) cho các trạng thái `Waiting` hoặc `Considering`.
- **Hạn chế:**
  1. Chuyển động xoay vòng 360 độ vô tận chỉ biểu thị trạng thái "đang hoạt động" chung chung, **không cho người dùng biết món đồ đã hạ nhiệt được bao nhiêu %** hoặc còn bao lâu nữa thì hoàn thành.
  2. Tạo cảm giác sốt ruột và gây nhiễu thị giác không cần thiết khi người dùng lướt màn hình Home.
  3. Món đang Freeze (cần kiên nhẫn, không đụng vào) lại có hiệu ứng xoay mạnh mẽ hơn món đã hoàn thành Freeze (Sẵn sàng xem xét — CTA chính cần người dùng tương tác).

---

## 2. Hướng đi đề xuất (Recommended Direction)

### 2.1. Phân tầng thị giác (Visual Hierarchy): "Mời gọi xem xét, làm dịu hạ nhiệt"
- **Món "Sẵn sàng xem xét" (Ready for Review) — Ưu tiên cao nhất (Primary Focus / CTA):**
  - Viền tròn khép kín màu Mint/Xanh ngọc tươi sáng (`CandyColors.HomeV2.statusReadyRing`).
  - Hiệu ứng nhịp thở nhẹ (Subtle Glow / Breathing Pulse: chu kỳ 2–3s) trên viền hoặc bóng đổ, kích thích nhẹ nhàng người dùng bấm vào xem chi tiết để đưa ra quyết định mua hay bỏ qua.
  - Badge góc trên phải hiển thị dấu tích xanh nổi bật.
- **Món "Đang Freeze" (Cooling-off Active / Waiting) — Trực quan hóa tiến độ tĩnh dịu (Calm Progress Arc):**
  - Thay vì xoay 360 độ vô nghĩa, viền ngoài chuyển thành **Thanh tiến trình hình tròn (Circular Countdown Arc)**.
  - Vòng viền chạy từ góc 12h (hoặc -90 độ):
    - Chiều dài cung viền tỷ lệ với thời gian Freeze còn lại: $\text{Sweep Angle} = 360^\circ \times \frac{\text{remainingMillis}}{\text{totalDurationMillis}}$.
    - Cung viền thu hẹp dần khi thời gian trôi qua (hoặc cung viền hoàn thành dần), với nền rãnh mờ (track mờ 20% alpha).
  - Không có animation xoay tít; chỉ cập nhật góc cung arc. Giữ sắc thái yên ả, tĩnh lặng, hỗ trợ tâm lý "bình tĩnh hạ nhiệt".
- **Món Đã mua (Purchased) & Đã bỏ qua (Skipped) — Hoàn tất (Completed / Subdued):**
  - Viền nét liền tĩnh, màu sắc trung tính dịu nhẹ (Slate / SlateBlue).
  - Không có bất kỳ animation nào, nằm yên vị trên hành trình thời gian.

---

## 3. Key Assumptions (Các giả định cốt lõi)
1. Người dùng chỉ cần nhìn lướt qua viền tròn là biết ngay món đồ còn bao nhiêu phần trăm thời gian Freeze mà chưa cần phải bấm vào xem text chi tiết.
2. Hiệu ứng thở (pulse) nhẹ nhàng chỉ dành riêng cho món `Ready` sẽ giúp chuyển đổi hành động (Actionable Decision) tốt hơn mà không gây ức chế thị giác.
3. Chế độ giảm chuyển động (`isReducedMotionEnabled = true`) sẽ tắt hoàn toàn hiệu ứng thở (pulse) của món `Ready`, chỉ giữ viền tĩnh rõ ràng để tuân thủ WCAG.

---

## 4. MVP Scope (Phạm vi triển khai)
- [ ] **Circular Countdown Arc (`ItemJourneySection.kt`)**:
  - Tính toán `progress = remainingSeconds / totalSeconds` (hoặc nội suy từ `createdAt` và thời gian Freeze theo phân khúc giá).
  - Vẽ Canvas `drawArc` với đường nét tròn (`StrokeCap.Round`), rãnh nền nhạt và cung viền màu cam ấm/hổ phách hoặc lavender đếm ngược.
- [ ] **Ready Node Pulse Animation (`ItemJourneySection.kt`)**:
  - Thêm `infiniteRepeatable` dạng `tween` nhịp thở từ tốn (alpha từ 0.35f đến 0.7f cho shadow/viền) khi `status == Ready` và `!isReducedMotionEnabled`.
- [ ] **Badge & Semantics**:
  - Cập nhật talkback/semantics: đọc rõ *"Đang hạ nhiệt, còn X ngày"* hoặc *"Đã sẵn sàng xem xét, chạm để quyết định"*.
- [ ] **Kiểm thử & Thẩm mỹ**:
  - Đảm bảo vượt qua `ColorArchitectureTest` (sử dụng 100% token từ `CandyColors`).
  - Kiểm tra Preview các kịch bản: `COOLING_EXPIRED`, `DEFAULT_FIVE_NODES`, `ONE_ITEM`.

---

## 5. Not Doing (Những điều KHÔNG làm)
- Không dùng hiệu ứng tuyết rơi, tia băng giá hay hạt lấp lánh (particle systems) phức tạp làm nặng hiệu năng hoặc biến app thành game.
- Không xoay tròn 360 độ vô nghĩa gây chóng mặt.
- Không thay đổi đường nối ziczac nét đứt màu lavender (`#BDA8EB`) đã đạt chuẩn giữa các node.
