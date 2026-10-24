# Chanko — My Items & Item Detail
## Approved UX Decisions — Recovery Baseline

**Status:** Approved UX decisions  
**Purpose:** Khôi phục các quyết định từ cuộc trò chuyện thiết kế để hoàn thiện handoff v1.0.

### 1. My Items — ITEM-01 đến ITEM-07

| ID | Approved decision |
|---|---|
| ITEM-01 | A — List View mặc định, card ngang, thumbnail bên trái |
| ITEM-02 | B — Compact 3D Chanko Hero |
| ITEM-03 | A — Ba overview tiles: All Items, Cooling Off, Ready; nhấn để lọc |
| ITEM-04 | B — Filter hai cấp: All / Active / Purchased / Skipped; Active gồm Saved / Considering / Cooling Off / Ready / Postponed. Có Search và Sort |
| ITEM-05 | A — Compact read-only item cards; nhấn mở Item Detail |
| ITEM-06 | A — Empty State với Chanko và hộp kẹo; có Save an Item hoặc Clear Filters |
| ITEM-07 | Giữ 4 navigation tabs và nút + toàn cục; không thêm FAB trùng lặp |

### 2. Item Detail — DETAIL-01 đến DETAIL-14

| ID | Approved decision |
|---|---|
| DETAIL-01 | A — Pastel Candy Product Stage; chỉ hiển thị ảnh sản phẩm hoặc dummy placeholder |
| DETAIL-02 | A — Product summary và expandable details; Edit trong More Options |
| DETAIL-03 | CTA và nội dung thay đổi theo trạng thái item |
| DETAIL-04 | A — Chanko nhỏ bên cạnh contextual speech bubble, nằm dưới status |
| DETAIL-05 | A — Collapsible Decision Journey dựa trên lịch sử thật |
| DETAIL-06 | A — Sticky Primary CTA + More Options |
| DETAIL-07 | A — Cooling Off countdown nổi bật; Adjust/Skip Wait trong More Options |
| DETAIL-08 | A — Ready có mindful review ngắn, có thể bỏ qua |
| DETAIL-09 | A — Purchased/Skipped ưu tiên read-only |
| DETAIL-10 | A — Postponed hiển thị thời gian review tiếp theo khi có |
| DETAIL-11 | A — Purchase Confirmation Bottom Sheet |
| DETAIL-12 | A — Need More Time có thể đặt thời gian chờ mới hoặc chuyển Considering và ghi nhận Postponed |
| DETAIL-13 | A — Direct Mark as Purchased trong More Options, không bắt buộc Quick Check |
| DETAIL-14 | A — Purchase History là section riêng trong Item Detail |

**Lưu ý:** DETAIL-03 là tổng hợp quy tắc contextual UI; không được tự suy diễn thành một lựa chọn A/B mới.

### 3. Edge Cases

| ID | Approved decision |
|---|---|
| EDGE-01 | A — Cooling Off hết hạn offline: hiển thị Ready tạm thời, đồng bộ sau |
| EDGE-03 | A — Archive/Hide item có purchase, giữ nguyên transaction và budget |
| EDGE-04 | A — Buy Again trong More Options, tạo purchase transaction mới |
| EDGE-07 | A — Offline giữ dữ liệu form mua; yêu cầu online để xác nhận |

Các EDGE khác phải tuân theo Functional Spec và TECH-02; không tự đánh dấu Approved nếu chưa có lựa chọn riêng.

### 4. UX Gaps đã chốt

| ID | Approved decision |
|---|---|
| GAP-01 | A — Ready duy trì đến khi người dùng thực hiện hành động xử lý rõ ràng |
| GAP-02 | A — Skip Waiting kết thúc phiên chờ và mở lựa chọn quyết định, không tự Skip item |
| GAP-03 | A — Archived Items trong menu, hỗ trợ Restore |
| GAP-04 | A — Nhấn purchase transaction mở Transaction Detail để xem/sửa/xóa giao dịch nhập sai |

### 5. State Machine & Business Invariants

Chỉ có **5 Item Status chính thức**: WISHLIST, CONSIDERING, WAITING, PURCHASED, SKIPPED.

- Ready for Review là derived UI state, không phải Item Status.
- Postponed là decision/event hoặc presentation label, không phải Item Status.
- Cooling Off ACTIVE tương ứng WAITING.
- Khi hết hạn: Cooling Off → COMPLETED; Item → CONSIDERING.
- Need More Time sau Completed tạo phiên ACTIVE mới, không gia hạn phiên cũ.
- Mua khi Waiting kết thúc Cooling Off và tạo transaction.
- AI Verdict không tự thay đổi Item Status hoặc tạo Purchase.
- Một item có thể có nhiều Purchase Transactions.
- Xóa Purchase cuối cùng không tự động thay đổi Item Status.
- Archive/Restore phải giữ nguyên lịch sử và budget.
- Offline Room là read cache và nơi lưu manual drafts, không phải nguồn dữ liệu nghiệp vụ chính thức.

### 6. Screen & Component Scope

**4 màn hình điều hướng:**
1. MyItemsScreen
2. ItemDetailScreen
3. TransactionDetailScreen
4. ArchivedItemsScreen

**3 Bottom Sheets:**
1. Filter & Sort
2. More Options
3. Record a Purchase

Purchase History và Decision Journey là sections/components trong Item Detail, không phải màn hình điều hướng riêng.

### 7. Visual Design — Mandatory

- Candy Pop 3D, warm cream background, vibrant purple, pink và pastel accents.
- Typography Nunito, rounded cards, Material 3.
- Mascot phải là **mèo trắng Chanko canonical đã duyệt**, không phải chó hoặc mascot tự tạo khác.
- Chanko được phép xuất hiện ở My Items Hero, Empty State và contextual speech bubble.
- **Không đặt Chanko trong Product Hero, cạnh ảnh sản phẩm hoặc chồng lên ảnh sản phẩm.**
- Product Hero dùng dummy image/placeholder trung tính, không dùng ảnh túi, tai nghe hoặc giày từ mockup trước.
- Không celebration mua hàng, không shaming, không khuyến khích mua thêm.

### 8. Handoff Instructions

1. Đối chiếu các quyết định trên với Functional Specification v1.1 và TECH-02 trên Google Drive.
2. Thay những phần Reference trong v0.9 bằng Approved chỉ khi có bằng chứng tương ứng từ baseline này.
3. Ghi rõ những hành vi chưa được đặc tả trong Functional Spec hoặc TECH-02, đặc biệt Ready resolution, Archive khi Waiting và Restore.
4. Không tự tạo mascot canonical mới nếu chưa có đúng asset.
5. Xuất ZIP handoff v1.0 và báo cáo thay đổi so với v0.9.
6. Không sửa Android code, không tạo PR, không triển khai ứng dụng.

**Đây là bản khôi phục các quyết định UX từ cuộc trò chuyện, không thay thế Functional Spec/TECH-02 làm nguồn chuẩn cho các quy tắc backend.**
