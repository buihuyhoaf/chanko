# Task List: Triển khai BaseViewModel & Side Effect Architecture

Kế hoạch chi tiết: `tasks/base-viewmodel-plan.md`  
Tài liệu đặc tả: `docs/ideas/base-viewmodel.md`

- [x] Task 1: Xây dựng BaseViewModel Foundation
  - Acceptance: Tạo class trừu tượng `BaseViewModel<UiState, UiEffect>` trong `com.hoabh.chanko.presentation.base`. Hỗ trợ `uiState: StateFlow`, `effect: Flow`, `updateState`, và `sendEffect`.
  - Verify: `./gradlew assembleDebug`
  - Files: `app/src/main/java/com/hoabh/chanko/presentation/base/BaseViewModel.kt`

- [x] Task 2: Viết Unit Tests cho BaseViewModel
  - Acceptance: Viết `BaseViewModelTest` kiểm tra khởi tạo state ban đầu, cập nhật `updateState`, phát và đệm `sendEffect` (Channel buffer), hoạt động chuẩn với test dispatcher.
  - Verify: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.presentation.base.BaseViewModelTest"`
  - Files: `app/src/test/java/com/hoabh/chanko/presentation/base/BaseViewModelTest.kt`

- [x] Checkpoint 1: BaseViewModel Core Verified
  - Acceptance: Task 1 và Task 2 hoàn tất, test pass 100%, build sạch.

- [x] Task 3: Xây dựng Composable Helper CollectEffect
  - Acceptance: Tạo hàm `@Composable fun <T> CollectEffect(...)` bọc `LaunchedEffect` và `repeatOnLifecycle(Lifecycle.State.STARTED)` để UI thu thập effect an toàn.
  - Verify: `./gradlew assembleDebug`
  - Files: `app/src/main/java/com/hoabh/chanko/presentation/base/CollectEffect.kt`

- [x] Task 4: Pilot Migration trên LoginViewModel & LoginRoute
  - Acceptance: Refactor `LoginViewModel` kế thừa `BaseViewModel<LoginUiState, LoginUiEffect>`. Tích hợp `CollectEffect` vào UI. Bảo toàn 100% test trong `LoginViewModelTest`.
  - Verify: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.LoginViewModelTest"` && `./gradlew assembleDebug`
  - Files: `../app/src/main/java/com/hoabh/chanko/presentation/screens/login/viewmodel/LoginViewModel.kt`, `LoginScreen.kt`, `app/src/test/java/com/hoabh/chanko/LoginViewModelTest.kt`

- [x] Task 5: Quality Gate & Regression Check
  - Acceptance: Chạy toàn bộ test suite dự án pass 100%, assembleDebug thành công, kiểm tra tuân thủ CODING_STANDARDS.md.
  - Verify: `./gradlew testDebugUnitTest && ./gradlew assembleDebug`
  - Files: Toàn bộ files đã thêm/sửa

- [x] Checkpoint 2: Final Verification
  - Acceptance: Tất cả acceptance criteria được đáp ứng, sẵn sàng nghiệm thu hoặc merge.
