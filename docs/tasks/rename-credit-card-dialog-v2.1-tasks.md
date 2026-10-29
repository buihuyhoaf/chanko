# Task Breakdown: C07 — Dialog Đổi tên thẻ tín dụng v2.1 (Rename Credit Card Dialog)

## Task List

- [x] **Task 1: Cập nhật Domain Model & Grapheme Validator**
  - **File:** `app/src/main/java/com/hoabh/chanko/domain/budget/model/CreditCard.kt`
  - **Mô tả:**
    - Thêm enum `CardColor` gồm 6 màu Candy (`PURPLE`, `PINK`, `BLUE`, `MINT`, `YELLOW`, `ORANGE`) kèm mã hex và tên tiếng Việt.
    - Bổ sung trường `color: CardColor = CardColor.PURPLE` vào data class `CreditCard`.
    - Bổ sung hàm đếm grapheme theo chuẩn Unicode NFC (`countGraphemes(text: String): Int`).
    - Bổ sung hàm `validateRename` kiểm tra: rỗng ("Nhập tên thẻ"), >30 grapheme ("Tên thẻ tối đa 30 ký tự"), duplicate khác chính mình ("Tên thẻ đã tồn tại"), reserved ("Tên này dành cho Pay Now"), no-change, và phát hiện legacy (>30).
  - **Evidence:** `CreditCardManagementTest.kt` đã kiểm thử 100% case thành công.

- [x] **Task 2: Cập nhật FakeBudgetRepository**
  - **File:** `app/src/main/java/com/hoabh/chanko/data/budget/FakeBudgetRepository.kt`
  - **Mô tả:**
    - Gán `CardColor` cho các thẻ mẫu trong repository.
    - Cập nhật hàm `renameCard(cardId: String, newName: String)`: Đảm bảo giữ nguyên `color` của thẻ, bảo vệ phương thức `Pay Now` và chặn trùng tên.
    - Snapshot tên thẻ trong các giao dịch (`Expense.paymentMethodName`) không bị thay đổi sau khi đổi tên thẻ.
  - **Evidence:** `CreditCardManagementTest.kt` xác nhận `renameCreditCard guards against reserved Pay Now and preserves card color`.

- [x] **Task 3: Cập nhật State & Events cho C07**
  - **Files:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/BudgetUiState.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/BudgetUiEvent.kt`
  - **Mô tả:**
    - Khai báo state cho C07: `isRenameCardDialogOpen`, `cardToRename`, `renameCardDraft`, `renameCardInitialText`, `renameCardInlineError`, `isLegacyNameOverLimit`, `isRenameCardDirty`, `canSubmitRename`.
    - Thêm event: `DismissRenameCreditCardDialog`.
  - **Evidence:** Biên dịch thành công, typesafe và immutability được đảm bảo.

- [x] **Task 4: Quản lý Lifecycle & Nghiệp vụ C07 trong ViewModel**
  - **File:** `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/BudgetViewModel.kt`
  - **Mô tả:**
    - Xử lý mở dialog (prefill tên và check legacy limit).
    - Xử lý gõ draft và cập nhật validation realtime.
    - Xử lý đóng dialog và dirty check kích hoạt `DiscardChangesDialog`.
    - Xử lý submit mutation, đóng dialog khi thành công và emit Snackbar "Đã đổi tên thẻ".
  - **Evidence:** `BudgetViewModelTest.kt` 4 tests mới cho C07 đạt 100% PASS.

- [x] **Task 5: Xây dựng Composable Live Preview dùng chung (`CardPreview.kt`)**
  - **File:** `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/wallet/components/CardPreview.kt`
  - **Mô tả:**
    - Thẻ bo góc 20dp, bóng đổ theo màu kẹo Candy Pop 3D.
    - Nền gradient theo `CardColor`, tên thẻ cập nhật thời gian thực theo draft (hoặc "Tên thẻ" nếu rỗng).
    - Icon nhãn ví trung tính (`ChankoIconId.BUD_01`), không logo VISA/chip.
  - **Evidence:** Tích hợp và biên dịch thành công.

- [x] **Task 6: Xây dựng Composable Dialog C07 (`RenameCreditCardDialog.kt`)**
  - **File:** `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/wallet/dialogs/RenameCreditCardDialog.kt`
  - **Mô tả:**
    - Centered Modal Dialog với `DialogProperties(usePlatformDefaultWidth = false)` trên nền dim.
    - Header: Icon `ChankoIconId.ACT_05`, tiêu đề "Đổi tên thẻ tín dụng", nút đóng X (`ChankoIconId.ACT_03`).
    - Input: `OutlinedTextField` counter `n/30`, inline error, hỗ trợ cảnh báo rút gọn cho thẻ legacy.
    - Màu thẻ hiện tại: Hiển thị chấm màu + tên màu tiếng Việt (chỉ đọc).
    - Tích hợp `CardPreview`.
    - Ghi chú: "Các giao dịch đã ghi nhận trước đây vẫn hiển thị theo tên thẻ cũ, không bị thay đổi." (`ChankoIconId.SYS_15`).
    - Footer: Nút "Hủy" và "Lưu thay đổi" (disabled khi no-change/invalid, loading khi submitting).
    - Cuộn dọc khi IME hiển thị (`imePadding` và `verticalScroll`).
  - **Evidence:** UI Composable hoàn chỉnh và đạt chuẩn responsive.

- [x] **Task 7: Tích hợp Dialog vào Giao diện & Dọn dẹp Sheet cũ**
  - **Files:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/BudgetRoute.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/sheets/BudgetSheetHost.kt`
    - Xóa `RenameCreditCardSheet.kt` cũ.
  - **Mô tả:**
    - Tích hợp `RenameCreditCardDialog` vào `BudgetRoute.kt` ngang hàng với `DeleteCardConfirmDialog`.
    - Xử lý BackHandler khi dialog đang mở.
    - Gỡ bỏ hoàn toàn `RenameCreditCardSheet.kt` cũ.
  - **Evidence:** Clean architecture, không còn sheet thừa cho đổi tên thẻ.

- [x] **Task 8: Kiểm thử Tổng thể & Nghiệm thu AC30–AC39**
  - **Mô tả:**
    - Chạy toàn bộ Unit Tests: `./gradlew testDevDebugUnitTest` (100% PASS).
    - Chạy build: `./gradlew assembleDevDebug` (BUILD SUCCESSFUL).
    - Đối chiếu và kiểm tra từng tiêu chí trong bảng nghiệm thu AC30–AC39.
  - **Evidence:**
    - AC30: PASS. Dialog căn giữa trên nền dim, không phải sheet/route.
    - AC31: PASS. Live preview cập nhật realtime theo draft; màu chỉ đọc; không VISA/chip.
    - AC32: PASS. Giới hạn 30 graphemes, counter đếm đúng chuẩn NFC (Techcombank=11).
    - AC33: PASS. No-change khóa Lưu; đổi hoa/thường cho phép lưu; trùng thẻ khác hoặc "Pay Now" bị khóa.
    - AC34: PASS. Thẻ legacy >30 ký tự prefill đầy đủ không cắt ngắn, hiển thị hướng dẫn rút gọn ≤ 30.
    - AC35: PASS. Hủy/X/Back/scrim có dirty check mở DiscardChangesDialog; không ghi khi dismiss.
    - AC36: PASS. Submitting khóa dismiss; lỗi giữ draft và hiển thị "Chưa đổi tên được thẻ. Vui lòng thử lại.".
    - AC37: PASS. Offline và past cycle khóa quyền canSave.
    - AC38: PASS. Đổi tên thành công chỉ đổi tên thẻ; chi tiêu cũ giữ nguyên snapshot tên cũ; snackbar "Đã đổi tên thẻ".
    - AC39: PASS. Cuộn dọc với bàn phím ảo IME, min touch target 48dp, contentDescription đầy đủ cho TalkBack.
