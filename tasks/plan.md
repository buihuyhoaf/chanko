# Implementation Plan: Màn hình Home (Chanko Home v5)

## Overview
Xây dựng và tích hợp hoàn chỉnh màn hình Trang chủ (`HomeScreen`) cho ứng dụng Chanko theo đúng bản thiết kế chốt v5 tiếng Việt (`UI-HANDOFF.md`). Màn hình bao gồm: Header lời chào, Thẻ Lịch chi tiêu tương tác theo chu kỳ neo thực tế (với nhãn thứ động, trạng thái ngày, linh vật Mèo 3D kèm bong bóng thoại, thống kê ngân sách 3 cột), Danh sách món đồ "Sẵn sàng xem lại" (mint card + nút Xem lại $\ge 48\text{dp}$), Danh sách món đồ "Đang cân nhắc" (timer đếm ngược), và Thanh điều hướng BottomBar 4 tab với nút Thêm (+) trung tâm.

## Architecture Decisions
1. **Clean Architecture & MVVM:** Presentation layer độc lập, `HomeViewModel` kết nối với `ItemRepository` (cho danh sách món đồ) và `HomeSpendingRepository` (in-memory mock provider cho chu kỳ chi tiêu và giao dịch mẫu).
2. **Cycle Calendar Logic (Lưới lịch chu kỳ):** Tự viết bằng Compose thuần, không dùng thư viện ngoài. Thứ của cột đầu tiên trong lưới lịch được tính toán động từ ngày neo (`anchorDay`, ví dụ: ngày 15 bắt đầu Thứ Năm $\rightarrow$ cột 1 là T5).
3. **Mascot Speech Bubble:** Asset 3D [mascot_crossed_arms.png](file:///Users/buihoa/Downloads/chanko-home-handoff/assets/mascot_crossed_arms.png) tách nền trong suốt tại `drawable-nodpi/`. Bong bóng thoại được vẽ bằng Compose UI vector và text thật để linh hoạt thay đổi nội dung theo tiến độ ngân sách.
4. **Custom BottomBar Navigation:** Giữ nguyên 4 tab chuẩn của ứng dụng (`Trang chủ`, `Món đồ`, `Ngân sách`, `Phân tích`), tạo khoảng hở trung tâm $64-72\text{dp}$ cho nút Floating Action Button (+) kích thước $56\text{dp}$. Nút (+) cung cấp callback `onAddNewItemClick: () -> Unit`.
5. **Accessibility & Responsive:** Mọi touch target $\ge 48\text{dp}$, font scale tới 200%, hỗ trợ co giãn mượt từ $320\text{dp}$ tới $393\text{dp}$, 100% chuỗi tiếng Việt trong `strings_home.xml`.

---

## Task List

### Phase 1: Foundation (Tài nguyên & Mô hình dữ liệu)
- [ ] **Task 1: Assets, Strings & Design Tokens Setup**
- [ ] **Task 2: Home Domain Models & Cycle Calculation Logic**

### Checkpoint: Foundation
- [ ] Toàn bộ asset PNG đã copy vào `drawable-nodpi/`.
- [ ] String tiếng Việt và Color tokens biên dịch không lỗi.
- [ ] Unit tests cho logic chu kỳ chi tiêu (`SpendingCycleTest`) pass 100%.

---

### Phase 2: State Engine & Presentation Logic
- [ ] **Task 3: HomeUiState, HomeUiEvent, HomeViewModel & Unit Tests**

### Checkpoint: State Engine
- [ ] `HomeViewModel` nạp dữ liệu từ repository, phát ra state chính xác.
- [ ] Unit tests `HomeViewModelTest` kiểm tra date selection, today jump, budget progress, filtering pass 100%.

---

### Phase 3: Vertical Slices UI Components
- [ ] **Task 4: Calendar & Budget UI Components (`SpendingCalendarCard`)**
- [ ] **Task 5: Review & Cooling Item UI Components (`ReadyReviewSection`, `CoolingOffSection`)**

### Checkpoint: UI Slices
- [ ] Các component con độc lập, có Preview rõ ràng.
- [ ] Touch target $\ge 48\text{dp}$, không hardcode màu sắc hay chuỗi.

---

### Phase 4: Screen Assembly & Navigation
- [ ] **Task 6: Screen Assembly (`HomeScreen`), Stateful Route & Previews**
- [ ] **Task 7: Custom BottomBar Navigation & Home Route Wiring**

### Checkpoint: Screen & Navigation Complete
- [ ] `HomeScreen` hiển thị đầy đủ trên `MainNavigation` tại tab Trang chủ.
- [ ] Nút (+) ở trung tâm hoạt động đúng qua callback.
- [ ] Build ứng dụng không phát sinh lỗi.

---

### Phase 5: Quality Gate & Regression Verification
- [ ] **Task 8: End-to-End Quality Gate & Regression Verification**

### Checkpoint: Complete & Done
- [ ] Toàn bộ unit tests pass: `./gradlew testDebugUnitTest`.
- [ ] Build debug thành công: `./gradlew assembleDebug`.
- [ ] Kiểm tra không vi phạm quy tắc dự án (không chạy emulator/device, không sửa skills, mã sạch).

---

## Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Lưới lịch bắt đầu theo ngày neo (không phải Thứ Hai chuẩn) | High | Viết pure Kotlin algorithm tính toán offset và danh sách thứ, kiểm chứng bằng Unit Test trước khi render UI. |
| Chia cho 0 khi ngân sách bằng 0 hoặc null | High | Hàm an toàn `safeBudgetProgress(spent, budget)` trả về 0f khi `budget <= 0`. |
| Tràn chữ trên màn hình hẹp 320dp hoặc khi font scale 200% | Medium | Lưới lịch dùng `weight(1f)` linh hoạt, 3 cột ngân sách dùng layout reflow tự động xuống dòng khi thiếu chỗ, không ellipsis số tiền. |
| Asset PNG bị vỡ hoặc mất độ sắc nét | Low | Đặt nguyên bản tại thư mục `drawable-nodpi/` theo hướng dẫn handoff §8. |

## Open Questions
- Không còn (Các câu hỏi về Mock Provider cho Daily Purchases, hành vi nút (+), và nhánh Git đã được người dùng phê duyệt).
