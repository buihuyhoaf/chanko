# Asset delivery v3

Đủ sáu raster để bắt đầu dựng Living Room theo ảnh bằng file riêng: [asset pack README](assets/living-room/README.md), [preview ghép lớp](assets/living-room/PREVIEW.html), [manifest](assets/living-room/asset-manifest.json), [scene layout](assets/living-room/scene-layout.json), [prompt set](assets/living-room/generation-prompts.json).

Các ảnh được tạo bằng built-in image_gen với ảnh Home chuẩn làm reference, không phải layer gốc trích chính xác. Đã chọn lại mèo để bỏ quầng sáng lớn từ bản đầu; giữ bản nguồn PNG, không xử lý/cắt ảnh bằng Python. Năm sprite RGBA có pixel alpha=0 và alpha=255, background RGB. Glass có alpha nhưng hiệu ứng xuyên kính phải kiểm tra trên scene thật. Kiểm tra cấu trúc file/alpha không thay thế visual review runtime.

Có thể tiến hành Phase 3/4 với asset thật thay placeholder. Chưa gọi app pixel-perfect, chưa có runtime/build test; dp layout/hit targets/date overlay vẫn cần tinh chỉnh theo previews. Các icon UI thông dụng dựng vector/Compose theo cùng style, không yêu cầu PNG; icon bulb Observation có thể dùng vector/Compose có cùng treatment để triển khai. TECH contracts/persistence/navigation theo pending alignment.

v3 thay thế báo thiếu sáu PNG trong v2. Không thay đổi approved layout hoặc thứ tự/component invariants. Không sửa repo/code hoặc sources synced. Layer/source 3D gốc và quyền dùng tài nguyên tham chiếu/font vẫn theo pending log; file tái tạo mới có provenance generation trong prompt set.
