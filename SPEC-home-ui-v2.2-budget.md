# Capability Map & Master Spec: Chanko Home v2.2 (Tri-Cycle Budget Carousel & Budget Integration)

> **Tài liệu đặc tả chi tiết:** [docs/features/home-ui-v2.2-budget-spec.md](docs/features/home-ui-v2.2-budget-spec.md)  
> **Nguồn bàn giao gốc:** `Chanko-Home-v2.2-Implementation.docx` (04/11/2026 · 24 Acceptance Criteria `BC22-01`..`BC22-24`)  
> **Trạng thái:** SPEC APPROVED / READY FOR PLAN

---

## 1. Capability Map (Phase 0)

Bản nâng cấp **Chanko Home v2.2** chuyển đổi thẻ ngân sách đơn trên `HomeScreen` thành **`BudgetCreditCardCarousel` (Carousel tối đa 3 vị trí: Previous, Current, Next)**. Toàn bộ tính năng được phân rã thành 4 module độc lập, kiểm thử riêng biệt với quan hệ phụ thuộc một chiều:

| Module ID | Trách nhiệm chính | Phụ thuộc (Depends on) | Thứ tự xây dựng (Build Order) |
| :--- | :--- | :--- | :---: |
| **`candy-budget-tokens`** | Sở hữu 12 theme Candy Gradient (Sáng $\to$ Chính $\to$ Đậm: Tangerine, Lavender, Mint, Strawberry, Sky Blue, Lemon, Peach, Grape, Aqua, Rose, Pistachio, Blueberry), token thẻ trung tính (Neutral Placeholder: viền nét đứt, icon `+`), và token cảnh báo Coral khi vượt ngân sách (`#B83248`). Cung cấp bảng tra cứu theme độc lập theo index `0..11`. | — | Bước 1 (Song song) |
| **`budget-cycle-domain`** | Sở hữu định danh chu kỳ (`cycleId`), số thứ tự bất biến (`ordinal`), công thức ánh xạ theme `floorMod(ordinal, 12)`, quy tắc tính ranh giới ngày neo $[\text{start}, \text{nextStart})$, mô hình cửa sổ 3 kỳ (`TriCycleWindow`), phép tính tổng chi kết hợp Pay Now và Credit theo ngày mua, và cơ chế chuyển chu kỳ (rollover) idempotent. | — | Bước 1 (Song song) |
| **`budget-carousel-ui`** | Thành phần `BudgetCreditCardCarousel` sử dụng `HorizontalPager` tối đa 3 trang (`[Previous? - Current - Next]`). Tự động snap về `Current` khi mở hoặc quay lại Home. Hỗ trợ 4 kiểu thẻ: Previous (Đã kết thúc, chỉ đọc), Current (Đang diễn ra), Next Configured (Sắp tới, hạn mức dự kiến), Next Unconfigured (Placeholder viền nét đứt). Xử lý đầy đủ các trạng thái rỗng, loading skeleton, lỗi cục bộ và offline. Chuẩn hóa cử chỉ (gesture) không cướp cuộn dọc và TalkBack accessibility. | `candy-budget-tokens`, `budget-cycle-domain` | Bước 2 |
| **`budget-home-integration`** | Tích hợp Carousel vào `HomeScreen`. Đặt nút *"Xem tất cả"* tại Header của section Budget. Triển khai `FakeBudgetRepository` dùng chung giữa Home và Budget để đồng bộ dữ liệu hai chiều (thiết lập kỳ sau $\to$ cập nhật Home). Mở rộng `BudgetRoute` nhận `cycleId` và `mode` điều hướng. Chặn thao tác ghi dữ liệu khi ngoại tuyến. | `budget-carousel-ui` | Bước 3 |

**Build Order:**  
(`candy-budget-tokens` $\parallel$ `budget-cycle-domain`) $\longrightarrow$ `budget-carousel-ui` $\longrightarrow$ `budget-home-integration`

---

## 2. Gated Workflow Progress

- [x] **Phase 0: Scope Check & Capability Map** (Đã phân rã và phê duyệt)
- [x] **Phase 1: Specify** (Đã hoàn thiện tại [docs/features/home-ui-v2.2-budget-spec.md](docs/features/home-ui-v2.2-budget-spec.md))
- [x] **Phase 2: Plan** (Kế hoạch kỹ thuật tại [tasks/plan.md](tasks/plan.md) & [docs/plans/home-v2.2-budget-plan.md](docs/plans/home-v2.2-budget-plan.md))
- [x] **Phase 3: Tasks** (Danh sách công việc tại [tasks/todo.md](tasks/todo.md) & [docs/tasks/home-v2.2-budget-tasks.md](docs/tasks/home-v2.2-budget-tasks.md))
- [ ] **Phase 4: Implement** (Thực thi từng task theo TDD)
