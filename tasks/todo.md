# Tasks: Chanko Profile & Settings (`docs/features/profile-and-settings.md`)

Kế hoạch chi tiết: [`tasks/plan.md`](file:///Users/buihoa/AndroidStudioProjects/chanko/tasks/plan.md) | Đặc tả: [`docs/features/profile-and-settings.md`](file:///Users/buihoa/AndroidStudioProjects/chanko/docs/features/profile-and-settings.md)

---

## Phase 1: Foundation (Assets, Strings & Tokens)
- [x] **Task 1: Cài đặt Resource Strings Tiếng Việt & Import Cozy Corner Asset**
  - **Acceptance:**
    - [x] `strings_profile.xml` chứa đầy đủ copy tiếng Việt: Profile & Settings, Cozy Corner, 3 tính cách (Thân thiện, Lém lỉnh, Thẳng thắn) kèm preview, switch thông báo, dialog xác nhận hủy/đăng xuất.
    - [x] Import `cozy_corner_candidate_v1.png` vào `res/drawable-nodpi/chanko_profile_cozy_corner.png`.
  - **Verify:**
    - [x] `./gradlew assembleDevDebug` thành công.
  - **Files:**
    - `app/src/main/res/values/strings_profile.xml`
    - `app/src/main/res/drawable-nodpi/chanko_profile_cozy_corner.png`

---

## Phase 2: Navigation & State Contracts
- [x] **Task 2: Khai báo Routes và State/Event Models**
  - **Acceptance:**
    - [x] Khai báo `Screen.Profile` và `Screen.Personality` trong `Screen.kt`.
    - [x] Định nghĩa `ProfileUiState`, `ProfileUiEvent`, `ProfileUiEffect`.
    - [x] Định nghĩa `PersonalityUiState`, `PersonalityUiEvent`, `PersonalityUiEffect`.
  - **Verify:**
    - [x] `./gradlew assembleDevDebug` thành công.
  - **Files:**
    - `app/src/main/java/com/hoabh/chanko/presentation/navigation/Screen.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/ProfileUiState.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/personality/PersonalityUiState.kt`

---

## Phase 3: Profile & Settings UI Components
- [x] **Task 3: Xây dựng các Composable cho màn hình chính Profile & Settings**
  - **Acceptance:**
    - [x] `CozyCornerCard` có hiệu ứng scale chạm 1.0 -> 1.025 -> 1.0, hỗ trợ Reduce Motion (pressed feedback nhẹ, không scale).
    - [x] Đầy đủ các phần: TopBar, AccountIdentity, Setting rows (Personality, Ready for Review switch, Google Account read-only, Điều khoản & Về ứng dụng, Nút Đăng xuất).
    - [x] Mọi control đạt chuẩn touch target >= 48dp, có semantics/contentDescription, cuộn mượt ở font 200%.
  - **Verify:**
    - [x] `./gradlew assembleDevDebug` thành công.
  - **Files:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/components/CozyCornerCard.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/components/ProfileComponents.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/ProfileScreen.kt`

---

## Phase 4: Cat Personality Sub-Screen Components
- [ ] **Task 4: Xây dựng Composable cho màn hình chọn Tính cách linh thú (`PersonalityScreen`)**
  - **Acceptance:**
    - [ ] 3 lựa chọn Radio: Thân thiện, Lém lỉnh, Thẳng thắn.
    - [ ] Chọn radio đổi tức thì câu thoại xem trước (PreviewCard) offline.
    - [ ] Nút Save chỉ bấm được khi có thay đổi (`isDirty`) và online.
    - [ ] Bấm Back khi dirty hiển thị `UnsavedChangesDialog` ("Tiếp tục chỉnh sửa" / "Hủy bỏ").
  - **Verify:**
    - [ ] `./gradlew assembleDevDebug` thành công.
  - **Files:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/personality/PersonalityScreen.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/personality/components/PersonalityComponents.kt`

---

## Phase 5: ViewModels, Data Flow & Navigation Integration
- [ ] **Task 5: Triển khai `ProfileViewModel` và `PersonalityViewModel`**
  - **Acceptance:**
    - [ ] `ProfileViewModel` kết nối dữ liệu user Google, switch thông báo, đăng xuất xóa backstack về Login.
    - [ ] `PersonalityViewModel` quản lý dirty state machine, lưu thành công cập nhật profile và pop back.
    - [ ] Kết nối `onProfileClick` từ `HomeScreen` -> mở `ProfileRoute`, từ Profile -> mở `PersonalityRoute`.
  - **Verify:**
    - [ ] `./gradlew :app:testDevDebugUnitTest` pass.
  - **Files:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/ProfileViewModel.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/profile/personality/PersonalityViewModel.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/navigation/NavGraph.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`

---

## Phase 6: Unit Test Suite & QA Checkpoint
- [ ] **Task 6: Bộ Unit Test toàn diện (AC-01 đến AC-20) & Build Verification**
  - **Acceptance:**
    - [ ] Unit tests cho `ProfileViewModel` (navigation, sign-out, preferences) pass 100%.
    - [ ] Unit tests cho `PersonalityViewModel` (dirty back guard, save idempotency, online check) pass 100%.
    - [ ] `./gradlew assembleDevDebug :app:testDevDebugUnitTest` hoàn tất không lỗi.
  - **Verify:**
    - [ ] `./gradlew assembleDevDebug :app:testDevDebugUnitTest` thành công.
  - **Files:**
    - `app/src/test/java/com/hoabh/chanko/presentation/screens/profile/ProfileViewModelTest.kt`
    - `app/src/test/java/com/hoabh/chanko/presentation/screens/profile/PersonalityViewModelTest.kt`

---

### Checkpoint: Hoàn tất & Nghiệm thu
- [ ] Toàn bộ unit tests pass.
- [ ] Build dự án thành công.
- [ ] Sẵn sàng nghiệm thu theo `AC-01` đến `AC-20`.
