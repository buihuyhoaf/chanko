# Implementation Plan: Chanko Home v2.2 (Tri-Cycle Budget Carousel & Budget Integration)

> **Spec tham chiếu:** [docs/features/home-ui-v2.2-budget-spec.md](../docs/features/home-ui-v2.2-budget-spec.md) & [SPEC-home-ui-v2.2-budget.md](../SPEC-home-ui-v2.2-budget.md)  
> **Nguồn bàn giao gốc:** `Chanko-Home-v2.2-Implementation.docx` (04/11/2026)  
> **Chiến lược dữ liệu:** **Fake-First** (Dùng chung `FakeBudgetRepository` trong Phase 1; Room Entity/DAO/Migration quy hoạch cho Phase sau).

---

## 1. Overview (Tổng quan triển khai)

Nâng cấp khu vực Ngân sách trên `HomeScreen` từ thẻ đơn `BudgetCreditCard` v2.0 thành `BudgetCreditCardCarousel` v2.2:
- Carousel tối đa 3 vị trí: `Previous` (chỉ xem, đã đóng), `Current` (đang diễn ra), `Next` (đã cấu hình hạn mức hoặc placeholder trung tính viền nét đứt).
- Mặc định luôn snap về `Current` khi vào hoặc quay lại Home.
- Hệ thống 12 theme Candy Gradient chạy theo `floorMod(ordinal, 12)` gắn chặt với identity của chu kỳ.
- Vượt ngân sách clamp thanh tiến trình ở 100%, nhãn hiển thị % thực tế (ví dụ: `110%`), text cảnh báo màu Coral trên nền gradient của kỳ.
- Nút "Xem tất cả" nằm ở Header của section Budget, mở danh sách đầy đủ lịch sử chu kỳ.
- Cả Home và Budget đọc/ghi từ một `FakeBudgetRepository` dùng chung để kiểm chứng luồng thiết lập kỳ sau $\to$ cập nhật Home $\to$ rollover tức thì.

---

## 2. Architecture Decisions (Quyết định Kiến trúc Cốt lõi)

1. **Fake-First & In-Memory Shared State:**
   - Hoãn việc tạo Room Entity, DAO và migration đến giai đoạn kết nối dữ liệu thật.
   - Triển khai StateFlow in-memory trong `FakeBudgetRepository` cấp Singleton để `HomeViewModel` và `BudgetViewModel` cùng quan sát và cập nhật dữ liệu nhất quán.
2. **Domain Cycle Decoupling:**
   - Module `budget-cycle-domain` hoàn toàn thuần Kotlin, không phụ thuộc vào `Color` hay UI Tokens. Domain sở hữu `cycleId`, `ordinal`, ranh giới ngày neo $[\text{start}, \text{nextStart})$, mô hình `TriCycleWindow`, và quy tắc tính Pay Now/Credit.
   - UI layer tự dùng `floorMod(ordinal, 12)` để tra cứu Brush gradient từ `CandyBudgetThemes`.
3. **Bounded Carousel Interaction:**
   - Dùng `HorizontalPager` trong `HomeScreen` cuộn dọc. Giới hạn snap tối đa 1 trang mỗi gesture fling.
   - Header của section Budget chứa tiêu đề `"Ngân sách mua sắm"` và nút `"Xem tất cả"` nằm bên ngoài pager để luôn bấm được bất kể pager đang ở trang nào.
4. **Offline Resilience & Write Protection:**
   - Offline có cache cho phép xem đầy đủ các thẻ.
   - Thao tác thiết lập kỳ sau yêu cầu kết nối mạng; nếu ngoại tuyến thì chặn từ UI và thông báo lý do rõ ràng.

---

## 3. Dependency Graph & Implementation Order

```
[candy-budget-tokens] (Task 1) ──┐
                                 ├──> [budget-ui-models] (Task 5) ──> [budget-carousel-ui] (Task 6, 7)
[budget-cycle-domain] (Task 2, 3) ┘                 │                                │
        │                                           │                                │
        └──> [budget-data-contract] (Task 4) ───────┘                                │
                        │                                                            │
                        └───────────────────────────> [budget-home-integration] (Task 8, 9, 10)
```

---

## 4. Task List & Checkpoints

### Phase 1: Foundation (Tokens & Domain Model)
- [x] **Task 1:** Hệ thống 12 Candy Gradient Themes & Neutral Tokens (`candy-budget-tokens`)
- [x] **Task 2:** Domain Cycle Identity, Ordinal Math & TriCycleWindow (`budget-cycle-domain`)
- [x] **Task 3:** Domain Budget Calculation & Rollover Logic (`budget-calculation`)

### Checkpoint 1: Foundation Verification
- [x] Chạy kiểm thử Token & Domain: `./gradlew testDebugUnitTest --tests "*BudgetCycle*" --tests "*CandyBudget*"`
- [x] Build biên dịch sạch: `./gradlew compileDebugKotlin`

### Phase 2: Shared Data Contract & UI State Models
- [x] **Task 4:** Mở rộng `FakeBudgetRepository` dùng chung & Hợp đồng Cửa sổ Chu kỳ (`budget-data-contract`)
- [x] **Task 5:** Thiết kế `BudgetCarouselUi` & `BudgetCardSlotUi` State Models (`budget-ui-models`)

### Checkpoint 2: Contract Verification
- [x] Kiểm thử Repository & State Mapping: `./gradlew testDebugUnitTest --tests "*FakeBudgetRepositoryTest*" --tests "*HomeDomainLogicTest*"`

### Phase 3: Carousel UI Components (`budget-carousel-ui`)
- [x] **Task 6:** Xây dựng Composable các loại Thẻ Ngân sách (`BudgetCreditCard` v2.2 & `BudgetPlaceholderCard`)
- [x] **Task 7:** Xây dựng Composable `BudgetCreditCardCarousel` với `HorizontalPager`

### Checkpoint 3: UI Component Verification
- [x] Preview hiển thị đủ 4 loại thẻ và các trạng thái loading/lỗi/rỗng.
- [x] Không có lỗi biên dịch: `./gradlew assembleDebug`

### Phase 4: Integration, Navigation & Verification (`budget-home-integration`)
- [x] **Task 8:** Tích hợp Carousel & Header "Xem tất cả" vào `HomeScreen` & `HomeViewModel`
- [x] **Task 9:** Mở rộng Điều hướng `BudgetRoute` & Tích hợp Luồng Thiết lập Ngân sách Kỳ sau
- [x] **Task 10:** Kiểm thử Xuyên Suốt (End-to-End Verification) & Kiểm tra Hồi quy (BC22-01..BC22-24)

### Checkpoint 4: Final Sign-off
- [x] Tất cả unit tests trong dự án chạy pass: `./gradlew testDebugUnitTest`
- [x] Build debug hoàn tất: `./gradlew assembleDebug`
- [x] 24 tiêu chí nghiệm thu BC22-01..BC22-24 đạt yêu cầu.

---

## 5. Risks and Mitigations

| Rủi ro | Mức độ | Biện pháp giảm thiểu |
| :--- | :---: | :--- |
| **Xung đột cử chỉ Pager lồng trong LazyColumn** | Medium | Sử dụng Compose `HorizontalPager` tiêu chuẩn, cấu hình `beyondBoundsPageCount = 1`, không cho phép fling vượt quá 1 trang để tránh chặn cuộn dọc. |
| **Lệch trạng thái giữa Home và Budget** | High | Sử dụng chung một instance `FakeBudgetRepository` được inject qua Hilt Singleton trong giai đoạn Fake-first. |
| **Độ tương phản màu text trên 12 themes** | Medium | 12 theme đã được chuẩn hóa Sáng $\to$ Chính $\to$ Đậm; text chính dùng màu trắng/ink tương phản cao; kiểm tra qua preview ở cả 12 themes. |
| **Nhầm lẫn kỳ khi vuốt** | Medium | Badge trạng thái nổi bật (`ĐÃ KẾT THÚC`, `ĐANG DIỄN RA`, `SẮP TỚI`); luôn snap về `Current` khi vào lại màn hình. |

---

## 6. Open Questions

*Không còn câu hỏi mở. Sẵn sàng thực hiện.*
