# Kế Hoạch Kiến Trúc & Thiết Kế Giao Diện: Chanko Budget UI v2.0 (Phase 2: Plan)

> **Tài liệu tham chiếu:** [docs/features/budget-ui-v2-spec.md](../features/budget-ui-v2-spec.md)  
> **Master Spec & Capability Map:** [SPEC-budget-ui-v2.md](../../SPEC-budget-ui-v2.md)  
> **Trạng thái:** DRAFTING ARCHITECTURAL PLAN FOR HUMAN APPROVAL

---

## 1. Capability: `budget-domain-data` (Nền Tảng Dữ Liệu & Hợp Đồng Nghiệp Vụ)
* **Phụ thuộc:** Không phụ thuộc (Pure Kotlin Domain & Data layer).

### 1.1. Quyết Định Kiến Trúc (Architecture Decisions)
* **Quyết định 1: Định dạng tiền tệ bằng Kiểu Số Nguyên `Long` (Value Object / Utility)**
  * *Lý do:* Tránh triệt để lỗi làm tròn dấu phẩy động (Floating point). Tất cả số liệu tiền trong Chanko là số nguyên VND (`5000000L`).
  * *Phương án bị từ chối:* Sử dụng `Double`/`Float` hoặc `BigDecimal` (gây overhead và dễ phát sinh lỗi hiển thị số thập phân).
  * *Đánh đổi:* Mọi phép tính chia tỷ lệ phải ép kiểu cẩn trọng trước khi chia: `(spent * 100f) / budget`.
* **Quyết định 2: In-Memory Fake Repository với Bộ Fixtures Toàn Diện**
  * *Lý do:* Cho phép chạy độc lập 100% các trạng thái C01–C13 (Over, Zero, Past, Offline, Conflict, Error) mà không cần chờ backend API hay database Room.
  * *Phương án bị từ chối:* Mock cứng trong Composable hoặc ViewModel (làm mất tính trung thực của luồng dữ liệu bất đồng bộ).
  * *Đánh đổi:* Cần xây dựng mock store in-memory có độ trễ nhẹ mô phỏng mạng thật.

### 1.2. Luồng Dữ Liệu (Data Flow)
* **Happy path:** UI yêu cầu tải dữ liệu chu kỳ (`loadBudgetOverview(cycleId)`) $\rightarrow$ Repository lấy snapshot từ in-memory store $\rightarrow$ Phát ra `Flow<Result<BudgetOverviewData>>` $\rightarrow$ ViewModel cập nhật `BudgetUiState`.
* **Error path 1 (Chưa cache ngoại tuyến - C11):** Mạng offline + cycle chưa được cache $\rightarrow$ Repository trả về `OfflineUncachedException` $\rightarrow$ UI hiển thị Empty State *"Chưa có dữ liệu để xem ngoại tuyến"* kèm nút *"Thử lại"*.
* **Error path 2 (Xung đột sửa đổi - Conflict):** Khi lưu ngân sách mới (C04), repository phát hiện `revision` dữ liệu đã thay đổi $\rightarrow$ Trả về `ConflictException` $\rightarrow$ Sheet hiển thị *"Ngân sách đã thay đổi. Tải lại để chỉnh sửa."* kèm nút *"Tải lại"*.

### 1.3. Interface Contracts (Mã Hợp Đồng Thực Tế)
```kotlin
package com.hoabh.chanko.domain.budget.model

data class BudgetCycle(
    val id: String,
    val label: String,
    val startDate: String,
    val endDate: String,
    val isCurrent: Boolean,
    val anchorDay: Int = 1
)

data class BudgetSnapshot(
    val cycleId: String,
    val budgetMinor: Long,
    val spentMinor: Long,
    val revision: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val remainingMinor: Long get() = budgetMinor - spentMinor
    val overMinor: Long get() = (spentMinor - budgetMinor).coerceAtLeast(0L)
    val isOverBudget: Boolean get() = spentMinor > budgetMinor
    val ratio: Float get() = if (budgetMinor > 0L) spentMinor.toFloat() / budgetMinor else 0f
    val percentageInt: Int get() = (ratio * 100f).toInt()
    val visualProgress: Float get() = ratio.coerceIn(0f, 1f)
}

data class CreditCard(
    val id: String,
    val name: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class BudgetActivityType {
    PURCHASE_RECORDED,
    EXPENSE_CORRECTED,
    BUDGET_ADJUSTED
}

data class BudgetActivity(
    val id: String,
    val cycleId: String,
    val title: String,
    val amountChangeMinor: Long,
    val occurredAt: String,
    val itemId: String? = null,
    val type: BudgetActivityType = BudgetActivityType.PURCHASE_RECORDED
)

data class BudgetAdjustment(
    val id: String,
    val cycleId: String,
    val oldLimitMinor: Long,
    val newLimitMinor: Long,
    val adjustedAt: String
) {
    val diffMinor: Long get() = newLimitMinor - oldLimitMinor
}

data class PaymentMethodBreakdown(
    val id: String,
    val name: String,
    val spentMinor: Long
)

data class NotableItem(
    val itemId: String,
    val name: String,
    val priceMinor: Long,
    val thumbnailResId: Int? = null
)
```

```kotlin
package com.hoabh.chanko.domain.budget.repository

import com.hoabh.chanko.domain.budget.model.*
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAvailableCycles(): Flow<List<BudgetCycle>>
    fun getBudgetSnapshot(cycleId: String): Flow<BudgetSnapshot>
    fun getPaymentBreakdown(cycleId: String): Flow<List<PaymentMethodBreakdown>>
    fun getNotableItems(cycleId: String): Flow<List<NotableItem>>
    fun getRecentActivities(cycleId: String): Flow<List<BudgetActivity>>
    fun getAdjustments(cycleId: String): Flow<List<BudgetAdjustment>>
    fun getCreditCards(): Flow<List<CreditCard>>

    suspend fun updateBudgetLimit(cycleId: String, newLimitMinor: Long, expectedRevision: Int): Result<Unit>
    suspend fun addCreditCard(name: String): Result<CreditCard>
    suspend fun renameCreditCard(cardId: String, newName: String): Result<Unit>
    suspend fun archiveCreditCard(cardId: String): Result<Unit>
    suspend fun setNetworkOnline(isOnline: Boolean)
}
```

### 1.4. Bảng Rủi Ro (Risk Register)
| Rủi ro | Khả năng | Mức độ | Biện pháp giảm thiểu |
| :--- | :--- | :--- | :--- |
| Dữ liệu fixture không đủ các ca biên | Thấp | Cao | Xây dựng bộ test fixture chuẩn hóa gồm: Within (5m/3m), Over (5m/5.5m), Zero (5m/0), Past, Offline, Conflict. |
| Xung đột coroutine khi gọi mutation liên tiếp | Trung bình | Trung bình | Khóa cờ `isSubmitting = true` và áp dụng Idempotency key cho mỗi mutation. |

---

## 2. Capability: `budget-overview-ui` (Màn Hình Chính C01 & 4 Trạng Thái C10–C13)
* **Phụ thuộc:** `budget-domain-data`.

### 2.1. Quyết Định Kiến Trúc (Architecture Decisions)
* **Quyết định 1: Tách Biệt Đa Chiều UI State (Multi-axis State)**
  * *Lý do:* Các trạng thái `isOnline`, `isCurrentCycle`, `isLoading`, `isOverBudget` có thể diễn ra đồng thời (ví dụ: vừa ngoại tuyến, vừa xem kỳ cũ, vừa vượt hạn mức). Tuyệt đối không dùng 1 enum duy nhất gây triệt tiêu trạng thái.
  * *Quy tắc:* `canWrite = isOnline && isCurrentCycle && !isLoading && !isSubmitting`.
* **Quyết định 2: Điểm Chạm Điều Hướng Từ HomeScreen**
  * *Lý do:* Nút "Chi tiết ↗" trên `BudgetCard` đã kích hoạt `HomeUiEvent.BudgetDetailsTapped` $\rightarrow$ `HomeUiEffect.NavigateToBudgetDetails`. Ta kết nối effect này trong `MainNavigation.kt` để điều hướng tới route `budget_details`.

### 2.2. Luồng Dữ Liệu (Data Flow)
* **Happy path:** Bấm "Chi tiết ↗" ở Home $\rightarrow$ `NavigateToBudgetDetails` $\rightarrow$ Mở `BudgetRoute` $\rightarrow$ `BudgetViewModel` tải dữ liệu chu kỳ hiện tại $\rightarrow$ Render các section: App Bar $\rightarrow$ Cycle Button $\rightarrow$ Summary Card $\rightarrow$ Mascot Slot $\rightarrow$ Payment Breakdown $\rightarrow$ Notable Items $\rightarrow$ Recent Activity.
* **Over-budget path (C10):** Spent (5.5m) > Limit (5m) $\rightarrow$ Summary Card hiển thị nhãn "Vượt ngân sách 500.000đ", tỷ lệ 110%, thanh progress kẹo cap 100%, mascot hiển thị sắc thái bình tĩnh.
* **Offline path (C11):** Mất mạng $\rightarrow$ Banner cảnh báo hiện ở đầu màn hình, các nút sửa/thêm chuyển sang disabled kèm thông báo "Kết nối mạng để chỉnh sửa".

### 2.3. Interface Contracts (Mã Hợp Đồng Thực Tế)
```kotlin
package com.hoabh.chanko.presentation.screens.budget

import com.hoabh.chanko.domain.budget.model.*

data class BudgetUiState(
    val selectedCycle: BudgetCycle? = null,
    val availableCycles: List<BudgetCycle> = emptyList(),
    val snapshot: BudgetSnapshot? = null,
    val paymentMethods: List<PaymentMethodBreakdown> = emptyList(),
    val notableItems: List<NotableItem> = emptyList(),
    val recentActivities: List<BudgetActivity> = emptyList(),
    val isOnline: Boolean = true,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val lastSyncTimeText: String? = null,
    val currentSheet: BudgetSheetDestination? = null,
    val sheetBackStack: List<BudgetSheetDestination> = emptyList(),
    val isDiscardDialogOpen: Boolean = false
) {
    val isCurrentCycle: Boolean get() = selectedCycle?.isCurrent == true
    val canWrite: Boolean get() = isOnline && isCurrentCycle && !isLoading && !isSubmitting
    val isOverBudget: Boolean get() = snapshot?.isOverBudget == true
    val isZeroSpend: Boolean get() = snapshot?.spentMinor == 0L
}

sealed interface BudgetUiEvent {
    data object CyclePickerClicked : BudgetUiEvent
    data object WalletSettingsClicked : BudgetUiEvent
    data object EditBudgetClicked : BudgetUiEvent
    data class CycleSelected(val cycleId: String) : BudgetUiEvent
    data class NotableItemClicked(val itemId: String) : BudgetUiEvent
    data class ActivityItemClicked(val activity: BudgetActivity) : BudgetUiEvent
    data object DismissSheet : BudgetUiEvent
    data object BackPressedInSheet : BudgetUiEvent
    data object ConfirmDiscardChanges : BudgetUiEvent
    data object CancelDiscardChanges : BudgetUiEvent
    data object RetryClicked : BudgetUiEvent
}

sealed interface BudgetUiEffect {
    data class NavigateToSharedItemDetail(val itemId: String) : BudgetUiEffect
    data class ShowSnackbar(val message: String) : BudgetUiEffect
}
```

---

## 3. Capability: `budget-cycle-wallet` (C02 Chọn Chu Kỳ, C03 Thiết Lập Ví, C09 Lịch Sử)
* **Phụ thuộc:** `budget-domain-data`, `budget-overview-ui`.

### 3.1. Quyết Định Kiến Trúc (Architecture Decisions)
* **Quyết định: Single ModalBottomSheet Host với Internal Backstack**
  * *Lý do:* Tránh lỗi xung đột insets và window gesture khi mở modal lồng nhau trong Compose.
  * *Cách thực hiện:* `BudgetSheetHost` quan sát `currentSheet`. Khi người dùng đi sâu (C03 $\rightarrow$ C05 $\rightarrow$ C06), màn hình sheet cũ được đẩy vào `sheetBackStack`. Khi bấm Back hoặc nút quay lại, pop sheet trên cùng ra.

### 3.2. Luồng Dữ Liệu (Data Flow)
* **Happy path (C02 Chọn chu kỳ):** Bấm chọn chu kỳ $\rightarrow$ Mở `CyclePickerSheet` $\rightarrow$ Tap vào chu kỳ cũ $\rightarrow$ Đóng sheet ngay lập tức $\rightarrow$ C01 hiển thị dữ liệu lịch sử với badge "Đã kết thúc · Chỉ xem".
* **Happy path (C03 & C09):** Bấm "Thiết lập ví" $\rightarrow$ Mở C03 $\rightarrow$ Bấm "Lịch sử điều chỉnh" $\rightarrow$ Mở C09 $\rightarrow$ Hiển thị danh sách thay đổi hạn mức tăng/giảm $\rightarrow$ Bấm nút "Đóng" hoặc Back quay về C03 $\rightarrow$ Bấm đóng về C01.

### 3.3. Interface Contracts
```kotlin
sealed interface BudgetSheetDestination {
    data object CyclePicker : BudgetSheetDestination
    data object WalletSettings : BudgetSheetDestination
    data class EditBudget(val currentLimit: Long) : BudgetSheetDestination
    data object CreditCardManage : BudgetSheetDestination
    data object AddCreditCard : BudgetSheetDestination
    data class RenameCreditCard(val card: CreditCard) : BudgetSheetDestination
    data object AdjustmentHistory : BudgetSheetDestination
}
```

---

## 4. Capability: `budget-edit-preview` (C04 Sửa Ngân Sách & Live Preview)
* **Phụ thuộc:** `budget-domain-data`.

### 4.1. Quyết Định Kiến Trúc (Architecture Decisions)
* **Quyết định: Tính Toán Xem Trước Thời Gian Thực (Instant Keystroke Live Preview)**
  * *Lý do:* Người dùng cần thấy ngay số tiền còn lại và thanh tiến trình sẽ biến động ra sao *trước khi* nhấn Lưu.
  * *Validation:* Chặn số âm, thập phân, vượt $999.999.999.999đ$. Khi input lỗi, xóa preview số cũ và hiện hướng dẫn *"Nhập ngân sách hợp lệ để xem trước"*.
  * *Cảnh báo hạ hạn mức:* Nếu số mới < đã chi $\rightarrow$ Báo động kẹo vàng: *"Ngân sách mới thấp hơn số đã chi. Bạn sẽ vượt ngân sách X đ"* nhưng vẫn cho phép Lưu.

### 4.2. Luồng Dữ Liệu (Data Flow)
* **Happy path:** Mở C04 $\rightarrow$ Nhập số tiền `7.000.000` $\rightarrow$ Live preview cập nhật `Còn lại: 4.000.000đ, 42%` $\rightarrow$ Bấm "Lưu" $\rightarrow$ Lưu thành công $\rightarrow$ Đóng sheet $\rightarrow$ Snackbar "Đã cập nhật ngân sách".
* **Dirty discard path:** Đã sửa số tiền nhưng bấm ra vùng tối (Scrim) hoặc nút Back $\rightarrow$ Hiển thị `DiscardChangesDialog` ("Bỏ thay đổi?") $\rightarrow$ Bấm "Bỏ thay đổi" thì đóng form không lưu; bấm "Tiếp tục chỉnh sửa" thì giữ nguyên bản nháp.

---

## 5. Capability: `budget-card-management` (C05 Quản Lý, C06 Thêm, C07 Đổi Tên, C08 Xóa Thẻ)
* **Phụ thuộc:** `budget-domain-data`, `budget-cycle-wallet`.

### 5.1. Quyết Định Kiến Trúc (Architecture Decisions)
* **Quyết định: Quy Chuẩn Tên Thẻ 1–40 Ký Tự, NFC, Trim, Không Cascade Xóa Chi Tiêu**
  * *Lý do:* Thẻ tín dụng chỉ là metadata phân loại. Xóa thẻ là đánh dấu `isArchived = true`, giữ nguyên toàn bộ lịch sử chi tiêu cũ đã ghi bằng thẻ đó.
  * *Validation:* Chuỗi sau khi trim phải từ 1 đến 40 ký tự, chuẩn hóa Unicode NFC, so sánh trùng tên không phân biệt hoa thường (Case-insensitive) với các thẻ đang hoạt động.

### 5.2. Luồng Dữ Liệu (Data Flow)
* **Happy path (Thêm thẻ C06):** Tại C05 bấm "Thêm thẻ" $\rightarrow$ Mở C06 $\rightarrow$ Nhập "Thẻ tín dụng Chanko" (counter 20/40) $\rightarrow$ Bấm "Thêm" $\rightarrow$ Thành công $\rightarrow$ Quay về C05 có thẻ mới $\rightarrow$ Snackbar "Đã thêm thẻ".
* **Happy path (Xóa thẻ C08):** Tại C05 bấm icon xóa trên thẻ $\rightarrow$ Mở dialog C08 $\rightarrow$ Bấm "Xóa thẻ" $\rightarrow$ Đánh dấu archive $\rightarrow$ Thẻ biến mất khỏi danh sách $\rightarrow$ Snackbar "Đã xóa thẻ".

---

## 6. Ma Trận Rủi Ro Toàn Diện (Master Risk Register)

| Rủi ro | Khả năng | Mức độ | Biện pháp giảm thiểu |
| :--- | :--- | :--- | :--- |
| Tràn chữ hoặc che nút CTA khi bật font 200% | Trung bình | Cao | Toàn bộ sheet sử dụng `LazyColumn` hoặc `verticalScroll` kết hợp `WindowInsets.ime` và `WindowInsets.navigationBars`. |
| Trùng lặp sự kiện click đúp (Double-tap dispatch) | Cao | Trung bình | Biến cờ `isSubmitting` khóa toàn bộ tương tác ngay ở lần tap đầu tiên. |
| Người dùng mất bản nháp vô tình | Trung bình | Trung bình | Cơ chế phát hiện form dirty kích hoạt `DiscardChangesDialog` trước khi đóng sheet. |
| Vi phạm quy tắc máy ảo / emulator | Không có | Nghiêm trọng | Tuân thủ tuyệt đối `AGENTS.md` — chỉ kiểm thử qua build terminal và unit tests. |
