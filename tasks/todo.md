# Task List: Chat with Chanko (Android Frontend First)

## Giai đoạn 1: Assets & Design Tokens
- [ ] **Task 1.1**: Import các biểu cảm Mascot (8 poses) từ `08_ASSETS/expressions/` vào `app/src/main/res/drawable-nodpi/`.
- [ ] **Task 1.2**: Import icons và font Nunito từ `08_ASSETS/icons/` và `fonts/` vào `app/src/main/res/`.
- [ ] **Task 1.3**: Bổ sung Color Tokens & Typography cho Chat trong `app/src/main/java/com/hoabh/chanko/ui/theme/`.

## Giai đoạn 2: Domain Layer
- [ ] **Task 2.1**: Tạo domain models: `ChatThread`, `ChatMessage`, `DecisionVerdict`, `MascotEmotion`, `ThreadMode` trong `domain/chat/model/`.
- [ ] **Task 2.2**: Tạo interface `ChatRepository` và các models kết quả trong `domain/chat/repository/`.

## Giai đoạn 3: Data & Fake Fixtures
- [ ] **Task 3.1**: Tạo `FakeChatDataSource` mô phỏng đầy đủ 11 màn hình S01–S11.
- [ ] **Task 3.2**: Thiết lập Room Entity và DAO cho Chat (`ChatDatabase`, `ChatDao`).
- [ ] **Task 3.3**: Cài đặt `ChatRepositoryImpl` với cơ chế ưu tiên đọc cache Room khi offline.

## Giai đoạn 4: Reusable UI Components
- [ ] **Task 4.1**: Xây dựng `ChatMascotHeader` (hiển thị biểu cảm Chanko theo ngữ cảnh).
- [ ] **Task 4.2**: Xây dựng `ChatMessageBubble` (bong bóng chat Candy Pop 3D).
- [ ] **Task 4.3**: Xây dựng `ChatComposerBar` và `PrototypeBadge`.
- [ ] **Task 4.4**: Xây dựng `DecisionVerdictCard` (BUY / WAIT / SKIP card).

## Giai đoạn 5: Screens & Navigation
- [ ] **Task 5.1**: Xây dựng `ChatViewModel` (quản lý 11 UI states, draft tin nhắn, retry).
- [ ] **Task 5.2**: Xây dựng `ChatScreen` (tổng hợp Header, Message List, Composer, Verdict).
- [ ] **Task 5.3**: Xây dựng `ChatHistoryScreen` và `ItemPickerSheet` (S05).
- [ ] **Task 5.4**: Đấu nối Navigation từ Home CTA và Item Detail vào ChatScreen.

## Giai đoạn 6: Kiểm thử & Hoàn thiện
- [ ] **Task 6.1**: Viết Unit tests cho `ChatViewModel` và `ChatRepositoryImpl`.
- [ ] **Task 6.2**: Biên dịch `./gradlew :app:assembleDebug` và chạy unit tests verify.
