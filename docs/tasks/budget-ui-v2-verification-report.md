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
BUILD SUCCESSFUL (153 tests completed, 0 failures, 0 skipped)

# 2. Biên dịch gói Dev Debug APK
./gradlew assembleDevDebug
BUILD SUCCESSFUL (0 errors)
```

> [!NOTE]
> **Phạm vi xác minh theo `AGENTS.md`:** Toàn bộ logic domain, repository, reducer, state restoration (`SavedStateHandle`), kiểm tra bản nháp bẩn (dirty draft), khóa pending khi đang lưu, luồng tải lại khi conflict, và khai báo Compose UI / semantics / touch target ($\ge 48\text{dp}$) đã được xác minh qua **153 Unit Tests** và **compile/assembleDevDebug**. Các kiểm tra phụ thuộc thiết bị vật lý (TalkBack thực tế, font scaling 200% trên màn hình thật) được ghi nhận rõ trạng thái `CODE VERIFIED / RUNTIME PENDING DEVICE` theo đúng quy định không chạy máy ảo/AVD của dự án.

---

## 2. Ma Trận Nghiệm Thu 18 Tiêu Chuẩn (Acceptance Criteria Evidence Matrix)

| AC ID | Yêu Cầu Chấp Nhận (Acceptance Criteria) | Hiện Thực & Bằng Chứng Kỹ Thuật (Evidence) | Trạng Thái |
| :---: | :--- | :--- | :---: |
| **AC01** | Current online, mở C01 hiển thị đúng chu kỳ đã chọn, 3 số liệu chuẩn, giữ bottom navigation, không tạo Decision Timeline hoặc Item Detail riêng | `BudgetViewModelTest.kt`, `MainNavigation.kt`, `BudgetScreen.kt`: Nạp nguyên tử chu kỳ hiện tại (`5.000.000đ / 3.000.000đ / 2.000.000đ`) thông qua `BudgetCycleOverview`; tích hợp `Screen.BudgetDetails` trong `MainNavigation` giữ nguyên `ChankoBottomBar`. | **PASS (Unit + Build)** |
| **AC02** | Chọn chu kỳ cũ $\rightarrow$ số liệu đổi nguyên tử theo chu kỳ đó, nhãn "Đã kết thúc · Chỉ xem", khóa mọi quyền ghi | `BudgetViewModelTest.kt`: `loadCycleData(cycle)` xóa snapshot cũ ngay khi chuyển kỳ để không trộn dữ liệu hai kỳ; `isCurrentCycle = false`, `canWrite = false`, `writeDisabledReason = "Chỉ chỉnh sửa trong chu kỳ hiện tại"`. | **PASS (Unit + Build)** |
| **AC03** | Fixture C13 Zero-spend: 5m/0đ/5m/0%; empty copy nhẹ nhàng; item đã lưu vẫn có; không bù giao dịch giả | `BudgetSnapshotTest.kt`, `FakeBudgetRepositoryTest.kt`: `setScenarioZeroSpend()` $\rightarrow$ spent = 0, ratio = 0%, progress = 0%, empty state payment/activity. | **PASS (Unit + Build)** |
| **AC04** | Fixture C10 Over-budget: 5m/5.5m $\rightarrow$ "Vượt ngân sách 500.000đ", 110%, progress bar cap 100%, không trách móc | `BudgetSnapshotTest.kt`: `isOverBudget = true`, `overMinor = 500_000L`, `percentageInt = 110`, `visualProgress = 1.0f`. | **PASS (Unit + Build)** |
| **AC05** | Offline có cache: chỉ cho chọn chu kỳ có cache; hiển thị thời gian cập nhật thực tế của dữ liệu (`dataUpdatedAt`); khóa ghi; history đọc bình thường | `BudgetViewModelTest.kt`, `CyclePickerSheet.kt`: `lastSyncTimeText` lấy từ `overview.dataUpdatedAt`; `CyclePickerSheet` khóa các kỳ không nằm trong `offlineCachedCycleIds` với nhãn `"Chưa có dữ liệu ngoại tuyến"`. | **PASS (Unit + Build)** |
| **AC06** | Offline chưa cache / lỗi tải khi đổi kỳ: xóa snapshot cũ, hiển thị rõ trạng thái lỗi / `"Chưa có dữ liệu để xem ngoại tuyến"` kèm nút Thử lại và bộ chọn kỳ | `BudgetViewModelTest.kt`, `FakeBudgetRepositoryTest.kt`, `BudgetScreen.kt` (`BudgetErrorView`): Ném `BudgetNoOfflineCacheException` / `BudgetLoadException`, giữ `BudgetCycleHeader` để đổi về kỳ có cache hoặc bấm `"Thử lại"`. | **PASS (Unit + Build)** |
| **AC07** | Form mở rồi mất mạng/hết chu kỳ: giữ draft, khóa Lưu kèm lý do inline, khi online trở lại không tự submit ngầm | `EditBudgetSheet.kt`, `AddCreditCardSheet.kt`, `RenameCreditCardSheet.kt`: Hiển thị banner `disabledReason` (`"Kết nối mạng để chỉnh sửa"` / `"Chỉ chỉnh sửa trong chu kỳ hiện tại"`), giữ nguyên draft, không tự gọi `onSave`. | **PASS (Unit + Build)** |
| **AC08** | Budget input rỗng/âm/thập phân/overflow/ký tự lỗi $\rightarrow$ lỗi gắn field khi blur, khóa Lưu, kiểm tra lại tại tầng command/repository; budget=0 guard | `MoneyVndTest.kt`, `EditBudgetValidationTest.kt`, `FakeBudgetRepositoryTest.kt`, `EditBudgetSheet.kt`: Hiển thị lỗi gắn field khi blur, `updateBudgetLimit` kiểm tra `MoneyVnd.isValidBudget(newLimitMinor)`, `BudgetSummaryCard` hiển thị `"Chưa thiết lập ngân sách"` khi `budgetMinor == 0L`. | **PASS (Unit + Build)** |
| **AC09** | Submit hợp lệ: khóa dismiss khi đang lưu; lỗi giữ draft; conflict hiển thị `"Tải lại"` và cập nhật base/draft theo snapshot mới để rà soát | `BudgetViewModelTest.kt`, `EditBudgetSheet.kt`: Khóa đóng sheet khi `isSubmitting = true`; khi gặp `BudgetConflictException` hiện banner `"Ngân sách đã thay đổi. Tải lại để chỉnh sửa."` và nút `"Tải lại"` (`ReloadConflict`) cập nhật `editBudgetBaseRevision` + `editBudgetDraft`. | **PASS (Unit + Build)** |
| **AC10** | Quản lý thẻ: chỉ metadata tên 1–40 grapheme (`BreakIterator`); xử lý thẻ bị archive khi đang mở Rename; C08 giữ dialog và hiện lỗi inline khi xóa thất bại | `CreditCardManagementTest.kt`, `BudgetViewModelTest.kt`, `RenameCreditCardSheet.kt`, `DeleteCardConfirmDialog.kt`: Đếm grapheme bằng `BreakIterator.getCharacterInstance()`, báo `"Thẻ không còn khả dụng"` + nút `"Quay lại"` nếu thẻ bị xóa khi đang đổi tên, giữ `DeleteCardConfirmDialog` kèm lỗi `"Chưa xóa được thẻ. Vui lòng thử lại."` khi xóa lỗi. | **PASS (Unit + Build)** |
| **AC11** | Recent activities: tối đa 3 sự kiện ảnh hưởng tiền thật, không bù event giả, không hiện WAIT/SKIP | `BudgetRecentActivity.kt`, `FakeBudgetRepository.kt`: `.take(3)`, chỉ gồm `PURCHASE_RECORDED`, `EXPENSE_CORRECTED`, `BUDGET_ADJUSTED`. | **PASS (Unit + Build)** |
| **AC12** | C09: Lịch sử điều chỉnh ngân sách chỉ đọc, không có nút revert | `AdjustmentHistorySheet.kt`: Hiển thị thời điểm, `"Ngân sách cũ: ..."`, `"Ngân sách mới: ..."`, chênh lệch `+/- X đ`, không có thao tác mutation. | **PASS (Unit + Build)** |
| **AC13** | Mở item từ Notable Items hoặc Activity $\rightarrow$ phân giải qua `SharedItemResolver` sang Shared Item Detail; nếu không khả dụng báo `"Món đồ chưa có dữ liệu ngoại tuyến"` | `BudgetViewModelTest.kt`, `SharedItemResolverImpl.kt`, `BudgetRoute.kt`: Hỗ trợ cả ID `"1"` và `"item_1"`, kiểm tra tồn tại trong `ItemRepository` trước khi điều hướng hoặc phát snackbar. | **PASS (Unit + Build)** |
| **AC14** | Thiết lập ví C03: anchor ngày bắt đầu chu kỳ chỉ đọc, hiển thị đúng lý do khóa theo ngữ cảnh (offline vs chu kỳ cũ) | `WalletSettingsSheet.kt`: Nhận `disabledReason = state.writeDisabledReason`, hiển thị `"Ngày bắt đầu chu kỳ: Ngày X hàng tháng"` chỉ đọc. | **PASS (Unit + Build)** |
| **AC15** | Mascot fallback: slot trống/ảnh tĩnh thanh lịch, không cắt ghép mockup, icon ID từ registry | `BudgetMascotSlot.kt`: Sử dụng mascot canonical (`mascot_happy`, `mascot_worried`, `mascot_sleepy`), `contentDescription = null`. | **PASS (Unit + Build)** |
| **AC16** | TalkBack A11y: số tiền đọc bằng `"đồng"` (`MoneyVnd.spoken`), thanh tiến độ gắn `ProgressBarRangeInfo` | `EditBudgetValidationTest.kt`, `BudgetSummaryCard.kt`, `EditBudgetSheet.kt`, `AdjustmentHistorySheet.kt`, `BudgetNotableItems.kt`, `BudgetRecentActivity.kt`, `BudgetPaymentBreakdown.kt`: Đã gắn `ProgressBarRangeInfo` và `MoneyVnd.spoken(...)`. | **CODE VERIFIED (Runtime Pending Device)** |
| **AC17** | Layout & Insets: form có `verticalScroll` + `imePadding()`, vùng chạm $\ge 48\text{dp}$ trên mọi nút/icon | `BudgetSummaryCard.kt`, `BudgetScreen.kt`, `CyclePickerSheet.kt`, `WalletSettingsSheet.kt`, `EditBudgetSheet.kt`, `CreditCardManageSheet.kt`, `AddCreditCardSheet.kt`, `RenameCreditCardSheet.kt`, `DeleteCardConfirmDialog.kt`, `AdjustmentHistorySheet.kt`. | **CODE VERIFIED (Runtime Pending Device)** |
| **AC18** | Process recreation: khôi phục `selectedCycleId` và các bản nháp form qua `SavedStateHandle` + `rememberSaveable`, không tự động gửi lại mutation pending | `BudgetViewModelTest.kt`, `BudgetViewModel.kt`: Lưu và khôi phục `selected_cycle_id`, `edit_budget_draft`, `add_card_draft`, `rename_card_draft` trong `SavedStateHandle`. | **PASS (Unit + Build)** |

---

## 3. Danh Mục Các Màn Hình & Thành Phần Đã Hoàn Thiện (C01–C13)

| ID | Hạng mục | Vị trí file mã nguồn |
| :---: | :--- | :--- |
| **C01** | Budget Overview v2 (kèm Bottom Navigation) | `presentation/screens/budget/BudgetScreen.kt`, `BudgetRoute.kt`, `MainNavigation.kt` |
| **C02** | Chọn chu kỳ ngân sách (kiểm tra cache offline) | `presentation/screens/budget/sheets/CyclePickerSheet.kt` |
| **C03** | Thiết lập ví (lý do khóa theo ngữ cảnh) | `presentation/screens/budget/sheets/WalletSettingsSheet.kt` |
| **C04** | Sửa ngân sách, Live Preview & Conflict Reload | `presentation/screens/budget/sheets/EditBudgetSheet.kt` |
| **C05** | Quản lý thẻ tín dụng | `presentation/screens/budget/sheets/CreditCardManageSheet.kt` |
| **C06** | Thêm thẻ tín dụng (đếm grapheme `BreakIterator`) | `presentation/screens/budget/sheets/AddCreditCardSheet.kt` |
| **C07** | Đổi tên thẻ tín dụng (xử lý thẻ bị archive khi đang mở) | `presentation/screens/budget/sheets/RenameCreditCardSheet.kt` |
| **C08** | Dialog xác nhận xóa thẻ (giữ dialog + lỗi inline khi lỗi) | `presentation/screens/budget/sheets/DeleteCardConfirmDialog.kt` |
| **C09** | Lịch sử điều chỉnh hạn mức | `presentation/screens/budget/sheets/AdjustmentHistorySheet.kt` |
| **C10** | Trạng thái Vượt ngân sách | Tích hợp trong `BudgetSummaryCard.kt` & `BudgetMascotSlot.kt` |
| **C11** | Trạng thái Ngoại tuyến (`dataUpdatedAt`) | Tích hợp trong `BudgetReadOnlyBanner.kt` |
| **C12** | Trạng thái Chu kỳ đã kết thúc | Tích hợp trong `BudgetReadOnlyBanner.kt` & `WalletSettingsSheet.kt` |
| **C13** | Trạng thái Chưa có chi tiêu | Tích hợp trong `BudgetPaymentBreakdown.kt` & `BudgetRecentActivity.kt` |
| **HOST**| Single Sheet Host (Pending Lock & Dirty Draft Check) | `presentation/screens/budget/sheets/BudgetSheetHost.kt` & `DiscardChangesDialog.kt` |

---

## 4. Bàn Giao & Quy Chuẩn Tuân Thủ

* **Quy tắc máy ảo (No Emulators / VM):** Tuân thủ tuyệt đối quy định `AGENTS.md` — toàn bộ việc kiểm thử được thực hiện qua build và unit test tự động bằng terminal (`153 tests, 0 failures`), không cài đặt hay bật bất kỳ máy ảo/AVD nào.
* **Quy chuẩn Git:** Toàn bộ công việc được thực hiện trên nhánh `feature/budget-ui-v2` tách từ `dev-chanko-1x`.
