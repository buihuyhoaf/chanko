# Capability Map: Chanko Core (Cooling-off, Rewards, Mascot Economy, Budget Tracking & Notifications)

> **Cập nhật ngày:** 07/11/2026  
> **Trạng thái:** Chờ phê duyệt Phase 0 (Gated Workflow: Spec-Driven Development)  
> **Tài liệu nguồn:** Quyết định đã chốt ngày 07/11/2026

---

## 1. Phân rã Trách nhiệm Module (Capability Map)

Sáng kiến này bao gồm nhiều năng lực độc lập có thể kiểm thử và triển khai riêng rẽ, liên kết theo một chiều không chu trình (DAG):

| Module ID | Trách nhiệm chính (Responsibility) | Phụ thuộc (Depends on) |
| :--- | :--- | :--- |
| **`cooling-off-core`** | Quản lý toàn bộ vòng đời sản phẩm từ Quick Check đến Cooling-off: bắt buộc Freeze (không bỏ qua), Chanko tự ấn định thời gian chờ, khóa giá sản phẩm tại thời điểm Freeze, chuyển trạng thái "Sẵn sàng xem xét" khi hết hạn, hỗ trợ Unfreeze sớm và Freeze lại (chặn trùng thưởng). | `core:domain`, `core:database` |
| **`rewards-mascot-economy`** | Quản lý sổ cái tài nguyên Hạt và Pate (thay thế Point/EXP), tiến trình Level của Chanko (tự động lên cấp, Level không bao giờ tụt). Quy tắc thưởng Freeze tự động (tối đa 1 lần/ngày), thưởng Daily Check-in cố định, công thức phạt Unfreeze sớm theo thời gian còn lại và giá sản phẩm (số dư không âm), cơ chế thu hồi thưởng và phạt hồi tố. | `cooling-off-core` |
| **`budget-item-tracking`** | Theo dõi chi tiêu chỉ dựa trên các sản phẩm đã thêm vào Chanko khi có xác nhận mua hàng thực tế (hỗ trợ Pay Now & thẻ tín dụng ghi nhận ngay, hoàn tiền, điều chỉnh hồi tố có audit trail). Cơ chế ngoại tuyến: chỉ đọc cache, chặn ghi dữ liệu. | `cooling-off-core` |
| **`home-mascot-presentation`** | Tích hợp hiển thị số dư Hạt, thanh tiến độ Pate, Level trên Home TopBar. Mascot phản ứng cảm xúc theo ngữ cảnh (vui vẻ khi nhận thưởng/lên cấp, giọng điệu trung lập khi Unfreeze). Tích hợp hiển thị trạng thái Freeze trên thẻ sản phẩm mà không làm đảo lộn Budget Carousel hiện hữu. | `cooling-off-core`, `rewards-mascot-economy`, `budget-item-tracking` |
| **`notifications-daily-checkin`** | Hệ thống Push Notification và Daily Email: chủ động hỏi người dùng đã mua sản phẩm hay chưa, nhắc Daily Check-in, thông báo sản phẩm sắp/đã hết Freeze, thông báo nhận thưởng Hạt/Pate và Level up, email tổng kết định kỳ. | `cooling-off-core`, `rewards-mascot-economy`, `budget-item-tracking` |

---

## 2. Thứ tự triển khai (Build Order)

```
cooling-off-core
      │
      ├───────────────────────────────┐
      ▼                               ▼
rewards-mascot-economy     budget-item-tracking
      │                               │
      ├───────────────────────────────┘
      │
      ├───────────────────────────────┐
      ▼                               ▼
home-mascot-presentation   notifications-daily-checkin
```

1. **Bước 1:** `cooling-off-core` (Nền tảng trạng thái sản phẩm và thời gian Freeze).
2. **Bước 2 (Song song):** `rewards-mascot-economy` và `budget-item-tracking`.
3. **Bước 3 (Song song):** `home-mascot-presentation` và `notifications-daily-checkin`.

---

## 3. Các giả định kỹ thuật ban đầu (Assumptions)

1. **Nền tảng:** Android native (Kotlin, Jetpack Compose, Room Database, Kotlin Coroutines & Flow).
2. **Kiến trúc:** Clean Architecture chia theo multi-module (`core:domain`, `core:database`, `core:data`, `core:network`, `app`).
3. **Tính toán thời gian Freeze:** Thời gian Freeze chạy theo đồng hồ thực tế UTC/Epoch timestamp lưu trong database để tính toán thời gian còn lại độc lập với việc mở ứng dụng.
4. **Bảo toàn dữ liệu hiện tại:** Giữ nguyên cấu trúc `BudgetCreditCardCarousel` (v2.2) và Item Journey trên Home, không thay thế bằng mini-game nuôi mèo.

---

## 4. Các điểm còn mở cần chốt thông số (Open Decisions)

| Mã | Nội dung còn mở | Đề xuất mặc định (nếu có) |
| :--- | :--- | :--- |
| **REWARD-01** | Số Hạt/Pate cố định cho Daily Check-in | 10 Hạt + 10 Pate mỗi ngày |
| **REWARD-02** | Tỷ lệ % thưởng Freeze theo giá | Cần chốt hệ số hoặc trần tối đa (cap) |
| **REWARD-03** | Tỷ lệ % chi phí Unfreeze sớm | $\text{Chi phí} = \alpha \times \text{Giá} \times \frac{t_{\text{còn}}}{T}$ |
| **REWARD-04** | Thời gian Freeze cụ thể theo phân khúc giá | Đề xuất: 1 ngày (<200k), 3 ngày (200k-1tr), 7 ngày (1tr-5tr), 14 ngày (>5tr) |
| **REWARD-05** | Ngưỡng Pate của từng Level Chanko | Cần công thức lũy tiến (Level $n \to n+1$) |
| **REWARD-06** | Thời gian Freeze có ảnh hưởng thêm đến thưởng hay không | Có cộng thêm trọng số thời gian hay chỉ thuần theo giá |
| **REWARD-07** | Xử lý Level và số dư khi thu hồi thưởng hồi tố | Số dư về 0 (không âm); Level đã đạt có bị hạ hay giữ nguyên? |
| **NOTI-01** | Giờ gửi Push và Daily Email | Đề xuất: Push 20:00 hàng ngày, Email 21:00 Chủ Nhật |
| **NOTI-02** | Tần suất hỏi người dùng đã mua sản phẩm chưa | Đề xuất: 1 lần/ngày trong khung giờ tối |
| **CHECKIN-01** | Phạm vi xác nhận không mua sắm và xử lý khi khai báo mua hồi tố | Check-in xác nhận "Hôm nay tôi không mua ngoài dự kiến" |
