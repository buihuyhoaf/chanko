# Tasks: Architecture & Codebase Restructuring

> **Plan tham chiếu:** [tasks/plan.md](plan.md)  
> **Coding Standards:** [CODING_STANDARDS.md](../CODING_STANDARDS.md)  
> **Project Rules:** [AGENTS.md](../AGENTS.md)  

---

## Phase 0: Architecture Baseline & Documentation

### Task 0: Thiết lập `docs/architecture/phase-0/README.md` và Source Inventory
**Description:**
Xây dựng tài liệu kiến trúc baseline cho Phase 0 tại `docs/architecture/phase-0/README.md` theo quy định tại `AGENTS.md`. Tài liệu ghi nhận hiện trạng kiểm kê source code (Source Inventory), phân loại rõ các quyết định (Approved, Proposed, Pending, Superseded), và định nghĩa ranh giới các layer cho đợt refactor.

**Acceptance criteria:**
- [x] Tạo file `docs/architecture/phase-0/README.md` với đầy đủ các mục: Source Inventory, Module Boundaries, Package Conventions, Status of Decisions (Approved/Proposed/Pending/Superseded).
- [x] Liệt kê chính xác hiện trạng các module `:app`, `:core:domain`, `:core:data`, `:core:database`, `:core:network`.
- [x] Định nghĩa rõ các ranh giới kiến trúc cần đạt sau khi hoàn thành tái cấu trúc.

**Verification:**
- [x] File `docs/architecture/phase-0/README.md` được tạo và kiểm tra nội dung đúng chuẩn markdown.
- [x] Không chỉnh sửa bất kỳ code nào trong Phase 0.

**Dependencies:** None  
**Files likely touched:**
- `docs/architecture/phase-0/README.md`  
**Estimated scope:** Small (1 file)

---

## Checkpoint 0: Phase 0 Baseline Documentation
- [x] Tài liệu baseline kiến trúc đã hoàn chỉnh và sẵn sàng để người dùng thẩm duyệt trước khi bước sang Phase 1.

---

## Phase 1: Presentation & Design System Standardization

### Task 1: Hợp nhất Design System và Gom cụm UI Tokens
**Description:**
Gom các phần tử Design System đang bị phân tán giữa `com.hoabh.chanko.ui` (`theme/`, `mascot/`, generic components) và `com.hoabh.chanko.presentation.designsystem.icon` về một cấu trúc package tập trung duy nhất `com.hoabh.chanko.presentation.designsystem` (hoặc `ui.designsystem`), gồm `theme/`, `tokens/`, `components/`, `icon/`, `mascot/`. Cập nhật tất cả các import liên quan trong `:app`.

**Acceptance criteria:**
- [x] Không còn tình trạng Design System bị xé đôi giữa `ui/` và `presentation/designsystem/`.
- [x] Các token màu, gradient, typography, shape và icons được import nhất quán.
- [x] Tất cả các Composable Preview liên quan đến Theme và Mascot hiển thị chính xác.

**Verification:**
- [x] Tests pass: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest --tests "*Theme*" --tests "*CandyBudget*"`
- [x] Build succeeds: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew compileDevDebugKotlin`

**Dependencies:** Task 0  
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/ui/theme/*`
- `app/src/main/java/com/hoabh/chanko/ui/mascot/*`
- `app/src/main/java/com/hoabh/chanko/presentation/designsystem/icon/*`
- Các screens tham chiếu đến theme và mascot  
**Estimated scope:** Medium (4-5 files di chuyển và cập nhật imports)

---

### Task 2: Chuẩn hóa cấu trúc các Screens (`home`, `login`, `additem`)
**Description:**
Đồng bộ hóa cấu trúc thư mục của các feature screens theo đúng quy chuẩn `CODING_STANDARDS.md`:
- Đơn giản hóa `screens/login/`: bỏ các sub-packages `screen/` và `viewmodel/`, đặt phẳng `LoginScreen.kt`, `LoginViewModel.kt`, `LoginUiState.kt`, `LoginUiEffect.kt`.
- Di chuyển `HomeFakeDataSource.kt` ra khỏi `screens/home/data/` (chuyển sang `dev` hoặc package dữ liệu thích hợp).
- Phân nhóm màn hình `screens/additem/`: tách rõ ràng các dialogs, bottom sheets, form components thành sub-package `components/` và flow screens.

**Acceptance criteria:**
- [x] Không còn thư mục `data/` nằm bên trong `presentation/screens/home/`.
- [x] Package `presentation/screens/login/` phẳng và sạch sẽ.
- [x] Package `presentation/screens/additem/` có phân cấp rõ ràng (màn hình chính vs sub-components).
- [x] Không làm thay đổi logic hay hành vi người dùng trên bất kỳ màn hình nào.

**Verification:**
- [x] Các màn hình Login, Setup, Chat, Home và AddItem đã được gom về cấu trúc phẳng chuẩn `CODING_STANDARDS.md`.
- [x] Cập nhật toàn bộ các import và test files tương ứng.

**Dependencies:** Task 1  
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/*`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/login/*`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/additem/*`  
**Estimated scope:** Medium (4-5 files)

---

## Checkpoint 1: Presentation & UI Cleanup
- [x] Toàn bộ UI và Presentation biên dịch sạch không có warning về import hỏng.
- [x] Các màn hình chính (Home, Login, Add Item) chạy unit test giao diện / viewmodel đều pass.

---

## Phase 2: Budget Feature Migration to Clean Core

### Task 3: Chuyển Domain Models & Interfaces của Budget sang `:core:domain`
**Description:**
Di chuyển toàn bộ các model (`BudgetCycle`, `BudgetCalculation`, `MoneyVnd`, `BudgetSnapshot`, `BudgetAdjustment`, `NotableItem`, `BudgetCycleStatus`, `BudgetActivity`, `BudgetCycleBoundaryCalculator`, `TriCycleWindow`) và repository interfaces (`BudgetRepository`, `SharedItemResolver`) từ `app/src/main/java/com/hoabh/chanko/domain/budget/` sang `core/domain/src/main/kotlin/com/hoabh/chanko/domain/budget/`. Đảm bảo module `:core:domain` là pure Kotlin (không có Android/Compose dependencies).

**Acceptance criteria:**
- [x] Tất cả model và repository interface của `budget` nằm trong `:core:domain` với package `com.hoabh.chanko.domain.budget.*`.
- [x] `:core:domain` không có bất kỳ import nào từ Android SDK hoặc Jetpack Compose.
- [x] Xóa bỏ package `app/src/main/java/com/hoabh/chanko/domain/budget`.

**Verification:**
- [x] Tests pass: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :core:domain:test`
- [x] Build succeeds: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :core:domain:compileKotlin`

**Dependencies:** Task 2  
**Files likely touched:**
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/budget/*`
- `app/src/main/java/com/hoabh/chanko/domain/budget/*` (xóa sau khi di chuyển)
- `core/domain/build.gradle.kts`  
**Estimated scope:** Medium (4-5 files di chuyển)

---

### Task 4: Chuyển Repository Implementation của Budget sang `:core:data`
**Description:**
Di chuyển `FakeBudgetRepository.kt` và `SharedItemResolverImpl.kt` từ `app/src/main/java/com/hoabh/chanko/data/budget/` sang `core/data/src/main/java/com/hoabh/chanko/data/budget/`. Đảm bảo `:core:data` cung cấp đúng implementation cho `BudgetRepository` và `SharedItemResolver` từ `:core:domain`.

**Acceptance criteria:**
- [x] `FakeBudgetRepository` và `SharedItemResolverImpl` nằm trong `:core:data` với package `com.hoabh.chanko.data.budget.*`.
- [x] Xóa bỏ thư mục `app/src/main/java/com/hoabh/chanko/data/budget/`.
- [x] Các tests kiểm thử repository của Budget chuyển sang `:core:data:src/test` hoặc cập nhật import tương ứng.

**Verification:**
- [x] Tests pass: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :core:data:test`
- [x] Build succeeds: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :core:data:compileDebugKotlin`

**Dependencies:** Task 3  
**Files likely touched:**
- `core/data/src/main/java/com/hoabh/chanko/data/budget/*`
- `app/src/main/java/com/hoabh/chanko/data/budget/*` (xóa)  
**Estimated scope:** Medium (3-4 files)

---

### Task 5: Cập nhật DI Binding trong `:app`
**Description:**
Cập nhật các module Hilt DI trong `app/src/main/java/com/hoabh/chanko/di/` (`RepositoryModule.kt`, `AddItemModule.kt`) để inject các dependencies của Budget từ `:core:domain` và `:core:data`. Đảm bảo các ViewModels (`HomeViewModel`, `BudgetViewModel`, `AddItemViewModel`) nhận được dependencies thông qua Interface mà không phụ thuộc trực tiếp vào class triển khai nội bộ.

**Acceptance criteria:**
- [x] Hilt module cung cấp `BudgetRepository` và `SharedItemResolver` dưới dạng Singleton.
- [x] Mọi ViewModels sử dụng interface từ `:core:domain`.
- [x] Toàn bộ luồng DI build và inject thành công không có lỗi compile-time hay duplicate binding.

**Verification:**
- [x] Tests pass: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDevDebugUnitTest`
- [x] Build succeeds: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDevDebug`

**Dependencies:** Task 4  
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/di/RepositoryModule.kt`
- `app/src/main/java/com/hoabh/chanko/di/AddItemModule.kt`
- Các ViewModels liên quan  
**Estimated scope:** Medium (3-4 files)

---

## Checkpoint 2: Clean Architecture for Budget
- [x] Module `:app` hoàn toàn không còn chứa domain và data của Budget.
- [x] Hệ thống Dependency Injection liên kết thông suốt giữa `:app`, `:core:domain`, và `:core:data`.

---

## Phase 3: Data Layer, Mappers & Dev Code Cleanliness

### Task 6: Chuẩn hóa Data Mappers và Naming trong `:core:data`
**Description:**
Refactor file `core/data/.../entity/CooldownItemEntity.kt`: đổi tên thành `CooldownItemMapper.kt` và chuyển sang package `com.hoabh.chanko.data.mapper`. Rà soát tất cả các data mapper khác giữa Room Entity, Remote DTO và Domain Model để đảm bảo tuân thủ mục 2 của `CODING_STANDARDS.md`.

**Acceptance criteria:**
- [x] Không còn file mapper nào mang tên trùng với Room Entity.
- [x] Toàn bộ extension functions `toDomain()`, `toEntity()` được đặt trong package `com.hoabh.chanko.data.local.entity` chuẩn hóa cùng Entity.
- [x] Mọi lời gọi mapper trong `:core:data` được cập nhật import chính xác.

**Verification:**
- [x] Tests pass: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :core:data:test`
- [x] Build succeeds: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :core:data:compileDebugKotlin`

**Dependencies:** Task 5  
**Files likely touched:**
- `core/data/src/main/java/com/hoabh/chanko/data/mapper/CooldownItemMapper.kt`
- `core/data/src/main/java/com/hoabh/chanko/data/local/entity/CooldownItemEntity.kt` (xóa/đổi tên)
- Các file repository impl trong `:core:data`  
**Estimated scope:** Small (2-3 files)

---

### Task 7: Phân tách và Cô lập Dev/Mock Utilities
**Description:**
Rà soát thư mục `app/src/main/java/com/hoabh/chanko/dev/` (`DevDataSeeder`, `DevSetupGateway`, `DevFakeAuthenticationRepository`). Đảm bảo các mock/fake này được đóng gói độc lập, chỉ được kích hoạt khi `BuildConfig.IS_DEV_MOCK == true`, và không bị rò rỉ vào luồng khởi tạo chính của Staging/Production.

**Acceptance criteria:**
- [x] Các class Dev/Mock được bảo vệ nghiêm ngặt bởi flag môi trường (`IS_DEV_MOCK`).
- [x] Production build (`prodRelease`) không kích hoạt bất kỳ logic seeding hay fake auth nào.
- [x] Các Fake Data Source (như `HomeFakeDataSource`) đã chuyển về đúng khu vực dev/test data.

**Verification:**
- [x] Tests pass: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDevDebugUnitTest`
- [x] Build succeeds: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew compileProdReleaseKotlin`

**Dependencies:** Task 6  
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/dev/*`
- `app/src/main/java/com/hoabh/chanko/ChankoApplication.kt`  
**Estimated scope:** Small (2-3 files)

---

### Task 8: Đóng gói Persistence Layer và Rà soát Dependency Graph
**Description:**
Kiểm tra mối quan hệ phụ thuộc giữa các module trong `settings.gradle.kts` và các file `build.gradle.kts`. Đảm bảo rằng `:core:database` chỉ phục vụ như nguồn dữ liệu cho `:core:data`. Xác định và giảm thiểu các liên kết trực tiếp không cần thiết từ `:app` vào `:core:database` mà không thông qua Repository layer.

**Acceptance criteria:**
- [x] Bản đồ phụ thuộc giữa các module tuân thủ luồng: `:app` -> `:core:data` & `:core:domain`, `:core:data` -> `:core:database` & `:core:network` & `:core:domain`.
- [x] Không có circular dependencies giữa các Gradle modules.
- [x] Tất cả các Room DAOs được truy cập thông qua Repository trong `:core:data`.

**Verification:**
- [x] Tests pass: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew test`
- [x] Build succeeds: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDevDebug`

**Dependencies:** Task 7  
**Files likely touched:**
- `app/build.gradle.kts`
- `core/data/build.gradle.kts`
- `app/src/main/java/com/hoabh/chanko/di/DatabaseModule.kt`  
**Estimated scope:** Medium (3-4 files)

---

## Checkpoint 3: Structural Integrity Verification
- [x] Toàn bộ các module độc lập, ranh giới rõ ràng, không có rò rỉ persistence hay mapper sai quy chuẩn.
- [x] Toàn bộ unit tests của cả dự án đều vượt qua.

---

## Phase 4: Final Verification & Sign-off

### Task 9: Kiểm thử Hồi quy Toàn diện và Kiểm tra Biên dịch
**Description:**
Chạy toàn bộ bộ test suite của dự án trên tất cả các module (`:core:domain`, `:core:data`, `:core:database`, `:core:network`, `:app`), kiểm tra biên dịch cả 3 flavors (`dev`, `staging`, `prod`). Tuân thủ quy định tại `AGENTS.md`: không cài đặt lên máy ảo hay thiết bị thật, chỉ kiểm tra build và unit tests.

**Acceptance criteria:**
- [x] Toàn bộ unit tests pass: `./gradlew testDebugUnitTest`.
- [x] Build thành công: `./gradlew assembleDevDebug` và `./gradlew assembleProdRelease`.
- [x] Không có lỗi lint hoặc compilation warning nghiêm trọng.

**Verification:**
- [x] Terminal logs xác nhận 100% tests passed.
- [x] Build APK artifacts được tạo thành công trong `app/build/outputs/apk/`.

**Dependencies:** Task 8  
**Files likely touched:** Không chỉnh sửa code, chỉ chạy lệnh kiểm thử  
**Estimated scope:** Small (0-1 file)

---

### Task 10: Rà soát Tiêu chuẩn Coding Standards & Báo cáo Hoàn tất
**Description:**
Thực hiện audit cuối cùng đối chiếu với [CODING_STANDARDS.md](../CODING_STANDARDS.md):
- Kiểm tra quy ước đặt tên (Naming Conventions) cho Screens, ViewModels, States, Events, Repositories, UseCases.
- Kiểm tra không còn hardcode text UI (tuân thủ `strings.xml`).
- Kiểm tra không còn file mồ côi hoặc unused imports.
- Lập báo cáo hoàn tất tái cấu trúc.

**Acceptance criteria:**
- [x] 100% các tiêu chí trong `CODING_STANDARDS.md` được thỏa mãn.
- [x] Cập nhật trạng thái hoàn thành trong `tasks/todo.md` và `tasks/plan.md`.

**Verification:**
- [x] Code review checklist đạt chuẩn.

**Dependencies:** Task 9  
**Files likely touched:**
- `tasks/todo.md`
- `tasks/plan.md`  
**Estimated scope:** Small (1-2 files)

---

## Checkpoint 4: Final Sign-off
- [x] Toàn bộ 11 tasks từ Task 0 đến Task 10 hoàn thành.
- [x] Dự án Chanko đạt chuẩn Clean Architecture & Multi-module hoàn chỉnh, sẵn sàng cho các đợt phát triển tính năng tiếp theo.
