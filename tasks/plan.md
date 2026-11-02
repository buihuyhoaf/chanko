# Implementation Plan: Cải tiến UI/UX UrlInputDialog (Chuẩn Handoff v1)

## Overview
Nâng cấp toàn diện giao diện và trải nghiệm người dùng cho `UrlInputDialog` và `ExtractionFailedDialog` (kích hoạt khi người dùng nhấn nút `+` ở Bottom Bar) theo bộ tài liệu bàn giao `Chanko-Url-Dialogs-Handoff-v1`:
- Thay thế phong cách form cũ bằng Card nền kem hồng `#FFF7F5`, bo góc `32dp`, viền trắng 1dp, shadow mềm 12dp.
- Bổ sung minh họa giỏ hàng 3D `chanko_url_basket.png` tràn lên trên mép card `32dp`.
- Bộ copy tiếng Việt mới tạo thiện cảm và cảm xúc tích cực (*"Món gì khiến bạn chú ý thế?"*).
- Ô nhập link bo góc `20dp`, tích hợp icon "Dán" chủ động chuẩn trợ năng 48×48dp, chỉ đọc clipboard khi chạm.
- Cặp nút hành động tỉ lệ 40:60 (Hủy viền tím nhạt, Lấy thông tin gradient tím `#A259D7` → `#7D35B3` với spinner lồng trong nút khi loading).
- `ExtractionFailedDialog` độc lập (thay thế cửa sổ, không xếp chồng 2 dialog), icon cảnh báo vector riêng.
- Quản lý trạng thái: Single-session in-memory qua `AddItemViewModel`, hủy job và dọn dẹp khi đóng.

---

## Architecture Decisions
- **Tài nguyên độc lập (Decoupled Assets):** Đặt ảnh 3D vào `res/drawable-nodpi/` để giữ nguyên độ phân giải và kích thước tính bằng `dp` trong Compose; các icon vector hình học đặt trong `res/drawable/`.
- **Window Management & Scrim:** Chỉ render 1 Dialog window tại 1 thời điểm (`AddItemEntryHost` điều phối: hiển thị `UrlInputDialog` hoặc `ExtractionFailedDialog`), tránh lỗi double scrim và lỗi TalkBack focus xuyên nền.
- **Responsive Layout & Keyboard Occlusion:** Dùng `BoxWithConstraints` và `verticalScroll` với `heightIn(max = maxHeight)` để đảm bảo dialog co giãn linh hoạt, không bị bàn phím ảo (IME) che khuất nút bấm.
- **Privacy-First Clipboard:** Chỉ đọc `LocalClipboardManager` khi người dùng bấm trực tiếp vào icon Dán, không quét clipboard tự động.
- **State Seam:** Giữ nguyên hợp đồng tương tác với `AddItemViewModel` (sự kiện `editUrl`, `extract`, `cancel`, `dismissExtractionFailure`, `confirmManualInput`).

---

## Phased Implementation Roadmap

### Phase 1: Assets & Resources Setup
- [ ] Task 1: Thiết lập Assets (Ảnh giỏ hàng 3D và các vector icon mới).
- [ ] Task 2: Cập nhật String Resources tiếng Việt theo handoff.

### Checkpoint 1: Assets
- [ ] Tất cả drawables và strings compile hợp lệ, không xung đột resource ID.

### Phase 2: Component Implementation & Host Wiring
- [ ] Task 3: Xây dựng Composable `UrlInputDialog` và `ExtractionFailedDialog` mới theo chuẩn Handoff v1.
- [ ] Task 4: Tích hợp thao tác Clipboard và bàn phím (IME Action Done).
- [ ] Task 5: Cập nhật `AddItemEntryHost` điều phối hiển thị duy nhất 1 Dialog và dọn dẹp focus bàn phím.

### Checkpoint 2: UI & Navigation
- [ ] Dialog hiển thị đúng giao diện, thao tác Dán / Hủy / Lấy thông tin / Popup lỗi chuyển Manual hoạt động mượt mà.

### Phase 3: Previews, Testing & Verification
- [ ] Task 6: Bổ sung Compose Previews cho các trạng thái Dialog (Idle, Extracting, Error, ExtractionFailed).
- [ ] Task 7: Chạy kiểm thử tự động (`./gradlew testDevDebugUnitTest`) và verify build (`./gradlew assembleDevDebug`).

### Checkpoint 3: Hoàn tất
- [ ] Tất cả unit test passed, build assembleDevDebug thành công không có lỗi biên dịch.

---

## Risks and Mitigations
| Rủi ro | Mức độ | Biện pháp giảm thiểu |
| :--- | :--- | :--- |
| Minh họa 3D bị cắt hoặc đẩy lệch khi bàn phím mở | Trung bình | Đặt trong `Box` với padding/offset hợp lý, bọc toàn bộ nội dung trong `BoxWithConstraints` và `verticalScroll`. |
| Double dim / đơ focus khi popup lỗi bật lên | Cao | Chỉ hiển thị 1 Native Dialog tại một thời điểm trong `AddItemEntryHost` (`if (state.extractionFailed) ExtractionFailedDialog(...) else UrlInputDialog(...)`). |
| Chữ trên nút gradient bị chói / khó đọc | Thấp | Sử dụng gradient tím đậm (`#A259D7` → `#7D35B3`) đảm bảo tỷ lệ tương phản chữ trắng ≥ 4.5:1. |

---

## Open Questions
- Không có câu hỏi còn tồn đọng (Lựa chọn 1 đã được chốt và thống nhất).
