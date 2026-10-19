# Chanko Coding Standards & Architecture Guidelines

Tài liệu này định nghĩa các quy chuẩn code (Coding Standards), kiến trúc và quy ước đặt tên cho dự án Android Chanko. Tất cả các tính năng và refactor mới đều cần tuân thủ các quy tắc này.

---

## 1. Kiến trúc tổng thể & Tổ chức Code (Clean Architecture & Multi-module)

Dự án áp dụng mô hình Clean Architecture kết hợp MVVM, chia theo các module / package rõ ràng:

- **Presentation Layer** (`app/src/main/java/.../presentation/` hoặc `feature/*`):
  - Chứa UI (Jetpack Compose), Screen, ViewModel, UI State (`UiState`), UI Event / Effect (`UiEvent`, `UiAction`).
  - Presentation chỉ phụ thuộc vào Domain layer (UseCases, Domain Models), không phụ thuộc trực tiếp vào Data layer hay database.
- **Domain Layer** (`core/domain/` hoặc từng feature domain):
  - Chứa pure Kotlin code: Domain Models, Repository Interfaces, UseCases.
  - Tuyệt đối **không** phụ thuộc Android SDK (không `Context`, không `android.os.*`, không Compose).
- **Data Layer** (`core/data/`, `core/database/`, `core/network/`):
  - Cung cấp triển khai thực tế (Implementation) của các Repository Interface từ Domain.
  - Quản lý Local Data Source (Room / DataStore) và Remote Data Source (Retrofit / Ktor).
  - Chuyển đổi DTO / Entity sang Domain Model thông qua Mapper (`toDomain()`, `toEntity()`).

---

## 2. Quy ước đặt tên (Naming Conventions)

| Thành phần | Quy tắc đặt tên | Ví dụ |
| :--- | :--- | :--- |
| **Classes, Interfaces, Enums, Objects** | `PascalCase`, danh từ hoặc cụm danh từ | `UserProfileViewModel`, `TransactionRepository`, `Expense` |
| **Screen Composable (Màn hình chính)** | `<Feature>Screen` (bắt buộc hậu tố `Screen`) | `HomeScreen`, `LoginScreen`, `TransactionDetailScreen` |
| **Composable Functions (Thành phần UI/Component)** | `PascalCase`, danh từ/mô tả UI (trả về Unit) | `PrimaryButton`, `TransactionItem`, `HeaderBar` |
| **Hàm / Methods thông thường** | `camelCase`, bắt đầu bằng động từ | `getUserProfile()`, `calculateTotal()`, `onDeleteClicked()` |
| **Biến & Thuộc tính (Variables & Properties)** | `camelCase` | `userId`, `isLoading`, `transactionList` |
| **Hằng số (Constants / `const val`)** | `SCREAMING_SNAKE_CASE` | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| **ViewModel** | `<Feature>ViewModel` | `LoginViewModel`, `StatisticsViewModel` |
| **UI State** | `<Feature>UiState` (data class) | `SplashUiState`, `WalletUiState` |
| **UI Event / User Action** | `<Feature>UiEvent` hoặc `<Feature>Action` (sealed interface/class) | `HomeUiEvent.Refresh`, `LoginUiEvent.Submit` |
| **Repository Interface** | `<Entity>Repository` | `AccountRepository`, `CategoryRepository` |
| **Repository Implementation** | `<Entity>RepositoryImpl` hoặc `<Source><Entity>Repository` | `AccountRepositoryImpl`, `OfflineFirstAccountRepository` |
| **UseCase** | Động từ hành động + danh từ: `<Action><Entity>UseCase` | `GetTransactionsUseCase`, `DeleteCategoryUseCase` |
| **Data Mappers** | Extension functions `toDomain()`, `toEntity()`, `toDto()` | `TransactionEntity.toDomainModel(): Transaction` |

---

## 3. Quy chuẩn Jetpack Compose

- **State Hoisting**:
  - Tách biệt Stateless Composable và Stateful Composable:
    - `Stateless`: Nhận state làm tham số và emit event thông qua lambda callback (ví dụ `onItemClick: (String) -> Unit`). Dễ test và hiển thị Preview.
    - `Stateful`: Kết nối với ViewModel, thu thập state (`collectAsStateWithLifecycle()`), truyền data và callback xuống Stateless Composable.
- **Single Source of Truth (SSOT)**:
  - ViewModel sở hữu và quản lý UI State duy nhất thông qua `StateFlow<UiState>`.
- **Modifier Parameter**:
  - Hầu hết các Composable có thể tái sử dụng phải có tham số `modifier: Modifier = Modifier` ở vị trí đầu tiên trong danh sách tham số tùy chọn (optional parameters) và áp dụng nó lên layout gốc của Composable đó.
- **Naming Màn hình (Screen)**:
  - Tất cả Composable đại diện cho một màn hình độc lập và file chứa nó bắt buộc phải có hậu tố `Screen` (ví dụ: `HomeScreen.kt`, `fun HomeScreen(...)`, `LoginScreen.kt`, `fun LoginScreen(...)`).
  - Nếu chia tách Stateful/Stateless: sử dụng `HomeScreen` (Stateful entry point) và `HomeContent` (hoặc `HomeScreenContent` - Stateless cho UI & Preview).
- **Preview**:
  - Cung cấp `@Preview` cho các Stateless Composable (hỗ trợ Light / Dark theme nếu có thể).
  - Sử dụng sample mock data sạch cho Preview.

---

## 4. Quản lý Coroutines & Dispatchers

- **Không hardcode Dispatchers**:
  - Không gọi trực tiếp `Dispatchers.IO`, `Dispatchers.Default` trong UseCase / Repository.
  - Sử dụng Dependency Injection (Hilt) để inject `CoroutineDispatcher` (giúp viết Unit Test dễ dàng với `StandardTestDispatcher`).
- **ViewModel Scope**:
  - Chạy coroutine trong ViewModel qua `viewModelScope.launch`.
  - Luôn xử lý lỗi bằng `try-catch` hoặc `runCatching` để cập nhật trạng thái lỗi vào UI State, tránh crash app.
- **StateFlow & Flow**:
  - Dùng `asStateFlow()` để expose read-only state ra UI từ `MutableStateFlow`.
  - Dùng `SharingStarted.WhileSubscribed(5000)` khi chuyển đổi Flow sang StateFlow trong ViewModel để tối ưu tài nguyên khi UI đi vào background.

---

## 5. Quản lý Resource & Chuỗi hiển thị (Strings & Resources)

- **Không hardcode String**:
  - Tuyệt đối không hardcode text hiển thị trên UI trong mã nguồn Kotlin/Compose.
  - Toàn bộ chuỗi phải được định nghĩa trong `res/values/strings.xml` (hoặc `values-vi/strings.xml` theo quy định UI tiếng Việt của dự án).
  - Sử dụng `stringResource(R.string.your_string_id)` trong Composable hoặc `Context.getString()` khi cần.
- **Quy tắc đặt tên String resource**:
  - Định dạng: `<màn_hình>_<thành_phần>_<nội_dung>` hoặc `<module>_<tính_năng>`.
  - Ví dụ: `login_btn_submit`, `splash_title_welcome`, `home_error_empty_data`.
- **Dimens & Icons**:
  - Sử dụng Material Theme Tokens (`MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, `MaterialTheme.shapes.*`) thay vì hardcode giá trị dp/màu riêng lẻ khi không cần thiết.

---

## 6. Code Style & Clean Code Checklist

- **Ưu tiên Immutability**:
  - Luôn sử dụng `val` thay vì `var`, trừ khi trạng thái nội bộ thực sự cần thay đổi cục bộ.
  - Sử dụng immutable collections (`List`, `Set`, `Map`) trong State và Data Models.
- **Hạn chế Force Unwrap**:
  - Tránh tuyệt đối toán tử `!!`. Sử dụng safe call `?.`, Elvis operator `?:`, hoặc `checkNotNull` / `requireNotNull` kèm thông báo rõ ràng.
- **Độ dài hàm & Lớp**:
  - Mỗi hàm chỉ nên làm một việc duy nhất (Single Responsibility Principle).
  - Composable quá dài (> 100-150 dòng) nên được bóc tách thành các Composable con chuyên biệt.
