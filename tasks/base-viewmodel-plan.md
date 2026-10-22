# Implementation Plan: BaseViewModel & Side Effect Architecture

Tài liệu đặc tả nguồn: [docs/ideas/base-viewmodel.md](file:///Users/buihoa/AndroidStudioProjects/chanko/docs/ideas/base-viewmodel.md)  
Quy chuẩn kiến trúc: [CODING_STANDARDS.md](file:///Users/buihoa/AndroidStudioProjects/chanko/CODING_STANDARDS.md)

---

## 1. Overview
Triển khai kiến trúc `BaseViewModel<UiState, UiEffect>` gọn nhẹ và tiện ích Composable `CollectEffect` cho Presentation Layer của Chanko. Mục tiêu chính là giảm mã lặp (boilerplate) khi quản lý `StateFlow`, chuẩn hóa xử lý One-off Side Effect (Navigation, Toast, Dialog) qua Kotlin `Channel`, đồng thời tuân thủ nghiêm ngặt nguyên tắc YAGNI và Single Source of Truth (SSOT).

---

## 2. Architecture Decisions & Rationale

1. **Lightweight Base Class qua Generic `<UiState, UiEffect>`**:
   - `_uiState: MutableStateFlow<UiState>` được quản lý nội bộ, chỉ expose `uiState: StateFlow<UiState>` ra UI.
   - `_effect: Channel<UiEffect>(Channel.BUFFERED)` expose qua `Flow<UiEffect>` với `receiveAsFlow()`. Đảm bảo không mất sự kiện khi màn hình đang chuyển tiếp hoặc xoay màn hình (recomposition/lifecycle change).
   - Cung cấp 2 hàm helper: `updateState { ... }` và `sendEffect(...)`.

2. **Không ép buộc `UiIntent` (No Redux-style boilerplate)**:
   - UI gọi trực tiếp các action methods của ViewModel (`onAmountChanged`, `loginWithGoogle`). Giữ code tự nhiên, dễ đọc, giảm số lượng class và lambda trung gian.

3. **Lifecycle-Aware Effect Collection (`CollectEffect`)**:
   - Sử dụng `LocalLifecycleOwner.current` và `repeatOnLifecycle(Lifecycle.State.STARTED)` để chỉ thu thập effect khi màn hình đang hiển thị, tự động dừng khi vào background.

4. **Tránh God-Class & Tuân thủ YAGNI**:
   - Không nhét `isLoading`, `errorMessage` hay `CoroutineDispatcher` vào Base class. Tất cả trạng thái thuộc về nghiệp vụ phải nằm trong từng `UiState` cụ thể; Dispatchers được inject qua constructor.

---

## 3. Dependency Graph & Triển khai theo lát cắt (Vertical Slicing)

```
[Task 1: BaseViewModel Foundation]
          │
          ├── [Task 2: BaseViewModel Unit Tests]
          │
[Task 3: CollectEffect Composable Helper]
          │
          └── [Task 4: Pilot Migration (LoginViewModel) & UI Integration]
                    │
                    └── [Task 5: Quality Gate & Verification]
```

---

## 4. Detailed Task Breakdown

### Task 1: Xây dựng BaseViewModel Foundation
**Description:** Tạo class trừu tượng `BaseViewModel<UiState, UiEffect>` tại package `com.hoabh.chanko.presentation.base`. Cung cấp quản lý `uiState` (MutableStateFlow) và `effect` (Channel BUFFERED) cùng các hàm tiện ích `updateState` và `sendEffect`.

**Acceptance criteria:**
- Class `BaseViewModel` kế thừa `androidx.lifecycle.ViewModel`.
- `uiState` expose ra ngoài dưới dạng `StateFlow<UiState>`.
- `effect` expose ra ngoài dưới dạng `Flow<UiEffect>` qua `receiveAsFlow()`.
- Hỗ trợ `updateState(reducer: (UiState) -> UiState)` và `sendEffect(effect: UiEffect)` / `sendEffect(builder: () -> UiEffect)`.

**Verification:**
- Build succeeds: `./gradlew assembleDebug`

**Dependencies:** None  
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/base/BaseViewModel.kt`

**Estimated scope:** Small (1 file)

---

### Task 2: Viết Unit Tests cho BaseViewModel
**Description:** Tạo bộ kiểm thử đơn vị `BaseViewModelTest` kiểm tra toàn bộ luồng hoạt động của `BaseViewModel`: khởi tạo state ban đầu, cập nhật state đồng bộ qua `updateState`, buffer và phát side effect qua `sendEffect`, đảm bảo hoạt động trơn tru với `StandardTestDispatcher`.

**Acceptance criteria:**
- Kiểm tra state ban đầu khớp với giá trị truyền vào constructor.
- Kiểm tra `updateState` cập nhật giá trị mới chính xác vào `uiState` và `currentState`.
- Kiểm tra `sendEffect` phát sự kiện vào Flow và subscriber nhận được đầy đủ theo thứ tự.
- Kiểm tra hành vi buffer của Channel khi effect được phát trước khi có collector.

**Verification:**
- Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.presentation.base.BaseViewModelTest"`

**Dependencies:** Task 1  
**Files likely touched:**
- `app/src/test/java/com/hoabh/chanko/presentation/base/BaseViewModelTest.kt`

**Estimated scope:** Small (1 file)

---

### Checkpoint 1: BaseViewModel Core Verified
- [ ] Task 1 & Task 2 hoàn thành.
- [ ] Unit tests cho `BaseViewModel` pass 100%.
- [ ] Codebase compile không lỗi.

---

### Task 3: Xây dựng Composable Helper CollectEffect
**Description:** Tạo Composable function `CollectEffect` tại package `com.hoabh.chanko.presentation.base`. Tiện ích này đóng gói `LaunchedEffect` và `Lifecycle.repeatOnLifecycle` để thu thập `Flow<UiEffect>` an toàn theo vòng đời Compose.

**Acceptance criteria:**
- `CollectEffect` nhận `effectFlow: Flow<T>` và callback `onEffect: suspend (T) -> Unit`.
- Mặc định sử dụng `Lifecycle.State.STARTED` và `LocalLifecycleOwner.current`.
- Hủy thu thập coroutine khi lifecycle rơi xuống dưới `STARTED` (khi app vào background hoặc screen navigate đi).

**Verification:**
- Build succeeds: `./gradlew assembleDebug`

**Dependencies:** None (song song với Task 1 & 2)  
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/base/CollectEffect.kt`

**Estimated scope:** Small (1 file)

---

### Task 4: Pilot Migration trên LoginViewModel & LoginRoute
**Description:** Áp dụng `BaseViewModel` và `CollectEffect` làm mẫu (pilot) trên `LoginViewModel` và `LoginRoute`. Định nghĩa `LoginUiEffect` (ví dụ: `NavigateToHome`), refactor `LoginViewModel` kế thừa `BaseViewModel<LoginUiState, LoginUiEffect>`, và sử dụng `CollectEffect` trong `LoginRoute` hoặc `LoginScreen`.

**Acceptance criteria:**
- `LoginViewModel` kế thừa `BaseViewModel<LoginUiState, LoginUiEffect>`.
- Toàn bộ hành vi đăng nhập Google, xử lý lỗi `AuthFailure`, và huỷ job đăng nhập được bảo toàn 100%.
- Toàn bộ test case trong `LoginViewModelTest` tiếp tục pass 100% mà không bị phá vỡ.
- `CollectEffect` được tích hợp vào `LoginRoute` để xử lý sự kiện điều hướng sau khi login thành công.

**Verification:**
- Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.LoginViewModelTest"`
- Build succeeds: `./gradlew assembleDebug`

**Dependencies:** Task 1, Task 2, Task 3  
**Files likely touched:**
- `../app/src/main/java/com/hoabh/chanko/presentation/screens/login/viewmodel/LoginViewModel.kt`
- `../app/src/main/java/com/hoabh/chanko/presentation/screens/login/screen/LoginScreen.kt`
- `app/src/test/java/com/hoabh/chanko/LoginViewModelTest.kt`

**Estimated scope:** Medium (2-3 files)

---

### Task 5: Quality Gate & Regression Check
**Description:** Chạy toàn bộ test suite của dự án, kiểm tra compilation, rà soát static analysis / lint rules và đối chiếu với [CODING_STANDARDS.md](file:///Users/buihoa/AndroidStudioProjects/chanko/CODING_STANDARDS.md).

**Acceptance criteria:**
- `./gradlew testDebugUnitTest` pass 100% trên toàn bộ các test suites của app và modules.
- `./gradlew assembleDebug` thành công không có lỗi hoặc warning mới.
- Không vi phạm nguyên tắc YAGNI và coding standards.

**Verification:**
- Tests pass: `./gradlew testDebugUnitTest`
- Build succeeds: `./gradlew assembleDebug`

**Dependencies:** Task 4  
**Files likely touched:**
- Toàn bộ các files đã thêm/sửa

**Estimated scope:** Small (verification only)

---

### Checkpoint 2: Final Verification
- [ ] Toàn bộ 5 tasks hoàn thành.
- [ ] Không có hồi quy (regression) trên các tính năng hiện tại.
- [ ] Kiến trúc sẵn sàng nhân rộng cho các ViewModel khác trong tương lai.
