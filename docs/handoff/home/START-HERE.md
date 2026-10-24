> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# START HERE — v2, bám ảnh người dùng

**Nguồn visual chuẩn duy nhất cho Home mới: [ảnh người dùng gửi](references/approved-home-user-reference.png).** Đọc [visual mapping](00-VISUAL-REFERENCE.md), rồi approved UX spec và contracts trước khi implement.

## Thứ tự ưu tiên

1. Chỉ dẫn “làm đúng theo ảnh kia” và ảnh đính kèm của người dùng ngày 24/10/2026.
2. Quy tắc đã chốt: decisions tối đa 3, Ready → Waiting → Considering → Saved; Observation/Draft optional; bốn tab với + trung tâm mở Global Quick Action; contextual sheets.
3. Hình thức theo ảnh: Header có dòng phụ, Living Room có dialogue bên trong, hai thẻ action, budget ngang, ba decision row, Observation và bottom nav/sheet.
4. Token v5 cũ chỉ là giá trị khởi đầu. Không dùng ảnh calendar-led v5 cũ làm chuẩn bố cục hoặc pose mèo mới.

## Cách dùng

Phase 0 mapping vào repo được giao, chốt business contracts còn mở. Phase 1–11 triển khai theo component; Phase 4 là dialogue NẰM TRONG scene đã dựng Phase 3, không là row độc lập. Prompt mỗi phase đã được cập nhật. Đối chiếu ảnh ở từng review visual, không chỉ đọc text mô tả.

## Những gì có / còn thiếu

Có ảnh chuẩn gốc được copy nguyên byte vào gói, tài liệu theo ảnh, fixture và 12 prompt. Scene mới chưa có layer/source hoặc asset rời final; mèo legacy khoanh tay khác pose trong ảnh và không được dùng làm final. Hourglass/Calendar/Wallet/Pause Jar chưa có file rời đúng ảnh. Font/asset license cần xác nhận trước phát hành.

Đây là tài liệu triển khai và tài nguyên tham chiếu, không phải code đã tích hợp. Không sửa repository hay synced sources. Không xác nhận tình trạng build/phase app. Không dùng emulator/AVD/VM; runtime chỉ thiết bị thật, nếu chưa có thì báo unverified. Không thêm launcher riêng.
