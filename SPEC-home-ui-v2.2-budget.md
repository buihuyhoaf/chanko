# Capability Map & Master Spec: Chanko Home v2.2 (Tri-Cycle Budget Carousel & Budget Integration)

> **Tài liệu đặc tả chi tiết:** [docs/features/home-ui-v2.2-budget-spec.md](docs/features/home-ui-v2.2-budget-spec.md)  
> **Kế hoạch triển khai:** [tasks/plan.md](tasks/plan.md)  
> **Danh sách công việc:** [tasks/todo.md](tasks/todo.md)  
> **Nguồn bàn giao gốc:**
> - `Chanko-Home-v2.2 2/CHANKO-HOME-v2.2-HANDOFF.md` (Bàn giao tổng hợp UX, Technical, Accessibility & QA ngày 05/11/2026)
> - `Chanko-Home-v2.2 2/tokens/design-tokens.json` & `reference/approved-preview.png`
> - `Chanko-Home-v2.2-Implementation.docx` (04/11/2026 · 24 Acceptance Criteria `BC22-01`..`BC22-24`)  
> **Trạng thái:** PLAN & TASKS READY FOR IMPLEMENTATION

---

## 1. Capability Map (Phase 0)

Bản nâng cấp **Chanko Home v2.2** chuyển đổi thẻ ngân sách đơn trên `HomeScreen` thành **`BudgetCreditCardCarousel` (Carousel tối đa 3 vị trí: Previous, Current, Next)**. Toàn bộ tính năng được phân rã thành 4 module độc lập, kiểm thử riêng biệt với quan hệ phụ thuộc một chiều:

| Module ID | Trách nhiệm chính | Phụ thuộc (Depends on) | Thứ tự xây dựng (Build Order) |
| :--- | :--- | :--- | :---: |
| **`candy-budget-tokens`** | Sở hữu 12 theme Candy Gradient (Tangerine, Lavender, Mint, Strawberry, Sky, Lemon, Peach, Grape, Aqua, Rose, Pistachio, Blueberry), token thẻ trung tính (Neutral Placeholder: viền nét đứt, icon `+`), và token cảnh báo Coral khi vượt ngân sách (`#9C2943`). Cung cấp bảng tra cứu theme độc lập theo index `01..12`. | — | Bước 1 (Song song) |
| **`budget-cycle-domain`** | Sở hữu định danh chu kỳ (`cycleId`), số thứ tự bất biến (`ordinal`), `labelMonth` (`01`..`12`), công thức ánh xạ theme `floorMod(ordinal, 12) + 1`, quy tắc tính ranh giới ngày neo $[\text{start}, \text{nextStart})$, mô hình cửa sổ 3 kỳ (`TriCycleWindow`), phép tính tổng chi kết hợp Pay Now và Credit theo ngày mua, và cơ chế chuyển chu kỳ (rollover) idempotent. | — | Bước 1 (Song song) |
| **`budget-carousel-ui`** | Thành phần `BudgetCreditCardCarousel` sử dụng `HorizontalPager` tối đa 3 trang (`[Previous? - Current - Next]`). Tự động snap về `Current` khi mở hoặc quay lại Home. Hỗ trợ các kiểu thẻ: Previous (Đã kết thúc, `"Còn lại cuối kỳ"`), Current (Đang diễn ra, `"Ngân sách còn lại"` hoặc `"Vượt ngân sách"`), Next Configured (`"Ngân sách dự kiến"`, không chi tiêu/progress giả), Next Unconfigured (Placeholder viền nét đứt). Tích hợp Month Chip `MM` trên TopBar (`BC-CARD-LABEL-01`), bỏ dòng `"Đã chi X / Y"` (`BC-CARD-CONTENT-01`). Dải điều hướng bên dưới (`<`, dots, `>`, `"Xem tất cả →"`). | `candy-budget-tokens`, `budget-cycle-domain` | Bước 2 |
| **`budget-home-integration`** | Tích hợp Carousel vào `HomeScreen`. Triển khai `FakeBudgetRepository` dùng chung giữa Home và Budget để đồng bộ dữ liệu hai chiều (thiết lập kỳ sau $\to$ cập nhật Home). Mở rộng `BudgetRoute` nhận `cycleId` và `mode` điều hướng. Chặn thao tác ghi dữ liệu khi ngoại tuyến. | `budget-carousel-ui` | Bước 3 |

**Build Order:**  
(`candy-budget-tokens` $\parallel$ `budget-cycle-domain`) $\longrightarrow$ `budget-carousel-ui` $\longrightarrow$ `budget-home-integration`

---

## 2. Gated Workflow Progress

- [x] **Phase 0: Scope Check & Capability Map** (Đã phân rã và phê duyệt)
- [x] **Phase 1: Specify** (Đặc tả chi tiết tại [docs/features/home-ui-v2.2-budget-spec.md](docs/features/home-ui-v2.2-budget-spec.md))
- [x] **Phase 2: Plan** (Kế hoạch kiến trúc tại [tasks/plan.md](tasks/plan.md))
- [x] **Phase 3: Tasks** (Danh sách 10 công việc chi tiết tại [tasks/todo.md](tasks/todo.md))
- [x] **Phase 4: Implement** (Đã hoàn thành toàn bộ 4 phases & 24 tiêu chí nghiệm thu BC22-01..BC22-24)
