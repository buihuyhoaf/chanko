# Tasks: Chanko Home UI v1.0 (Candy Pop 3D)

> **Plan tham chiếu:** [tasks/plan.md](plan.md) & [docs/plans/home-ui-v1-plan.md](../docs/plans/home-ui-v1-plan.md)  
> **Spec tham chiếu:** [SPEC-home-ui-v1.md](../SPEC-home-ui-v1.md) & [docs/features/home-ui-v1-spec.md](../docs/features/home-ui-v1-spec.md)

---

## Phase 1: Foundation & Domain Reconciliation (`home-domain-reconcile`)

### Task 1: Nâng cấp Domain Logic, State Model & Reconcile Cooling-off

**Description:** Cập nhật `HomeDomainLogic.kt`, `HomeUiState.kt`, `HomeUiEvent.kt` và `HomeUiEffect.kt` để tuân thủ hợp đồng Frozen v1.0: tự động chuyển món hết hạn `Cooling-off` (`remainingSeconds <= 0`) từ nhóm *Đang chờ* sang nhóm *Cần xem lại* mà không tự đổi verdict thành `BUY` (`AC-07`), chống đếm trùng `itemId` giữa hai nhóm (`AC-08`), chọn tối đa 3 món ưu tiên cần hành động (`AC-09`), hiển thị `"Chưa có giá"` khi `priceMinor == null` (`HOME-EDGE-04`), loại bỏ events/states của Quick Actions & Global Sheet (`AC-02`), và bổ sung model `HomeMyItemsFilter` cùng `ContextKind.Observation`.

**Acceptance criteria:**
- [x] Món có `statusRaw = "Waiting"` nhưng `remainingSeconds <= 0` được phân vào nhóm `Review` (*Cần xem lại*) với nhãn `"Vừa hết thời gian chờ"`, giữ nguyên `verdict != BUY` (`AC-07`).
- [x] Cùng một `itemId` không bao giờ xuất hiện đồng thời ở cả nhóm `Waiting` và `Review`; tổng số lượng badge của Little Room khớp chính xác với dữ liệu đã reconcile (`AC-08`).
- [x] `RawDecisionCandidate` hỗ trợ `priceMinor: Long?`; khi `priceMinor == null` trả về `priceFormatted = "Chưa có giá"`, khi `priceMinor == 0L` trả về `"0 ₫"` (`HOME-EDGE-04`).
- [x] Xóa `HomeSheet.Global`, `QuickCheckTapped`, `SaveItemTapped` khỏi `HomeUiState` / `HomeUiEvent` (`AC-02`); thêm `ContextKind.Observation` và `HomeMyItemsFilter` (`WAITING`, `REVIEW`, `SKIPPED`, `ALL`) vào `HomeUiEffect`.

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** None

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/domain/HomeDomainLogic.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiState.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiEvent.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiEffect.kt`

**Estimated scope:** Medium (4 files)

---

### Task 2: Cập nhật `HomeFakeDataSource`, `HomeViewModel` & Bộ Unit Test Domain/ViewModel

**Description:** Cập nhật `HomeFakeDataSource.kt` và `HomeViewModel.kt` để cung cấp dữ liệu đồng nhất cho 4 vật thể Little Room (*Đang chờ*, *Cần xem lại*, *Ngân sách*, *Đã bỏ qua*), xử lý mở Contextual Sheet cho cả 4 vật thể (`AC-04`) và nút *"Giải thích thêm"* của Observation, hỗ trợ empty state / error state trong sheet (`AC-05`, `AC-15`), ghi nhớ `hiddenInsightIds` không xuất hiện lại khi refresh (`AC-14`), tự cập nhật khi đổi chu kỳ ngân sách (`AC-13`), và chặn thao tác mạng / nút `+` khi offline (`AC-16`, `AC-17`). Cập nhật `HomeDomainLogicTest.kt` và `HomeViewModelTest.kt`.

**Acceptance criteria:**
- [x] Chạm 4 vật thể Little Room mở đúng `HomeSheet.Contextual` với dữ liệu/count đồng bộ và hỗ trợ `isEmpty` khi count = 0 (`AC-04`, `AC-05`).
- [x] Nhấn nút chính trong sheet phát `HomeUiEffect.NavigateToMyItems(filter)` đúng bộ lọc (`WAITING`, `REVIEW`, `SKIPPED`) hoặc `NavigateToBudgetDetails` cho Ví (`AC-06`).
- [x] Nhấn *"Giải thích thêm"* trên `Observation` mở `HomeSheet.Contextual(kind = ContextKind.Observation)` hiển thị chi tiết giải thích ngay tại Home; nhấn *"Ẩn gợi ý này"* hoặc `"X"` lưu `insightId` vào `hiddenInsightIds` và không hiện lại sau khi refresh/select scenario (`AC-14`).
- [x] Khi `isOffline == true`, nhấn nút `+` (`GlobalAddTapped`) không mở Add Item và không tạo draft, chỉ phát hiệu ứng thông báo cần kết nối mạng (`AC-17`).
- [x] Toàn bộ unit tests trong `HomeDomainLogicTest` và `HomeViewModelTest` chạy PASS.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.home.*" --no-configuration-cache`

**Dependencies:** Task 1

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/data/HomeFakeDataSource.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeViewModel.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeDomainLogicTest.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeViewModelTest.kt`

**Estimated scope:** Medium (4 files)

---

### Checkpoint 1: Foundation & Domain Reconciliation
- [x] Tất cả unit tests của domain và viewmodel (`HomeDomainLogicTest`, `HomeViewModelTest`) đều PASS.
- [x] Logic hết hạn `Cooling-off`, giá thiếu `"Chưa có giá"`, ẩn `Observation`, và điều hướng bộ lọc được kiểm chứng tự động.

---

## Phase 2: Core UI Components & Contextual Sheet (`home-layout-header-room`, `home-attention-budget-obs`, `home-contextual-sheet`)

### Task 3: Cập nhật `HomeHeader`, `CompactLivingRoom` & Strings theo chuẩn Candy Pop 3D v1.0

**Description:** Cập nhật `strings_home.xml`, `HomeHeader.kt` và `CompactLivingRoom.kt` bám sát visual baseline `home-candy-pop-3d-approved.png` và `COMPONENTS.md`: Header hiển thị lời chào theo buổi, dòng phụ *"Hôm nay mình cùng Chanko mua sắm có suy nghĩ nhé 💜"*, badge ngoại tuyến kèm thời điểm đồng bộ thực tế (`AC-16`), nút Avatar tròn tím $\ge 48\text{dp}$ mở `Profile & Settings` (`AC-03`). `CompactLivingRoom` hiển thị 4 vật thể + pill nhãn native (*"Đang chờ"*, *"Cần xem lại"*, *"Ngân sách"*, *"Đã bỏ qua"*) + badge số lượng động + bóng thoại ngắn gọn (`AC-04`, `HOME-REV-03`, `HOME-GAP-05`), mỗi vật thể gộp thành 1 node TalkBack $\ge 48\text{dp}$ và tôn trọng Reduced Motion (`AC-20`, `AC-21`).

**Acceptance criteria:**
- [x] `HomeHeader` hiển thị đúng lời chào, phụ đề mới, avatar $\ge 48\text{dp}$, và hiển thị thời điểm đồng bộ thực tế khi ở chế độ offline (`AC-03`, `AC-16`).
- [x] `CompactLivingRoom` hiển thị đúng 4 pill nhãn tiếng Việt: `"Đang chờ"` (Đồng hồ cát), `"Cần xem lại"` (Lịch), `"Ngân sách"` (Ví — không có count badge), `"Đã bỏ qua"` (Pause Jar) (`HOME-REV-03`, `AC-04`).
- [x] Mỗi vật thể + pill nhãn + badge hợp thành 1 vùng chạm duy nhất $\ge 48\text{dp}$ không chồng lấn, có `contentDescription` rõ ràng cho TalkBack; tôn trọng cờ giảm chuyển động (`AC-20`, `AC-21`).

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** Task 1, Task 2

**Files likely touched:**
- `app/src/main/res/values/strings_home.xml`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/HomeHeader.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/CompactLivingRoom.kt`

**Estimated scope:** Medium (3 files)

---

### Task 4: Cập nhật `YourDecisionsSection` (Khung Card Bao Ngoài), `BudgetCard` & `ObservationCard`

**Description:** Cập nhật 3 khu vực nội dung theo đúng mockup Candy Pop 3D v1.0:
1. `YourDecisionsSection.kt`: Bọc toàn bộ header (*"Những món cần chú ý"* + nút *"Tất cả ›"*) và các hàng món đồ (tối đa 3 món) trong một **khung Card sáng bo góc lớn (`24dp`)**; hiển thị pill trạng thái (*"Đến hạn xem lại"*, *"Vừa hết thời gian chờ"*, *"Đang chờ"*) sử dụng icon Candy Duotone (`ITM-05`, `ITM-04`); tự động đẩy pill trạng thái xuống dòng trên màn hình hẹp hoặc font lớn; giữ empty state nhỏ khi 0 món (`AC-09`, `AC-10`, `HOME-STATE-01`).
2. `BudgetCard.kt`: Hiển thị icon ví `BUD-01`, tiêu đề *"Ngân sách mua sắm"*, chu kỳ, số tiền *"Còn lại"* hoặc *"Vượt hạn mức"* (`32sp ExtraBold`), mascot phụ bên phải, thanh tiến trình capsule + *"Đã chi XX%"* bên phải; toàn bộ card là 1 vùng chạm semantic duy nhất mở `BudgetScreen` (`AC-11`, `AC-12`).
3. `ObservationCard.kt`: Tiêu đề *"Góc nhìn từ Chanko"*, icon bóng đèn, nút đóng `X`, nội dung nhận xét, nút *"Giải thích thêm"* và *"Ẩn gợi ý này"* (`AC-14`, `AC-22`).

**Acceptance criteria:**
- [x] `YourDecisionsSection` có khung card trắng/kem bo tròn lớn bao quanh cả header và danh sách món đồ đúng như mockup; giá thiếu hiển thị `"Chưa có giá"`; empty state nhỏ gọn khi 0 món (`AC-09`, `HOME-STATE-01`, `HOME-EDGE-04`).
- [x] `BudgetCard` mở `BudgetScreen` khi chạm bất kỳ vùng nào trên card hoặc nút *"Chi tiết ›"*, không có nested clickable gây đọc lặp TalkBack (`AC-12`).
- [x] `ObservationCard` hiển thị tiêu đề *"Góc nhìn từ Chanko"*, hai nút *"Giải thích thêm"* và *"Ẩn gợi ý này"* cùng nút đóng `X` đạt chuẩn vùng chạm $\ge 48\text{dp}$ (`AC-14`, `AC-20`).

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** Task 3

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/YourDecisionsSection.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetCard.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/ObservationCard.kt`

**Estimated scope:** Medium (3 files)

---

### Task 5: Cập nhật `HomeSheetHost` & Tái cấu trúc `HomeScreen` 5 Khu Vực (Gỡ bỏ Quick Actions)

**Description:** Cập nhật `HomeSheetHost.kt` để hiển thị Contextual Bottom Sheet cho 4 vật thể Little Room và nội dung giải thích chi tiết của *Góc nhìn từ Chanko* (`ContextKind.Observation`), hỗ trợ empty state khi nhóm không có dữ liệu (`AC-05`) và trạng thái lỗi độc lập (`AC-15`). Cập nhật `HomeScreen.kt` theo đúng thứ tự 5 khu vực `Header → Little Room → Những món cần chú ý → Budget → Observation` (`AC-01`) và xóa bỏ hoàn toàn `QuickActionsRow.kt` (`AC-02`).

**Acceptance criteria:**
- [x] `HomeScreen` hiển thị chính xác 5 khu vực theo thứ tự: `Header` $\rightarrow$ `Little Room` $\rightarrow$ `Những món cần chú ý` $\rightarrow$ `Budget Snapshot` $\rightarrow$ `Observation` (`AC-01`).
- [x] File `QuickActionsRow.kt` được xóa và không còn bất kỳ thành phần Quick Actions (`HOME-03`) hay Global Quick Action Sheet nào trên Home (`AC-02`).
- [x] `HomeSheetHost` hiển thị đúng empty state khi nhóm có 0 món (`AC-05`), hiển thị giải thích chi tiết + dẫn chứng khi mở từ nút *"Giải thích thêm"*, và điều hướng đúng đích khi nhấn nút chính (`AC-06`).

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** Task 3, Task 4

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/HomeSheetHost.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/QuickActionsRow.kt` *(xóa)*

**Estimated scope:** Medium (3 files)

---

### Checkpoint 2: Core UI Components
- [x] Tất cả components biên dịch sạch sẽ, không còn tham chiếu tới `QuickActionsRow` hay `HomeSheet.Global`.
- [x] Thứ tự 5 khu vực trên `HomeScreen` khớp 100% với `AC-01` và `HOME-REV-02`.

---

## Phase 3: Navigation Shell, Offline Gate & Verification (`home-shell-navigation-a11y`)

### Task 6: Kết nối Điều hướng `HomeRoute`, `MainNavigation` & Chặn Nút `+` Khi Offline

**Description:** Kết nối đầy đủ các side-effects từ `HomeRoute` vào `MainNavigation.kt` và `MyItemsViewModel.kt`:
- Chạm thẻ món trong *Những món cần chú ý* điều hướng sang `ItemDetailScreen` với đúng `itemId` (`AC-10`).
- Hành động chính từ Contextual Sheet (*Đang chờ*, *Cần xem lại*, *Đã bỏ qua*) và nút *"Tất cả ›"* chuyển sang tab `MyItems` và áp dụng đúng bộ lọc tương ứng (`AC-06`).
- Trên `ChankoBottomBar` (5 vị trí: `Trang chủ / Món đồ / + / Trò chuyện / Phân tích`), kiểm tra trạng thái mạng khi nhấn nút `+`: nếu đang offline thì hiển thị thông báo cần kết nối mạng (Snackbar) và **không mở Add Item, không tạo draft** (`AC-17`).

**Acceptance criteria:**
- [x] Chạm thẻ món trong *Những món cần chú ý* mở `ItemDetailScreen(itemId)` (`AC-10`).
- [x] Hành động chính trong sheet *Đang chờ*, *Cần xem lại*, *Đã bỏ qua* chuyển sang màn hình `MyItems` với đúng bộ lọc (`COOLING_OFF`, `READY`, `SKIPPED`), còn sheet *Ví* mở `BudgetScreen` (`AC-06`).
- [x] Khi offline, nhấn nút `+` trên Bottom Bar hiển thị thông báo cần kết nối mạng, không mở dialog/screen Add Item và không tạo bản nháp (`AC-17`).
- [x] Quay lại Home từ `ItemDetailScreen` hoặc `BudgetScreen` giữ nguyên vị trí cuộn của `LazyColumn` (`AC-18`).

**Verification:**
- [x] Build & unit tests pass: `./gradlew :app:testDevDebugUnitTest --no-configuration-cache`

**Dependencies:** Task 5

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/navigation/MainNavigation.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/myitems/MyItemsViewModel.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/myitems/MyItemsScreen.kt`

**Estimated scope:** Medium (4 files)

---

### Task 7: Hoàn thiện Responsive Previews & Chạy Kiểm Thử Toàn Diện Nghiệm Thu 22 AC

**Description:** Cập nhật bộ `@Preview` trong `HomeScreen.kt` theo ma trận bàn giao (`320dp`, `360dp`, `412dp`, `600dp`, `840dp`, font scale `1.0f / 1.5f / 2.0f`, online/offline, 0/1/3 món cần chú ý, giá thiếu, vượt ngân sách, contextual sheet empty/content, reduced motion). Chạy toàn bộ unit tests, lint và assemble build để nghiệm thu 22 AC.

**Acceptance criteria:**
- [x] Có đủ các `@Preview` đại diện cho baseline Candy Pop 3D v1.0, màn hình hẹp `320dp` + `fontScale = 2.0f`, màn hình rộng `600dp`/`840dp`, offline cache, vượt ngân sách, empty attention và contextual sheet mở.
- [x] `./gradlew :app:testDevDebugUnitTest --no-configuration-cache` PASS 100%.
- [x] `./gradlew :app:assembleDevDebug --no-configuration-cache` BUILD SUCCESSFUL.
- [x] Không cài đặt hay khởi chạy trên thiết bị thật hoặc máy ảo (tuân thủ `AGENTS.md`).

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --no-configuration-cache`
- [x] Build succeeds: `./gradlew :app:assembleDevDebug --no-configuration-cache`

**Dependencies:** Task 6

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeViewModelTest.kt`

**Estimated scope:** Small (2 files)

---

### Checkpoint 3: Complete & Ready for Review
- [x] Toàn bộ 22 quyết định Frozen (`HOME-REV-01`..`HOME-GAP-05`) và 22 Acceptance Criteria (`AC-01`..`AC-22`) hoàn tất.
- [x] Build và Unit Test xanh 100%, sẵn sàng báo cáo nghiệm thu.
