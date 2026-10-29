# Specification: Thêm & Cân Nhắc Món Đồ (Add & Consider Flow) — v1.0

> **Mã tính năng:** `chanko-add-consider-v1.0`  
> **Nguồn bàn giao:** `chanko-add-consider-v1.0` (29/10/2026 · Android · Tiếng Việt · Light Theme)  
> **Trạng thái:** DRAFT FOR USER REVIEW (Phase 1 Gated)

---

## 1. Objective (Mục Tiêu & Tầm Nhìn)

Xây dựng luồng nghiệp vụ cốt lõi **"Thêm & Cân nhắc món đồ" (Add & Consider Flow)** trên Android (Kotlin + Jetpack Compose) theo đúng chuẩn thiết kế Candy Pop 3D và 5 quyết định bất biến (ADD-03 A $\rightarrow$ ADD-07 A):
1. **Link-first & Input tin cậy (Màn 01):** Cho phép dán link sản phẩm (Shopee, Lazada, Tiki...) để tự động bóc tách thông tin hoặc nhập thủ công ngay từ đầu; phát hiện món trùng lặp (Duplicate Warning) mà không gộp ngầm; yêu cầu xác nhận một mức giá cụ thể $> 0$.
2. **Khảo sát phản xạ tài chính Adaptive (Màn 02 - Quick Check):** Khảo sát thích ứng từ $1$ đến tối đa $3$ câu hỏi (không gượng ép đủ 3 câu); hỗ trợ tính năng "Nhận định sớm" khi người dùng muốn có kết luận ngay.
3. **Nhận định trung tính & Làm nguội (Màn 03 - Result):** Hiển thị nhận định khách quan với 3 kết quả:
   * **BUY (Có thể mua):** Vàng `#FFD75E` (`ITM_09`), không pháo hoa/confetti ăn mừng, không kích thích chốt đơn.
   * **WAIT (Nên chờ thêm):** Xanh trời `#8DCDF8` (`ITM_10`), người dùng chủ động chọn "Áp dụng thời gian chờ" (Cooling-off) hoặc "Không áp dụng" (không tự ép 7 ngày).
   * **SKIP (Chưa nên mua):** Xanh bạc hà `#78DCC3` (`ITM_11`), khuyên nhìn lại nhu cầu, không gợi ý sản phẩm thay thế.
4. **Bản nháp kiên định (Draft Invariant):** Duy trì tối đa 1 bản nháp duy nhất trên mỗi tài khoản; khi nhấn (+) nếu có nháp dở dang $\rightarrow$ hỏi *"Tiếp tục bản nháp"* hay *"Bắt đầu món mới"*.
5. **Lưu trữ nguyên tử (Atomic Persistence):** Lưu đồng thời Item + Decision + History vào Room DB trong 1 giao thức logic, xóa bản nháp, dọn dẹp back stack và điều hướng trực tiếp vào màn hình `ItemDetailScreen` của món đồ vừa tạo.

---

## 2. Capability Map & Build Order (Phase 0)

| Module ID | Trách nhiệm chính | Phụ thuộc (Depends on) |
| :--- | :--- | :--- |
| **`add-item-draft`** | Quản lý vòng đời bản nháp (Single Draft per Account), lưu trữ bền vững qua process death (Room/DataStore), Dialog hỏi Tiếp tục / Thay thế khi bấm (+). | `core:database` |
| **`add-item-form`** | **Màn 01 (Thêm món đồ):** Dán link, bóc tách URL (giả lập/adapter), nhập thủ công, cảnh báo trùng (Duplicate Warning Sheet), chọn biến thể (Variant Sheet), xác nhận giá cụ thể $> 0$, chọn ảnh qua Photo Picker. | `add-item-draft` |
| **`add-item-quick-check`**| **Màn 02 (Cân nhắc cùng Chanko):** Adaptive question engine ($1\text{--}3$ câu hỏi), lưu vết câu trả lời, xử lý "Nhận định sớm" (Early Result) kèm giới hạn nhận định. | `add-item-form` |
| **`add-item-result-save`** | **Màn 03 (Kết quả) & Hoàn tất:** Hiển thị Card BUY / WAIT / SKIP, cấu hình Cooling-off (Sheet chỉnh số ngày), lưu nguyên tử vào Room, navigate `ItemDetail(itemId)` duy nhất và dọn dẹp draft. | `add-item-quick-check`, `core:domain` |

> **Thứ tự xây dựng (Build Order):**  
> `add-item-draft` $\longrightarrow$ `add-item-form` (Màn 01) $\longrightarrow$ `add-item-quick-check` (Màn 02) $\longrightarrow$ `add-item-result-save` (Màn 03)

---

## 3. Tech Stack & Commands

* **Ngôn ngữ:** Kotlin 2.0+
* **UI Toolkit:** Jetpack Compose + Material 3 (Theme Candy Pop Light)
* **Kiến trúc:** MVVM + Clean Architecture (StateFlow, Hilt DI, Room Database)
* **Design System Tokens:** `CandyColors`, `CandyShape`, `CandySpace`, `Nunito` typography, `ChankoIconId` (Bộ 74 Icon Candy Duotone v2.0-r1).

### Executable Commands:
```bash
Build: ./gradlew :app:assembleDevDebug --no-configuration-cache
Test:  ./gradlew :app:testDevDebugUnitTest --no-configuration-cache
Lint:  ./gradlew :app:lintDevDebug --no-configuration-cache
```

---

## 4. Project Structure (Bố Trí Mã Nguồn)

```
app/src/main/java/com/hoabh/chanko/
├── domain/additem/
│   ├── model/ (AddItemDraft, AssessmentSnapshot, Verdict, CoolingChoice, AddStep)
│   └── repository/ (DraftRepository, ProductExtractionGateway, QuestionEngineGateway)
├── data/additem/
│   ├── local/ (DraftDao, AddItemDraftEntity)
│   └── repository/ (DraftRepositoryImpl, FakeProductExtractionGateway, FakeQuestionEngineGateway)
└── presentation/screens/additem/
    ├── AddItemViewModel.kt              // Reducer quản lý FSM toàn bộ 3 màn & Draft
    ├── AddItemUiState.kt & UiEffect.kt  // Immutable UI states
    ├── screen/
    │   ├── AddItemFormScreen.kt         // Màn 01: Nhập thông tin & Link
    │   ├── QuickCheckScreen.kt          // Màn 02: Cân nhắc cùng Chanko (1-3 câu hỏi)
    │   └── DecisionResultScreen.kt      // Màn 03: Kết quả & Cấu hình chờ
    └── components/
        ├── DraftResumeDialog.kt         // Dialog: Tiếp tục nháp / Bắt đầu mới (ADD-03)
        ├── DuplicateWarningSheet.kt     // Sheet: Phát hiện trùng link/món (ADD-04)
        ├── VariantPickerSheet.kt        // Sheet: Chọn biến thể sản phẩm
        ├── CoolingDurationSheet.kt      // Sheet: Chọn số ngày chờ cho WAIT
        └── ExitConfirmDialog.kt         // Dialog: Rời luồng cân nhắc / Lưu nháp
```

---

## 5. Chi Tiết Quy Chuẩn Nghiệp Vụ & Giao Diện (Core Specifications)

### 5.1. Màn 01: Thêm món đồ (`AddItemFormScreen`)
* **Thứ tự bố cục:** TopAppBar (`Quay lại`) $\rightarrow$ Tiêu đề *"Bắt đầu từ món bạn muốn mua"* $\rightarrow$ Khung dán Link sản phẩm + CTA *"Dán link"* / *"Lấy thông tin"* $\rightarrow$ CTA *"Nhập thủ công"* $\rightarrow$ Khung thông tin:
  1. **Tên món đồ (Bắt buộc):** Input text, trim khoảng trắng.
  2. **Giá dự kiến (₫) (Bắt buộc):** Số nguyên minor-unit VND ($> 0$), định dạng hiển thị `vi-VN` (ví dụ: `2.490.000 ₫`). Không nhận khoảng giá, không nhận số âm hoặc 0 (ADD-07 A).
  3. **Xác nhận giá (Checkbox bắt buộc):** *"Tôi xác nhận mức giá cụ thể ở trên"*. Nếu người dùng sửa giá, link hoặc biến thể $\rightarrow$ checkbox tự động hủy xác nhận.
  4. **Danh mục (Bắt buộc):** Dropdown / Pill selector (Điện tử, Thời trang, v.v.).
  5. **Lý do muốn mua (Bắt buộc):** Textarea do chính người dùng nhập, AI không tự điền thay.
  6. **Ảnh sản phẩm (Tùy chọn):** Android Photo Picker (1 ảnh duy nhất, có placeholder *"Chưa có ảnh"*, hỗ trợ thay/xóa).
* **Cảnh báo trùng lặp (Duplicate Warning Sheet - ADD-04 A):**
  * Kiểm tra URL chuẩn hóa: nếu đã từng có món đồ dùng link này $\rightarrow$ mở Sheet hiển thị món cũ.
  * Hai hành động: *"Xem món đồ cũ"* (mở ItemDetail của món cũ, giữ nguyên draft) hoặc *"Tiếp tục thêm món mới"* (tạo món độc lập có history & verdict riêng - ADD-06 A).

### 5.2. Màn 02: Cân nhắc cùng Chanko (`QuickCheckScreen`)
* **Thứ tự bố cục:** TopAppBar $\rightarrow$ Product Summary Card (ảnh, tên, giá) $\rightarrow$ Nhãn tiến trình *"Câu {n} · tối đa 3 câu"* (không hiển thị dạng "1/3" cam kết cứng) $\rightarrow$ Mascot Placeholder tĩnh ($96\text{dp}$, rect bo góc, decorative) $\rightarrow$ Nội dung câu hỏi $\rightarrow$ Danh sách Radio choices $\rightarrow$ Footer:
  * Nút *"Tiếp tục"* (hoặc *"Xem nhận định"* khi đến câu cuối).
  * Nút phụ *"Nhận định sớm"* (Early Result): cho phép kết luận ngay dựa trên dữ kiện đã có, kết quả sẽ kèm cảnh báo *"Nhận định có giới hạn"*.
* **Nguyên tắc Revisions:** Nếu quay lại câu trước đổi đáp án $\rightarrow$ invalidate các câu hỏi nhánh sau, tính toán lại branch mà không làm tăng tổng số câu vượt quá 3.

### 5.3. Màn 03: Kết quả (`DecisionResultScreen`)
* **Bảng mã màu & Phản hồi Verdict trung tính:**
  * **BUY (`#FFD75E`):** Nhãn *"Có thể mua"* (`ChankoIconId.ITM_09`). Đưa ra nhận định trung lập dựa trên lý do người dùng nêu. Tuyệt đối không pháo hoa/confetti ăn mừng, không khuyến khích đặt hàng.
  * **WAIT (`#8DCDF8`):** Nhãn *"Nên chờ thêm"* (`ChankoIconId.ITM_10`). Bắt buộc người dùng phải chọn rõ ràng: *"Áp dụng thời gian chờ"* (mở Sheet cấu hình ngày) hoặc *"Không áp dụng"* trước khi nút Lưu sáng đèn. Không tự động gán mặc định 7 ngày nếu chưa có xác nhận.
  * **SKIP (`#78DCC3`):** Nhãn *"Chưa nên mua"* (`ChankoIconId.ITM_11`). Khuyên suy nghĩ lại nhu cầu, không gợi ý mua sản phẩm thay thế.
* **Hoàn tất & Điều hướng:**
  * Bấm *"Hoàn tất và lưu món đồ"*: Thực thi transaction lưu Item + Decision + History + Cooling-off vào Room Database.
  * Thành công $\rightarrow$ Xóa bản nháp, điều hướng duy nhất tới `Screen.ItemDetail.createRoute(newId)`, pop toàn bộ 3 màn của Add Flow khỏi back stack.

### 5.4. Cơ chế Bản Nháp (Draft Management - ADD-03 A & ADD-05 A)
* **Bảo toàn dữ liệu:** Bản nháp được lưu bền vững (Room Database) phân vùng theo `accountId`, sống sót qua Process Death và xoay màn hình.
* **Tương tác khi bấm (+):**
  * Nếu tài khoản đang có 1 bản nháp dở dang $\rightarrow$ Hiện `DraftResumeDialog` gồm thông tin món + bước hiện tại kèm 2 lựa chọn:
    1. *"Tiếp tục bản nháp"*: Khôi phục chính xác form, URL, answers và bước dở dang.
    2. *"Bắt đầu món mới"*: Mở tiếp Dialog xác nhận *"Thay thế bản nháp?"* (*"Bản nháp hiện tại sẽ bị bỏ."*). Chỉ xóa draft cũ khi người dùng bấm *"Thay thế và bắt đầu"*.

---

## 6. Testing Strategy & Kế Hoạch Nghiệm Thu (Acceptance)

### 6.1. Unit Tests (Robolectric / Local JVM)
1. **`DraftRepositoryTest`:** Xác minh 1 draft duy nhất / account, lưu và khôi phục toàn vẹn dữ liệu form, xóa nháp an toàn.
2. **`AddItemViewModelTest`:**
   * Kiểm thử FSM chuyển đổi giữa 3 màn (`FORM` $\rightarrow$ `QUICK_CHECK` $\rightarrow$ `RESULT`).
   * Kiểm thử xác nhận giá: từ chối số âm, số 0, khoảng giá; tự động hủy confirm khi sửa giá.
   * Kiểm thử luồng Early Result và giới hạn nhận định.
   * Kiểm thử tính năng Duplicate Warning (tiếp tục tạo item độc lập, không gộp).
   * Kiểm thử tính năng WAIT bắt buộc chọn Apply/None trước khi cho phép Save.

### 6.2. Compose UI & A11y Tests
1. **Touch Target & Spacing:** Mọi CTA $\ge 56\text{dp}$, Answer row $\ge 64\text{dp}$, vùng chạm $\ge 48\text{dp} \times 48\text{dp}$.
2. **Font Scale 200% & Landscape:** Thử nghiệm font $1.0\text{x} \rightarrow 2.0\text{x}$, văn bản tự động wrap nhiều dòng, không cắt cụt, footer tự động đưa vào danh sách cuộn dọc khi thiếu không gian.
3. **TalkBack Semantics:** Đọc đúng radio group `selected`, heading cho các tiêu đề, `contentDescription = null` cho icon trang trí cạnh chữ.

---

## 7. Boundaries (Ranh Giới Bất Biến)

* **Always:**
  * Tái sử dụng `CandyColors`, `ChankoIconId` (bộ 74 icon Candy Duotone), không tạo icon hay mascot mới.
  * Toàn bộ văn bản giao diện là Tiếng Việt chuẩn mực.
  * Giữ nguyên nút và thanh điều hướng 4 tab hiện có; sau khi lưu thành công, đích đến duy nhất là `ItemDetailScreen`.
* **Ask first:**
  * Thay đổi cấu trúc bảng Room Database hoặc thêm migration mới.
  * Tích hợp network API thật (hiện tại sử dụng Deterministic Fake Adapter).
* **Never:**
  * Tự động gộp món trùng hoặc xóa lịch sử đánh giá cũ.
  * Tự động đặt thời gian chờ 7 ngày cho kết quả WAIT mà không qua người dùng chọn.
  * Hiển thị hiệu ứng pháo hoa, sparkles hoặc ăn mừng khi nhận định BUY.
  * Cài đặt hoặc chạy emulator/máy ảo (chỉ build và verify trên JVM terminal theo quy tắc AGENTS.md).

---

## 8. Success Criteria (Tiêu Chí Thành Công)

1. [ ] Bấm (+) khi có nháp hiển thị đúng Dialog tiếp tục / bắt đầu mới.
2. [ ] Màn 01 hỗ trợ cả Dán link và Nhập thủ công; validate bắt buộc tên, giá cụ thể $> 0$, xác nhận checkbox, danh mục và lý do.
3. [ ] Cảnh báo trùng lặp hiển thị đúng món cũ và cho phép tạo món mới độc lập.
4. [ ] Màn 02 hiển thị câu hỏi thích ứng $1\text{--}3$ câu, hỗ trợ Nhận định sớm.
5. [ ] Màn 03 hiển thị đúng 3 verdict (BUY vàng, WAIT sky, SKIP mint); WAIT yêu cầu chọn thời gian chờ rõ ràng.
6. [ ] Lưu thành công ghi vào Room Database, xóa bản nháp và mở `ItemDetailScreen`.
7. [ ] Toàn bộ unit tests pass 100%, `./gradlew assembleDevDebug` biên dịch thành công 0 lỗi.

---

## 9. Quyết Định Đã Chốt Từ Người Dùng (Finalized Decisions)

1. **Điểm gắn kết với Bottom Bar FAB (Phương án A đã chốt):**
   * Nút (+) trung tâm trên Bottom Bar kích hoạt trực tiếp luồng **Thêm & Cân nhắc món đồ (`chanko-add-consider-v1.0`)**.
   * Khi người dùng nhấn (+):
     - Nếu có bản nháp dở dang $\rightarrow$ mở `DraftResumeDialog` (Tiếp tục bản nháp / Bắt đầu món mới).
     - Nếu không có bản nháp $\rightarrow$ mở thẳng Màn 01 (`AddItemFormScreen`).
   * Không sử dụng Speed Dial trung gian nữa; nút (+) trở về vai trò là phím tắt tạo món đồ duy nhất và trực tiếp nhất.
2. **Xử lý Bóc tách URL khi chưa có Backend Server:**
   * Khi người dùng bấm nút *"Lấy thông tin"* sau khi dán link: Hiển thị thông báo Toast tiếng Việt *"Tính năng chưa được hỗ trợ"* (hoặc thông báo trạng thái nhẹ nhàng).
   * Người dùng sử dụng luồng **Nhập thủ công (Manual Input)** để điền Tên món đồ, Giá dự kiến, Danh mục và Lý do muốn mua.
   * Toàn bộ form và validation vẫn hoạt động đầy đủ, không bị block bởi việc thiếu server.
