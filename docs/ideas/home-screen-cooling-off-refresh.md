# Đề xuất Tinh chỉnh UI/UX Màn hình Home (Home Screen Refresh)

**Ngày cập nhật:** 07/11/2026  
**Trạng thái:** Proposed (Đã tinh lọc qua `/idea-refine`)  
**Mục tiêu:** Cập nhật màn hình Home để phản ánh trọn vẹn luồng Cooling-off mới, kinh tế Hạt/Pate và Mascot Economy tự động, đồng thời bảo toàn tính ổn định của Carousel ngân sách v2.2.

---

## 1. Problem Statement (Vấn đề cần giải quyết)
Sau khi chốt các cơ chế cốt lõi mới (Quick Check $\to$ Freeze bắt buộc $\to$ Nhận Hạt/Pate tự động $\to$ Chanko lớn lên $\to$ Sẵn sàng xem xét $\to$ Ghi nhận mua hàng trừ budget):
- Người dùng chưa có phương thức thuận tiện để thực hiện **Daily Check-in** (xác nhận không mua sắm ngoài dự kiến để nhận Hạt & Pate) ngay trên Home.
- Danh sách món đồ cần thể hiện rõ thời gian đếm ngược Freeze hoặc trạng thái "Sẵn sàng xem xét" mà không làm xáo trộn cấu trúc hiển thị theo trình tự thời gian.
- Mascot Chanko cần phản hồi sinh động về cấp độ và tài nguyên mà không tạo thêm gánh nặng chăm mèo thủ công (`MASCOT-FEEDING = A`).

---

## 2. Hướng đi đề xuất (Recommended Direction)

### 2.1. Daily Check-in: Popup chủ động & Nút mở lại trên Home
1. **Popup Daily Check-in tự động**:
   - Khi người dùng mở app lần đầu trong ngày và chưa check-in: hiển thị một Dialog tinh gọn, nhẹ nhàng.
   - Nội dung: Hỏi nhanh *"Hôm nay bạn có giữ được kế hoạch chi tiêu, không mua sắm ngoài dự kiến chứ?"*
   - Hai lựa chọn:
     - **"Xác nhận (+10 Hạt, +10 Pate)"**: Chanko vui mừng (`MascotEmotion.ENCOURAGING`), cộng tài nguyên tức thì, tự động cập nhật Header.
     - **"Để sau"**: Đóng popup, không ép buộc.
2. **Nút nhỏ mở lại trên Home**:
   - Đặt một nút/badge nhỏ ở khu vực Header/Action bar (cạnh các chip Hạt & Level) với nhãn:
     - Khi chưa check-in: `[Check-in +10 🌰]` (bấm vào mở lại Popup).
     - Khi đã check-in hôm nay: `[Đã check-in ✓]` (disabled, màu nhạt/tick xanh để người dùng yên tâm).

### 2.2. Item Journey: Danh sách đơn nhất sắp xếp theo thời gian
- **Quy tắc sắp xếp**: Giữ 1 danh sách duy nhất, sắp xếp theo thứ tự thời gian được thêm vào (**món mới nhất nằm ở dưới cùng**).
- **Phân biệt trạng thái trên thẻ món đồ**:
  - **Món đang Freeze**: Hiển thị mini countdown (VD: `Còn 2 ngày 14h`) kèm thanh tiến độ thời gian hạ nhiệt.
  - **Món đã kết thúc Freeze (Sẵn sàng xem xét)**: Hiển thị badge xanh nổi bật `[Sẵn sàng xem xét]` và cho phép chạm vào để đưa ra quyết định (Xác nhận mua / Bỏ qua / Freeze lại).
  - Không chia tab hay tách nhóm phức tạp, giữ trải nghiệm cuộn liền mạch.

### 2.3. Mascot Chanko & TopBar Header
- **Vị trí**: Mascot tiếp tục xuất hiện tại Header/Dialogue với câu thoại theo ngữ cảnh (chúc mừng khi check-in, động viên khi hạ nhiệt, trung lập khi mua sắm).
- **Chip Hạt & Level Chanko**: Giữ nguyên vị trí ở Header (đã triển khai), bấm vào mở BottomSheet chi tiết tài nguyên.
- **Level Up Celebration**: Khi Pate vượt ngưỡng cấp độ mới, tự động hiển thị popup chúc mừng Chanko đã lớn hơn.
- **Bảo lưu tuyệt đối**: Giữ nguyên toàn bộ cấu trúc 3 thẻ của `BudgetCreditCardCarousel` (BC22-01..BC22-24).

---

## 3. Key Assumptions (Các giả định cốt lõi)
1. Popup Daily Check-in xuất hiện tối đa 1 lần/ngày khi mở app, nếu người dùng bấm "Để sau" thì không tự ý pop-up lại trong cùng phiên (chỉ mở lại khi bấm nút nhỏ trên Home).
2. Thứ tự thời gian (mới nhất ở dưới cùng) giúp người dùng theo dõi được hành trình từ các món cũ đã hạ nhiệt trước đến các món mới thêm gần đây.
3. Người dùng hài lòng với cơ chế nuôi mèo tự động, không đòi hỏi các nút cho ăn thủ công hay trang trại ảo.

---

## 4. MVP Scope (Phạm vi triển khai)
- [ ] Thêm Popup `DailyCheckInDialog.kt` (hỏi xác nhận không mua sắm, nhận +10 Hạt/+10 Pate).
- [ ] Thêm nút `DailyCheckInPill` trên `HomeHeader.kt` (chuyển trạng thái khi đã check-in).
- [ ] Cập nhật `ItemJourneySection` để sắp xếp item theo `addedTimestamp` tăng dần (mới nhất ở dưới cùng).
- [ ] Cập nhật thẻ item trong Journey để thể hiện rõ trạng thái `Cooling-off` (countdown) và `Ready for Review` (badge nổi bật).
- [ ] Kích hoạt `LevelUpCelebrationDialog` trên HomeScreen khi `ResourceBalance.currentLevel` tăng.

---

## 5. Not Doing (Những điều KHÔNG làm)
- Không chia tab / nhóm danh sách món đồ trên Home thành nhiều phần gây phân mảnh thị giác.
- Không thêm mini-game chăm sóc, cho ăn, tắm rửa cho Chanko Mascot.
- Không đưa các thông báo khuyến khích mua hàng hoặc đếm chuỗi (streak penalty) khi người dùng quên check-in.
- Không thay đổi cấu trúc hoặc luồng vuốt của `BudgetCreditCardCarousel`.
