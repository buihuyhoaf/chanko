# Idea Refine: Chat with Chanko (Hybrid AI & Decision Engine)

## 1. Problem Statement
Làm thế nào để người dùng Chanko vừa có một người bạn đồng hành trò chuyện dí dỏm, vừa nhận được những phán quyết tài chính khách quan, chuẩn xác theo quy tắc cứng trước khi vung tiền mua sắm?

## 2. Kiến trúc & Công nghệ
- **Mobile (Android):** Kotlin + Jetpack Compose (Theme Candy Pop 3D) + MVVM + Clean Architecture + Hilt + Room (Cache đọc offline) + Coroutines/StateFlow + Retrofit/OkHttp.
- **Backend (Python / FastAPI):**
  - **API Layer:** FastAPI (RESTful, xử lý chat requests và điều phối luồng).
  - **Storage:** PostgreSQL (Lưu threads, messages, decision history).
  - **Cache & Concurrency:** Redis (Cache hội thoại, distributed lock, idempotency key).
  - **Container:** Docker deployment.
- **AI & Decision Orchestration:**
  - **Hybrid AI Model:** LLM chịu trách nhiệm giao tiếp tự nhiên & trích xuất ngữ cảnh/thông số mua sắm.
  - **Deterministic Decision Engine:** Động cơ nghiệp vụ backend tự tính toán phán quyết `BUY`, `WAIT`, hoặc `SKIP` dựa trên ngân sách, lịch sử và quy tắc tài chính cứng (không phụ thuộc vào ảo giác của AI).
  - **Personality Layer:** AI nhận lại Verdict từ Decision Engine để sinh phản hồi phù hợp với tính cách đã chọn (`Friendly`, `Sassy`, hoặc `Savage`). Verdict bất biến, chỉ có sắc thái nói thay đổi.
  - **LLMProvider Abstraction:** Tạo interface tách biệt trong FastAPI để có thể chuyển đổi linh hoạt giữa OpenAI, Gemini, Claude hoặc self-hosted model mà không ảnh hưởng tới Android App.

## 3. Quyết định kỹ thuật quan trọng
- **Free Chat + Item Chat:** Hỗ trợ trò chuyện tự do hoặc gắn trực tiếp vào một món đồ cụ thể.
- **REST vs Streaming Transport:** MVP tuân thủ chuẩn REST completed-response (theo TECH-03). Trên Android UI có thể mô phỏng hiệu ứng đánh máy (Typewriter animation) để mang lại cảm giác mượt mà mà không phụ thuộc hạ tầng SSE/WebSocket ngay giai đoạn đầu.
- **Offline Mode:** Room làm cache cục bộ cho phép đọc lại lịch sử cũ mà không cần mạng. Chặn gửi tin nhắn và gọi AI khi offline.
- **Multi-device Sync:** Backend là Single Source of Truth (SSOT), Room là read/write cache.
- **R07 Deletion & Retention:** Khi người dùng xóa phiên chat, các bản ghi quyết định chính thức (Official Decision Records) gắn với Shopping Item vẫn được bảo lưu độc lập.
- **AI Context Window:** Chỉ gửi ngữ cảnh liên quan (món đồ hiện tại, 3–5 tin nhắn gần nhất, tóm tắt thông số), tránh nhồi toàn bộ lịch sử mua sắm vào prompt.

## 4. MVP Scope
- [x] Chat UI chuẩn Material 3 Candy Pop 3D trên Android (Message list, Input bar, Quick replies).
- [x] API chat FastAPI tích hợp `LLMProvider` abstraction layer.
- [x] Động cơ Decision Engine chuẩn hóa phán quyết `BUY`/`WAIT`/`SKIP`.
- [x] Lưu trữ Room offline cache & đồng bộ PostgreSQL.
- [x] Xử lý kịch bản xóa chat bảo tồn Decision Records.

## 5. Not Doing (Tạm hoãn ở MVP)
- Chưa triển khai SSE/WebSocket Streaming thật ở backend.
- Không để AI tự ý đưa ra phán quyết chi tiêu vượt quyền Decision Engine.
- Chưa triển khai nhận diện hình ảnh/OCR bill mua sắm.
