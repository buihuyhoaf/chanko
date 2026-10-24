> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Resource / asset manifest

Các đường dẫn bên dưới trỏ tới file thật trong gói. `resource-manifest.json` có source thực, size, hash. Tài nguyên được copy từ outputs hiện có; không trích xuất từ conversation attachment (read_thread không trả attachment).

| File trong gói | Vai trò / giới hạn |
|---|---|
| references/home-v5-legacy.png | Full-scroll cũ đã kiểm tra; visual reference, bố cục không chuẩn cho yêu cầu mới |
| references/UI-HANDOFF-legacy.md | Lưu nguyên văn để truy xuất nguồn; layout đã superseded |
| references/tokens-legacy.json | Candy Pop implementation values đề xuất |
| assets/mascot_crossed_arms.png | Mèo trắng 3D RGBA legacy khoanh tay; không đúng pose nằm chống cằm của ảnh chuẩn, không dùng final |
| assets/ready_open_door.png | Icon cửa legacy; không thay Hourglass hoặc Pause Jar |
| assets/cooling_snowflake.png | Icon tuyết legacy; không thay prop scene |
| assets/sample_jacket.png | Mock thumbnail áo |
| assets/sample_headphones.png | Mock thumbnail tai nghe |
| assets/sample_keyboard.png | Mock thumbnail bàn phím |
| assets/fonts/nunito_400.ttf | Font regular; license pending |
| assets/fonts/nunito_600.ttf | Font semibold; license pending |
| assets/fonts/nunito_700.ttf | Font bold; license pending |
| assets/fonts/nunito_800.ttf | Font extrabold; license pending |

## Thiếu thật

Chưa thấy file rời đúng scene cho Hourglass/Calendar/Wallet/Pause Jar; đã có approved raster composition mới nhưng chưa có background/layer/source rời; chưa có asset pose nằm chống cằm final; chưa có Figma/layer/source artwork và license; chưa có license font đi kèm; icon-set và nội dung sheet final cần chọn/chốt. Missing entries có path=null, không đưa ra đường dẫn repo giả. Không tạo ảnh mới rồi gọi là ảnh được duyệt.

## Quy cách bàn giao asset final đề xuất

Mèo và bốn prop: cùng góc camera, ánh sáng mềm, vật liệu 3D trắng/pastel, alpha sạch, không chữ/số bake-in, mỗi prop file riêng, silhouette rõ ở kích thước nhỏ. Preview trên cream, lavender và checkerboard để kiểm tra fringe/alpha. Cần nguồn đủ nét cho size hiển thị thực; không upscale để che thiếu chi tiết.

UI text, badge, progress, button, bubble, nav và sheet phải là Compose. Bộ icon rounded thống nhất thay vì cắt raster. Android resource key/module/directory do Phase 0 chọn sau khi đọc repo; gói không áp đặt `res/` path chưa được xác minh. Nếu dùng raster không phụ thuộc density, TECH chọn import policy và đo memory; kiểm tra WebP alpha nếu tối ưu. Không load PNG nhiều megapixel nguyên bản chỉ để render thumbnail 64dp.

Tài liệu cũ ghi PNG đã được ImageGen tách/tái tạo từ composite, không là layer gốc pixel-perfect. Đây là provenance theo tài liệu nguồn, chưa có license release riêng trong gói. Ảnh sản phẩm chỉ dùng fixture/demo. Không mặc định quyền production đã được xác nhận.

## Ảnh chuẩn mới đã đóng gói

`references/approved-home-user-reference.png` là ảnh người dùng gửi trực tiếp, copy nguyên byte, giữ layout/composition và Global sheet làm chuẩn. Nó không phải asset scene có thể dùng thẳng cho production: chứa cả text/action và composite sheet. Không dùng full screenshot làm UI.

Asset thiếu cần tạo/tách lại đúng ảnh: phòng pastel với cửa sổ vòm/kệ/cây; mèo nằm trên đệm tím; hourglass vàng trên sách; calendar bàn tím với text động; wallet mint; jar thủy tinh tim hồng. Không cắt bitmap có text khỏi ảnh và báo là asset final sạch. `assets/mascot_crossed_arms.png` chỉ giữ để truy nguồn, không đạt visual mới.
