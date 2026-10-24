# Tasks: Chanko Home v1.1 (`SPEC-home.md`)

Nhánh: `refactor/clean-home-screen` | Kế hoạch: `tasks/plan.md` | Đặc tả: `SPEC-home.md`

---

## Phase 0: Clean Slate Teardown
- [x] **Task 0: Clean Slate Teardown (Xóa Home v5 cũ & dọn tài nguyên)**
  - **Acceptance:**
    - [x] Xóa toàn bộ `components/*`, `model/*`, `data/*`, `HomeViewModel.kt`, `HomeUiState.kt` và tests cũ của Home v5.
    - [x] Gỡ `bindHomeSpendingRepository` trong `RepositoryModule.kt` và xóa 6 drawable mẫu chỉ dùng cho Home v5.
    - [x] Giữ shell tối giản trong `HomeScreen.kt` để `MainNavigation.kt` hoạt động bình thường.
  - **Verify:**
    - [x] `./gradlew assembleDebug testDebugUnitTest` pass 100%.
  - **Files:** `HomeScreen.kt`, `RepositoryModule.kt`, `strings_home.xml`

---

## Phase P1: Home Foundation + Header (`HOME-01`)
- [ ] **Task 1: Section-aware `HomeUiState`, `HomeUiEvent`, `HomeFakeDataSource` & `HomeViewModel` Foundation (P1)**
  - **Acceptance:**
    - [ ] Định nghĩa `SectionState<T>` (`Loading`, `Content(data, isFromCache)`, `Error`) và `HomeUiState` hỗ trợ trạng thái độc lập theo section.
    - [ ] Định nghĩa `HeaderUiModel` (lời chào theo ngữ cảnh, tên người dùng hỗ trợ tên dài/trống, avatar initial).
    - [ ] Tạo `HomeFakeDataSource` (deterministic scenarios: `Default`, `NewUser`, `LongName`, `EmptyName`) và `HomeViewModel` phát `StateFlow<HomeUiState>`.
  - **Verify:**
    - [ ] Unit tests trong `HomeViewModelTest.kt` kiểm tra trạng thái khởi tạo P1, xử lý tên dài/tên trống và callbacks.
  - **Files:** `HomeUiState.kt`, `HomeUiEvent.kt`, `HomeFakeDataSource.kt`, `HomeViewModel.kt`, `HomeViewModelTest.kt`

- [ ] **Task 2: `HomeHeader` (`HOME-01`), `HomeScreen` Scroll/Insets Scaffold & P1 Previews**
  - **Acceptance:**
    - [ ] Xây dựng `HomeHeader.kt` tuân thủ `HOME-01`: hiển thị lời chào theo ngữ cảnh, xử lý tên dài/tên trống và `fontScale = 1.5f`, nút `Settings` và `Avatar` đạt vùng chạm $\ge 48\text{dp}$ kèm semantics và callbacks rõ ràng.
    - [ ] Cập nhật `HomeScreen.kt` (`HomeRoute` stateful + `HomeScreen` stateless) sử dụng `LazyColumn` tôn trọng system insets và khoảng trống dưới cho Bottom Navigation.
    - [ ] Cung cấp `@Preview` cho `Default`, `LongName`, `EmptyName` và `FontScale 1.5f`.
  - **Verify:**
    - [ ] `./gradlew assembleDebug testDebugUnitTest` pass 100%.
  - **Files:** `HomeHeader.kt`, `HomeScreen.kt`, `strings_home.xml`

### Checkpoint: After P1 (Tasks 1–2)
- [ ] All unit tests pass (`./gradlew testDebugUnitTest`)
- [ ] Application builds without errors (`./gradlew assembleDebug`)
- [ ] Review P1 with user before proceeding to P2

---

## Phase P2: Compact Living Room (`HOME-02`)
- [ ] **Task 3: Living Room Models, Smart Dialogue & 4 Interactive Objects (`Hourglass`, `Calendar`, `Wallet`, `Pause Jar`)**
  - **Acceptance:**
    - [ ] Định nghĩa `ChankoPersonality` (`Friendly`, `Sassy`, `Savage`), `SmartDialogue` (ưu tiên thông minh, chống lặp) và 4 objects (`Hourglass`, `Calendar` với dynamic day text, `Wallet`, `Pause Jar`).
    - [ ] Xây dựng `CompactLivingRoomSection.kt` gọn gàng (không hardcode chiều cao chiếm hết màn hình, để `Quick Actions` xuất hiện sớm), 4 vùng chạm độc lập $\ge 48\text{dp}$, không chồng lấn, có TalkBack semantics.
  - **Verify:**
    - [ ] `./gradlew assembleDebug testDebugUnitTest` pass.
  - **Files:** `LivingRoomModels.kt`, `CompactLivingRoomSection.kt`, `HomeUiState.kt`, `strings_home.xml`

- [ ] **Task 4: Contextual Preview Bottom Sheets cho 4 Objects & P2 Previews/Tests**
  - **Acceptance:**
    - [ ] Chạm từng object (`Hourglass`, `Calendar`, `Wallet`, `Pause Jar`) mở `CandyBottomSheet` hiển thị Contextual Preview tương ứng (`Waiting preview`, `Ready preview`, `Budget preview`, `Thoughtful Pauses preview`).
    - [ ] Unit tests kiểm tra chạm từng object mở/đóng đúng sheet và chống lặp dialogue.
  - **Verify:**
    - [ ] `./gradlew assembleDebug testDebugUnitTest` pass.
  - **Files:** `LivingRoomPreviewSheet.kt`, `HomeScreen.kt`, `HomeViewModel.kt`, `HomeViewModelTest.kt`

### Checkpoint: After P2 (Tasks 3–4)
- [ ] Review P2 with user before proceeding to P3

---

## Phase P3: Quick Actions + Global `+` (`HOME-03`, `HOME-08`)
- [ ] **Task 5: `QuickActionsSection` (`Quick Check`, `Save an Item`) & Global `+` Action Bottom Sheet**
  - **Acceptance:**
    - [ ] `QuickActionsSection.kt` hiển thị 2 direct CTAs (`Quick Check`, `Save an Item`) ngay dưới Living Room, bấm phát đúng callback điều hướng trực tiếp (không mở Global Action Bottom Sheet).
    - [ ] Nâng cấp nút `+` trong `MainNavigation.kt` mở Bottom Sheet gồm `Quick Check` và `Save an Item`; dismiss giữ nguyên tab/scroll, không tạo `ShoppingItem` khi chỉ mở sheet.
  - **Verify:**
    - [ ] `./gradlew assembleDebug testDebugUnitTest` pass.
  - **Files:** `QuickActionsSection.kt`, `MainNavigation.kt`, `HomeScreen.kt`, `strings_home.xml`, `HomeViewModelTest.kt`

### Checkpoint: After P3 (Task 5)
- [ ] Review P3 with user before proceeding to P4

---

## Phase P4: Budget Snapshot (`HOME-04`)
- [ ] **Task 6: `BudgetSnapshotSection` & Budget Threshold Logic (`Normal`, `Warning`, `Reached`, `OverBudget`)**
  - **Acceptance:**
    - [ ] Hiển thị Ngân sách tháng, Đã chi (chỉ tính `Purchased`, gồm `Pay Now` & `Credit`; loại trừ `Saved`, `Considering`, `Waiting`, `Ready`, `Local Draft`), Còn lại/Vượt, Tiến độ và Chu kỳ.
    - [ ] Hỗ trợ 4 ngưỡng (`<80%`, `80%..<100%`, `100%`, `>100%`) và cảnh báo theo chuyển trạng thái (không lặp lại chỉ vì mở lại Home).
  - **Verify:**
    - [ ] Unit tests cho mọi ngưỡng ngân sách và `SectionState` (`Loading`, `Content`, `Cached`, `Error`).
  - **Files:** `BudgetSnapshotModels.kt`, `BudgetSnapshotSection.kt`, `HomeViewModel.kt`, `HomeViewModelTest.kt`

### Checkpoint: After P4 (Task 6)
- [ ] Review P4 with user before proceeding to P5

---

## Phase P5: Your Decisions (`HOME-05`)
- [ ] **Task 7: `YourDecisionsSection` (Action-first Feed tối đa 3 items) & Priority Sorting**
  - **Acceptance:**
    - [ ] Sắp xếp đúng thứ tự ưu tiên: `Ready for Review (chờ lâu nhất) -> Waiting (gần xong Cooling-off nhất) -> Considering (gần đây) -> Saved (chỉ khi thiếu)`, lấy tối đa 3 items.
    - [ ] Hiển thị rõ trạng thái, ngữ cảnh, callback mở `Item Details` và nút `View All` (`My Items -> All`).
  - **Verify:**
    - [ ] Unit tests kiểm chứng thuật toán sắp xếp ưu tiên và giới hạn 3 items; `./gradlew assembleDebug testDebugUnitTest` pass.
  - **Files:** `DecisionFeedModels.kt`, `YourDecisionsSection.kt`, `HomeViewModel.kt`, `HomeViewModelTest.kt`

### Checkpoint: After P5 (Task 7)
- [ ] Review P5 with user before proceeding to P6

---

## Phase P6: Observation, Draft, Integration & Polish (`HOME-06`, `HOME-07`)
- [ ] **Task 8: `ObservationSection` (`HOME-06`) & `LocalDraftIndicatorSection` (`HOME-07`)**
  - **Acceptance:**
    - [ ] `ObservationSection`: tối đa 1 insight hợp lệ, hỗ trợ `Explain` (mở Explanation Sheet) và `Dismiss` (ẩn ngay theo Semantic Finding Identity, không lặp lại); ẩn hoàn toàn nếu không có.
    - [ ] `LocalDraftIndicatorSection`: phân biệt 4 trạng thái (`Local Draft`, `Pending Sync`, `Sync Failed`, `Synced / Converted`); ẩn hoàn toàn nếu không có draft.
  - **Verify:**
    - [ ] `./gradlew assembleDebug testDebugUnitTest` pass.
  - **Files:** `OptionalSectionModels.kt`, `ObservationSection.kt`, `LocalDraftIndicatorSection.kt`, `HomeViewModel.kt`, `HomeViewModelTest.kt`

- [ ] **Task 9: Multi-Scenario Previews (`NewUser`, `OfflineCache`, `PartialError`), Responsive/FontScale QA & Smoke Tests**
  - **Acceptance:**
    - [ ] Previews cho `320dp`, `393dp`, `fontScale = 1.5f`, `NewUser`, `OfflineCache`, `SectionError`.
    - [ ] Toàn bộ test suite và lint/build xanh.
  - **Verify:**
    - [ ] `./gradlew assembleDebug testDebugUnitTest` pass 100%.
  - **Files:** `HomeScreen.kt`, `HomeViewModelTest.kt`
