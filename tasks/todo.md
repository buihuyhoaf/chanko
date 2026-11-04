# Tasks: Chanko Home v2.2 (Tri-Cycle Budget Carousel & Budget Integration)

> **Plan tham chiếu:** [tasks/plan.md](plan.md) & [docs/plans/home-v2.2-budget-plan.md](../docs/plans/home-v2.2-budget-plan.md)  
> **Spec tham chiếu:** [docs/features/home-ui-v2.2-budget-spec.md](../docs/features/home-ui-v2.2-budget-spec.md)  
> **Capability Map:** [SPEC-home-ui-v2.2-budget.md](../SPEC-home-ui-v2.2-budget.md)

---

## Phase 1: Foundation (Tokens & Domain Model)

### Task 1: Hệ thống 12 Candy Gradient Themes & Neutral Tokens (`candy-budget-tokens`)

**Description:**
Khai báo 12 bộ màu Candy Gradient (Tangerine, Lavender, Mint, Strawberry, Sky Blue, Lemon, Peach, Grape, Aqua, Rose, Pistachio, Blueberry) với 3 điểm màu (Light $\to$ Main $\to$ Dark), token màu cho thẻ trung tính (Neutral Placeholder: nền trắng ngà/xám pastel, viền nét đứt, icon `+`), và token cảnh báo Coral khi vượt ngân sách (`#B83248`) trong `Color.kt`. Tạo helper `CandyBudgetThemes` để tra cứu Brush gradient theo `floorMod(ordinal, 12)`.

**Acceptance criteria:**
- [x] Khai báo đủ 12 bộ màu gradient chuẩn trong `CandyPalette` và ánh xạ qua `CandyColors`.
- [x] Tạo `CandyBudgetThemes.getGradientBrush(ordinal: Int): Brush` và token thẻ trung tính `PlaceholderTokens`.
- [x] Không vi phạm quy chuẩn quản lý màu sắc tập trung của dự án (`ColorArchitectureTest`).

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "*ColorArchitectureTest*"`
- [x] Build succeeds: `./gradlew compileDebugKotlin`

**Dependencies:** None

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/ui/theme/Color.kt`
- `app/src/main/java/com/hoabh/chanko/ui/theme/CandyBudgetThemes.kt`
- `app/src/test/java/com/hoabh/chanko/ui/theme/CandyBudgetThemesTest.kt`

**Estimated scope:** Small (2-3 files)

---

### Task 2: Domain Cycle Identity, Ordinal Math & TriCycleWindow (`budget-cycle-domain`)

**Description:**
Mở rộng domain model `BudgetCycle` bổ sung số thứ tự bất biến (`ordinal`), trạng thái chu kỳ (`BudgetCycleStatus`: `Closed`, `Active`, `UpcomingConfigured`, `UpcomingUnconfigured`). Xây dựng logic ranh giới ngày neo $\min(\text{anchorDay}, \text{daysInMonth})$ và khoảng nghiệp vụ $[\text{start}, \text{nextStart})$. Định nghĩa data class `TriCycleWindow(previous: BudgetCycle?, current: BudgetCycle, next: BudgetCycle)` để đại diện cho cửa sổ 3 kỳ.

**Acceptance criteria:**
- [x] `BudgetCycle` có thuộc tính `ordinal: Int`, `status: BudgetCycleStatus`, `themeIndex` tính theo `Math.floorMod(ordinal, 12)`.
- [x] Logic tính ngày bắt đầu/kết thúc hỗ trợ đầy đủ các tháng 28, 29 (năm nhuận), 30, 31 ngày.
- [x] `TriCycleWindow` mô hình hóa chuẩn xác: slot `previous` có thể null nếu tài khoản mới chưa có lịch sử, `current` luôn tồn tại, `next` có thể là trạng thái đã cấu hình hoặc chưa cấu hình.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.domain.budget.model.BudgetCycleTest"`

**Dependencies:** None

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/domain/budget/model/BudgetCycle.kt`
- `app/src/main/java/com/hoabh/chanko/domain/budget/model/BudgetCycleStatus.kt`
- `app/src/main/java/com/hoabh/chanko/domain/budget/model/TriCycleWindow.kt`
- `app/src/test/java/com/hoabh/chanko/domain/budget/model/BudgetCycleTest.kt`

**Estimated scope:** Medium (3-4 files)

---

### Task 3: Domain Budget Calculation & Rollover Logic (`budget-calculation`)

**Description:**
Xây dựng các quy tắc tính toán tài chính độc lập trong domain:
- Hợp nhất tổng chi Pay Now và Credit theo ngày mua thực tế (`purchaseDate`).
- Tính toán tỷ lệ phần trăm thực tế (ví dụ chi 5.5M trên 5.0M $\to$ `110%`).
- Tính clamped progress trong đoạn $[0.0f, 1.0f]$.
- Tính số tiền vượt hạn mức (`overMinor`).
- Xây dựng quy tắc chuyển chu kỳ (rollover) idempotent: khi sang kỳ mới, đóng kỳ cũ, kích hoạt kỳ tiếp theo (nếu đã cấu hình) và không chuyển số dư kỳ cũ sang kỳ mới.

**Acceptance criteria:**
- [x] Pay Now và Credit cùng tính vào tổng chi theo ngày mua.
- [x] Khi `spent > limit`: thanh tiến trình clamp ở `1.0f`, tỷ lệ phần trăm hiển thị số thật $\ge 100\%$, `overMinor = spent - limit`.
- [x] Logic rollover đảm bảo tính duy nhất của chu kỳ Active, không trùng lặp ngày, số dư không bị chuyển tiếp.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.domain.budget.model.BudgetCalculationTest"`

**Dependencies:** Task 2

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/domain/budget/model/BudgetCalculation.kt`
- `app/src/test/java/com/hoabh/chanko/domain/budget/model/BudgetCalculationTest.kt`

**Estimated scope:** Small-Medium (2-3 files)

---

## Checkpoint 1: Foundation Verification
- [x] Tất cả tests cho Token, Domain math, boundary, calculation chạy pass: `./gradlew testDebugUnitTest --tests "*BudgetCycle*" --tests "*CandyBudget*" --tests "*BudgetCalculation*"`
- [x] Biên dịch mã nguồn thành công: `./gradlew compileDebugKotlin`

---

## Phase 2: Shared Data Contract & UI State Models

### Task 4: Mở rộng `FakeBudgetRepository` dùng chung & Hợp đồng Cửa sổ Chu kỳ (`budget-data-contract`)

**Description:**
Nâng cấp `FakeBudgetRepository` để triển khai hợp đồng `BudgetRepository` mới:
- Cung cấp `observeTriCycleWindow(): Flow<TriCycleWindow>` phản ánh tức thì trạng thái của 3 chu kỳ.
- Phương thức `setupNextCycleBudget(limitMinor: Long)` lưu cấu hình hạn mức cho kỳ tiếp theo.
- Phương thức `triggerRollover()` giả lập chuyển chu kỳ.
- Hỗ trợ các kịch bản kiểm thử: tài khoản mới không có kỳ trước, tài khoản có kỳ trước, kỳ tiếp theo chưa cấu hình, kỳ tiếp theo đã cấu hình, và cờ giả lập offline (có cache / thiếu cache).
- Đảm bảo `FakeBudgetRepository` được chia sẻ dùng chung giữa Home và Budget.

**Acceptance criteria:**
- [x] `FakeBudgetRepository` phát ra `TriCycleWindow` chính xác qua `Flow`.
- [x] Gọi `setupNextCycleBudget` làm thẻ Next trong `TriCycleWindow` chuyển từ `UpcomingUnconfigured` sang `UpcomingConfigured` kèm hạn mức mới.
- [x] Kiểm thử kịch bản offline và snapshot dữ liệu nguyên tử theo `cycleId`.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.data.budget.FakeBudgetRepositoryTest"`

**Dependencies:** Task 2, Task 3

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/domain/budget/repository/BudgetRepository.kt`
- `app/src/main/java/com/hoabh/chanko/data/budget/FakeBudgetRepository.kt`
- `app/src/test/java/com/hoabh/chanko/data/budget/FakeBudgetRepositoryTest.kt`

**Estimated scope:** Medium (3-4 files)

---

### Task 5: Thiết kế `BudgetCarouselUi` & `BudgetCardSlotUi` State Models (`budget-ui-models`)

**Description:**
Cập nhật `HomeUiState.kt` để thay thế payload thẻ đơn bằng `BudgetCarouselUi`:
- `BudgetCarouselUi` chứa `slots: List<BudgetCardSlotUi>`, `selectedSlotKey: String`, `currentCycleKey: String`, và `lastSyncedAtText: String?`.
- Các biến thể `BudgetCardSlotUi`:
  + `ClosedCard` (Previous: chỉ xem, badge "ĐÃ KẾT THÚC", theme gradient gốc).
  + `ActiveCard` (Current: badge "ĐANG DIỄN RA", spent/limit/remaining/over, progress clamped, real percent, theme gradient hiện tại).
  + `UpcomingConfiguredCard` (Next: badge "SẮP TỚI", limit đã cấu hình, theme gradient kỳ sau).
  + `UpcomingPlaceholderCard` (Next neutral: viền nét đứt, icon `+`, CTA "Thiết lập ngân sách").
  + `CurrentEmptyCard` (Current chưa thiết lập).
- Viết mapper từ `TriCycleWindow` sang `BudgetCarouselUi`.

**Acceptance criteria:**
- [x] `BudgetCarouselUi` mô hình hóa đầy đủ 4 loại thẻ và các trạng thái ngoại vi.
- [x] Mapper chuyển đổi chính xác từ domain `TriCycleWindow` sang UI state.
- [x] Unit test mapper bao phủ các trường hợp đủ 3 kỳ, thiếu kỳ trước, vượt ngân sách.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.home.*MapperTest*"` (hoặc `HomeDomainLogicTest`)

**Dependencies:** Task 1, Task 4

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiState.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/domain/HomeDomainLogic.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeDomainLogicTest.kt`

**Estimated scope:** Small-Medium (2-3 files)

---

## Checkpoint 2: Contract Verification
- [x] Hợp đồng fake repository và state mapper chạy xanh: `./gradlew testDebugUnitTest --tests "*FakeBudgetRepositoryTest*" --tests "*HomeDomainLogicTest*"`
- [x] Build thành công: `./gradlew compileDebugKotlin`

---

## Phase 3: Carousel UI Components (`budget-carousel-ui`)

### Task 6: Xây dựng Composable các loại Thẻ Ngân sách (`BudgetCreditCard` v2.2 & `BudgetPlaceholderCard`)

**Description:**
- Nâng cấp `BudgetCreditCard.kt`:
  + Nhận `themeOrdinal` và áp dụng linear gradient từ `CandyBudgetThemes` (Sáng $\to$ Chính $\to$ Đậm).
  + Hiển thị Badge trạng thái chu kỳ (`ĐÃ KẾT THÚC`, `ĐANG DIỄN RA`, `SẮP TỚI`).
  + Thanh tiến trình clamped $[0f, 1f]$, nhãn hiển thị % thật (ví dụ `110%`).
  + Khi vượt ngân sách (`spent > limit`): hiển thị số tiền vượt bằng màu Coral (`#B83248`) trên nền gradient của kỳ.
  + Giữ nguyên các chi tiết trang trí: chip EMV vàng, sóng contactless, logo mèo (tất cả có `contentDescription = null`).
- Tạo mới `BudgetPlaceholderCard.kt`:
  + Nền trắng ngà/xám pastel (`#F7F5FA` / `#EFEBFA`).
  + Viền nét đứt (`dash = 6.dp, gap = 4.dp`), icon `+` trung tính.
  + Tiêu đề "Chu kỳ tiếp theo" và nút CTA "+ Thiết lập ngân sách".
- Đảm bảo TalkBack semantics đầy đủ và kích thước touch target $\ge 48\text{dp}$.

**Acceptance criteria:**
- [x] Render chính xác cả 4 loại thẻ với đầy đủ thông tin theo từng trạng thái.
- [x] Nền thẻ giữ nguyên gradient của chu kỳ khi vượt ngân sách; chỉ nhãn cảnh báo dùng Coral.
- [x] Thẻ Placeholder có viền nét đứt và icon `+` trung tính rõ nét.
- [x] TalkBack đọc đúng vai trò thẻ, khoảng ngày, limit $\to$ spent $\to$ còn/vượt $\to$ tỷ lệ.

**Verification:**
- [x] Previews compile & render sạch trong Android Studio.
- [x] Build succeeds: `./gradlew compileDebugKotlin`

**Dependencies:** Task 1, Task 5

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetCreditCard.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetPlaceholderCard.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetCycleBadge.kt`
- `app/src/main/res/values/strings.xml`

**Estimated scope:** Medium (3-4 files)

---

### Task 7: Xây dựng Composable `BudgetCreditCardCarousel` với `HorizontalPager`

**Description:**
Xây dựng component `BudgetCreditCardCarousel.kt`:
- Sử dụng Compose `HorizontalPager` với `pageCount = slots.size` (1..3).
- Cấu hình snap tối đa 1 trang mỗi gesture fling.
- Cấu hình edge peek (16-24dp) để hé lộ nhẹ mép thẻ trước/sau.
- Không xung đột cử chỉ với `LazyColumn` cuộn dọc của màn Home.
- Render tương ứng theo `SectionState`: Loading skeleton, Content (Carousel), Empty current, Error cục bộ + retry, Offline thiếu cache.
- Tự động đồng bộ vị trí pager với `selectedSlotKey`.

**Acceptance criteria:**
- [x] Pager hiển thị tối đa 3 thẻ, vuốt mượt mà tối đa 1 trang mỗi lần.
- [x] Nếu không có kỳ trước, chỉ có 2 trang `[Current, Next]`.
- [x] Kéo dọc màn hình Home không bị pager cướp cử chỉ; vuốt ngang không kích hoạt click thẻ.
- [x] Xử lý đầy đủ các trạng thái ngoài Content (Skeleton, Error, Offline).

**Verification:**
- [x] Build succeeds: `./gradlew compileDebugKotlin`
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "*BudgetCreditCardCarouselTest*"` (hoặc Compose test)

**Dependencies:** Task 6

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetCreditCardCarousel.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetCreditCardSkeleton.kt`

**Estimated scope:** Medium (2-3 files)

---

## Checkpoint 3: UI Component Verification
- [x] Tất cả component thẻ và carousel biên dịch sạch không lỗi: `./gradlew assembleDebug`
- [x] Các Preview hoạt động tốt ở các cấu hình màn hình khác nhau.

---

## Phase 4: Integration, Navigation & Verification (`budget-home-integration`)

### Task 8: Tích hợp Carousel & Header "Xem tất cả" vào `HomeScreen` & `HomeViewModel`

**Description:**
- Trong `HomeScreen.kt`:
  + Bổ sung Section Header cho Budget: Tiêu đề "Ngân sách mua sắm" nằm ngang hàng với nút text "Xem tất cả" (bên ngoài pager).
  + Thay thế slot thẻ đơn `BudgetCreditCard` bằng `BudgetCreditCardCarousel`.
- Trong `HomeViewModel.kt`:
  + Quan sát `observeTriCycleWindow()` từ `BudgetRepository`.
  + Xử lý sự kiện `HomeEntered`: Tự động cuộn pager về thẻ `Current`.
  + Xử lý sự kiện `BudgetSeeAllTapped`: Phát effect mở màn hình lịch sử ngân sách.
  + Xử lý sự kiện `CardActivated(slotKey)`: Phân loại theo loại thẻ để điều hướng chính xác.
  + Kiểm tra trạng thái ngoại tuyến: Nếu mất mạng và bấm vào Placeholder để thiết lập kỳ sau $\to$ hiển thị snackbar thông báo cần kết nối mạng và chặn mở màn setup.

**Acceptance criteria:**
- [x] Nút "Xem tất cả" luôn hiển thị ở header section và bấm được ở mọi trang carousel.
- [x] Vào hoặc quay lại Home, carousel luôn snap về thẻ `Current`.
- [x] Chạm thẻ phát đúng intent điều hướng; chạm placeholder khi offline bị chặn và hiện thông báo tiếng Việt.
- [x] Lỗi ở khu vực Budget không làm ảnh hưởng đến Header hay Item Journey.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.home.HomeViewModelTest"`

**Dependencies:** Task 5, Task 7

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeViewModel.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiEvent.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiEffect.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeViewModelTest.kt`

**Estimated scope:** Medium (4-5 files)

---

### Task 9: Mở rộng Điều hướng `BudgetRoute` & Tích hợp Luồng Thiết lập Ngân sách Kỳ sau

**Description:**
- Cập nhật `BudgetRoute.kt` và `NavGraph.kt`:
  + Tiếp nhận đối số `cycleId: String?` và `mode: BudgetScreenMode` (`Active`, `ReadOnly`, `SetupNext`).
  + Với thẻ `Previous`: Mở `BudgetRoute` ở chế độ chỉ đọc (`ReadOnly`).
  + Với thẻ `Current`: Mở `BudgetRoute` ở chế độ bình thường (`Active`).
  + Với thẻ `Next Placeholder`: Mở luồng thiết lập ngân sách kỳ sau (`SetupNext`).
  + Nút "Xem tất cả": Mở `BudgetRoute` hiển thị đầy đủ danh sách lịch sử chu kỳ.
- Đảm bảo `BudgetViewModel` sử dụng cùng instance `FakeBudgetRepository` với `HomeViewModel` (thông qua Hilt Singleton). Khi người dùng lưu hạn mức kỳ sau thành công trên màn Budget/Setup $\to$ quay lại Home, thẻ Next lập tức cập nhật sang trạng thái `UpcomingConfigured` với theme màu thật.

**Acceptance criteria:**
- [x] Điều hướng từ Home sang Budget truyền đúng `cycleId` và `mode`.
- [x] Thiết lập kỳ sau từ Home $\to$ Budget $\to$ Quay lại Home: Dữ liệu đồng bộ tức thì, thẻ Next đổi trạng thái và màu sắc.
- [x] Thẻ Previous mở ở chế độ chỉ đọc, không cho sửa hạn mức kỳ đã đóng.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.budget.BudgetViewModelTest"`

**Dependencies:** Task 4, Task 8

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/BudgetRoute.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/budget/BudgetViewModel.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/navigation/NavGraph.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/budget/BudgetViewModelTest.kt`

**Estimated scope:** Medium (3-4 files)

---

### Task 10: Kiểm thử Xuyên Suốt (End-to-End Verification) & Kiểm tra Hồi quy (BC22-01..BC22-24)

**Description:**
Viết bộ kiểm thử xuyên suốt luồng người dùng (E2E Integration Test) và lập báo cáo kiểm chứng toàn diện cho 24 tiêu chí nghiệm thu `BC22-01` $\to$ `BC22-24`:
- Luồng 1: Mở Home $\to$ thấy Current ở giữa $\to$ vuốt Previous (xem kỳ cũ) $\to$ vuốt Next (thấy placeholder nét đứt).
- Luồng 2: Bấm Setup Next $\to$ nhập hạn mức $\to$ bấm Lưu $\to$ quay lại Home $\to$ thấy thẻ Next đổi sang theme màu của kỳ sau và hiển thị hạn mức.
- Luồng 3: Kích hoạt rollover $\to$ thẻ Next trở thành thẻ Current mới, giữ nguyên màu theme $\to$ thẻ Current cũ trở thành Previous.
- Luồng 4: Bấm "Xem tất cả" mở danh sách đầy đủ lịch sử.
- Luồng 5: Giả lập mất mạng $\to$ xem cache bình thường $\to$ bấm thiết lập kỳ sau bị chặn kèm thông báo tiếng Việt.
- Chạy toàn bộ test suite và build debug.

**Acceptance criteria:**
- [x] 100% các ca kiểm thử trong `HomeBudgetE2EFlowTest` chạy pass.
- [x] Toàn bộ 24 tiêu chí `BC22-01`..`BC22-24` được xác thực đạt.
- [x] Tạo báo cáo xác minh tại `docs/tasks/home-v2.2-budget-verification-report.md`.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest`
- [x] Build succeeds: `./gradlew assembleDebug`

**Dependencies:** Task 8, Task 9

**Files likely touched:**
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeBudgetE2EFlowTest.kt`
- `docs/tasks/home-v2.2-budget-verification-report.md`

**Estimated scope:** Medium (2-3 files)

---

## Checkpoint 4: Complete Integration & Final Sign-off
- [x] Toàn bộ unit tests trong toàn bộ project pass: `./gradlew testDebugUnitTest`
- [x] Biên dịch Debug APK thành công: `./gradlew assembleDebug`
- [x] 24/24 tiêu chí nghiệm thu hoàn thành.
