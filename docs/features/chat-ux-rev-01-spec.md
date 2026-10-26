# Specification: Chat with Chanko — CHAT-UX-REV-01

**Ngày cập nhật:** 26/10/2026  
**Tài liệu cơ sở:** `Chat_with_Chanko_CHAT-UX-REV-01_2026-10-25` (bổ sung trên Handoff v1.4)  
**Trạng thái:** DRAFT FOR USER REVIEW (Gated Phase 1)  

---

## 1. Objective & Overview

Tái cấu trúc và xây dựng hoàn chỉnh trải nghiệm tính năng **"Chat with Chanko"** trên Android (Kotlin + Jetpack Compose) theo đúng chuẩn **CHAT-UX-REV-01 (25/10/2026)**:
- **Cấu trúc tinh gọn với 3 màn hình duy nhất:**
  1. **CL (Chat List):** Màn danh sách quản lý hội thoại dạng Messenger với thanh tìm kiếm, bộ lọc trạng thái, danh sách thread cards và nút cố định `+ Đoạn chat mới`. (Không có ô gõ chat ở đây).
  2. **CD (Chat Detail):** Màn chi tiết cuộc trò chuyện. Là nơi **duy nhất** có khung gõ tin nhắn (Composer). Chứa Timeline bong bóng chat, nhận định khách quan **Decision Card (Neutral BUY)**, gợi ý phản hồi nhanh (Quick Replies) và tính năng ghim liên kết món đồ trong cùng cuộc trò chuyện (Same-thread item bind).
  3. **NC (New Chat):** Màn hình/Sheet bắt đầu cuộc trò chuyện mới. Cho phép chọn giữa **"Trò chuyện tự do"** hoặc **"Về một món đồ"** (kèm bộ chọn món đồ inline). Bấm CTA sẽ mở màn CD ở trạng thái chưa gửi (unsent).
- **Kiến trúc điều hướng Nested:** Giữ nguyên 4 Tab chính trên thanh điều hướng BottomBar (`Trang chủ`, `Món đồ`, `Ngân sách`, `Phân tích`) và nút `+` toàn cục. Chat là một luồng con xuất phát từ CTA trên màn **Trang chủ (Home)** hoặc nút **"Cân nhắc cùng Chanko"** trên màn **Chi tiết món đồ (Item Detail)**.
- **Bảo toàn nguyên tắc thiết kế & nghiệp vụ:**
  - Quyết định BUY mang tính tư vấn trung tính: *"BUY · Có thể cân nhắc mua"*, không dùng màu xanh lá ăn mừng, không hiệu ứng pháo hoa/sparkles, mascot Neutral.
  - Phân định ranh giới độc lập tuyệt đối giữa **Chat History** và **Lịch sử quyết định chính thức (Official Decision Records / Item Evaluation)**.

---

## 2. Executable Commands

- **Build APK:** `./gradlew :app:assembleDevDebug --no-configuration-cache`
- **Unit Tests:** `./gradlew :app:testDevDebugUnitTest --no-configuration-cache`
- **Lint Check:** `./gradlew :app:lintDevDebug --no-configuration-cache`

---

## 3. Assumptions (Giả định cốt lõi)

1. **Về Điều hướng BottomBar:** Phục hồi thanh BottomBar về đúng **4 Tab nguyên bản** (`Trang chủ`, `Món đồ`, `Ngân sách`, `Phân tích`) cùng nút `+` toàn cục. Tab Chat thứ 5 trước đây sẽ được loại bỏ khỏi BottomBar. Lối vào Chat sẽ qua nút CTA tại `Home` và `Item Detail`.
2. **Về Giai đoạn Triển khai (Local Fixture / Fake Repository):** Tập trung hoàn thiện 100% UI/UX của 3 màn CL, CD, NC, toàn bộ Dialogs/BottomSheet và State Machine (offline/pending/error/retry) sử dụng Deterministic Fake Repository gắn nhãn `Bản mẫu · Phản hồi dựng sẵn` trước khi cắm nối Backend FastAPI.
3. **Về RENAME-API:** Tính năng đổi tên cuộc trò chuyện (Rename) được hỗ trợ đầy đủ trên giao diện và Room Local Cache. Ghi nhận `RENAME-API` là API gap cho backend (chưa claim cloud sync).
4. **Về Asset Mascot & Icons:** Sử dụng ảnh tĩnh Mascot Neutral/Curious chuẩn canonical từ bộ assets đính kèm (`02_ASSETS/canonical`). Icon sử dụng Material Symbols Rounded vector.

---

## 4. Capability Map & Build Order

```
┌────────────────────────────────────────────────────────┐
│                   CAPABILITY MAP                       │
├────────────────────┬───────────────────────────────────┤
│ Module ID          │ Trách nhiệm chính                 │
├────────────────────┼───────────────────────────────────┤
│ core-chat-domain   │ Data models (Thread, Message,     │
│                    │ Decision, ContextRevision),       │
│                    │ Repository interfaces             │
├────────────────────┼───────────────────────────────────┤
│ core-chat-data     │ Room Entities, DAO, Deterministic │
│                    │ Fake Source, Repository Impl      │
├────────────────────┼───────────────────────────────────┤
│ ui-chat-shared     │ Bubble, DecisionCard (Neutral),   │
│                    │ ComposerBar, ConnectivityBanner,  │
│                    │ Options & Confirmation Dialogs    │
├────────────────────┼───────────────────────────────────┤
│ ui-chat-list (CL)  │ App Bar, Hero, Search & Filter    │
│                    │ chips, Thread rows, Fixed CTA (+) │
├────────────────────┼───────────────────────────────────┤
│ ui-chat-new (NC)   │ Mode selector (Free/Item),        │
│                    │ Inline item picker, Start CTA     │
├────────────────────┼───────────────────────────────────┤
│ ui-chat-detail(CD) │ Message Timeline, Quick Replies,  │
│                    │ Composer, Same-thread bind sheet  │
├────────────────────┼───────────────────────────────────┤
│ chat-navigation    │ Home/ItemDetail entry, Back stack,│
│                    │ 4-tabs BottomBar restoration      │
└────────────────────┴───────────────────────────────────┘

Thứ tự triển khai (Build Order):
core-chat-domain ➔ core-chat-data ➔ ui-chat-shared ➔ ui-chat-list (CL) ➔ ui-chat-new (NC) ➔ ui-chat-detail (CD) ➔ chat-navigation
```

---

## 5. Screen Specifications (Chi tiết 3 màn hình)

### 5.1. Màn hình CL — Chat List (Danh sách cuộc trò chuyện)
- **Cấu trúc từ trên xuống:**
  1. **Top Header:** 
     - Tiêu đề: `"Chat với Chanko"` (24sp, bold 700, màu `textPrimary`).
     - Nút `More` (48x48dp target): Mở menu/sheet quản lý, bao gồm bộ lọc `"Đã lưu trữ"`.
  2. **Subtitle:** `"Cùng suy nghĩ kỹ hơn trước mỗi quyết định"` (14sp/20sp).
  3. **Hero Banner:** Thẻ bo góc `radius32`, nền pastel gradient nhẹ, chứa mascot Chanko (148dp contain) và lời nhắn nhủ không thiên vị.
  4. **Search Bar:** Chiều cao tối thiểu 48dp, bo góc 16dp, placeholder: `"Tìm cuộc trò chuyện"`. Hỗ trợ tìm kiếm theo tiêu đề thread và tên món đồ liên kết.
  5. **Filter Chips (Cuộn ngang):** `"Tất cả"`, `"Đang cân nhắc"`, `"Chờ xem lại"`, `"Đã mua"`.
  6. **Thread List (LazyColumn):** 
     - Mỗi hàng (Thread Card): Bo góc 24dp, thumbnail/avatar 56dp bên trái (món đồ hoặc icon Chanko).
     - Tiêu đề thread (17sp bold, tối đa 2 dòng).
     - Đoạn xem trước tin nhắn (14sp, tối đa 2 dòng).
     - Thời gian (12sp, ví dụ: *"09:42"*, *"Hôm qua"*).
     - Tag loại cuộc trò chuyện (`# Tự do` hoặc `# Tên món đồ`).
     - Nhấn vào hàng: Điều hướng sang màn CD (Chat Detail).
     - Long press hoặc nút More trên hàng: Mở BottomSheet tùy chọn: *Đổi tên, Lưu trữ/Khôi phục, Xóa chat, Xem món đồ*.
  7. **Fixed Footer CTA:** Nút `"+ Đoạn chat mới"` (chiều cao 56dp, bo góc 20dp) ghim cố định ở đáy màn hình (trên insets an toàn). Danh sách có padding đáy tương ứng để không bị che khuất.
  - **Lưu ý:** Tuyệt đối **không** có ô gõ tin nhắn (composer) tại CL.

### 5.2. Màn hình CD — Chat Detail (Chi tiết cuộc trò chuyện)
- **Cấu trúc:**
  1. **Top Bar:** 
     - Nút Quay lại (`ArrowBack`): Nếu vào từ CL thì back về CL; nếu vào trực tiếp từ Item Detail thì back về Item Detail.
     - Avatar tròn 40dp + Tên món đồ (hoặc *"Trò chuyện tự do"*) + Trạng thái kết nối. Nhấn vào tên món đồ sẽ mở màn Chi tiết món đồ.
     - Nút More (48x48dp): Mở Sheet tùy chọn thread.
  2. **Dòng ngữ cảnh (Provenance):** Khi thread đã liên kết món đồ: `"Đã liên kết với [tên món đồ] từ [thời điểm]"`.
  3. **Connectivity Banner (nếu offline):** `"Đang xem dữ liệu đã lưu"` (vẫn đọc được toàn bộ tin nhắn offline theo Hợp đồng A4).
  4. **Timeline tin nhắn (LazyColumn):**
     - **Bong bóng User:** Canh phải, tối đa 82% chiều rộng, nền lavender dịu (`#F0E9FF`), chữ tối độ tương phản cao, kèm thời gian.
     - **Bong bóng Chanko:** Canh trái, kèm avatar Chanko 32dp, nền trắng bo cong, kèm nhãn *"Chanko"* và thời gian.
     - **Decision Card (Thẻ nhận định Chanko):**
       - Render từ kết quả thẩm định structured: `BUY` (*"Có thể cân nhắc mua"*), `WAIT` (*"Có thể chờ thêm"*), `SKIP` (*"Có thể bỏ qua"*).
       - Thiết kế trung tính: Nền pastel lavender nhạt, chữ tối, mascot Neutral (không xanh lá cây chiến thắng, không pháo hoa/sparkles).
       - Chứa lý do thẩm định có căn cứ và nút `"Xem món đồ"`.
  5. **Quick Replies:** Hàng chip gợi ý câu trả lời phía trên ô gõ. Khi chạm vào: **chỉ điền vào ô composer (`FillComposer`)**, không tự động gửi.
  6. **Composer Bar:** 
     - Đặt cố định phía trên bàn phím (IME).
     - Nút `+` (Liên kết món đồ): Chỉ hiện khi ở chế độ `FREE`, nhấn vào mở Sheet chọn món đồ (Same-thread item bind).
     - Ô nhập đa dòng (min 48dp, max 120dp rồi cuộn).
     - Nút Gửi (`Send`): Chỉ kích hoạt khi có nội dung hợp lệ và thiết bị đang online.
     - Nếu thread bị Lưu trữ (`Archived`): Ẩn composer, hiện banner thông báo và nút `"Khôi phục cuộc trò chuyện"`.

### 5.3. Màn hình NC — New Chat (Bắt đầu đoạn chat mới)
- **Cấu trúc:** Dạng Full-height BottomSheet hoặc Full-screen dialog.
  1. **Header:** Nút Đóng (`Close`), Tiêu đề `"Đoạn chat mới"`, Phụ đề `"Chọn cách bắt đầu"`.
  2. **Hai thẻ chọn chế độ (Context Mode Cards):**
     - Thẻ 1: **"Trò chuyện tự do"** (Hỏi đáp, tâm sự chi tiêu, nhận khuyên can chung).
     - Thẻ 2: **"Về một món đồ"** (Chọn một món trong danh sách mong muốn để thẩm định).
  3. **Inline Item Picker:** 
     - Chỉ xuất hiện khi chọn thẻ *"Về một món đồ"*.
     - Có ô tìm kiếm món đồ, danh sách cuộn các món với thumbnail, giá tiền và radio button chọn duy nhất 1 món.
     - Nếu chưa có món đồ: Hiển thị trạng thái rỗng và nút điều hướng sang màn Món đồ.
  4. **Fixed Footer CTA:**
     - Nút `"Bắt đầu trò chuyện"` (khi chọn Tự do) hoặc `"Trò chuyện về [tên món đồ]"` (khi chọn Món đồ).
     - Nhấn CTA sẽ đóng NC và mở màn CD (Chat Detail) ở trạng thái rỗng chưa gửi. Tin nhắn đầu tiên gửi đi sẽ chính thức tạo ChatThread trong cơ sở dữ liệu.
  - **Lưu ý:** Tuyệt đối **không** có ô gõ tin nhắn (composer) tại NC.

---

## 6. Dialogs & Modal Contracts

1. **Đổi tên cuộc trò chuyện (Rename Dialog):**
   - Tiêu đề: `"Đổi tên cuộc trò chuyện"`. Ô nhập văn bản kèm nút `"Hủy"` và `"Lưu"`. Cập nhật tiêu đề hiển thị trong Room DB.
2. **Xác nhận Lưu trữ (Archive Confirmation):**
   - Thông báo rõ: Chat lưu trữ vẫn xem lại được trong bộ lọc "Đã lưu trữ", và việc lưu trữ chat **không** làm thay đổi trạng thái của món đồ.
3. **Xác nhận Xóa (Delete Confirmation):**
   - Cảnh báo: Lịch sử trò chuyện này sẽ bị xóa vĩnh viễn. **Toàn bộ lịch sử quyết định thẩm định, đánh giá và mua sắm của món đồ vẫn được giữ nguyên an toàn.**
4. **Xác nhận Liên kết món đồ (Same-thread Bind Confirmation):**
   - Thông báo: *"Liên kết cuộc trò chuyện này với [tên món đồ]? Toàn bộ tin nhắn cũ được giữ nguyên trong cùng cuộc trò chuyện. Tin nhắn tự do trước đây không trở thành lịch sử quyết định chính thức."*

---

## 7. Ranh giới & Bất biến (Boundaries & Invariants)

- **Always:**
  - Giữ nguyên 4 Tab chính của BottomBar (`Trang chủ`, `Món đồ`, `Ngân sách`, `Phân tích`).
  - Dữ liệu chat trong Room Database được bảo toàn đọc offline vô thời hạn (Hợp đồng A4).
  - Thẩm định BUY phải luôn hiển thị trung tính, không hiệu ứng chúc mừng hay pháo hoa.
- **Ask First:**
  - Mở rộng thêm bất kỳ màn hình nào ngoài bộ 3 CL/CD/NC.
  - Thay đổi schema thực thể `ShoppingItemEntity`.
- **Never:**
  - Không thêm Tab Chat thứ 5 trên thanh BottomBar.
  - Không đặt composer / ô gõ tin nhắn ở màn hình CL và NC.
  - Không xóa lịch sử quyết định (Decision Records) khi người dùng xóa phiên chat.
  - Không cho phép gửi tin nhắn khi offline.
