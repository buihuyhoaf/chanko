# Chanko Living Room — asset pack v1

Đã tạo 6 PNG thực bằng built-in image_gen, dựa trên ảnh Home người dùng cung cấp. Không sửa code app. Ảnh tái tạo có thể khác chi tiết nhỏ; không phải layer gốc pixel-perfect, chưa thay thế design sign-off trên màn thật.

## File dùng triển khai

- `room_background.png`: nền phòng trống 1983×793 RGB, không mèo/prop/text. Clip rounded corners bằng Compose.
- `cat_on_cushion.png`: mèo trắng nằm chống cằm trên đệm lavender, alpha; chọn bản không có quầng sáng lớn.
- `hourglass_books.png`: đồng hồ cát vàng và chồng sách trong một sprite alpha.
- `calendar_blank.png`: lịch tím, trống ngày/tháng; overlay text dynamic bằng Compose.
- `wallet_mint.png`: ví mint và coin, alpha.
- `pause_jar_heart.png`: hũ tim hồng, alpha. Giữ alpha; không phủ white background lên toàn sprite.

Mở [PREVIEW.html](PREVIEW.html) trong trình duyệt để xem ghép lớp và alpha checker. Manifest có size/mode/alpha bounds/hash. `scene-layout.json` chứa vị trí normalized đề xuất; `generation-prompts.json` lưu prompt đã dùng.

## Ghép trong Compose

Background → Hourglass/books → Calendar → Wallet → Cat/cushion → Jar → dialogue. Dùng Image ContentScale.Fit cho sprite, bounding box theo scene-layout; không stretch từng vật. Cat/cushion và hourglass/books là assembly, không render đệm/sách lần nữa. Bubble/timer/text/date/icon/nav/action cards dựng Compose/vector, không bake vào PNG.

Layer bounds là vị trí artwork, KHÔNG là hit target nghiệp vụ. Tạo target ít nhất 48dp khi có thể, không overlap, có semantics riêng; ở màn hẹp cần bố trí adaptive. Date box trong JSON chỉ là gợi ý, cần align theo perspective panel và kiểm tra font lớn. Gesture/action theo contracts TECH đã chốt, không tự thêm nghiệp vụ.

Giữ file nguồn alpha nguyên bản; chọn decode size phù hợp trước khi dùng trên Android. Tập này đủ để dựng Living Room bằng artwork thật, nhưng cần review composition/edge fringe, độ trong hũ và memory tại size đích; không gọi đây là app đã pixel-match hoặc đã test runtime.
