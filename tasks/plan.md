# Implementation Plan: Chanko Profile & Settings (`docs/features/profile-and-settings.md`)

## Overview
Triển khai màn hình **Profile & Settings** và màn hình con **Cat Personality** bám sát tài liệu bàn giao `chanko-profile-handoff` v1.0 và đặc tả đã duyệt tại `docs/features/profile-and-settings.md`. Toàn bộ giao diện sử dụng Jetpack Compose, Material 3, font Nunito, hệ màu Candy Pop 3D, 100% copy Tiếng Việt, hỗ trợ đầy đủ các tiêu chuẩn trợ năng (TalkBack, Font Scale 200%, Touch Target >= 48dp, Reduce Motion).

## Architecture Decisions
1. **Single Entry Point & Child Destination (PROFILE-01, PROFILE-02, PROFILE-03):**
   - Nút Avatar tròn tím trên `HomeHeader` (`onProfileClick`) là cổng duy nhất dẫn vào Profile.
   - Profile & Settings là một child screen, không hiển thị Bottom Navigation Bar.
   - Nút Back (TopBar và System Gesture) đưa người dùng quay lại Home, giữ nguyên ngữ cảnh tab và vị trí cuộn.
2. **Cozy Corner (Linh thú Chanko) (PROFILE-04):**
   - Card minh họa scene chú mèo trắng ngồi ôm cốc tím tại bàn trà (`cozy_corner_candidate_v1.png`).
   - Tương tác tap kích hoạt chuyển động co dãn nhẹ nhàng toàn khung cảnh (`whole-scene scale 1.00 -> 1.025 -> 1.00`), không gọi AI/LLM, không pet game, tắt chuyển động phóng to khi Reduce Motion bật.
3. **Cat Personality Sub-screen & State Machine (PROFILE-05, PROFILE-06):**
   - Màn hình riêng `PersonalityScreen` cho phép chọn giữa 3 tính cách: *Thân thiện (Friendly)*, *Lém lỉnh (Sassy - Mặc định)*, *Thẳng thắn (Savage)*.
   - Chọn tính cách cập nhật ngay khung thoại mẫu xem trước (Preview) hoàn toàn offline.
   - Nút "Lưu thay đổi" (Save) chỉ kích hoạt khi có thay đổi (`isDirty`) và có kết nối mạng (`isOnline`).
   - Khi có thay đổi chưa lưu mà bấm Back -> Hiển thị `UnsavedChangesDialog` ("Hủy thay đổi?" / "Tiếp tục chỉnh sửa" / "Hủy bỏ").
4. **Ready for Review Notification Preference (PROFILE-07):**
   - Switch cấu hình thông báo nhắc nhở đợt duyệt đồ hạ nhiệt.
   - Phối hợp giữa preference của ứng dụng và quyền thông báo hệ thống Android (`POST_NOTIFICATIONS`).
5. **Google Account & Safety Policy (PROFILE-08, PROFILE-10):**
   - Card Google Account chỉ đọc (Read-only: Avatar, Tên hiển thị, Email).
   - Dialog xác nhận Đăng xuất (Sign Out), thu hồi token/phiên, dọn dẹp state và điều hướng sạch sẽ về `Screen.Login` xóa trắng back stack.

---

## Dependency Graph

```text
Phase 1: Foundation (Tokens, Assets & Strings)
   │
   ▼
Phase 2: Navigation & State Contracts (Screen.kt, NavGraph.kt, UiStates)
   │
   ├──► Phase 3: Profile & Settings UI Components (Header, Cozy Corner, Setting Rows, Google Card, Sign Out)
   │
   ├──► Phase 4: Cat Personality Sub-Screen (PersonalityOption, Preview, UnsavedChangesDialog)
   │
   └──► Phase 5: ViewModels & Business Logic (ProfileViewModel, PersonalityViewModel, Offline/Online, Sign Out)
           │
           ▼
   Phase 6: Integration, Accessibility (Font 200%, TalkBack, Insets) & Unit Tests
```

---

## Task List

### Phase 1: Foundation (Assets, Strings & Tokens)
- [ ] **Task 1: Cài đặt Resource Strings Tiếng Việt & Import Cozy Corner Asset**
  - **Description:** Thêm toàn bộ các chuỗi tiếng Việt chuẩn mực cho Profile, Personality, Notification, Dialogs vào `res/values/strings_profile.xml`. Import file ảnh `cozy_corner_candidate_v1.png` từ handoff vào `res/drawable-nodpi/chanko_profile_cozy_corner.png`.
  - **Acceptance criteria:**
    - [ ] `strings_profile.xml` chứa 100% tiếng Việt theo đặc tả (tên màn, mô tả 3 tính cách, preview copy tiếng Việt, dialog xác nhận hủy/đăng xuất).
    - [ ] `chanko_profile_cozy_corner.png` sẵn sàng trong `drawable-nodpi`.
  - **Verification:**
    - [ ] `./gradlew assembleDevDebug` biên dịch thành công.
  - **Files likely touched:**
    - `app/src/main/res/values/strings_profile.xml`
    - `app/src/main/res/drawable-nodpi/chanko_profile_cozy_corner.png`

---

### Phase 2: Navigation & State Contracts
- [ ] **Task 2: Khai báo Routes và State/Event Models**
  - **Description:** Cập nhật `Screen.kt` với `Profile` và `Personality`. Định nghĩa immutable `ProfileUiState`, `ProfileUiEvent`, `ProfileUiEffect`, và `PersonalityUiState`, `PersonalityUiEvent`, `PersonalityUiEffect`.
  - **Acceptance criteria:**
    - [ ] Routes `Screen.Profile` (`"profile"`) và `Screen.Personality` (`"personality"`) được định nghĩa chuẩn.
    - [ ] Các data classes biểu diễn đầy đủ trạng thái UI: `accountInfo`, `committedPersonality`, `notificationEnabled`, `isOnline`, `isLoading`, `isDirty`, `draftPersonality`.
  - **Verification:**
    - [ ] `./gradlew assembleDevDebug` biên dịch thành công.
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/navigation/Screen.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/ProfileUiState.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/personality/PersonalityUiState.kt`

---

### Phase 3: Profile & Settings UI Components
- [ ] **Task 3: Xây dựng các Composable cho màn hình chính Profile & Settings**
  - **Description:** Tạo các component giao diện theo Candy Pop 3D: `ProfileTopBar`, `AccountIdentityCard`, `CozyCornerCard` (kèm hiệu ứng co dãn 1.0 -> 1.025 -> 1.0 khi tap), `PersonalitySettingRow`, `ReviewNotificationRow`, `GoogleAccountCard`, `LegalAndAboutSection`, `SignOutAction`.
  - **Acceptance criteria:**
    - [ ] `CozyCornerCard` có animation scale mượt mà khi chạm, hỗ trợ `reduceMotion` (không scale nếu bật).
    - [ ] Mọi item tương tác đều có vùng chạm >= 48dp, có semantics/contentDescription đầy đủ.
    - [ ] `ProfileScreen` hiển thị cuộn mượt mà với nền cream `#FAF8F5`, hỗ trợ font scale 200%.
  - **Verification:**
    - [ ] `@Preview` hiển thị đúng trên Android Studio và biên dịch `./gradlew assembleDevDebug`.
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/components/CozyCornerCard.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/components/ProfileComponents.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/ProfileScreen.kt`

---

### Phase 4: Cat Personality Sub-Screen Components
- [ ] **Task 4: Xây dựng Composable cho màn hình chọn Tính cách linh thú (`PersonalityScreen`)**
  - **Description:** Tạo `PersonalityScreen` gồm TopBar có nút Back, danh sách 3 thẻ chọn Radio (`PersonalityOptionCard`: Thân thiện, Lém lỉnh, Thẳng thắn), khung xem trước câu thoại tĩnh (`PersonalityPreviewCard`), nút "Lưu thay đổi" (Save) ở cuối màn hình, và `UnsavedChangesDialog`.
  - **Acceptance criteria:**
    - [ ] Chọn radio đổi ngay câu thoại preview offline tĩnh tương ứng.
    - [ ] Nút Save bị disabled khi chưa thay đổi (`!isDirty`) hoặc khi mất mạng (`!isOnline`).
    - [ ] Bấm Back khi dirty mở `UnsavedChangesDialog` ("Tiếp tục chỉnh sửa" / "Hủy bỏ").
  - **Verification:**
    - [ ] `@Preview` hiển thị đủ các trạng thái và biên dịch `./gradlew assembleDevDebug`.
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/personality/PersonalityScreen.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/personality/components/PersonalityComponents.kt`

---

### Phase 5: ViewModels, Data Flow & Navigation Integration
- [ ] **Task 5: Triển khai `ProfileViewModel` và `PersonalityViewModel`**
  - **Description:** Viết logic ViewModel quản lý state, kết nối với dữ liệu tài khoản hiện hành (Google Auth), xử lý toggle notification preference, cập nhật personality draft vs committed, xử lý timeout/error/retry, và flow Sign Out thu hồi phiên an toàn.
  - **Acceptance criteria:**
    - [ ] `ProfileViewModel` phát đúng effect mở Personality, mở URL điều khoản, và điều hướng về Login khi Đăng xuất.
    - [ ] `PersonalityViewModel` quản lý chuẩn state machine: dirty check, online/offline guard, lưu thành công cập nhật committed và pop back.
  - **Verification:**
    - [ ] Unit tests pass: `./gradlew :app:testDevDebugUnitTest`
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/ProfileViewModel.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/personality/PersonalityViewModel.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/navigation/NavGraph.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt` (gắn callback `onProfileClick`)

---

### Phase 6: Unit Test Suite & QA Checkpoint
- [ ] **Task 6: Bộ Unit Test toàn diện (AC-01 đến AC-20) & Build Verification**
  - **Description:** Viết unit tests kiểm thử chặt chẽ các hành vi cốt lõi: Navigation, Dirty Guard, Save Idempotency, Offline guard, Sign Out flow, và kiểm tra Accessibility (Font 200%, System Insets).
  - **Acceptance criteria:**
    - [ ] Toàn bộ unit tests cho `ProfileViewModel` và `PersonalityViewModel` pass 100%.
    - [ ] `./gradlew assembleDevDebug :app:testDevDebugUnitTest` pass toàn bộ.
  - **Verification:**
    - [ ] Terminal check: `./gradlew assembleDevDebug :app:testDevDebugUnitTest`
  - **Files likely touched:**
    - `app/src/test/java/com/hoabh/chanko/presentation/screens/profile/ProfileViewModelTest.kt`
    - `app/src/test/java/com/hoabh/chanko/presentation/screens/profile/PersonalityViewModelTest.kt`

---

## Checkpoint: Final Verification
- [ ] Build dự án hoàn toàn sạch: `./gradlew assembleDevDebug`
- [ ] Toàn bộ unit tests pass: `./gradlew testDevDebugUnitTest`
- [ ] Sẵn sàng để nghiệm thu thực tế theo đúng yêu cầu handoff.
