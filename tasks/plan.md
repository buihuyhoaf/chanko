# Implementation Plan: Chat with Chanko (Android Frontend First)

## Tổng quan
Triển khai tính năng **Chat with Chanko** trên Android bằng Kotlin + Jetpack Compose theo chuẩn Candy Pop 3D, hỗ trợ 11 màn hình (S01–S11) từ handoff v1.4, sử dụng Fake Fixtures / Deterministic Repository trước để kiểm chứng trải nghiệm và luồng nghiệp vụ.

---

## Các giai đoạn thực hiện (Phases)

### Giai đoạn 1: Assets & Design Tokens Foundation
1. Nhập khẩu (import) toàn bộ assets từ `08_ASSETS`:
   - 8 biểu cảm Mascot WebP/PNG (`concerned`, `curious`, `encouraging`, `neutral`, `relaxed`, `sleeping`, `thinking`, `welcome`) vào `res/drawable-nodpi/`.
   - Các icon SVG (`chat`, `history`, `more`, `trash`, `archive`, `reflect`, `send`...) vào `res/drawable/`.
   - Font `Nunito-Variable.ttf` vào `res/font/`.
2. Định nghĩa Design Tokens bổ trợ cho Chat UI (Candy Pop 3D chat bubbles, verdict colors `BUY`/`WAIT`/`SKIP`, elevation, shadows).

### Giai đoạn 2: Domain Layer & Models
1. Xây dựng Data Models thuần Kotlin:
   - `ChatThread`, `ChatMessage`, `MessageSender` (User, Chanko, System).
   - `DecisionVerdict` (BUY, WAIT, SKIP), `MascotEmotion` (8 poses whitelist).
   - `ContextRevision`, `ThreadMode` (FREE, ITEM).
2. Định nghĩa Repository Interfaces:
   - `ChatRepository`: đọc danh sách threads, messages, gửi tin, bind item, archive/delete thread.
   - `ShoppingItemReadRepository`: liên kết context món đồ hiện tại.

### Giai đoạn 3: Data Layer & Deterministic Fixtures (Fake Data Source)
1. Cài đặt `FakeChatDataSource` cung cấp dữ liệu mẫu đại diện cho 11 kịch bản màn hình (S01 Empty, S02 Free Chat, S03 Item Chat có Verdict, S04 History, S06 Offline, S08 Error, S11 Archived...).
2. Cài đặt Room Database & DAO cho Chat (Entity `ChatThreadEntity`, `ChatMessageEntity`) để kiểm chứng cache offline không bao giờ bị purge theo age.

### Giai đoạn 4: UI Components (Candy Pop 3D)
1. Mascot Header & Presenter Component: tự động mapping biểu cảm theo context màn hình.
2. Message Bubbles: bubble người dùng, bubble Chanko, bubble hệ thống (chuyển đổi FREE→ITEM).
3. Composer Bar: ô nhập tin nhắn, nút gửi, nút đính kèm món đồ, badge Prototype.
4. Structured Verdict Card: hiển thị kết quả phân tích mua sắm (`BUY`/`WAIT`/`SKIP`).
5. Quick Reply Chips.

### Giai đoạn 5: Screens & Navigation Flow
1. Cài đặt màn hình chính `ChatScreen` hỗ trợ chuyển đổi trạng thái mượt mà (Empty → Free Chat → Item Chat).
2. Cài đặt `ChatHistoryScreen` và `ItemPickerSheet` (S05 chuyển đổi cùng thread).
3. Cài đặt Dialog xác nhận xóa / lưu trữ (`DeleteConfirmationDialog`, `ThreadOptionsSheet`).
4. Gắn kết Navigation: mở Chat từ nút CTA trên Home và từ màn hình Item Detail (không thêm tab thứ 5).

### Giai đoạn 6: Testing & Verification
1. Chạy Unit test cho ViewModel và Repository.
2. Kiểm tra biên dịch `./gradlew :app:assembleDebug`.
3. Kiểm tra các tiêu chuẩn kiểm thử tự động `./gradlew :app:testDebugUnitTest`.
