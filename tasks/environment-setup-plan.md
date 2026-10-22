# Implementation Plan: Cấu Hình 3 Môi Trường (Dev - Staging - Release)

## Overview
Thiết lập 3 môi trường phát triển độc lập (`dev`, `staging`, `prod`) cho ứng dụng Chanko thông qua Gradle Product Flavors (`flavorDimensions += "environment"`) kết hợp Hilt Dependency Injection. Môi trường `dev` cho phép lập trình viên làm việc mượt mà, offline 100%, tự động bypass authentication và nạp dữ liệu mẫu mà không phụ thuộc backend; môi trường `staging` kết nối máy chủ phát triển thực tế với công cụ ghi log cho QA; môi trường `prod` tối ưu bảo mật, kích hoạt R8 và trỏ máy chủ thực tế. Cả 3 môi trường có `applicationIdSuffix` độc lập, cho phép cài đặt song song trên cùng thiết bị thực tế.

## Architecture Decisions
1. **Single-Module Flavor at `:app`**: Chỉ khai báo `productFlavors` tại module `:app`, giữ các library modules (`:core:network`, `:core:data`, `:core:database`, `:core:domain`) không flavor để tránh nhân số lượng variant build làm chậm Gradle.
2. **Package Separation (`applicationIdSuffix`)**:
   - `dev`: `com.hoabh.chanko.dev` (App label: "Chanko Dev")
   - `staging`: `com.hoabh.chanko.stg` (App label: "Chanko Stg")
   - `prod`: `com.hoabh.chanko` (App label: "Chanko")
   Giúp cài đặt song song 3 app trên cùng một điện thoại thực tế mà không đè Room database hay preferences của nhau.
3. **Flavored DI Source Sets**:
   - `app/src/dev/`: Chứa `DevAuthModule` cung cấp `FakeGoogleAuthRepository` (trả về tài khoản test ngay lập tức, không cần Google Play Services) và nạp sẵn mock database data.
   - `app/src/staging/` & `app/src/prod/`: Sử dụng real network bindings qua `NetworkModule` và `AuthModule`.
4. **Zero Mock Leakage**: Toàn bộ class Fake/Mock chỉ tồn tại trong `src/dev/`, Gradle tự động loại bỏ khỏi artifact của Staging và Prod khi build release.

---

## Task List

### Phase 1: Gradle Build Matrix & Flavor Configuration (Foundation)
- [x] **Task 1: Cấu hình Product Flavors & Application ID Suffix trong `app/build.gradle.kts`**
  - **Description:** Khai báo dimension `environment` với 3 flavors: `dev`, `staging`, `prod`. Cấu hình `applicationIdSuffix`, `resValue` cho `app_name`, và các biến `buildConfigField` cho API Base URL và cờ `IS_MOCK_ENV`.
  - **Acceptance criteria:**
    - Gradle sync thành công, Android Studio sinh ra 6 build variants (`devDebug`, `devRelease`, `stagingDebug`, `stagingRelease`, `prodDebug`, `prodRelease`).
    - Cài đặt test app `dev` và `staging` có tên hiển thị phân biệt ("Chanko Dev", "Chanko Stg", "Chanko").
  - **Verification:** `./gradlew help` và `./gradlew tasks --all` hiển thị đúng danh sách variants.
  - **Dependencies:** None
  - **Files likely touched:** [app/build.gradle.kts](file:///Users/buihoa/AndroidStudioProjects/chanko/app/build.gradle.kts), `app/src/main/res/values/strings.xml`
  - **Estimated scope:** Small (1-2 files)

### Checkpoint 1: Build Matrix Verification
- [x] Gradle project sync sạch sẽ không có xung đột manifest hay namespace.
- [x] Khung flavor sẵn sàng để tạo source sets.

---

### Phase 2: Dev Environment Fakes & Bypass Engine (Dev Slice)
- [x] **Task 2: Tạo Source Set `dev` & Fake Auth Provider**
  - **Description:** Tạo thư mục `app/src/dev/java/com/hoabh/chanko/di/` chứa DI binding cho `GoogleAuthRepository` và `AuthBackend` trả về thành công tức thì với `TestUser` mẫu, giúp dev không bị chặn ở màn hình đăng nhập.
  - **Acceptance criteria:**
    - Ở flavor `dev`, khi mở ứng dụng hoặc gọi đăng nhập, app tự động cấp phiên làm việc hợp lệ mà không cần gọi Google Sign-In SDK hay mạng.
    - Không ảnh hưởng đến code của `main`.
  - **Verification:** `./gradlew assembleDevDebug` biên dịch thành công. Unit tests cho Dev Auth pass.
  - **Dependencies:** Task 1
  - **Files likely touched:** `app/src/dev/java/com/hoabh/chanko/di/DevAuthModule.kt`, `app/src/main/java/com/hoabh/chanko/di/AuthModule.kt`
  - **Estimated scope:** Medium (2-3 files)

- [x] **Task 3: Seed Data & Local Mock Data Setup cho Dev**
  - **Description:** Thiết lập cơ chế tự động nạp dữ liệu mẫu (danh sách món đồ, ngân sách mẫu, lịch chi tiêu) vào Room DB hoặc In-Memory Repository khi chạy trên biến thể `devDebug`.
  - **Acceptance criteria:**
    - Sau khi cài bản `dev`, mở app là có sẵn dữ liệu mẫu hiển thị trên HomeScreen mà không cần thao tác tạo bằng tay.
  - **Verification:** Chạy test hoặc kiểm tra log khởi tạo seed data trên `devDebug`.
  - **Dependencies:** Task 2
  - **Files likely touched:** `app/src/dev/java/com/hoabh/chanko/data/DevDataSeeder.kt`, `app/src/dev/java/com/hoabh/chanko/di/DevRepositoryModule.kt`
  - **Estimated scope:** Medium (2-3 files)

### Checkpoint 2: Dev Environment Ready
- [x] `./gradlew assembleDevDebug` thành công.
- [x] Biến thể `dev` hoạt động 100% offline không cần server.

---

### Phase 3: Staging & Production Configuration (Real Server Slice)
- [x] **Task 4: Thiết lập Source Sets & URL Cấu hình cho Staging và Prod**
  - **Description:** Cấu hình `src/staging/` và `src/prod/` để inject Real Repositories và Real Auth backend. Cấu hình HTTP Logging Interceptor và debug symbols cho Staging, đồng thời bật tối ưu ProGuard/R8 và tắt log cho Prod.
  - **Acceptance criteria:**
    - Bản Staging trỏ đúng URL máy chủ thử nghiệm, ghi log network ra Logcat/Chucker.
    - Bản Prod trỏ máy chủ thật, không có log nhạy cảm, R8 thu gọn code an toàn.
  - **Verification:** `./gradlew assembleStagingDebug` và `./gradlew assembleProdRelease` build thành công.
  - **Dependencies:** Task 1, Task 2
  - **Files likely touched:** [app/build.gradle.kts](file:///Users/buihoa/AndroidStudioProjects/chanko/app/build.gradle.kts), `app/src/staging/java/com/hoabh/chanko/di/*`, `app/src/prod/java/com/hoabh/chanko/di/*`
  - **Estimated scope:** Medium (3-4 files)

### Checkpoint 3: Multi-Environment Isolation
- [x] `assembleDevDebug`, `assembleStagingDebug`, `assembleProdRelease` đều build sạch.
- [x] Không có mã giả lập nào bị rò rỉ vào APK Prod.

---

### Phase 4: Verification & Multi-Variant Regression Gate
- [x] **Task 5: Chạy Unit Tests đa Variant và Tài liệu hóa Workflow**
  - **Description:** Chạy toàn bộ bộ test trên tất cả các variants để bảo đảm không có regression. Cập nhật tài liệu hướng dẫn chuyển đổi variant trong Android Studio và cấu hình CI/CD.
  - **Acceptance criteria:**
    - Toàn bộ unit test của cả 3 biến thể đều PASS: `./gradlew testDevDebugUnitTest`, `./gradlew testStagingDebugUnitTest`, `./gradlew testProdReleaseUnitTest`.
    - Tài liệu hướng dẫn sử dụng rõ ràng bằng tiếng Việt.
  - **Verification:** Cả 3 lệnh test chạy thành công không có lỗi.
  - **Dependencies:** Task 1, 2, 3, 4
  - **Files likely touched:** `README.md`, `docs/features/environment-setup.md`
  - **Estimated scope:** Small (1-2 files)

### Checkpoint 4: Complete & Done
- [x] Toàn bộ 5 task hoàn tất và được nghiệm thu.
- [x] Sẵn sàng đưa vào nhánh `feature/multi-environment` và tạo PR vào `dev-chanko-1x`.

---

## Risks and Mitigations
| Risk | Impact | Mitigation |
|---|---|---|
| Hilt DI xung đột do duplicate binding giữa các source set | High | Tách interface chung ở `:core:domain`, đặt `@InstallIn` cụ thể trong từng source set `src/dev/` và `src/staging/` thay vì đặt song song ở `src/main/`. |
| Google OAuth Client ID lỗi do khác package name (`.dev`, `.stg`) | Medium | Với `dev`, dùng fake repo hoàn toàn (không gọi Google API). Với `staging`, thêm SHA-1 và package ID tương ứng lên Google Cloud Console. |
| Thời gian build chậm khi có nhiều flavors | Low | Chỉ đặt flavor ở `:app`, các core module giữ dạng standard Android library. |

## Open Questions
- Bạn muốn đặt URL mặc định cho server Staging là gì (hoặc dùng tạm placeholder `https://staging-api.chanko.app/api/v1/`)?
