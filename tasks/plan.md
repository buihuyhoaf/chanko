# Implementation Plan: Chanko Home v2.2 (Tri-Cycle Budget Carousel & Budget Integration)

> **Spec tham chiếu:** [docs/features/home-ui-v2.2-budget-spec.md](../docs/features/home-ui-v2.2-budget-spec.md) & [SPEC-home-ui-v2.2-budget.md](../SPEC-home-ui-v2.2-budget.md)  
> **Nguồn chuẩn (Handoff 05/11/2026):**
> - `Chanko-Home-v2.2 2/CHANKO-HOME-v2.2-HANDOFF.md`
> - `Chanko-Home-v2.2 2/tokens/design-tokens.json` & `reference/approved-preview.png`
> - `Chanko-Home-v2.2 2/STATE-MATRIX.csv` (S01..S19) & `NAVIGATION-CONTRACT.csv` (N01..N13)
> - `Chanko-Home-v2.2-Implementation.docx` (04/11/2026)  
> **Chiến lược dữ liệu:** **Fake-First** (dùng chung `FakeBudgetRepository` giữa Home và Budget; Room Entity/DAO/Migration quy hoạch cho Phase tích hợp dữ liệu thật tiếp theo).

---

## 1. Overview (Tổng quan Triển khai)

Nâng cấp khu vực Ngân sách trên `HomeScreen` từ thẻ đơn `BudgetCreditCard` thành **`BudgetCreditCardCarousel` v2.2** tối đa 3 vị trí (`[Previous? - Current - Next]`):
- **Quyết định `BC-CARD-LABEL-01`:** TopBar của thẻ hiển thị Month Chip pill 2 chữ số `MM` (`01`..`12`). Bỏ hoàn toàn năm (`2026`), khoảng ngày (`01/10 – 31/10`) và text badge trạng thái (`ĐANG DIỄN RA`, `ĐÃ KẾT THÚC`, `SẮP TỚI`) trên mặt thẻ.
- **Quyết định `BC-CARD-CONTENT-01`:** Bỏ hoàn toàn dòng `"Đã chi X / Y"` trên mặt tất cả các thẻ.
  - Previous: `"Còn lại cuối kỳ"` + số tiền còn lại + thanh tiến trình + nhãn tỷ lệ % chi thực tế. Mascot: `R.drawable.mascot_sleepy`.
  - Current: `"Ngân sách còn lại"` (hoặc `"Vượt ngân sách"` với số tiền vượt, % thật > 100%, thanh tiến trình clamp 100%, màu cảnh báo Coral `#9C2943`). Mascot: `R.drawable.mascot_happy` / `mascot_worried`.
  - Next đã cấu hình: `"Ngân sách dự kiến"` + hạn mức kế hoạch. Không có chi tiêu hay thanh tiến trình giả định.
  - Next chưa cấu hình: `"Chu kỳ tiếp theo"`, phụ đề `"Chưa thiết lập ngân sách"`, nút pill `+ Thiết lập ngân sách` (`#6326A8`, chữ trắng), Month Chip `MM` ở góc trên, viền nét đứt native.
- **Dải điều hướng Pager:** Phía dưới Carousel có nút `<`, dải chấm chỉ báo trang (chấm active màu tím `#6326A8`), nút `>`, và liên kết `"Xem tất cả →"`.
- **Hệ thống 12 Candy Gradient Themes & Semantic Tokens:** Tangerine, Lavender, Mint, Strawberry, Sky, Lemon, Peach, Grape, Aqua, Rose, Pistachio, Blueberry theo công thức $\text{themeId} = \text{floorMod}(\text{ordinal}, 12) + 1$. Chiều cao thẻ `216.dp`, bo góc `28.dp`, padding `20.dp`.
- **Cơ chế Fake-First:** Home và Budget dùng chung một `FakeBudgetRepository` singleton để dữ liệu đồng bộ hai chiều tức thì.

---

## 2. Architecture Decisions (Quyết định Kiến trúc Cốt lõi)

1. **Fake-First Shared State:**
   - Tạm hoãn Room Entity, DAO và Database Migration sang giai đoạn dữ liệu thật.
   - Triển khai StateFlow in-memory trong `FakeBudgetRepository` (Hilt Singleton) để `HomeViewModel` và `BudgetViewModel` cùng quan sát, đảm bảo tính nhất quán giữa hai màn hình.
2. **Domain Cycle Decoupling & `labelMonth`:**
   - Module `budget-cycle-domain` thuần Kotlin. Domain sở hữu `cycleId`, `ordinal`, `labelMonth` (`01`..`12`), ranh giới $[\text{start}, \text{nextStart})$, mô hình `TriCycleWindow`, và quy tắc tính Pay Now/Credit theo ngày mua.
   - UI layer tự dùng $\text{floorMod}(\text{ordinal}, 12) + 1$ để tra cứu Brush gradient từ `CandyBudgetThemes`.
3. **Tách biệt Thao tác và Điều hướng (Navigation Contract):**
   - Click thẻ phát một effect duy nhất kèm định danh chu kỳ và mode (`ReadOnly`, `Active`, `ViewOrEditPlan`, `SetupNext`, `History`).
   - Chặn double-tap và khóa thao tác ghi/setup khi ngoại tuyến (`OfflineNoCache`).
4. **Pager Gesture & Layout Integrity:**
   - Giới hạn fling gesture tối đa 1 trang mỗi lần vuốt.
   - Không đặt recognizer drag thứ hai trên thẻ để không xung đột cuộn dọc của màn Home.
   - Dải điều khiển (nút `<`, dots, `>`, `"Xem tất cả →"`) nằm dưới Carousel, luôn có thể truy cập độc lập.

---

## 3. Dependency Graph & Implementation Order

```
[candy-budget-tokens] (Task 1) ──┐
                                 ├──> [budget-ui-models] (Task 3) ──> [budget-carousel-ui] (Task 5, 6, 7)
[budget-cycle-domain] (Task 2) ──┘                 │                                │
        │                                          │                                │
        └──> [budget-data-contract] (Task 4) ──────┘                                │
                        │                                                           │
                        └───────────────────────────> [budget-home-integration] (Task 8, 9, 10)
```

---

## 4. Task List & Checkpoints

### Phase 1: Foundation (Tokens & Domain Model)
- [ ] **Task 1:** Cập nhật 12 Candy Gradient Themes & Semantic Tokens theo `design-tokens.json` (`candy-budget-tokens`)
- [ ] **Task 2:** Bổ sung `labelMonth` & Ánh xạ Theme Formula vào Domain Cycle (`budget-cycle-domain`)

### Checkpoint 1: Foundation Verification
- [ ] Chạy kiểm thử Token & Domain: `./gradlew testDebugUnitTest --tests "*BudgetCycle*" --tests "*CandyBudget*"`
- [ ] Biên dịch mã nguồn: `./gradlew compileDebugKotlin`

### Phase 2: Shared Data Contract & UI State Models
- [ ] **Task 3:** Cập nhật `BudgetCardSlotUi` State Models theo `BC-CARD-LABEL-01` & `BC-CARD-CONTENT-01` (`budget-ui-models`)
- [ ] **Task 4:** Cập nhật `FakeBudgetRepository` dùng chung & Hợp đồng Cửa sổ Chu kỳ (`budget-data-contract`)

### Checkpoint 2: Contract Verification
- [ ] Kiểm thử Repository & State Mapping: `./gradlew testDebugUnitTest --tests "*FakeBudgetRepositoryTest*" --tests "*HomeDomainLogicTest*"`
- [ ] Biên dịch mã nguồn: `./gradlew compileDebugKotlin`

### Phase 3: Carousel UI Components (`budget-carousel-ui`)
- [ ] **Task 5:** Tái cấu trúc Composable Mặt Thẻ `BudgetCreditCard` v2.2 (`BC-CARD-LABEL-01` & `BC-CARD-CONTENT-01`)
- [ ] **Task 6:** Tái cấu trúc Composable Thẻ Placeholder `BudgetPlaceholderCard` v2.2
- [ ] **Task 7:** Xây dựng Thanh Điều khiển Pager & Nâng cấp `BudgetCreditCardCarousel`

### Checkpoint 3: UI Component Verification
- [ ] Previews compile & render chính xác các thẻ và trạng thái.
- [ ] Build biên dịch sạch: `./gradlew assembleDebug`

### Phase 4: Integration, Navigation & Verification (`budget-home-integration`)
- [ ] **Task 8:** Tích hợp Carousel & Thanh Điều khiển Pager vào `HomeScreen` & `HomeViewModel`
- [ ] **Task 9:** Mở rộng Điều hướng `BudgetRoute` & Tích hợp Luồng Thiết lập Ngân sách Kỳ sau
- [ ] **Task 10:** Kiểm thử Xuyên Suốt (End-to-End Verification) & Kiểm tra Hồi quy (BC22-01..BC22-24)

### Checkpoint 4: Final Sign-off
- [ ] Toàn bộ unit tests trong toàn bộ ứng dụng pass: `./gradlew testDebugUnitTest`
- [ ] Biên dịch Debug APK thành công: `./gradlew assembleDebug`
- [ ] 24 tiêu chí nghiệm thu BC22-01..BC22-24 đạt yêu cầu.

---

## 5. Risks and Mitigations

| Rủi ro | Mức độ | Biện pháp giảm thiểu |
| :--- | :---: | :--- |
| **Xung đột cử chỉ Pager lồng trong LazyColumn** | Medium | Sử dụng Compose `HorizontalPager` tiêu chuẩn, cấu hình `beyondBoundsPageCount = 1`, snap tối đa 1 trang mỗi gesture fling. |
| **Lệch trạng thái giữa Home và Budget** | High | Sử dụng chung một instance `FakeBudgetRepository` được inject qua Hilt Singleton trong giai đoạn Fake-first. |
| **Độ tương phản chữ trên 12 themes** | Medium | Áp dụng màu text `#241332` trên nền pastel theo `design-tokens.json`; trên theme đậm hoặc khi dùng chữ trắng, áp dụng độ tương phản/đổ bóng cục bộ để đảm bảo WCAG AA $\ge 4.5:1$. |
| **Chu kỳ lệch ngày 1 tính sai tháng `labelMonth`** | Low | Domain sở hữu thuộc tính `labelMonth` chu kỳ; nếu chưa có thì lấy tháng ngày bắt đầu theo chuẩn bàn giao. |
