# Tasks: Màn hình Home (Chanko Home v5)

Nhánh: `feature/home-screen` | Theo dõi: `tasks/plan.md` | Tài liệu: `SPEC-home.md`

---

- [x] Task 1: Assets, Strings & Design Tokens Setup
  - Acceptance: 6 file PNG được đặt tại `drawable-nodpi/`. File `strings_home.xml` đầy đủ chuỗi tiếng Việt. Bổ sung token màu trong `Color.kt`.
  - Verify: Build assembleDebug succeeds.
  - Files: `app/src/main/res/drawable-nodpi/*`, `app/src/main/res/values/strings_home.xml`, `app/src/main/java/com/hoabh/chanko/ui/theme/Color.kt`

---

- [x] Task 2: Home Domain Models & Cycle Calculation Logic
  - Acceptance: `SpendingCycle`, `CalendarDayState`, `DailyPurchaseSummary`, `HomeSpendingRepository`, unit tests `SpendingCycleTest`.
  - Verify: Tests pass, build succeeds.
  - Files: `SpendingCycle.kt`, `CalendarDayState.kt`, `BudgetSummary.kt`, `HomeSpendingRepository.kt`, `RepositoryModule.kt`, `SpendingCycleTest.kt`

## Checkpoint 1: Foundation Complete
- [x] Toàn bộ asset PNG và strings sẵn sàng.
- [x] Unit tests cho logic chu kỳ chi tiêu pass 100%.
- [x] Build dự án thành công không lỗi.

---

- [x] Task 3: HomeUiState, HomeUiEvent, HomeViewModel & Unit Tests
  - Acceptance: `HomeUiState`, `HomeUiEvent`, `HomeViewModel` (kết nối `ItemRepository` + `HomeSpendingRepository`), `HomeViewModelTest`.
  - Verify: Tests pass, build succeeds.
  - Files: `HomeUiState.kt`, `HomeViewModel.kt`, `HomeViewModelTest.kt`

## Checkpoint 2: State Engine Complete
- [x] `HomeViewModel` hoạt động trơn tru với cả 2 repository.
- [x] Unit tests pass 100%, bảo đảm không có exception khi xử lý date hoặc chia tiến độ.

---

- [x] Task 4: Calendar & Budget UI Components
  - Acceptance: `HomeHeader`, `CycleCalendarGrid`, `MascotSpeechBanner`, `BudgetSummarySection`, `SpendingCalendarCard`.
  - Verify: Build assembleDebug succeeds, touch target $\ge 48\text{dp}$.
  - Files: `HomeHeader.kt`, `CycleCalendarGrid.kt`, `MascotSpeechBanner.kt`, `BudgetSummarySection.kt`, `SpendingCalendarCard.kt`, `CurrencyUtils.kt`

---

- [x] Task 5: Review & Cooling Item UI Components
  - Acceptance: `ReadyReviewSection` (cửa mint 3D, card mint, nút Xem lại $\ge 48\text{dp}$) và `CoolingOffSection` (bông tuyết 3D, card xanh nhạt, timer đếm ngược).
  - Verify: Build assembleDebug succeeds, touch target $\ge 48\text{dp}$.
  - Files: `ReadyReviewSection.kt`, `CoolingOffSection.kt`

---

## Checkpoint 3: UI Slices Complete
- [x] Toàn bộ các component UI con đã sẵn sàng và biên dịch sạch.
- [x] Mọi nút bấm và tương tác đều đạt kích thước chuẩn $\ge 48\text{dp}$.

---

- [x] Task 6: Screen Assembly (`HomeScreen`), Stateful Route & Previews
  - Acceptance: `HomeScreen.kt` (Stateless & Stateful `HomeRoute`), responsive $320\text{dp} - 393\text{dp}$, `@Preview` đầy đủ.
  - Verify: Build assembleDebug succeeds.
  - Files: `HomeScreen.kt`

---

## Task 7: Custom BottomBar Navigation & Home Route Wiring

**Description:** Tùy biến `MainNavigation` với BottomBar 4 tab có khoảng trống $64-72\text{dp}$ ở trung tâm cho nút Floating Action Button (+), nối tab Trang chủ tới `HomeRoute` và truyền callback `onAddNewItemClick`.

**Acceptance criteria:**
- [ ] `MainNavigation.kt`: Cập nhật BottomBar với 4 tab (Trang chủ, Món đồ, Ngân sách, Phân tích) bo góc 24dp, nền trắng surface.
- [ ] Nút (+) trung tâm nổi bật kích thước $56\text{dp}$, icon $28\text{dp}$, background `brandPurple` `#8965F6`, content description "Thêm món đồ", kích hoạt `onAddNewItemClick`.
- [ ] Tab `TopLevelDestination.Home` render `HomeRoute()`.

**Verification:**
- [ ] Build succeeds: `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew assembleDebug`
- [ ] Tests pass: `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew testDebugUnitTest`

**Dependencies:** Task 6
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/navigation/MainNavigation.kt`

**Estimated scope:** S (1 file)

---

## Checkpoint 4: Integration Complete
- [ ] Màn hình Home đã được nối vào navigation chính của app.
- [ ] Nút (+) và các tab điều hướng hoạt động theo đúng hợp đồng.

---

## Task 8: End-to-End Quality Gate & Regression Verification

**Description:** Chạy toàn bộ test suite của toàn bộ project, build APK debug kiểm tra biên dịch, rà soát code theo [CODING_STANDARDS.md](file:///Users/buihoa/AndroidStudioProjects/chanko/CODING_STANDARDS.md) và kiểm tra các tiêu chí nghiệm thu của [SPEC-home.md](file:///Users/buihoa/AndroidStudioProjects/chanko/SPEC-home.md).

**Acceptance criteria:**
- [ ] `./gradlew testDebugUnitTest` chạy pass 100% tất cả các test unit trong dự án.
- [ ] `./gradlew assembleDebug` biên dịch thành công.
- [ ] Không có chuỗi hardcode hay mã màu hex phân tán ngoài `strings_home.xml` và `Color.kt`.
- [ ] Không vi phạm quy tắc cấm cài đặt lên máy ảo/thiết bị và không can thiệp vào agent skills.

**Verification:**
- [ ] Tests pass: `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew testDebugUnitTest`
- [ ] Build succeeds: `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew assembleDebug`

**Dependencies:** Task 1-7
**Files likely touched:**
- Toàn bộ codebase liên quan đến Home feature.

**Estimated scope:** S (Verification & Audit)
