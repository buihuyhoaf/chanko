# BaseViewModel & Side Effect Architecture Specification

## Problem Statement
Làm thế nào để chuẩn hóa việc quản lý UI State và Side Effect một lần (one-off effect) trên toàn bộ ViewModel của Chanko, giúp giảm thiểu mã lặp (boilerplate) mà không biến ViewModel thành "God Class" vi phạm nguyên tắc YAGNI?

---

## Recommended Direction: Lightweight State & Effect Container
Xây dựng một `abstract class BaseViewModel<UiState, UiEffect>(initialState: UiState) : ViewModel()` gọn nhẹ kết hợp tiện ích Composable `CollectEffect` ở Presentation Layer:
1. **Quản lý trạng thái màn hình (`uiState: StateFlow<UiState>`)**: Đảm bảo Single Source of Truth (SSOT), cập nhật an toàn theo luồng dữ liệu một chiều qua `updateState`.
2. **Quản lý tác vụ một lần (`effect: Flow<UiEffect>`)**: Sử dụng `Channel(Channel.BUFFERED).receiveAsFlow()`, đảm bảo các sự kiện điều hướng (Navigation), hiển thị thông báo (Toast, Snackbar) hoặc Dialog không bị mất và không bị bắn lại khi có Recomposition hoặc Configuration Change (xoay màn hình).
3. **Thu thập Effect an toàn theo vòng đời**: Cung cấp hàm Composable helper `CollectEffect` sử dụng `Lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED)` giúp UI lắng nghe sự kiện ngắn gọn chỉ trong 1 dòng lệnh.
4. **Giữ nguyên User Actions dạng Methods**: Không ép buộc tạo các lớp trung gian `UiIntent` cồng kềnh; UI gọi trực tiếp các phương thức nghiệp vụ của ViewModel.

---

## Architectural Blueprint (Đặc tả thiết kế)

### 1. BaseViewModel Class
Đặt tại: `app/src/main/java/com/hoabh/chanko/presentation/base/BaseViewModel.kt`

```kotlin
package com.hoabh.chanko.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lớp ViewModel cơ sở quản lý UI State và One-off UI Effects.
 *
 * @param UiState Kiểu dữ liệu đại diện cho trạng thái màn hình (thường là data class).
 * @param UiEffect Kiểu dữ liệu đại diện cho sự kiện dùng 1 lần (thường là sealed interface).
 *                 Nếu màn hình không có effect, có thể truyền [Nothing].
 */
abstract class BaseViewModel<UiState, UiEffect>(
    initialState: UiState
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    protected val currentState: UiState
        get() = _uiState.value

    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect: Flow<UiEffect> = _effect.receiveAsFlow()

    /**
     * Cập nhật UI State một cách an toàn và đồng bộ.
     */
    protected fun updateState(reducer: (UiState) -> UiState) {
        _uiState.update(reducer)
    }

    /**
     * Gửi một Side Effect đến UI (chạy trong viewModelScope).
     */
    protected fun sendEffect(effect: UiEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * Gửi một Side Effect thông qua lambda builder.
     */
    protected fun sendEffect(builder: () -> UiEffect) {
        sendEffect(builder())
    }
}
```

### 2. Composable Effect Collector Helper
Đặt tại: `app/src/main/java/com/hoabh/chanko/presentation/base/CollectEffect.kt`

```kotlin
package com.hoabh.chanko.presentation.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Tiện ích thu thập Flow Effect một cách an toàn theo vòng đời của màn hình Compose.
 * Chỉ nhận effect khi màn hình ở trạng thái tối thiểu là [Lifecycle.State.STARTED].
 */
@Composable
fun <T> CollectEffect(
    effectFlow: Flow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: suspend (T) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(effectFlow, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            effectFlow.collect { effect ->
                onEffect(effect)
            }
        }
    }
}
```

---

## Pattern Example (Ví dụ áp dụng thực tế)

### ViewModel
```kotlin
sealed interface LoginUiEffect {
    data object NavigateToHome : LoginUiEffect
    data class ShowErrorSnackbar(val messageResId: Int) : LoginUiEffect
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthenticationRepository
) : BaseViewModel<LoginUiState, LoginUiEffect>(LoginUiState()) {

    fun loginWithGoogle(provider: GoogleTokenProvider) {
        if (currentState.isLoading) return
        updateState { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                when (repository.signIn(provider)) {
                    is AuthResult.Success -> {
                        updateState { it.copy(isLoginSuccess = true) }
                        sendEffect(LoginUiEffect.NavigateToHome)
                    }
                    is AuthResult.Failure -> {
                        sendEffect(LoginUiEffect.ShowErrorSnackbar(R.string.login_failed))
                    }
                }
            } finally {
                updateState { it.copy(isLoading = false) }
            }
        }
    }
}
```

### UI Composable
```kotlin
@Composable
fun LoginRoute(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Thu thập effect chuẩn mực chỉ với 1 dòng lệnh
    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is LoginUiEffect.NavigateToHome -> onNavigateToHome()
            is LoginUiEffect.ShowErrorSnackbar -> {
                snackbarHostState.showSnackbar(context.getString(effect.messageResId))
            }
        }
    }

    LoginScreen(
        state = state,
        onGoogleSignInClick = { viewModel.loginWithGoogle(it) }
    )
}
```

---

## Key Assumptions to Validate
- [ ] **Channel Buffering**: `Channel.BUFFERED` đảm bảo giữ được effect khi UI đang transition/navigate mà không bị drop event trước khi Composable kịp collect.
- [ ] **Compose Collection**: Mọi Screen thu thập `effect` bằng `CollectEffect` tuân thủ vòng đời `repeatOnLifecycle`, không rò rỉ coroutine khi app vào background.
- [ ] **Unit Testability**: Unit test cho ViewModel có thể test cả `uiState` (qua assertions) và `effect` (sử dụng thư viện `Turbine` hoặc collect flow thông thường) mà không gặp trở ngại.

---

## MVP Scope
- Tạo 2 file nền tảng:
  - `presentation/base/BaseViewModel.kt`
  - `presentation/base/CollectEffect.kt`
- Hỗ trợ định nghĩa 2 type parameter: `UiState` và `UiEffect` (dùng `Nothing` nếu không có effect).
- Thử nghiệm trên 1 màn hình đơn lẻ khi bước vào giai đoạn Phase 1 trước khi nhân rộng toàn dự án.

---

## Not Doing (and Why)
- **KHÔNG nhét `isLoading` / `errorMessage` vào BaseViewModel:** Mỗi màn hình có trạng thái lỗi và tải riêng biệt (inline spinner, skeleton, full-screen dialog, v.v.). Nhét chung vào Base là nguyên nhân số 1 biến Base thành "God Class".
- **KHÔNG tích hợp Coroutine Dispatchers vào Base:** Dispatchers phải được inject qua Constructor của ViewModel cụ thể theo quy chuẩn [CODING_STANDARDS.md](file:///Users/buihoa/AndroidStudioProjects/chanko/CODING_STANDARDS.md#L113) để phục vụ Unit Test độc lập.
- **KHÔNG bắt buộc sealed class `UiIntent`:** Tránh boilerplate cho các màn hình đơn giản; chỉ áp dụng MVI Intent nếu màn hình có state machine cực kỳ phức tạp.
- **KHÔNG tự động refactor các ViewModel hiện có ngay lúc này:** Tuân thủ ranh giới Phase 0 documentation-only, bảo vệ code hiện trạng cho đến khi có chỉ thị Phase 1.
