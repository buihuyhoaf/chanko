# Implementation Plan: Refactor Initial Setup Flow (Direct Form & Error Dialog)

## Overview
Cải tiến luồng màn hình **Initial Setup (`SetupScreen`)**:
1. Vào thẳng biểu mẫu thiết lập 2 bước (`FORM`) mà không chặn người dùng bằng trạng thái `CHECKING` hoặc toàn màn hình lỗi `"Chưa thể xác nhận thiết lập"` (`SetupStatusScreen`).
2. Khi người dùng hoàn thành 2 bước và nhấn **"Lưu và bắt đầu"**, app mới gửi request lưu và xác nhận trạng thái.
3. Nếu xảy ra lỗi trong quá trình lưu (offline, server lỗi, không sẵn sàng): giữ nguyên dữ liệu người dùng đã nhập trên form và hiển thị **AlertDialog** thông báo lỗi tiếng Việt (có tùy chọn "Thử lại" hoặc "Đóng"), thay vì chuyển sang màn hình lỗi toàn trang.

---

## Architecture & Design Decisions

1. **State & Event Flow**:
   - Trạng thái khởi tạo của `SetupUiState.page` sẽ là `SetupPage.FORM`.
   - Bỏ lệnh gọi tự động `checkStatus()` trong `init` của `SetupViewModel`.
   - Thêm trạng thái Dialog vào `SetupUiState`:
     ```kotlin
     data class SetupUiState(
         val page: SetupPage = SetupPage.FORM,
         val step: Int = 1,
         val amount: String = "",
         val day: Int? = null,
         val amountError: Boolean = false,
         val dayError: Boolean = false,
         val busy: Boolean = false,
         val problem: SetupProblem? = null,
         val showErrorDialog: Boolean = false,
         val needsReconciliation: Boolean = false,
     )
     ```
2. **Handling Save Failure**:
   - Khi `save()` thất bại (hoặc kết quả kiểm tra reconciliation không thành công):
     - Giữ `page = SetupPage.FORM`.
     - Cập nhật `busy = false`, `problem = <SetupProblem>`, `showErrorDialog = true`.
   - Thêm hàm `dismissErrorDialog()` trong `SetupViewModel` để ẩn dialog khi user đóng hoặc chọn thử lại.
3. **Dialog UI Component**:
   - Tạo Compose Dialog component `SetupErrorDialog` sử dụng `AlertDialog` của Material3.
   - Text tiêu đề: *"Chưa thể lưu thiết lập"* (`R.string.setup_error_dialog_title`).
   - Text nội dung: Tương ứng với từng `SetupProblem` (ngoại tuyến, máy chủ chưa sẵn sàng, lỗi lưu,...).
   - Nút hành động:
     - Nút chính: *"Thử lại"* (`R.string.setup_retry`) $\rightarrow$ đóng dialog và gọi lại `onSave()`.
     - Nút phụ: *"Đóng"* (`R.string.setup_close`).

---

## Task Breakdown

### Phase 1: Foundation & ViewModel Logic Update
- [ ] **Task 1: Resource Strings & UI State Update**
- [ ] **Task 2: Refactor `SetupViewModel` & Unit Tests**

### Checkpoint 1
- [ ] Unit tests `SetupViewModelTest` pass 100%.

---

### Phase 2: UI Implementation & Integration
- [ ] **Task 3: Implement `SetupErrorDialog` & Integrate with `SetupScreen`**
- [ ] **Task 4: Update Existing Tests & Verification**

### Checkpoint 2 (Hoàn tất)
- [ ] Build dự án thành công (`./gradlew assembleDebug`).
- [ ] Tất cả unit tests đều pass.
