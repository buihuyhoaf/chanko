# Feature-Driven Presentation Architecture (Chuyển đổi Screens sang Features)

## Problem Statement
Làm thế nào để tổ chức tầng Presentation của Chanko từ tư duy màn hình đơn lẻ (`screens/`) sang tư duy tính năng người dùng (`features/`), nhằm hỗ trợ các tính năng đa màn hình (Multi-screen features như Chat, Add Item, Auth) và sẵn sàng cho việc phân tách Feature Modules trong tương lai mà không gây lộn xộn hay trùng lặp mã nguồn?

---

## Recommended Direction

Áp dụng mô hình **Feature-Driven Hierarchy** cho toàn bộ tầng Presentation (`com.hoabh.chanko.presentation.features.*`):

1. **Thay thế package gốc**: Đổi tên `presentation.screens` thành `presentation.features`.
2. **Phân loại 2 cấp độ tính năng**:
   - **Single-Screen Feature** (Tính năng 1 màn hình: `home`, `archived`, `transaction`):
     - Đặt phẳng bộ ba cốt lõi: `<Feature>Screen.kt`, `<Feature>ViewModel.kt`, `<Feature>UiState.kt` tại root của feature.
     - Folder con tùy chọn: `components/` (khi có $\ge 2$ Composable con tách file), `mapper/` (khi có pure UI layout/mapping helper phức tạp).
   - **Multi-Screen Feature** (Tính năng chuỗi hành trình nhiều màn hình: `chat`, `additem`, `setup`, `profile`):
     - Mỗi màn hình con là một sub-package độc lập chứa Screen, ViewModel, UiState của riêng màn hình đó.
     - `components/`: Chứa các Composable dùng chung cho toàn bộ feature (ví dụ: bong bóng chat, thanh soạn thảo tin nhắn).
     - `navigation/`: Chứa Nested Navigation Graph nội bộ của feature (`<Feature>NavGraph.kt`), đóng gói toàn bộ routes và transitions của feature đó.

---

## Key Assumptions to Validate
- [ ] Việc đổi `presentation.screens` sang `presentation.features` có thể thực hiện thông qua rename package an toàn mà không làm gãy Hilt DI hay Navigation.
- [ ] Các feature đa màn hình (`chat`, `additem`) sau khi phân cấp theo sub-packages sẽ dễ đọc, giảm kích thước file và tránh lộn xộn.
- [ ] Tất cả unit test và compilation verification (`compileDevDebugKotlin`, `compileProdReleaseKotlin`) duy trì 100% pass rate.

---

## MVP Scope (Phạm vi Triển khai)
1. **Tài liệu hóa & Quy chuẩn**:
   - Cập nhật [CODING_STANDARDS.md](../../CODING_STANDARDS.md) mục 1 và mục 3–4 để ghi nhận kiến trúc `features/` thay thế `screens/`.
2. **Refactoring Codebase**:
   - Đổi tên thư mục `app/src/main/java/com/hoabh/chanko/presentation/screens/` $\to$ `.../presentation/features/`.
   - Cấu trúc lại feature đa màn hình:
     - `chat/`: Tách thành `list/`, `detail/`, `newchat/`, `components/`.
     - `additem/`: Tách rõ `entry/`, `flow/`, `components/`.
   - Cập nhật toàn bộ các file Navigation (`NavGraph.kt`, `MainNavigation.kt`), Views, ViewModels và Unit Tests.
3. **Kiểm tra hồi quy**: Chạy `./gradlew testDebugUnitTest` và `./gradlew assembleDevDebug`.

---

## Not Doing (and Why)
- **Không tạo folder `domain/` hay `data/` trong feature của `:app`**: Mọi nghiệp vụ ứng dụng và database persistence phải ở `:core:domain` và `:core:data`. Trong feature presentation chỉ có UI mappers/models.
- **Không tạo folder `viewmodel/` đơn lẻ cho các màn hình 1 file**: Giữ Screen và ViewModel cùng cấp để giảm độ sâu cây thư mục.
- **Không tách ngay thành Gradle Module `:feature:*` trong đợt này**: Trước mắt chuẩn hóa cấu trúc thư mục package trong `:app`, chuẩn bị sẵn sàng cho việc trích xuất module sau.

---

## Open Questions
- Không còn câu hỏi mở. Hướng đi đã được thống nhất.
