# Implementation Plan: Chanko Home — Compose v2.0 (Image-Aligned)

## Overview
Triển khai màn hình `HomeScreen` và `GlobalQuickActionSheet` trên Jetpack Compose bám sát 100% ảnh chuẩn [`approved-home-user-reference.png`](file:///Users/buihoa/HocTap/Android/chanko/chanko-home-compose-handoff-v2/references/approved-home-user-reference.png) và đặc tả [`SPEC-home.md`](file:///Users/buihoa/HocTap/Android/chanko/SPEC-home.md) (v2.0). Kiến trúc sử dụng Clean Architecture + MVVM (`BaseViewModel`), trạng thái độc lập theo từng khu vực (`SectionState<T>`), tiêm đồng hồ (`Clock` / `ZoneId`), và bộ dữ liệu kịch bản xác định gồm **22 scenarios (`S01`–`S22`)** từ `fixtures/home-scenarios.json`.

## Architecture Decisions
1. **Image-Aligned Visual Hierarchy (`v2.0`):**
   - Thứ tự vùng cuộn trong `HomeScreen`: `HomeHeader` (có icon `☀️` và dòng phụ kèm `💜`, nút Gear tròn lavender, Avatar tròn tím) → `CompactLivingRoom` (chứa luôn `ChankoDialogue` bubble ở góc trên-trái bên trong scene, mèo trắng 3D nằm chống cằm trên đệm tím, và 4 `RoomProp`: `Hourglass`, `Calendar` ngày động, `Wallet`, `PauseJar` hũ tim hồng) → `QuickActionsRow` (`Quick Check` & `Save an Item`) → `BudgetCard` (kèm pill `"Chi tiết >"`, cột `"Đã chi"` / `"Còn lại"`, badge cam `"Đã tiêu 80%"`, thanh progress tím) → `YourDecisionsSection` (`"Những món cần chú ý"`, tối đa 3 dòng `Ready` / `Waiting` / `Considering` / `Saved`) → `ObservationCard` (`"Góc nhìn hôm nay"`, optional) → `DraftResumeCard` (optional).
2. **Global Quick Action Bottom Sheet (`Modal Overlay`):**
   - Nhấn nút `+` ở giữa `ChankoBottomBar` mở `GlobalQuickActionSheet` dưới dạng modal overlay có scrim (`"Bạn muốn làm gì?"`, nút `X`, 2 tile ngang `Quick Check` và `Save an Item` với bộ icon riêng), giữ nguyên tab `Home` đang chọn và chỉ cho phép tối đa 1 sheet hoạt động tại một thời điểm (`S16`, `S22`).
3. **Pure Kotlin Domain Helpers cho Ranking & Budget:**
   - `DecisionRanker`: Dedup theo ID, lọc bỏ status không hợp lệ (`S18`), sắp xếp ổn định `Ready → Waiting → Considering → Saved`, giữ nguyên thứ tự upstream trong cùng nhóm và lấy tối đa 3 items (`S01`–`S04`, `S20`).
   - `BudgetCalculator`: Xử lý bằng `Long` minor units (VND), không chia cho 0 (`S07`), không biến `null` thành `0 ₫` (`S08`), giữ số âm khi vượt hạn mức và kẹp thanh tiến độ trong `0f..1f` (`S06`).
4. **Living Room Layered Asset Pack v3 (`T14` — `11-ASSET-DELIVERY.md`):**
   - Sử dụng bộ 6 file PNG tách lớp trong `chanko-home-compose-handoff-v2/assets/living-room/` (`room_background.png`, `cat_on_cushion.png`, `hourglass_books.png`, `calendar_blank.png`, `wallet_mint.png`, `pause_jar_heart.png`) lắp ráp theo tọa độ chuẩn hóa trong `scene-layout.json` (`z = 0..6`, tỷ lệ `2.5:1`), kèm chữ động `month` / `day` nghiêng `-3°` trên `calendar_blank.png` và 3 ảnh thumbnail mẫu (`sample_jacket.png`, `sample_headphones.png`, `sample_keyboard.png`) cho kịch bản `S21`.

---

## Dependency Graph

```text
Phase 0: Discovery & TECH Alignment (Done)
   │
   ▼
Phase 1: Candy Pop v2 Tokens & Sample Assets (Task 1)
   │
   ▼
Phase 2: v2 State Contracts, 22 Scenarios & HomeHeader (Tasks 2–3)
   │
   ├──► Phase 3 & 4: CompactLivingRoom + ChankoDialogue Overlay (Tasks 4–5)
   │
   ├──► Phase 5: QuickActionsRow — Quick Check & Save an Item (Task 6)
   │
   ├──► Phase 6: BudgetCard & BudgetCalculator (Task 7)
   │
   ├──► Phase 7: YourDecisionsSection & DecisionRanker (Task 8)
   │
   ├──► Phase 8: Optional ObservationCard & DraftResumeCard (Task 9)
   │
   └──► Phase 9: ChankoBottomNavigation + HomeSheetHost (Task 10)
           │
           ▼
   Phase 10 & 11: Full 22-Scenario Integration, Matrix Previews & QA Sign-off (Tasks 11–12)
```

---

## Task List

### Phase 0: Clean Slate & Discovery (`T01`–`T18`)
- [x] **Task 0: Clean Slate Teardown & Phase 0 Discovery (`SPEC-home.md` v2.0)**

---

### Phase 1 & 2: Foundation, State Contracts & Header (v2 + Asset Pack v3)
- [ ] **Task 1 (Phase 1): Candy Pop v2 Semantic Tokens, Strings & Asset Pack v3 Drawables (6 Living Room PNGs + 3 Sample Thumbnails)**
- [ ] **Task 2 (Phase 2a): Full v2 State/Event Contracts (`HomeUiState`, `HomeAction`, `SectionState`) & Pure Calculators (`DecisionRanker`, `BudgetCalculator`)**
- [ ] **Task 3 (Phase 2b): `HomeHeader` v2 (Sun/Heart, Lavender Gear, Purple Avatar) & `HomeScreen` Shell**

### Checkpoint 1: Foundation & Header (After Tasks 1–3)
- [ ] Unit tests cho `DecisionRanker` (`S01`–`S04`, `S18`, `S20`) và `BudgetCalculator` (`S06`–`S08`) pass 100%.
- [ ] `HomeHeader` hiển thị đúng ảnh chuẩn v2, hỗ trợ tên dài/font 2.0; build `./gradlew assembleDebug` thành công.

---

### Phase 3, 4 & 5: Living Room Scene, Dialogue Overlay & Quick Actions
- [ ] **Task 4 (Phase 3): `CompactLivingRoom` Scene Assembly (`scene-layout.json`, 6 PNG Layers, `-3°` Dynamic Calendar Overlay & 4 $\ge 48\text{dp}$ Hit Targets)**
- [ ] **Task 5 (Phase 4): `ChankoDialogue` Bubble Overlay (`z = 6`) bên trong `CompactLivingRoom`**
- [ ] **Task 6 (Phase 5): `QuickActionsRow` (`Quick Check` Gradient Card & `Save an Item` Card)**

### Checkpoint 2: Scene & Direct Actions (After Tasks 4–6)
- [ ] 4 `RoomProp` có vùng chạm độc lập $\ge 48\text{dp}$, không chồng lấn; `Calendar` hiển thị ngày/tháng động từ `Clock`/`ZoneId` (`S19`).
- [ ] `ChankoDialogue` nằm ở góc trên-trái bên trong scene, ẩn sạch khi `null`, không tràn ở `fontScale = 2.0`.
- [ ] `Quick Check` và `Save an Item` phát 2 intent riêng biệt, tự xếp dọc ở `320dp` / font lớn.

---

### Phase 6, 7 & 8: Budget, Your Decisions & Optional Sections
- [ ] **Task 7 (Phase 6): `BudgetCard` (`Đã chi`, `Còn lại`, pill `Chi tiết`, badge `Đã tiêu 80%`, progress bar)**
- [ ] **Task 8 (Phase 7): `YourDecisionsSection` & `DecisionRow` (`Ready` mint, `Waiting` lavender + countdown, `Considering`, `Saved`)**
- [ ] **Task 9 (Phase 8): `ObservationCard` (`Góc nhìn hôm nay`, `Explain`/`Hide`/`X`) & `DraftResumeCard`**

### Checkpoint 3: Content Sections Complete (After Tasks 7–9)
- [ ] Toàn bộ các section chính render khớp ảnh chuẩn `S21`.
- [ ] Khi `observation == null` hoặc `draft == null`, không để lại khoảng trống dư (`S01`, `S09`–`S11`).
- [ ] Khi timer hết hạn nhưng source còn `Waiting` (`S12`), không tự chuyển sang `Ready` hoặc tự mua.

---

### Phase 9, 10 & 11: Navigation, Sheets, 22 Scenarios & QA Sign-Off
- [ ] **Task 10 (Phase 9): `HomeSheetHost` (`GlobalQuickActionSheet` `S22` & `ContextualSheet` `S17`) + `MainNavigation` `+` Wiring (`S16`)**
- [ ] **Task 11 (Phase 10): `HomeScenarioFixtures` (Đầy đủ 22 Scenarios `S01`–`S22`) & `HomeViewModel` Reducer/Effects Integration**
- [ ] **Task 12 (Phase 11): Responsive/Font-Scale Preview Matrix (`320/393/600dp`, `S21`, `S22`) & Full Unit Test Suite Verification**

### Checkpoint 4: Complete & Ready for Review (After Tasks 10–12)
- [ ] Toàn bộ 22 scenarios (`S01`–`S22`) có unit tests kiểm chứng trong `HomeViewModelTest`.
- [ ] Lệnh `./gradlew assembleDebug :app:testDevDebugUnitTest` pass 100%.
- [ ] Báo cáo đối chiếu đầy đủ các tiêu chí `A01`–`A18` và `V01`–`V05`.

---

## Risks and Mitigations
| Risk | Impact | Mitigation |
|---|---|---|
| Bong bóng thoại che mặt mèo hoặc đè lên vùng chạm của 4 vật thể khi chữ to (`fontScale = 2.0`) | High | Thiết kế `CompactLivingRoom` cho phép chiều cao linh hoạt theo nội dung bubble ở nửa trên-trái, đặt mèo ở giữa-phải và hàng 4 prop ở dải dưới có vùng chạm tách biệt. |
| Cắt chữ số tiền trên màn hẹp `320dp` (`S15`) | High | Dùng `Adaptive/Flow` layout trong `BudgetCard` và `DecisionRow` để tự động xuống dòng khi thiếu chiều ngang; không bao giờ dùng `TextOverflow.Ellipsis` cho số tiền. |
| Nhấn `+` nhiều lần mở chồng nhiều Bottom Sheet (`S16`) | Medium | Quản lý `activeSheet: HomeSheet?` làm nguồn sự thật duy nhất; `AddTapped` chỉ gán `activeSheet = HomeSheet.Global`. |

## Open Questions
- Không còn (Người dùng đã phê duyệt `SPEC-home.md` v2.0 và phương án xử lý `T14`).
