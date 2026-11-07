# Implementation Plan: Architecture & Codebase Restructuring

> **Mục tiêu:** Chuẩn hóa toàn bộ cấu trúc mã nguồn Chanko theo đúng mô hình Clean Architecture & Multi-module, khắc phục triệt để tình trạng phân mảnh UI/Design System, layer leakage (Domain/Data của Budget bị đặt ở `:app`), và chuẩn hóa quy ước đặt tên/package theo [CODING_STANDARDS.md](../CODING_STANDARDS.md) cùng quy định tại [AGENTS.md](../AGENTS.md).

---

## 1. Overview (Tổng quan Kế hoạch)

Hiện tại, codebase Chanko đã phân tách một số module (`:core:domain`, `:core:data`, `:core:database`, `:core:network`), nhưng cấu trúc thực tế đang bị lai tạp và chưa nhất quán:
1. **Layer Leakage:** Domain và Data của tính năng `budget` lại đang nằm hoàn toàn trong module `:app` (`app/.../domain/budget` và `app/.../data/budget`), trong khi các tính năng khác (`chat`, `auth`, `item`) đều nằm ở `:core:domain` và `:core:data`.
2. **UI & Design System bị phân mảnh:** Tồn tại song song 2 package gốc `com.hoabh.chanko.ui` (chứa Theme, Mascot, generic components) và `com.hoabh.chanko.presentation` (chứa Screens, Components, Navigation, Design System Icon).
3. **Cấu trúc package màn hình (Screens) thiếu chuẩn hóa:** 
   - `screens/login/` bị chia nhỏ thành thư mục con `screen/` và `viewmodel/`.
   - `screens/home/data/` chứa trực tiếp `HomeFakeDataSource` ngay trong tầng Presentation.
   - `screens/additem/` dồn 11 file vào một thư mục phẳng mà không phân cấp components.
4. **Data Layer & Dev Code chưa tách bạch:** Mappers bị đặt tên trùng Entity (`CooldownItemEntity.kt` trong `:core:data`), và Dev Data Seeder / Fake Auth nằm lẫn trong production source set của `:app`.

Kế hoạch này chia làm 4 giai đoạn rõ ràng:
- **Phase 0:** Lập tài liệu kiến trúc cơ sở (Architecture Baseline & Source Inventory) tại `docs/architecture/phase-0/README.md` theo quy định nghiêm ngặt tại `AGENTS.md`.
- **Phase 1:** Chuẩn hóa tầng Presentation & Design System trong `:app`.
- **Phase 2:** Di dời Domain & Data của `budget` về đúng `:core:domain` và `:core:data`.
- **Phase 3:** Chuẩn hóa Data Layer, Mappers, đóng gói Persistence và tách bạch Dev/Mock code.
- **Phase 4:** Kiểm thử toàn diện (Unit Tests, Build Verification, Coding Standards Audit).

---

## 2. Architecture Decisions (Các Quyết định Kiến trúc Cốt lõi)

1. **AD-01 (Single Source for Design System):**
   - Hợp nhất toàn bộ Tokens, Colors, Typography, Mascot, Icons và Base Components về `presentation/designsystem` (hoặc `ui/designsystem`), tạo nền móng vững chắc để có thể trích xuất ra module `:core:designsystem` độc lập trong tương lai mà không làm vỡ tham chiếu.
2. **AD-02 (Pure Kotlin Domain Layer):**
   - Toàn bộ Domain models và Repository interfaces của `budget` phải chuyển sang `:core:domain` (`com.hoabh.chanko.domain.budget.*`).
   - `:core:domain` tuyệt đối không phụ thuộc vào Android SDK hay Compose dependencies.
3. **AD-03 (Encapsulated Persistence & Single Repository Entry):**
   - Triển khai Repository của Budget (`FakeBudgetRepository`, `SharedItemResolverImpl`) được đưa về `:core:data`.
   - Module `:app` chỉ tương tác với Data thông qua Repository Interface của Domain và DI Module. Không để tầng Presentation gọi trực tiếp Data Sources hay Room DAOs.
4. **AD-04 (Screen Package Pattern Uniformity):**
   - Mọi feature screen trong `presentation/screens/<feature>/` phải tuân thủ cấu trúc thống nhất:
     - `<Feature>Screen.kt` (Stateful Route + Stateless Screen Composable)
     - `<Feature>ViewModel.kt`
     - `<Feature>UiState.kt`, `<Feature>UiEvent.kt`, `<Feature>UiEffect.kt` (nếu có)
     - `components/` (chứa các Composable con chuyên biệt của màn hình đó)
   - Xóa bỏ các thư mục con dư thừa (như `login/screen/`, `login/viewmodel/`) và di chuyển data source ra khỏi presentation (`screens/home/data/`).
5. **AD-05 (Explicit Mapper & Dev Separation):**
   - Đặt tên mapper rõ ràng theo định dạng `<Entity>Mapper.kt` trong `com.hoabh.chanko.data.mapper`.
   - Gom các công cụ dev/mock (`DevSetupGateway`, `DevDataSeeder`, `FakeAuthRepository`) vào vùng dev có ranh giới rõ ràng, không lẫn vào luồng production.

---

## 3. Dependency Graph & Implementation Order

```
[Phase 0: Architecture Baseline & Inventory (docs)]
                         │
                         ▼
[Phase 1: Presentation & Design System Standardization]
    ├── Task 1: Unify Design System & Tokens
    └── Task 2: Standardize Screen Package Structure
                         │
                         ▼
[Phase 2: Clean Architecture for Budget Feature]
    ├── Task 3: Move Budget Domain to :core:domain
    ├── Task 4: Move Budget Data/Repo to :core:data
    └── Task 5: Re-wire Hilt DI in :app
                         │
                         ▼
[Phase 3: Data Layer, Mappers & Dev Code Cleanliness]
    ├── Task 6: Refactor Mappers & Naming
    ├── Task 7: Isolate Dev/Mock Tools
    └── Task 8: Persistence Encapsulation Audit
                         │
                         ▼
[Phase 4: Full Verification & Regression Gate]
    ├── Task 9: Unit Tests & Build Verification
    └── Task 10: Coding Standards & Rules Audit
```

---

## 4. Task List & Checkpoints

### Phase 0: Architecture Baseline & Documentation
- [x] **Task 0:** Thiết lập `docs/architecture/phase-0/README.md` và Source Inventory theo quy định `AGENTS.md`.

### Phase 1: Presentation & Design System Standardization
- [x] **Task 1:** Hợp nhất Design System (Theme, Colors, Mascot, Icons, Common UI Components) về một cấu trúc package đồng nhất.
- [x] **Task 2:** Chuẩn hóa cấu trúc các màn hình (`home`, `login`, `additem`) và loại bỏ `HomeFakeDataSource` khỏi presentation.

### Checkpoint 1: Presentation Verification
- [x] Biên dịch Kotlin tầng UI sạch sẽ, không có import bị lỗi.
- [x] Preview của các component và screen hoạt động bình thường.

### Phase 2: Budget Feature Migration to Clean Core
- [x] **Task 3:** Chuyển Domain Models & Interfaces của `budget` từ `:app` sang `:core:domain`.
- [x] **Task 4:** Chuyển Repository Implementation của `budget` từ `:app` sang `:core:data`.
- [x] **Task 5:** Cập nhật Hilt DI Modules (`RepositoryModule`, `AddItemModule`) và liên kết dependencies giữa các module.

### Checkpoint 2: Core Domain & Data Verification
- [x] `:core:domain` compile thuần túy không có Android dependency.
- [x] Các unit test cho Budget Domain & Logic (`BudgetCycleTest`, `HomeDomainLogicTest`, ...) chạy thông suốt.

### Phase 3: Data Layer, Mappers & Dev Code Cleanliness
- [x] **Task 6:** Chuẩn hóa Mappers (tách và đổi tên `CooldownItemEntity.kt` trong `:core:data` thành `CooldownItemMapper.kt` trong package `mapper`).
- [x] **Task 7:** Phân tách và dọn dẹp Dev/Mock components (`app/.../dev/`), tránh lẫn vào production logic.
- [x] **Task 8:** Rà soát và đóng gói persistence layer (`:core:database`), giảm thiểu tiếp xúc trực tiếp từ `:app`.

### Checkpoint 3: Structural Integrity Verification
- [x] Toàn bộ các module `:core:domain`, `:core:data`, `:core:database`, `:core:network`, `:app` liên kết chặt chẽ và không có vòng lặp (circular dependency).

### Phase 4: Final Verification & Sign-off
- [x] **Task 9:** Thực hiện kiểm thử toàn bộ dự án (`./gradlew testDebugUnitTest`) và kiểm tra build (`./gradlew assembleDebug`).
- [x] **Task 10:** Đối chiếu toàn diện với `CODING_STANDARDS.md` và quy định cấm cài đặt thiết bị/máy ảo trong `AGENTS.md`.

### Checkpoint 4: Completion & Readiness
- [x] Không có file thừa, không còn import mồ côi (unused imports).
- [x] Báo cáo nghiệm thu cấu trúc hoàn tất.

---

## 5. Risks and Mitigations

| Rủi ro | Mức độ | Biện pháp giảm thiểu |
|---|---|---|
| Đổi package làm gãy hàng loạt import và references trong UI | Cao | Thực hiện từng slice nhỏ (move từng package), dùng refactor an toàn, chạy compile check sau mỗi bước nhỏ. |
| Xung đột DI khi di chuyển Repository từ `:app` sang `:core:data` | Trung bình | Kiểm tra kỹ các `@Provides` / `@Binds` trong Hilt modules; cập nhật scope `@Singleton` đồng bộ. |
| Vi phạm quy định Phase 0 / Phase 1 trong `AGENTS.md` | Cao | Luôn lập tài liệu baseline `docs/architecture/phase-0/README.md` trước khi tiến hành code refactor, và xác nhận trước khi thực hiện Phase 1. |
| Môi trường sandbox chặn Gradle daemon socket | Thấp | Chạy gradle với quyền bypass sandbox hoặc cấu hình flags thích hợp khi build verification. |

---

## 6. Open Questions
- Bạn có muốn trích xuất Design System ra thành một Gradle module riêng (`:core:designsystem`) ngay trong đợt này hay trước mắt gom gọn bên trong package `presentation/designsystem` của `:app`?
- Các dev utilities (`DevDataSeeder`, `DevSetupGateway`) nên được giữ trong package `dev` của `:app` (được bảo vệ bởi `BuildConfig.IS_DEV_MOCK`) hay tách riêng thành debug source set?
