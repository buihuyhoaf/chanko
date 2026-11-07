# Capability Map & Master Spec: Chanko Chat UI v2.2 (Product-Centric Chat Experience)

> **Tài liệu đặc tả chi tiết:** [docs/features/chat-ui-v2.2-spec.md](docs/features/chat-ui-v2.2-spec.md)  
> **Kế hoạch triển khai:** [tasks/plan.md](tasks/plan.md)  
> **Danh sách công việc:** [tasks/todo.md](tasks/todo.md)  
> **Nguồn bàn giao gốc:**  
> - Thư mục bàn giao: `Chanko-Chat-UI-v2.2/` (Revision 5, ngày 07/11/2026)  
> - Tài nguyên: `android/res` (39 Candy Duotone icons, Mascot Hero/Avatar WebP nodpi, Font Nunito 400/600/700/800, 5 Fixture PNG/SVG)  
> - Hợp đồng API: `contract/openapi.yaml` (OpenAPI 3.0.3, 13 operations) & `docs/CONTRACT-SYNC.md`  
> - 27 Quyết định UX: `docs/UX-27-ORIGINAL.md` & `docs/SUPERSESSION.md`  
> - State Matrix & Traceability: `docs/STATE-MATRIX.csv` & `docs/ACCEPTANCE-TRACEABILITY.csv`  
> **Trạng thái:** PLAN & TASKS READY FOR IMPLEMENTATION (Gated Phase 2 & 3 Completed)  

---

## 1. Capability Map (Phase 0: Scope Check & Decomposition)

Tính năng **Chanko Chat v2.2** chuyển dịch hoàn toàn từ chat tự do cũ sang **Product-Centric Chat**, xoay quanh 1 sản phẩm chính và các sản phẩm tham chiếu để so sánh, hiển thị phân tích khách quan không phán quyết BUY/WAIT/SKIP, tích hợp Mandatory Cooling-off.

Toàn bộ tính năng được phân rã thành **4 module độc lập**, kiểm thử riêng biệt với hướng phụ thuộc một chiều, không chu trình:

| Module ID | Trách nhiệm chính | Phụ thuộc (Depends on) | Thứ tự xây dựng (Build Order) |
| :--- | :--- | :--- | :---: |
| **`chat-assets-tokens`** | Sở hữu tài nguyên giao diện bàn giao v2.2: 39 Candy Duotone Icons (VectorDrawable XML với 3 trạng thái default/selected/disabled), Mascot Hero & Avatar WebP alpha sạch, font tĩnh Nunito upstream (400/600/700/800), bảng màu Candy Color Tokens (`#8965F6`, CTA `#7043D6`, `#EEE6FF`), và 5 Fixture hình học. | — | Bước 1 (Song song) |
| **`chat-contract-domain`** | Sở hữu Domain Models bất biến (`ChatThread`, `ChatMessage`, `ProductSnapshot`, `CoolingOffInfo`, `ReadOnlyReason`), giao diện `ChatRepository` chuẩn hóa 13 operations theo OpenAPI 3.0.3, logic tính toán thời gian hạ nhiệt monotonic (`elapsedRealtime`), và chính sách Idempotency (`Idempotency-Key`, `clientMessageId`). | — | Bước 1 (Song song) |
| **`chat-data-fake`** | Triển khai `FakeChatRepository` hỗ trợ đầy đủ 13 endpoints (tra cứu lịch sử sản phẩm, tạo thread nguyên tử cùng firstMessage, streaming phản hồi SSE giả lập, hủy generation, quản lý tham chiếu, cập nhật snapshot). Cập nhật Room Entity/DAO để lưu cache offline an toàn theo kiến trúc Clean Architecture. | `chat-contract-domain` | Bước 2 |
| **`chat-ui-screens`** | Xây dựng và hoàn thiện UI 3 màn hình và các components dùng chung: <br>• **Components:** `ChatComposerBar` (IME padding, max 6 dòng), `ChatMessageBubble` (phân tích từng món qua `productSnapshots`, không phán quyết), `ProductContextCard` & `ReferenceChipsRow`, `CoolingOffIndicator`.<br>• **Màn hình CL:** Header mascot, search bar, thread items với thumbnail chính + badge `+N`, cooling-off status, không có filter cũ.<br>• **Màn hình NC:** Chọn duy nhất 1 sản phẩm chính (radio), tra cứu lịch sử (tiếp tục / tạo mới / xem lưu trữ), draft client không ghi server trước tin đầu, không Free Chat.<br>• **Màn hình CD:** Header cố định, danh sách tham chiếu cuộn ngang (thêm/bớt), timeline LazyColumn giữ vị trí khi tải tin cũ, khóa tham chiếu khi AI stream, hủy generation trước khi lưu trữ/xóa.<br>• **Điều hướng:** Home $\to$ CL; Item Detail ("Cân nhắc cùng Chanko") $\to$ NC/CD. | `chat-assets-tokens`, `chat-contract-domain`, `chat-data-fake` | Bước 3 |

**Thứ tự xây dựng (Build Order):**  
(`chat-assets-tokens` $\parallel$ `chat-contract-domain`) $\longrightarrow$ `chat-data-fake` $\longrightarrow$ `chat-ui-screens`

---

## 2. Core Spec Areas (Phase 1: Specify)

### 2.1. Mục tiêu (Objective)
- Chuẩn hóa toàn bộ trải nghiệm Chat v2.2 trên Android, hoàn thành 27 tiêu chí UX gốc (3 CHAT + 12 GAP + 12 EDGE).
- Xóa bỏ triệt để các tàn dư cũ: không còn "Trò chuyện tự do", không còn bộ lọc trạng thái (Đang cân nhắc / Chờ xem lại), không còn thẻ phán quyết BUY/WAIT/SKIP.
- Đảm bảo khả năng phục hồi lỗi mạng tuyệt đối: gửi tin idempotent, không bao giờ sinh tin nhắn hoặc thread trùng lặp khi retry.

### 2.2. Lệnh thực thi & Kiểm chứng (Commands)
- **Build APK:** `./gradlew :app:assembleDevDebug --no-configuration-cache`
- **Unit Tests:** `./gradlew :app:testDevDebugUnitTest --no-configuration-cache`
- **Lint Check:** `./gradlew :app:lintDevDebug --no-configuration-cache`

### 2.3. Cấu trúc Dự án (Project Structure)
- `presentation/features/chat/`: `list/`, `newchat/`, `detail/`, `components/`
- `domain/chat/`: `model/`, `repository/`
- `data/chat/`: `dto/`, `fake/`, `local/`, `mapper/`
- `app/src/main/res/`: Các tài nguyên icon, font, mascot được tích hợp.

### 2.4. Phong cách Mã nguồn (Code Style)
- Clean Architecture, MVVM, Stateless UI + Stateful Route.
- Semantics đầy đủ cho trợ năng (Role Radio, StateDescription, ContentDescription tiếng Việt).
- Toàn bộ touch targets $\ge 48\,\text{dp}$.

### 2.5. Chiến lược Kiểm thử (Testing Strategy)
- Unit tests toàn diện cho `ChatListViewModel`, `NewChatViewModel`, `ChatViewModel`, và `FakeChatRepository`.
- 100% test pass trên JVM thông qua `./gradlew :app:testDevDebugUnitTest`.
- Xác nhận kiểm thử trợ năng (TalkBack/IME) sẽ do User trực tiếp thực hiện trên thiết bị vật lý ở giai đoạn tích hợp runtime.

### 2.6. Ranh giới (Boundaries)
- **Luôn làm:** Kiểm tra build/test sau mỗi bước; text tiếng Việt tự nhiên; giữ đúng màu CTA tương phản cao `#7043D6`.
- **Hỏi trước:** Nâng cấp schema Room Database; thêm dependency mới.
- **Không bao giờ:** Cài đặt/chạy máy ảo hoặc thiết bị thật (`AGENTS.md`); đụng chạm thư mục `.agent/skills/`; khôi phục Free Chat hoặc Decision Card cũ.

---

## 3. Danh sách Giả định Ban đầu (Assumptions)

1. **Về Backend:** Tuân thủ B07 (`Contract Ready — Pending Backend Implementation`). Toàn bộ luồng dữ liệu của Phase 1 sẽ chạy trên `FakeChatRepository` độc lập, không phụ thuộc vào backend FastAPI thật.
2. **Về Tài nguyên:** Toàn bộ 39 icon vector, mascot WebP và font Nunito có trong `Chanko-Chat-UI-v2.2/android/res` sẽ được sao chép vào `app/src/main/res/` khi bắt đầu thực hiện Phase 4.
3. **Về Trợ năng & Runtime:** Agent chỉ thực hiện build và unit tests trên máy Mac; các bước kiểm tra TalkBack và bàn phím IME trên máy thật do User đảm nhiệm theo đúng `docs/USER-CHECKLIST.md`.

---

## 4. Bảng Câu Hỏi Mở & Cần Phê Duyệt (Open Questions & Approval Request)

1. **Về việc chuyển giao tài nguyên:** Bạn có chấp thuận việc copy các asset (`39 VectorDrawables`, `2 Mascot WebP nodpi`, `4 Font Nunito TTF`, `5 Fixtures PNG`) từ `Chanko-Chat-UI-v2.2/android/res` sang `app/src/main/res/` khi triển khai không?
2. **Về Room Database Migration:** Bạn muốn chúng ta thêm Room Migration an toàn từ version hiện tại sang version mới (hỗ trợ `primaryProductId`, `references`, `readOnlyReason`) hay fallback destructive migration cho môi trường Dev?
3. **Về Phê duyệt Spec & Capability Map:** Bạn có đồng ý với Capability Map (4 modules: `chat-assets-tokens`, `chat-contract-domain`, `chat-data-fake`, `chat-ui-screens`) và 27 tiêu chí nghiệm thu được mô tả ở trên không?
