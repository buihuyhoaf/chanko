# Decision traceability • Recovery Baseline

Nguồn trực tiếp: `sources/approved_ux_recovery_baseline.md`, user cung cấp24/10/2026. Cả29 quyết định UX đã Approved; mapping dưới đây không chứng nhận backend đã implement. Không gán DETAIL-03 thành lựa chọn A/B mới.

| ID | Approved UX (nguyên văn baseline) | Mapping |
|---|---|---|
| ITEM-01 | A — List View mặc định, card ngang, thumbnail bên trái | 03 MyItems; AC35 |
| ITEM-02 | B — Compact 3D Chanko Hero | 03 Hero; AC36 |
| ITEM-03 | A — Ba overview tiles: All Items, Cooling Off, Ready; nhấn để lọc | 03 tiles; AC36 |
| ITEM-04 | B — Filter hai cấp: All / Active / Purchased / Skipped; Active gồm Saved / Considering / Cooling Off / Ready / Postponed. Có Search và Sort | 03 Filter; AC37 |
| ITEM-05 | A — Compact read-only item cards; nhấn mở Item Detail | 03 card; AC35 |
| ITEM-06 | A — Empty State với Chanko và hộp kẹo; có Save an Item hoặc Clear Filters | 03 Empty/05 assets; AC38 |
| ITEM-07 | Giữ 4 navigation tabs và nút + toàn cục; không thêm FAB trùng lặp | 03 navigation; AC36 |
| DETAIL-01 | A — Pastel Candy Product Stage; chỉ hiển thị ảnh sản phẩm hoặc dummy placeholder | 03 Stage; AC53 |
| DETAIL-02 | A — Product summary và expandable details; Edit trong More Options | 03 summary/More; AC43 |
| DETAIL-03 | CTA và nội dung thay đổi theo trạng thái item | 03 contextual; AC44 |
| DETAIL-04 | A — Chanko nhỏ bên cạnh contextual speech bubble, nằm dưới status | 01/03 bubble; AC44 |
| DETAIL-05 | A — Collapsible Decision Journey dựa trên lịch sử thật | 03 Journey; AC44 |
| DETAIL-06 | A — Sticky Primary CTA + More Options | 03 sticky CTA; AC44 |
| DETAIL-07 | A — Cooling Off countdown nổi bật; Adjust/Skip Wait trong More Options | 02/03 countdown; AC40/44 |
| DETAIL-08 | A — Ready có mindful review ngắn, có thể bỏ qua | 02 Ready; AC39 |
| DETAIL-09 | A — Purchased/Skipped ưu tiên read-only | 03 read-only; AC43 |
| DETAIL-10 | A — Postponed hiển thị thời gian review tiếp theo khi có | 02 Postponed; AC41 |
| DETAIL-11 | A — Purchase Confirmation Bottom Sheet | 03 purchase sheet; AC42 |
| DETAIL-12 | A — Need More Time có thể đặt thời gian chờ mới hoặc chuyển Considering và ghi nhận Postponed | 02 two branches; AC12/41 |
| DETAIL-13 | A — Direct Mark as Purchased trong More Options, không bắt buộc Quick Check | 03 More; AC42 |
| DETAIL-14 | A — Purchase History là section riêng trong Item Detail | 03 history; AC43 |
| EDGE-01 | A — Cooling Off hết hạn offline: hiển thị Ready tạm thời, đồng bộ sau | 02 offline; AC11 |
| EDGE-03 | A — Archive/Hide item có purchase, giữ nguyên transaction và budget | 02 archive; AC24/45 |
| EDGE-04 | A — Buy Again trong More Options, tạo purchase transaction mới | 03 More; AC42/49 |
| EDGE-07 | A — Offline giữ dữ liệu form mua; yêu cầu online để xác nhận | 03 form; AC28 |
| GAP-01 | A — Ready duy trì đến khi người dùng thực hiện hành động xử lý rõ ràng | 02 Ready; AC39/46 |
| GAP-02 | A — Skip Waiting kết thúc phiên chờ và mở lựa chọn quyết định, không tự Skip item | 02 Skip Waiting; AC40 |
| GAP-03 | A — Archived Items trong menu, hỗ trợ Restore | 03 menu/Archive; AC45 |
| GAP-04 | A — Nhấn purchase transaction mở Transaction Detail để xem/sửa/xóa giao dịch nhập sai | 03 TransactionDetail; AC21/23/43 |

## Đối chiếu với Functional/TECH-02

Không có mâu thuẫn trực tiếp bắt buộc đổi5 status, accounting, multi-purchase, correction, offline/cache/draft hoặc scope. EDGE01 Ready tạm thời là presentation, không server completion offline. GAP02 Skip Waiting map cancel ACTIVE→Considering rồi decision choices, tách Skip item. DETAIL12 bổ sung nhánh UX Considering+Postponed bên cạnh new ACTIVE đã chốt trong phụ lục; event persistence còn cần contract.

GAP01 chốt giữ Ready đến action rõ; không đủ định nghĩa lúc clear sau Still Want It, Postponed hoặc archive. EDGE03/GAP03 chốt archive item có purchase và Restore, không chốt ACTIVE timer lifecycle/quyền ghi archived. Các khoảng này là contract gaps còn lại, không còn thiếu nội dung mã. Các EDGE ngoài4 mã được cung cấp không được tự nâng Approved.

Visual tokens và business invariants độc lập đã Approved theo nguồn09. Canonical mascot/Empty State composition chưa có file, không được coi là đã bàn giao hình.
