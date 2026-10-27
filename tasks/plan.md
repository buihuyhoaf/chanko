# Implementation Plan: Chanko All 74 Icons System Migration

## Overview
Di dời toàn bộ hệ thống icon của ứng dụng Chanko từ thư viện Google Material Icons (`androidx.compose.material.icons.extended`) sang bộ thiết kế độc quyền **Chanko All 74 Icons**:
- **66 Icon 2D Candy Duotone v2.0-r1**: 198 Android Vector Drawables (3 biến thể `default`, `selected`, `disabled` cho mỗi icon) phủ 100% các icon chức năng trong ứng dụng.
- **8 Icon 3D Signature (C01 - C08)**: 8 file WebP lossless 512×512 đặt tại `res/drawable-nodpi/` cho các điểm chạm trải nghiệm cảm xúc.
- **Design Token Component**: Đóng gói qua `com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon`.
- **Gỡ bỏ Dependency**: Loại bỏ hoàn toàn `material-icons-extended` khỏi `build.gradle.kts`.

---

## Architectural Decisions
1. **Resource Segregation**:
   - 198 file vector XML 2D chuyển vào `app/src/main/res/drawable/`.
   - Bảng màu tham khảo `chanko_icon_colors.xml` chuyển vào `app/src/main/res/values/`.
   - 8 file WebP 3D chuyển vào `app/src/main/res/drawable-nodpi/` để triệt tiêu nguy cơ Out-Of-Memory do tự động scale mật độ màn hình trên Android.
2. **Component Wrapper Pattern**:
   - `ChankoIconId`: Enum định danh 66 ID ánh xạ chính xác đến 3 resource drawable (`normal`, `selected`, `disabled`).
   - `ChankoIcon`: Composable chuẩn hóa, tự động chọn resource theo trạng thái `enabled` / `selected`, giữ nguyên kích thước mặc định 24dp và `tint = Color.Unspecified` để bảo toàn màu sắc Duotone.
   - `ChankoSignatureImage`: Composable hiển thị 8 artwork 3D Signature với tỉ lệ fit gốc.
3. **Accessibility First (A11y)**:
   - Các icon có text nhãn đi kèm: `contentDescription = null` để chống đọc trùng lặp trên TalkBack.
   - Các icon dạng button hành động độc lập: `contentDescription` bắt buộc lấy từ chuỗi Tiếng Việt trong `strings.xml`.
   - Kích thước touch target tối thiểu của button bọc ngoài: `48.dp × 48.dp`.

---

## Phased Implementation Roadmap

### Phase 1: Foundation (Assets & Design System Tokens)
- Copy 198 vector drawables và file màu vào `res/drawable/` và `res/values/`.
- Copy 8 file WebP 3D vào `res/drawable-nodpi/`.
- Xây dựng `ChankoIconId.kt`, `ChankoIcon.kt`, và `ChankoSignatureImage.kt` trong package `com.hoabh.chanko.presentation.designsystem.icon`.
- **Checkpoint 1**: Biên dịch `./gradlew assembleDevDebug` thành công với các resource mới.

### Phase 2: Navigation & Core Shell Migration
- Chuyển đổi toàn bộ `MainNavigation.kt`: 4 tabs BottomBar (`NAV_01` .. `NAV_04`) và nút thêm trung tâm (`ACT_01`).
- **Checkpoint 2**: Kiểm tra navigation bar hiển thị icon Candy Duotone đúng trạng thái `selected` / `default`.

### Phase 3: Screen Slices Migration
- **Slice A (Home & Dashboard)**: `HomeScreen.kt`, `QuickActionsRow.kt`, `ObservationCard.kt`, `YourDecisionsSection.kt`, `DraftResumeCard.kt`, `HomeSheetHost.kt`. Tích hợp 3D Signature C01..C04 vào các card.
- **Slice B (Item Detail & Actions)**: `ItemDetailScreen.kt`, `StickyBottomActionBar.kt`, `ItemMoreOptionsBottomSheet.kt`, `CoolingOffCountdownCard.kt`, `ExpandableDetailsSection.kt`, `DecisionJourneySection.kt`.
- **Slice C (Add Item, Decision, Vault & Profile)**: `AddItemScreen.kt`, `DecisionScreen.kt`, `VaultScreen.kt`, `TransactionDetailScreen.kt`, `ProfileScreen.kt`, `ProfileComponents.kt`, `ArchivedItemsScreen.kt`, `ItemUi.kt`.
- **Checkpoint 3**: Mọi màn hình UI biên dịch sạch sẽ không còn dùng `Icons.Rounded.*`.

### Phase 4: Dependency Pruning & Verification
- Xóa bỏ `implementation(libs.androidx.compose.material.icons.extended)` khỏi `app/build.gradle.kts`.
- Dọn dẹp toàn bộ unused imports `androidx.compose.material.icons.*`.
- **Checkpoint 4**: `./gradlew assembleDevDebug` và `./gradlew testDevDebugUnitTest` PASS 100%.
