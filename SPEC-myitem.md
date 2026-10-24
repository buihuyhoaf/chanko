# Spec: Màn hình Món đồ & Chi tiết Món đồ (My Items & Item Detail)

## Objective
Xây dựng phân hệ quản lý danh sách món đồ cá nhân (**My Items**) và chi tiết quyết định mua sắm (**Item Detail**), hỗ trợ quy trình trì hoãn mua sắm bốc đồng (Cooling Off period), ghi nhận giao dịch mua thủ công (Record Purchase), chỉnh sửa/xóa giao dịch nhập sai (Transaction Detail / Correction) và quản lý lưu trữ (Archived Items).

Mục tiêu chính:
- Thể hiện sự đồng hành nhẹ nhàng, không phán xét, phản ánh đúng triết lý của Chanko.
- Đáp ứng đầy đủ **29 quyết định UX Approved** từ Recovery Baseline ngày 24/10/2026.
- Tuân thủ thiết kế Candy Pop 3D, Material Design 3, thiết kế sạch cho tablet/điện thoại, hỗ trợ tiếp cận (a11y) và font chữ lớn (200%).

---

## Tech Stack
- **Ngôn ngữ**: Kotlin (2.x / JVM 17)
- **UI Toolkit**: Jetpack Compose + Material 3 (Candy Design Tokens)
- **Kiến trúc**: MVI / MVVM với Unidirectional Data Flow (StateFlow, SharedFlow/Channel Effect)
- **DI**: Dagger Hilt
- **Local Persistence**: Room Database (Entity, DAO, TypeConverters)
- **Async & Reactive**: Kotlin Coroutines + Flow

---

## Phạm vi màn hình & Sheets (Scope)

Phân hệ gồm **4 Screens** và **3 Modal Bottom Sheets**:
1. **Screens**:
   - `MyItemsScreen`: Màn hình danh sách chính (thuộc Tab thứ 2 của `MainNavigation`).
   - `ItemDetailScreen`: Chi tiết món đồ theo itemId (Product Stage dummy placeholder, trạng thái, countdown, speech bubble, Decision Journey, Purchase History, sticky CTA).
   - `TransactionDetailScreen`: Chi tiết 1 lần mua hàng theo purchaseId (xem, sửa thông tin nhập sai hoặc xóa giao dịch nhập sai).
   - `ArchivedItemsScreen`: Danh sách các món đồ đã lưu trữ, hỗ trợ tìm kiếm và khôi phục (Restore).
2. **Modal Bottom Sheets**:
   - `FilterSortBottomSheet`: Lọc 2 cấp (Cấp 1: Tất cả/Đang hoạt động/Đã mua/Đã bỏ qua; Cấp 2 trong Đang hoạt động: Đã lưu/Đang cân nhắc/Đang hạ nhiệt/Đến lúc xem lại/Đã hoãn) và Sắp xếp.
   - `ItemMoreOptionsBottomSheet`: Menu ngữ cảnh (Chỉnh sửa món, Ghi nhận mua hàng / Mua lại, Bỏ qua chờ đợi, Hoãn/Thêm thời gian, Lưu trữ).
   - `RecordPurchaseBottomSheet`: Ghi nhận giao dịch mua thủ công cho món đồ (Đơn giá thực tế, số lượng, phương thức thanh toán, ngày mua, ghi chú).

---

## Commands
- Build Project:
  ```bash
  ./gradlew assembleDebug
  ```
- Run Unit Tests:
  ```bash
  ./gradlew testDebugUnitTest
  ```
- Verification Check:
  ```bash
  ./gradlew test
  ```

---

## Project Structure
```
app/src/main/
├── java/com/hoabh/chanko/
│   ├── presentation/
│   │   └── screens/
│   │       ├── myitems/
│   │       │   ├── MyItemsScreen.kt
│   │       │   ├── MyItemsViewModel.kt
│   │       │   ├── MyItemsUiState.kt
│   │       │   ├── MyItemsUiEffect.kt
│   │       │   └── components/
│   │       │       ├── MyItemsHero.kt
│   │       │       ├── MyItemsOverviewTiles.kt
│   │       │       ├── MyItemsCard.kt
│   │       │       ├── MyItemsEmptyState.kt
│   │       │       └── FilterSortBottomSheet.kt
│   │       ├── itemdetail/
│   │       │   ├── ItemDetailScreen.kt
│   │       │   ├── ItemDetailViewModel.kt
│   │       │   ├── ItemDetailUiState.kt
│   │       │   ├── ItemDetailUiEffect.kt
│   │       │   └── components/
│   │       │       ├── ProductStageHero.kt
│   │       │       ├── ItemDetailHeader.kt
│   │       │       ├── CoolingOffPanel.kt
│   │       │       ├── SpeechBubbleSection.kt
│   │       │       ├── DecisionJourneySection.kt
│   │       │       ├── PurchaseHistorySection.kt
│   │       │       ├── StickyBottomBar.kt
│   │       │       └── ItemMoreOptionsBottomSheet.kt
│   │       ├── transaction/
│   │       │   ├── TransactionDetailScreen.kt
│   │       │   ├── TransactionDetailViewModel.kt
│   │       │   └── components/
│   │       │       ├── TransactionInfoCard.kt
│   │       │       ├── EditTransactionForm.kt
│   │       │       └── DeleteCorrectionDialog.kt
│   │       ├── archived/
│   │       │   ├── ArchivedItemsScreen.kt
│   │       │   └── ArchivedItemsViewModel.kt
│   │       └── purchase/
│   │           └── RecordPurchaseBottomSheet.kt
│   └── di/
│       └── ...
core/
├── domain/
│   └── model/
│       ├── ItemStatus.kt
│       ├── CooldownItem.kt
│       ├── PurchaseTransaction.kt
│       └── DecisionEvent.kt
└── database/
    └── src/main/java/com/hoabh/chanko/data/local/
        ├── entity/
        │   ├── CooldownItemEntity.kt
        │   └── PurchaseTransactionEntity.kt
        └── dao/
            ├── ItemDao.kt
            └── PurchaseDao.kt
```

---

## Code Style & Conventions
- Tuân thủ quy chuẩn **[CODING_STANDARDS.md](file:///Users/buihoa/AndroidStudioProjects/chanko/CODING_STANDARDS.md)**:
  - Tất cả chuỗi hiển thị UI bằng **tiếng Việt**, khai báo đầy đủ trong `strings.xml`.
  - State hoisting: Stateless composable nhận `uiState` và `onEvent`/`callbacks`.
  - Không truyền `NavController` sâu vào composable con; chỉ truyền callbacks.
  - Sử dụng Token màu sắc, Spacing, Typography chuẩn (`CandyColors`, `CandySpace`, `MaterialTheme.typography`).
  - Xử lý insets (`WindowInsets.navigationBars`, `WindowInsets.statusBars`, `imePadding`) an toàn.
  - Thẻ `Image` dùng `ic_product_placeholder.xml` làm fallback neutral; không stretch ảnh.

```kotlin
@Composable
fun MyItemsRoute(
    viewModel: MyItemsViewModel = hiltViewModel(),
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToArchived: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MyItemsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToArchived = onNavigateToArchived
    )
}
```

---

## Testing Strategy
- **Framework**: JUnit 4 / JUnit 5 + MockK / Turbine + Coroutines Test.
- **Vị trí**: `app/src/test/java/...` và `core/database/src/test/java/...`
- **Mức độ kiểm thử**:
  - `ViewModel Tests`: Đảm bảo state flow, chuyển đổi filter 2 cấp, tìm kiếm, tính toán thời gian cooldown còn lại, mapping derived `Ready`/`Postponed`.
  - `DAO & Room Migration Tests`: Truy vấn lọc status, transaction count/total, archive toggle.
  - `Domain Model Logic Tests`: Kiểm tra tính hợp lệ của đơn giá thực tế, số lượng, remaining millis, canDecide.

---

## Boundaries
- **Always do**:
  - Giữ nguyên 5 `ItemStatus` (WISHLIST, CONSIDERING, WAITING, PURCHASED, SKIPPED).
  - Coi `Ready for Review` và `Postponed` là trạng thái hiển thị dẫn xuất (derived UI state), không thêm enum mới vào backend/database.
  - Sử dụng placeholder trung tính dummy từ `chanko_myitem/assets/` cho hero/thumbnail; không thêm ảnh sản phẩm mẫu hay ảnh mèo bên cạnh/đè lên ảnh sản phẩm.
  - Nếu thiếu asset mèo canonical, ẩn slot hình mèo và giữ nguyên khung thoại (speech text).
  - Đảm bảo insets và font scaling lên tới 200% không bị che khuất nút hay vỡ layout.
  - Giữ 4 tabs chính (`Home`, `MyItems`, `Budget`, `Insights`) và nút `+` toàn cục trên Bottom Bar; không thêm FAB trùng lặp ở `MyItems`.
- **Ask first**:
  - Thay đổi schema Room Database nếu ảnh hưởng đến phiên bản migration trước đó.
  - Bổ sung thư viện bên ngoài mới vào `gradle/libs.versions.toml`.
- **Never do**:
  - Không cài đặt hay chạy emulator / máy ảo / adb install theo quy định nghiêm ngặt trong `AGENTS.md`.
  - Không tự gán `PURCHASED` mà không có bản ghi giao dịch mua (`PurchaseTransaction`).
  - Không cascade xóa transactions khi xóa hoặc lưu trữ item.
  - Không tạo hoạt ảnh ăn mừng bốc đồng (confetti/cheer) khi người dùng ghi nhận mua hàng.

---

## Success Criteria (Acceptance Criteria)
1. **Khớp 29 Quyết định UX Approved** (`ITEM-01..07`, `DETAIL-01..14`, `EDGE-01/03/04/07`, `GAP-01..04`):
   - Màn `MyItems` hiển thị danh sách dạng card ngang, thumbnail bên trái, Compact 3D Chanko Hero, 3 overview tiles (All, Cooling Off, Ready) hỗ trợ chạm để lọc, và bộ lọc 2 cấp hoạt động mượt mà.
   - Thao tác chạm vào card chuyển hướng mượt mà sang `ItemDetail`.
   - `ItemDetail` hiển thị Product Stage dummy placeholder, countdown nổi bật, Speech bubble riêng biệt, Decision Journey và Purchase History dạng collapsible/section riêng.
   - `TransactionDetail` cho phép xem và sửa/xóa giao dịch nhập sai với cảnh báo tính toán lại ngân sách chu kỳ.
   - `ArchivedItems` quản lý các món đồ đã lưu trữ và khôi phục độc lập.
2. **Assets & Visual**:
   - `ic_product_placeholder.xml` được tích hợp vào `res/drawable/` của module app làm placeholder chuẩn.
   - Slot Chanko Mascot xử lý fallback an toàn (ẩn hình, giữ lời thoại) đúng theo `ChankoMascot`.
3. **Build & Quality Gates**:
   - Biên dịch thành công với `./gradlew assembleDebug`.
   - Toàn bộ unit tests mới và cũ pass 100% với `./gradlew test`.
