# Đặc tả Kỹ thuật & Nghiệp vụ: Chanko Chat UI v2.2 (Product-Centric Chat)

**Ngày cập nhật:** 07/11/2026  
**Phiên bản:** v2.2 (Handoff Final - Revision 5)  
**Tài liệu cơ sở:** Bộ bàn giao `Chanko-Chat-UI-v2.2` (Assets, OpenAPI 3.0.3 Contract, 27 UX Decisions, State Matrix, Accessibility & QA Report)  
**Trạng thái:** DRAFT FOR USER REVIEW (Gated Phase 1)  

---

## 1. Mục tiêu & Tổng quan (Objective & Overview)

Tái cấu trúc và nâng cấp toàn diện tính năng **"Chat with Chanko"** trên Android (Kotlin + Jetpack Compose) theo bộ đặc tả và tài nguyên **Chanko Chat UI v2.2**:
- **Chuyển dịch sang Product-Centric Chat:**
  - Mỗi cuộc trò chuyện gắn liền với **1 sản phẩm chính bất biến (`primaryProductId`)** và cho phép gắn thêm **các sản phẩm tham chiếu (`references`)** để so sánh, đối chiếu.
  - **Bãi bỏ hoàn toàn luồng "Trò chuyện tự do" (Free Chat)** (`CHAT-01`, `NC-GAP-02`).
  - **Bãi bỏ thẻ quyết định cũ (Decision Verdict Card BUY/WAIT/SKIP)** (`CD-GAP-03`, `EDGE-11`). Thay bằng **phân tích khách quan theo từng sản phẩm (`productSnapshots`)**, nêu rõ đặc tính từng món mà không phán quyết mua/chờ/bỏ.
  - **Bãi bỏ bộ lọc trạng thái cũ** (Đang cân nhắc / Chờ xem lại / Đã mua) trên Chat List và New Chat (`CL-GAP-02`, `SUPERSESSION`). Thay bằng widget **Mandatory Cooling-off** (Thời gian hạ nhiệt bắt buộc do Chanko ấn định).
- **Trải nghiệm 3 màn hình chuẩn hóa:**
  1. **CL (Chat List):** Danh sách thread sắp xếp theo tin nhắn mới nhất (`lastMessageAt DESC, id DESC`), hiển thị thumbnail sản phẩm chính + badge `+N` tham chiếu (`CL-GAP-01`), thanh tìm kiếm (Search bar min 52dp), trạng thái Cooling-off, nút tạo chat mới (`Đoạn chat mới` min 56dp).
  2. **NC (New Chat):** Luồng chọn sản phẩm chính duy nhất (Single-select với semantics radio, min 88dp/card, thumbnail 64dp). Tự động tra cứu lịch sử hội thoại của món đồ (`CHAT-02`, `NC-GAP-01`): nếu đã có chat đang hoạt động thì gợi ý tiếp tục hoặc tạo mới; hỗ trợ xem danh sách lưu trữ. Bản nháp (Draft) chỉ lưu ở client, không ghi server/Room cho đến khi gửi tin nhắn đầu tiên thành công (`NC-GAP-04`, `EDGE-05`, `EDGE-06`).
  3. **CD (Chat Detail):** Header sản phẩm chính (bất biến, min 96–120dp), hàng chip tham chiếu mở rộng/cuộn ngang (`CD-GAP-01`), dòng thời gian tin nhắn (LazyColumn giữ vị trí cuộn khi tải tin cũ, chỉ báo tin mới khi đang đọc), khung soạn thảo đa dòng (Composer min 56dp, tối đa 6 dòng rồi cuộn nội bộ, tự co giãn theo bàn phím với `imePadding` và `navigationBarsPadding`). Khóa chỉnh sửa tham chiếu khi AI đang phản hồi (`EDGE-09`).
- **Bảo toàn dữ liệu & Snapshot lịch sử:**
  - Xóa sản phẩm chính: Giữ toàn bộ tin nhắn và snapshot lịch sử; thread chuyển sang chế độ chỉ đọc (`readOnlyReason = primary_deleted`), cho phép lưu trữ hoặc xóa (`CL-GAP-04`, `EDGE-01`).
  - Xóa sản phẩm tham chiếu: Giữ snapshot phân tích trong các tin nhắn cũ, gỡ bỏ khỏi danh sách tham chiếu đang hoạt động (`CD-GAP-04`, `EDGE-02`, `EDGE-08`).
  - Thay đổi giá/thông tin sản phẩm: Cập nhật dữ liệu mới lên thẻ hiển thị hiện tại, bảo lưu bất biến các snapshot trong lịch sử tin nhắn (`EDGE-03`).
- **Nền tảng Giao tiếp & Ngoại tuyến:**
  - Đồng bộ theo chuẩn hợp đồng **OpenAPI 3.0.3 (B07: Contract Ready — Pending Backend Implementation)**. Giai đoạn này sử dụng Deterministic Fake Repository độc lập hỗ trợ đầy đủ 13 operations, mock RAM, idempotency key, SSE-like flow streaming.
  - An toàn gửi tin: `Idempotency-Key` và `clientMessageId` ngăn chặn tạo tin trùng lặp khi retry qua mạng chập chờn (`EDGE-10`).
  - Hủy phản hồi rõ ràng (`POST generation/cancel`) trước khi xóa hoặc lưu trữ đoạn chat (`EDGE-12`).

---

## 2. Lệnh Thực Thi & Kiểm Thử (Executable Commands)

- **Biên dịch Debug (Dev):** `./gradlew :app:assembleDevDebug --no-configuration-cache`
- **Kiểm thử Đơn vị (Unit Tests):** `./gradlew :app:testDevDebugUnitTest --no-configuration-cache`
- **Kiểm tra Lint:** `./gradlew :app:lintDevDebug --no-configuration-cache`
- **Quy tắc An toàn Máy ảo/Thực:** Tuyệt đối không cài đặt hoặc chạy ứng dụng trên thiết bị thật hoặc máy ảo (AVD/Emulator) (`AGENTS.md`). Chỉ kiểm chứng thông qua build và test terminal.

---

## 3. Tech Stack & Hệ Thống Token / Tài Nguyên (Design Tokens & Assets)

### 3.1. Design Tokens (v2.2)
- **Primary Hue:** `#8965F6` (Màu thương hiệu / trang trí)
- **Primary Text & CTA:** `#7043D6` (Đạt độ tương phản WCAG $\ge 4.5:1$ trên nền trắng)
- **Body Text:** `#292238`
- **Secondary Text:** `#62586F`
- **Surface:** `#FFFFFF`
- **Background Gradient:** `["#F5F0FF", "#FFF1F7"]`
- **User Message Bubble:** `#EEE6FF`
- **Chanko Bubble:** `#FFFFFF` với viền mềm
- **Touch Target tối thiểu:** $\ge 48\,\text{dp}$ cho mọi thành phần tương tác
- **Icon chuẩn:** Viewport $24 \times 24\,\text{dp}$, bao ngoài bởi touch target $\ge 48\,\text{dp}$

### 3.2. Typography (Nunito v3.602 upstream)
- Font tĩnh chuẩn upstream: `chat_nunito_400.ttf`, `chat_nunito_600.ttf`, `chat_nunito_700.ttf`, `chat_nunito_800.ttf`
- `headingSp = 24.sp`
- `bodySp = 15.sp`, `bodyLineSp = 22.sp`
- Hỗ trợ co giãn văn bản lớn (`fontScale = 2.0`), tự động thu gọn chi tiết trang trí để bảo toàn Composer.

### 3.3. Asset Handoff (`Chanko-Chat-UI-v2.2/android/res`)
- **39 Candy Duotone Icons (VectorDrawable XML):** 13 chức năng $\times$ 3 trạng thái (`default`, `selected`, `disabled`):
  - Điều hướng & Thao tác: `chat_back_*`, `chat_close_*`, `chat_more_*`, `chat_chevron_*`
  - Chức năng: `chat_search_*`, `chat_new_chat_*`, `chat_reference_*`, `chat_send_*`, `chat_retry_*`
  - Quản trị: `chat_rename_*`, `chat_archive_*`, `chat_delete_*`, `chat_cooling_*`
- **Mascot (drawable-nodpi):**
  - `chanko_chat_hero.webp` ($1536 \times 1024\,\text{px}$, ContentScale.Fit, chiều cao CL: 100–120dp)
  - `chanko_chat_avatar.webp` ($512 \times 512\,\text{px}$, hiển thị $32\,\text{dp}$ / $40\,\text{dp}$)
- **Fixtures hình học sạch (drawable-nodpi):**
  - `chat_fixture_headphones.png`, `chat_fixture_watch.png`, `chat_fixture_keyboard.png`, `chat_fixture_chair.png`, `chat_fixture_neutral.png`

---

## 4. Kiến Trúc & Cấu Trúc Thư Mục (Project Structure)

Tuân thủ Clean Architecture + MVVM + Feature-Driven (`CODING_STANDARDS.md`):

```
app/src/main/java/com/hoabh/chanko/
├── domain/chat/                           # Domain Layer (Pure Kotlin)
│   ├── model/
│   │   ├── ChatThread.kt                  # Thread entity, primaryProductId, references
│   │   ├── ChatMessage.kt                 # Message, sender, productSnapshots, status
│   │   ├── ProductSnapshot.kt             # Bất biến: id, name, price, coolingStatus
│   │   ├── CoolingOffInfo.kt              # Trạng thái hạ nhiệt: start, end, progress, status
│   │   └── ReadOnlyReason.kt              # None, Archived, PrimaryDeleted, Offline
│   └── repository/
│       └── ChatRepository.kt              # Giao diện kho dữ liệu chuẩn 13 operations
├── data/chat/                             # Data Layer
│   ├── dto/                               # DTO khớp OpenAPI 3.0.3
│   ├── fake/
│   │   └── FakeChatRepository.kt          # Triển khai Deterministic Fake InMemory/Seed
│   ├── local/                             # Room Entity & DAO (Offline cache)
│   │   ├── entity/
│   │   └── dao/
│   └── mapper/                            # DTO <-> Domain <-> Entity Mappers
└── presentation/features/chat/            # Presentation Layer
    ├── components/
    │   ├── ChatComposerBar.kt             # Khung gõ tin nhắn, IME padding, đếm dòng
    │   ├── ChatMessageBubble.kt           # Bubble tin nhắn + Product Snapshot Cards
    │   ├── ProductContextCard.kt          # Header sản phẩm chính & reference chips
    │   ├── CoolingOffIndicator.kt         # Countdown & progress hạ nhiệt bắt buộc
    │   ├── ChatDialogs.kt                 # Dialog tiếp tục/tạo mới, xác nhận xóa/lưu trữ
    │   └── ThreadOptionsSheet.kt          # BottomSheet đổi tên, lưu trữ, xóa
    ├── list/
    │   ├── ChatListScreen.kt              # CL: Header mascot, search, thread rows (+N)
    │   ├── ChatListViewModel.kt
    │   └── ChatListUiState.kt
    ├── newchat/
    │   ├── NewChatScreen.kt               # NC: Chọn 1 sản phẩm chính, radio semantics
    │   ├── NewChatViewModel.kt
    │   └── NewChatUiState.kt
    └── detail/
        ├── ChatDetailScreen.kt            # CD: Timeline, references LazyRow, composer
        ├── ChatViewModel.kt
        └── ChatUiState.kt
```

---

## 5. Quy Chuẩn Code & Mẫu Cấu Trúc (Code Style & Snippets)

### 5.1. Mô hình Dữ liệu Domain Bất biến (Domain Models)
```kotlin
data class ChatThread(
    val id: String,
    val title: String,
    val primaryProduct: ProductSnapshot,
    val referenceProducts: List<ProductSnapshot> = emptyList(),
    val lastMessage: ChatMessage?,
    val updatedAt: Long,
    val isArchived: Boolean = false,
    val readOnlyReason: ReadOnlyReason = ReadOnlyReason.NONE
)

data class ProductSnapshot(
    val productId: String,
    val name: String,
    val price: Long, // Integer minor unit VND
    val thumbnailResId: Int?,
    val coolingOff: CoolingOffInfo? = null,
    val isPrimaryDeleted: Boolean = false
)

sealed interface ReadOnlyReason {
    data object NONE : ReadOnlyReason
    data object ARCHIVED : ReadOnlyReason
    data object PRIMARY_DELETED : ReadOnlyReason
    data object OFFLINE : ReadOnlyReason
}
```

### 5.2. Stateless Composable & Accessibility Semantics
```kotlin
@Composable
fun ProductSelectRow(
    product: ProductSnapshot,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onSelect(product.productId) },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFF5F0FF) else Color.White,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Color(0xFF8965F6) else Color(0xFFE8E5EE)
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = "${product.name}, ${formatVnd(product.price)}"
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Product image, details and selection radio indicator
        }
    }
}
```

---

## 6. Chiến Lược Kiểm Thử (Testing Strategy)

- **Unit Test Framework:** JUnit4, Kotlinx Coroutines Test, Turbine (kiểm tra StateFlow), Truth / AssertJ.
- **Phạm vi kiểm thử:**
  1. `ChatListViewModelTest`: Tìm kiếm, sắp xếp theo tin mới nhất (`CL-GAP-03`), hiển thị badge `+N` (`CL-GAP-01`), không còn filter cũ (`CL-GAP-02`), xóa sản phẩm chính chuyển read-only (`CL-GAP-04`).
  2. `NewChatViewModelTest`: Bỏ Free Chat, chỉ chọn 1 sản phẩm chính (`CHAT-01`, `NC-GAP-02`), phát hiện chat cũ chuyển sang Continue/New (`CHAT-02`, `NC-GAP-01`), draft không ghi Room cho tới khi gửi tin đầu (`NC-GAP-04`, `EDGE-05`, `EDGE-06`).
  3. `ChatViewModelTest`: Khóa sản phẩm chính bất biến (`CD-GAP-02`), thêm/bớt tham chiếu không ảnh hưởng lịch sử (`CD-GAP-04`, `EDGE-08`), phân tích đính kèm snapshot không có verdict (`CD-GAP-03`, `EDGE-11`), khóa sửa tham chiếu khi streaming (`EDGE-09`), retry tin nhắn idempotent khi mất mạng (`EDGE-10`), hủy generation trước khi lưu trữ/xóa (`EDGE-12`).
  4. `CoolingOffCalculationTest`: Tính toán thời gian thực monotonic, resync khi foreground, trạng thái active/elapsed/unknown (`CD-GAP-03`).

---

## 7. Ranh Giới Phát Triển (Boundaries)

- **Luôn luôn làm (Always):**
  - Chạy `./gradlew assembleDevDebug` và `./gradlew testDevDebugUnitTest` trước khi báo cáo kết quả.
  - Sử dụng toàn bộ text giao diện bằng tiếng Việt tự nhiên, chính xác theo hướng dẫn thiết kế.
  - Đảm bảo touch target tương tác $\ge 48\,\text{dp}$ cho mọi icon, button, radio card.
  - Tuân thủ thứ tự ưu tiên của 27 quyết định nguyên bản (`UX-27-ORIGINAL.md`) và bảng thay thế (`SUPERSESSION.md`).
- **Phải hỏi ý kiến người dùng trước (Ask First):**
  - Thực hiện thay đổi cấu trúc bảng Room Database hoặc schema migration.
  - Bổ sung thư viện bên ngoài mới vào `build.gradle.kts`.
  - Thay đổi hành vi điều hướng từ các màn hình khác (Home, Item Detail) vào Chat.
- **Tuyệt đối không làm (Never):**
  - Không cài đặt hoặc khởi động ứng dụng trên thiết bị thật, máy ảo, emulator (`AGENTS.md`).
  - Không chỉnh sửa, xóa hoặc can thiệp vào các tệp Agent Skills (`.agent/skills/`).
  - Không phục hồi luồng Free Chat hoặc Decision Verdict Card BUY/WAIT/SKIP cũ.
  - Không âm thầm bỏ qua các điều kiện biên (Edge cases) đã quy định.

---

## 8. Tiêu Chí Nghiệm Thu (Acceptance Criteria Traceability: 27/27)

| Mã | Nội dung Quyết Định | Tiêu chí Nghiệm thu Cụ thể |
|---|---|---|
| **CHAT-01** | 1 SP chính + nhiều SP tham chiếu | Chỉ chọn duy nhất 1 SP chính khi tạo chat. Thêm tối đa tham chiếu trong CD. Không còn Free Chat. |
| **CHAT-02** | Mặc định tiếp tục chat cũ, cho tạo mới | Nếu SP chính đã có chat, hiển thị tùy chọn Tiếp tục hoặc Tạo mới; có lối xem chat lưu trữ. |
| **CHAT-03** | Từ Home mở Chat List | Nút Chat trên Home điều hướng vào màn hình CL. |
| **CL-GAP-01** | Thumbnail SP chính + badge +N | Thread item hiển thị ảnh SP chính; nếu có $>0$ tham chiếu thì hiển thị badge `+N`. |
| **CL-GAP-02** | Bỏ filter trạng thái cũ | Không còn filter chips (Cân nhắc / Chờ xem lại / Đã mua). Thay bằng trạng thái Cooling-off. |
| **CL-GAP-03** | Sắp xếp theo tin nhắn mới nhất | Sắp xếp `lastMessageAt DESC`, không phân nhóm tab. |
| **CL-GAP-04** | SP chính bị xóa giữ snapshot | SP chính bị xóa vẫn giữ lịch sử trò chuyện và snapshot thẻ; chat trở thành chỉ đọc. |
| **NC-GAP-01** | Có chat cũ cho chọn tiếp tục/mới | Hiển thị Dialog/Sheet chọn Tiếp tục đoạn chat cũ hoặc Tạo đoạn chat mới. |
| **NC-GAP-02** | Chọn SP chính trước, tham chiếu ở CD | Màn NC chỉ cho phép chọn 1 SP chính (Radio). Tham chiếu chỉ thêm tại CD. |
| **NC-GAP-03** | Chưa có SP: Empty state + CTA thêm món | Hiển thị Empty State kèm nút điều hướng sang màn Thêm món đồ. |
| **NC-GAP-04** | Chỉ lưu thread khi gửi tin đầu thành công | Thoát trước khi gửi không tạo thread trong cơ sở dữ liệu. |
| **CD-GAP-01** | Card SP chính + chip tham chiếu | Header hiển thị card SP chính; hàng chip tham chiếu cuộn ngang có nút thêm/bớt. |
| **CD-GAP-02** | Khóa SP chính sau khi tạo | SP chính là bất biến, không có nút đổi SP chính trong cuộc trò chuyện. |
| **CD-GAP-03** | Phân tích gắn rõ từng sản phẩm | Bong bóng phản hồi của Chanko phân tích kèm snapshot từng món, không sinh BUY/WAIT/SKIP. |
| **CD-GAP-04** | Bỏ tham chiếu không xóa lịch sử | Thao tác xóa chip tham chiếu giữ nguyên toàn bộ tin nhắn phân tích trước đó. |
| **EDGE-01** | Xóa SP chính: chat chỉ đọc, cho xóa/lưu trữ | Khóa composer khi SP chính bị xóa; vẫn cho phép người dùng đổi tên, lưu trữ hoặc xóa thread. |
| **EDGE-02** | Xóa SP tham chiếu: giữ snapshot cũ | SP tham chiếu bị xóa chỉ gỡ khỏi chip đang hoạt động, tin nhắn cũ giữ nguyên snapshot. |
| **EDGE-03** | Giá SP cập nhật: card mới, snapshot cũ | Card sản phẩm cập nhật giá mới; tin nhắn lịch sử giữ nguyên giá tại thời điểm phân tích. |
| **EDGE-04** | Ưu tiên chat gần nhất, xem được lưu trữ | Mặc định mở chat hoạt động gần nhất; cho phép mở danh sách chat đã lưu trữ. |
| **EDGE-05** | Thoát trước tin đầu không cảnh báo | Nếu composer rỗng, bấm Back thoát ngay không hiển thị dialog cảnh báo bỏ nháp. |
| **EDGE-06** | Gửi tin đầu lỗi: giữ nội dung, cho retry | Giữ nguyên văn bản trong ô soạn thảo và client ID để retry, chưa lưu thread chính thức. |
| **EDGE-07** | Không cho gắn trùng SP chính/tham chiếu | Chặn không cho chọn SP tham chiếu trùng với SP chính hoặc trùng với tham chiếu đã có. |
| **EDGE-08** | Gỡ tham chiếu: snapshot phân tích còn nguyên | Phân tích đã tạo trong quá khứ không bị biến mất khi gỡ tham chiếu. |
| **EDGE-09** | Khóa sửa tham chiếu khi AI đang trả lời | Vô hiệu hóa nút thêm/gỡ chip tham chiếu trong lúc generation đang diễn ra. |
| **EDGE-10** | Mất mạng retry an toàn, không sinh trùng tin | Sử dụng `Idempotency-Key` và `clientMessageId` để gộp tin nhắn khi mạng kết nối lại. |
| **EDGE-11** | So sánh nhiều món: phân tích rõ từng SP | Không gộp chung phán quyết; liệt kê ưu/nhược điểm từng món kèm định danh rõ ràng. |
| **EDGE-12** | Đang streaming không cho xóa/lưu trữ trực tiếp | Yêu cầu hủy generation (`POST cancel`) hoặc đợi hoàn tất trước khi xóa/lưu trữ. |

---

## 9. Câu Hỏi Mở & Điểm Cần Làm Rõ (Open Questions)

1. **Về việc tích hợp tài nguyên Handoff (`Chanko-Chat-UI-v2.2/android/res`):**
   - Bộ 39 icon duotone vector, 2 ảnh mascot WebP và 5 fixture hình học hiện đang nằm trong thư mục bàn giao `Chanko-Chat-UI-v2.2/android/res`. Bạn có đồng ý cho phép sao chép các tài nguyên này vào thư mục chính `app/src/main/res/` khi triển khai không?
2. **Về cơ sở dữ liệu Room Cache:**
   - Để hỗ trợ lưu trữ `primaryProductId`, `references` (dưới dạng danh sách ID/Snapshot) và `readOnlyReason`, cấu trúc Entity `ChatThreadEntity` sẽ cần cập nhật. Bạn muốn nâng version Room Database kèm migration hay dùng schema fallback destructive (nếu chỉ là bản dev)? (Khuyến nghị: viết Migration an toàn theo chuẩn Clean Architecture).
3. **Về Fake Backend Contract vs Backend Thật:**
   - Xác nhận: Ở giai đoạn này, toàn bộ logic mạng sẽ được mô phỏng hoàn chỉnh qua `FakeChatRepository` hỗ trợ 13 endpoints chuẩn OpenAPI 3.0.3, đảm bảo có thể chuyển sang Retrofit/Ktor backend khi API thật sẵn sàng mà không cần sửa giao diện. Bạn xác nhận cách tiếp cận này chứ?
