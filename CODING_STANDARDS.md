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
| **UI State** | `<Feature>UiState` (data class hoặc sealed interface) | `SplashUiState`, `OverviewUiState`, `ItemUiState` |
| **UI Event / User Action** | `<Feature>UiEvent` hoặc `<Feature>Action` (sealed interface/class) | `HomeUiEvent.Refresh`, `LoginUiEvent.Submit` |
| **Repository Interface** | `<Entity>Repository` | `AccountRepository`, `CategoryRepository` |
| **Repository Implementation** | `<Entity>RepositoryImpl` hoặc `<Source><Entity>Repository` | `AccountRepositoryImpl`, `OfflineFirstAccountRepository` |
| **UseCase** | Động từ hành động + danh từ: `<Action><Entity>UseCase` | `GetTransactionsUseCase`, `DeleteCategoryUseCase` |
| **Data Mappers** | Extension functions `toDomain()`, `toEntity()`, `toDto()` | `TransactionEntity.toDomainModel(): Transaction` |

---

## 3. Quy chuẩn Jetpack Compose

- **State Hoisting & Quản lý State đúng phạm vi**:
  - Tách biệt Stateless Composable và Stateful Composable:
    - `Stateless`: Nhận state làm tham số và emit event thông qua lambda callback (ví dụ `onItemClick: (String) -> Unit`). Dễ test và hiển thị Preview.
    - `Stateful`: Kết nối với ViewModel, thu thập state (`collectAsStateWithLifecycle()`), truyền data và callback xuống Stateless Composable.
  - **Phạm vi State**: Không bắt buộc mọi UI state phải nằm trong ViewModel. State chỉ thuộc về UI nội bộ (ví dụ: expanded/collapsed của một card, text input tạm thời, scroll position, animation state) nên được giữ ngay tại Composable bằng `remember` hoặc `rememberSaveable`. Chỉ đưa lên ViewModel các trạng thái liên quan đến business logic, dữ liệu cần nạp/lưu hoặc cần sống sót qua cấu hình (configuration changes).
- **Single Source of Truth (SSOT)**:
  - ViewModel sở hữu và quản lý UI State cấp màn hình thông qua `StateFlow<UiState>`.
- **Modifier Parameter**:
  - Hầu hết các Composable có thể tái sử dụng phải có tham số `modifier: Modifier = Modifier` ở vị trí đầu tiên trong danh sách tham số tùy chọn (optional parameters) và áp dụng nó lên layout gốc của Composable đó.
- **Naming Màn hình (Screen)**:
  - Tất cả Composable đại diện cho một màn hình độc lập và file chứa nó bắt buộc phải có hậu tố `Screen` (ví dụ: `HomeScreen.kt`, `fun HomeScreen(...)`, `LoginScreen.kt`, `fun LoginScreen(...)`).
  - Nếu chia tách Stateful/Stateless: có thể sử dụng `<Feature>Route` (Stateful entry point) và `<Feature>Screen` (Stateless cho UI & Preview).
- **Preview**:
  - Cung cấp `@Preview` cho các Stateless Composable (hỗ trợ Light / Dark theme nếu có thể).
  - Sử dụng sample mock data sạch cho Preview.

---

## 4. Component Reusability & Thiết kế Phân cấp

Cần phân loại rõ ràng mục đích và phạm vi sử dụng của các Composable Component, tránh tạo các tầng trừu tượng (abstraction) không cần thiết hoặc sớm hơn thực tế:

1. **Design System Components** (`ui/theme/` hoặc `core/designsystem/`):
   - Các viên gạch nền tảng: Typography, Colors, Shapes, Spacing, Buttons cơ bản (`CandyPrimaryButton`), Cards, Inputs.
   - Không chứa bất kỳ business logic hay dữ liệu domain nào.
2. **Shared / Common Components** (`presentation/components/`):
   - Các UI block dùng chung giữa nhiều màn hình khác nhau trong app (ví dụ: `ItemPage`, `ItemRow`, `EmptyStateView`, `StatusBadge`).
   - Nhận dữ liệu tổng quát hoặc domain model thuần túy qua tham số, không ràng buộc trực tiếp với ViewModel cụ thể.
3. **Feature-specific Components** (nằm bên trong thư mục của feature/screen đó):
   - Các Composable chỉ phục vụ cho một màn hình hoặc tính năng riêng biệt (ví dụ: form nhập liệu riêng, phần header đặc thù của feature).
   - Đặt cùng package với màn hình đó; chỉ di chuyển sang Shared Components khi có ít nhất 2 feature độc lập cùng tái sử dụng.
4. **Tránh Over-engineering & Unnecessary Abstraction**:
   - Tuân thủ nguyên tắc YAGNI (You Aren't Gonna Need It) và Rule of Three: Không vội vàng tạo component dùng chung, base class hay generic wrapper nếu component đó chỉ mới dùng tại một nơi duy nhất.

---

## 5. Design System & Quản lý Resource / Theme

- **Tuân thủ Design Tokens được duyệt**:
  - Bắt buộc sử dụng các design token màu sắc, kiểu chữ (typography), bo góc (shapes) và khoảng cách (spacing/padding) đã được định nghĩa trong hệ thống Design System (`MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, `MaterialTheme.shapes.*`, hoặc custom theme như `CandyShape`).
  - Tránh hardcode giá trị màu hex (như `Color(0xFF123456)`), kích thước font hoặc bo góc tùy tiện rải rác trong code.
  - Tránh duplicate định nghĩa kiểu dáng và màu sắc giữa các màn hình.
- **Không hardcode String trên UI**:
  - Tuyệt đối không hardcode text hiển thị trên UI trong mã nguồn Kotlin/Compose.
  - Toàn bộ chuỗi phải được định nghĩa trong `res/values/strings.xml` (hoặc `values-vi/strings.xml` theo quy định UI tiếng Việt của dự án).
  - Sử dụng `stringResource(R.string.your_string_id)` trong Composable hoặc `Context.getString()` khi cần.
- **Quy tắc đặt tên String resource**:
  - Định dạng: `<màn_hình>_<thành_phần>_<nội_dung>` hoặc `<module>_<tính_năng>`.
  - Ví dụ: `login_btn_submit`, `splash_title_welcome`, `home_error_empty_data`.

---

## 6. Compose Best Practices & Hiệu năng (Performance)

- **Đơn giản hóa Component API**:
  - Giữ danh sách tham số của Composable gọn gàng, rõ ràng; nhóm các event callback hoặc state có quan hệ mật thiết nếu cần.
  - Tránh truyền toàn bộ ViewModel hay controller phức tạp vào sâu các component con; chỉ truyền đúng dữ liệu và event lambda mà component đó cần.
- **Tránh Unnecessary Recomposition**:
  - Đảm bảo data class truyền vào Composable là ổn định (Stable/Immutable).
  - Tránh khởi tạo object mới hoặc lambda không cần thiết ngay trong thân Composable (sử dụng `remember` khi tính toán nặng hoặc tạo list/map cố định).
  - Sử dụng `derivedStateOf` khi state phụ thuộc vào một state khác thay đổi liên tục (ví dụ: scroll offset, so sánh điều kiện boolean).
  - Tránh đọc StateFlow/State ở cấp độ quá cao trong cây Composable nếu chỉ có một phần tử con nhỏ bên dưới cần recompose.

---

## 7. Quản lý Coroutines & Dispatchers

- **Không hardcode Dispatchers**:
  - Không gọi trực tiếp `Dispatchers.IO`, `Dispatchers.Default` trong UseCase / Repository.
  - Sử dụng Dependency Injection (Hilt) hoặc default parameter constructor để inject `CoroutineDispatcher` (giúp viết Unit Test dễ dàng với `StandardTestDispatcher`).
- **ViewModel Scope & Xử lý lỗi**:
  - Chạy coroutine trong ViewModel qua `viewModelScope.launch`.
  - **Linh hoạt xử lý lỗi**: Không bắt buộc mọi coroutine phải bọc trong `try-catch`. Chỉ sử dụng `try-catch` / `runCatching` ở các tác vụ có rủi ro exception (I/O, network, Keystore, parsing) hoặc khi cần bắt lỗi để cập nhật error state hiển thị lên UI. Luôn lưu ý re-throw `CancellationException` để không phá vỡ cơ chế huỷ coroutine của Kotlin.
- **StateFlow & Flow**:
  - Dùng `asStateFlow()` để expose read-only state ra UI từ `MutableStateFlow`.
  - **Linh hoạt cấu hình Flow**: Sử dụng `SharingStarted.WhileSubscribed(5_000)` là khuyến nghị tốt cho các cold flow cấp màn hình cần tiết kiệm tài nguyên khi UI vào background. Tuy nhiên, không bắt buộc mọi Flow phải dùng `WhileSubscribed(5000)` — có thể dùng `SharingStarted.Eagerly` hoặc `Lazily` khi dữ liệu cần được chuẩn bị sẵn hoặc phụ thuộc vào vòng đời khác.

---

## 8. Agent Refactoring Rules (Quy tắc Refactor dành cho AI / Developer)

Để đảm bảo an toàn tuyệt đối cho codebase trong quá trình refactor, agent và developer phải tuân thủ nghiêm ngặt quy trình:

1. **Audit trước khi sửa (Audit First)**:
   - Luôn phân tích, rà soát phạm vi ảnh hưởng (dependency tree, usages) và lập danh sách chi tiết các điểm cần sửa trước khi bắt tay vào thay đổi code.
2. **Bảo toàn hành vi (Preserve Behavior)**:
   - Refactor chỉ nhằm cải thiện cấu trúc, tính rõ ràng, hiệu năng hoặc tuân thủ quy chuẩn — **tuyệt đối không làm thay đổi hành vi nghiệp vụ** sẵn có nếu không có yêu cầu rõ ràng từ người dùng.
3. **Thực hiện từng bước nhỏ (Thin Slices & Atomic PRs)**:
   - Chia nhỏ công việc thành từng lát cắt mỏng, dễ kiểm soát. Mỗi thay đổi nên được commit độc lập với message rõ ràng theo Conventional Commits.
   - Không gom tất cả thay đổi lớn vào một bước duy nhất.
4. **Chạy Test & Build xác minh (Verify Continuously)**:
   - Sau mỗi bước thay đổi, bắt buộc phải chạy kiểm thử (`./gradlew testDebugUnitTest`) và kiểm tra build (`./gradlew assembleDebug`).
   - Tuyệt đối không cài đặt lên máy ảo/thiết bị nếu không có chỉ thị theo quy định của dự án. Chỉ bàn giao khi toàn bộ test suite đã pass.

---

## 9. Code Style & Clean Code Checklist

- **Ưu tiên Immutability**:
  - Luôn sử dụng `val` thay vì `var`, trừ khi trạng thái nội bộ thực sự cần thay đổi cục bộ.
  - Sử dụng immutable collections (`List`, `Set`, `Map`) trong State và Data Models.
- **Hạn chế Force Unwrap**:
  - Tránh tuyệt đối toán tử `!!`. Sử dụng safe call `?.`, Elvis operator `?:`, hoặc `checkNotNull` / `requireNotNull` kèm thông báo rõ ràng.
- **Độ dài hàm & Lớp**:
  - Mỗi hàm chỉ nên làm một việc duy nhất (Single Responsibility Principle).
  - Composable quá dài (> 100-150 dòng) nên được bóc tách thành các Composable con chuyên biệt.
