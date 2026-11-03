# Capability Map & Master Spec: Chanko Home UI v2.0 (Budget Credit Card & Item Journey)

> **Tài liệu đặc tả chi tiết:** [docs/features/home-ui-v2-spec.md](docs/features/home-ui-v2-spec.md)  
> **Nguồn bàn giao gốc:** `Chanko_Home_v2.0_Handoff` (03/11/2026 · 31 Acceptance Criteria `AC-01`..`AC-31` · Mockup `reference/home-v2-approved.png`)  
> **Trạng thái:** COMPLETED & VERIFIED (31/31 AC PASSED)

---

## 1. Capability Map (Phase 0)

Bản nâng cấp **Chanko Home v2.0** thay thế hoàn toàn kiến trúc Little Room + 4 vật thể + Contextual Bottom Sheet + danh sách *Những món cần chú ý* của v1.0 bằng bố cục **4 khu vực mới (`Header → Budget Credit Card → Item Journey → Observation`)**. Yêu cầu được tách thành 4 module độc lập có thể kiểm thử tách biệt theo thứ tự phụ thuộc một chiều:

| Module ID | Trách nhiệm chính | Phụ thuộc (Depends on) | Thứ tự xây dựng (Build Order) |
| :--- | :--- | :--- | :---: |
| **`home-v2-domain-state`** | Mô hình trạng thái độc lập từng khu vực (`SectionState<T>`: `InitialLoading`, `Content`, `Empty`, `Error` kèm `cachedData`); truy vấn `ItemJourney` (`exclude Archived → sort createdAt DESC → tie-break itemId ASC → take(5)`, đổi status không đổi thứ tự — `AC-10`, `AC-11`); tính toán `BudgetCreditCard` (hợp nhất `Pay Now` + `Credit`, `limit <= 0`, thiếu giá `"Chưa đủ dữ liệu giá"`, vượt ngân sách giữ số âm & số vượt — `AC-03`, `AC-06`..`AC-09`); cơ chế khóa snapshot theo gesture (`pointer-down` đến `click/cancel` không đổi `itemId` dưới ngón tay — `AC-23`, `AC-24`); lưu bền vững `hiddenInsightIds` (`AC-26`). | `core:domain` | Bước 1 |
| **`home-v2-budget-credit-card`** | Component `BudgetCreditCard` phong cách **3D Candy Orange (`#FFB665` / `#F59145`)**: logo dấu chân + `CHANKO SHOPPING BUDGET`, chip vàng + sóng contactless + mèo 3D trang trí (`contentDescription = null`), số tiền *Ngân sách còn lại* `32sp ExtraBold`, thanh tiến trình capsule (`0..1`) + pill `%`, dòng *Đã chi / tổng ngân sách*, khoảng ngày chu kỳ; giữ nền cam khi vượt hạn mức kèm cảnh báo màu Coral (`#B83248`); hỗ trợ trạng thái `NoBudget`, `Skeleton`, `Error + Retry cục bộ`, `OfflineCache` và `OfflineNoCache`; toàn thẻ là lối vào `BudgetScreen` duy nhất trên Home (`AC-03`..`AC-09`, `AC-18`..`AC-22`). | `home-v2-domain-state` | Bước 2 |
| **`home-v2-item-journey`** | Component `ItemJourneySection` (*"Món đồ gần đây"* + *"Xem tất cả ›"* mở `MyItems` mặc định — `AC-16`): hiển thị $\min(n, 5)$ node tròn (`80–96dp`) xếp ziczac dọc bên trái nối bằng $n - 1$ đoạn nét đứt màu lavender (`#BDA8EB`, $n = 1$ không có dây nối — `AC-12`, `AC-13`); mỗi node có ảnh sản phẩm (hoặc placeholder khi lỗi ảnh), viền màu + badge icon góc trên phải cho đủ 6 trạng thái (`Saved`, `Considering`, `Waiting`, `Ready`, `Purchased`, `Skipped` — không chỉ dùng màu — `AC-14`); chạm node mở trực tiếp `ItemDetailScreen(itemId)` không qua sheet (`AC-15`); 1 mascot Chanko canonical đứng bên phải cuộn cùng Journey, tự thu nhỏ/dịch chuyển trên màn hẹp không che vùng chạm (`AC-17`); hỗ trợ `Empty`, `Loading`, `Error + Retry cục bộ`. | `home-v2-domain-state` | Bước 2 |
| **`home-v2-header-observation-shell`** | Cập nhật `HomeHeader` (lời chào theo buổi + *"Hôm nay mình cùng cân nhắc nhé!"* + Avatar mở `Profile & Settings` gộp — `AC-02`), `ObservationCard` (mèo Chanko ló đầu góc trái dưới + bóng đèn + *"Góc nhìn từ Chanko"* + nút mở giải thích `>` + nút ẩn bền vững theo `insightId` — `AC-26`, `AC-27`); lắp ghép `HomeScreen` đúng thứ tự 4 khu vực (`Header → Budget Credit Card → Item Journey → Observation` — `AC-01`), dọn bỏ `CompactLivingRoom`, `YourDecisionsSection` và `HomeSheetHost` của v1.0; kết nối `MainNavigation` (4 tab + nút `+` mở `URLInputDialog` khi online / báo cần mạng khi offline, bảo toàn scroll & anchor qua process death, đạt chuẩn trợ năng `320dp` & `fontScale = 2.0f` — `AC-28`..`AC-31`). | `home-v2-budget-credit-card`, `home-v2-item-journey` | Bước 3 |

**Build Order:**  
`home-v2-domain-state` $\longrightarrow$ (`home-v2-budget-credit-card` $\parallel$ `home-v2-item-journey`) $\longrightarrow$ `home-v2-header-observation-shell`

---

## 2. Gated Workflow Progress

- [x] **Phase 0: Scope Check & Capability Map** (Đã được người dùng phê duyệt)
- [x] **Phase 1: Specify** (Đã được người dùng phê duyệt tại [docs/features/home-ui-v2-spec.md](docs/features/home-ui-v2-spec.md))
- [x] **Phase 2: Plan** (Kế hoạch kiến trúc tại [tasks/plan.md](tasks/plan.md) & [docs/plans/home-ui-v2-plan.md](docs/plans/home-ui-v2-plan.md) — đã duyệt & hoàn thành)
- [x] **Phase 3: Tasks** (Danh sách công việc tại [tasks/todo.md](tasks/todo.md) & [docs/tasks/home-ui-v2-tasks.md](docs/tasks/home-ui-v2-tasks.md) — đã duyệt & hoàn thành)
- [x] **Phase 4: Implement** (Đã hoàn thành toàn bộ 6 tasks và kiểm chứng 31/31 AC)
