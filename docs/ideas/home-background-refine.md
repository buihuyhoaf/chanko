# Ý tưởng Nền HomeScreen: HOME-BG-01 (Lavender Dream + Ambient Mesh)

## Problem Statement
Làm thế nào để màu nền HomeScreen thể hiện rõ chất thương hiệu Candy/Pastel, cố định trên toàn bộ viewport, tạo độ tương phản rõ ràng làm nổi bật các Card trắng/Mascot 3D/Carousel mà không cạnh tranh sự chú ý với 12 theme thẻ ngân sách và các trạng thái hành trình?

## Recommended Direction
Kết hợp **Phương án 1 (Lavender Dream)** làm dải gradient nền tĩnh toàn viewport với **Phương án 5 (Ambient Blurred Orbs)** tạo hiệu ứng ánh sáng mờ bồng bềnh (mesh/orbs với opacity thấp 15%–25%, blur mềm mại không viền gắt).

- **Cấu trúc nền cố định (Fixed Viewport Layer)**: Toàn bộ danh sách `LazyColumn` cuộn bên trên bề mặt nền tĩnh.
- **Bảng màu chuyển sắc 3 nấc + 2 điểm tụ ánh sáng**:
  - `HomeBgTop` (`#EDE7FF` - Lavender pastel) $\rightarrow$ `HomeBgMiddle` (`#F1E7FB`) $\rightarrow$ `HomeBgBottom` (`#FBEAF4` - Hồng phấn).
  - Đốm sáng mờ `AmbientPurple` (`#D7C5FF`, alpha 0.15–0.25) đặt lệch ở góc trên ngoài vùng tương tác chính.
  - Đốm sáng mờ `AmbientPeach` (`#FFD9C4`, alpha 0.15–0.20) đặt lệch ở góc dưới đáy màn hình.
- **Tách lớp thị giác (Visual Layering)**: Các thẻ nội dung (`CardSurface = #FFFFFF`), bong bóng hội thoại (`WarmCream`), và 12 theme màu động của Carousel nổi bật rõ rệt nhờ nền có sắc thái nhưng êm dịu, không gây nhiễu thị giác.

## Key Assumptions to Validate
- [ ] **Hiệu năng GPU / Compose Render**: Render 2 ambient orbs với hiệu ứng blur/gradient radial trên các thiết bị Android tầm trung/thấp không gây sụt giảm khung hình khi cuộn nhanh `LazyColumn` (xác thực qua Android Profiler / GPU Rendering).
- [ ] **Độ tương phản nội dung (WCAG & Legibility)**: Đốm ánh sáng ambient không làm suy giảm độ tương phản của chữ và số liệu quan trọng trên Budget Card hoặc Item Journey.
- [ ] **Tính nhất quán thanh điều hướng**: Bottom Navigation Bar và Status Bar giữ được surface/frosted blur riêng biệt, không bị lem màu hoặc đục mờ khó đọc icon/text.

## MVP Scope
- Khai báo các color token mới trong `CandyPalette` & `CandyColors` (`HomeBgTop`, `HomeBgMiddle`, `HomeBgBottom`, `AmbientPurple`, `AmbientPeach`).
- Tách `Modifier.background(...)` ra khỏi vùng cuộn `LazyColumn` để gán vào `Box` cha cố định toàn màn hình của `HomeScreen`.
- Thêm lớp Ambient Orbs dạng radial gradient mềm (hoặc canvas draw có blur vừa phải) phía sau danh sách cuộn.
- Cập nhật các preview của HomeScreen để kiểm tra visual trên nhiều kích thước màn hình.

## Not Doing (and Why)
- **Không đổi màu nền theo ngân sách (Budget-Adaptive Background)**: Tránh cảm giác trừng phạt/lo âu khi vượt ngân sách ("không phán xét" là triết lý cốt lõi của Chanko); chỉ dùng Coral trên các chip/thành phần cảnh báo nhỏ trong Budget Card.
- **Không đặt Ambient Orb ngay phía sau các chỉ số tài chính hoặc cụm node Journey**: Giữ nguyên độ rõ ràng tối đa cho typography và biểu đồ.
- **Không gắn nền gradient vào LazyColumn**: Tránh việc gradient bị kéo dãn hoặc dịch chuyển theo chiều dài danh sách cuộn.

## Open Questions
- Với Compose trên Android < API 31 (chưa hỗ trợ `RenderEffect.createBlurEffect` mượt ở cấp modifier), nên vẽ ambient orbs bằng `Brush.radialGradient` thuần túy hay dùng asset đồ họa vector/bitmap có sẵn blur? (Khuyến nghị: `Brush.radialGradient` đa nấc với alpha fall-off mềm để tối ưu hiệu năng và tương thích mọi phiên bản Android).
