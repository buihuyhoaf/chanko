# Task List: Chanko All 74 Icons System Migration

## Phase 1: Foundation (Assets & Design System Tokens)

- [x] **Task 1: Import 2D Vector Drawables & 3D WebP Assets**
  - **Acceptance**:
    - 198 file XML vector của 66 icon 2D có mặt tại `app/src/main/res/drawable/`.
    - `chanko_icon_colors.xml` có mặt tại `app/src/main/res/values/`.
    - 8 file 3D WebP (`chanko_c01_quick_check.webp` .. `chanko_c08_chanko_chat.webp`) có mặt tại `app/src/main/res/drawable-nodpi/`.
  - **Verify**: `./gradlew assembleDevDebug` (Resource compilation test).
  - **Files**:
    - `app/src/main/res/drawable/chanko_*.xml`
    - `app/src/main/res/values/chanko_icon_colors.xml`
    - `app/src/main/res/drawable-nodpi/chanko_c0*.webp`

- [x] **Task 2: Implement Design System Icon Tokens & Wrappers**
  - **Acceptance**:
    - Tạo `com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId` (enum 66 IDs ánh xạ chính xác đến 3 drawable variant `normal`, `selected`, `disabled`).
    - Tạo `com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon` (Composable bọc Icon 24dp, `tint = Color.Unspecified`).
    - Tạo `com.hoabh.chanko.presentation.designsystem.icon.ChankoSignatureImage` (Composable bọc Image cho 8 artwork 3D C01..C08).
  - **Verify**: `./gradlew assembleDevDebug` biên dịch thành công package mới.
  - **Files**:
    - `app/src/main/java/com/hoabh/chanko/presentation/designsystem/icon/ChankoIconId.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/designsystem/icon/ChankoIcon.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/designsystem/icon/ChankoSignatureImage.kt`

- [x] **Checkpoint 1: Foundation Verify**
  - [x] `./gradlew assembleDevDebug` PASS
  - [x] R-class nhận diện đầy đủ 198 vector và 8 WebP assets

---

## Phase 2: Navigation & Core Shell Migration

- [x] **Task 3: Migrate Bottom Navigation & Global Actions**
  - **Acceptance**:
    - `MainNavigation.kt`: Chuyển 4 tabs (`Home`, `My Items`, `Budget`, `Insights`) sang dùng `ChankoIcon(id = ChankoIconId.NAV_01 .. NAV_04, selected = ...)`.
    - Nút Floating Action (+) trung tâm dùng `ChankoIconId.ACT_01`.
  - **Verify**: `./gradlew assembleDevDebug`.
  - **Files**:
    - `app/src/main/java/com/hoabh/chanko/presentation/navigation/MainNavigation.kt`

- [x] **Checkpoint 2: Shell Verify**
  - [x] BottomBar dùng icon Candy Duotone chuẩn xác, trạng thái selected/unselected hoạt động mượt mà.

---

## Phase 3: Screen Slices Migration

- [x] **Task 4: Migrate Home Screen Components & 3D Signature Integration**
  - **Acceptance**:
    - `HomeScreen.kt`, `QuickActionsRow.kt`, `ObservationCard.kt`, `YourDecisionsSection.kt`, `DraftResumeCard.kt`, `HomeSheetHost.kt` được thay thế toàn bộ Material Icons bằng `ChankoIconId`.
    - Tích hợp 3D Signature C01 (Quick Check), C02 (Cooling-off), C04 (Pause Jar) vào các card tương ứng trên Home.
  - **Verify**: `./gradlew assembleDevDebug`.
  - **Files**:
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/QuickActionsRow.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/ObservationCard.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/YourDecisionsSection.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/DraftResumeCard.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/HomeSheetHost.kt`

- [x] **Task 5: Migrate Item Detail & Action Bar Components**
  - **Acceptance**:
    - `ItemDetailScreen.kt`, `StickyBottomActionBar.kt`, `ItemMoreOptionsBottomSheet.kt`, `CoolingOffCountdownCard.kt`, `ExpandableDetailsSection.kt`, `DecisionJourneySection.kt` được di dời sang `ChankoIconId` (Back -> `ACT_02`, More -> `ACT_10`, Archive -> `ACT_06` / `ITM_06`, v.v.).
  - **Verify**: `./gradlew assembleDevDebug`.
  - **Files**:
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/itemdetail/ItemDetailScreen.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/itemdetail/components/*.kt`

- [x] **Task 6: Migrate Add Item, Decision, Vault, Profile & Shared UI**
  - **Acceptance**:
    - `AddItemScreen.kt`, `DecisionScreen.kt` (Verdict BUY/WAIT/SKIP dùng `ITM_09`, `ITM_10`, `ITM_11`).
    - `VaultScreen.kt`, `TransactionDetailScreen.kt` (Ví ngân sách, xóa, sửa, lọc).
    - `ProfileScreen.kt`, `ProfileComponents.kt` (Bảo mật, thông tin, cài đặt).
    - `ArchivedItemsScreen.kt`, `ItemUi.kt`.
  - **Verify**: `./gradlew assembleDevDebug`.
  - **Files**:
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/additem/AddItemScreen.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/decision/DecisionScreen.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/vault/VaultScreen.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/transaction/TransactionDetailScreen.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/ProfileScreen.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/components/ProfileComponents.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/archived/ArchivedItemsScreen.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/components/ItemUi.kt`

- [x] **Checkpoint 3: All Screens Converted**
  - [x] Không còn bất kỳ màn hình nào import `androidx.compose.material.icons.*`.

---

## Phase 4: Dependency Pruning & Final Clean Verification

- [x] **Task 7: Remove `material-icons-extended` Dependency & Clean Codebase**
  - **Acceptance**:
    - Xoá dòng `implementation(libs.androidx.compose.material.icons.extended)` khỏi `app/build.gradle.kts`.
    - Dọn dẹp tất cả các import thừa liên quan đến Material Icons.
  - **Verify**:
    - `./gradlew assembleDevDebug` thành công rực rỡ mà không có cảnh báo thiếu icon.
    - `./gradlew testDevDebugUnitTest` PASS 100%.
  - **Files**:
    - `app/build.gradle.kts`

- [x] **Checkpoint 4: Final Sign-off**
  - [x] Toàn bộ test và build hoàn tất sạch sẽ.
