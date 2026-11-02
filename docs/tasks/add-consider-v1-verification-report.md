# Add & Consider — báo cáo triển khai luồng không bản nháp

Ngày: 29/10/2026. Nhánh: `feature/add-consider-flow`.
Trạng thái: UX approved · integration pending.

## Nguồn và thay đổi phạm vi

Theo cập nhật mới nhất trong `docs/features/add-and-consider-spec.md`, `tasks/plan.md`, `tasks/todo.md` và chỉ thị người dùng: URL chuyển thành dialog; trích xuất thành công mở Verify, thất bại hỏi chuyển sang Manual; bãi bỏ ADD-03 A / ADD-05 A về bản nháp. Bản HTML bàn giao cũ chỉ còn là nguồn visual cho form/card/Quick Check/Result, không còn quyết định entry point hoặc persistence.

## Đã triển khai

- (+) trên Bottom Bar và các hành động thêm món từ Home mở `UrlInputDialog` ngay trên màn hình hiện tại. Không mở trang nhập URL toàn màn hình.
- Dialog có URL, Hủy và Lấy thông tin; Nút Lấy thông tin bấm được cả khi URL trống/sai theo yêu cầu kiểm tra UI mới nhất; các trường hợp này mở popup chuyển nhập thủ công, không gọi service. Chỉ URL http/https có host được gửi đến service. Loading vô định, chống gửi lặp; Hủy vẫn đóng được khi đang trích xuất.
- `UrlExtractionService` là integration seam, không coi là DTO backend đã duyệt. Binding ứng dụng trả `ExtractionResult.Failed` khi chưa có server.
- Lỗi mở `ExtractionFailedDialog` với copy trong đặc tả. Hủy đóng popup và giữ nguyên URL; Xác nhận đóng cả hai overlay, mở `ManualItemInputScreen`.
- Success và partial success đóng dialog, mở `VerifyItemInfoScreen`, điền phần có dữ liệu. Không tự xác nhận giá, không tự điền lý do người dùng.
- Verify và Manual dùng chung bộ field tiếng Việt: tên/giá/danh mục/lý do bắt buộc, một ảnh tùy chọn, giá Long VND >0 có xác nhận rõ ràng. Chỉnh thông tin làm mất xác nhận và assessment phụ thuộc.
- Trạng thái chỉ trong ViewModel của phiên đang mở: không Room, DataStore, SavedStateHandle hay khôi phục form sau process death. Ảnh không yêu cầu persistable URI permission cho nháp.
- Cancel/Back/thoát hủy request và reset toàn bộ form/answers/assessment; mở lại là phiên trắng với save key mới. Response cũ không được ghi đè phiên mới; đổi tài khoản hủy phiên cũ.
- Đã gỡ Entity/DAO/database/repository/codec/schema nháp và cấu hình KSP schemaLocation từng thêm. `core/database/build.gradle.kts` trở lại nội dung ban đầu; không sửa AppDatabase, bảng món đồ hay migration legacy.
- Gỡ thẻ tiếp tục nháp, event/effect, capability/model và fixture nháp trên Home. Không có lối khôi phục nháp cũ trong UI.
- Giữ giao diện Quick Check, ba verdict, WAIT Apply/None, duplicate/variant sheet và các preview. Fixture/mock chỉ nằm trong preview/test.
- Quick Check có contract xử lý question/result/early và giới hạn tối đa ba câu; response phải khớp revision. Không tự phát minh quy tắc tài chính.

## Phần còn pending khi chưa có server

- Trích xuất thật, canonical URL/duplicate matching, dữ liệu variant thật.
- Question engine, quy tắc verdict, budget snapshot và cooling duration/timezone.
- Lưu chính thức Item + Decision + History, xác nhận giao dịch, retry/idempotency/unknown outcome và điều hướng Item Detail sau save.
- Binding runtime của question engine hiện Unavailable; không trả BUY/WAIT/SKIP giả. Nút lưu chính thức vẫn disabled, không tạo món hay báo lưu thành công bằng dữ liệu mẫu. Dữ liệu đang nhập bị hủy khi thoát, đúng chính sách mới.
- Sheet chỉnh duration hiện thông báo chưa hỗ trợ, không tự áp dụng số ngày.

## Xác minh

- `./gradlew :app:assembleDevDebug :app:testDevDebugUnitTest :core:data:testDebugUnitTest --no-configuration-cache`: PASS.
- App: 191 tests, 0 failures, 0 errors, 0 skipped.
- Core data: 43 tests, 0 failures, 0 errors, 0 skipped. Ba test repository nháp cũ đã được gỡ cùng phần persistence bị bãi bỏ.
- Bộ kiểm thử Add & Consider: 4 test validation/model và 18 test ViewModel. Bao gồm URL sai không gọi service; lỗi/Hủy giữ URL; Xác nhận mở Manual; success/partial mở Verify chưa xác nhận giá; hủy/xóa phiên; tạo ViewModel mới không restore; single-flight; exception; stale/canceled response; unavailable; early không đoán đáp án; đổi tài khoản; revision lỗi; câu hỏi vượt ba.
- Home test được cập nhật theo việc bỏ chỉ báo nháp, giữ kiểm tra observation của S09/S10/S11.
- `git diff --check`: PASS.
- Lint trước đó còn 8 lỗi ngoài phạm vi (HomeDomainLogic NewApi và BudgetRoute LocalContextGetResourceValueCall); chưa chạy lại lint trong lượt thay đổi này.

## Giới hạn kiểm chứng

Chỉ build/unit test; không cài/chạy thiết bị hoặc emulator. Android runtime, dialog/navigation thực tế, ảnh, font/IME, TalkBack/Switch Access và pixel parity chưa xác minh. Compose previews đã compile, chưa render kiểm tra bằng mắt. Các gói thiết kế và ba tài liệu do người dùng cập nhật không được thêm vào staging trong lượt này.

Cập nhật nút Lấy thông tin: build và 191 app unit tests PASS; URL trống/sai vẫn mở được popup, Xác nhận vào Manual. Nút chỉ khóa khi đang xử lý hoặc popup lỗi đang mở.
