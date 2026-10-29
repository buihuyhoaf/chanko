# Implementation Plan: Chanko Budget UI & Wallet Settings v2.1

## 1. Overview
Triển khai giao diện Ngân sách và Thiết lập ví v2.1 theo tài liệu đặc tả `docs/features/budget-wallet-settings-v2.1-spec.md` và gói bàn giao `Chanko-Wallet-Settings-v2.1-Handoff` / `Chanko-Dialog-Them-The-Tin-Dung-v2.1-Handoff`.
Mục tiêu chính: Chuẩn hóa toàn bộ cấu trúc thành **2 màn hình, 2 bottom sheets và 3 dialogs**, chuyển C03 thành sub-screen độc lập với 3 sections, và xây dựng Dialog C06 Thêm thẻ tín dụng dạng modal căn giữa với bộ đếm 30 grapheme, 6 màu Candy và Live Preview.

---

## 2. Component Dependency Graph
```
[Domain: CreditCard & CardColor]
              │
              ▼
[Data: FakeBudgetRepository (colorId, Pay Now system method)]
              │
              ▼
[Navigation: Screen.WalletSettings route in NavGraph & MainNavigation]
              │
              ▼
[ViewModel: BudgetViewModel & BudgetUiState (v2.1 events & states)]
       │                      │
       ▼                      ▼
[Dialogs: C06 Add,     [Wallet Sections:
 C07 Rename, C08 Delete, BudgetSettings, C05 Payments, C09 History]
 DiscardChanges]              │
       │                      ▼
       └──────────────► [Screen: WalletSettingsScreen (C03)]
                              │
                              ▼
                        [Screen: BudgetScreen (C01 Overview) Integration]
                              │
                              ▼
                        [Full Verification: Unit Tests & Build]
```

---

## 3. Implementation Steps & Verification Criteria

### Step 1: Domain & Data Foundation
- **Tasks**:
  1. Thêm enum `CardColor` vào `domain/budget/model/CreditCard.kt` gồm 6 màu (`PURPLE`, `PINK`, `BLUE`, `MINT`, `YELLOW`, `ORANGE`) kèm mã hex (`#7447FF`, `#FF64A4`, `#4D96FF`, `#34CBA0`, `#FFD363`, `#FF9C55`) và tên hiển thị tiếng Việt.
  2. Cập nhật model `CreditCard` với thuộc tính `color: CardColor = CardColor.PURPLE`.
  3. Bổ sung validator đếm grapheme cho C06 (max 30) trong khi giữ validator domain/rename 40 grapheme.
  4. Cập nhật `FakeBudgetRepository.kt`:
     - Khởi tạo danh sách thẻ mẫu có gắn màu `CardColor`.
     - Đảm bảo phương thức `Pay Now` luôn là phương thức hệ thống mặc định (không thể sửa/xóa/archive).
     - Hỗ trợ thêm thẻ mới nhận `color: CardColor`.
     - Archive thẻ bảo lưu toàn bộ chi tiêu và snapshot tên cũ.
- **Verification Criteria**:
  - Chạy `./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.domain.budget.*"` đạt 100% PASS.

---

### Step 2: Navigation & Route Registration
- **Tasks**:
  1. Thêm `Screen.WalletSettings` vào `Screen.kt` với route `wallet_settings?expandHistory={expandHistory}` và hàm `createRoute(expandHistory: Boolean = false)`.
  2. Đăng ký destination `Screen.WalletSettings` trong `MainNavigation.kt` (hoặc `NavGraph.kt`), kết nối nút Quay lại (Back) để pop về `BudgetScreen` (C01).
  3. Cập nhật `BudgetRoute.kt` nhận callback điều hướng đến `WalletSettings`.
- **Verification Criteria**:
  - Project biên dịch thành công; route và tham số điều hướng được cấu hình chuẩn.

---

### Step 3: ViewModel & State Models (v2.1 Architecture)
- **Tasks**:
  1. Cập nhật `BudgetUiState.kt`:
     - Thêm trạng thái cho Wallet Settings: `isHistoryExpanded: Boolean`.
     - Thêm trạng thái cho các Dialog: `isAddCardDialogOpen: Boolean`, `isRenameCardDialogOpen: Boolean`, `isDeleteCardDialogOpen: Boolean`, `isDiscardChangesDialogOpen: Boolean`.
     - Thêm trạng thái form C06: `addCardNameDraft: String`, `addCardColorDraft: CardColor`, `addCardError: String?`, `isAddCardDirty: Boolean`.
     - Thêm tham chiếu thẻ đang thao tác: `cardToRename: CreditCard?`, `cardToDelete: CreditCard?`.
  2. Cập nhật `BudgetUiEvent.kt`:
     - Thêm events: `OpenWalletSettings(expandHistory: Boolean)`, `ToggleHistoryAccordion`, `OpenAddCardDialog`, `CloseAddCardDialog`, `ChangeAddCardName(String)`, `SelectAddCardColor(CardColor)`, `SubmitAddCard`, `OpenRenameCardDialog(CreditCard)`, `SubmitRenameCard(String)`, `OpenDeleteCardDialog(CreditCard)`, `ConfirmDeleteCard`, `ConfirmDiscardDraft`.
  3. Cập nhật `BudgetViewModel.kt`:
     - Xử lý logic draft cho C06: Theo dõi cả thay đổi tên và thay đổi màu sắc. Nếu draft bị dirty mà người dùng dismiss -> mở `DiscardChangesDialog`.
     - Kiểm tra tên trùng lặp (case-insensitive) và tên dành riêng "Pay Now".
     - Xử lý mở/thu gọn accordion lịch sử C09 (tự động mở nếu `expandHistory = true`).
- **Verification Criteria**:
  - Viết và chạy unit tests trong `BudgetViewModelTest` kiểm tra toàn bộ luồng thêm thẻ, đổi màu, dirty check, trùng tên, và toggle history.

---

### Step 4: Dialog Components (C06, C07, C08)
- **Tasks**:
  1. Triển khai `AddCreditCardDialog.kt` (C06):
     - Modal Dialog căn giữa màn hình, nền dim (Scaffold/Surface).
     - Header: Icon, tiêu đề "Thêm thẻ tín dụng", nút đóng X.
     - Mô tả: "Thêm nhãn thẻ để phân loại chi tiêu trong chu kỳ ngân sách. Không liên kết ngân hàng."
     - OutlinedTextField với bộ đếm `n/30`, hiển thị lỗi inline.
     - Bộ chọn 6 màu Candy Swatches: Vòng tròn màu có viền/check khi chọn, contentDescription tiếp cận.
     - Live Preview Card: Card bo tròn, màu nền theo swatch đang chọn, tên thẻ hiển thị thời gian thực theo text nhập (hoặc "Tên thẻ" nếu trống), icon nhãn trung tính (không logo VISA/chip).
     - Footer: Nút "Hủy" và "Thêm thẻ" (với trạng thái loading khi submitting).
     - Hỗ trợ cuộn khi bàn phím ảo hiển thị (IME insets).
  2. Chuyển đổi `RenameCreditCardDialog.kt` (C07):
     - Modal Dialog căn giữa, prefill tên hiện tại, bộ đếm `n/40`, nút Hủy và Lưu thay đổi.
  3. Cập nhật `DeleteCardConfirmDialog.kt` (C08):
     - AlertDialog hiển thị thông điệp giữ nguyên chi tiêu cũ.
- **Verification Criteria**:
  - Giao diện dialog hiển thị chuẩn, Live Preview cập nhật mượt mà khi gõ tên và bấm chọn màu.

---

### Step 5: Screen Thiết lập ví (`WalletSettingsScreen` - C03) & 3 Sections
- **Tasks**:
  1. Tạo `wallet/components/BudgetSettingsSection.kt`:
     - Hiển thị ngân sách trực tiếp: "Ngân sách: 5.000.000đ".
     - Khoảng chu kỳ ngày: "Chu kỳ 15/10/2026 – 14/11/2026".
     - "Ngày bắt đầu chu kỳ: 15 · Chỉ xem".
     - Đã chi, còn lại, thanh tiến trình % chi tiêu.
     - Nút "Sửa ngân sách" mở C04 `EditBudgetSheet`.
  2. Tạo `wallet/components/PaymentMethodsSection.kt` (C05):
     - Helper text: "Pay Now và thẻ tín dụng đều tính vào ngân sách khi xác nhận mua."
     - Nút "Thêm thẻ" mở C06.
     - Row "Pay Now": Ghim đầu danh sách, huy hiệu "Mặc định", không có nút đổi tên/xóa.
     - Rows Thẻ tín dụng: Hiển thị icon màu + tên thẻ + nút Đổi tên (C07) + nút Xóa (C08).
     - Trạng thái rỗng: "Chưa có thẻ tín dụng" nhưng vẫn giữ Pay Now và nút Thêm thẻ.
  3. Tạo `wallet/components/AdjustmentHistorySection.kt` (C09):
     - Accordion card với icon chevron mở/thu gọn ở header.
     - Danh sách bản ghi chỉ đọc: Thời gian, loại điều chỉnh Tăng/Giảm, Mức cũ → Mức mới, Chu kỳ áp dụng.
     - Trạng thái rỗng: "Chưa có điều chỉnh ngân sách trong chu kỳ".
  4. Tạo `WalletSettingsScreen.kt` (C03):
     - Scaffold với TopAppBar (Back button, Tiêu đề "Thiết lập ví", subtitle).
     - LazyColumn chứa 3 section theo đúng thứ tự.
     - Tích hợp các Dialogs (C06, C07, C08, DiscardChanges) và BottomSheet C04.
- **Verification Criteria**:
  - Màn hình hiển thị đầy đủ 3 section, cuộn mượt mà, Back button hoạt động chuẩn xác.

---

### Step 6: Tích hợp Overview (C01) & Dọn dẹp Sheets cũ
- **Tasks**:
  1. Cập nhật `BudgetScreen.kt`:
     - Nút "Thiết lập ví" trên app bar điều hướng sang `Screen.WalletSettings`.
     - Click vào sự kiện điều chỉnh ngân sách trong Hoạt động gần đây điều hướng sang `Screen.WalletSettings` với cờ `expandHistory = true`.
  2. Thu gọn `BudgetSheetHost.kt`:
     - Chỉ còn quản lý đúng 2 sheets: `CyclePickerSheet` (C02) và `EditBudgetSheet` (C04).
     - Xóa bỏ các sheets không còn sử dụng: `WalletSettingsSheet.kt`, `CreditCardManageSheet.kt`, `AdjustmentHistorySheet.kt`, `AddCreditCardSheet.kt`, `RenameCreditCardSheet.kt`.
- **Verification Criteria**:
  - Không còn code thừa; toàn bộ luồng điều hướng Overview -> Wallet Settings -> Sheets/Dialogs hoạt động nhất quán.

---

### Step 7: Kiểm thử tổng thể & Nghiệm thu AC01–AC29
- **Tasks**:
  1. Cập nhật và bổ sung các Unit Tests trong `app/src/test/java/...`:
     - Tests kiểm tra validation 30 grapheme, trùng tên, tên "Pay Now".
     - Tests kiểm tra Pay Now bất biến, archive thẻ giữ nguyên chi tiêu.
     - Tests kiểm tra trạng thái mở/thu gọn history accordion.
  2. Chạy `./gradlew testDevDebugUnitTest`.
  3. Chạy `./gradlew assembleDevDebug`.
  4. Cập nhật báo cáo nghiệm thu tại `docs/tasks/budget-ui-v2-verification-report.md`.
- **Verification Criteria**:
  - Tất cả tests đạt 100% green, build devDebug thành công không có lỗi.

---

## 4. Verification Commands
```bash
# 1. Chạy unit tests toàn dự án
./gradlew testDevDebugUnitTest

# 2. Build APK devDebug để xác thực tính tương thích runtime/compilation
./gradlew assembleDevDebug
```
