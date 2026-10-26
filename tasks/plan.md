# Implementation Plan: Chat with Chanko (CHAT-UX-REV-01)

## Overview
Tái thiết kế toàn diện tính năng **Chat with Chanko** theo bản đặc tả **CHAT-UX-REV-01 (25/10/2026)**.
Thay thế kiến trúc cũ (S01–S11) bằng mô hình tinh gọn gồm **3 màn hình duy nhất**:
1. **CL (Chat List):** Màn quản lý danh sách cuộc trò chuyện kiểu Messenger với search, filter chips, hero banner và nút cố định `+ Đoạn chat mới`. (Không có composer).
2. **CD (Chat Detail):** Màn chi tiết hội thoại — **nơi duy nhất có ô gõ chat (Composer)**. Chứa timeline tin nhắn, Decision Card trung tính (Neutral BUY), Quick Replies (`FillComposer`), và liên kết món đồ cùng thread (Same-thread item bind).
3. **NC (New Chat):** Màn hình/Sheet chọn chế độ bắt đầu ("Trò chuyện tự do" hoặc "Về một món đồ" với inline item picker), nút CTA mở CD ở trạng thái chưa gửi. (Không có composer).

Ứng dụng phục hồi lại thanh BottomBar về **4 Tab nguyên bản** (`Trang chủ`, `Món đồ`, `Ngân sách`, `Phân tích`). Luồng Chat là nested flow xuất phát từ CTA trên màn Trang chủ (Home) hoặc "Cân nhắc cùng Chanko" trên Chi tiết món đồ (Item Detail).

---

## Architecture Decisions
- **3 Màn hình độc lập (CL / CD / NC):** Phân tách rõ ràng trách nhiệm; loại bỏ toàn bộ các màn Welcome riêng, History route riêng. Trạng thái rỗng, offline, lỗi, lưu trữ được đóng gói dưới dạng State/Dialog/Sheet.
- **Vị trí Composer:** Composer bar chỉ tồn tại duy nhất ở màn CD (Chat Detail). CL và NC tuyệt đối không có input chat.
- **Neutral BUY Decision Card:** Thẻ nhận định của Chanko sử dụng phong cách trung tính (nền lavender `#F0E9FF`, chữ tối độ tương phản cao, mascot Neutral, không màu xanh lá chiến thắng, không pháo hoa/sparkles).
- **Same-thread Item Bind:** Trong CD ở chế độ `FREE`, người dùng có thể liên kết món đồ có sẵn qua nút `+` trên Composer, giữ nguyên threadId và toàn bộ lịch sử tin nhắn.
- **Offline Retention:** Dữ liệu chat trong Room Database được bảo toàn đọc offline vô thời hạn (Hợp đồng A4). Khi offline, composer bị khóa và hiển thị lý do cần kết nối mạng.
- **Phục hồi 4 Tabs Shell:** Gỡ bỏ Tab Chat thứ 5 trên BottomBar; kết nối Chat qua entry point chuẩn trên Home và Item Detail.

---

## Task List & Phases

### Phase 1: Domain & Data Layer Updates (Foundation)
- [ ] **Task 1: Update Domain Models & ChatRepository Interface**
  - Cập nhật model `ChatThread`: thêm `updatedAt`, `previewText`, `linkedItemId`, `linkedItemName`, `status` (CONSIDERING, READY, PURCHASED).
  - Bổ sung hàm `renameThread(threadId: String, newTitle: String): Result<Unit>` vào `ChatRepository`.
- [ ] **Task 2: Update Room Database, DAO & Fake Repository**
  - Cập nhật `ChatThreadEntity` và `ChatDao` hỗ trợ lưu `previewText`, cập nhật tiêu đề (Rename), lọc theo trạng thái.
  - Cập nhật `FakeChatDataSource` và `ChatRepositoryImpl` cung cấp danh sách threads mẫu kiểu Messenger phong phú, hỗ trợ rename local.
- [ ] **Checkpoint 1: Foundation Verify**
  - Biên dịch và unit tests cho domain/data layer pass 100%.

### Phase 2: Shared UI Components (Candy Pop 3D / Neutral BUY)
- [ ] **Task 3: Refactor DecisionCard & ChatMessageBubble**
  - Cập nhật `DecisionCard`: áp dụng phong cách trung tính (Neutral BUY, nền lavender, mascot Neutral, nút "Xem món đồ", loại bỏ màu xanh lá và sparkles).
  - Cập nhật `ChatMessageBubble`: bong bóng User (chữ tối trên lavender `#F0E9FF`), bong bóng Chanko (avatar Chanko 32dp + bong bóng trắng).
- [ ] **Task 4: Build Dialogs & Sheets (Rename, Archive, Delete, Bind)**
  - Tạo `RenameThreadDialog` (tiêu đề, text input, nút Hủy/Lưu).
  - Tạo `ArchiveConfirmationDialog` và `DeleteConfirmationDialog` (cảnh báo độc lập với official records).
  - Tạo `SameThreadItemPickerSheet` cho CD (tìm kiếm món đồ, xác nhận liên kết cùng thread).
- [ ] **Checkpoint 2: Components Verify**
  - Unit tests / Component render verification.

### Phase 3: Screen 1 — CL (Chat List Screen)
- [ ] **Task 5: Build ChatListScreen UI**
  - Header: Tiêu đề `"Chat với Chanko"`, subtitle `"Cùng suy nghĩ kỹ hơn trước mỗi quyết định"`, nút More (bộ lọc "Đã lưu trữ").
  - Hero Card: Chanko mascot 148dp contain, gradient pastel.
  - Search bar: Placeholder `"Tìm cuộc trò chuyện"`.
  - Filter chips: `"Tất cả"`, `"Đang cân nhắc"`, `"Chờ xem lại"`, `"Đã mua"`.
  - Thread cards list: Thumbnail 56dp, tiêu đề 17sp bold, preview 14sp, timestamp, type tag. Long press / More mở menu (Đổi tên, Lưu trữ, Xóa, Xem món đồ).
  - Fixed Footer CTA: `"+ Đoạn chat mới"` (56dp height, bo góc 20dp) ghim ở đáy an toàn.
  - Không có ô gõ tin nhắn (composer) tại CL.
- [ ] **Task 6: Build ChatListViewModel**
  - Quản lý state danh sách threads, tìm kiếm theo tên thread/món đồ, lọc theo chip trạng thái, xử lý rename/archive/delete thread.
- [ ] **Checkpoint 3: Chat List Verify**
  - Unit tests cho `ChatListViewModel` và kiểm tra giao diện CL.

### Phase 4: Screen 2 — NC (New Chat Screen / Sheet)
- [ ] **Task 7: Build NewChatScreen UI & Logic**
  - Header: Tiêu đề `"Đoạn chat mới"`, subtitle `"Chọn cách bắt đầu"`, nút Đóng (`Close`).
  - 2 Thẻ chọn chế độ: Thẻ 1 `"Trò chuyện tự do"`, Thẻ 2 `"Về một món đồ"`.
  - Inline Item Picker: Xuất hiện khi chọn thẻ 2, có ô tìm kiếm và danh sách món đồ kèm radio button chọn 1 món duy nhất.
  - Fixed Footer CTA: `"Bắt đầu trò chuyện"` hoặc `"Trò chuyện về [tên món đồ]"`.
  - Nhấn CTA mở màn CD ở trạng thái chưa gửi (unsent). Không có composer ở NC.
- [ ] **Checkpoint 4: New Chat Verify**
  - Kiểm tra flow chọn mode và inline picker hoạt động chính xác.

### Phase 5: Screen 3 — CD (Chat Detail Screen)
- [ ] **Task 8: Refactor ChatDetailScreen UI**
  - App Bar: Nút Back, avatar/tên món đồ hoặc "Trò chuyện tự do", nút More thread.
  - Dòng provenance: `"Đã liên kết với [tên món đồ] từ [thời điểm]"`.
  - Timeline tin nhắn: LazyColumn bong bóng User, Chanko, Decision Card, trạng thái Chanko đang suy nghĩ.
  - Quick Replies: Hàng chip gợi ý, khi nhấn chỉ điền vào composer (`FillComposer`), không tự động gửi.
  - Composer Bar: Ô gõ tin nhắn trên bàn phím, nút Gửi, nút `+` liên kết món đồ (nếu đang ở mode FREE).
  - Xử lý trạng thái Offline banner và Archived banner (kèm nút Khôi phục).
- [ ] **Task 9: Refactor ChatDetailViewModel**
  - Hỗ trợ khởi tạo thread chưa gửi (unsent state), tạo thread khi gửi tin nhắn đầu tiên, gửi tin nhắn tiếp theo, liên kết món đồ (same-thread bind).
- [ ] **Checkpoint 5: Chat Detail Verify**
  - Unit tests cho `ChatDetailViewModel` (unsent flow, sending, same-thread bind, quick reply FillComposer).

### Phase 6: Shell Navigation & System Integration
- [ ] **Task 10: Restore 4-Tab BottomBar & Add Home CTA**
  - Phục hồi `TopLevelDestination` và `ChankoBottomBar` về 4 Tabs (`Trang chủ`, `Món đồ`, `Ngân sách`, `Phân tích`).
  - Thêm nút CTA Chat trên màn hình `HomeScreen` để mở màn `ChatListScreen`.
- [ ] **Task 11: Connect End-to-End Navigation Graph**
  - Cấu hình route: `Home` -> `CL` -> `CD`, `CL` -> `NC` -> `CD`, `ItemDetail` -> `CD`.
  - Xử lý Back stack: Back từ CD về CL hoặc Item Detail chuẩn xác; phục hồi trạng thái cuộn/tìm kiếm tại CL.
- [ ] **Checkpoint 6: Final Verification**
  - `./gradlew :app:assembleDevDebug` thành công không lỗi.
  - `./gradlew :app:testDevDebugUnitTest` toàn bộ unit tests pass 100%.
  - Commit git với thông điệp chuẩn mực.
