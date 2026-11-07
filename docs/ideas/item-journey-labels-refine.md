# Idea Refine: Hiển thị Tên món đồ & Giá tiền trên Item Journey (Home Screen)

## 1. Problem Statement (Vấn đề thực tế)
- **Hiện trạng:** Các node trên Item Journey (khu vực 3 trên màn hình Home) hiện tại chỉ là các vòng tròn ảnh (hoặc placeholder icon) kèm viền màu trạng thái và badge icon nhỏ ở góc.
- **Điểm nghẽn UX:** Người dùng nhìn vào màn hình không thể biết được hình tròn đó đại diện cho món đồ nào (đặc biệt khi ảnh thu nhỏ khó nhận diện hoặc hiển thị icon placeholder `ITM_01`) và giá trị bao nhiêu tiền nếu không nhấn mở `ItemDetailScreen` hoặc không bật TalkBack.
- **Mục tiêu ("How Might We"):** Làm thế nào để người dùng nhận diện tức thì **tên món** và **giá tiền** của từng món đồ ngay trên hành trình ziczac mà vẫn giữ được sự tinh tế, ngộ nghĩnh và không làm rối bố cục đặc trưng của Candy Theme?

---

## 2. Recommended Direction: Sub-Label Dưới Chân Node (Node-Anchored Tag)

### 2.1. Cấu trúc hiển thị của mỗi Node
Mỗi trạm trên hành trình sẽ là một khối dọc căn giữa (`Column`, `Alignment.CenterHorizontally`, chiều rộng tối đa `104dp`):
1. **Node tròn chính (`80–88dp`)**:
   - Vòng tròn ảnh sản phẩm / icon placeholder.
   - Viền Canvas: Cung đếm ngược đọng băng (Circular Countdown Arc - Xanh Băng Tuyết) hoặc Viền nhịp thở (Breathing Pulse - Tím Thạch/Ready).
   - Badge trạng thái góc trên phải (`JourneyStatusBadge`).
2. **Khoảng đệm nhỏ (`4dp`)**.
3. **Tên món đồ (Item Title)**:
   - Typography: `12sp`, `FontWeight.SemiBold`, màu `CandyColors.HomeV2.textPrimary` (`#1F1A24`).
   - Giới hạn: Tối đa 1 dòng, `TextOverflow.Ellipsis` khi quá dài.
   - Text align: `TextAlign.Center`.
4. **Pill Giá tiền (Price Chip)**:
   - Box nhỏ bo tròn (`RoundedCornerShape(8.dp)`), `padding(horizontal = 6.dp, vertical = 2.dp)`.
   - Nền: `Color(0xFFF0ECF8)` (tím phấn nhạt Candy) hoặc `CandyPalette.Gray100`.
   - Typography: `11sp`, `FontWeight.Bold`, màu `CandyColors.HomeV2.textSecondary` (`#5C5466`).
   - Format: `priceFormatted` (ví dụ `1.5M` hoặc `350k` rút gọn theo `HomeCurrencyFormatter`).
   - Xử lý thiếu giá (`AC-09`): Nếu `priceMinor == null`, hiển thị chữ `"Chưa rõ giá"` với sắc thái trung tính nhẹ nhàng.

---

## 3. Điều chỉnh Kỹ thuật & Bố cục Ziczac

1. **Tọa độ & Đường nối Ziczac (`connectorColor`)**:
   - Các đoạn nét đứt kết nối hiện tại vẽ giữa tâm 2 node tròn (`nodeCenters`).
   - Giữ nguyên tâm kết nối tại tâm của Node tròn (`nodeCenter`), đường nét đứt đi phía sau sub-label (hoặc sub-label có nền nhẹ / khoảng hở an toàn) để text luôn sắc nét, không bị đường nét đứt đè lên mặt chữ.
2. **Khoảng cách dọc giữa các trạm (`stepY`)**:
   - Tăng nhẹ bước nhảy dọc giữa các trạm từ `110dp` lên `130–140dp` để nhường không gian `36dp` cho cụm text (Tên + Giá), tránh việc chân text của node trên dính sát vào đỉnh vòng tròn của node dưới.
3. **Mascot Chanko (`AC-17`)**:
   - Mascot Chanko vẫn đứng ở khoảng trống bên phải đường ziczac, không bị chồng lấn vùng chạm hoặc che khuất text của các node.

---

## 4. Key Assumptions & Constraints
- Dữ liệu `title` và `priceFormatted` đã có sẵn đầy đủ trong `JourneyNodeUi`, không cần thay đổi tầng Repository hay UseCase.
- Chiều cao tổng thể khu vực Journey sẽ tăng thêm khoảng ~100–120dp khi có 5 nodes, nhưng đây là khu vực cuộn tự do bên dưới Budget Card nên hoàn toàn không ảnh hưởng đến cấu trúc trang.
- TalkBack (`AC-31`): `semantics(mergeDescendants = true)` trên toàn khối trạm sẽ đọc liền mạch: *"[Tên món], [Trạng thái], [Giá tiền]"*, đồng bộ hoàn hảo giữa âm thanh và hiển thị trực quan.

---

## 5. Scope Phân định

### In-Scope (MVP):
- Thêm cụm Text Tên món + Pill Giá tiền bên dưới mỗi Node trong `ItemJourneyZigZagCanvas` / `JourneyProductNode`.
- Tự động rút gọn (ellipsis) tên món dài và format giá dạng compact (`1.5M`).
- Điều chỉnh bước nhảy dọc của đường Ziczac để cân đối tỷ lệ thị giác.
- Unit tests & UI Preview cập nhật.

### Not Doing:
- Không thêm thao tác sửa nhanh giá/tên trực tiếp trên Home (mọi tương tác chạm vẫn dẫn tới `ItemDetailScreen`).
- Không làm popup tooltip phức tạp làm gián đoạn luồng cuộn của người dùng.
