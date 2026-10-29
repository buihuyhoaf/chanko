# Capability Map & Master Spec: Chanko Budget UI v2.0

> **Tài liệu đặc tả chi tiết:** [docs/features/budget-ui-v2-spec.md](docs/features/budget-ui-v2-spec.md)  
> **Tài liệu bàn giao gốc:** `chanko-budget-ui-v2.0-handoff 3`  
> **Trạng thái:** SPEC APPROVED · READY FOR PLAN & TASKS

---

## 1. Capability Map

| Module ID | Trách nhiệm | Phụ thuộc | Thứ tự xây dựng (Build Order) |
|---|---|---|---|
| **`budget-domain-data`** | Pure Domain Models (`MoneyVnd`, `BudgetCycle`, `BudgetSnapshot`, `CreditCard`, `BudgetActivity`, `BudgetAdjustment`) + Repository Interface + `FakeBudgetRepository` với 100% fixture C10–C13. | — | Bước 1 |
| **`budget-overview-ui`** | Màn hình chính **C01 Budget Overview**, tích hợp state **C10 (Over-budget)**, **C11 (Offline)**, **C12 (Past cycle)**, **C13 (Zero-spend)**; Kết nối effect từ `BudgetCard` của `HomeScreen`. | `budget-domain-data` | Bước 2 |
| **`budget-cycle-wallet`** | **C02 (Sheet Chọn chu kỳ)**, **C03 (Sheet Thiết lập ví)**, **C09 (Sheet Lịch sử điều chỉnh ngân sách)**. | `budget-domain-data`, `budget-overview-ui` | Bước 3 |
| **`budget-edit-preview`** | **C04 (Sheet Sửa ngân sách & Live Preview)**: Tính toán tức thời remaining/over/progress theo từng ký tự, cảnh báo hạ hạn mức dưới đã chi, validation số nguyên 1–999.999.999.999đ. | `budget-domain-data` | Bước 4 |
| **`budget-card-management`** | Phân hệ thẻ tín dụng: **C05 (Quản lý thẻ)**, **C06 (Thêm thẻ)**, **C07 (Đổi tên thẻ)**, **C08 (Dialog xác nhận xóa thẻ)**. Quy chuẩn 1–40 ký tự, NFC, trim, trùng lặp case-insensitive. | `budget-domain-data`, `budget-cycle-wallet` | Bước 5 |

Build order: `budget-domain-data` → `budget-overview-ui` → `budget-cycle-wallet` → `budget-edit-preview` → `budget-card-management`.

---

## 2. Gated Workflow Progress

- [x] **Phase 0: Scope Check & Capability Map** (Hoàn thành & đã được duyệt)
- [x] **Phase 1: Specify** (Đặc tả hoàn thành tại `docs/features/budget-ui-v2-spec.md`)
- [x] **Phase 2: Plan** (Kế hoạch kiến trúc hoàn thành tại `docs/plans/budget-ui-v2-plan.md`)
- [x] **Phase 3: Tasks** (Bẻ khóa 11 đầu việc kiểm thử được tại `docs/tasks/budget-ui-v2-tasks.md`)
- [ ] **Phase 4: Implement** (Tiến hành code và chạy unit tests xác minh)
