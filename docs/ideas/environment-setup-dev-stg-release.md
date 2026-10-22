# Kiến trúc 3 Môi Trường: Dev - Staging - Release

## Problem Statement
Làm thế nào để thiết lập 3 môi trường tách biệt (dev - stg - release) cho Chanko giúp lập trình viên phát triển UI/Logic hoàn toàn độc lập mà không bị gián đoạn bởi server/auth, đồng thời QA kiểm thử sát thực tế và bản release đạt chuẩn bảo mật cao nhất?

## Recommended Direction
Thiết lập **Gradle Product Flavors** với dimension `environment` (`dev`, `staging`, `prod`) kết hợp phân chia nguồn dữ liệu qua **Hilt Dependency Injection**:
1. **Môi trường Dev (`devDebug`)**:
   - `applicationId`: `com.hoabh.chanko.dev`
   - Dữ liệu: 100% Mock / Fake Repository nạp sẵn dữ liệu mẫu (Seed Data) vào Room DB hoặc in-memory.
   - Xác thực: Tự động đăng nhập với Test User (Bypass Google/OTP Auth) để dev vào thẳng tính năng cần làm.
2. **Môi trường Staging (`stagingDebug` / `stagingRelease`)**:
   - `applicationId`: `com.hoabh.chanko.stg`
   - Dữ liệu: Kết nối trực tiếp máy chủ Staging qua `CHANKO_API_BASE_URL_STG`.
   - Tiện ích: Kèm HTTP Logging Interceptor và debug symbols để QA bắt lỗi.
3. **Môi trường Release (`prodRelease`)**:
   - `applicationId`: `com.hoabh.chanko`
   - Dữ liệu: Kết nối máy chủ Production thật.
   - Bảo mật: Tối ưu R8/ProGuard, tắt toàn bộ log nhạy cảm và gỡ bỏ công cụ debug.

## Key Assumptions to Validate
- [ ] Backend Staging API đã có hợp đồng dữ liệu (API Schema / Swagger) khớp với cấu trúc Fake Repository của Dev.
- [ ] Google Cloud Console / Auth Provider cho phép đăng ký thêm Client ID cho package `com.hoabh.chanko.dev` và `com.hoabh.chanko.stg`.
- [ ] Việc cài đặt song song 3 app không gây nhầm lẫn biểu tượng trên màn hình chính (cần badge màu và tên phân biệt cho Dev và Staging).

## MVP Scope
- Cấu hình `flavorDimensions += "environment"` trong [app/build.gradle.kts](file:///Users/buihoa/AndroidStudioProjects/chanko/app/build.gradle.kts).
- Khai báo 3 flavors: `dev`, `staging`, `prod` với `applicationIdSuffix` và `manifestPlaceholders["appLabel"]`.
- Tạo source set `app/src/dev/` chứa Hilt Module cung cấp `FakeAuthRepository` / `FakeDataRepository`.
- Tạo source set `app/src/staging/` và `app/src/prod/` cung cấp `RealRepository` kết nối Retrofit qua [NetworkModule](file:///Users/buihoa/AndroidStudioProjects/chanko/app/src/main/java/com/hoabh/chanko/di/NetworkModule.kt).
- Đặt `CHANKO_API_BASE_URL` mặc định riêng cho từng flavor qua BuildConfig hoặc gradle properties.

## Not Doing (and Why)
- **Không tạo flavor ở các module core (`:core:network`, `:core:data`, `:core:database`)**: Giữ cấu trúc Gradle đơn giản, build nhanh, tránh lỗi missing variant dimension.
- **Không nhúng file JSON mock khổng lồ vào app release**: Toàn bộ mock data chỉ nằm trong source set `dev/`, Gradle sẽ loại bỏ hoàn toàn khỏi build Staging/Release.
- **Không làm In-app dynamic environment switcher trong MVP**: Đã cài song song 3 app độc lập trên máy thì không cần thêm dropdown chuyển URL phức tạp trong app.

## Open Questions
- Server Staging hiện tại đã có URL cố định hay đang dựng local?
- Luồng Google Sign-In ở Staging có cần test thực tế với tài khoản Google thật hay có backend endpoint bypass riêng cho tester?
