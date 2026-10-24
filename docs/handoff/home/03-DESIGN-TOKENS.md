> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Candy Pop / Nunito

Visual identity đã chốt: Candy Pop, nền cream, lavender/pink accents, mèo trắng 3D mềm, bề mặt bo tròn và Nunito. Không làm lại aesthetic thành dashboard công sở hoặc flat mascot.

`references/tokens-legacy.json` là token từ handoff v5 cũ, status nguyên bản là “Reference implementation values … not original vector design tokens”. Đây là đề xuất kỹ thuật để bắt đầu, không có bằng chứng tất cả HEX/dp đã được duyệt độc lập cho scene mới.

| Vai trò | Giá trị tham chiếu |
|---|---|
| brandPurple / brandPink | #8965F6 / #FF85B3 |
| background / surface | #FAF8F5 / #FFFFFF |
| primaryInk / textPrimary / textSecondary | #5731C6 / #151329 / #585C82 |
| lavender surface / speech | #F4EEFF / #FFECE5 |
| ready surface / action / ink | #D9FAE6 / #00634F / #075C48 |
| waiting blue / considering lavender | #E0EBFF / #E7E1FF |
| spacing | 4, 8, 12, 16, 20, 24, 32dp |
| gutter / section gap | 16–20dp / 20–24dp |
| card radius / thumbnail radius | 20–24dp / 14dp |
| touch target / center + | ≥48dp / 56dp |
| nav content height | tham chiếu 72dp + bottom inset; cho phép reflow |

Typography đề xuất: heading 20/28sp weight 800; item 16/22sp 800; body 14/20sp 400–600; money 18/24sp 800; status 12/18sp 600; button 14/20sp 800; nav 12/16sp 600–700. Không khóa fixed-height khi scale tăng. Font files 400/600/700/800 có trong gói, coverage tiếng Việt cần kiểm tra trên runtime; license/provenance chưa có bản kèm nguồn, cần bổ sung trước phát hành.

Gradient nhẹ; không phủ nhiều lớp card hoặc shadow đen nặng. Không suy ra size pixel PNG thành dp/sp. Scene height và tỉ lệ prop chưa có thông số duyệt: dùng preview responsive để chốt, không đặt số rồi gọi approved. Đo contrast trên màu thực/alpha/pressed/disabled; không coi cặp màu mẫu là chứng nhận toàn UI. Ready không chỉ dùng màu; Waiting/Considering/Saved cần nhãn chữ riêng. Dark mode và animation asset chưa có thiết kế duyệt; Phase 0 chốt phạm vi, không âm thầm invent theme thứ hai.

## Áp dụng theo ảnh chuẩn mới

Ảnh chuẩn ở references/approved-home-user-reference.png quyết định composition, hierarchy và treatment; token legacy chỉ gần đúng màu. Thêm semantic roles cho Quick Check gradient, secondary action surface, observation tile, budget warning orange, selected nav tile và sheet tiles. Giá trị final lấy từ design/preview đã xác nhận, không tự gọi HEX ước lượng là approved. Giữ Nunito và bóng mềm. Scene mới dùng mèo nằm trên đệm tím; không tái dùng pose khoanh tay dù cùng palette.
