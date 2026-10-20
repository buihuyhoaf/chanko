# Tasks: Refactor Initial Setup Flow

- [x] **Task 1: Resource Strings & UI State Update**
  - **Description**: Bổ sung resource strings tiếng Việt cho AlertDialog và cập nhật `SetupUiState` thêm thuộc tính quản lý Dialog.
  - **Acceptance criteria**:
    - `strings_setup.xml` có các chuỗi: `setup_error_dialog_title` ("Chưa thể lưu thiết lập"), `setup_close` ("Đóng").
    - `SetupUiState` có `showErrorDialog: Boolean = false`, khởi tạo mặc định `page = SetupPage.FORM`.
  - **Verification**: `./gradlew compileDebugKotlin`
  - **Files**: `app/src/main/res/values/strings_setup.xml`, `app/src/main/java/com/hoabh/chanko/presentation/screens/setup/SetupViewModel.kt`

- [x] **Task 2: Refactor SetupViewModel & Unit Tests**
  - **Description**: Loại bỏ tự động check status ở `init`, xử lý khi `save()` lỗi sẽ bật `showErrorDialog` thay vì đổi page, và thêm `dismissErrorDialog()`. Cập nhật `SetupViewModelTest`.
  - **Acceptance criteria**:
    - `SetupViewModel` khởi tạo mở thẳng `SetupPage.FORM`.
    - Khi `save()` thất bại (offline / error / unavailable), `showErrorDialog` bật `true`, `page` vẫn giữ `FORM`.
    - `dismissErrorDialog()` gán `showErrorDialog = false`.
    - Toàn bộ unit tests trong `SetupViewModelTest` pass.
  - **Verification**: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.SetupViewModelTest"`
  - **Files**: `app/src/main/java/com/hoabh/chanko/presentation/screens/setup/SetupViewModel.kt`, `app/src/test/java/com/hoabh/chanko/SetupViewModelTest.kt`

- [x] **Checkpoint 1: Logic & ViewModel Unit Tests**
  - Verification: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.SetupViewModelTest"`

- [x] **Task 3: Implement SetupErrorDialog & Integrate into SetupScreen**
  - **Description**: Xây dựng composable `SetupErrorDialog` và tích hợp hiển thị khi `state.showErrorDialog == true` trong `SetupScreen`.
  - **Acceptance criteria**:
    - Dialog hiển thị đúng tiêu đề, nội dung lỗi theo `SetupProblem`, nút "Thử lại" và "Đóng".
    - Nhấn "Thử lại" gọi hàm save tiếp tục.
    - Nhấn "Đóng" đóng dialog, giữ nguyên dữ liệu đã nhập ở form.
  - **Verification**: `./gradlew compileDebugKotlin`
  - **Files**: `app/src/main/java/com/hoabh/chanko/presentation/screens/setup/SetupStatusScreen.kt` (hoặc tạo file Dialog riêng), `app/src/main/java/com/hoabh/chanko/presentation/screens/setup/screen/SetupScreen.kt`

- [x] **Task 4: Update Existing Tests & Full Verification**
  - **Description**: Điều chỉnh các test case cũ còn phụ thuộc vào luồng `CHECKING`/`STATUS_ERROR` (nếu có), đảm bảo toàn bộ unit test và build compile pass.
  - **Acceptance criteria**:
    - `./gradlew test` pass 100%.
    - `./gradlew assembleDebug` thành công.
  - **Verification**: `./gradlew test assembleDebug`
  - **Files**: `app/src/androidTest/java/com/hoabh/chanko/InitialSetupScreenTest.kt`, `app/src/androidTest/java/com/hoabh/chanko/SplashNavigationTest.kt` (nếu cần cập nhật assertion)
