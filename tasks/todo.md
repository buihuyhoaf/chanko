# Task List: Chat with Chanko (CHAT-UX-REV-01)

## Phase 1: Domain & Data Layer Updates (Foundation)

### Task 1: Update Domain Models & ChatRepository Interface
- [x] Cập nhật model `ChatThread`: thêm `updatedAt`, `previewText`, `linkedItemId`, `linkedItemName`, `status`
- [x] Bổ sung hàm `renameThread(threadId: String, newTitle: String): Result<Unit>` vào interface `ChatRepository`
- [x] Verification: `./gradlew :core:domain:compileKotlin` pass

### Task 2: Update Room Database, DAO & Fake Repository
- [x] Cập nhật `ChatThreadEntity` & `ChatDao` hỗ trợ lưu `previewText`, cập nhật tiêu đề (`renameThread`)
- [x] Cập nhật `FakeChatDataSource` và `ChatRepositoryImpl` với dữ liệu threads mẫu đa dạng kiểu Messenger
- [x] Verification: `./gradlew :core:database:compileDebugKotlin :core:data:compileDebugKotlin` pass

### Checkpoint 1: Foundation Verify
- [x] Toàn bộ code domain và data layer biên dịch sạch sẽ
- [x] `./gradlew :core:data:testDebugUnitTest` pass 100%

---

## Phase 2: Shared UI Components (Candy Pop 3D / Neutral BUY)

### Task 3: Refactor DecisionCard & ChatMessageBubble
- [x] Refactor `DecisionCard`: áp dụng Neutral BUY (nền `#F0E9FF`, chữ tối, mascot Neutral, nút "Xem món đồ", bỏ xanh lá và sparkles)
- [x] Refactor `ChatMessageBubble`: User bubble (chữ tối trên lavender `#F0E9FF`), Chanko bubble (avatar 32dp + bong bóng trắng)
- [x] Verification: `./gradlew :app:compileDevDebugKotlin` pass

### Task 4: Build Dialogs & Sheets (Rename, Archive, Delete, Bind)
- [x] Xây dựng `RenameThreadDialog` (tiêu đề, text field, nút Hủy/Lưu)
- [x] Xây dựng `ArchiveConfirmationDialog` và `DeleteConfirmationDialog`
- [x] Xây dựng `SameThreadItemPickerSheet` cho CD (tìm kiếm món đồ, xác nhận liên kết cùng thread)
- [x] Verification: `./gradlew :app:compileDevDebugKotlin` pass

### Checkpoint 2: Components Verify
- [x] Các thành phần UI dùng chung hoàn thiện và biên dịch thành công

---

## Phase 3: Screen 1 — CL (Chat List Screen)

### Task 5: Build ChatListScreen UI
- [ ] Xây dựng Header "Chat với Chanko" (title 24sp bold) + Subtitle + nút More
- [ ] Xây dựng Hero Card với Chanko mascot 148dp contain
- [ ] Xây dựng Search bar ("Tìm cuộc trò chuyện") và Filter chips ("Tất cả", "Đang cân nhắc", "Chờ xem lại", "Đã mua")
- [ ] Xây dựng danh sách Thread cards (thumbnail 56dp, title 17sp, preview 14sp, time, tag)
- [ ] Xây dựng Fixed Footer CTA `+ Đoạn chat mới` (56dp height, bo góc 20dp)
- [ ] Đảm bảo KHÔNG có ô gõ tin nhắn (composer) tại CL
- [ ] Verification: `./gradlew :app:compileDevDebugKotlin` pass

### Task 6: Build ChatListViewModel
- [ ] Quản lý state danh sách threads, tìm kiếm, lọc chip, rename/archive/delete thread
- [ ] Viết unit tests cho `ChatListViewModel`
- [ ] Verification: `./gradlew :app:testDevDebugUnitTest` pass

### Checkpoint 3: Chat List Verify
- [ ] Màn hình CL và ViewModel hoạt động ổn định, 100% test pass

---

## Phase 4: Screen 2 — NC (New Chat Screen / Sheet)

### Task 7: Build NewChatScreen UI & Logic
- [ ] Xây dựng Header: Title "Đoạn chat mới", Subtitle "Chọn cách bắt đầu", nút Close
- [ ] Xây dựng 2 Thẻ chọn chế độ: "Trò chuyện tự do" và "Về một món đồ"
- [ ] Xây dựng Inline Item Picker khi chọn thẻ 2 (tìm kiếm + danh sách chọn 1 món)
- [ ] Xây dựng Fixed Footer CTA "Bắt đầu trò chuyện" / "Trò chuyện về [tên món đồ]"
- [ ] Đảm bảo KHÔNG có ô gõ tin nhắn (composer) tại NC
- [ ] Verification: `./gradlew :app:compileDevDebugKotlin` pass

### Checkpoint 4: New Chat Verify
- [ ] Màn hình NC hoàn thiện, hỗ trợ chọn mode và mở CD ở trạng thái chưa gửi

---

## Phase 5: Screen 3 — CD (Chat Detail Screen)

### Task 8: Refactor ChatDetailScreen UI
- [ ] Xây dựng App Bar: nút Back, avatar/tiêu đề, nút More thread
- [ ] Xây dựng Provenance bar (khi đã bind món đồ)
- [ ] Xây dựng Message Timeline (bong bóng User/Chanko, Neutral Decision Card, thinking indicator)
- [ ] Xây dựng Quick Replies chips (khi chạm chỉ điền vào composer `FillComposer`, không tự gửi)
- [ ] Xây dựng Composer Bar (gắn trên bàn phím, nút Gửi, nút `+` liên kết món đồ ở mode FREE)
- [ ] Xử lý trạng thái Offline banner và Archived banner
- [ ] Verification: `./gradlew :app:compileDevDebugKotlin` pass

### Task 9: Refactor ChatDetailViewModel
- [ ] Hỗ trợ trạng thái chưa gửi (unsent state), tạo thread khi gửi tin đầu tiên, gửi tin tiếp theo
- [ ] Hỗ trợ liên kết món đồ cùng thread (same-thread item bind)
- [ ] Viết unit tests cho `ChatDetailViewModel`
- [ ] Verification: `./gradlew :app:testDevDebugUnitTest` pass

### Checkpoint 5: Chat Detail Verify
- [ ] Màn hình CD và ViewModel hoạt động chuẩn xác, 100% test pass

---

## Phase 6: Shell Navigation & System Integration

### Task 10: Restore 4-Tab BottomBar & Add Home CTA
- [ ] Phục hồi `TopLevelDestination` và `ChankoBottomBar` về đúng 4 Tabs (`Trang chủ`, `Món đồ`, `Ngân sách`, `Phân tích`)
- [ ] Thêm nút CTA Chat trên màn hình `HomeScreen` để mở `ChatListScreen`
- [ ] Verification: Bottom navigation hiển thị 4 tabs chuẩn

### Task 11: Connect End-to-End Navigation Graph
- [ ] Cấu hình điều hướng: `Home` -> `CL` -> `CD`, `CL` -> `NC` -> `CD`, `ItemDetail` -> `CD`
- [ ] Xử lý Back stack: Back từ CD về đúng màn nguồn (CL hoặc Item Detail), phục hồi query/scroll tại CL
- [ ] Verification: `./gradlew :app:assembleDevDebug :app:testDevDebugUnitTest` pass 100%

### Checkpoint 6: Final Verification
- [ ] Toàn bộ ứng dụng build thành công không lỗi
- [ ] Toàn bộ unit tests vượt qua 100%
- [ ] Git commit sạch sẽ trên branch `feature/chat-with-chanko-ui`
