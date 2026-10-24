# Implementation Plan: Chanko Home — Product UX & Implementation Specification v1.1

## Overview
Xây dựng lại màn hình `HomeScreen` và nâng cấp điều hướng `MainNavigation` theo **Chanko Home — Product UX & Implementation Specification v1.1** (`SPEC-home.md`). Màn hình được triển khai theo hướng UI-first với deterministic fake/fixture data, hỗ trợ **Section-aware Data States** và bao gồm 7 thành phần trong vùng cuộn (`HOME-01 Header` → `HOME-02 Compact Living Room` → `HOME-03 Quick Actions` → `HOME-04 Budget Snapshot` → `HOME-05 Your Decisions` → `HOME-06 Observation (optional)` → `HOME-07 Local Draft Indicator (optional)`) cùng `HOME-08 Bottom Navigation + Global (+) Bottom Sheet`.

## Architecture Decisions
1. **Clean Slate First:** Tách bước xóa toàn bộ UI/ViewModel/Model/Resource của Home v5 cũ thành một commit độc lập (`Task 0`) trước khi dựng kiến trúc v1.1.
2. **Section-Aware State (`HomeUiState`):** Thay vì một trạng thái `Loading/Error` toàn màn hình, mỗi khu vực dữ liệu (`Budget`, `Your Decisions`, `Observation`, `Local Draft Indicator`) có trạng thái độc lập (`SectionState<T>: Loading | Content(data, isFromCache) | Error`), đảm bảo lỗi ở một section không làm sập toàn bộ Home.
3. **Deterministic Fake Data (`HomeFakeDataSource`):** Cung cấp dữ liệu giả lập xác định (không random khi recompose, không gọi mạng) hỗ trợ đầy đủ các kịch bản: `Default`, `NewUser`, `OfflineCache`, `OverBudget`, `PartialError`.
4. **Strict Phase Gate (`P1 → P6`):** Mỗi phase tương ứng với một lát cắt dọc (vertical slice) hoàn chỉnh, có Unit Tests + Compose Previews và dừng lại chờ người dùng nghiệm thu trước khi qua phase tiếp theo.

---

## Task List

### Phase 0: Clean Slate Teardown
- [x] **Task 0: Clean Slate Teardown (Xóa Home v5 cũ & dọn tài nguyên)**

### Checkpoint: Clean Slate
- [x] Build `./gradlew assembleDebug` và `./gradlew testDebugUnitTest` pass 100%.

---

### Phase P1: Home Foundation + Header (`HOME-01`)
- [x] **Task 1: Section-aware `HomeUiState`, `HomeUiEvent`, `HomeFakeDataSource` & `HomeViewModel` Foundation (P1)**
- [x] **Task 2: `HomeHeader` (`HOME-01`), `HomeScreen` Scroll/Insets Scaffold & P1 Previews/Tests**

### Checkpoint: P1 Complete
- [x] Render ổn định với section-aware foundation, `HomeHeader` hỗ trợ tên dài/tên trống/font scaling, tôn trọng system insets.
- [x] Có Compose Previews và Unit Tests cho P1; `./gradlew assembleDebug :app:testDevDebugUnitTest` pass.
- [x] Dừng lại chờ người dùng review trước khi qua P2.

---

### Phase P2: Compact Living Room (`HOME-02`)
- [ ] **Task 3: Living Room Models, Smart Dialogue (`Friendly`/`Sassy`/`Savage`) & 4 Interactive Objects (`Hourglass`, `Calendar`, `Wallet`, `Pause Jar`)**
- [ ] **Task 4: Contextual Preview Bottom Sheets cho 4 Objects & P2 Previews/Tests**

### Checkpoint: P2 Complete
- [ ] Đúng 4 objects có vùng chạm riêng $\ge 48\text{dp}$, không chồng lấn, có semantics, mở đúng Contextual Preview Bottom Sheet.
- [ ] Dừng lại chờ người dùng review trước khi qua P3.

---

### Phase P3: Quick Actions + Global `+` (`HOME-03`, `HOME-08`)
- [ ] **Task 5: `QuickActionsSection` (`Quick Check`, `Save an Item`) & Global `+` Action Bottom Sheet trong `MainNavigation`**

### Checkpoint: P3 Complete
- [ ] 2 direct CTAs trên Home phát đúng action; nút `+` mở Bottom Sheet gồm `Quick Check` & `Save an Item`, dismiss giữ nguyên tab/scroll.
- [ ] Dừng lại chờ người dùng review trước khi qua P4.

---

### Phase P4: Budget Snapshot (`HOME-04`)
- [ ] **Task 6: `BudgetSnapshotSection` (4 ngưỡng `Normal`/`Warning`/`Reached`/`OverBudget`, state transition warning, loại trừ non-purchased items) & P4 Tests**

### Checkpoint: P4 Complete
- [ ] Tính đúng tiến độ và ngưỡng cảnh báo, không chia cho 0, hỗ trợ cached/error state cấp section.
- [ ] Dừng lại chờ người dùng review trước khi qua P5.

---

### Phase P5: Your Decisions (`HOME-05`)
- [ ] **Task 7: `YourDecisionsSection` (Priority feed tối đa 3 items: `Ready -> Waiting -> Considering -> Saved`, `View All`, empty/loading/error state) & P5 Tests**

### Checkpoint: P5 Complete
- [ ] Thứ tự ưu tiên chính xác, không nhầm `Considering` với `Waiting`, tối đa 3 items, callback đúng item ID.
- [ ] Dừng lại chờ người dùng review trước khi qua P6.

---

### Phase P6: Observation, Draft, Integration & Polish (`HOME-06`, `HOME-07`)
- [ ] **Task 8: `ObservationSection` (`Explain`, `Dismiss`, Semantic Finding ID) & `LocalDraftIndicatorSection` (4 sync states)**
- [ ] **Task 9: End-to-End Scenarios (`NewUser`, `OfflineCache`, `PartialError`), Accessibility/Font-Scale Previews & Full Smoke Tests**

### Checkpoint: P6 Complete & Done
- [ ] Toàn bộ tiêu chí nghiệm thu P1–P6 trong `SPEC-home.md` hoàn tất.
- [ ] `./gradlew assembleDebug testDebugUnitTest` pass 100%.

---

## Risks and Mitigations
| Risk | Impact | Mitigation |
|---|---|---|
| Lỗi ở một section làm toàn bộ Home chuyển sang màn hình lỗi | High | Thiết kế `SectionState<T>` riêng cho từng section (`Budget`, `Decisions`) ngay từ P1. |
| Nút `+` làm đổi tab hoặc nhân bản Bottom Sheet khi xoay màn hình | Medium | Quản lý trạng thái hiển thị sheet bằng `rememberSaveable` / single state source trong `MainNavigation`, tách biệt hoàn toàn với `NavController.selectTab`. |
| Thiếu PNG 3D riêng cho `Hourglass` và `Wallet` | Low | Sử dụng vector/icon 3D-styled trên nền badge `CandyColors` trong P2, tách rời khỏi logic vùng chạm và ghi rõ trong báo cáo bàn giao. |

## Open Questions
- Không còn cho P0/P1.
