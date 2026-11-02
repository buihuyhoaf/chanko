# Tasks: Cải tiến UI/UX UrlInputDialog (Chuẩn Handoff v1)

## Phase 1: Assets & Resources Setup

### Task 1: Thiết lập Assets (Ảnh giỏ hàng 3D và các vector icon mới)
**Description:** Sao chép các tệp ảnh và vector icon từ thư mục handoff vào các thư mục tài nguyên tương ứng trong module `app`.
**Acceptance criteria:**
- [x] `chanko_url_basket.png` được đặt tại `app/src/main/res/drawable-nodpi/`.
- [x] Các icon vector `chanko_dialog_arrow.xml`, `chanko_dialog_link.xml`, `chanko_dialog_paste.xml`, `chanko_dialog_warning.xml` được đặt tại `app/src/main/res/drawable/`.
- [x] Tài nguyên không bị méo, hiển thị đúng cấu hình.
**Verification:**
- [x] Build succeeds: `./gradlew processDevDebugResources`
**Dependencies:** None
**Files likely touched:**
- `app/src/main/res/drawable-nodpi/chanko_url_basket.png`
- `app/src/main/res/drawable/chanko_dialog_arrow.xml`
- `app/src/main/res/drawable/chanko_dialog_link.xml`
- `app/src/main/res/drawable/chanko_dialog_paste.xml`
- `app/src/main/res/drawable/chanko_dialog_warning.xml`
**Estimated scope:** Small (1-2 config/asset changes)

---

### Task 2: Cập nhật String Resources tiếng Việt theo handoff
**Description:** Bổ sung các chuỗi tiếng Việt mới từ `chanko_dialog_strings.xml` vào `app/src/main/res/values/add_item_strings.xml` hoặc file strings riêng biệt, bảo đảm các text tiêu đề, gợi ý, cảnh báo và nút bấm khớp 100% với handoff.
**Acceptance criteria:**
- [x] Bổ sung các strings: `candy_url_title`, `candy_url_body`, `candy_url_label`, `candy_url_hint`, `candy_url_paste`, `candy_cancel`, `candy_extract`, `candy_extracting`, `candy_failure_title`, `candy_failure_body`, `candy_confirm`.
- [x] Giữ nguyên các string hiện có đang được các màn hình khác sử dụng.
**Verification:**
- [x] Build succeeds: `./gradlew processDevDebugResources`
**Dependencies:** None
**Files likely touched:**
- `app/src/main/res/values/add_item_strings.xml` (hoặc `chanko_dialog_strings.xml`)
**Estimated scope:** Small (1 file)

---

### Checkpoint 1: Assets & Resources
- [x] Các drawables và strings compile thành công, không bị trùng lặp ID.

---

## Phase 2: Component Implementation & Host Wiring

### Task 3: Xây dựng Composable `UrlInputDialog` và `ExtractionFailedDialog` mới
**Description:** Cập nhật lại `UrlInputDialog.kt` dựa trên thiết kế mẫu `CandyUrlDialogs.kt`: Card nền kem hồng `#FFF7F5`, bo góc `32dp`, viền trắng 1dp, shadow 12dp; ảnh 3D giỏ hàng tràn lên mép card 32dp; typography căn giữa; input bo tròn 20dp; cặp nút hành động tỷ lệ 40:60 (xếp dọc khi màn hình hẹp/font lớn).
**Acceptance criteria:**
- [x] `UrlInputDialog` hiển thị đúng giao diện card kem hồng, ảnh giỏ hàng 3D, ô nhập link có leading icon và trailing paste button.
- [x] Nút "Lấy thông tin" hiển thị gradient tím, lồng spinner loading 20dp khi `extracting = true`.
- [x] `ExtractionFailedDialog` hiển thị icon cảnh báo 40dp, tiêu đề và nội dung hỏi chuyển sang nhập thủ công với 2 nút Hủy và Xác nhận.
- [x] Hỗ trợ cuộn dọc `verticalScroll` và co giãn theo kích thước cửa sổ (`BoxWithConstraints`).
**Verification:**
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`
**Dependencies:** Task 1, Task 2
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/additem/UrlInputDialog.kt`
**Estimated scope:** Medium (1-2 files)

---

### Task 4: Tích hợp thao tác Clipboard và bàn phím (IME Action Done)
**Description:** Kết nối icon Dán trong ô nhập URL với `LocalClipboardManager` để dán link khi người dùng chạm, đồng thời hỗ trợ nhấn nút Done/Enter trên bàn phím ảo để tự động kích hoạt "Lấy thông tin".
**Acceptance criteria:**
- [x] Nhấn icon Dán đọc chuỗi từ `LocalClipboardManager` và cập nhật vào URL input (nếu clipboard có văn bản).
- [x] Vùng chạm của icon Dán tối thiểu 48×48dp chuẩn trợ năng.
- [x] Bàn phím hiển thị `ImeAction.Done`, khi nhấn Done thì gọi hành động trích xuất nếu URL không trống.
**Verification:**
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`
**Dependencies:** Task 3
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/additem/UrlInputDialog.kt`
**Estimated scope:** Small (1 file)

---

### Task 5: Cập nhật `AddItemEntryHost` điều phối hiển thị duy nhất 1 Dialog
**Description:** Tinh chỉnh `AddItemEntryHost` trong `UrlInputDialog.kt` để quản lý việc hiển thị luân phiên giữa `UrlInputDialog` và `ExtractionFailedDialog`, tránh tình trạng render 2 dialog xếp chồng lên nhau; ẩn bàn phím ảo khi mở popup lỗi.
**Acceptance criteria:**
- [x] Khi `state.extractionFailed == true`, chỉ hiển thị `ExtractionFailedDialog`.
- [x] Khi `state.extractionFailed == false`, hiển thị `UrlInputDialog`.
- [x] Nhấn nút Back hoặc chạm ngoài Scrim sẽ hủy phiên nhập và hủy extraction job đang chạy.
- [x] Nút Hủy trong popup lỗi đóng popup và quay lại `UrlInputDialog` với URL được giữ nguyên.
- [x] Nút Xác nhận chuyển sang bước `MANUAL_INPUT`.
**Verification:**
- [x] Unit tests pass: `./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.additem.*"`
**Dependencies:** Task 3, Task 4
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/additem/UrlInputDialog.kt`
**Estimated scope:** Small (1 file)

---

### Checkpoint 2: UI & Navigation
- [x] Giao diện Dialog mới hiển thị chuẩn xác, không bị đơ hoặc lỗi focus khi đổi qua lại giữa Dialog URL và Popup lỗi.

---

## Phase 3: Previews, Testing & Verification

### Task 6: Bổ sung Compose Previews cho các trạng thái Dialog
**Description:** Cập nhật hoặc thêm mới các Preview trong `AddItemPreviews.kt` cho `UrlInputDialog` (trạng thái bình thường, đang tải trích xuất, và popup lỗi).
**Acceptance criteria:**
- [x] Có Preview cho `UrlInputDialog` trạng thái bình thường.
- [x] Có Preview cho `UrlInputDialog` trạng thái đang tải (`extracting = true`).
- [x] Có Preview cho `ExtractionFailedDialog`.
- [x] Previews render thành công không báo lỗi.
**Verification:**
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`
**Dependencies:** Task 5
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/additem/AddItemPreviews.kt`
**Estimated scope:** Small (1 file)

---

### Task 7: Chạy kiểm thử tự động và verify build
**Description:** Thực thi toàn bộ unit tests và compile build để đảm bảo chất lượng, không vi phạm quy tắc CODING_STANDARDS và AGENTS.md (chỉ build và test, không cài đặt máy ảo/máy thật).
**Acceptance criteria:**
- [x] Chạy `./gradlew testDevDebugUnitTest` toàn bộ PASS.
- [x] Chạy `./gradlew assembleDevDebug` thành công với mã thoát 0.
**Verification:**
- [x] Tests pass: `./gradlew testDevDebugUnitTest`
- [x] Build succeeds: `./gradlew assembleDevDebug`
**Dependencies:** Task 6
**Files likely touched:** None (verification run)
**Estimated scope:** Verification task

---

### Checkpoint 3: Hoàn tất
- [x] Mọi tiêu chí nghiệm thu hoàn thành, sẵn sàng báo cáo người dùng.
