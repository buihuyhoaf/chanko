# Implementation Plan: C07 — Dialog Đổi tên thẻ tín dụng v2.1 (Rename Credit Card Dialog)

## 1. Overview
Triển khai lát cắt giao diện **Dialog Đổi tên thẻ tín dụng (C07)** cho tính năng Thiết lập ví theo tài liệu đặc tả `docs/features/rename-credit-card-dialog-v2.1-spec.md` và gói bàn giao `Chanko-Budget-UI-v2.1-Rename-Updated-Handoff 2`.
Mục tiêu chính: Xây dựng Modal Dialog căn giữa màn hình (thay thế Bottom Sheet cũ), prefill tên hiện tại, bộ đếm grapheme max 30, hiển thị màu sắc chỉ đọc, Live Preview thời gian thực, ghi chú bảo lưu lịch sử, và tích hợp chặt chẽ với ViewModel/FakeRepository.

---

## 2. Component Dependency Graph
```
[Domain & Model: CardColor enum & Grapheme Validator (max 30 / legacy 40)]
                                  │
                                  ▼
[Data Layer: FakeBudgetRepository (rename preserves snapshot & color)]
                                  │
                                  ▼
[Presentation State: BudgetUiState & BudgetViewModel (C07 state & events)]
                                  │
                                  ▼
[UI Component: CardPreview (thẻ xem trước trung lập)]
                                  │
                                  ▼
[UI Component: RenameCreditCardDialog (Modal căn giữa, nền dim, IME handling)]
                                  │
                                  ▼
[Integration: Tích hợp vào WalletSettingsScreen / BudgetSheetHost & Tests]
```

---

## 3. Implementation Steps & Verification Criteria

### Step 1: Cập nhật Model & Hàm Validator (Domain Layer)
- **Tasks**:
  1. Đảm bảo enum `CardColor` (6 màu Candy: `PURPLE`, `PINK`, `BLUE`, `MINT`, `YELLOW`, `ORANGE`) và thuộc tính `color: CardColor = CardColor.PURPLE` đã có trong `CreditCard.kt`.
  2. Bổ sung hàm đếm grapheme và validate tên thẻ cho UI (tối đa 30 ký tự, hỗ trợ tiếng Việt có dấu và emoji).
  3. Bổ sung logic kiểm tra no-change (so sánh normalized name giữa draft và tên gốc, cho phép đổi kiểu hoa/thường) và kiểm tra thẻ legacy (31–40 ký tự).
- **Verification Criteria**:
  - Chạy unit tests cho `CreditCard` và hàm validate tên thẻ đạt 100% PASS.

---

### Step 2: Cập nhật FakeBudgetRepository (Data Layer)
- **Tasks**:
  1. Kiểm tra hàm `renameCard(cardId, newName)`:
     - Giữ nguyên `color` của thẻ.
     - Kiểm tra không cho phép đổi tên phương thức hệ thống `Pay Now`.
     - Kiểm tra không cho phép trùng tên với thẻ đang hoạt động khác.
     - Đảm bảo snapshot tên thẻ trong các giao dịch cũ (`Expense`) không bị thay đổi.
- **Verification Criteria**:
  - Viết/chạy unit test cho repository xác nhận snapshot chi tiêu cũ giữ nguyên sau khi đổi tên thẻ.

---

### Step 3: Quản lý State & Events trong BudgetViewModel (Presentation State Layer)
- **Tasks**:
  1. Cập nhật `BudgetUiState`:
     - Thêm trường: `cardToRename: CreditCard?`, `renameDraft: String`, `renameError: String?`, `isRenameSubmitting: Boolean`, `isRenameDirty: Boolean`.
  2. Cập nhật `BudgetUiEvent`:
     - `OpenRenameCardDialog(card: CreditCard)`
     - `ChangeRenameDraft(draft: String)`
     - `SubmitRenameCard`
     - `DismissRenameCard`
     - `ConfirmDiscardRenameDraft`
  3. Xử lý trong `BudgetViewModel`:
     - Khi mở: Prefill `cardToRename`, đặt `renameDraft = card.name`, `renameError = null`, `isRenameDirty = false`.
     - Khi gõ: Cập nhật draft, tính toán lại lỗi inline, cập nhật `isRenameDirty = (draft != card.name)`.
     - Khi submit: Validate điều kiện (online, chu kỳ hiện tại, tên hợp lệ, changed), gửi mutation, cập nhật state thành công, đóng dialog, hiển thị snackbar "Đã đổi tên thẻ".
     - Khi dismiss: Nếu dirty -> kích hoạt `DiscardChangesDialog`; nếu chưa dirty -> đóng ngay.
- **Verification Criteria**:
  - Viết unit tests trong `BudgetViewModelTest` kiểm tra toàn bộ vòng đời của C07 (mở, gõ, no-change disabled, trùng tên, dirty check, submit thành công).

---

### Step 4: Xây dựng Composable Live Preview dùng chung
- **Tasks**:
  1. Tạo `CardPreview.kt`:
     - Hiển thị chiếc thẻ bo góc mềm mại theo đúng phong cách Candy Pop 3D.
     - Nền thẻ mang màu sắc của thẻ (`CardColor`), có hiệu ứng chuyển màu hoặc đổ bóng nhẹ.
     - Hiển thị tên thẻ theo thời gian thực (nếu draft rỗng thì hiển thị placeholder "Tên thẻ").
     - Chứa biểu tượng nhãn ví trung tính; không có logo VISA, Mastercard hay hình chip thẻ.
- **Verification Criteria**:
  - Render preview hoạt động chuẩn xác, cập nhật tức thì khi prop tên thay đổi.

---

### Step 5: Xây dựng Composable `RenameCreditCardDialog` (C07)
- **Tasks**:
  1. Tạo `RenameCreditCardDialog.kt`:
     - Modal Dialog căn giữa màn hình với `DialogProperties(usePlatformDefaultWidth = false)`.
     - Nền Surface bo góc 28dp, độ rộng tối đa 480dp, nền dim phía sau.
     - Header: Icon nhãn thẻ + Tiêu đề "Đổi tên thẻ tín dụng" + Nút đóng (X).
     - Mô tả: "Cập nhật tên hiển thị của thẻ. Màu thẻ sẽ không thay đổi."
     - Ô nhập tên: Single-line `OutlinedTextField`, counter `n/30`, hiển thị lỗi inline.
     - Thông tin "Màu thẻ hiện tại": Chấm tròn màu + Tên màu tiếng Việt (chỉ đọc).
     - Live Preview: Tích hợp `CardPreview`.
     - Chú thích: "Các giao dịch đã ghi nhận trước đây vẫn hiển thị theo tên thẻ cũ, không bị thay đổi."
     - Footer: Nút "Hủy" và "Lưu thay đổi" (có trạng thái loading khi submitting).
     - Xử lý bàn phím ảo (IME padding) và cuộn dọc khi font chữ phóng to.
- **Verification Criteria**:
  - Composable hiển thị đúng layout, preview đồng bộ, nút Lưu tự động bật/tắt theo điều kiện hợp lệ.

---

### Step 6: Tích hợp vào Giao diện & Dọn dẹp Sheet cũ
- **Tasks**:
  1. Tích hợp `RenameCreditCardDialog` vào `WalletSettingsScreen` (hoặc `BudgetSheetHost`).
  2. Kết nối nút "Đổi tên" trên từng thẻ tín dụng để gọi event `OpenRenameCardDialog`.
  3. Xóa bỏ hoặc thay thế file `RenameCreditCardSheet.kt` cũ.
- **Verification Criteria**:
  - Nhấp "Đổi tên" trên thẻ mở dialog căn giữa, đóng mở mượt mà.

---

### Step 7: Kiểm thử Tổng thể & Nghiệm thu AC30–AC39
- **Tasks**:
  1. Chạy toàn bộ Unit Tests: `./gradlew testDevDebugUnitTest`.
  2. Build kiểm tra: `./gradlew assembleDevDebug`.
  3. Lập bảng đối chiếu kết quả kiểm thử theo tiêu chí AC30–AC39.
- **Verification Criteria**:
  - 100% tests pass, build thành công, không có cảnh báo/lỗi biên dịch.

---

## 4. Verification Commands
```bash
# 1. Chạy unit tests cho module budget
./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.budget.*"
./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.domain.budget.*"

# 2. Build APK devDebug
./gradlew assembleDevDebug
```
