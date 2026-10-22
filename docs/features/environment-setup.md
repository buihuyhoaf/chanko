# Hướng dẫn Cấu hình và Sử dụng Đa Môi Trường (Dev - Staging - Release)

Tài liệu này hướng dẫn cách cấu hình, chuyển đổi và làm việc với 3 môi trường độc lập trong ứng dụng Chanko: **Dev**, **Staging**, và **Release (Production)**.

---

## 1. Ma trận Môi trường (Environment Matrix)

| Thuộc tính | Dev (`dev`) | Staging (`staging`) | Production (`prod`) |
|---|---|---|---|
| **Package Name (`applicationId`)** | `com.hoabh.chanko.dev` | `com.hoabh.chanko.stg` | `com.hoabh.chanko` |
| **Tên hiển thị (App Name)** | Chanko Dev | Chanko Stg | Chanko |
| **Cài đặt song song** | Có (độc lập) | Có (độc lập) | Có (độc lập) |
| **Chế độ Mock (`IS_DEV_MOCK`)** | `true` (Offline 100%) | `false` (Kết nối server) | `false` (Kết nối server) |
| **Xác thực (Authentication)** | Tự động bypass, cấp phiên `dev-user` | Kết nối Auth Staging / Google OAuth thật | Google OAuth thật & Production Backend |
| **Dữ liệu ban đầu (Seed Data)** | Tự động nạp dữ liệu mẫu vào Room DB | Phụ thuộc dữ liệu tài khoản trên server | Dữ liệu người dùng thực tế |
| **API Base URL mặc định** | `https://dev-api.chanko.app/api/v1/` | `https://staging-api.chanko.app/api/v1/` | Cấu hình qua `CHANKO_API_BASE_URL` |
| **Tối ưu hóa mã (R8 / ProGuard)** | Tắt khi Debug | Tắt khi Debug, bật khi Release | Bật toàn bộ (`optimization.enable = true`) |
| **Mục đích sử dụng** | Phát triển tính năng, làm UI, kiểm thử offline | QA/Tester kiểm thử tính năng kết nối server | Bản phát hành chính thức lên Google Play |

---

## 2. Cách Chuyển đổi Biến thể Build (Build Variants) trong Android Studio

Cả 3 môi trường đều được định nghĩa thông qua Gradle Product Flavors (`dimension: environment`) kết hợp Build Types (`debug`, `release`), tạo thành 6 biến thể:
* `devDebug` *(khuyến nghị cho lập trình viên hàng ngày)*
* `devRelease`
* `stagingDebug` *(khuyến nghị cho QA test nội bộ)*
* `stagingRelease`
* `prodDebug`
* `prodRelease` *(bản đóng gói xuất xưởng)*

### Các bước chuyển đổi:
1. Mở Android Studio.
2. Tại thanh công cụ bên trái (hoặc góc dưới bên trái), chọn tab **Build Variants** (hoặc vào menu `View` > `Tool Windows` > `Build Variants`).
3. Tại dòng module `:app`, nhấp vào cột **Active Build Variant** và chọn biến thể mong muốn (ví dụ: `devDebug` hoặc `stagingDebug`).
4. Nhấn **Run** (phím tắt `Shift + F10`) để biên dịch và cài đặt trực tiếp lên thiết bị.

> [!NOTE]
> Nhờ có `applicationIdSuffix` phân biệt (`.dev` và `.stg`), bạn có thể cài đồng thời cả 3 app: **Chanko Dev**, **Chanko Stg**, và **Chanko** trên cùng một chiếc điện thoại thật để so sánh song song mà không sợ bị đè cơ sở dữ liệu Room hay Cache.

---

## 3. Cấu hình Tham số Môi trường Tùy chỉnh

Bạn có thể ghi đè các tham số môi trường trong file `local.properties` hoặc `gradle.properties` của máy cục bộ mà không làm thay đổi git tracking:

```properties
# Ghi đè URL API cho từng môi trường
CHANKO_API_BASE_URL_DEV=https://my-custom-dev-server.internal/api/v1/
CHANKO_API_BASE_URL_STG=https://staging.mycompany.com/api/v1/
CHANKO_API_BASE_URL=https://api.chanko.app/api/v1/

# Cấu hình OAuth Client ID tương ứng
CHANKO_GOOGLE_WEB_CLIENT_ID=your-google-client-id-here.apps.googleusercontent.com
```

---

## 4. Cơ chế Hoạt động Đặc thù của Môi trường Dev

### 4.1. Tự Động Bypass Đăng Nhập (`DevFakeAuthenticationRepository`)
Ở flavor `dev`, lớp [DevFakeAuthenticationRepository](file:///Users/buihoa/AndroidStudioProjects/chanko/app/src/main/java/com/hoabh/chanko/dev/DevFakeAuthenticationRepository.kt) được tự động inject vào `AuthenticationRepository`.
* Ứng dụng tự động khôi phục phiên đăng nhập hợp lệ với định danh `dev-user`.
* Màn hình Splash sẽ tự động chuyển thẳng vào Dashboard/HomeScreen mà không yêu cầu đăng nhập.
* Nếu truy cập màn hình Login và nhấn Đăng nhập bằng Google, tiến trình sẽ thành công ngay lập tức mà không cần mạng hoặc Google Play Services.

### 4.2. Tự Động Nạp Dữ Liệu Mẫu (`DevDataSeeder`)
Khi khởi chạy bản `devDebug`, nếu Room Database chưa có dữ liệu, [DevDataSeeder](file:///Users/buihoa/AndroidStudioProjects/chanko/app/src/main/java/com/hoabh/chanko/dev/DevDataSeeder.kt) sẽ tự động nạp sẵn 3 món đồ:
1. **Tai nghe Sony WH-1000XM5** (6,500,000 đ): Đã kết thúc thời gian hạ nhiệt $\rightarrow$ xuất hiện ngay trong mục *Sẵn sàng xem lại*.
2. **Bàn phím cơ NuPhy Air75** (2,800,000 đ): Đang đếm ngược thời gian $\rightarrow$ xuất hiện trong mục *Đang hạ nhiệt*.
3. **Đồng hồ thông minh Smartwatch** (4,500,000 đ): Đã bỏ qua $\rightarrow$ cộng dồn vào thống kê *Đã tiết kiệm*.

Lập trình viên có thể kiểm thử toàn bộ tương tác của `HomeScreen`, `SetupScreen`, dialogs và bottom bar ngay từ giây đầu tiên mở app.
