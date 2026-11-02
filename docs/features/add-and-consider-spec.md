# Đặc tả Kỹ thuật & Nghiệp vụ: Chanko Thêm & Cân Nhắc Món Đồ

- **Feature Name:** Thêm & Cân nhắc món đồ (Add and Consider Flow)
- **Feature Branch:** `feature/add-consider-flow` (tách từ `dev-chanko-1x`)
- **Status:** **UX approved · integration pending**
- **Author / Source:** Gói bàn giao thiết kế `chanko-add-consider-v1.0/` kết hợp quyết định cập nhật đơn giản hóa luồng (Cập nhật 2026-10-29).

---

## 1. Bối cảnh & Mục tiêu

Thiết kế luồng giúp người dùng kiềm chế mua sắm bốc đồng (impulse buying), chuyển hóa hành vi mua sắm từ "thấy link là mua ngay" sang "nhập link qua Dialog → kiểm tra hoặc nhập thông tin → tự vấn phản tư cùng Chanko → nhận định khách quan → lưu vào My Items".

### Luồng Trải nghiệm Người dùng Mới (New Streamlined UX Flow)
1. **Entry Point (Nút `+` ở Bottom Bar):**
   - Người dùng nhấn nút `(+)` ở Bottom Bar.
   - Hiển thị ngay **Dialog Nhập URL** (`UrlInputDialog`).
   - Dialog gồm:
     - Tiêu đề: *"Thêm món đồ"*
     - Ô nhập URL: Hỗ trợ dán liên kết sản phẩm (`http`/`https` có host).
     - Hai nút hành động:
       - **"Hủy"**: Đóng Dialog, giữ nguyên màn hình hiện tại.
       - **"Lấy thông tin"**: Bắt đầu tiến trình trích xuất URL (chỉ kích hoạt khi URL hợp lệ).
2. **Xử lý Kết quả Trích xuất URL:**
   - **Trích xuất thành công (hoặc một phần):**
     - Đóng `UrlInputDialog`.
     - Chuyển thẳng đến màn hình **Kiểm tra thông tin** (`VerifyItemInfoScreen`) với các trường đã lấy được điền tự động (người dùng kiểm tra và bổ sung trường còn thiếu).
   - **Trích xuất thất bại (Lỗi / Không hỗ trợ):**
     - Hiển thị Popup cảnh báo:
       - Tiêu đề: *"Không thể lấy thông tin sản phẩm"*
       - Nội dung: *"Chanko không thể tự động trích xuất liên kết này. Bạn có muốn chuyển sang nhập thông tin thủ công không?"*
       - 2 nút hành động:
         - **"Hủy"**: Đóng Popup, quay lại `UrlInputDialog` với URL vẫn được giữ nguyên.
         - **"Xác nhận"**: Đóng Popup & `UrlInputDialog`, chuyển thẳng đến màn hình **Nhập thông tin thủ công** (`ManualItemInputScreen`).
3. **Màn hình Nhập Liệu (Kiểm tra thông tin hoặc Nhập thủ công):**
   - Điền đầy đủ các trường bắt buộc: Tên món đồ, Giá dự kiến, Danh mục, Lý do muốn mua.
   - Xác thực giá cụ thể (ADD-07 A): Checkbox *"Tôi xác nhận giá {formattedPrice}"*.
   - Bấm CTA *"Cân nhắc cùng Chanko"* để tiếp tục.
4. **Màn Quick Check (`QuickCheckScreen`):**
   - Khảo sát $1\text{--}3$ câu hỏi phản tư thích ứng ("Câu {n} · tối đa 3 câu"), có nút "Nhận định sớm".
5. **Màn Kết quả (`DecisionResultScreen`):**
   - Nhận định trung tính: `BUY` (Vàng), `WAIT` (Sky), `SKIP` (Mint) không confetti.
   - Nhận định `WAIT`: Bắt buộc người dùng chọn giữa *"Áp dụng thời gian chờ"* hoặc *"Không áp dụng thời gian chờ"*.
6. **Lưu Món Đồ:**
   - Bấm *"Hoàn tất và lưu món đồ"*: Lưu **Item + Decision + History Record** vào cơ sở dữ liệu.
   - Chuyển đến `ItemDetailScreen` trong **My Items**.

---

## 2. Quyết định Nghiệp vụ & Khóa Bất biến Cập nhật

### 2.1. Xóa bỏ Hoàn toàn Chức năng Bản nháp (No Draft Policy)
- **Bãi bỏ hoàn toàn các quyết định liên quan đến lưu nháp (Thay thế ADD-03 A & ADD-05 A):**
  - **Không lưu bản nháp** vào Room Database hay bất kỳ bộ nhớ bền vững nào.
  - **Không có tính năng khôi phục nháp**, không hiển thị Dialog *"Tiếp tục bản nháp?"*.
  - Toàn bộ dữ liệu luồng thêm đồ chỉ tồn tại trong vòng đời màn hình / ViewModel (In-Memory).
  - **Thoát trước khi hoàn tất:** Nếu người dùng nhấn nút Back hệ thống, nút đóng hoặc hủy luồng ở bất kỳ bước nào, toàn bộ dữ liệu chưa lưu sẽ bị hủy bỏ ngay lập tức mà không cần xác nhận lưu nháp.

### 2.2. Quyết định ADD-04 A & ADD-06 A: Phát hiện Trùng lặp URL (Duplicate Detection)
- Khi nhập URL đã tồn tại trong danh sách đồ cá nhân:
  - Hiển thị `DuplicateWarningSheet`: Cảnh báo món đồ có thể đã tồn tại.
  - Tùy chọn 1: *"Xem món đồ cũ"* $\rightarrow$ Đóng luồng thêm, mở `ItemDetailScreen` của món cũ.
  - Tùy chọn 2: *"Tiếp tục thêm mới"* $\rightarrow$ Cho phép tiếp tục tạo item mới hoàn toàn độc lập, có lịch sử và nhận định riêng; không gộp ngầm, không xóa món cũ.

### 2.3. Quyết định ADD-07 A: Xác thực Mức giá Cụ thể (Price Validation)
- Bắt buộc xác nhận một mức giá cụ thể $> 0$ (VNĐ).
- Không chấp nhận khoảng giá hoặc số âm.
- Checkbox bắt buộc: *"Tôi xác nhận giá {formattedPrice}"*. Nút tiếp tục sang Quick Check chỉ kích hoạt khi đã tick checkbox và giá hợp lệ. Khi đổi giá, checkbox tự động hủy tick.

### 2.4. Trích xuất URL (URL Extraction Handler)
- Giao diện cung cấp `UrlExtractionService`.
- Khi chưa có backend bóc tách dữ liệu: Service trả về `ExtractionResult.Failed` $\rightarrow$ Kích hoạt Popup hỏi chuyển sang nhập thủ công như mục 1.

---

## 3. Kiến trúc Luồng Màn hình (Screen & Dialog Architecture)

```
[Màn hình chính (Home) / Bottom Bar]
                 │
           Nhấn nút (+)
                 │
                 ▼
       ┌──────────────────┐
       │  UrlInputDialog  │ ◄───────┐
       └─────────┬────────┘         │
                 │ Bấm "Lấy thông tin"
                 ▼                  │
        Trích xuất URL              │
                 │                  │
        ┌────────┴────────┐         │
   Thành công          Thất bại     │ Bấm "Hủy"
        │                 │         │
        │                 ▼         │
        │           ┌───────────┐   │
        │           │   Popup   │───┘
        │           │ Cảnh báo  │
        │           └─────┬─────┘
        │                 │ Bấm "Xác nhận"
        ▼                 ▼
 ┌───────────────┐  ┌──────────────────┐
 │VerifyItemInfo │  │ ManualItemInput  │
 └───────┬───────┘  └────────┬─────────┘
         │                   │
         └─────────┬─────────┘
                   ▼
         Xác nhận giá cụ thể (ADD-07 A)
                   │
                   ▼
        [QuickCheckScreen] (1–3 câu hỏi · Nhận định sớm)
                   │
                   ▼
       [DecisionResultScreen] (BUY / WAIT / SKIP)
                   │
                   ▼
         [Lưu vào My Items] ──► Mở ItemDetailScreen
```

---

## 4. Danh mục Thành phần Kỹ thuật (Technical Inventory)

1. **Giao diện & Dialogs:**
   - `UrlInputDialog`: Dialog nhập link (`ChankoTextField`, nút "Hủy", nút "Lấy thông tin").
   - `ExtractionFailedDialog`: Popup khi trích xuất thất bại (nút "Hủy", nút "Xác nhận").
   - `VerifyItemInfoScreen`: Màn hình kiểm tra và bổ sung thông tin từ link.
   - `ManualItemInputScreen`: Màn hình nhập tay toàn bộ thông tin sản phẩm.
   - `QuickCheckScreen`: Màn hình trả lời câu hỏi tự vấn phản tư.
   - `DecisionResultScreen`: Màn hình hiển thị kết quả và cấu hình thời gian chờ cho WAIT.
2. **Logic & ViewModel:**
   - `AddItemViewModel`: Quản lý state in-memory trong suốt luồng (không ghi Room DB draft).
   - `UrlExtractionService`: Contract và mock service xử lý trích xuất URL.
   - `MockQuickCheckEngine`: Cung cấp câu hỏi và nhận định theo fixture chuẩn từ thiết kế.
3. **Data & Persistence:**
   - Không tạo `AddItemDraftEntity` hay `DraftDao`.
   - `SaveItemUseCase` / `ItemRepository`: Lưu giao dịch **Item + Decision + History Record** khi hoàn tất ở màn hình kết quả.

---

## 5. Tiêu chuẩn Ràng buộc (Constraints)

1. **Tuân thủ AGENTS.md:**
   - Tuyệt đối không cài đặt lên máy thật hoặc máy ảo (không `adb install`).
   - Chỉ build và verify kết quả qua command line: `./gradlew assembleDevDebug`, `./gradlew testDevDebugUnitTest`.
   - 100% văn bản giao diện bằng Tiếng Việt.
2. **Design Tokens & Accessibility:**
   - Màu nhận định chuẩn: `BUY` (`#FFD75E`), `WAIT` (`#8DCDF8`), `SKIP` (`#78DCC3`), nền `#FAF8F5`.
   - Vùng chạm tối thiểu $48\times 48\text{dp}$. Hỗ trợ TalkBack accessibility semantics đầy đủ.
