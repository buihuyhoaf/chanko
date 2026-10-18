# Initial Setup — giao diện tiếng Việt

18/10/2026. Nhánh `feature/initial-setup-vietnamese` từ `origin/dev-chanko-1x` (`d290641`, gồm Phase 6 vừa merge trong lúc triển khai).

Triển khai theo visual đã duyệt trong chat, SETUP-01…05 và UI UX DS v1.0. Hai ảnh scene được giữ nguyên từ thiết kế: mèo trắng ở xưởng với lọ tiền/sổ và lịch tím. Chữ là native Compose/Nunito, không nằm trong bitmap. Hai bước: ngân sách → ngày bắt đầu chu kỳ, không Skip/Welcome/Ready.

## Đã triển khai

- Toàn bộ nội dung Initial Setup bằng tiếng Việt trong `values/strings_setup.xml`, kể cả semantics, validation, loading, offline, Retry và trạng thái chưa xác nhận.
- Ngân sách bắt buộc >0, số nguyên VND, không thêm min/max. Giữ dạng chuỗi chữ số để không áp đặt `Long` limit trước khi có contract; không dùng floating point. Định dạng hiển thị nhóm nghìn bằng dấu chấm với OffsetMapping để nhập/chỉnh sửa bằng con trỏ. Paste số âm/thập phân không được biến thành giá trị dương.
- Chọn rõ ngày 1–31, không mặc định. Bottom sheet cuộn, mỗi hàng >=56dp, selected semantics. Cảnh báo ngày cố định và quy tắc tháng thiếu ngày xuất hiện trước nút lưu.
- Back từ bước 2 giữ cả hai giá trị. Draft ở ViewModel sống qua xoay màn hình; không tuyên bố giữ draft qua process death. Khởi động lại phải kiểm tra server, không tin cờ completed trong saved state/cache. Draft và pending operation bị hủy khi account subtree rời UI (ngoại trừ configuration change).
- Scroll cả form và CTA, safe drawing + IME insets, hero giảm chiều cao khi IME mở hoặc chữ lớn. Touch >=48dp. Lỗi được đọc bằng live region. Không ellipsis copy cảnh báo/CTA.
- Múi giờ vùng IANA của thiết bị ở lúc gửi. Không thêm selector hay offset tự suy ra. Không tính ngày chu kỳ đầu.
- Cổng trạng thái đặt trong authenticated subtree. Chỉ `COMPLETE` từ kiểm tra server mới render `MainNavigation`; Google Sign-In hoặc submit accepted không đủ. Unknown/error/unavailable có Thử lại. Response đến muộn sau hủy không mở Home.

## Ranh giới tích hợp (chưa sẵn sàng production)

`SetupGateway` là seam presentation để kiểm thử/tiếp nhận adapter sau, KHÔNG phải API contract được duyệt. Không có endpoint, payload JSON hay thuật toán chu kỳ đầu mới. `SetupDraft` chỉ là input presentation chưa serialize. `SetupSubmission.ACCEPTED` và `UNKNOWN` luôn kéo theo kiểm tra status; retry sau phản hồi không rõ phải reconcile trước khi có thể gửi tiếp. `ERROR`/`OFFLINE` chỉ được adapter tương lai dùng khi biết chắc chưa commit; mọi phản hồi có thể đã commit phải là `UNKNOWN`.

Adapter production hiện là `UnconfiguredSetupGateway`, luôn trả UNAVAILABLE: sau auth, app sẽ hiển thị chưa thể xác nhận + Thử lại, không cho đi vòng vào Home. Đây là giới hạn thật của bản UI, không phải tích hợp backend hoàn chỉnh. Cần chốt API status/submit, idempotency, precision và quy tắc chu kỳ đầu rồi mới bật luồng end-to-end. Vì vậy PR để draft.

Authentication/OAuth hiện hữu không được thay đổi trong nhánh này. Không thêm fake login hoặc fake server success. Không sửa Room schema, các protected skill hoặc sources/.

## Xem giao diện trực tiếp

Bản debug có launcher riêng **Xem thử thiết lập** (`SetupReviewActivity`). Nó chỉ hiển thị form và báo dịch vụ chưa sẵn sàng khi nhấn lưu. Dòng “Bản xem thử giao diện · chưa kết nối máy chủ” luôn có mặt. Không thể chuyển Home. Activity/launcher này chỉ có trong `src/debug`, không nằm trong release. Các `@Preview` hỗ trợ 393×852 và 320×568 fontScale 2.

Visual foreground dùng các màu đề xuất trong mockup đã review, gồm text #30233E, secondary #62596D, onPrimary #15131D trên brand #8965F6. DS PV-01 vẫn mở, không gắn nhãn toàn bộ palette đã approved. Scene là PNG opaque tĩnh, chưa có layer alpha/3D rig riêng.

## Kiểm chứng

Xem báo cáo `initial-setup-validation.md` cùng thư mục. Không coi dòng BUILD SUCCESSFUL của Gradle khi thiết bị từ chối cài test là device PASS; chỉ kết quả test thực tế mới được tính.
