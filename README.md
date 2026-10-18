# Chanko

Ứng dụng Android giúp hạ nhiệt mua sắm: lưu món đồ muốn mua, chờ hết thời gian cân nhắc, rồi chọn mua hoặc bỏ qua. Giá trị các món bỏ qua được cộng vào két tiết kiệm.

## Kiến trúc

Một module `app`, Kotlin, Compose Material 3, Navigation Compose, Hilt và Room.

- `presentation/screens`: Route thu thập state bằng `collectAsStateWithLifecycle`; Screen nhận state và callback để hiển thị UI.
- `OverviewViewModel`: dữ liệu Dashboard/Vault. `AddItemViewModel`: form và thao tác thêm. `ItemViewModel`: một món đồ, đồng hồ và quyết định.
- `domain`: model thuần Kotlin và hợp đồng repository.
- `data`: entity Room, mapping, DAO và repository implementation.
- `di`: cấu hình Hilt. `ui/theme`: màu sắc và typography.

Luồng xác thực: Splash → Google Sign-In → xác nhận Initial Setup từ server → hai bước thiết lập hoặc Home. Adapter setup hiện chưa cấu hình nên cổng server hiển thị lỗi có Thử lại; không suy đoán hoàn tất từ cache. Xem [Initial Setup tiếng Việt](docs/features/initial-setup.md). APK chỉ có biểu tượng Chanko, không có launcher xem thử riêng.

Các màn nghiệp vụ Dashboard/Thêm món/Chi tiết/Quyết định/Vault có source kế thừa; bốn tab hiện là foundation placeholders, chưa được nối trọn luồng production.

## Quy tắc dữ liệu

- Giá VND là `Long`, không dùng số thực. Tên không rỗng, giá dương, thời gian nhập từ 1 đến 8760 giờ.
- Thời gian còn lại được tính từ timestamp lưu trong database, không phụ thuộc số lần mở app.
- DAO chỉ cho chuyển một món đang hạ nhiệt sang trạng thái quyết định khi đã hết hạn; cập nhật có điều kiện để tránh ghi đè quyết định trước đó.
- Database v2 tách entity khỏi domain, chuyển giá REAL sang INTEGER bằng cách làm tròn tới đồng gần nhất. Migration 1→2 giữ các trường còn lại; không dùng destructive migration.
- Thông báo chạy nền, chọn ảnh và quy đổi giá sang giờ công chưa được triển khai. Chưa có nâng cấp đồng loạt phiên bản thư viện.

## Kiểm tra

Yêu cầu JDK 17, Android SDK theo `app/build.gradle.kts`.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-configuration-cache
./gradlew :app:assembleDebugAndroidTest --no-configuration-cache
# Khi có thiết bị hoặc emulator:
./gradlew :app:connectedDebugAndroidTest --no-configuration-cache
```

Unit test kiểm tra ranh giới thời gian, quyết định lặp, validation, lỗi đọc/ghi, retry và chống gửi form hai lần. Instrumentation test kiểm tra DAO, tổng tiết kiệm và migration database; cần thiết bị Android để chạy.

Smoke test thủ công: thêm món → kiểm tra Dashboard và chi tiết → đóng/mở app → kiểm tra đếm ngược; sau khi hết hạn, bỏ qua món và kiểm tra Vault. Kiểm tra thêm dark mode, xoay màn hình khi nhập form và nút quay lại.

## Git

Tạo nhánh từ `dev-chanko-1x`, PR về `dev-chanko-1x`. `main` dành cho bản ổn định.
