# Capability Map & Master Spec: Chanko Home UI v1.0 (Candy Pop 3D)

> **Tài liệu đặc tả chi tiết:** [docs/features/home-ui-v1-spec.md](docs/features/home-ui-v1-spec.md)  
> **Tài liệu bàn giao gốc:** `Chanko-Home-Design-Handoff-v1.0` (02/11/2026 · 22 Frozen Decisions · 22 Acceptance Criteria · Mockup Candy Pop 3D)  
> **Trạng thái:** COMPLETED & VERIFIED (22/22 AC PASSED)

---

## 1. Capability Map (Phase 0)

Yêu cầu nâng cấp `HomeScreen` theo gói bàn giao `Chanko-Home-Design-Handoff-v1.0` bao gồm 5 năng lực độc lập có thể kiểm thử tách biệt theo thứ tự phụ thuộc một chiều:

| Module ID | Trách nhiệm chính | Phụ thuộc (Depends on) | Thứ tự xây dựng (Build Order) |
| :--- | :--- | :--- | :--- |
| **`home-domain-reconcile`** | Logic phân nhóm đồng nhất giữa Little Room & Attention; tự động chuyển món hết hạn `Cooling-off` từ *Đang chờ* sang *Cần xem lại* mà không tự đổi verdict thành `BUY` (`AC-07`), chống đếm trùng `itemId` (`AC-08`), ưu tiên tối đa 3 món cần hành động (`AC-09`), xử lý giá thiếu `"Chưa có giá"` (`HOME-EDGE-04`) và đồng bộ chu kỳ Budget (`AC-11`, `AC-13`). | `core:domain`, `budget-domain-data` | Bước 1 |
| **`home-layout-header-room`** | Cấu trúc cuộn 5 khu vực theo thứ tự chốt: `Header → Little Room → Những món cần chú ý → Budget → Observation` (`AC-01`); gỡ bỏ hoàn toàn 2 thẻ Quick Actions cũ (`AC-02`); `HomeHeader` (chào theo buổi, badge offline + timestamp, avatar mở Profile `AC-03`); `LittleRoom` 4 vật thể (*Đang chờ*, *Cần xem lại*, *Ngân sách*, *Đã bỏ qua*) + pill nhãn + badge động + bóng thoại ngắn (`HOME-GAP-05`). | `home-domain-reconcile` | Bước 2 |
| **`home-contextual-sheet`** | `ContextualBottomSheet` cho 4 vật thể Little Room (`AC-04`) và nút `"Giải thích thêm"` của Observation, hỗ trợ empty state theo ngữ cảnh (`AC-05`), cô lập lỗi/loading (`AC-15`), điều hướng action chính sang `MyItems` kèm bộ lọc (`COOLING_OFF`, `READY`, `SKIPPED`) hoặc `BudgetScreen` (`AC-06`), và tách biệt state để **không tự mở lại sheet** sau khi Android hủy tiến trình (`AC-19`). | `home-layout-header-room` | Bước 3 |
| **`home-attention-budget-obs`** | Khung card bao ngoài mục *Những món cần chú ý* (tối đa 3 món, empty state nhỏ `HOME-STATE-01`, tap mở `ItemDetailScreen(itemId)` `AC-10`); `BudgetCard` đặt sau Attention với toàn bộ vùng chạm mở `BudgetScreen` (`AC-12`); `ObservationCard` (*Góc nhìn từ Chanko*) chỉ hiển thị khi đủ dữ liệu và lưu bền vững `insightId` đã ẩn (`AC-14`). | `home-domain-reconcile` | Bước 4 |
| **`home-shell-navigation-a11y`** | Kết nối `HomeRoute` & `MainNavigation` (5 vị trí `Trang chủ / Món đồ / + / Trò chuyện / Phân tích`, chặn nút `+` khi offline không mở Add Item và không tạo draft `AC-16`, `AC-17`), giữ vị trí cuộn khi quay lại (`AC-18`), đảm bảo vùng chạm $\ge 48\text{dp}$ + TalkBack semantics (`AC-20`), tôn trọng Reduced Motion (`AC-21`) và không gây áp lực mua sắm (`AC-22`). | `home-layout-header-room`, `home-contextual-sheet`, `home-attention-budget-obs` | Bước 5 |

**Build Order:**  
`home-domain-reconcile` $\longrightarrow$ `home-layout-header-room` & `home-attention-budget-obs` $\longrightarrow$ `home-contextual-sheet` $\longrightarrow$ `home-shell-navigation-a11y`

---

## 2. Gated Workflow Progress

- [x] **Phase 0: Scope Check & Capability Map** (Đã được người dùng phê duyệt)
- [x] **Phase 1: Specify** (Đã được người dùng phê duyệt tại [docs/features/home-ui-v1-spec.md](docs/features/home-ui-v1-spec.md))
- [x] **Phase 2: Plan** (Kế hoạch kiến trúc tại [docs/plans/home-ui-v1-plan.md](docs/plans/home-ui-v1-plan.md) & [tasks/plan.md](tasks/plan.md) — chờ duyệt)
- [x] **Phase 3: Tasks** (Danh sách công việc tại [docs/tasks/home-ui-v1-tasks.md](docs/tasks/home-ui-v1-tasks.md) & [tasks/todo.md](tasks/todo.md) — chờ duyệt)
- [x] **Phase 4: Implement** (Đã hoàn thành & kiểm chứng toàn bộ 7 tasks, 22/22 AC)
