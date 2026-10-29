# Spec: C07 — Dialog Đổi tên thẻ tín dụng v2.1 (Rename Credit Card Dialog)

## Objective
Triển khai lát cắt giao diện **Dialog Đổi tên thẻ tín dụng (C07)** cho tính năng Thiết lập ví (Wallet Settings) theo gói bàn giao `Chanko-Budget-UI-v2.1-Rename-Updated-Handoff 2` và ảnh reference đã duyệt `reference/rename-credit-card-v2.1-approved-reference.png`:
1. Chuyển đổi C07 từ Bottom Sheet cũ thành **Modal Dialog căn giữa màn hình (Centered Dialog) trên nền dim của màn Thiết lập ví (C03)**.
2. Áp dụng override mới nhất: Giới hạn độ dài tên thẻ tối đa **30 graphemes** (đồng bộ với Dialog Thêm thẻ C06); bộ đếm ký tự hiển thị `n/30` tính toán chính xác theo chuẩn NFC (ví dụ: "Techcombank" là 11 ký tự, không chép số 12 từ ảnh mockup).
3. Hiển thị "Màu thẻ hiện tại" dưới dạng chấm màu và tên màu (chỉ đọc, không cho chọn/đổi màu).
4. Thẻ xem trước (Live Preview) cập nhật thời gian thực theo bản nháp tên đang nhập và màu sắc vốn có của thẻ; tuyệt đối không có logo VISA/Mastercard hay hình chip thẻ.
5. Hiển thị ghi chú rõ ràng: "Các giao dịch đã ghi nhận trước đây vẫn hiển thị theo tên thẻ cũ, không bị thay đổi."
6. Nút "Lưu thay đổi" bị khóa (disabled) khi tên không thay đổi (no-change), khi tên rỗng/quá 30 ký tự, khi trùng tên thẻ khác, khi offline hoặc chu kỳ đã kết thúc.

---

## Tech Stack
- **Framework & UI Toolkit:** Android Jetpack Compose (BOM 2024.09.00+), Material 3 (`androidx.compose.material3`).
- **Language & Runtime:** Kotlin 2.0+, Coroutines & StateFlow.
- **Theme & Design System:** Chanko Design System (`CandyColors`, `ChankoTypography`, `ChankoIcon`).
- **Target OS:** Android 14 / 15 (minSdk 26+).

---

## Commands
```bash
# Build dự án (chỉ build debug, không deploy thiết bị)
./gradlew assembleDevDebug

# Chạy toàn bộ Unit Tests liên quan
./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.budget.*"
./gradlew testDevDebugUnitTest --tests "com.hoabh.chanko.domain.budget.*"
```

---

## Project Structure
```
app/src/main/java/com/hoabh/chanko/
├── domain/budget/
│   ├── model/
│   │   └── CreditCard.kt                   → Giữ hàm validateName, normalizeName, bổ sung helper đếm grapheme max 30 cho UI
├── presentation/screens/budget/
│   ├── BudgetUiState.kt                    → State cho C07: cardToRename, renameDraft, renameError, isRenameSubmitting, isRenameDirty
│   ├── BudgetUiEvent.kt                    → Events: OpenRenameCardDialog, ChangeRenameDraft, SubmitRenameCard, DismissRenameCard
│   ├── BudgetViewModel.kt                  → Quản lý lifecycle C07, validation, dirty check, idempotency
│   └── wallet/
│       ├── dialogs/
│       │   └── RenameCreditCardDialog.kt   → Composable Modal Dialog C07 (Căn giữa, nền dim, preview, text ghi chú)
│       └── components/
│           └── CardPreview.kt              → Composable hiển thị thẻ xem trước trung lập dùng chung cho C06 và C07
```

---

## Detailed Component Specifications (C07)

### 1. Bố cục & Thành phần Giao diện
- **Kiểu hiển thị:** Modal Dialog căn giữa màn hình (Centered Dialog) trên nền dim của màn Thiết lập ví (`WalletSettingsScreen` - C03). Chặn touch và accessibility phía sau nền. Không drag handle, không swipe-to-dismiss.
- **Header:**
  - Icon minh họa nhỏ (trang trí nhãn thẻ).
  - Tiêu đề: "Đổi tên thẻ tín dụng".
  - Nút đóng (X) ở góc phải với contentDescription "Đóng".
- **Mô tả phụ:** "Cập nhật tên hiển thị của thẻ. Màu thẻ sẽ không thay đổi."
- **Trường nhập tên thẻ:**
  - Nhãn: "Tên thẻ" kèm dấu chỉ báo bắt buộc `*`.
  - Input một dòng, prefill sẵn tên hiện tại của thẻ khi mở dialog.
  - Bộ đếm ký tự: `n/30` (tối đa 30 graphemes theo chuẩn NFC sau khi trim).
  - Vùng hiển thị lỗi validation trực tiếp dưới ô nhập.
- **Thông tin màu hiện tại:**
  - Nhãn "Màu thẻ hiện tại".
  - Hiển thị chấm tròn màu và tên tiếng Việt của màu (ví dụ: chấm màu Hồng + chữ "Hồng").
  - Thành phần này hoàn toàn tĩnh (chỉ đọc): Không thể bấm, không có radio button, không chọn màu khác.
- **Thẻ xem trước (Live Preview):**
  - Tiêu đề phụ: "Xem trước thẻ".
  - Card bo tròn theo màu sắc hiện tại của thẻ (`colorId`), hiển thị tên đang gõ theo thời gian thực.
  - Nếu trường tên bị xóa rỗng, preview hiển thị placeholder "Tên thẻ" (không giữ tên cũ gây hiểu nhầm).
  - Sử dụng biểu tượng nhãn ví trung tính; không chứa logo VISA, Mastercard hay chip thẻ điện tử.
- **Ghi chú bảo lưu lịch sử:**
  - Đoạn text giải thích rõ: "Các giao dịch đã ghi nhận trước đây vẫn hiển thị theo tên thẻ cũ, không bị thay đổi."
- **Nút điều khiển (Footer):**
  - Nút "Hủy" (nền kem/lavender).
  - Nút "Lưu thay đổi" (nút chính tím nổi bật).
  - Bố trí cuộn được khi bàn phím ảo (IME) hiển thị; không để bàn phím che lấp nút bấm.

### 2. Quy tắc Validation & Kiểm tra Tính hợp lệ
- **Chuẩn hóa chuỗi:** Trim khoảng trắng đầu/cuối và chuẩn hóa Unicode NFC.
- **Bộ đếm grapheme:** Đếm theo ký tự hiển thị thực tế (hỗ trợ dấu tiếng Việt tổ hợp và emoji ghép), không dùng `String.length`.
- **Rỗng hoặc toàn khoảng trắng:** Báo lỗi "Nhập tên thẻ", nút Lưu disabled.
- **Vượt quá 30 grapheme:** Báo lỗi "Tên thẻ tối đa 30 ký tự", nút Lưu disabled.
- **Không thay đổi (No-change):** Nếu tên sau khi chuẩn hóa trùng khớp hoàn toàn với tên gốc đã chuẩn hóa -> Nút Lưu bị disabled. (Thay đổi kiểu chữ hoa/thường của chính thẻ đó, ví dụ `techcombank` -> `Techcombank`, được phép lưu).
- **Trùng tên thẻ khác (Duplicate):** So sánh case-insensitive (chuẩn hóa NFC + lowercase `Locale.ROOT`) với các thẻ đang hoạt động khác (loại trừ chính thẻ đang đổi tên). Nếu trùng, báo lỗi: "Tên thẻ đã tồn tại", nút Lưu disabled.
- **Tên dành riêng cho hệ thống:** Không được đặt tên là "Pay Now" ("Tên này dành cho Pay Now").
- **Tên cũ vượt chuẩn (Legacy 31–40 ký tự):** Prefill đầy đủ tên cũ (không tự động cắt ngắn làm biến dạng tên). Hiển thị bộ đếm `n/30` và dòng hướng dẫn: "Tên hiện tại vượt giới hạn mới. Rút gọn còn tối đa 30 ký tự để lưu." Nút Lưu bị khóa cho đến khi người dùng sửa tên xuống còn ≤ 30 ký tự.

### 3. Vòng đời Modal & Xử lý Trạng thái
- **Khởi tạo:** Prefill đúng tên và màu của thẻ được chọn, preview đồng bộ, nút Lưu disabled.
- **Hủy / Đóng dialog (Nút Hủy, X, phím Back, chạm nền Scrim):**
  - Nếu bản nháp chưa bị thay đổi (`rawDraft == originalName`): Đóng ngay lập tức.
  - Nếu bản nháp đã bị sửa đổi (`rawDraft != originalName`): Hiển thị dialog xác nhận `DiscardChangesDialog` ("Tiếp tục chỉnh sửa" / "Bỏ thay đổi").
- **Đang lưu (Pending / Submitting):**
  - Nút Lưu chuyển sang trạng thái "Đang lưu..." kèm biểu tượng loading.
  - Khóa toàn bộ ô nhập, nút Hủy, nút đóng X và nền scrim (chống double tap).
- **Lưu thành công:**
  - Cập nhật tên thẻ trong repository theo đúng `cardId`.
  - Màu sắc và `cardId` giữ nguyên; không thay đổi số tiền chi tiêu, hạn mức hay lịch sử điều chỉnh C09.
  - Các giao dịch (expenses) trước đây giữ nguyên snapshot tên cũ; các giao dịch mới sau này sẽ dùng tên mới.
  - Đóng dialog, hiển thị snackbar thông báo: "Đã đổi tên thẻ".
  - Khôi phục tiêu điểm (restore focus) về thẻ tương ứng trong C03.
- **Xử lý lỗi / Xung đột (Error & Conflict):**
  - Khi lưu thất bại: Hiển thị lỗi inline "Chưa đổi tên được thẻ. Vui lòng thử lại.", giữ nguyên draft để người dùng bấm thử lại.
  - Khi thẻ bị archive hoặc xóa ở nơi khác trong lúc dialog đang mở: Khóa nút Lưu, hiển thị "Thẻ không còn khả dụng" và cho phép đóng dialog.
  - Trạng thái Offline / Past Cycle: Khóa nút Lưu kèm thông báo lý do; không tự động gửi ngầm khi có mạng lại.

---

## Code Style & Conventions
- **Stateless Composable:** `RenameCreditCardDialog` chỉ nhận state và emit các lambda events.
- **Accessibility:** Mọi nút và icon đều có `contentDescription` bằng tiếng Việt rõ nghĩa ("Đóng dialog đổi tên thẻ", "Màu hồng").
- **Ví dụ cấu trúc Composable:**
```kotlin
@Composable
fun RenameCreditCardDialog(
    isOpen: Boolean,
    card: CreditCard?,
    draftName: String,
    errorMessage: String?,
    isSubmitting: Boolean,
    onDraftNameChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onSubmit: () -> Unit
) {
    if (!isOpen || card == null) return

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Hộp thoại modal bo góc 28dp, padding 20-24dp, cuộn khi IME xuất hiện
    }
}
```

---

## Testing Strategy
- **Unit Tests:**
  - Test bộ đếm grapheme và validator: Đảm bảo độ dài 30 ký tự, test ký tự tiếng Việt có dấu ("Ngân hàng Á Châu"), test emoji ghép.
  - Test no-change logic: Tên không đổi -> canSave = false; đổi hoa/thường chính thẻ -> canSave = true.
  - Test duplicate logic: Trùng tên thẻ khác -> lỗi; trùng tên chính mình -> hợp lệ.
  - Test legacy name (35 ký tự): Prefill đủ, canSave = false, hướng dẫn rút gọn tên.
  - Test snapshot retention: Đổi tên thẻ không làm thay đổi tên snapshot của các giao dịch trước đó.
- **Compose UI Tests:**
  - Test mở dialog, kiểm tra prefill đúng tên và màu sắc.
  - Test live preview cập nhật khi người dùng gõ tên mới.
  - Test đóng dialog khi chưa sửa (đóng ngay) và khi đã sửa (hiện DiscardChangesDialog).

---

## Boundaries
- **Always:**
  - Giữ nguyên màu của thẻ, không cho phép đổi màu trong dialog này.
  - Bảo lưu snapshot tên thẻ của các giao dịch cũ.
  - Giữ dialog dạng modal căn giữa, không chuyển thành bottom sheet.
- **Ask first:**
  - Bất kỳ thay đổi nào tác động đến cấu trúc model `CreditCard` trong domain.
- **Never:**
  - Không hiển thị hoặc lưu trữ số thẻ, last-four, CVV hay ngày hết hạn.
  - Không tự ý cắt cụt tên thẻ cũ có độ dài 31-40 ký tự khi prefill.
  - Không cho phép đổi tên phương thức hệ thống "Pay Now".

---

## Success Criteria (Mapping to AC30–AC39)
- [ ] **SC01 (AC30):** Bấm "Đổi tên" trên thẻ tín dụng ở C03 mở modal dialog căn giữa với nền dim, prefill đúng tên và màu của thẻ đó, có nút X/Hủy/Lưu thay đổi.
- [ ] **SC02 (AC31):** Gõ tên cập nhật Live Preview tức thì; màu thẻ hiển thị chỉ đọc (không đổi được); không có logo VISA/chip.
- [ ] **SC03 (AC32):** Tên 1–30 grapheme hợp lệ; rỗng hoặc >30 báo lỗi và khóa nút Lưu; đếm đúng grapheme NFC (Techcombank = 11).
- [ ] **SC04 (AC33):** Tên không đổi hoặc chỉ thêm khoảng trắng -> khóa nút Lưu; đổi kiểu chữ hoa/thường của chính thẻ thì cho phép lưu; trùng tên thẻ khác hoặc tên "Pay Now" thì báo lỗi và khóa nút Lưu.
- [ ] **SC05 (AC34):** Thẻ có tên cũ 31–40 ký tự prefill đầy đủ không bị cắt ngắn; hiện hướng dẫn rút gọn xuống ≤ 30 ký tự để lưu.
- [ ] **SC06 (AC35):** Hủy/X/Back/Scrim khi chưa sửa thì đóng ngay; khi đã sửa thì kích hoạt xác nhận bỏ thay đổi; không ghi đè dữ liệu.
- [ ] **SC07 (AC36):** Trạng thái pending khóa toàn bộ thao tác, chống double tap; lỗi lưu giữ nguyên draft để thử lại; xung đột báo tải lại.
- [ ] **SC08 (AC37):** Offline hoặc chu kỳ cũ khóa chức năng lưu kèm lý do; khi có mạng lại không tự ý submit ngầm.
- [ ] **SC09 (AC38):** Đổi tên thành công chỉ cập nhật tên hiện hành của thẻ, hiện snackbar "Đã đổi tên thẻ"; các khoản chi tiêu cũ vẫn giữ nguyên snapshot tên cũ; tổng chi, ngân sách và lịch sử điều chỉnh C09 không đổi.
- [ ] **SC10 (AC39):** Giao diện tương thích phông chữ lớn 200%, màn hình hẹp, bàn phím ảo IME không che nút; vượt qua 100% tests (`./gradlew testDevDebugUnitTest`).

---

## Open Questions
*(Không có câu hỏi mở chặn triển khai — tài liệu handoff C07 đã giải quyết triệt để các vấn đề tương thích và giao diện).*
