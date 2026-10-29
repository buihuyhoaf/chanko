# Spec: Chanko Budget UI & Wallet Settings v2.1

## Objective
Nâng cấp và tinh gọn toàn bộ kiến trúc giao diện Ngân sách (Budget) theo gói bàn giao `Chanko-Wallet-Settings-v2.1-Handoff`:
1. Giảm tải số lượng bottom sheets từ 7 sheets (v2.0) xuống kiến trúc chuẩn v2.1: **Chính xác 2 màn hình (Screens), 2 Bottom Sheets và 3 Dialogs nghiệp vụ**.
2. Chuyển đổi màn hình Thiết lập ví (`WalletSettingsScreen` - C03) thành một sub-screen độc lập có nút Quay lại (Back), tích hợp trực tiếp Section Quản lý Phương thức thanh toán (C05) và Section Lịch sử điều chỉnh ngân sách dạng accordion mở rộng/thu gọn (C09).
3. Triển khai Dialog Thêm thẻ tín dụng mới (`AddCreditCardDialog` - C06) dạng modal giữa màn hình với nền dim, bộ đếm tối đa 30 grapheme, 6 màu Candy Swatches và Live Preview thời gian thực.
4. Đảm bảo toàn bộ quy tắc nghiệp vụ: Pay Now là phương thức hệ thống mặc định (bất biến), không số thẻ/CVV/ngân hàng, past cycle & offline chỉ đọc, đồng bộ draft/discard confirmation, và đáp ứng đầy đủ tiêu chí nghiệm thu AC01–AC29.

---

## Tech Stack
- **Framework & UI Toolkit:** Android Jetpack Compose (BOM 2024.09.00+), Material 3 (`androidx.compose.material3`).
- **Language & Runtime:** Kotlin 2.0+, Coroutines & StateFlow.
- **Dependency Injection:** Hilt (`hiltViewModel`).
- **Navigation:** Jetpack Navigation Compose (`NavHostController`).
- **Theme & Design System:** Chanko Design System (`CandyColors`, `ChankoTypography`, `ChankoIcon`).
- **Target OS:** Android 14 / 15 (minSdk 26+).

---

## Surface Inventory (2 Màn hình, 2 Sheets, 3 Dialogs)

| ID | Surface | Phân loại | Vai trò & Hành vi |
|---|---|---|---|
| **C01** | `BudgetScreen` | Screen (Overview) | Màn hình chính mở từ `HomeScreen` (hoặc tab shell). Hiển thị ngân sách, cycle picker, breakdown, món đáng chú ý, hoạt động gần đây. Nút "Thiết lập ví" điều hướng sang C03. |
| **C03** | `WalletSettingsScreen` | Screen (Sub-screen) | Màn hình phụ tại route `wallet_settings?expandHistory={expandHistory}` với nút Quay lại (Back), hiển thị: (1) Ngân sách trực tiếp + Edit CTA, (2) Phương thức thanh toán C05, (3) Lịch sử điều chỉnh C09. |
| **C02** | `CyclePickerSheet` | Modal Bottom Sheet | Chọn chu kỳ ngân sách (chọn ngay khi tap, không có nút Lưu/Tạo/Future cycle). |
| **C04** | `EditBudgetSheet` | Modal Bottom Sheet | Chỉnh sửa hạn mức ngân sách + Live Preview. Được gọi từ cả C01 (Overview) và C03 (Settings). |
| **C06** | `AddCreditCardDialog` | Centered Modal Dialog | Thêm thẻ tín dụng: modal giữa màn hình, nền dim, input tối đa 30 graphemes, 6 màu Candy swatches, live card preview, nút Hủy / Thêm thẻ. |
| **C07** | `RenameCreditCardDialog` | Centered Modal Dialog | Đổi tên thẻ tín dụng: modal giữa màn hình, prefill tên cũ, tối đa 40 graphemes, nút Hủy / Lưu thay đổi. |
| **C08** | `DeleteCardConfirmDialog` | AlertDialog | Xác nhận xóa/lưu trữ thẻ tín dụng: giải thích các khoản chi đã ghi vẫn được giữ nguyên, nút Hủy / Xóa thẻ. |
| *Utility* | `DiscardChangesDialog` | Confirmation Dialog | Hộp thoại dùng chung xác nhận khi người dùng hủy/đóng sheet hoặc dialog có thay đổi draft dở dang. |

---

## Commands
```bash
# Build dự án (chỉ build debug, không deploy thiết bị)
./gradlew assembleDevDebug

# Chạy toàn bộ Unit Tests
./gradlew testDevDebugUnitTest

# Chạy kiểm thử riêng module Budget
./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.budget.*"
./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.domain.budget.*"
```

---

## Project Structure
```
app/src/main/java/com/hoabh/chanko/
├── domain/budget/
│   ├── model/
│   │   ├── BudgetCycle.kt
│   │   ├── BudgetSummary.kt
│   │   ├── CreditCard.kt               → Bổ sung CardColor enum (PURPLE, PINK, BLUE, MINT, YELLOW, ORANGE)
│   │   ├── PaymentMethod.kt
│   │   └── BudgetAdjustmentEvent.kt
│   └── repository/
│       └── BudgetRepository.kt
├── data/budget/
│   └── FakeBudgetRepository.kt         → Cập nhật hỗ trợ CardColor, Pay Now system default, lưu trữ colorId
└── presentation/
    ├── navigation/
    │   ├── Screen.kt                   → Bổ sung Screen.WalletSettings route
    │   ├── NavGraph.kt                 → Đăng ký WalletSettings composable
    │   └── MainNavigation.kt
    └── screens/budget/
        ├── BudgetRoute.kt              → Overview entry point & navigation callbacks
        ├── BudgetScreen.kt             → C01 Overview Screen
        ├── BudgetViewModel.kt          → Quản lý state cho Overview & Sheets/Dialogs
        ├── BudgetUiState.kt            → State data classes & sealed interfaces
        ├── BudgetUiEvent.kt            → User intent events
        ├── BudgetUiEffect.kt           → Navigation & Snackbar one-shot events
        ├── wallet/
        │   ├── WalletSettingsScreen.kt → C03 Sub-screen (chứa C05 và C09)
        │   ├── components/
        │   │   ├── BudgetSettingsSection.kt    → Section 1: Hạn mức, chu kỳ, spent/remaining, nút Sửa
        │   │   ├── PaymentMethodsSection.kt    → Section 2 (C05): Pay Now + Thẻ tín dụng + Thêm thẻ CTA
        │   │   └── AdjustmentHistorySection.kt → Section 3 (C09): Accordion toggle + Lịch sử điều chỉnh
        │   └── dialogs/
        │       ├── AddCreditCardDialog.kt      → C06 Dialog căn giữa, 30 ký tự, 6 màu Candy, Preview
        │       └── RenameCreditCardDialog.kt   → C07 Dialog đổi tên
        ├── sheets/
        │   ├── BudgetSheetHost.kt              → Chỉ host C02 CyclePicker & C04 EditBudget
        │   ├── CyclePickerSheet.kt             → C02 Bottom Sheet
        │   ├── EditBudgetSheet.kt              → C04 Bottom Sheet (gọi từ C01 hoặc C03)
        │   ├── DeleteCardConfirmDialog.kt      → C08 Dialog xác nhận xóa
        │   └── DiscardChangesDialog.kt         → Dialog xác nhận hủy draft
        └── components/
            ├── BudgetSummaryCard.kt
            ├── PaymentBreakdownCard.kt
            ├── NotableItemsCard.kt
            └── RecentActivityCard.kt
```

---

## Detailed Specifications per Component

### 1. Screen C03: Thiết lập ví (`WalletSettingsScreen`)
- **Navigation:**
  - Route: `wallet_settings?expandHistory={expandHistory}`
  - Nút Back trên TopAppBar: nhãn ngữ nghĩa "Quay lại", quay về `BudgetScreen` (C01), giữ nguyên chu kỳ đã chọn và vị trí cuộn của C01.
- **Header:**
  - Tiêu đề: "Thiết lập ví", phụ đề mô tả rõ ràng.
- **Section 1: Ngân sách mua sắm**
  - Hiển thị trực tiếp: "Ngân sách: 5.000.000đ".
  - Khoảng chu kỳ ngày: "Chu kỳ 15/10/2026 – 14/11/2026".
  - Ngày bắt đầu chu kỳ: "Ngày bắt đầu chu kỳ: 15 · Chỉ xem" (read-only, không có editor MVP).
  - Tình trạng chi tiêu: "Đã chi 3.200.000đ", "Còn lại 1.800.000đ, 64%" (hoặc "Vượt ngân sách ...").
  - Thanh tiến trình (ProgressBar) trực quan theo tỷ lệ % chi tiêu.
  - Nút hành động: "Sửa ngân sách" -> mở C04 `EditBudgetSheet`.
- **Section 2: Phương thức thanh toán (C05)**
  - Tiêu đề "Phương thức thanh toán".
  - Dòng hướng dẫn (Helper): "Pay Now và thẻ tín dụng đều tính vào ngân sách khi xác nhận mua."
  - Nút CTA "Thêm thẻ" -> mở C06 `AddCreditCardDialog`.
  - Phương thức "Pay Now": Luôn hiển thị ở đầu danh sách, có huy hiệu "Mặc định", không thể sửa hoặc xóa.
  - Thẻ tín dụng: Hiển thị tên thẻ, icon/màu sắc tương ứng, hai nút hành động có nhãn tiếp cận rõ ràng: "Đổi tên {tên_thẻ}" (mở C07) và "Xóa {tên_thẻ}" (mở C08).
  - Trạng thái chưa có thẻ: Hiển thị text "Chưa có thẻ tín dụng", nhưng Pay Now và nút "Thêm thẻ" vẫn hiển thị đầy đủ.
  - Tuyệt đối không hiển thị: số thẻ che `•••• 1234`, CVV, ngày hết hạn, hoặc liên kết ngân hàng.
- **Section 3: Lịch sử điều chỉnh ngân sách (C09)**
  - Tiêu đề: "Lịch sử điều chỉnh ngân sách" với icon chevron mở/đóng ở phần header.
  - Trạng thái mặc định: Thu gọn (collapsed) khi người dùng mở từ menu Thiết lập ví; Tự động mở rộng (expanded) khi người dùng click vào sự kiện điều chỉnh ngân sách từ mục Hoạt động gần đây ở C01.
  - Danh sách bản ghi (chỉ đọc):
    - Thời gian: Ngày và giờ địa phương.
    - Nhãn loại điều chỉnh: "Tăng ngân sách" hoặc "Giảm ngân sách".
    - Mức thay đổi: Hạn mức cũ → Hạn mức mới (ví dụ "5.000.000đ → 6.000.000đ").
    - Chu kỳ áp dụng: Hiển thị rõ chu kỳ áp dụng của đợt điều chỉnh.
    - Không có chevron trên từng dòng, không có tương tác click hay nút hoàn tác (revert) trên từng dòng.
  - Trạng thái rỗng: "Chưa có điều chỉnh ngân sách trong chu kỳ".

### 2. Dialog C06: Thêm thẻ tín dụng (`AddCreditCardDialog`)
- **Hình thức:** Modal Dialog căn giữa màn hình (centered dialog) trên nền dim mờ của C03, không phải bottom sheet.
- **Header:** Icon minh họa nhỏ, Tiêu đề "Thêm thẻ tín dụng", nút đóng (X).
- **Mô tả:** "Thêm nhãn thẻ để phân loại chi tiêu trong chu kỳ ngân sách. Không liên kết ngân hàng."
- **Trường nhập tên:**
  - Nhãn "Tên thẻ" kèm dấu chỉ báo bắt buộc `*`.
  - Placeholder: "Ví dụ: Thẻ cá nhân". Không prefill sẵn text mẫu khi mở mới.
  - Bộ đếm ký tự: `n/30` (tối đa 30 graphemes theo chuẩn NFC sau khi trim).
  - Kiểm tra tính hợp lệ: Không để trống, không vượt quá 30 grapheme, không trùng tên với các thẻ đang hoạt động (không phân biệt hoa thường), không đặt tên trùng với từ khóa hệ thống "Pay Now".
- **Bảng chọn màu (Candy Swatches):**
  - Gồm 6 màu kẹo ngọt ngào:
    1. `PURPLE` (`#7447FF`) - Mặc định được chọn khi mở dialog.
    2. `PINK` (`#FF64A4`)
    3. `BLUE` (`#4D96FF`)
    4. `MINT` (`#34CBA0`)
    5. `YELLOW` (`#FFD363`)
    6. `ORANGE` (`#FF9C55`)
  - Swatch đang chọn có viền / dấu check và semantics tiếp cận (ví dụ "Màu tím, đã chọn").
- **Thẻ xem trước (Live Preview):**
  - Xem trước giao diện thẻ tức thời theo màu sắc đang chọn và tên người dùng đang nhập.
  - Nếu trường tên rỗng, preview hiển thị placeholder "Tên thẻ".
  - Thẻ mang biểu tượng nhãn chi tiêu trung tính; không chứa logo VISA, Mastercard hay hình chip thẻ.
- **Nút điều khiển:**
  - Nút "Hủy" (dismiss) và "Thêm thẻ" (submit).
  - Trạng thái pending/submitting: Khóa input, khóa chọn màu, nút hiển thị progress bar "Đang thêm...".
  - Kiểm tra thay đổi dở dang (Dirty check): Khi người dùng chạm ra ngoài scrim hoặc bấm nút Hủy/Back, nếu đã nhập tên hoặc thay đổi màu thì hiển thị `DiscardChangesDialog`. Nếu chưa sửa đổi gì thì đóng ngay.

### 3. Dialog C07 & C08
- **C07 Rename Credit Card Dialog:**
  - Modal Dialog căn giữa màn hình.
  - Prefill tên hiện tại của thẻ, bộ đếm tối đa 40 graphemes (giữ nguyên độ dài tối đa domain của các thẻ hiện hữu).
  - Nút "Hủy" và "Lưu thay đổi". Tên không đổi thì nút Lưu bị disabled.
- **C08 Delete Card Confirm Dialog:**
  - AlertDialog xác nhận.
  - Giải thích rõ: Khi xóa thẻ, các giao dịch và khoản chi tiêu đã ghi nhận trước đây vẫn được bảo lưu nguyên vẹn trong ngân sách chu kỳ.
  - Thao tác thực chất là đưa thẻ vào trạng thái `isArchived = true`.

---

## Code Style & Conventions
- **Clean Compose Architecture:** Composable stateless, nhận state và đẩy events lên ViewModel qua lambda.
- **Immutability & Stability:** Sử dụng Kotlin Data Classes với `@Immutable` hoặc `@Stable` annotations khi cần thiết.
- **Format tiền tệ:** Hàm định dạng tiền tệ dùng chung `formatVnd(amount)` trả về chuỗi có phân cách hàng nghìn và đuôi "đ" (TalkBack đọc là "đồng").
- **Accessibility:** Mọi nút và icon đều có `contentDescription` cụ thể bằng tiếng Việt. Vùng chạm tối thiểu 48dp.
- **Ví dụ code chuẩn:**
```kotlin
@Composable
fun AddCreditCardDialog(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    onSubmit: (name: String, color: CardColor) -> Unit,
    isSubmitting: Boolean,
    errorMessage: String? = null
) {
    if (!isOpen) return
    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Implementation với Box, Surface, Column cuộn được khi mở bàn phím (IME padding)
    }
}
```

---

## Testing Strategy
- **Unit Tests:**
  - Kiểm tra logic đếm grapheme và validate tên thẻ (độ dài 30 cho add, 40 cho rename, dấu tiếng Việt có dấu tổ hợp, emoji).
  - Kiểm tra `BudgetViewModel` xử lý các flow: Mở Wallet Settings, toggle history accordion, mở Add Card Dialog, chọn màu, submit thêm thẻ, xử lý lỗi trùng tên / Pay Now reserved.
  - Kiểm tra tính toán `BudgetSummary`, `isOverBudget`, phần trăm chi tiêu, xử lý không chia cho 0 khi hạn mức bằng 0.
- **Fake Repository Tests:**
  - Bảo vệ Pay Now không bao giờ bị rename hoặc archive.
  - Khi archive thẻ, danh sách chi tiêu và snapshot tên cũ vẫn được giữ nguyên.
- **UI Tests (Compose Test Rule):**
  - Kiểm tra mở và đóng các dialog, kiểm tra Live Preview cập nhật khi gõ tên và chọn màu swatch.
  - Kiểm tra điều hướng giữa `BudgetScreen` (C01) và `WalletSettingsScreen` (C03).

---

## Boundaries
- **Always:**
  - Giữ toàn bộ văn bản giao diện bằng tiếng Việt theo quy định của dự án.
  - Giữ đúng cấu trúc: 2 màn hình, 2 sheets, 3 dialogs.
  - Chạy `./gradlew testDevDebugUnitTest` và `./gradlew assembleDevDebug` để kiểm chứng.
- **Ask first:**
  - Bất kỳ thay đổi nào làm thay đổi route gốc ngoài `wallet_settings` hoặc thay đổi cấu trúc navigation toàn cục của App.
  - Thêm bất kỳ thư viện bên thứ ba nào vào `build.gradle.kts`.
- **Never:**
  - Không chạy lệnh cài đặt lên thiết bị thật hoặc máy ảo (`adb install`, `./gradlew installDebug`).
  - Không lưu trữ, xử lý hoặc hiển thị số thẻ thật, số thẻ che (last-four), CVV, ngày hết hạn.
  - Không biến dialog C06, C07 thành bottom sheet hay màn hình riêng.

---

## Success Criteria (Mapping to AC01–AC29)
- [ ] **SC01 (AC01-AC03):** Ứng dụng chỉ có đúng 2 màn hình ngân sách (`BudgetScreen` và `WalletSettingsScreen`), 2 bottom sheets (`CyclePickerSheet` và `EditBudgetSheet`), và 3 dialogs nghiệp vụ (`AddCreditCardDialog`, `RenameCreditCardDialog`, `DeleteCardConfirmDialog`). Không tồn tại route hay sheet quản lý thẻ riêng lẻ.
- [ ] **SC02 (AC04):** `WalletSettingsScreen` hiển thị đúng 3 khu vực: (1) Ngân sách trực tiếp, chu kỳ ngày, spent/remaining, anchor ngày bắt đầu chỉ đọc; (2) Phương thức thanh toán C05; (3) Lịch sử điều chỉnh ngân sách C09.
- [ ] **SC03 (AC05):** Nút "Sửa ngân sách" từ cả Overview và WalletSettings đều mở chung C04 `EditBudgetSheet`; lưu thành công sẽ cập nhật lại đúng màn hình gọi nó.
- [ ] **SC04 (AC07, AC10):** Phương thức "Pay Now" luôn hiện đầu tiên với nhãn "Mặc định", không thể sửa hay xóa. Thẻ tín dụng chỉ hiển thị tên nhãn và màu sắc; chi tiêu chỉ trừ vào ngân sách khi xác nhận mua món đồ.
- [ ] **SC05 (AC11, AC12):** Lịch sử điều chỉnh C09 là accordion có thể toggle mở/thu gọn ở header; khi điều hướng từ sự kiện điều chỉnh ở C01 thì tự động mở rộng C09.
- [ ] **SC06 (AC22-AC25):** Dialog Thêm thẻ C06 là modal giữa màn hình nền dim, hỗ trợ nhập tối đa 30 graphemes, chọn 6 màu Candy, có xem trước Live Preview đồng bộ tức thời với màu và tên đang nhập; chặn tên trùng và tên "Pay Now".
- [ ] **SC07 (AC26-AC28):** Đóng dialog C06/C07/C04 khi có thay đổi dở dang (kể cả chỉ đổi màu swatch) đều kích hoạt cảnh báo bỏ thay đổi; trạng thái offline/past cycle khóa chức năng lưu; submit hiển thị loading pending rõ ràng.
- [ ] **SC08 (AC20, AC29):** Hỗ trợ đầy đủ TalkBack, phông chữ lớn 200%, xử lý bàn phím ảo (IME insets) không che lấp nút bấm hoặc preview.
- [ ] **SC09:** Toàn bộ unit tests vượt qua 100% (`./gradlew testDevDebugUnitTest`) và biên dịch thành công (`./gradlew assembleDevDebug`).

---

## Open Questions
*(Hiện tại không có câu hỏi mở chặn triển khai — handoff v2.1 đã chốt đầy đủ các quyết định thiết kế và giao diện. Nếu bạn có yêu cầu chỉnh sửa gì về bảng màu Candy hay câu chữ tiếng Việt, xin hãy phản hồi).*
