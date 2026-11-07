# Danh sách Công việc Triển khai: Chanko Chat UI v2.2 (Product-Centric Chat)

> **Kế hoạch kiến trúc:** [tasks/plan.md](tasks/plan.md)  
> **Tài liệu đặc tả:** [SPEC-chat-ui-v2.2.md](../SPEC-chat-ui-v2.2.md) & [docs/features/chat-ui-v2.2-spec.md](../docs/features/chat-ui-v2.2-spec.md)  
> **Quy chuẩn thực thi:** Kiểm thử đơn vị + Build compilation (`assembleDevDebug`) sau mỗi bước; không chạy trên emulator/thiết bị thật.

---

## Phase 1: Assets & Design Tokens

### Task 1: Tích hợp Tài nguyên Handoff v2.2 vào `app/src/main/res/`

**Description:**
Sao chép toàn bộ tài nguyên đã kiểm định từ `Chanko-Chat-UI-v2.2/android/res` vào `app/src/main/res/`:
- 39 Candy Duotone Icons (VectorDrawable XML) vào `res/drawable/`.
- 2 ảnh Mascot WebP (`chanko_chat_hero.webp`, `chanko_chat_avatar.webp`) vào `res/drawable-nodpi/`.
- 4 font static Nunito TTF (`chat_nunito_400/600/700/800.ttf`) vào `res/font/`.
- 5 fixture ảnh PNG sạch vào `res/drawable-nodpi/`.

**Acceptance criteria:**
- [x] Toàn bộ 39 tệp XML icon, 2 WebP mascot, 4 TTF font và 5 PNG fixture có mặt đúng thư mục `app/src/main/res/`.
- [x] Không có lỗi xung đột định danh tài nguyên (resource conflict).
- [x] Biên dịch AAPT thành công.

**Verification:**
- [x] Build succeeds: `./gradlew :app:assembleDevDebug --no-configuration-cache`

**Dependencies:** None

**Files likely touched:**
- `app/src/main/res/drawable/chat_*.xml` (39 files)
- `app/src/main/res/drawable-nodpi/chanko_chat_*.webp` (2 files)
- `app/src/main/res/drawable-nodpi/chat_fixture_*.png` (5 files)
- `app/src/main/res/font/chat_nunito_*.ttf` (4 files)

**Estimated scope:** Medium (Resource copy)

---

### Task 2: Định nghĩa Design Tokens & Typography Chat v2.2

**Description:**
Tạo đối tượng `ChatTokens` và cấu hình typography Nunito cho Chat trong hệ thống Design System:
- Các mã màu chuẩn v2.2: Primary `#8965F6`, CTA `#7043D6`, Body `#292238`, Secondary `#62586F`, User Bubble `#EEE6FF`, Nền gradient `["#F5F0FF", "#FFF1F7"]`.
- `FontFamily` Nunito liên kết với 4 tệp font `chat_nunito_400/600/700/800`.
- Thiết lập kích thước font chuẩn: Heading 24sp, Body 15sp, LineHeight 22sp.

**Acceptance criteria:**
- [x] Đối tượng token cung cấp đầy đủ các màu và kiểu chữ cho Chat v2.2.
- [x] Tỷ lệ tương phản màu văn bản CTA trên nền trắng đạt $\ge 4.5:1$.

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** Task 1

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/designsystem/theme/ChatTokens.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/designsystem/theme/Type.kt`

**Estimated scope:** Small (1-2 files)

---

### Checkpoint 1: Assets & Tokens Ready
- [x] Toàn bộ VectorDrawables, Mascot WebP và Nunito TTF biên dịch thành công.
- [x] Token màu và Typography sẵn sàng cho UI.

---

## Phase 2: Domain Layer

### Task 3: Mô hình Hóa Domain Entities cho Product-Centric Chat

**Description:**
Định nghĩa các Domain Models bất biến tuân thủ Clean Architecture:
- `ProductSnapshot`: `productId`, `name`, `price` (Long VND), `thumbnailResId`, `coolingOff: CoolingOffInfo?`, `isPrimaryDeleted: Boolean`.
- `CoolingOffInfo`: `startedAtEpochMillis`, `durationMillis`, `remainingMillis`, `progress: Float`, `status: CoolingOffStatus` (`ACTIVE`, `ELAPSED`, `UNKNOWN`).
- `ChatMessage`: `id`, `threadId`, `sender` (`USER`, `CHANKO`), `content`, `productSnapshots: List<ProductSnapshot>`, `createdAtEpochMillis`, `status` (`SENDING`, `SENT`, `FAILED`).
- `ChatThread`: `id`, `title`, `primaryProduct: ProductSnapshot`, `referenceProducts: List<ProductSnapshot>`, `lastMessage: ChatMessage?`, `updatedAtEpochMillis`, `isArchived: Boolean`, `readOnlyReason: ReadOnlyReason`.
- `ReadOnlyReason`: Sealed interface (`NONE`, `ARCHIVED`, `PRIMARY_DELETED`, `OFFLINE`).

**Acceptance criteria:**
- [x] Các models là pure Kotlin (không phụ thuộc Android framework).
- [x] Không chứa khái niệm "Trò chuyện tự do" (mọi thread bắt buộc có `primaryProduct`).
- [x] Không chứa phán quyết BUY/WAIT/SKIP (phân tích chỉ lưu qua `productSnapshots`).

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** None

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/domain/chat/model/ChatThread.kt`
- `app/src/main/java/com/hoabh/chanko/domain/chat/model/ChatMessage.kt`
- `app/src/main/java/com/hoabh/chanko/domain/chat/model/ProductSnapshot.kt`
- `app/src/main/java/com/hoabh/chanko/domain/chat/model/CoolingOffInfo.kt`
- `app/src/main/java/com/hoabh/chanko/domain/chat/model/ReadOnlyReason.kt`

**Estimated scope:** Medium (4-5 files)

---

### Task 4: Giao diện ChatRepository & Tính toán Thời gian Monotonic

**Description:**
Định nghĩa `ChatRepository` interface bao quát 13 operations chuẩn hóa:
- Quản lý sản phẩm: `getEligibleProducts(query, cursor, limit): Flow<List<ProductSnapshot>>`.
- Tra cứu lịch sử: `findActiveThreadForProduct(productId): ChatThread?`, `getArchivedThreadsForProduct(productId): List<ChatThread>`.
- Quản lý Threads: `getThreads(query, archived): Flow<List<ChatThread>>`, `getThread(threadId): Flow<ChatThread?>`, `renameThread(threadId, newTitle)`, `archiveThread(threadId)`, `restoreThread(threadId)`, `deleteThread(threadId)`.
- Quản lý Tin nhắn & Gửi tin nguyên tử: `getMessages(threadId): Flow<List<ChatMessage>>`, `sendFirstMessage(draftThread, firstMessageText, idempotencyKey): ChatThread`, `sendMessage(threadId, messageText, idempotencyKey)`.
- Tham chiếu: `addReference(threadId, productId)`, `removeReference(threadId, productId)`.
- Generation & Streaming: `streamAiResponse(threadId, generationId): Flow<ChatGenerationEvent>`, `cancelGeneration(threadId, generationId)`.
- Xây dựng lớp tiện ích `MonotonicClockCalculator` tính toán `remainingMillis` và `progress` dựa trên `elapsedRealtime`.

**Acceptance criteria:**
- [x] Interface bao quát đầy đủ 13 nghiệp vụ của hợp đồng OpenAPI 3.0.3.
- [x] Lớp tính toán hạ nhiệt có unit test xác minh chính xác các trạng thái `ACTIVE`, `ELAPSED`, `UNKNOWN`.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*CoolingOffCalculatorTest*"`

**Dependencies:** Task 3

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/domain/chat/repository/ChatRepository.kt`
- `app/src/main/java/com/hoabh/chanko/domain/chat/util/MonotonicClockCalculator.kt`
- `app/src/test/java/com/hoabh/chanko/domain/chat/util/CoolingOffCalculatorTest.kt`

**Estimated scope:** Medium (3 files)

---

### Checkpoint 2: Domain Layer Verified
- [x] Domain Models và Repository interface hoàn thiện.
- [x] Unit tests cho logic tính toán thời gian hạ nhiệt đạt 100%.

---

## Phase 3: Data Layer & Fake Repository

### Task 5: DTOs Khớp OpenAPI 3.0.3 & Data Mappers

**Description:**
Tạo các Data Transfer Objects (DTO) ánh xạ 1:1 với hợp đồng `Chanko-Chat-UI-v2.2/contract/openapi.yaml`:
- `ChatProductDto`, `ChatThreadDto`, `ChatMessageDto`, `ProductSnapshotDto`, `CoolingOffDto`, `GenerationEventDto`.
- Mappers hai chiều chuyển đổi giữa DTO $\longleftrightarrow$ Domain Model.

**Acceptance criteria:**
- [x] Khớp cấu trúc JSON của hợp đồng OpenAPI.
- [x] Chuyển đổi chính xác tiền tệ VND (integer minor unit) và thời gian RFC3339 / epoch millis.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*ChatDtoMapperTest*"`

**Dependencies:** Task 3

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/data/chat/dto/ChatDtos.kt`
- `app/src/main/java/com/hoabh/chanko/data/chat/mapper/ChatMapper.kt`
- `app/src/test/java/com/hoabh/chanko/data/chat/mapper/ChatDtoMapperTest.kt`

**Estimated scope:** Medium (3 files)

---

### Task 6: Xây dựng Deterministic FakeChatRepository (In-Memory Mock)

**Description:**
Triển khai `FakeChatRepository` phục vụ phát triển độc lập theo B07:
- Khởi tạo với dữ liệu seed từ `Chanko-Chat-UI-v2.2/mock/seed.json` (danh sách sản phẩm tai nghe, đồng hồ, bàn phím, ghế; các thread mẫu).
- Hỗ trợ gửi tin nhắn đầu tiên nguyên tử (`sendFirstMessage`), gộp optimistic ack, chống trùng lặp bằng `Idempotency-Key` (`EDGE-10`).
- Giả lập phát streaming phản hồi của Chanko (`streamAiResponse`) với các đoạn văn bản phân tích khách quan cho từng sản phẩm (`productSnapshots`), không tạo phán quyết BUY/WAIT/SKIP (`EDGE-11`).
- Hỗ trợ hủy generation (`cancelGeneration`) và khóa chỉnh sửa tham chiếu khi đang streaming (`EDGE-09`, `EDGE-12`).

**Acceptance criteria:**
- [x] Hoạt động trơn tru trong bộ nhớ RAM, phục vụ mọi luồng UI mà không cần mạng Internet.
- [x] Đạt 100% các ca kiểm thử hành vi tương đương hợp đồng OpenAPI.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*FakeChatRepositoryTest*"`

**Dependencies:** Tasks 4, 5

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/data/chat/fake/FakeChatRepository.kt`
- `app/src/test/java/com/hoabh/chanko/data/chat/fake/FakeChatRepositoryTest.kt`

**Estimated scope:** Medium (2 files)

---

### Task 7: Cập nhật Room Database Entity & DAO (Dev Fallback Mode)

**Description:**
Cập nhật Room Database để hỗ trợ lưu cache offline cho Chat v2.2:
- Cập nhật `ChatThreadEntity` (`primaryProductId`, `title`, `isArchived`, `readOnlyReason`).
- Cập nhật `ChatMessageEntity` (`threadId`, `content`, `productSnapshotsJson`, `status`).
- Áp dụng `fallbackToDestructiveMigration()` trong AppDatabase cấu hình Dev theo thỏa thuận người dùng (không cần migration script phức tạp).

**Acceptance criteria:**
- [x] Schema Room cập nhật hỗ trợ đầy đủ các trường mới của Chat v2.2.
- [x] Biên dịch KSP Room thành công, không báo lỗi schema mismatch.

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** Task 3

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/data/local/database/entity/ChatThreadEntity.kt`
- `app/src/main/java/com/hoabh/chanko/data/local/database/entity/ChatMessageEntity.kt`
- `app/src/main/java/com/hoabh/chanko/data/local/database/AppDatabase.kt`

**Estimated scope:** Medium (3 files)

---

### Checkpoint 3: Data Layer & Mock Engine Verified
- [x] `FakeChatRepository` pass toàn bộ unit tests.
- [x] Room schema mới biên dịch thành công.

---

## Phase 4: Shared UI Components

### Task 8: Xây dựng `ProductContextCard` & `ReferenceChipsRow`

**Description:**
Xây dựng header sản phẩm chính và hàng chip tham chiếu tại `components/`:
- `ProductContextCard`: Hiển thị ảnh thumbnail 56dp, tên món, giá tiền định dạng VND, huy hiệu trạng thái Cooling-off (Active / Elapsed / Unknown). Hỗ trợ trạng thái khi SP chính bị xóa (`CL-GAP-04`, `EDGE-01`).
- `ReferenceChipsRow`: Cuộn ngang (`LazyRow`), mỗi chip hiển thị thumbnail nhỏ + tên món + nút xóa (`chat_close`). Cuối hàng có nút `+ Thêm món so sánh` (`chat_reference`).
- Khóa tương tác khi AI đang stream (`EDGE-09`) hoặc thread chỉ đọc.
- Touch target của nút xóa và nút thêm $\ge 48\,\text{dp}$.

**Acceptance criteria:**
- [x] Render chuẩn xác theo thiết kế v2.2.
- [x] Semantics trợ năng đầy đủ: đọc tên món, giá, trạng thái hạ nhiệt.

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** Tasks 2, 3

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/components/ProductContextCard.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/components/ReferenceChipsRow.kt`

**Estimated scope:** Medium (2 files)

---

### Task 9: Xây dựng `ChatMessageBubble` & `CoolingOffIndicator`

**Description:**
Tái cấu trúc bong bóng tin nhắn:
- Bong bóng User: Nền `#EEE6FF`, góc bo 20dp, căn phải, max 82% chiều rộng màn hình.
- Bong bóng Chanko: Nền trắng viền mềm, avatar Chanko 32dp, hiển thị nội dung phân tích khách quan kèm thẻ con `ProductSnapshotCard` cho từng sản phẩm được nhắc đến (`CD-GAP-03`, `EDGE-11`).
- **Loại bỏ hoàn toàn code hiển thị Decision Verdict Card BUY/WAIT/SKIP cũ.**
- Component `CoolingOffIndicator`: Hiển thị đếm ngược thời gian thực (ví dụ: "Còn lại 2 ngày 14 giờ") kèm thanh tiến trình determinate; khi hết thời gian hiển thị "Sẵn sàng xem xét".

**Acceptance criteria:**
- [x] Không còn bất kỳ dấu vết nào của thẻ BUY/WAIT/SKIP cũ trong `ChatMessageBubble`.
- [x] Thẻ con snapshot hiển thị rõ ràng thông tin sản phẩm và giá tại thời điểm phân tích (`EDGE-03`).

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** Tasks 2, 3

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/components/ChatMessageBubble.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/components/CoolingOffIndicator.kt`

**Estimated scope:** Medium (2 files)

---

### Task 10: Xây dựng `ChatComposerBar` Nâng Cao

**Description:**
Cải tiến khung soạn thảo tin nhắn:
- Chiều cao tối thiểu 56dp, co giãn đa dòng tối đa 6 dòng rồi tự động cuộn nội bộ.
- Tích hợp `Modifier.imePadding()` và `Modifier.navigationBarsPadding()` chống bị bàn phím che khuất.
- Nút Gửi (`chat_send_default` / `chat_send_selected` / `chat_send_disabled`) với touch target $\ge 48\,\text{dp}$.
- Trạng thái vô hiệu hóa kèm giải thích rõ ràng khi thread chỉ đọc (`readOnlyReason`: Đã lưu trữ, Món đồ chính đã xóa, hoặc Ngoại tuyến).

**Acceptance criteria:**
- [x] Composer hoạt động mượt mà, không bị che khi mở IME.
- [x] Vô hiệu hóa đúng khi thread chỉ đọc hoặc đang ngoại tuyến.

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** Tasks 1, 2

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/components/ChatComposerBar.kt`

**Estimated scope:** Small (1 file)

---

### Checkpoint 4: Shared Components Verified
- [x] Toàn bộ components dùng chung biên dịch thành công.
- [x] Không còn tàn dư Decision Card cũ.

---

## Phase 5: Screens Refactor

### Task 11: Tái cấu trúc Màn hình `ChatListScreen` (CL)

**Description:**
Nâng cấp giao diện Danh sách trò chuyện (CL):
- **Loại bỏ toàn bộ Filter Chips cũ** (Đang cân nhắc / Chờ xem lại / Đã mua) (`CL-GAP-02`).
- Thanh tìm kiếm (OutlinedTextField min 52dp, icon `chat_search_default`).
- Header Hero Mascot (`chanko_chat_hero.webp`, cao 100–120dp, ContentScale.Fit).
- Thread rows: Hiển thị thumbnail sản phẩm chính (56dp) + huy hiệu `+N` nếu có tham chiếu (`CL-GAP-01`), tiêu đề, tin nhắn xem trước, thời gian, và trạng thái Cooling-off.
- Sắp xếp thread theo tin nhắn mới nhất (`lastMessageAt DESC, id DESC`) (`CL-GAP-03`).
- Nút bấm tạo chat mới (`Đoạn chat mới` min 56dp, icon `chat_new_chat_default`).
- Xử lý Thread khi sản phẩm chính bị xóa: Giữ lịch sử và hiển thị nhãn chỉ đọc (`CL-GAP-04`, `EDGE-01`).
- Menu tùy chọn (`ThreadOptionsSheet`): Đổi tên, Lưu trữ, Xóa (`EDGE-01`).

**Acceptance criteria:**
- [x] Không còn filter chips cũ.
- [x] Hiển thị badge `+N` chuẩn xác theo số lượng tham chiếu.
- [x] Sắp xếp danh sách chính xác.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*ChatListViewModelTest*"`
- [x] Build succeeds: `./gradlew :app:assembleDevDebug --no-configuration-cache`

**Dependencies:** Tasks 8, 9, 10

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/list/ChatListScreen.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/list/ChatListViewModel.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/list/ChatListUiState.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/features/chat/list/ChatListViewModelTest.kt`

**Estimated scope:** Medium (4 files)

---

### Task 12: Tái cấu trúc Màn hình `NewChatScreen` (NC)

**Description:**
Nâng cấp giao diện Bắt đầu trò chuyện mới (NC):
- **Loại bỏ hoàn toàn chế độ "Trò chuyện tự do"** (`CHAT-01`).
- Màn hình tập trung vào việc chọn duy nhất 1 sản phẩm chính từ danh sách (Single-select với semantics radio, card min 88dp, thumbnail 64dp) (`NC-GAP-02`).
- Empty State khi chưa có món đồ: Nút CTA dẫn sang luồng thêm món (`NC-GAP-03`).
- Khi chọn 1 món đồ đã có cuộc trò chuyện: Tra cứu lịch sử (`CHAT-02`, `NC-GAP-01`, `EDGE-04`), hiển thị Dialog tùy chọn: *"Tiếp tục đoạn chat cũ"* (mặc định mở chat hoạt động gần nhất) hoặc *"Tạo đoạn chat mới"*; cho phép mở danh sách chat đã lưu trữ.
- Bản nháp (Draft) chỉ lưu ở client, không ghi server/Room trước khi gửi tin nhắn đầu tiên thành công (`NC-GAP-04`, `EDGE-05`, `EDGE-06`).

**Acceptance criteria:**
- [x] Không còn tùy chọn Free Chat.
- [x] Chọn sản phẩm kích hoạt tra cứu lịch sử và hiển thị đúng dialog tiếp tục/tạo mới.
- [x] Thoát màn hình khi draft rỗng không cảnh báo; không tạo thread rác trong Room.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*NewChatViewModelTest*"`
- [x] Build succeeds: `./gradlew :app:assembleDevDebug --no-configuration-cache`

**Dependencies:** Tasks 6, 8, 10

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/newchat/NewChatScreen.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/newchat/NewChatViewModel.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/newchat/NewChatUiState.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/features/chat/newchat/NewChatViewModelTest.kt`

**Estimated scope:** Medium (4 files)

---

### Task 13: Tái cấu trúc Màn hình `ChatDetailScreen` (CD)

**Description:**
Nâng cấp màn hình Chi tiết trò chuyện (CD):
- TopAppBar hiển thị Avatar Chanko (40dp), tên cuộc trò chuyện, nút Quay lại và nút Menu (Đổi tên, Lưu trữ, Xóa).
- Header cố định hiển thị `ProductContextCard` của sản phẩm chính (bất biến, không thể thay đổi sản phẩm chính) (`CD-GAP-02`).
- Ngay dưới là `ReferenceChipsRow` cuộn ngang: cho phép bấm `+ Thêm món` để mở sheet chọn sản phẩm tham chiếu (chặn không cho chọn trùng SP chính hoặc trùng SP đã có - `EDGE-07`).
- Bỏ tham chiếu không làm mất tin nhắn phân tích cũ (`CD-GAP-04`, `EDGE-08`).
- Timeline tin nhắn: `LazyColumn` giữ nguyên vị trí cuộn khi nạp tin cũ; hiển thị chỉ báo tin mới khi đang đọc tin cũ.
- Khi AI đang streaming: Hiển thị typing/partial stream, khóa sửa tham chiếu (`EDGE-09`), vô hiệu hóa nút xóa/lưu trữ cho tới khi cancel hoặc hoàn tất (`EDGE-12`).
- Khung gõ `ChatComposerBar` ở dưới cùng.

**Acceptance criteria:**
- [x] Sản phẩm chính bất biến, không thể gỡ bỏ.
- [x] Thao tác thêm/bớt tham chiếu mượt mà, chặn chọn trùng lặp.
- [x] Quản lý stream an toàn và cho phép hủy generation rõ ràng.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*ChatViewModelTest*"`
- [x] Build succeeds: `./gradlew :app:assembleDevDebug --no-configuration-cache`

**Dependencies:** Tasks 8, 9, 10, 12

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/detail/ChatDetailScreen.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/detail/ChatViewModel.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/features/chat/detail/ChatUiState.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/features/chat/detail/ChatViewModelTest.kt`

**Estimated scope:** Large (4 files)

---

### Checkpoint 5: 3 Screens Refactored & Passing Tests
- [x] CL, NC, CD hoạt động đồng bộ trên FakeChatRepository.
- [x] Toàn bộ unit tests của 3 ViewModels đều pass 100%.

---

## Phase 6: Navigation Integration & Full Verification

### Task 14: Tích hợp Điều hướng Toàn cục & Kiểm chứng 27 Tiêu chí UX

**Description:**
Hoàn thiện tích hợp điều hướng giữa các màn hình ứng dụng:
- Từ `HomeScreen`: Nút Chat điều hướng vào `ChatListScreen` (`CHAT-03`).
- Từ `ItemDetailScreen`: Nút "Cân nhắc cùng Chanko" điều hướng trực tiếp sang Chat với sản phẩm này làm sản phẩm chính (nếu đã có chat thì mở CD, nếu chưa thì mở CD trạng thái nháp).
- Viết test suite hồi quy `ChatEndToEndFlowTest` xác minh đầy đủ 27 tiêu chí:
  - 3 CHAT decisions (`CHAT-01`, `02`, `03`).
  - 12 GAP decisions (`CL-GAP-01..04`, `NC-GAP-01..04`, `CD-GAP-01..04`).
  - 12 EDGE decisions (`EDGE-01..EDGE-12`).
- Biên dịch toàn bộ ứng dụng và chạy toàn bộ unit tests.

**Acceptance criteria:**
- [x] Điều hướng hoạt động thông suốt từ Home và Item Detail.
- [x] Toàn bộ 27 tiêu chí nghiệm thu được bao phủ bởi Unit Tests.
- [x] `./gradlew :app:testDevDebugUnitTest` PASS 100%.
- [x] `./gradlew :app:assembleDevDebug` PASS 100%.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest`
- [x] Build succeeds: `./gradlew :app:assembleDevDebug --no-configuration-cache`

**Dependencies:** Tasks 11, 12, 13

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/navigation/ChatNavGraph.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/features/home/HomeScreen.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/features/chat/ChatEndToEndFlowTest.kt`

**Estimated scope:** Medium (3 files)

---

### Checkpoint 6: Chanko Chat v2.2 Fully Verified
- [x] 27/27 Tiêu chí UX và Edge Cases đạt tiêu chuẩn.
- [x] 100% tests JVM pass.
- [x] APK DevDebug biên dịch sạch, không lỗi cảnh báo nghiêm trọng.
