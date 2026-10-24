> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Asset briefs — bám ảnh chuẩn, chưa là asset final

Nguồn cần mở: ../references/approved-home-user-reference.png. Giữ Candy Pop 3D mềm, ánh sáng pastel ấm, cùng camera/material/scale. Không tạo biến thể sáng tạo khác pose/bố cục rồi gọi approved. Không chạy tạo ảnh trong nhiệm vụ đóng gói tài liệu này.

| Asset | Yêu cầu theo ảnh |
|---|---|
| Room background | Phòng pastel, kệ/cây trái, cửa sổ vòm/cây phải, mặt bàn ấm; chừa vùng bubble trên-trái và các prop/mèo; không chứa text |
| Cat | Mèo trắng 3D nằm chống cằm, nháy mắt, trên đệm lavender; vị trí giữa-phải; tuyệt đối không pose khoanh tay legacy |
| Hourglass | Đồng hồ cát vàng trên chồng sách ở dưới-trái; không countdown bake-in |
| Calendar | Lịch bàn tím sau/trái Wallet; mặt để ngày/tháng Compose động, không hardcode OCT 24 |
| Wallet | Ví mint có nút/chi tiết như ảnh, giữa calendar và cat; không số dư bake-in |
| Pause Jar | Hũ thủy tinh, nắp hồng/lavender, tim hồng lớn bên trong; phía phải mèo; không tự đổi thành hũ tiền |
| Observation icon | Bóng đèn vàng trong tile lavender; icon source/vector hoặc asset có quyền dùng, không cắt chữ từ screenshot |

Tách scene background và object theo cách tránh double-render. Nếu scene static composite được design duyệt, vẫn phải để text/calendar động/hit targets ở Compose và không chồng object lần nữa. Asset final alpha sạch, cùng perspective, preview ở size thật trên cream/lavender; ghi source/license, kích thước, version, owner/approval và bounds. Gói chưa có những file final này; không invent resource path Android.

UI bubble, title/body, labels, cards, pills, buttons, progress, nav, sheet handle và close/chevrons là Compose/vector UI thật. Icon sheet Quick Check là bubble/question, Save là bookmark/add hồng, khác icon search/+ trong action cards Home như ảnh.
