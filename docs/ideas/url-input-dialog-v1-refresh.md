# Kế hoạch Tinh chỉnh UI/UX UrlInputDialog (Chuẩn Handoff v1)

## 1. Problem Statement
Làm thế nào để biến việc dán link sản phẩm từ một form nhập liệu khô khan thành một "điểm dừng chân trực quan, ấm áp", đồng nhất với nhận diện đồ họa của Chanko và tôn trọng tối đa quyền riêng tư của người dùng?

## 2. Recommended Direction (Lựa chọn 1 - Hai bước chiến lược)
* **Giai đoạn 1 (Thực hiện ngay):** Hoàn thiện chuẩn xác 100% giao diện và tương tác của `UrlInputDialog` và `ExtractionFailedDialog` dựa trên bộ tài liệu `Chanko-Url-Dialogs-Handoff-v1`:
  * Card nền kem hồng `#FFF7F5`, bo góc `32dp`, viền trắng 1dp nổi bật trên scrim tím mận `#32183F` (36% opacity).
  * Minh họa giỏ hàng 3D `chanko_url_basket.png` tràn lên trên mép card `32dp`.
  * Bộ copy tiếng Việt mới: *"Món gì khiến bạn chú ý thế?"*, *"Dán link sản phẩm, Chanko sẽ lấy thông tin giúp bạn."*
  * Ô nhập link bo góc `20dp`, tích hợp icon "Dán" chủ động chuẩn trợ năng 48×48dp, chỉ đọc clipboard khi chạm.
  * Cặp nút tỉ lệ 40:60: Nút "Hủy" nhẹ nhàng và nút "Lấy thông tin" gradient tím thương hiệu với spinner tích hợp trong nút.
  * `ExtractionFailedDialog` độc lập: thay thế cửa sổ khi lỗi (không xếp chồng 2 dialog), icon cảnh báo vector, nút Hủy (giữ URL trong RAM) và Xác nhận (chuyển sang `ManualItemInputScreen`).
  * Quản lý trạng thái: Single-session in-memory qua `AddItemViewModel`, tự hủy job và dọn dẹp khi đóng dialog.
* **Giai đoạn 2 (Kế hoạch tiếp theo):** Bổ sung `Intent Filter` trong `AndroidManifest.xml`, xử lý `ACTION_SEND` từ các ứng dụng mua sắm (Shopee, Lazada, TikTok Shop) và bóc tách URL sạch từ văn bản chia sẻ để tự động kích hoạt `UrlInputDialog`.

## 3. Key Assumptions to Validate
- [ ] Ảnh 3D giỏ hàng không che khuất nội dung hoặc bị méo mó khi bàn phím ảo (IME) mở lên trên các màn hình có chiều cao giới hạn (Test bằng `BoxWithConstraints` và `verticalScroll`).
- [ ] Chuyển đổi giữa `UrlInputDialog` và `ExtractionFailedDialog` không gây hiện tượng nháy màn hình, double scrim hay lỗi focus trợ năng TalkBack.
- [ ] Thao tác dán qua `LocalClipboardManager` hoạt động trơn tru trên các phiên bản Android (từ Android 8 đến Android 14+).

## 4. MVP Scope (Giai đoạn 1)
- **Tài nguyên (Assets):**
  - Chuyển `chanko_url_basket.png` vào `app/src/main/res/drawable-nodpi/`.
  - Bổ sung các icon vector hình học độc lập (link, clipboard, warning) vào `app/src/main/res/drawable/`.
  - Cập nhật chuỗi giao diện vào `app/src/main/res/values/add_item_strings.xml`.
- **Giao diện (Compose):**
  - Cập nhật `UrlInputDialog.kt` theo cấu trúc `CandyUrlInputDialog` và `CandyExtractionFailedDialog` từ tài liệu handoff.
  - Tích hợp kiểm soát focus, ẩn bàn phím khi chuyển sang popup lỗi.
- **Kiểm thử (Verification):**
  - Đảm bảo `./gradlew test` và compile thành công không lỗi syntax/type.

## 5. Not Doing (Trong Giai đoạn 1)
- **Không làm trong giai đoạn 1:** `Intent Filter` / Share Sheet từ Shopee/Tiktok (sẽ làm ở Giai đoạn 2).
- **Không lưu nháp / Không Auto-resume:** Khi người dùng đóng dialog hoặc thoát app, phiên nhập bị hủy hoàn toàn, không lưu vào Room/DataStore.
- **Không thay đổi theme toàn cục:** Chỉ áp dụng màu sắc và gradient cục bộ cho dialog, không làm xáo trộn các token màu hiện hữu của ứng dụng.

## 6. Open Questions / Next Steps
- Bắt đầu triển khai Giai đoạn 1 ngay lập tức.
