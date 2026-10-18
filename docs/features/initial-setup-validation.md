# Kiểm chứng Initial Setup tiếng Việt

18/10/2026, Asia/Ho_Chi_Minh. Đây là kiểm chứng cục bộ của tác giả, không phải independent review hoặc remote CI.

- `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest --offline --no-configuration-cache`: PASS. 36 app unit tests sau khi rebase lên d290641 (Phase 6), 0 failures/errors, gồm 6 test SetupViewModel mới. Lint 0 errors/15 warnings (không thêm suppression hoặc nâng dependency).
- Android 16/API 36 arm64 emulator, 393×851dp: runner thực trả `OK (5 tests)` cho InitialSetupScreenTest và SplashNavigationTest. Kiểm tra nhập ngân sách, chọn rõ ngày 31, Back giữ draft, chữ 200% và gate không cho vào Home khi chưa xác nhận server.
- Cùng emulator ở 320×568dp, bật IME khi có hardware keyboard: runner thực trả `OK (3 tests)` cho InitialSetupScreenTest. Test chữ 200% cuộn được cảnh báo và CTA; luồng nhập liệu/chọn ngày/Back PASS.
- Đã xuất và xem ảnh Android thực `budget-vi.png`, `day-vi.png`, `day-font200-vi.png` trong `outputs/initial-setup-android-vi` của workspace cha. Ảnh compact 320dp kèm theo. Ảnh 200% chụp ở vị trí cuộn tới CTA nên không chứa toàn bộ header/scene.
- Preview HTML tiếng Việt: validation, Back giữ draft, error/offline/unknown/loading và 320px với mô phỏng 200% không overflow ngang toàn trang. Đây là kiểm tra preview bổ sung, không thay bằng chứng Android.
- `git diff --check`: PASS.

Lần chạy đầu trên điện thoại Android kết nối không thực thi test vì thiết bị chặn tùy chọn cấp quyền cài đặt, sau đó từ chối cài APK. Không thay cài đặt bảo mật điện thoại, không thử cài tiếp trên điện thoại; chuyển sang emulator riêng trong /tmp và tắt sau kiểm tra. Dòng BUILD SUCCESSFUL của lần chạy bị chặn không được tính là test pass.

Giới hạn: chưa kiểm tra TalkBack thực, landscape, process death hoặc backend end-to-end. API status/submit, precision, idempotency và chu kỳ đầu chưa chốt; production adapter luôn UNAVAILABLE và PR để draft. Không dùng trạng thái local để mở Home. Bản xem debug chỉ thể hiện UI, không cấp session hay server completion.

## Sửa launcher theo yêu cầu người dùng

Đã gỡ debug manifest, SetupReviewActivity và copy xem thử. Build `:app:assembleDebug` PASS; kiểm tra manifest APK bằng aapt: chỉ có một launchable activity là MainActivity, không còn SetupReviewActivity. Bản sửa này chưa kiểm tra runtime trên thiết bị thật và không thực hiện kiểm tra máy ảo. User nghiêm cấm cài/tạo/cấu hình/chạy máy ảo; quy tắc đã được lưu vào AGENTS.md. Bằng chứng emulator phía trên chỉ là lịch sử trước chỉ thị này, không phải phương pháp được phép cho công việc tiếp theo.
