# Tasks: Chanko Core Ecosystem (Cooling-off, Rewards, Mascot Economy, Budget Tracking & Notifications)

> **Plan tham chiếu:** [tasks/plan.md](plan.md)  
> **Capability Map:** [CAPABILITY-MAP-chanko-core.md](../CAPABILITY-MAP-chanko-core.md)  
> **Nguồn quyết định đã chốt:** Cập nhật ngày 07/11/2026

---

## Phase 1: Cooling-off Engine & Shopping Flow (`cooling-off-core`)

### Task 1: Định nghĩa Domain Models & Policy cho Vòng đời Freeze bắt buộc

**Description:**
Xây dựng mô hình miền (Domain models) cho vòng đời Cooling-off của sản phẩm: loại bỏ hoàn toàn các phán quyết BUY/WAIT/SKIP và trạng thái "Đang cân nhắc". Mọi sản phẩm sau Quick Check tự động chuyển sang trạng thái `FREEZE_ACTIVE` với thời gian Freeze do `FreezeDurationPolicy` tự động xác định dựa trên giá sản phẩm. Hỗ trợ trạng thái `READY_FOR_REVIEW` khi hết thời gian, đánh dấu cờ `isRefrozen` để chặn thưởng lần hai.

**Acceptance criteria:**
- [x] Enum `ItemFreezeStatus` gồm: `FREEZE_ACTIVE`, `READY_FOR_REVIEW`, `UNFROZEN_EARLY`, `BOUGHT`, `PASSED`.
- [x] `FreezeDurationPolicy` tự tính thời gian Freeze theo giá sản phẩm:
  - `< 200.000đ`: 1 ngày (24 giờ)
  - `200.000đ – 1.000.000đ`: 3 ngày (72 giờ)
  - `1.000.000đ – 5.000.000đ`: 7 ngày (168 giờ)
  - `> 5.000.000đ`: 14 ngày (336 giờ)
  - Người dùng không có quyền tùy chỉnh hay bỏ qua thời gian Freeze.
- [x] Model `FreezeState` lưu trữ: `lockedPrice`, `startedAtMillis`, `durationMillis`, `isRefrozen: Boolean`, `rewardClaimed: Boolean`.
- [x] Unit tests kiểm tra chuyển đổi trạng thái hợp lệ và tính toán thời gian theo phân khúc giá.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*FreezeDurationPolicyTest*" --tests "*FreezeLifecycleTest*"`
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`

**Dependencies:** None

**Files likely touched:**
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/model/FreezeModels.kt`
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/policy/FreezeDurationPolicy.kt`
- `core/domain/src/test/kotlin/com/hoabh/chanko/domain/policy/FreezeDurationPolicyTest.kt`

**Estimated scope:** Small (2-3 files)

---

### Task 2: Máy tính Đếm ngược & Khóa Giá Sản phẩm (Price Lock & Countdown Engine)

**Description:**
Hiện thực hóa bộ tính toán thời gian còn lại của Freeze căn cứ trên mốc thời gian tuyệt đối (Epoch timestamp UTC) và cơ chế khóa giá (`lockedPrice`). Cung cấp helper kiểm tra sản phẩm đã hoàn thành Freeze chưa (`isFreezeComplete(now)`), tính tỷ lệ thời gian còn lại $\frac{t_{\text{remaining}}}{t_{\text{total}}}$, và format chuỗi thời gian đếm ngược thân thiện tiếng Việt (ví dụ: `"Còn 2 ngày 5 giờ"`, `"Đã sẵn sàng xem xét"`).

**Acceptance criteria:**
- [x] `FreezeCountdownCalculator` tính chính xác số mili-giây còn lại: `remainingMillis(now) = max(0, (startedAt + duration) - now)`.
- [x] Tỷ lệ thời gian còn lại trả về giá trị float trong khoảng `[0.0f, 1.0f]`.
- [x] `lockedPrice` được gán cố định lúc bắt đầu Freeze và không bị biến đổi khi xem chi tiết hoặc tính phí.
- [x] Khi `remainingMillis == 0`, tự động xác định trạng thái là `READY_FOR_REVIEW` mà không tự động mua sản phẩm.
- [x] Unit tests phủ các mốc thời gian: vừa bắt đầu, giữa chừng, vừa hết hạn, và đã quá hạn nhiều ngày.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*FreezeCountdownCalculatorTest*"`

**Dependencies:** Task 1

**Files likely touched:**
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/policy/FreezeCountdownCalculator.kt`
- `core/domain/src/test/kotlin/com/hoabh/chanko/domain/policy/FreezeCountdownCalculatorTest.kt`

**Estimated scope:** Small (2 files)

---

### Task 3: Đơn giản hóa AddItemFlowScreen & QuickCheckScreen theo Luồng Bắt buộc Freeze

**Description:**
Cập nhật luồng Thêm sản phẩm (`AddItemFlowScreen` và `QuickCheckScreen`): sau khi hoàn thành 1–3 câu hỏi thích ứng của Quick Check, màn hình chuyển tiếp ngay sang xác nhận bắt đầu Freeze do Chanko ấn định. Loại bỏ hoàn toàn màn hình phán quyết `DecisionResultScreen` cũ với các nút chọn verdict BUY/WAIT/SKIP hoặc lựa chọn "Không áp dụng thời gian chờ".

**Acceptance criteria:**
- [x] Loại bỏ bước phán quyết BUY/WAIT/SKIP và các lựa chọn bỏ qua thời gian chờ.
- [x] Sau câu hỏi cuối của Quick Check (hoặc khi bấm "Nhận định sớm"), hiển thị màn hình thông báo Chanko kích hoạt Cooling-off với thời gian Freeze được gán tự động.
- [x] Nhấn hoàn tất sẽ lưu sản phẩm với trạng thái `FREEZE_ACTIVE` và mốc thời gian bắt đầu chính xác.
- [x] Dữ liệu lưu thành công chuyển thẳng về Home hoặc Item Detail của món đồ đang Freeze.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*AddItemViewModelTest*"`
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`

**Dependencies:** Task 1, Task 2

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/additem/AddItemViewModel.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/additem/AddItemFlowScreen.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/additem/QuickCheckScreen.kt`

**Estimated scope:** Medium (3-4 files)

---

### Task 4: Cập nhật ItemDetailScreen: Đếm ngược Freeze, Unfreeze sớm & Trạng thái Sẵn sàng xem xét

**Description:**
Nâng cấp `ItemDetailScreen` để hiển thị trực quan tiến trình Cooling-off: hiển thị card đếm ngược thời gian Freeze còn lại (`CoolingOffCountdownCard`), nút "Unfreeze sớm", và khi hết thời gian Freeze tự động hiển thị trạng thái *"Sẵn sàng xem xét"* cùng 2 hành động: "Xác nhận đã mua" hoặc "Bỏ qua / Freeze lại".

**Acceptance criteria:**
- [x] Khi đang `FREEZE_ACTIVE`: hiển thị thanh đếm ngược với thời gian còn lại, thông tin chi phí Unfreeze dự tính nếu mở sớm, và nút Unfreeze.
- [x] Khi hết thời gian: hiển thị banner *"Sẵn sàng xem xét"*, cho phép người dùng lựa chọn:
  - "Xác nhận đã mua"
  - "Bỏ qua món đồ"
  - "Freeze lại" (cập nhật `isRefrozen = true`, áp dụng chu kỳ Freeze mới nhưng không được nhận thưởng lần hai).
- [x] Giữ nguyên các phần thông tin sản phẩm và lịch sử hành trình hiện hữu.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*ItemDetailViewModelTest*"`
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`

**Dependencies:** Task 2, Task 3

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/itemdetail/ItemDetailScreen.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/itemdetail/ItemDetailViewModel.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/itemdetail/components/CoolingOffCountdownCard.kt`

**Estimated scope:** Medium (3 files)

---

### Checkpoint 1: Cooling-off Engine & Shopping Flow Verified
- [x] Vòng đời Freeze bắt buộc từ lúc thêm sản phẩm đến lúc hết hạn hoặc Unfreeze hoạt động trơn tru.
- [x] Tất cả unit tests của Phase 1 pass 100%.

---

## Phase 2: Rewards, Mascot Economy & Unfreeze Penalty (`rewards-mascot-economy`)

### Task 5: Sổ Cái Tài nguyên Hạt, Pate & Mô hình Tiến trình Level (Ledger & Level Engine)

**Description:**
Hiện thực hóa hệ thống quản lý tài nguyên thay thế Point/EXP: **Hạt** (Resource dùng cho tiện ích, bị trừ khi Unfreeze sớm) và **Pate** (EXP phát triển Chanko). Tài khoản áp dụng quy tắc bất biến: số dư Hạt và Pate không bao giờ âm (chặn sàn ở 0), Level của Chanko chỉ tăng khi tích lũy đủ Pate và không bao giờ bị giảm cấp kể cả khi trừ Pate.

**Acceptance criteria:**
- [x] `ResourceLedger` lưu số dư hiện tại `hatBalance: Long` và `pateBalance: Long`.
- [x] Mọi phép trừ tài nguyên áp dụng `coerceAtLeast(0)` (cho phép Unfreeze khi số dư bằng 0 mà không phát sinh số âm).
- [x] Bảng cấp độ `LevelProgressionTable` định nghĩa ngưỡng Pate từng Level (ví dụ: Level 1: 0, Level 2: 100, Level 3: 250, Level 4: 500...).
- [x] Thuộc tính `level` của Chanko được bảo lưu độc lập: khi Pate bị trừ do phạt, `currentLevel` giữ nguyên không bị giáng cấp.
- [x] Không có quy đổi Hạt/Pate ra tiền mặt; không có chỉ số đói hay bệnh tật của mascot.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*ResourceLedgerTest*" --tests "*LevelProgressionTest*"`

**Dependencies:** Task 1

**Files likely touched:**
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/rewards/ResourceLedger.kt`
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/rewards/LevelProgression.kt`
- `core/domain/src/test/kotlin/com/hoabh/chanko/domain/rewards/ResourceLedgerTest.kt`

**Estimated scope:** Small (3 files)

---

### Task 6: Công thức Tính Phạt Unfreeze & Cơ chế Phạt Hồi tố (Penalty & Retroactive Clawback)

**Description:**
Xây dựng bộ tính chi phí Unfreeze sớm theo nguyên tắc:
$$\text{Chi phí} = \alpha \times \text{lockedPrice} \times \frac{\text{remainingMillis}}{\text{totalDurationMillis}}$$
Chi phí giảm dần theo thời gian và bằng 0 khi Freeze kết thúc. Xử lý trường hợp mua khi đang Freeze tương đương Unfreeze sớm tại thời điểm mua. Xây dựng `RetroactivePurchaseHandler`: nếu người dùng khai báo mua trễ sau khi đã nhận thưởng Freeze không hợp lệ, hệ thống sẽ thu hồi thưởng đã nhận, áp dụng chi phí Unfreeze tại thời điểm mua thực tế và ghi nhật ký kiểm toán (audit log).

**Acceptance criteria:**
- [x] `UnfreezeCostCalculator` tính đúng chi phí Hạt và Pate phải trừ dựa trên giá đã khóa và tỷ lệ thời gian còn lại.
- [x] Khi `remainingMillis == 0`, chi phí phạt trả về đúng 0 Hạt, 0 Pate.
- [x] Khai báo mua hàng khi đang Freeze: tính chi phí phạt tại thời điểm mua và trừ Hạt/Pate tương ứng.
- [x] Khai báo mua trễ (retroactive buy): tự động hoàn tác thưởng Freeze đã cộng, tính phí Unfreeze tại thời điểm mua thực tế, ghi nhận vào `RewardAuditLog`, không trừ trùng lặp nếu trước đó đã Unfreeze.
- [x] Unit tests kiểm tra đầy đủ các kịch bản: Unfreeze ngay sau khi tạo, Unfreeze ở 50% thời gian, mua trong lúc Freeze, và khai báo mua sau khi đã nhận thưởng.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*UnfreezeCostCalculatorTest*" --tests "*RetroactivePurchaseHandlerTest*"`

**Dependencies:** Task 2, Task 5

**Files likely touched:**
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/rewards/UnfreezeCostCalculator.kt`
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/rewards/RetroactivePurchaseHandler.kt`
- `core/domain/src/test/kotlin/com/hoabh/chanko/domain/rewards/UnfreezeCostCalculatorTest.kt`

**Estimated scope:** Medium (3-4 files)

---

### Task 7: Cơ chế Thưởng Freeze Tự động & Daily Check-in Hợp lệ

**Description:**
Hiện thực hóa logic trao thưởng tự động:
1. **Hoàn thành Freeze hợp lệ:** Thưởng Hạt và Pate tỷ lệ theo giá sản phẩm, tự động cộng kể cả khi không mở app. Giới hạn tối đa 1 phần thưởng Freeze mỗi ngày: nếu có nhiều món hoàn thành cùng ngày, chọn món có giá trị thưởng cao nhất (`DailyRewardCapPolicy`).
2. **Daily Check-in:** Người dùng xác nhận "Hôm nay tôi không mua sắm ngoài dự kiến" để nhận mức Hạt/Pate cố định (ví dụ: 10 Hạt + 10 Pate), không có streak multiplier.
3. Không thưởng cho hành vi thêm sản phẩm hay hành vi mua sắm.

**Acceptance criteria:**
- [x] `FreezeRewardDispatcher` tự động quét các món đồ hoàn thành Freeze trong ngày và cộng thưởng cho món có giá trị cao nhất.
- [x] Sản phẩm Freeze lại (`isRefrozen == true`) bị chặn không cấp thưởng Freeze lần hai.
- [x] `DailyCheckInUseCase` kiểm tra và chỉ cho phép nhận thưởng 1 lần mỗi ngày dương lịch sau khi người dùng xác nhận không mua sắm.
- [x] Không có cơ chế streak multiplier (thưởng mỗi ngày như nhau).
- [x] Unit tests kiểm tra việc chọn món thưởng cao nhất khi nhiều món hoàn tất trong cùng 1 ngày.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*FreezeRewardDispatcherTest*" --tests "*DailyCheckInTest*"`

**Dependencies:** Task 5, Task 6

**Files likely touched:**
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/rewards/FreezeRewardDispatcher.kt`
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/rewards/DailyCheckInUseCase.kt`
- `core/domain/src/test/kotlin/com/hoabh/chanko/domain/rewards/FreezeRewardDispatcherTest.kt`

**Estimated scope:** Medium (3 files)

---

### Checkpoint 2: Rewards, Economy & Ledger Verified
- [x] Hệ kinh tế Hạt/Pate, công thức Unfreeze và Daily Check-in hoạt động chính xác.
- [x] Số dư không bao giờ âm; Level Chanko không bị tụt.
- [x] Toàn bộ unit tests của Phase 2 pass 100%.

---

## Phase 3: Item-Centric Budget Tracking & Offline Gate (`budget-item-tracking`)

### Task 8: Ghi nhận Giao dịch Ngân sách Chỉ theo Sản phẩm đã Thêm (Item Purchase Recorder)

**Description:**
Tích hợp giao dịch ngân sách với danh sách món đồ: Chanko chỉ trừ ngân sách chu kỳ hiện tại khi người dùng xác nhận đã mua sản phẩm đã có trong Chanko. Hỗ trợ 2 phương thức thanh toán: Pay Now và Thẻ tín dụng (đều ghi nhận chi tiêu ngay tại thời điểm mua). Hỗ trợ mua nhiều lần, hoàn tiền (refund) và điều chỉnh chi phí với nhật ký giao dịch minh bạch.

**Acceptance criteria:**
- [x] Ngân sách chỉ bị trừ khi có sự kiện mua hàng của một sản phẩm hợp lệ trong hệ thống (không có Quick Purchase độc lập).
- [x] Giao dịch Pay Now và Credit Card đều ghi nhận giảm ngân sách khả dụng của chu kỳ hiện tại ngay lập tức.
- [x] Hỗ trợ hoàn tiền (refund): khôi phục lại ngân sách chu kỳ tương ứng và cập nhật trạng thái món đồ.
- [x] Tất cả số tiền hạch toán đều là VNĐ nguyên vẹn, tách bạch hoàn toàn với số dư Hạt/Pate.
- [x] Unit tests kiểm tra ghi nhận chi tiêu, hoàn tiền và tính nhất quán với chu kỳ hiện tại.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*ItemPurchaseBudgetSyncTest*"`

**Dependencies:** Task 1, Task 4

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/domain/budget/repository/BudgetRepository.kt`
- `app/src/main/java/com/hoabh/chanko/data/budget/FakeBudgetRepository.kt`
- `app/src/test/java/com/hoabh/chanko/data/budget/ItemPurchaseBudgetSyncTest.kt`

**Estimated scope:** Medium (3 files)

---

### Task 9: Cơ chế Ngoại tuyến: Bảo vệ Ghi dữ liệu & Hiển thị Cache An toàn (Offline Write Gate)

**Description:**
Thiết lập cơ chế kiểm soát trạng thái mạng cho Budget và Item transactions: Khi ngoại tuyến (offline), ứng dụng chỉ cho phép xem dữ liệu cache (số dư ngân sách, danh sách đồ, thời gian Freeze). Mọi thao tác thay đổi trạng thái (xác nhận mua, Unfreeze sớm, hoàn tiền) bị chặn kèm thông báo tiếng Việt giải thích cần kết nối Internet.

**Acceptance criteria:**
- [x] Khi offline, màn hình Home và Item Detail hiển thị đầy đủ thông tin từ cache, không bị lỗi hay reset số dư về 0.
- [x] Khi bấm Unfreeze hoặc xác nhận mua lúc mất mạng: chặn thao tác ghi và hiển thị `UiText` hoặc Snackbar: *"Bạn đang ngoại tuyến. Vui lòng kết nối mạng để thực hiện thao tác này."*
- [x] Khi có mạng trở lại, tự động mở khóa các thao tác ghi.
- [x] Unit tests kiểm tra trạng thái chặn ghi và thông báo lỗi tương ứng.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*OfflineWriteGateTest*"`

**Dependencies:** Task 8

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/base/NetworkConnectivityManager.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/itemdetail/ItemDetailViewModel.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/itemdetail/ItemDetailOfflineTest.kt`

**Estimated scope:** Small (2-3 files)

---

### Checkpoint 3: Budget Tracking & Offline Guard Verified
- [x] Ngân sách chỉ trừ khi mua sản phẩm có trong app; hạch toán VNĐ chuẩn xác.
- [x] Ngoại tuyến chỉ xem cache và chặn mọi thao tác ghi dữ liệu.

---

## Phase 4: Home Header & Mascot Expression Integration (`home-mascot-presentation`)

### Task 10: Tích hợp Chip Tài nguyên Hạt, Pate Bar & Level trên TopBar Home

**Description:**
Nâng cấp phần Header/TopBar của `HomeScreen` để hiển thị trực quan các chỉ số tài nguyên của người dùng và Chanko:
- Pill Hạt: icon Hạt + số lượng hiện có.
- Pill Chanko Level & Thanh tiến độ Pate: Level hiện tại (ví dụ: `Lv. 3`) cùng thanh progress mini hiển thị tỷ lệ Pate đạt được so với ngưỡng lên cấp tiếp theo.
- Tự động cập nhật tức thì (StateFlow) khi có phần thưởng hoặc khi Unfreeze.
- Giữ nguyên cấu trúc `BudgetCreditCardCarousel` và `ItemJourneySection`.

**Acceptance criteria:**
- [x] TopBar hiển thị đầy đủ 2 chip thông tin: Hạt và Level Chanko (kèm tiến độ Pate).
- [x] Format số lượng Hạt thân thiện (ví dụ: `150 Hạt`).
- [x] Chạm vào chip hiển thị BottomSheet tóm tắt thông tin tài nguyên và lịch sử nhận thưởng gần nhất.
- [x] Không làm xô lệch hoặc gián đoạn layout của Carousel ngân sách v2.2 và danh sách món đồ phía dưới.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*HomeHeaderResourceTest*"`
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`

**Dependencies:** Task 5, Task 8

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeViewModel.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiState.kt`

**Estimated scope:** Medium (3 files)

---

### Task 11: Biểu cảm Mascot Phản hồi Theo Ngữ cảnh & Dialog Unfreeze Trung lập

**Description:**
Hiện thực hóa phản hồi biểu cảm của Chanko Mascot:
- Mascot phản ứng vui vẻ (`R.drawable.mascot_happy`) khi người dùng nhận thưởng Freeze, hoàn thành Daily Check-in hoặc Chanko lên Level.
- Khi người dùng bấm Unfreeze sớm: hiển thị Dialog xác nhận chi phí Hạt và Pate bị trừ với giọng điệu hoàn toàn trung lập, minh bạch và không phán xét. Chanko không khóc, không giận dỗi hay trách móc người dùng (`MASCOT-FEEDING = A`).

**Acceptance criteria:**
- [x] Dialog Unfreeze hiển thị rõ ràng: số Hạt và Pate sẽ bị trừ, số dư còn lại sau khi trừ, và nút xác nhận Unfreeze.
- [x] Văn bản trong dialog mang giọng điệu khách quan, hỗ trợ, không dùng từ ngữ tiêu cực hay tạo cảm giác tội lỗi.
- [x] Khi lên cấp: hiển thị hiệu ứng thông báo chúc mừng Chanko đã lớn hơn (tăng Level).
- [x] Unit tests kiểm tra trạng thái cảm xúc của Mascot theo từng sự kiện.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "*MascotEmotionStateTest*"`

**Dependencies:** Task 4, Task 6, Task 10

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/ui/mascot/ChankoMascot.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/itemdetail/components/UnfreezeConfirmDialog.kt`
- `app/src/test/java/com/hoabh/chanko/ui/mascot/MascotEmotionTest.kt`

**Estimated scope:** Medium (3 files)

---

### Checkpoint 4: Home & Mascot Interaction Verified
- [x] Header hiển thị đầy đủ Hạt, Pate và Level Chanko.
- [x] Mascot tương tác tích cực khi nhận thưởng và trung lập khi Unfreeze sớm.

---

## Phase 5: Proactive Notifications & Verification (`notifications-daily-checkin`)

### Task 12: Bộ Điều phối Thông báo Chủ động & Lời Nhắc Daily Check-in

**Description:**
Xây dựng module điều phối thông báo Push Notification và tổng hợp Daily Email:
1. Thông báo hỏi mua hàng: Định kỳ hỏi người dùng có mua sản phẩm nào đang thêm trong Chanko hay chưa (với các tùy chọn: "Chưa mua", "Đã mua [chọn sản phẩm]", "Để sau").
2. Nhắc nhở Daily Check-in vào khung giờ tối nếu người dùng chưa check-in.
3. Thông báo sản phẩm đã hoàn thành Freeze và sẵn sàng xem xét.
4. Thông báo nhận thưởng Hạt/Pate và Chanko lên cấp.
5. Email tổng kết: bản tin tóm tắt các món đang Freeze, tình hình ngân sách và thành tích nuôi Chanko. Tuyệt đối không dùng thông báo để kích thích mua sắm.

**Acceptance criteria:**
- [x] `NotificationScheduler` (hoặc `NotificationDispatcher`) lên lịch các thông báo theo đúng kịch bản nghiệp vụ.
- [x] Người dùng chạm vào thông báo hỏi mua hàng có thể chọn nhanh trạng thái hoặc mở ứng dụng để xác nhận.
- [x] Nội dung thông báo không bao giờ khuyến khích mua hàng mà tập trung hỗ trợ tự phản tư.
- [x] Unit tests kiểm tra điều kiện phát và nội dung thông báo cho từng kịch bản.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest --tests "*NotificationDispatcherTest*"`

**Dependencies:** Task 1, Task 7, Task 8

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/data/notification/NotificationDispatcher.kt`
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/notification/NotificationModels.kt`
- `app/src/test/java/com/hoabh/chanko/data/notification/NotificationDispatcherTest.kt`

**Estimated scope:** Medium (3 files)

---

### Task 13: Kiểm thử Tích hợp Toàn diện (E2E Integration & Regression Verification)

**Description:**
Thực hiện kiểm thử tích hợp toàn diện trên toàn bộ hệ thống từ:
1. Thêm sản phẩm $\to$ Quick Check $\to$ Kích hoạt Freeze bắt buộc $\to$ Khóa giá.
2. Đếm ngược $\to$ Hết hạn $\to$ Sẵn sàng xem xét $\to$ Thưởng Hạt & Pate tự động $\to$ Chanko tăng Level.
3. Luồng Unfreeze sớm: tính phí chính xác $\to$ trừ Hạt/Pate không âm.
4. Luồng xác nhận mua đồ: trừ ngân sách chu kỳ hiện tại chuẩn xác $\to$ cập nhật Home Carousel.
5. Kiểm tra hồi quy không ảnh hưởng đến bất kỳ tính năng hiện có nào của Home v2.2.

**Acceptance criteria:**
- [x] Toàn bộ luồng end-to-end từ thêm đồ đến thưởng hoặc unfreeze chạy thành công.
- [x] Không có hồi quy trên `BudgetCreditCardCarousel` (BC22-01..BC22-24 vẫn PASS 100%).
- [x] Chạy toàn bộ test suite thành công qua terminal `./gradlew testDebugUnitTest`.
- [x] Biên dịch `assembleDebug` thành công 100% không cảnh báo nghiêm trọng.

**Verification:**
- [x] Tests pass: `./gradlew testDebugUnitTest`
- [x] Build succeeds: `./gradlew assembleDebug`

**Dependencies:** Tasks 1–12

**Files likely touched:**
- `app/src/test/java/com/hoabh/chanko/e2e/ChankoCoreE2ETest.kt`

**Estimated scope:** Medium (1-2 files)

---

### Checkpoint 5: Hoàn tất Toàn diện & Sẵn sàng Triển khai
- [x] Đầy đủ 13 tasks qua 5 phases đã được định nghĩa với acceptance criteria và verification rõ ràng.
- [x] Kế hoạch sẵn sàng để lập trình viên hoặc AI agent thực hiện từng bước.

---

## Phase 6: Home Screen Refresh — Daily Check-in, Item Sorting & Mascot Celebration (`home-screen-refresh`)

### Task 14: Thành phần Dialog Popup Daily Check-in (`DailyCheckInDialog.kt`)

**Description:**
Xây dựng Composable `DailyCheckInDialog` xuất hiện tự động hoặc khi bấm nút Check-in trên Home:
- Hiển thị thông điệp nhẹ nhàng, không phán xét: *"Hôm nay bạn có giữ được kế hoạch chi tiêu, không mua sắm ngoài dự kiến chứ?"*
- Hiển thị rõ phần thưởng: `+10 Hạt, +10 Pate`
- 2 nút hành động:
  - Nút chính: *"Xác nhận (+10 Hạt, +10 Pate)"*
  - Nút phụ: *"Để sau"* (đóng popup nhẹ nhàng, không ép buộc hay spam)
- Giọng điệu thân thiện, tích cực, tuân thủ nguyên tắc `MASCOT-FEEDING = A`.

**Acceptance criteria:**
- [x] Dialog hiển thị tiêu đề, nội dung câu hỏi xác nhận và phần thưởng +10 Hạt & +10 Pate.
- [x] Bấm "Xác nhận" gọi callback `onConfirm` để cộng thưởng vào ledger.
- [x] Bấm "Để sau" gọi callback `onDismiss` đóng popup mà không phạt người dùng.
- [x] Tuân thủ chuẩn thiết kế Candy Colors (không dùng direct hex Color ngoài Color.kt) và responsive font scale.

**Verification:**
- [x] Preview hoạt động tốt trong Compose Previews.
- [x] Unit test giao diện hoặc kiểm thử trạng thái component pass.

**Dependencies:** Task 7, Task 10

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/DailyCheckInDialog.kt`

**Estimated scope:** Small (1 file)

---

### Task 15: Nút Pill Daily Check-in trên TopBar Header (`HomeHeader.kt`)

**Description:**
Nâng cấp hàng chip tài nguyên `HomeResourceChipsRow` trên `HomeHeader`:
- Thêm một chip `DailyCheckInPill` cạnh chip Hạt và Level:
  - Khi **chưa check-in hôm nay**: hiển thị pill màu cam/accent nổi bật `[Check-in +10 🌰]`, bấm vào mở lại `DailyCheckInDialog`.
  - Khi **đã check-in hôm nay**: hiển thị pill màu xám/xanh dịu `[Đã check-in ✓]`, disabled (không clickable) để người dùng yên tâm.
- Tự động mờ dần khi cuộn trang xuống theo `collapseFraction` tương tự các chip tài nguyên khác.

**Acceptance criteria:**
- [x] Khi `hasCheckedInToday = false`: pill hiển thị nhãn Check-in và bấm vào kích hoạt `onCheckInClick`.
- [x] Khi `hasCheckedInToday = true`: pill hiển thị trạng thái đã hoàn thành và vô hiệu hóa click.
- [x] Hiệu ứng thu gọn Header khi cuộn mượt mà không lỗi layout.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*HomeHeaderResourceTest*"`
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`

**Dependencies:** Task 10, Task 14

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/HomeHeader.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiState.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeHeaderResourceTest.kt`

**Estimated scope:** Small (2-3 files)

---

### Task 16: Tích hợp Luồng Daily Check-in & Level Up Celebration vào ViewModel và Screen

**Description:**
Kết nối `DailyCheckInUseCase` vào `HomeViewModel` và `HomeScreen`:
- Khi mở app lần đầu trong ngày: nếu người dùng chưa check-in, tự động set `isCheckInDialogOpen = true` (chỉ mở tự động 1 lần trong phiên để không làm phiền).
- Xử lý các sự kiện:
  - `HomeUiEvent.RequestCheckIn`: mở dialog.
  - `HomeUiEvent.DismissCheckInDialog`: đóng dialog.
  - `HomeUiEvent.ConfirmCheckIn`: gọi `DailyCheckInUseCase` $\to$ cộng 10 Hạt & 10 Pate $\to$ cập nhật số dư Header $\to$ cập nhật `hasCheckedInToday = true` $\to$ Mascot phản hồi vui vẻ (`MascotEvent.DailyCheckInCompleted`).
- Khi phát hiện `currentLevel` tăng: tự động mở `LevelUpCelebrationDialog` chúc mừng Chanko lớn hơn.

**Acceptance criteria:**
- [x] Tự động mở popup Check-in khi chưa check-in trong ngày; không mở lại nếu người dùng đã chọn "Để sau" trong phiên đó.
- [x] Bấm xác nhận check-in cộng đúng 10 Hạt & 10 Pate vào StateFlow của Header tức thì.
- [x] Nút pill trên Header chuyển sang trạng thái "Đã check-in" ngay lập tức.
- [x] Mascot phát câu thoại chúc mừng và dialog Level Up hiển thị khi Chanko tăng Cấp.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*HomeViewModelTest*"`

**Dependencies:** Task 7, Task 14, Task 15

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeViewModel.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiState.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiEvent.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`

**Estimated scope:** Medium (4 files)

---

### Task 17: Sắp xếp Danh sách Item Journey theo Thời gian (Mới nhất ở dưới cùng) & Trạng thái Trực quan

**Description:**
Điều chỉnh danh sách món đồ trong `ItemJourneySection`:
- Giữ 1 danh sách cuộn duy nhất (không chia tab/nhóm).
- Sắp xếp các món đồ theo thứ tự thời gian được thêm vào: món cũ nhất ở trên, **món mới nhất ở dưới cùng** (`addedTimestamp` tăng dần).
- Hiển thị trực quan trạng thái:
  - Món đang Cooling-off (`FREEZE_ACTIVE` / `Waiting`): hiển thị countdown thời gian còn lại (VD: `Còn 2 ngày 14h`).
  - Món đã hoàn thành Freeze (`READY_FOR_REVIEW` / `Ready`): hiển thị badge nổi bật *"Sẵn sàng xem xét"* để người dùng nhận diện ngay món nào cần đưa ra quyết định.

**Acceptance criteria:**
- [x] Danh sách món đồ trong Journey sắp xếp theo `addedTimestamp` tăng dần (món mới nhất ở dưới cùng).
- [x] Node / Card món đồ hiển thị đúng trạng thái đếm ngược hoặc sẵn sàng xem xét.
- [x] Chạm vào node điều hướng chính xác đến `ItemDetailScreen` của món tương ứng.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*HomeDomainLogicTest*"`
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`

**Dependencies:** Task 4, Task 16

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/ItemJourneySection.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeViewModel.kt`

**Estimated scope:** Medium (2 files)

---

### Task 18: Kiểm thử Toàn diện & Hồi quy Màn hình Home (Home Regression & E2E Test)

**Description:**
Thực hiện kiểm thử tích hợp và hồi quy toàn diện cho màn hình Home sau khi refresh:
- Kiểm tra toàn bộ luồng Check-in: tự động mở popup $\to$ xác nhận $\to$ Header cập nhật Hạt/Pate $\to$ pill chuyển trạng thái.
- Kiểm tra thứ tự sắp xếp danh sách món đồ (mới nhất dưới cùng).
- Xác minh không có bất kỳ hồi quy nào trên `BudgetCreditCardCarousel` (BC22-01..BC22-24 vẫn PASS 100%).
- Kiểm tra kiến trúc màu sắc `ColorArchitectureTest` (không dùng direct hex).
- Chạy toàn bộ test suite và build check `assembleDevDebug`.

**Acceptance criteria:**
- [x] Viết test `HomeCheckInFlowTest` kiểm tra trọn vẹn luồng Check-in và cập nhật tài nguyên.
- [x] Tất cả các bài test của Home (Carousel, Header, DomainLogic, CheckIn) PASS 100%.
- [x] `./gradlew :app:testDevDebugUnitTest` PASS 100% không lỗi.
- [x] `./gradlew assembleDevDebug` biên dịch thành công 100%.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest`
- [x] Build succeeds: `./gradlew assembleDevDebug`

**Dependencies:** Tasks 14–17

**Files likely touched:**
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeCheckInFlowTest.kt`

**Estimated scope:** Small (1 file)

---

### Checkpoint 6: Home Screen Refresh Verified
- [x] Popup Daily Check-in và pill mở lại trên Header hoạt động mượt mà.
- [x] Danh sách món đồ sắp xếp chuẩn thời gian (mới nhất dưới cùng) và hiển thị rõ trạng thái Freeze / Sẵn sàng.
- [x] Carousel ngân sách 3 thẻ v2.2 hoàn toàn nguyên vẹn, 100% test xanh.

---

## Phase 7: Item Journey Cooling-off Visuals & State Streamlining (`cooling-off-journey-visuals`)

### Task 19: Tinh gọn Enum Trạng thái `JourneyNodeStatus` & Ánh xạ Miền (Domain Mapping)

**Description:**
Loại bỏ hoàn toàn các trạng thái tàn dư cũ (`Saved` - "Đã lưu", `Considering` - "Đang cân nhắc") và verdict `BUY/WAIT/SKIP` khỏi Item Journey. Tinh gọn `JourneyNodeStatus` đồng bộ với `ItemFreezeStatus`:
- `CoolingOff` ("Đang hạ nhiệt")
- `Ready` ("Sẵn sàng xem xét")
- `Purchased` ("Đã mua")
- `Skipped` ("Đã bỏ qua")
Cập nhật `HomeDomainLogic.kt` (`parseStatus` và `selectRecentJourney`) để ánh xạ chuẩn xác, cập nhật các kịch bản mẫu trong `HomeFakeDataSource.kt` và sửa đổi `HomeDomainLogicTest.kt`.

**Acceptance criteria:**
- [x] `JourneyNodeStatus` chỉ còn 4 trạng thái: `CoolingOff`, `Ready`, `Purchased`, `Skipped`.
- [x] `HomeDomainLogic.parseStatus()` ánh xạ `"Waiting"`, `"CoolingOff"`, `"FreezeActive"` về `JourneyNodeStatus.CoolingOff`.
- [x] Khi hết thời gian đếm ngược (`remainingSeconds <= 0L`), tự động chuyển sang `JourneyNodeStatus.Ready`.
- [x] Mock data trong `HomeFakeDataSource.kt` không còn dùng trạng thái cũ `Saved` / `Considering`.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*HomeDomainLogicTest*"`
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`

**Dependencies:** Checkpoint 6

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/features/home/HomeUiState.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/features/home/HomeDomainLogic.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/features/home/HomeFakeDataSource.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/features/home/HomeDomainLogicTest.kt`

**Estimated scope:** Medium (4 files)

---

### Task 20: Tinh gọn Bảng Màu Viền & Visual Styles (`Color.kt` & `ItemJourneySection.kt`)

**Description:**
Loại bỏ các token màu viền tàn dư cũ (`statusSavedRing`, `statusConsideringRing`) trong `CandyColors.HomeV2`. Tinh giản `resolveJourneyNodeStyle()` xuống 3 nhóm thị giác:
- `CoolingOff`: Viền màu hổ phách/cam dịu (`CandyColors.HomeV2.statusWaitingRing` hoặc token băng mát tương ứng) trên nền rãnh track mờ (`alpha = 0.15f`).
- `Ready`: Viền màu Mint xanh ngọc tươi sáng (`CandyColors.HomeV2.statusReadyRing`).
- `Purchased` / `Skipped`: Viền trung tính nét liền tĩnh (`CandyColors.HomeV2.statusSkippedRing`).
Đảm bảo 100% tuân thủ `ColorArchitectureTest`.

**Acceptance criteria:**
- [x] `resolveJourneyNodeStyle(status)` chỉ xử lý 4 trạng thái, trả về đúng cấu hình viền và badge.
- [x] Không còn tham chiếu đến `Saved` hay `Considering` trong `ItemJourneySection.kt`.
- [x] Huy hiệu góc trên phải (`JourneyStatusBadge`) giữ lại icon chuẩn: ⏳ Đồng hồ cát (CoolingOff), ✓ Tích xanh (Ready), 🛍️ Túi (Purchased), − Dấu trừ (Skipped).

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*ColorArchitectureTest*"`
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`

**Dependencies:** Task 19

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/features/home/components/ItemJourneySection.kt`
- `app/src/main/java/com/hoabh/chanko/ui/theme/Color.kt`

**Estimated scope:** Small (2 files)

---

### Task 21: Vẽ Canvas Circular Progress Arc & Hiệu ứng Nhịp Thở Ready (`ItemJourneySection.kt`)

**Description:**
Triển khai render trực quan mới cho các node trong Item Journey:
- Loại bỏ hoàn toàn vòng quay 360 độ liên tục vô tận (`rotationAngle`).
- Đối với `CoolingOff`: Vẽ Canvas `drawArc` bắt đầu từ -90° (đỉnh 12h), chiều dài cung quét `sweepAngle` tỷ lệ theo thời gian còn lại, trên một rãnh tròn mờ bên dưới.
- Đối với `Ready`: Khi `!isReducedMotionEnabled`, tạo hiệu ứng nhịp thở nhẹ (`Breathing Pulse` chu kỳ 2.5s) trên viền và bóng đổ để thu hút sự chú ý vào quyết định mua/bỏ qua. Khi `isReducedMotionEnabled`, hiển thị viền tĩnh rõ ràng.
- Bổ sung `contentDescription` thân thiện cho TalkBack.

**Acceptance criteria:**
- [x] Node `CoolingOff` hiển thị cung tròn đếm ngược tĩnh tại, không quay chóng mặt.
- [x] Node `Ready` có nhịp thở mời gọi người dùng khi không bật Reduced Motion.
- [x] Khi bật `isReducedMotionEnabled`, mọi hiệu ứng thở đều được tắt, chỉ hiển thị viền tĩnh.
- [x] Chạm vào node vẫn điều hướng chính xác đến `ItemDetailScreen`.

**Verification:**
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`

**Dependencies:** Task 20

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/features/home/components/ItemJourneySection.kt`

**Estimated scope:** Medium (1-2 files)

---

### Task 22: Kiểm thử Hồi quy Toàn diện & Xác thực Biên dịch

**Description:**
Chạy toàn bộ kiểm thử đơn vị, kiểm thử kiến trúc màu sắc và kiểm tra biên dịch build sản phẩm sau khi hoàn tất refactor thị giác Item Journey.

**Acceptance criteria:**
- [x] Cập nhật các test case trong `HomeViewModelTest.kt`, `HomeCheckInFlowTest.kt` tương thích 100% với trạng thái mới.
- [x] `./gradlew :app:testDevDebugUnitTest` PASS 100%.
- [x] `./gradlew :app:testDevDebugUnitTest --tests "*ColorArchitectureTest*"` PASS 100%.
- [x] `./gradlew assembleDevDebug` biên dịch thành công 100%.

**Verification:**
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest`
- [x] Build succeeds: `./gradlew assembleDevDebug`

**Dependencies:** Tasks 19–21

**Files likely touched:**
- `app/src/test/java/com/hoabh/chanko/presentation/features/home/HomeViewModelTest.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/features/home/HomeCheckInFlowTest.kt`

**Estimated scope:** Small (2 test files)

---

### Checkpoint 7: Item Journey Cooling-off Visuals Verified
- [x] Không còn tàn dư trạng thái `Saved` / `Considering`.
- [x] Viền đếm ngược tròn (Circular Progress Arc) và nhịp thở (Breathing Pulse) hoạt động hoàn hảo.
- [x] Carousel 3 thẻ v2.2 và toàn bộ các tính năng khác giữ nguyên 100% độ ổn định.

---

## Phase 8: Item Journey Sub-Labels (Title & Price Display)

### Task 23: Thiết kế Component Sub-Label dưới chân Node (`JourneyNodeSubLabel`)

**Description:**
Xây dựng component `JourneyNodeSubLabel` hiển thị Tên món đồ (Title) và Pill Giá tiền (Price Tag) nằm gọn gàng bên dưới vòng tròn sản phẩm:
- Tên món đồ: tối đa 1 dòng, `12sp`, `FontWeight.SemiBold`, cắt ngắn với `TextOverflow.Ellipsis`, màu `CandyColors.HomeV2.textPrimary`.
- Pill Giá tiền: Chip nhỏ bo tròn `RoundedCornerShape(8.dp)`, nền tím pastel nhẹ `Color(0xFFF0ECF8)` hoặc `Gray100`, text `11sp`, `FontWeight.Bold`, hiển thị `node.priceFormatted` (ví dụ `1.5M`, `350k`), trường hợp thiếu giá (`AC-09`) hiển thị `"Chưa rõ giá"`.

**Acceptance criteria:**
- [x] Component `JourneyNodeSubLabel` render rõ ràng tên và pill giá tiền.
- [x] Xử lý an toàn khi thiếu giá hoặc tên dài.
- [x] Độ tương phản màu chữ đạt chuẩn WCAG AA (> 4.5:1).

**Verification:**
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`

**Dependencies:** None

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/features/home/components/ItemJourneySection.kt`

**Estimated scope:** Small (1 file)

---

### Task 24: Tích hợp Sub-Label vào `JourneyProductNode` & Cân chỉnh Bố cục Ziczac

**Description:**
Tích hợp `JourneyNodeSubLabel` vào khối trạm của mỗi node trên Item Journey:
- Gom vòng tròn chính và `JourneyNodeSubLabel` vào khối trạm dọc (`Column`, `Alignment.CenterHorizontally`).
- Căn chỉnh tâm vòng tròn chính khớp chính xác với `nodeCentersDp` để đường nét đứt kết nối giữ nguyên tọa độ chuẩn.
- Cập nhật `computeCenteredZigZagLayout`: Tăng bước nhảy dọc `verticalStep` từ `74dp/80dp` lên `116dp/124dp` và cập nhật `totalHeight` để nhường không gian cho text, tránh chữ bị đè hoặc chạm vào node tiếp theo.
- Đảm bảo 2 chú mèo mascot ở 2 bên không bị che khuất hoặc xâm lấn vùng chạm của node.

**Acceptance criteria:**
- [x] Các trạm ziczac hiển thị đầy đủ Vòng tròn + Tên món + Pill Giá tiền.
- [x] Đường nét đứt và các trạm không bị chồng chéo hay che khuất chữ.
- [x] Vùng chạm và semantics bao trùm toàn bộ khối trạm (chạm vào tên hoặc giá tiền vẫn điều hướng mở chi tiết sản phẩm).

**Verification:**
- [x] Build succeeds: `./gradlew compileDevDebugKotlin`

**Dependencies:** Task 23

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/features/home/components/ItemJourneySection.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/features/home/HomeDomainLogicTest.kt`

**Estimated scope:** Medium (1-2 files)

---

### Task 25: Kiểm thử Hồi quy, Accessibility Semantics & Build Verification

**Description:**
Chạy toàn bộ test suites, kiểm tra hồi quy layout test trong `HomeDomainLogicTest.kt`, kiểm tra semantics TalkBack và biên dịch APK `assembleDevDebug`.

**Acceptance criteria:**
- [x] `HomeDomainLogicTest.kt` kiểm tra layout centered ziczac vượt qua thành công với khoảng cách mới.
- [x] `./gradlew :app:testDevDebugUnitTest` PASS 100%.
- [x] `./gradlew test` PASS 100%.
- [x] `./gradlew assembleDevDebug` thành công 100%.

**Verification:**
- [x] Tests pass: `./gradlew test`
- [x] Build succeeds: `./gradlew assembleDevDebug`

**Dependencies:** Tasks 23, 24

**Files likely touched:**
- `app/src/test/java/com/hoabh/chanko/presentation/features/home/HomeDomainLogicTest.kt`

**Estimated scope:** Small (1-2 files)

---

### Checkpoint 8: Item Journey Sub-Labels Verified
- [x] Tên món đồ và giá tiền hiển thị rõ ràng, trực quan, thẩm mỹ theo phong cách Candy Theme.
- [x] Trải nghiệm cuộn ziczac mượt mà, không va chạm Mascot hay đè chữ.
- [x] 100% tests và build compilation passed.
