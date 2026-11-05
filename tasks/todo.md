# Tasks: Chanko Home v2.2 (Tri-Cycle Budget Carousel & Budget Integration)

> **Plan tham chiếu:** [tasks/plan.md](plan.md)  
> **Spec tham chiếu:** [docs/features/home-ui-v2.2-budget-spec.md](../docs/features/home-ui-v2.2-budget-spec.md)  
> **Capability Map:** [SPEC-home-ui-v2.2-budget.md](../SPEC-home-ui-v2.2-budget.md)  
> **Handoff nguồn:** `Chanko-Home-v2.2 2/CHANKO-HOME-v2.2-HANDOFF.md` & `tokens/design-tokens.json`

---

## Phase 1: Foundation (Tokens & Domain Model)

### Task 1: Cập nhật 12 Candy Gradient Themes & Semantic Tokens theo `design-tokens.json` (`candy-budget-tokens`)

**Description:**
Đồng bộ 12 bộ màu Candy Gradient theo chuẩn mới nhất từ `design-tokens.json` (`01` Tangerine, `02` Lavender, `03` Mint, `04` Strawberry, `05` Sky, `06` Lemon, `07` Peach, `08` Grape, `09` Aqua, `10` Rose, `11` Pistachio, `12` Blueberry) với 3 điểm màu (`0.0`, `0.5`, `1.0`), các token layout (`cardMinHeightDp = 216`, `cardRadiusDp = 28`, `contentPaddingDp = 20`, `amountSp = 32`, `monthSp = 24`, `peekDp = 16`), và semantic tokens (`text = #241332`, `badgeFill = #FFF8F2`, `warning = #9C2943`, `neutralBorder = #82758E`, `ctaFill = #6326A8`, `ctaText = #FFFFFF`). Cập nhật helper `CandyBudgetThemes` để tra cứu theo công thức $\text{themeId} = \text{floorMod}(\text{ordinal}, 12) + 1$.

**Acceptance criteria:**
- [x] Khai báo đủ 12 bộ gradient trong `Color.kt` / `CandyColors.CandyGradientTheme` khớp chính xác hex codes của `design-tokens.json`.
- [x] `CandyBudgetThemes.getGradientBrush(ordinal: Int)` áp dụng công thức `floorMod(ordinal, 12) + 1` và nội suy sRGB theo vector `[0, 0] -> [1, 1]` qua 3 điểm `0.0f, 0.5f, 1.0f`.
- [x] Bổ sung các tokens semantic và layout: `cardMinHeight = 216.dp`, `cardRadius = 28.dp`, `warningCoral = Color(0xFF9C2943)`, `ctaPurple = Color(0xFF6326A8)`, `neutralBorder = Color(0xFF82758E)`.
- [x] Tuân thủ `ColorArchitectureTest`.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "*ColorArchitectureTest*" --tests "*CandyBudget*"`
- [x] Build succeeds: `./gradlew compileDebugKotlin`

**Dependencies:** None

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/ui/theme/Color.kt`
- `app/src/main/java/com/hoabh/chanko/ui/theme/CandyBudgetThemes.kt`
- `app/src/test/java/com/hoabh/chanko/ui/theme/CandyBudgetThemesTest.kt`

**Estimated scope:** Small (2-3 files)

---

### Task 2: Bổ sung `labelMonth` & Ánh xạ Theme Formula vào Domain Cycle (`budget-cycle-domain`)

**Description:**
Mở rộng model `BudgetCycle` trong domain để bổ sung thuộc tính `labelMonth: String` (chuỗi 2 chữ số `01`..`12` theo `BC-CARD-LABEL-01`) từ ngày bắt đầu hoặc do domain cung cấp. Cập nhật `themeId` tính theo `floorMod(ordinal, 12) + 1` (trả về giá trị `1`..`12`). Đảm bảo `TriCycleWindow` chứa `labelMonth` cho cả 3 vị trí `[Previous?, Current, Next]`.

**Acceptance criteria:**
- [x] `BudgetCycle` có `val labelMonth: String` định dạng 2 chữ số (`01`..`12`).
- [x] `themeId` của cycle tính theo công thức `floorMod(ordinal, 12) + 1`.
- [x] Next placeholder (khi chưa cấu hình) vẫn tính được `targetLabelMonth` của kỳ kế tiếp dựa trên `current.endDateExclusive`.
- [x] Unit tests kiểm tra chu kỳ tháng 1 đến 12, năm nhuận và chuyển năm (tháng 12 $\to$ tháng 01).

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.domain.budget.model.BudgetCycleTest"`

**Dependencies:** None

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/domain/budget/model/BudgetCycle.kt`
- `app/src/main/java/com/hoabh/chanko/domain/budget/model/TriCycleWindow.kt`
- `app/src/test/java/com/hoabh/chanko/domain/budget/model/BudgetCycleTest.kt`

**Estimated scope:** Small (2-3 files)

---

## Checkpoint 1: Foundation Verification
- [x] Toàn bộ tests cho Token và Domain Cycle chạy pass: `./gradlew testDebugUnitTest --tests "*BudgetCycle*" --tests "*CandyBudget*"`
- [x] Biên dịch mã nguồn Kotlin thành công: `./gradlew compileDebugKotlin`

---

## Phase 2: Shared Data Contract & UI State Models

### Task 3: Cập nhật `BudgetCardSlotUi` State Models theo `BC-CARD-LABEL-01` & `BC-CARD-CONTENT-01` (`budget-ui-models`)

**Description:**
Cập nhật các data class con của `BudgetCardSlotUi` trong `HomeUiState.kt` để phản ánh đúng hai quyết định thẩm quyền mới:
- Thêm `monthLabel: String` (ví dụ `"09"`, `"10"`, `"11"`) cho Month Chip TopBar.
- Loại bỏ dòng `"Đã chi X / Y"` (`spentSummaryText`).
- Thẻ `ClosedCard` (Previous): `mainLabel = "Còn lại cuối kỳ"`, chứa số dư còn lại cuối kỳ + tiến độ + % chi thực tế.
- Thẻ `ActiveCard` (Current): `mainLabel = "Ngân sách còn lại"` (hoặc `"Vượt ngân sách"` khi vượt hạn mức), chứa số dư còn lại (hoặc số vượt), clamped progress, % chi thực tế.
- Thẻ `UpcomingConfiguredCard` (Next đã cấu hình): `mainLabel = "Ngân sách dự kiến"`, chỉ chứa hạn mức kế hoạch, không có chi tiêu hay thanh progress.
- Thẻ `UpcomingPlaceholderCard` (Next chưa cấu hình): chứa `monthLabel` của kỳ sau, tiêu đề `"Chu kỳ tiếp theo"`, phụ đề `"Chưa thiết lập ngân sách"`.

**Acceptance criteria:**
- [x] `BudgetCardSlotUi` có `monthLabel: String` và không chứa trường `spentSummaryText`.
- [x] Các nhãn chính hiển thị đúng vai trò theo quyết định `BC-CARD-CONTENT-01`.
- [x] Cập nhật mapper trong `HomeDomainLogic` hoặc tương đương để chuyển đổi từ `TriCycleWindow` sang các model mới.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.home.*"`

**Dependencies:** Task 1, Task 2

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiState.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/domain/HomeDomainLogic.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeDomainLogicTest.kt`

**Estimated scope:** Medium (3-4 files)

---

### Task 4: Cập nhật `FakeBudgetRepository` dùng chung & Hợp đồng Cửa sổ Chu kỳ (`budget-data-contract`)

**Description:**
Đồng bộ `FakeBudgetRepository` với các thuộc tính mới (`labelMonth`, `ordinal`, theme calculation 1..12). Đảm bảo `observeTriCycleWindow()` phát ra `TriCycleWindow` đầy đủ thông tin cho Previous (nếu có), Current và Next. Kiểm tra luồng `setupNextCycleBudget` kích hoạt cập nhật StateFlow tức thì cho các quan sát viên (cả Home và Budget).

**Acceptance criteria:**
- [x] `FakeBudgetRepository` phát ra cửa sổ chu kỳ có `labelMonth` chính xác cho cả 3 slot.
- [x] Khi gọi `setupNextCycleBudget`, slot Next chuyển sang trạng thái đã cấu hình với theme ordinal chuẩn.
- [x] Hỗ trợ giả lập trạng thái ngoại tuyến (`isOffline = true`) có cache và thiếu cache.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.data.budget.FakeBudgetRepositoryTest"`

**Dependencies:** Task 2, Task 3

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/domain/budget/repository/BudgetRepository.kt`
- `app/src/main/java/com/hoabh/chanko/data/budget/FakeBudgetRepository.kt`
- `app/src/test/java/com/hoabh/chanko/data/budget/FakeBudgetRepositoryTest.kt`

**Estimated scope:** Small-Medium (2-3 files)

---

## Checkpoint 2: Contract Verification
- [x] Kiểm thử Repository và State mapping chạy pass: `./gradlew testDebugUnitTest --tests "*FakeBudgetRepositoryTest*" --tests "*HomeDomainLogicTest*"`
- [x] Biên dịch mã nguồn Kotlin thành công: `./gradlew compileDebugKotlin`

---

## Phase 3: Carousel UI Components (`budget-carousel-ui`)

### Task 5: Tái cấu trúc Composable Mặt Thẻ `BudgetCreditCard` v2.2 (`BC-CARD-LABEL-01` & `BC-CARD-CONTENT-01`)

**Description:**
Tái cấu trúc giao diện `BudgetCreditCard.kt`:
- **TopBar:** Góc trái giữ `CHANKO` / `SHOPPING BUDGET` + biểu tượng contactless/chip trang trí. Góc phải hiển thị **Month Chip pill** hiển thị 2 chữ số tháng `MM` (`01`..`12`) (nền kem `#FFF8F2`, chữ `#241332`, font 24sp). **Loại bỏ hoàn toàn** năm, khoảng ngày và text badge trạng thái (`ĐANG DIỄN RA`, `ĐÃ KẾT THÚC`, `SẮP TỚI`).
- **Nội dung chính:** **Loại bỏ hoàn toàn** dòng `"Đã chi X / Y"`.
  + Thẻ Previous: Nhãn `"Còn lại cuối kỳ"` + số dư còn lại cuối kỳ + tiến độ + nhãn % chi thực tế. Mascot: `R.drawable.mascot_sleepy`.
  + Thẻ Current: Nhãn `"Ngân sách còn lại"` (hoặc `"Vượt ngân sách"` với số tiền vượt, % thật > 100%, progress clamp ở 100%, text cảnh báo màu Coral `#9C2943`). Mascot: `R.drawable.mascot_happy` (hoặc `mascot_worried` khi vượt mức).
  + Thẻ Next đã cấu hình: Nhãn `"Ngân sách dự kiến"` + hạn mức kế hoạch. Không có spent hay thanh progress.
- **Visual & Layout:** Chiều cao thẻ tối thiểu `216.dp`, bo góc `28.dp`, padding `20.dp`. Linear gradient sRGB nội suy qua 3 điểm `[0.0, 0.5, 1.0]`. Text màu `#241332` (hoặc màu tương phản cao đạt WCAG AA $\ge 4.5:1$).

**Acceptance criteria:**
- [x] TopBar có Month Chip pill `MM`; không còn text badge trạng thái hay khoảng ngày/năm.
- [x] Bỏ hoàn toàn dòng `"Đã chi X / Y"` trên tất cả các thẻ.
- [x] Hiển thị chính xác các nhãn theo role: `"Còn lại cuối kỳ"`, `"Ngân sách còn lại"`, `"Ngân sách dự kiến"`.
- [x] Thẻ Next không có chi tiêu hay thanh tiến trình giả định.
- [x] Thẻ Previous dùng mascot sleepy; Current dùng happy hoặc worried.

**Verification:**
- [x] Previews render chính xác các biến thể thẻ.
- [x] Build biên dịch sạch: `./gradlew compileDebugKotlin`

**Dependencies:** Task 1, Task 3

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetCreditCard.kt`
- `app/src/main/res/values/strings.xml`

**Estimated scope:** Medium (2-3 files)

---

### Task 6: Tái cấu trúc Composable Thẻ Placeholder `BudgetPlaceholderCard` v2.2

**Description:**
Tái cấu trúc `BudgetPlaceholderCard.kt`:
- **TopBar:** Góc phải có Month Chip pill `MM` của chu kỳ tiếp theo (nền kem `#FFF8F2`, chữ `#241332`). Góc trái giữ thương hiệu trang trí.
- **Nội dung:** Tiêu đề `"Chu kỳ tiếp theo"`, phụ đề `"Chưa thiết lập ngân sách"`.
- **Nút CTA:** Nút dạng pill `+ Thiết lập ngân sách` (nền tím `#6326A8`, chữ trắng `#FFFFFF`, chiều cao tối thiểu touch target 48dp).
- **Khung thẻ:** Nền trắng ngà/xám pastel (`#FBF9FD` / `#F3EFF7`), viền nét đứt native (`dash = 6.dp, gap = 4.dp`, màu `#82758E`, bo góc 28dp). Không dùng gradient màu chu kỳ.

**Acceptance criteria:**
- [x] Thẻ Placeholder hiển thị đúng Month Chip `MM` của kỳ kế tiếp ở TopBar.
- [x] Tiêu đề `"Chu kỳ tiếp theo"`, phụ đề `"Chưa thiết lập ngân sách"`.
- [x] Nút CTA dạng pill `+ Thiết lập ngân sách` với nền tím `#6326A8`, chữ trắng.
- [x] Viền nét đứt native Compose rõ nét, nền xám pastel.

**Verification:**
- [x] Previews render chính xác thẻ Placeholder.
- [x] Build biên dịch sạch: `./gradlew compileDebugKotlin`

**Dependencies:** Task 1, Task 3

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetPlaceholderCard.kt`
- `app/src/main/res/values/strings.xml`

**Estimated scope:** Small-Medium (2 files)

---

### Task 7: Xây dựng Thanh Điều khiển Pager & Nâng cấp `BudgetCreditCardCarousel`

**Description:**
Nâng cấp `BudgetCreditCardCarousel.kt`:
- `HorizontalPager` hiển thị 1..3 slots (`[Previous? - Current - Next]`). Chiều cao tối thiểu `216.dp`, peek hai bên `16.dp`, khoảng cách giữa các thẻ `16.dp`.
- Fling behavior giới hạn snap tối đa 1 trang mỗi lần vuốt.
- **Thanh Điều khiển Pager (Bottom Pager Bar):** Bố trí bên dưới Carousel gồm:
  + Nút mũi tên trái `<` (disabled khi ở trang đầu tiên).
  + Dải chấm chỉ báo trang (indicator dots, chấm active màu tím `#6326A8`, chấm inactive màu xám nhẹ).
  + Nút mũi tên phải `>` (disabled khi ở trang cuối cùng).
  + Liên kết `"Xem tất cả →"` (mở lịch sử ngân sách đầy đủ trên `BudgetScreen`).
- Cấu hình TalkBack accessibility: đọc đầy đủ thứ tự trang (ví dụ "Trang 2 trên 3"), đọc vai trò và nội dung thẻ.

**Acceptance criteria:**
- [x] Pager cuộn tối đa 1 trang mỗi lần vuốt; tự động snap về `Current` khi vào hoặc quay lại Home.
- [x] Thanh điều khiển bên dưới hiển thị nút `<`, dải chấm chỉ báo, nút `>`, và `"Xem tất cả →"`.
- [x] Bấm nút `<` / `>` kích hoạt cuộn sang trang trước/sau mượt mà.
- [x] Nút biên bị disabled khi ở trang đầu/cuối.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "*BudgetCreditCardCarouselTest*"`
- [x] Build biên dịch sạch: `./gradlew compileDebugKotlin`

**Dependencies:** Task 5, Task 6

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetCreditCardCarousel.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/components/BudgetCreditCardCarouselTest.kt`

**Estimated scope:** Medium (3-4 files)

---

## Checkpoint 3: UI Component Verification
- [x] Tất cả component thẻ và carousel biên dịch sạch không lỗi: `./gradlew assembleDebug`
- [x] Các Compose Previews render hoàn hảo trên môi trường debug.

---

## Phase 4: Integration, Navigation & Verification (`budget-home-integration`)

### Task 8: Tích hợp Carousel & Thanh Điều khiển Pager vào `HomeScreen` & `HomeViewModel`

**Description:**
- Trong `HomeScreen.kt`:
  + Tích hợp `BudgetCreditCardCarousel` vào section Budget.
  + Chuyển tiếp các callback điều hướng: chạm thẻ, bấm nút `<` / `>`, bấm `"Xem tất cả →"`.
  + Đảm bảo không xung đột cử chỉ với cuộn dọc của màn Home (`LazyColumn`).
- Trong `HomeViewModel.kt`:
  + Quan sát `observeTriCycleWindow()` từ repository.
  + Xử lý sự kiện `HomeEntered`: Tự động snap pager về thẻ `Current`.
  + Xử lý sự kiện `BudgetSeeAllTapped`: Phát effect mở lịch sử ngân sách (`BudgetRoute(mode = History)`).
  + Xử lý sự kiện chạm thẻ `CardActivated(slotKey)`: Điều hướng đúng theo loại thẻ (`ReadOnly`, `Active`, `SetupNext`).
  + Kiểm tra trạng thái ngoại tuyến: Nếu mất mạng và bấm thiết lập kỳ sau $\to$ hiển thị snackbar tiếng Việt thông báo cần kết nối mạng và chặn mở màn setup.

**Acceptance criteria:**
- [ ] Mở hoặc quay lại Home luôn snap về thẻ `Current`.
- [ ] Bấm `"Xem tất cả →"` hoặc chạm các thẻ phát đúng effect điều hướng.
- [ ] Chạm nút thiết lập kỳ sau khi mất mạng bị chặn và hiển thị snackbar thông báo.
- [ ] Lỗi ở khu vực Budget (nếu có) được cô lập hoàn toàn, không ảnh hưởng đến Header hay Item Journey.

**Verification:**
- [ ] Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.home.HomeViewModelTest"`

**Dependencies:** Task 4, Task 7

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
- Trong `BudgetRoute.kt` và `NavGraph.kt`:
  + Tiếp nhận đối số `cycleId: String?` và `mode: BudgetScreenMode` (`ReadOnly`, `Active`, `ViewOrEditPlan`, `SetupNext`, `History`).
  + Thẻ Previous mở màn hình ở chế độ chỉ đọc `ReadOnly` (không cho phép sửa đổi hạn mức).
  + Thẻ Current mở ở chế độ `Active`.
  + Thẻ Placeholder mở chế độ `SetupNext`.
  + Liên kết `"Xem tất cả →"` mở danh sách lịch sử chu kỳ `History`.
- Đảm bảo `BudgetViewModel` và `HomeViewModel` cùng inject một instance `FakeBudgetRepository` singleton. Sau khi người dùng lưu hạn mức kỳ sau thành công trên màn Setup $\to$ quay lại Home, thẻ Next lập tức đổi sang trạng thái `UpcomingConfigured` với theme màu thật.

**Acceptance criteria:**
- [ ] `BudgetRoute` nhận và xử lý đầy đủ các mode điều hướng.
- [ ] Dữ liệu đồng bộ tức thì hai chiều giữa Home và Budget.
- [ ] Thẻ Previous mở ở chế độ chỉ đọc, bảo toàn tính bất biến của lịch sử.

**Verification:**
- [ ] Tests pass: `./gradlew testDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.budget.BudgetViewModelTest"`

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
Viết bộ kiểm thử xuyên suốt luồng người dùng (E2E Integration Test) và xác thực toàn bộ 24 tiêu chí nghiệm thu `BC22-01` $\to$ `BC22-24`:
- Xác nhận các quyết định `BC-CARD-LABEL-01` (Month Chip `MM`, không có năm/ngày/text badge) và `BC-CARD-CONTENT-01` (bỏ `"Đã chi X / Y"`, nhãn role chuẩn hóa).
- Kiểm tra luồng: Thiết lập hạn mức kỳ sau $\to$ quay lại Home thấy thẻ Next đổi theme màu $\to$ kích hoạt rollover thấy Next thành Current mới, giữ nguyên theme $\to$ Current cũ thành Previous với nhãn `"Còn lại cuối kỳ"`.
- Xác thực TalkBack accessibility, touch target $\ge 48\text{dp}$, cô lập lỗi và chặn ghi ngoại tuyến.
- Tạo báo cáo nghiệm thu chi tiết tại `docs/tasks/home-v2.2-budget-verification-report.md`.

**Acceptance criteria:**
- [ ] 100% các ca kiểm thử trong `HomeBudgetE2EFlowTest` chạy pass.
- [ ] Toàn bộ 24 tiêu chí `BC22-01`..`BC22-24` đạt yêu cầu.
- [ ] Báo cáo nghiệm thu hoàn chỉnh tại `docs/tasks/home-v2.2-budget-verification-report.md`.

**Verification:**
- [ ] Tests pass: `./gradlew testDebugUnitTest`
- [ ] Build succeeds: `./gradlew assembleDebug`

**Dependencies:** Task 8, Task 9

**Files likely touched:**
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeBudgetE2EFlowTest.kt`
- `docs/tasks/home-v2.2-budget-verification-report.md`

**Estimated scope:** Medium (2-3 files)

---

## Checkpoint 4: Complete Integration & Final Sign-off
- [ ] Toàn bộ unit tests trong toàn bộ ứng dụng pass: `./gradlew testDebugUnitTest`
- [ ] Biên dịch Debug APK thành công: `./gradlew assembleDebug`
- [ ] 24/24 tiêu chí nghiệm thu hoàn thành.
