# Đặc Tả Kỹ Thuật: Chanko Budget UI v2.0 (Spec-Driven Development)

> **Tài liệu tham chiếu:** `chanko-budget-ui-v2.0-handoff 3` (Bản cập nhật Final Screen Coverage Review: 13/13 READY)  
> **Trạng thái:** SPECIFICATION APPROVED FOR PLANNING  
> **Target Variant:** `devDebug` (Compile & Unit Test Verification)  
> **Navigation Baseline:** Khởi tạo từ `BudgetCard` trên `HomeScreen` (`HomeUiEffect.NavigateToBudgetDetails`)

---

## 1. Objective (Mục Tiêu)

Xây dựng toàn diện giao diện và luồng tương tác của phân hệ **Ngân sách (Chanko Budget UI v2.0)** bằng **Jetpack Compose** kết hợp **In-memory Fake Repository & Fixtures**, đảm bảo đáp ứng đầy đủ 13 hạng mục (C01–C13) theo chuẩn Screen Coverage Review và 18 tiêu chuẩn nghiệm thu (AC01–AC18).

### 1.1. User Stories & Acceptance Overview
1. **Theo dõi nhịp ngân sách (C01, C10, C13):** Khi chạm vào `BudgetCard` ở Home, người dùng xem được tổng quan chu kỳ hiện tại: ngân sách, đã chi, còn lại/vượt mức (C10), thanh tiến trình kẹo, mascot trạng thái, danh sách phương thức thanh toán, tối đa 3 món đáng chú ý và tối đa 3 biến động gần nhất. Trường hợp chưa chi tiêu hiển thị giao diện nhẹ nhàng (C13).
2. **Duyệt chu kỳ lịch sử (C02, C12):** Mở sheet chọn chu kỳ để xem lại số liệu các kỳ trước ở chế độ chỉ đọc (C12).
3. **Cấu hình ví & quản lý thẻ (C03, C05–C08):** Xem anchor chu kỳ chỉ đọc; quản lý danh sách thẻ tín dụng (chỉ lưu metadata tên thẻ 1–40 ký tự để phân loại chi tiêu, thêm C06, đổi tên C07, xóa C08). Xóa thẻ là lưu trữ (archive), không xóa lịch sử chi tiêu.
4. **Sửa ngân sách & Xem trước tức thì (C04):** Mở form sửa hạn mức, hỗ trợ Live Preview tính toán lại còn lại/vượt mức và thanh progress theo từng ký tự nhập; cảnh báo rõ ràng khi giảm hạn mức dưới số đã chi.
5. **Xem lịch sử điều chỉnh hạn mức (C09):** Xem các lần tăng/giảm ngân sách của chu kỳ đang chọn.
6. **Bảo vệ ngoại tuyến (C11):** Khi mất mạng, hiển thị banner ngoại tuyến, khóa toàn bộ thao tác thêm/sửa/xóa, cho phép xem dữ liệu cache.

---

## 2. Tech Stack & Commands

* **Ngôn ngữ & Nền tảng:** Kotlin 2.x, Android SDK (Target 34+), Jetpack Compose (Material3 + Custom Candy Pop Design Tokens).
* **Kiến trúc:** Clean Architecture + MVI/MVVM (Stateless Composable + StateFlow ViewModel + Pure Domain Model + In-memory Fake Repository).
* **Kiểm thử:** JUnit 4, Kotlinx Coroutines Test, Compose UI Semantics Testing.

### Executable Commands:
```bash
# Compile & kiểm tra lỗi build
./gradlew assembleDevDebug

# Chạy toàn bộ Unit Tests của phân hệ & dự án
./gradlew testDevDebugUnitTest

# Chạy Lint kiểm tra quy chuẩn
./gradlew lintDevDebug
```

---

## 3. Project Structure (Tổ Chức Mã Nguồn)

```
app/src/main/java/com/hoabh/chanko/
├── domain/budget/
│   ├── model/
│   │   ├── MoneyVnd.kt                  # Value object tiền nguyên VND, format, guard chia 0
│   │   ├── BudgetCycle.kt               # Chu kỳ: id, label, dateRange, isCurrent, anchorDate
│   │   ├── BudgetSnapshot.kt            # Snapshot: budget, spent, remaining, over, ratio, progress
│   │   ├── CreditCard.kt                # Metadata thẻ: id, name, isArchived
│   │   ├── BudgetActivity.kt            # Event: id, cycleId, itemId?, type, amount, occurredAt
│   │   └── BudgetAdjustment.kt          # History: id, cycleId, oldLimit, newLimit, change, adjustedAt
│   └── repository/
│       └── BudgetRepository.kt          # Interface hợp đồng dữ liệu cho phân hệ ngân sách
│
├── data/budget/
│   └── FakeBudgetRepository.kt          # In-memory repository cung cấp 100% fixture C10-C13 & mutation
│
├── presentation/screens/budget/
│   ├── BudgetRoute.kt                   # Stateful screen: kết nối ViewModel, Navigation, BackHandler
│   ├── BudgetScreen.kt                  # Stateless C01 Overview Screen
│   ├── BudgetUiState.kt                 # StateFlow model đa chiều (Cycle, Network, Load, Spend)
│   ├── BudgetUiEvent.kt                 # User events từ UI lên ViewModel
│   ├── BudgetUiEffect.kt                # One-time side effects (Snackbar, Navigate to Shared Item Detail)
│   ├── BudgetViewModel.kt               # Quản lý state, live preview, FSM permission reducer
│   ├── components/
│   │   ├── BudgetSummaryCard.kt         # Card ngân sách, 3 số liệu, progress bar, nút sửa C04
│   │   ├── BudgetCycleHeader.kt         # Button chọn chu kỳ + chevron (mở C02)
│   │   ├── BudgetMascotSlot.kt          # Slot mascot hiển thị trạng thái cảm xúc tĩnh
│   │   ├── BudgetPaymentBreakdown.kt    # Danh sách phương thức thanh toán trong kỳ (chỉ đọc)
│   │   ├── BudgetNotableItems.kt        # Tối đa 3 món đã lưu (tap mở Shared Item Detail)
│   │   ├── BudgetRecentActivity.kt      # Tối đa 3 biến động ảnh hưởng ngân sách
│   │   └── BudgetReadOnlyBanner.kt      # Banner cảnh báo Offline (C11) và Chu kỳ cũ (C12)
│   └── sheets/
│       ├── BudgetSheetHost.kt           # Host quản lý ModalBottomSheet duy nhất & backstack nội bộ
│       ├── CyclePickerSheet.kt          # C02 Sheet chọn chu kỳ
│       ├── WalletSettingsSheet.kt       # C03 Sheet thiết lập ví
│       ├── EditBudgetSheet.kt           # C04 Sheet sửa ngân sách & Live Preview
│       ├── CreditCardManageSheet.kt     # C05 Sheet quản lý thẻ tín dụng
│       ├── AddCreditCardSheet.kt        # C06 Sheet thêm thẻ tín dụng
│       ├── RenameCreditCardSheet.kt     # C07 Sheet đổi tên thẻ tín dụng
│       ├── DeleteCardConfirmDialog.kt   # C08 AlertDialog xác nhận xóa thẻ
│       ├── DiscardChangesDialog.kt      # Dialog xác nhận bỏ thay đổi khi form dirty
│       └── AdjustmentHistorySheet.kt    # C09 Sheet lịch sử điều chỉnh hạn mức
│
└── presentation/navigation/
    ├── Screen.kt                        # Thêm Screen.BudgetDetails ("budget_details")
    └── MainNavigation.kt                # Hook HomeUiEffect.NavigateToBudgetDetails mở BudgetRoute
```

---

## 4. Code Style & Architectural Conventions (Quy Chuẩn Code)

### 4.1. Định dạng Tiền Tệ & Phép Tính Số Học
* **Không dùng `Float` hay `Double`** cho số tiền. Mọi số liệu tiền sử dụng kiểu `Long` (VND).
* Số tiền hiển thị qua formatter chuẩn tiếng Việt: `5000000L` $\rightarrow$ `5.000.000đ`.
* Công thức tính toán:
  * `remaining = budget - spent`
  * `over = max(spent - budget, 0)`
  * Nếu `budget > 0`: `percentage = (spent * 100f) / budget`.
  * Thanh Progress: `visualProgress = (percentage / 100f).coerceIn(0f, 1f)`.
  * Nhãn phần trăm: hiển thị số nguyên làm tròn gần nhất (Half-Up), vẫn hiển thị trung thực $>100\%$ khi vượt hạn mức.
  * Guard `budget = 0`: `spent = 0` hiển thị *"Chưa thiết lập ngân sách"*, không chia cho 0.

### 4.2. Kiến Trúc Single Sheet Host (Chống Xung Đột Modal)
* Để tuân thủ quy tắc *"Không stack nhiều sheet"*, `BudgetRoute` duy trì một `BudgetSheetDestination` trong UI State:
```kotlin
sealed interface BudgetSheetDestination {
    data object CyclePicker : BudgetSheetDestination                  // C02
    data object WalletSettings : BudgetSheetDestination               // C03
    data class EditBudget(val currentLimit: Long) : BudgetSheetDestination // C04
    data object CreditCardManage : BudgetSheetDestination             // C05
    data object AddCreditCard : BudgetSheetDestination                // C06
    data class RenameCreditCard(val card: CreditCard) : BudgetSheetDestination // C07
    data object AdjustmentHistory : BudgetSheetDestination            // C09
}
```
* Khi chuyển từ C03 $\rightarrow$ C05 $\rightarrow$ C06, host cập nhật nội dung sheet hiện tại và lưu lịch sử quay lại (Back Stack nội bộ).
* C08 (Xóa thẻ) và DiscardDialog là `AlertDialog` độc lập phủ trên ModalBottomSheet hiện thời.

### 4.3. Quyền Ghi Tập Trung (Centralized Permission Reducer)
Trạng thái có quyền chỉnh sửa (`canWrite`) được tính toán tập trung tại Domain/ViewModel:
```kotlin
val canWrite: Boolean = isOnline && isCurrentCycle && !isLoading && !isSubmitting
```
Nếu `!canWrite`, toàn bộ nút Lưu / Thêm / Sửa / Xóa đều bị khóa và hiển thị nhãn lý do (`Chỉ chỉnh sửa trong chu kỳ hiện tại` hoặc `Kết nối mạng để chỉnh sửa`).

---

## 5. Testing Strategy (Chiến Lược Kiểm Thử)

1. **Unit Tests Nghiệp Vụ (`test/`):**
   * `MoneyVndTest`: Kiểm tra định dạng tiền, parse chuỗi dấu chấm phân tách, phép trừ và guard chia 0.
   * `BudgetCalculatorTest`: Kiểm tra các kịch bản chuẩn C10 (Over-budget: 5m/5.5m), C13 (Zero-spend: 5m/0), tỷ lệ phần trăm làm tròn Half-Up.
   * `CreditCardValidationTest`: Kiểm tra độ dài 1–40 ký tự, chuẩn hóa NFC, trim khoảng trắng, phát hiện trùng tên case-insensitive.
   * `FakeBudgetRepositoryTest`: Kiểm tra nạp fixture, idempotency chống click đúp, archive thẻ không làm mất số tiền chi tiêu cũ.
   * `BudgetViewModelTest`: Kiểm tra chuyển đổi chu kỳ giữ đúng snapshot, FSM permission reducer (Offline/Past-cycle thắng quyền ghi), Live Preview cập nhật theo từng ký tự.
2. **Compose Semantics & A11y Tests:**
   * Kiểm tra thứ tự TalkBack của các thành phần C01: App bar $\rightarrow$ Cycle $\rightarrow$ Summary $\rightarrow$ Mascot $\rightarrow$ Methods $\rightarrow$ Items $\rightarrow$ Activity.
   * Kiểm tra vùng chạm tối thiểu $48\text{dp}$ cho tất cả các nút và icon $24\text{dp}$.
   * Kiểm tra khả năng mở rộng font size $200\%$ không bị cắt chữ hoặc che khuất nút CTA.

---

## 6. Boundaries (Ranh Giới Bắt Buộc)

| Phân loại | Quy tắc |
| :--- | :--- |
| **Always do** | • Chạy `./gradlew testDevDebugUnitTest` trước khi báo cáo hoàn thành.<br>• Mọi chuỗi hiển thị phải lấy từ `strings.xml`.<br>• Tiền VND là số nguyên `Long`.<br>• Đóng dialog/sheet dirty phải hỏi xác nhận "Bỏ thay đổi?". |
| **Ask first** | • Bổ sung thư viện mới vào `build.gradle.kts`.<br>• Thay đổi database Room production.<br>• Thêm navigation arguments mới vào Shared Item Detail. |
| **Never do** | • **CẤM** cài đặt ứng dụng lên máy ảo, Android emulator hoặc AVD.<br>• Không tạo route riêng cho Item Detail hoặc Decision Timeline trong Budget.<br>• Không lưu số thẻ tín dụng hoặc CVV.<br>• Không cho phép ghi ngoại tuyến (Offline mutation).<br>• Không sửa đổi agent skills. |

---

## 7. Success Criteria & Ma Trận Nghiệm Thu (Acceptance Criteria Mapping)

| AC ID | Hạng mục kiểm tra | Tiêu chuẩn đạt |
| :--- | :--- | :--- |
| **AC01** | C01 Current Online | Hiển thị đúng cycle hiện tại, 3 số liệu chuẩn, không có Decision Timeline riêng. |
| **AC02** | C02 / C12 Past Cycle | Chọn chu kỳ cũ $\rightarrow$ dữ liệu đổi theo kỳ đó, badge "Đã kết thúc · Chỉ xem", khóa mọi quyền ghi. |
| **AC03** | C13 Zero Spend | Fixture 5m/0đ/5m/0%; empty copy nhẹ nhàng; item đã lưu vẫn hiển thị; không bù giao dịch giả. |
| **AC04** | C10 Over Budget | Fixture 5m/5.5m; nhãn "Vượt ngân sách 500.000đ"; tỷ lệ 110%; thanh bar clamp 100%; không trách móc. |
| **AC05** | C11 Offline Cache | Banner offline kèm thời gian cập nhật; toàn bộ thao tác ghi bị khóa; dữ liệu cache đọc bình thường. |
| **AC06** | C11 Offline No Cache | Hiển thị empty state "Chưa có dữ liệu để xem ngoại tuyến" kèm nút Thử lại. |
| **AC07** | Mất mạng khi đang mở form | Giữ nguyên bản nháp (draft); khóa nút Lưu; khi có mạng trở lại không tự động submit ngầm. |
| **AC08** | C04 Validation | Chặn input rỗng, âm, thập phân, vượt $999.999.999.999đ$; inline error tiếng Việt. |
| **AC09** | C04 Live Preview | Nhập số hợp lệ $\rightarrow$ cập nhật tức thì "Ngân sách mới", "Còn lại/Vượt", progress bar. Cảnh báo khi giảm dưới đã chi. |
| **AC10** | C05–C08 Card Mutations | Thêm/sửa/xóa chỉ tác động metadata tên thẻ; xóa là archive; không xóa cascade chi tiêu cũ. |
| **AC11** | C01 Recent Activity | Hiển thị tối đa 3 sự kiện ảnh hưởng tiền; không hiển thị WAIT/SKIP; mở shared item detail hoặc C09. |
| **AC12** | C09 Adjustment History | Danh sách thay đổi hạn mức trước/sau trong kỳ; chỉ đọc, không có nút revert. |
| **AC13** | Notable Item Navigation | Bấm vào item $\rightarrow$ gọi callback mở Shared Item Detail với `itemId`. |
| **AC14** | C03 Wallet Settings | Anchor ngày bắt đầu chu kỳ chỉ đọc; không có trường hay action chỉnh sửa ngày. |
| **AC15** | Mascot Fallback | Thiếu asset $\rightarrow$ slot trống hiển thị thanh lịch; không cắt ghép mockup hay placeholder nhãn debug. |
| **AC16** | TalkBack Semantics | Đọc đúng thứ tự phân cấp, số tiền có chữ "đồng", progress đọc phần trăm thực tế. |
| **AC17** | Layout & Insets | Hoạt động chuẩn ở font 200%, bàn phím mở không che khuất các nút CTA dưới footer. |
| **AC18** | Process Recreation | Xoay màn hình / cấu hình thay đổi giữ nguyên `selectedCycleId` và bản nháp form. |

---

## 8. Open Questions (Theo Dõi Q)

* **Q1–Q2 (Visual Assets):** Tiếp tục dùng placeholder tĩnh thanh lịch và icon registry hiện có cho prototype; không chặn tiến độ dựng UI Compose.
* **Q3–Q8 (Production Policies):** Toàn bộ chính sách về anchor date, archive thẻ và format tiền được áp dụng chuẩn xác theo prototype contract của `UI-CONTRACT.md`.
