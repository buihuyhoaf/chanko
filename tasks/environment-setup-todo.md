# Tasks: Cấu Hình 3 Môi Trường (Dev - Staging - Release)

Nhánh: `feature/multi-environment` | Kế hoạch: `tasks/environment-setup-plan.md` | Đặc tả: `docs/ideas/environment-setup-dev-stg-release.md`

---

- [x] Task 1: Cấu hình Product Flavors & Application ID Suffix trong `app/build.gradle.kts`
  - Acceptance: Khai báo 3 flavors (`dev`, `staging`, `prod`) trong dimension `environment`. Cấu hình `applicationIdSuffix`, `resValue` app name ("Chanko Dev", "Chanko Stg", "Chanko"), và các cờ `IS_MOCK_ENV`.
  - Verify: `./gradlew tasks --all` hoặc `./gradlew help` nhận diện 6 variants.
  - Files: `app/build.gradle.kts`, `app/src/main/res/values/strings.xml`

## Checkpoint 1: Build Matrix Verification
- [x] Gradle project sync sạch sẽ không xung đột.
- [x] Khung variants sẵn sàng.

---

- [x] Task 2: Tạo Source Set `dev` & Fake Auth Provider
  - Acceptance: Tạo `app/src/main/java/com/hoabh/chanko/dev/DevFakeAuthenticationRepository.kt` và tích hợp `AuthModule` cung cấp fake auth trả về test user thành công ngay lập tức, bỏ qua luồng login thực tế.
  - Verify: `./gradlew assembleDevDebug` thành công.
  - Files: `app/src/main/java/com/hoabh/chanko/dev/DevFakeAuthenticationRepository.kt`, `app/src/main/java/com/hoabh/chanko/di/AuthModule.kt`

- [x] Task 3: Seed Data & Local Mock Data Setup cho Dev
  - Acceptance: Tự động nạp sẵn dữ liệu mẫu (món đồ, ngân sách, chu kỳ chi tiêu) khi khởi chạy trên `devDebug`.
  - Verify: Chạy unit test `DevEnvironmentTest` pass 100%.
  - Files: `app/src/main/java/com/hoabh/chanko/dev/DevDataSeeder.kt`, `app/src/main/java/com/hoabh/chanko/MainActivity.kt`

## Checkpoint 2: Dev Environment Ready
- [x] `./gradlew assembleDevDebug` thành công.
- [x] Biến thể `dev` hoạt động 100% offline không cần server.

---

- [x] Task 4: Thiết lập Source Sets & URL Cấu hình cho Staging và Prod
  - Acceptance: Cấu hình URL riêng biệt và cờ `IS_DEV_MOCK = false` cho Staging và Prod. Tối ưu hóa R8 và thu gọn mã nguồn cho Prod release.
  - Verify: `./gradlew assembleStagingDebug` và `./gradlew assembleProdRelease` build thành công.
  - Files: `app/build.gradle.kts`, `app/src/main/java/com/hoabh/chanko/di/NetworkModule.kt`, `app/src/main/java/com/hoabh/chanko/di/AuthModule.kt`

## Checkpoint 3: Multi-Environment Isolation
- [x] `assembleDevDebug`, `assembleStagingDebug`, `assembleProdRelease` đều build sạch.
- [x] Không có mã giả lập nào bị rò rỉ vào APK Prod (R8 tree shaking loại bỏ).

---

- [x] Task 5: Chạy Unit Tests đa Variant và Tài liệu hóa Workflow
  - Acceptance: Toàn bộ unit test của 3 biến thể đều PASS: `./gradlew testDevDebugUnitTest`, `./gradlew testStagingDebugUnitTest`, `./gradlew testProdDebugUnitTest`. Tài liệu hướng dẫn sử dụng tiếng Việt hoàn tất.
  - Verify: Cả 3 lệnh test và lệnh tổng `./gradlew test` chạy thành công không có lỗi.
  - Files: `docs/features/environment-setup.md`, `app/src/test/java/com/hoabh/chanko/dev/DevEnvironmentTest.kt`

## Checkpoint 4: Complete & Done
- [x] Toàn bộ 5 task hoàn tất và được nghiệm thu.
- [x] Sẵn sàng tạo Pull Request vào `dev-chanko-1x`.
