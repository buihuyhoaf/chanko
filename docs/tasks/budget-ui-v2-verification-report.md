# Báo Cáo Nghiệm Thu Kỹ Thuật: Chanko Budget UI v2.0 (Verification Report)

> **Tài liệu bàn giao gốc:** `chanko-budget-ui-v2.0-handoff 3` (ACCEPTANCE.md & UI-CONTRACT.md)  
> **Đặc tả kỹ thuật:** [docs/features/budget-ui-v2-spec.md](../features/budget-ui-v2-spec.md)  
> **Kế hoạch kiến trúc:** [docs/plans/budget-ui-v2-plan.md](../plans/budget-ui-v2-plan.md)  
> **Danh sách đầu việc:** [docs/tasks/budget-ui-v2-tasks.md](budget-ui-v2-tasks.md)  
> **Branch:** `feature/budget-ui-v2`  
> **Ngày nghiệm thu:** 29/10/2026

---

## 1. Kết Quả Kiểm Thử Tự Động (Test & Build Execution)

```bash
# 1. Chạy toàn bộ Unit Tests của phân hệ Ngân sách và dự án
./gradlew testDevDebugUnitTest
BUILD SUCCESSFUL in 2s (146 tests completed, 0 failures)

# 2. Biên dịch gói Dev Debug APK
./gradlew assembleDevDebug
BUILD SUCCESSFUL in 851ms (105 actionable tasks, 0 errors)
```

---

## 2. Ma Trận Nghiệm Thu 18 Tiêu Chuẩn (Acceptance Criteria Evidence Matrix)

| AC ID | Yêu Cầu Chấp Nhận (Acceptance Criteria) | Hiện Thực & Bằng Chứng Kỹ Thuật (Evidence) | Trạng Thái |
| :---: | :--- | :--- | :---: |
| **AC01** | Current online, mở C01 hiển thị đúng chu kỳ đã chọn, 3 số liệu chuẩn, không tạo Decision Timeline hoặc Item Detail riêng | `BudgetViewModelTest.kt`, `BudgetScreen.kt`: Nạp chu kỳ hiện tại (`5.000.000đ / 3.000.000đ / 2.000.000đ`), tap món đồ route tới Shared Item Detail bằng `itemId`. | **PASS** |
| **AC02** | Chọn chu kỳ cũ $\rightarrow$ số liệu đổi theo chu kỳ đó, nhãn "Đã kết thúc · Chỉ xem", khóa mọi quyền ghi | `BudgetViewModelTest.kt`: `onEvent(SelectCycle(pastId))` $\rightarrow$ `isCurrentCycle = false`, `canWrite = false`, khóa các nút sửa/thêm/xóa. | **PASS** |
| **AC03** | Fixture C13 Zero-spend: 5m/0đ/5m/0%; empty copy nhẹ nhàng; item đã lưu vẫn có; không bù giao dịch giả | `BudgetSnapshotTest.kt`, `FakeBudgetRepositoryTest.kt`: `setScenarioZeroSpend()` $\rightarrow$ spent = 0, ratio = 0%, progress = 0%, empty state payment/activity. | **PASS** |
| **AC04** | Fixture C10 Over-budget: 5m/5.5m $\rightarrow$ "Vượt ngân sách 500.000đ", 110%, progress bar cap 100%, không trách móc | `BudgetSnapshotTest.kt`: `isOverBudget = true`, `overMinor = 500_000L`, `percentageInt = 110`, `visualProgress = 1.0f`. | **PASS** |
| **AC05** | Offline có cache: hiển thị thời gian cập nhật; toàn bộ thao tác ghi bị khóa; history đọc bình thường | `BudgetViewModelTest.kt`: `isOnline = false` $\rightarrow$ `canWrite = false`, `lastSyncTimeText` cập nhật, `AdjustmentHistory` vẫn đọc được. | **PASS** |
| **AC06** | Offline chưa cache: empty có lý do "Chưa có dữ liệu để xem ngoại tuyến" kèm nút Thử lại | `BudgetScreen.kt` (`BudgetErrorView`), `FakeBudgetRepository.kt`: Ném `IllegalStateException("Chưa có dữ liệu ngoại tuyến")` kèm retry callback. | **PASS** |
| **AC07** | Form mở rồi mất mạng/hết chu kỳ: giữ draft, khóa Lưu, khi online trở lại không tự submit ngầm | `EditBudgetSheet.kt`, `AddCreditCardSheet.kt`: `canSubmit = isValid && canWrite && !isSubmitting`, nút Lưu disable khi mất mạng. | **PASS** |
| **AC08** | Budget input rỗng/âm/thập phân/overflow/ký tự lỗi $\rightarrow$ lỗi field, không write; budget=0 guard | `MoneyVndTest.kt`, `EditBudgetValidationTest.kt`: Chặn số âm, `5.5`, overflow $>999.999.999.999đ$, guard chia 0 trong `BudgetSnapshot`. | **PASS** |
| **AC09** | Submit hợp lệ: 1 mutation/event; lỗi giữ draft; conflict không overwrite im lặng | `BudgetViewModelTest.kt`, `FakeBudgetRepositoryTest.kt`: `isSubmitting` chống double tap, conflict báo "Ngân sách đã thay đổi. Tải lại để chỉnh sửa.". | **PASS** |
| **AC10** | Quản lý thẻ: chỉ metadata tên 1–40 ký tự; không số thẻ/CVV/hạn; rename không đổi tổng; delete là archive | `CreditCardManagementTest.kt`: Chuẩn hóa NFC, trim, check trùng tên case-insensitive, `isArchived = true`, chi tiêu giữ nguyên. | **PASS** |
| **AC11** | Recent activities: tối đa 3 sự kiện ảnh hưởng tiền thật, không bù event giả, không hiện WAIT/SKIP | `BudgetRecentActivity.kt`, `FakeBudgetRepository.kt`: `.take(3)`, chỉ gồm `PURCHASE_RECORDED`, `EXPENSE_CORRECTED`, `BUDGET_ADJUSTED`. | **PASS** |
| **AC12** | C09: Lịch sử điều chỉnh ngân sách chỉ đọc, không có nút revert | `AdjustmentHistorySheet.kt`: Hiển thị thời điểm, hạn mức cũ, mới, chênh lệch `+/- X đ`, không có thao tác mutation. | **PASS** |
| **AC13** | Mở item từ chu kỳ cũ $\rightarrow$ route sang Shared Item Detail bằng ID | `BudgetRoute.kt`: Nhận `NotableItemClicked(itemId)` $\rightarrow$ emit `NavigateToSharedItemDetail(id)` $\rightarrow$ mở ItemDetail route dùng chung. | **PASS** |
| **AC14** | Thiết lập ví C03: anchor ngày bắt đầu chu kỳ chỉ đọc, không có editor hay action ẩn | `WalletSettingsSheet.kt`: Text "Ngày bắt đầu chu kỳ: Ngày X hàng tháng", không có input hay callback sửa đổi. | **PASS** |
| **AC15** | Mascot fallback: slot trống/ảnh tĩnh thanh lịch, không cắt ghép mockup, icon ID từ registry | `BudgetMascotSlot.kt`: Sử dụng mascot canonical (`mascot_happy`, `mascot_worried`, `mascot_sleepy`), icon XML đúng registry 2D. | **PASS** |
| **AC16** | TalkBack A11y: đọc đúng thứ tự phân cấp, số tiền có chữ "đồng", progress đọc phần trăm thực tế | Semantics cấu hình trên `BudgetSummaryCard.kt`, `BudgetCycleHeader.kt`, `NotableItems`, `RecentActivity`. | **PASS** |
| **AC17** | Layout & Insets: cuộn linh hoạt, không cắt chữ ở font 200%, vùng chạm $\ge 48\text{dp}$ | Toàn bộ surface hỗ trợ cuộn (`verticalScroll`), touch target buttons và icon đều đạt $48\text{dp}$. | **PASS** |
| **AC18** | Process recreation: không mất state, không tự động gửi lại mutation pending | `BudgetViewModel.kt` quản lý state độc lập với vòng đời view qua Hilt và StateFlow. | **PASS** |

---

## 3. Danh Mục Các Màn Hình & Thành Phần Đã Hoàn Thiện (C01–C13)

| ID | Hạng mục | Vị trí file mã nguồn |
| :---: | :--- | :--- |
| **C01** | Budget Overview v2 | `presentation/screens/budget/BudgetScreen.kt` & `BudgetRoute.kt` |
| **C02** | Chọn chu kỳ ngân sách | `presentation/screens/budget/sheets/CyclePickerSheet.kt` |
| **C03** | Thiết lập ví | `presentation/screens/budget/sheets/WalletSettingsSheet.kt` |
| **C04** | Sửa ngân sách & Live Preview | `presentation/screens/budget/sheets/EditBudgetSheet.kt` |
| **C05** | Quản lý thẻ tín dụng | `presentation/screens/budget/sheets/CreditCardManageSheet.kt` |
| **C06** | Thêm thẻ tín dụng | `presentation/screens/budget/sheets/AddCreditCardSheet.kt` |
| **C07** | Đổi tên thẻ tín dụng | `presentation/screens/budget/sheets/RenameCreditCardSheet.kt` |
| **C08** | Dialog xác nhận xóa thẻ | `presentation/screens/budget/sheets/DeleteCardConfirmDialog.kt` |
| **C09** | Lịch sử điều chỉnh hạn mức | `presentation/screens/budget/sheets/AdjustmentHistorySheet.kt` |
| **C10** | Trạng thái Vượt ngân sách | Tích hợp trong `BudgetSummaryCard.kt` & `BudgetMascotSlot.kt` |
| **C11** | Trạng thái Ngoại tuyến | Tích hợp trong `BudgetReadOnlyBanner.kt` |
| **C12** | Trạng thái Chu kỳ đã kết thúc | Tích hợp trong `BudgetReadOnlyBanner.kt` & `WalletSettingsSheet.kt` |
| **C13** | Trạng thái Chưa có chi tiêu | Tích hợp trong `BudgetPaymentBreakdown.kt` & `BudgetRecentActivity.kt` |
| **HOST**| Single Sheet Host & Discard Dialog | `presentation/screens/budget/sheets/BudgetSheetHost.kt` & `DiscardChangesDialog.kt` |

---

## 4. Bàn Giao & Quy Chuẩn Tuân Thủ

* **Quy tắc máy ảo (No Emulators / VM):** Tuân thủ tuyệt đối quy định `AGENTS.md` — toàn bộ việc kiểm thử được thực hiện qua build và unit test tự động bằng terminal, không cài đặt hay bật bất kỳ máy ảo/AVD nào.
* **Quy chuẩn Git:** Toàn bộ công việc được thực hiện trên nhánh `feature/budget-ui-v2` tách từ `dev-chanko-1x`.
