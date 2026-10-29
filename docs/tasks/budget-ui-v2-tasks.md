# Danh Sách Đầu Việc Kỹ Thuật: Chanko Budget UI v2.0 (Phase 3: Tasks)

> **Tài liệu đặc tả:** [docs/features/budget-ui-v2-spec.md](../features/budget-ui-v2-spec.md)  
> **Kế hoạch kiến trúc:** [docs/plans/budget-ui-v2-plan.md](../plans/budget-ui-v2-plan.md)  
> **Master Spec & Capability Map:** [SPEC-budget-ui-v2.md](../../SPEC-budget-ui-v2.md)  
> **Trạng thái:** TASKS SPECIFIED · READY FOR HUMAN APPROVAL & IMPLEMENTATION

---

## 1. Module: `budget-domain-data`

### Task 1: Thiết Lập Git Branch & Chuỗi Tài Nguyên Tiếng Việt (Strings Resources)
* **Module:** `budget-domain-data`
* **Files:**
  * Git branch: `feature/budget-ui-v2` (tách từ `dev-chanko-1x`)
  * `app/src/main/res/values/strings.xml`
* **Depends on:** Không có.
* **Mô tả:**
  * Tạo nhánh feature mới `feature/budget-ui-v2` theo đúng quy định `AGENTS.md`.
  * Khai báo toàn bộ string resources tiếng Việt cho 13 hạng mục (C01–C13), các nhãn trạng thái (Vượt ngân sách, Ngoại tuyến, Đã kết thúc, Chưa có chi tiêu), các câu lỗi validation, helper và dialog confirmations.
* **Verification:**
  - [x] Git branch hiện tại là `feature/budget-ui-v2`.
  - [x] `./gradlew assembleDevDebug` hoàn thành thành công không có lỗi trùng hoặc thiếu resource.

---

### Task 2: Pure Domain Models & Xử Lý Số Học Tiền Tệ (MoneyVnd)
* **Module:** `budget-domain-data`
* **Files:**
  * `app/src/main/java/com/hoabh/chanko/domain/budget/model/MoneyVnd.kt`
  * `app/src/main/java/com/hoabh/chanko/domain/budget/model/BudgetCycle.kt`
  * `app/src/main/java/com/hoabh/chanko/domain/budget/model/BudgetSnapshot.kt`
  * `app/src/main/java/com/hoabh/chanko/domain/budget/model/CreditCard.kt`
  * `app/src/main/java/com/hoabh/chanko/domain/budget/model/BudgetActivity.kt`
  * `app/src/main/java/com/hoabh/chanko/domain/budget/model/BudgetAdjustment.kt`
  * `app/src/main/java/com/hoabh/chanko/domain/budget/model/PaymentMethodBreakdown.kt`
  * `app/src/main/java/com/hoabh/chanko/domain/budget/model/NotableItem.kt`
  * `app/src/test/java/com/hoabh/chanko/domain/budget/model/MoneyVndTest.kt`
  * `app/src/test/java/com/hoabh/chanko/domain/budget/model/BudgetSnapshotTest.kt`
* **Depends on:** Task 1.
* **Mô tả:**
  * Xây dựng các Domain Models thuần túy không phụ thuộc Android SDK.
  * `MoneyVnd`: Quản lý số tiền VND nguyên `Long`, format `5.000.000đ`, parse chuỗi có dấu chấm phân cách.
  * `BudgetSnapshot`: Tính toán `remainingMinor`, `overMinor`, `ratio`, `percentageInt` (Half-Up), `visualProgress` (clamp 0..1), guard `budget = 0`.
* **Verification:**
  - [x] Chạy `./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.domain.budget.model.*"`
  - [x] 100% unit tests pass, kiểm tra đầy đủ các ca biên: âm, 0đ, chia cho 0, vượt hạn mức.

---

### Task 3: BudgetRepository Interface & In-Memory FakeBudgetRepository Với Đầy Đủ Fixtures
* **Module:** `budget-domain-data`
* **Files:**
  * `app/src/main/java/com/hoabh/chanko/domain/budget/repository/BudgetRepository.kt`
  * `app/src/main/java/com/hoabh/chanko/data/budget/FakeBudgetRepository.kt`
  * `app/src/test/java/com/hoabh/chanko/data/budget/FakeBudgetRepositoryTest.kt`
* **Depends on:** Task 2.
* **Mô tả:**
  * Định nghĩa interface `BudgetRepository`.
  * Xây dựng `FakeBudgetRepository` hỗ trợ 100% fixture C10–C13:
    * Fixture Current Within: `5.000.000đ / 3.000.000đ` (còn `2.000.000đ`, 60%).
    * Fixture C10 Over-budget: `5.000.000đ / 5.500.000đ` (vượt `500.000đ`, 110%).
    * Fixture C13 Zero-spend: `5.000.000đ / 0đ` (còn `5.000.000đ`, 0%).
    * Fixture C12 Past-cycle: Chu kỳ tháng trước `isCurrent = false`.
    * Fixture C11 Offline cache & no-cache switch.
    * Thẻ tín dụng: danh sách thẻ hoạt động, thẻ rỗng, xóa thẻ là archive (`isArchived = true`).
    * Biến động gần nhất: tối đa 3 event ảnh hưởng tiền, sắp xếp mới nhất trước.
    * Lịch sử điều chỉnh hạn mức C09.
* **Verification:**
  - [x] Chạy `./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.data.budget.FakeBudgetRepositoryTest"`
  - [x] Tất cả các ca fixture, mutation, archive thẻ và conflict mô phỏng đều pass.

---

## 2. Module: `budget-overview-ui`

### Task 4: BudgetUiState, BudgetUiEvent & BudgetViewModel (FSM Permission Reducer)
* **Module:** `budget-overview-ui`
* **Files:**
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/BudgetUiState.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/BudgetUiEvent.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/BudgetUiEffect.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/BudgetViewModel.kt`
  * `app/src/test/java/com/hoabh/chanko/presentation/screens/budget/BudgetViewModelTest.kt`
* **Depends on:** Task 3.
* **Mô tả:**
  * Xây dựng `BudgetViewModel` quản lý StateFlow đa chiều:
    * `isOnline`, `isCurrentCycle`, `isLoading`, `isSubmitting`.
    * Centralized reducer: `canWrite = isOnline && isCurrentCycle && !isLoading && !isSubmitting`.
    * Điều hướng chu kỳ (chọn chu kỳ cập nhật snapshot tương ứng).
    * Quản lý stack của Single Sheet Host và dialog xác nhận "Bỏ thay đổi?".
* **Verification:**
  - [x] Chạy `./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.budget.BudgetViewModelTest"`
  - [x] Kiểm tra reducer chuyển đổi trạng thái chính xác: Offline và Past-cycle khóa quyền ghi.

---

### Task 5: Xây Dựng Các Thành Phần Giao Diện Overview (Components)
* **Module:** `budget-overview-ui`
* **Files:**
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/components/BudgetSummaryCard.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/components/BudgetCycleHeader.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/components/BudgetMascotSlot.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/components/BudgetPaymentBreakdown.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/components/BudgetNotableItems.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/components/BudgetRecentActivity.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/components/BudgetReadOnlyBanner.kt`
* **Depends on:** Task 4.
* **Mô tả:**
  * Dựng các Composable con theo đúng token màu Candy Pop, tuân thủ vùng chạm $\ge 48\text{dp}$, icon $24\text{dp}$:
    * `BudgetSummaryCard`: 3 dòng tiền (Ngân sách, Đã chi, Còn lại / Vượt ngân sách), thanh progress kẹo bo góc, nút "Sửa ngân sách".
    * `BudgetCycleHeader`: Nút bấm chu kỳ kèm khoảng ngày và chevron (`BUD-05`).
    * `BudgetMascotSlot`: Slot mascot tĩnh hiển thị sắc thái tương ứng trạng thái ngân sách (`Relaxed`, `Neutral`, calm over-budget).
    * `BudgetPaymentBreakdown`: Danh sách phương thức thanh toán trong kỳ (chỉ đọc).
    * `BudgetNotableItems`: Tối đa 3 món đã lưu, tap mở shared item detail.
    * `BudgetRecentActivity`: Tối đa 3 biến động ảnh hưởng ngân sách.
    * `BudgetReadOnlyBanner`: Banner ngoại tuyến (C11) và badge đã kết thúc (C12).
* **Verification:**
  - [x] `./gradlew assembleDevDebug` build thành công không lỗi syntax hay missing imports.

---

### Task 6: Hoàn Thiện C01 BudgetScreen, BudgetRoute & Tích Hợp Điều Hướng Từ HomeScreen
* **Module:** `budget-overview-ui`
* **Files:**
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/BudgetScreen.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/BudgetRoute.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/navigation/Screen.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/navigation/MainNavigation.kt`
* **Depends on:** Task 5.
* **Mô tả:**
  * Lắp ghép các thành phần thành `BudgetScreen` (Stateless C01 Overview) và `BudgetRoute` (Stateful).
  * Khai báo route `Screen.BudgetDetails` trong `Screen.kt`.
  * Hook `HomeUiEffect.NavigateToBudgetDetails` từ `HomeScreen` để chuyển sang `BudgetRoute`.
  * Hỗ trợ nút Back quay trở lại HomeScreen giữ nguyên trạng thái.
* **Verification:**
  - [x] `./gradlew assembleDevDebug` hoàn thành thành công.
  - [x] Bấm nút "Chi tiết ↗" trên `BudgetCard` kích hoạt navigation tới `BudgetDetails`.

---

## 3. Module: `budget-cycle-wallet`

### Task 7: Single Sheet Host & C02 Sheet Chọn Chu Kỳ (CyclePickerSheet)
* **Module:** `budget-cycle-wallet`
* **Files:**
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/sheets/BudgetSheetHost.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/sheets/CyclePickerSheet.kt`
* **Depends on:** Task 6.
* **Mô tả:**
  * Xây dựng `BudgetSheetHost` quản lý `ModalBottomSheet` duy nhất với ngăn xếp lùi (internal backstack).
  * Xây dựng `CyclePickerSheet` (C02): hiển thị danh sách radio chọn chu kỳ (ngày bắt đầu - kết thúc, nhãn "Hiện tại" hoặc "Đã kết thúc").
  * Tap vào chu kỳ hợp lệ: chọn chu kỳ, đóng sheet ngay lập tức về Overview. Chu kỳ chưa cache ngoại tuyến bị disabled.
* **Verification:**
  - [x] `./gradlew testDevDebugUnitTest` & `./gradlew assembleDevDebug` pass.

---

### Task 8: C03 Sheet Thiết Lập Ví & C09 Sheet Lịch Sử Điều Chỉnh Ngân Sách
* **Module:** `budget-cycle-wallet`
* **Files:**
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/sheets/WalletSettingsSheet.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/sheets/AdjustmentHistorySheet.kt`
* **Depends on:** Task 7.
* **Mô tả:**
  * `WalletSettingsSheet` (C03): xem thông tin cycle, ngân sách hiện tại, anchor ngày bắt đầu chỉ đọc. 3 dòng dẫn sang C04, C05, C09. Vô hiệu hóa hàng mutation khi offline hoặc past-cycle.
  * `AdjustmentHistorySheet` (C09): hiển thị danh sách các lần đổi hạn mức (thời điểm, cũ, mới, chênh lệch tăng/giảm). Hàng chỉ đọc.
* **Verification:**
  - [x] `./gradlew assembleDevDebug` pass.

---

## 4. Module: `budget-edit-preview`

### Task 9: C04 Sheet Sửa Ngân Sách Kèm Live Preview Tức Thì & Dialog Bỏ Bản Nháp
* **Module:** `budget-edit-preview`
* **Files:**
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/sheets/EditBudgetSheet.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/sheets/DiscardChangesDialog.kt`
  * `app/src/test/java/com/hoabh/chanko/presentation/screens/budget/EditBudgetValidationTest.kt`
* **Depends on:** Task 8.
* **Mô tả:**
  * Xây dựng `EditBudgetSheet` (C04): trường nhập số tiền VND với bàn phím số, prefill giá trị cũ.
  * Live Preview: cập nhật tức thời "Ngân sách mới", "Còn lại sau thay đổi" hoặc "Vượt ngân sách sau thay đổi", thanh progress theo từng ký tự.
  * Validation: 1–999.999.999.999đ, chặn số âm/thập phân/rỗng.
  * Cảnh báo hạ hạn mức dưới số đã chi: hiển thị cảnh báo nhưng vẫn cho phép Lưu.
  * Xây dựng `DiscardChangesDialog`: hiển thị khi người dùng thoát form có dữ liệu nháp thay đổi.
* **Verification:**
  - [x] Chạy `./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.budget.EditBudgetValidationTest"`
  - [x] 100% tests pass.

---

## 5. Module: `budget-card-management`

### Task 10: Phân Hệ Quản Lý Thẻ Tín Dụng (C05 Quản Lý, C06 Thêm, C07 Đổi Tên, C08 Xóa Thẻ)
* **Module:** `budget-card-management`
* **Files:**
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/sheets/CreditCardManageSheet.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/sheets/AddCreditCardSheet.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/sheets/RenameCreditCardSheet.kt`
  * `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/sheets/DeleteCardConfirmDialog.kt`
  * `app/src/test/java/com/hoabh/chanko/presentation/screens/budget/CreditCardManagementTest.kt`
* **Depends on:** Task 9.
* **Mô tả:**
  * `CreditCardManageSheet` (C05): hiển thị danh sách thẻ, action đổi tên (mở C07) và xóa (mở C08), footer thêm thẻ (mở C06).
  * `AddCreditCardSheet` (C06): nhập tên thẻ, counter 0/40, trim, Unicode NFC, kiểm tra trùng tên case-insensitive.
  * `RenameCreditCardSheet` (C07): prefill tên cũ, kiểm tra trùng tên ngoại trừ chính nó.
  * `DeleteCardConfirmDialog` (C08): `AlertDialog` xác nhận xóa thẻ (archive record, không cascade chi tiêu).
* **Verification:**
  - [x] Chạy `./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.budget.CreditCardManagementTest"`
  - [x] 100% tests pass.

---

## 6. Module: `verification-and-report`

### Task 11: Tổng Nghiệm Thu Toàn Diện 18 Tiêu Chuẩn AC01–AC18 & Báo Cáo Bàn Giao
* **Module:** Toàn bộ
* **Files:**
  * `docs/tasks/budget-ui-v2-verification-report.md`
* **Depends on:** Task 10.
* **Mô tả:**
  * Chạy toàn bộ test suite dự án và phân hệ Budget.
  * Biên dịch và kiểm tra lint.
  * Lập bảng báo cáo bằng chứng nghiệm thu (Evidence Matrix) chi tiết đối chiếu với 18 tiêu chuẩn AC01–AC18 của tài liệu bàn giao `ACCEPTANCE.md`.
* **Verification:**
  - [x] `./gradlew testDevDebugUnitTest` hoàn thành 100% GREEN (146 tests, 0 failures).
  - [x] `./gradlew assembleDevDebug` BUILD SUCCESSFUL.
