# Tasks: Chanko Home UI v2.0 (Budget Credit Card & Item Journey)

> **Plan tham chiếu:** [tasks/plan.md](plan.md) & [docs/plans/home-ui-v2-plan.md](../docs/plans/home-ui-v2-plan.md)  
> **Spec tham chiếu:** [SPEC-home-ui-v2.md](../SPEC-home-ui-v2.md) & [docs/features/home-ui-v2-spec.md](../docs/features/home-ui-v2-spec.md)

---

## Phase 1: Foundation, Tokens & Domain/State Engine (`home-v2-domain-state`)

### Task 1: Bổ sung Semantic Color Tokens v2.0, Strings & Domain Logic (`JourneySelector` + `BudgetV2Calculator`)

**Description:** Cập nhật `Color.kt` để khai báo bảng màu Home v2.0 (`candyOrange #FFB665`, `candyOrangeShadow #F59145`, `chipGold #F4CF71`, `coral #B83248`, `lavenderConnector #BDA8EB`, cùng màu viền/badge cho 6 trạng thái `Saved`, `Considering`, `Waiting`, `Ready`, `Purchased`, `Skipped`). Cập nhật `strings_home.xml` với đầy đủ chuỗi tiếng Việt cho Home v2.0. Cập nhật `HomeDomainLogic.kt` với `JourneySelector` (loại bỏ `Archived` $\rightarrow$ sắp xếp `createdAt DESC` $\rightarrow$ tie-break `itemId ASC` $\rightarrow$ lấy tối đa 5 món; đổi `status` không đổi thứ tự; reconcile hết hạn `Cooling-off` sang `Ready` không tự `BUY`) và `BudgetV2Calculator` (`remaining = limit - spent` gồm cả `Pay Now` + `Credit`, clamp `progress` về `0..1`, giữ số âm & tính số vượt khi `spent > limit`, xử lý `limit <= 0` không chia cho 0, và đánh dấu `hasMissingPriceItems` khi thiếu giá).

**Acceptance criteria:**
- [x] Toàn bộ mã màu Home v2.0 nằm trong `CandyPalette` và экспоrt qua `CandyColors.HomeV2` trong `Color.kt`; `ColorArchitectureTest` chạy PASS.
- [x] `JourneySelector` lọc bỏ `isArchived == true` trước khi lấy tối đa 5 món mới nhất theo `createdAt DESC` và tie-break ổn định theo `itemId ASC`; thay đổi `status` không làm đổi vị trí node (`AC-10`, `AC-11`, `AC-12`, `AC-25`).
- [x] `BudgetV2Calculator` tính nhất quán số còn lại, đã chi (`Pay Now` + `Credit`), tổng ngân sách, tỷ lệ `%` và chu kỳ; xử lý đúng `spent > limit` (giữ số âm, tính `overAmountMinor`), `limit <= 0` (`isConfigured = false`) và món thiếu giá (`hasMissingPriceItems = true`, hiển thị `"Chưa đủ dữ liệu giá"`) (`AC-03`, `AC-06`, `AC-07`, `AC-09`).

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** None

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/ui/theme/Color.kt`
- `app/src/main/res/values/strings_home.xml`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/domain/HomeDomainLogic.kt`

**Estimated scope:** Medium (3 files)

---

### Task 2: Nâng cấp `HomeUiState`, `HomeUiEvent`, `HomeUiEffect`, `HomeFakeDataSource` & `HomeViewModel` + Unit Tests

**Description:** Cập nhật `HomeUiState.kt`, `HomeUiEvent.kt`, `HomeUiEffect.kt`, `HomeFakeDataSource.kt` và `HomeViewModel.kt` sang kiến trúc Home v2.0:
- Sử dụng `SectionState<T>` độc lập cho `budgetState` và `journeyState` (`InitialLoading`, `Content`, `Empty`, `OfflineNoCache`, `Error(message, cachedData)`), kèm nút `Thử lại` cục bộ từng vùng (`AC-08`, `AC-18`..`AC-22`).
- Cơ chế khóa snapshot theo gesture (`JourneyNodeGestureStarted(itemId)` $\rightarrow$ `JourneyNodeGestureEnded` / `JourneyNodeClicked(itemId)`): hoãn áp dụng danh sách mới khi người dùng đang chạm giữ node; nếu node vừa bị xóa/archive trước khi click hoàn tất thì hủy click thay vì mở món kế tiếp (`AC-23`, `AC-24`).
- Quản lý ẩn `Observation` bền vững theo `insightId` và đóng/mở `InsightExplanationDialog` (`AC-26`, `AC-27`).
- Cập nhật `HomeDomainLogicTest.kt` và `HomeViewModelTest.kt` bao phủ toàn bộ 23 hàng trong `STATE-MATRIX.csv` và 31 AC.

**Acceptance criteria:**
- [x] Lỗi nguồn Budget không ảnh hưởng `ItemJourney` và ngược lại; mỗi vùng có sự kiện `RetryBudget` / `RetryJourney` riêng (`AC-08`, `AC-22`).
- [x] Thêm món mới hoặc xóa/archive món trong lúc đang giữ ngón tay trên node (`isGestureActive == true`) không làm đổi `itemId` dưới ngón tay; chỉ cập nhật sau khi gesture kết thúc (`AC-23`, `AC-24`).
- [x] Phân biệt rõ `OfflineCache` (hiện dữ liệu cache + `syncedAt`), `OfflineNoCache` (hiện *"Chưa có dữ liệu"*), `Empty` (0 món) và `NoBudget` (`AC-07`, `AC-20`, `AC-21`).
- [x] Toàn bộ unit tests trong `HomeDomainLogicTest` và `HomeViewModelTest` chạy PASS.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.home.*" --no-configuration-cache`

**Dependencies:** Task 1

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiState.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiEvent.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiEffect.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/data/HomeFakeDataSource.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeViewModel.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeDomainLogicTest.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeViewModelTest.kt`

**Estimated scope:** Medium (5 main files + 2 test files)

---

### Checkpoint 1: Foundation & Domain/ViewModel Tests
- [x] Toàn bộ unit tests cho `HomeDomainLogicTest`, `HomeViewModelTest` và `ColorArchitectureTest` chạy PASS.

---

## Phase 2: Core v2.0 UI Components (`home-v2-budget-credit-card`, `home-v2-item-journey`)

### Task 3: Xây dựng `BudgetCreditCard.kt` 3D Candy Orange

**Description:** Tạo mới `BudgetCreditCard.kt` theo đúng mockup `reference/home-v2-approved.png` và `UX-SPEC.md`:
- Thẻ 3D Candy Orange (`#FFB665` $\rightarrow$ `#F59145`) bo góc `24.dp`, `minHeight = 192.dp` (tự giãn chiều cao khi `fontScale = 2.0f`).
- Hàng trên: Icon dấu chân trắng + chữ `CHANKO` (`SHOPPING BUDGET`) bên trái; chip vàng EMV + sóng contactless bên phải (`contentDescription = null`).
- Giữa: Nhãn *"Ngân sách còn lại"*, số tiền `32sp ExtraBold` màu nâu đậm (`#30213F`), mèo trắng 3D ló đầu bên phải (`contentDescription = null`), thanh tiến trình capsule (`0..1`) kèm pill `XX%`.
- Hàng dưới: Icon túi mua sắm + *"Đã chi [spent] / [limit]"* và icon lịch + khoảng ngày chu kỳ (`dd/MM – dd/MM`).
- Trạng thái vượt ngân sách (`spent > limit`): **giữ nguyên nền thẻ cam**, hiển thị cảnh báo/số vượt bằng màu **Coral (`#B83248`)** (`AC-06`).
- Hỗ trợ đầy đủ chú thích thiếu giá (*"Chưa đủ dữ liệu giá"* — `AC-09`), `NoBudget` (`AC-07`), `Skeleton` (`AC-18`), `Error + Retry cục bộ` (`AC-08`) và `OfflineNoCache` (`AC-21`).
- Toàn bộ thẻ là 1 vùng chạm semantic duy nhất mở `BudgetScreen` (`AC-04`, `AC-31`).

**Acceptance criteria:**
- [x] `BudgetCreditCard` hiển thị đúng giao diện thẻ tín dụng mua sắm 3D Candy Orange, không có số thẻ/CVV/logo ngân hàng thật (`AC-05`).
- [x] Khi vượt ngân sách giữ nền cam và hiển thị số vượt màu Coral; thanh tiến trình không vượt miền `0..1` (`AC-06`).
- [x] Hiển thị đúng các trạng thái `NoBudget`, `MissingPrice`, `Skeleton`, `Error + Retry` và `OfflineNoCache` (`AC-07`..`AC-09`, `AC-18`, `AC-21`).
- [x] Chạm bất kỳ vùng nào trên thẻ phát duy nhất 1 sự kiện mở `BudgetScreen`; TalkBack đọc đầy đủ số còn lại, đã chi, tổng ngân sách và chu kỳ (`AC-04`, `AC-31`).

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** Task 1, Task 2

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetCreditCard.kt`

**Estimated scope:** Small (1 file)

---

### Task 4: Xây dựng `ItemJourneySection.kt` (*"Món đồ gần đây"*)

**Description:** Tạo mới `ItemJourneySection.kt` theo đúng mockup `reference/home-v2-approved.png`, `UX-SPEC.md` và `ANDROID-COMPOSE-GUIDE.md`:
- Header: Icon túi mua sắm tím + tiêu đề *"Món đồ gần đây"* + nút *"Xem tất cả ›"* ($\ge 48\text{dp}$, mở `MyItems` mặc định — `AC-16`).
- Vùng hành trình (`BoxWithConstraints`):
  - Hiển thị $\min(n, 5)$ node tròn đường kính `80–96dp` (tự thu về `80dp` trên màn hẹp `320dp`) xếp ziczac dọc bên trái (`AC-12`, `AC-13`).
  - Vẽ $n - 1$ đoạn đường nối nét đứt màu lavender (`#BDA8EB`, `stroke = 2.dp`, `dash = 6.dp`, `gap = 6.dp`) bằng `Canvas` nối từ mép đường tròn node $i$ tới mép đường tròn node $i+1$; khi $n = 1$ không vẽ đường nối; `Canvas` không nhận pointer và không tạo node TalkBack (`AC-12`, `AC-13`).
  - Mỗi node hiển thị ảnh sản phẩm ở giữa (hoặc icon placeholder khi chưa có/lỗi ảnh), viền màu 3D + huy hiệu icon góc trên phải cho đủ **6 trạng thái**: `Saved` (bookmark), `Considering` (dấu hỏi `?`), `Waiting` (đồng hồ cát), `Ready` (dấu tích `✓`), `Purchased` (túi mua sắm), `Skipped` (dấu trừ `−`) (`AC-14`).
  - Mỗi node khóa `itemId` khi `pointerInput` bắt đầu chạm (`onNodeGestureStart(itemId)` $\rightarrow$ `onNodeGestureEnd()`) và mở trực tiếp `ItemDetailScreen(itemId)` khi click (`AC-15`, `AC-23`, `AC-24`).
  - Đặt **1 mascot Chanko canonical** (`R.drawable.img_chanko_mascot`) ở khoảng trống bên phải cuộn cùng Journey, tự thu nhỏ/dịch trên màn hẹp và không che vùng chạm của bất kỳ node nào (`AC-17`).
- Hỗ trợ trạng thái `Empty` (mascot + thông điệp ngắn, không tạo node giả), `InitialLoading`, `OfflineNoCache` và `Error + Retry cục bộ` (`AC-12`, `AC-18`, `AC-21`, `AC-22`).

**Acceptance criteria:**
- [x] Hiển thị đúng $\min(n, 5)$ node tròn `80–96dp` xếp ziczac với $n - 1$ đoạn nối nét đứt lavender ($n = 1$ không có đoạn nối, $n = 0$ hiện empty state) (`AC-12`, `AC-13`).
- [x] Đầy đủ 6 trạng thái có viền màu + badge icon riêng biệt (không chỉ dùng màu); TalkBack đọc *tên món + trạng thái + hành động Mở chi tiết* theo thứ tự `createdAt` (`AC-14`, `AC-31`).
- [x] Mascot Chanko xuất hiện 1 lần bên phải, cuộn cùng Journey và không chồng lên vùng chạm của node (`AC-17`).
- [x] Chạm node mở trực tiếp `ItemDetailScreen(itemId)` không qua bottom sheet; chạm *"Xem tất cả ›"* mở `MyItems` mặc định (`AC-15`, `AC-16`).

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** Task 1, Task 2

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/ItemJourneySection.kt`

**Estimated scope:** Small (1 file)

---

### Checkpoint 2: Core v2.0 UI Components
- [x] `BudgetCreditCard.kt` và `ItemJourneySection.kt` biên dịch sạch sẽ, không sử dụng mã màu hex trực tiếp ngoài `Color.kt`.

---

## Phase 3: Header, Observation, HomeScreen Assembly & Navigation (`home-v2-header-observation-shell`)

### Task 5: Cập nhật `HomeHeader.kt`, `ObservationCard.kt` & Lắp ghép `HomeScreen.kt` 4 Khu Vực (Dọn bỏ Component v1.0)

**Description:**
- Cập nhật `HomeHeader.kt`: Lời chào theo buổi + phụ đề *"Hôm nay mình cùng cân nhắc nhé!"* + nút Avatar tròn mở `Profile & Settings` gộp + huy hiệu Offline kèm `syncedAt` (`AC-02`, `AC-20`).
- Cập nhật `ObservationCard.kt`: Giao diện v2.0 với mèo Chanko ló đầu góc trái dưới, icon bóng đèn + *"Góc nhìn từ Chanko"*, nội dung nhận xét, nút tròn mũi tên `>` mở `InsightExplanationDialog` và nút ẩn có nhãn trợ năng (`AC-26`, `AC-27`, `AC-31`).
- Cập nhật `HomeScreen.kt`: Lắp ghép đúng 4 khu vực theo thứ tự `Header → Budget Credit Card → Item Journey → Observation` (`AC-01`), bảo toàn vị trí cuộn và khôi phục anchor hợp lệ sau process death (`AC-30`), chặn double-tap navigation.
- Xóa bỏ các file chỉ thuộc Home v1.0 không còn dùng trong v2.0: `CompactLivingRoom.kt`, `YourDecisionsSection.kt`, `HomeSheetHost.kt`, và `BudgetCard.kt` cũ.

**Acceptance criteria:**
- [x] `HomeScreen` hiển thị đúng 4 khu vực theo thứ tự: `Header → Budget Credit Card → Item Journey → Observation` (`AC-01`).
- [x] Không còn `CompactLivingRoom`, `YourDecisionsSection`, `HomeSheetHost` hay `BudgetCard` cũ của v1.0.
- [x] `ObservationCard` ẩn hoàn toàn vùng và khoảng trống khi không có insight hợp lệ hoặc đã ẩn theo `insightId`; nhấn mũi tên `>` mở `InsightExplanationDialog` (`AC-26`, `AC-27`).

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

**Dependencies:** Task 3, Task 4

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/HomeHeader.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/ObservationCard.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/CompactLivingRoom.kt` *(xóa)*
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/YourDecisionsSection.kt` *(xóa)*
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/HomeSheetHost.kt` *(xóa)*
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetCard.kt` *(xóa)*

**Estimated scope:** Medium (3 files updated, 4 legacy files deleted)

---

### Task 6: Kết nối `MainNavigation.kt`, Hoàn thiện Responsive Previews v2.0 & Kiểm thử Nghiệm thu 31 AC

**Description:**
- Kiểm tra và cập nhật `MainNavigation.kt` để `SeeAllTap` từ `HomeRoute` điều hướng sang `MyItems` với bộ lọc mặc định (`HomeMyItemsFilter.ALL` — không lọc actionable `AC-16`), `ItemTap(itemId)` mở `ItemDetailScreen(itemId)` (`AC-15`), nút `+` khi online mở duy nhất 1 `URLInputDialog` (`AddItemEntryHost`) và khi offline hiển thị thông báo cần mạng trên Home mà không mở dialog (`AC-20`, `AC-29`).
- Bổ sung đầy đủ `@Preview` trong `HomeScreen.kt` cho các kịch bản v2.0 (`Default 5 Nodes`, `1 Node No Connector`, `Empty 0 Items`, `Over Budget Coral`, `No Budget`, `Missing Price`, `Partial Error`, `Offline Cache`, `320dp + FontScale 2.0f`).
- Chạy toàn bộ unit tests và build `assembleDevDebug` để nghiệm thu đủ 31 AC.

**Acceptance criteria:**
- [x] Chạm *"Xem tất cả ›"* mở `MyItems` ở bộ lọc mặc định (`ALL`); chạm node mở `ItemDetailScreen(itemId)`; chạm `+` khi online mở `URLInputDialog`, khi offline báo cần mạng (`AC-15`, `AC-16`, `AC-20`, `AC-28`, `AC-29`).
- [x] `./gradlew :app:testDevDebugUnitTest --no-configuration-cache` PASS 100%.
- [x] `./gradlew :app:assembleDevDebug --no-configuration-cache` BUILD SUCCESSFUL.
- [x] Tuân thủ nghiêm ngặt `AGENTS.md`: không cài đặt hay chạy trên thiết bị thật hoặc máy ảo/emulator.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --no-configuration-cache`
- [x] Build succeeds: `./gradlew :app:assembleDevDebug --no-configuration-cache`

**Dependencies:** Task 5

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/navigation/MainNavigation.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeViewModelTest.kt`

**Estimated scope:** Medium (3 files)

---

### Checkpoint 3: Complete & Ready for Review
- [x] Toàn bộ 31 Acceptance Criteria (`AC-01`..`AC-31`) hoàn tất và được kiểm chứng tự động.
- [x] Unit tests và `assembleDevDebug` xanh 100%, sẵn sàng tạo PR.
