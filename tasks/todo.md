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
- [ ] `ResourceLedger` lưu số dư hiện tại `hatBalance: Long` và `pateBalance: Long`.
- [ ] Mọi phép trừ tài nguyên áp dụng `coerceAtLeast(0)` (cho phép Unfreeze khi số dư bằng 0 mà không phát sinh số âm).
- [ ] Bảng cấp độ `LevelProgressionTable` định nghĩa ngưỡng Pate từng Level (ví dụ: Level 1: 0, Level 2: 100, Level 3: 250, Level 4: 500...).
- [ ] Thuộc tính `level` của Chanko được bảo lưu độc lập: khi Pate bị trừ do phạt, `currentLevel` giữ nguyên không bị giáng cấp.
- [ ] Không có quy đổi Hạt/Pate ra tiền mặt; không có chỉ số đói hay bệnh tật của mascot.

**Verification:**
- [ ] Tests pass: `./gradlew testDebugUnitTest --tests "*ResourceLedgerTest*" --tests "*LevelProgressionTest*"`

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
- [ ] `UnfreezeCostCalculator` tính đúng chi phí Hạt và Pate phải trừ dựa trên giá đã khóa và tỷ lệ thời gian còn lại.
- [ ] Khi `remainingMillis == 0`, chi phí phạt trả về đúng 0 Hạt, 0 Pate.
- [ ] Khai báo mua hàng khi đang Freeze: tính chi phí phạt tại thời điểm mua và trừ Hạt/Pate tương ứng.
- [ ] Khai báo mua trễ (retroactive buy): tự động hoàn tác thưởng Freeze đã cộng, tính phí Unfreeze tại thời điểm mua thực tế, ghi nhận vào `RewardAuditLog`, không trừ trùng lặp nếu trước đó đã Unfreeze.
- [ ] Unit tests kiểm tra đầy đủ các kịch bản: Unfreeze ngay sau khi tạo, Unfreeze ở 50% thời gian, mua trong lúc Freeze, và khai báo mua sau khi đã nhận thưởng.

**Verification:**
- [ ] Tests pass: `./gradlew testDebugUnitTest --tests "*UnfreezeCostCalculatorTest*" --tests "*RetroactivePurchaseHandlerTest*"`

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
- [ ] `FreezeRewardDispatcher` tự động quét các món đồ hoàn thành Freeze trong ngày và cộng thưởng cho món có giá trị cao nhất.
- [ ] Sản phẩm Freeze lại (`isRefrozen == true`) bị chặn không cấp thưởng Freeze lần hai.
- [ ] `DailyCheckInUseCase` kiểm tra và chỉ cho phép nhận thưởng 1 lần mỗi ngày dương lịch sau khi người dùng xác nhận không mua sắm.
- [ ] Không có cơ chế streak multiplier (thưởng mỗi ngày như nhau).
- [ ] Unit tests kiểm tra việc chọn món thưởng cao nhất khi nhiều món hoàn tất trong cùng 1 ngày.

**Verification:**
- [ ] Tests pass: `./gradlew testDebugUnitTest --tests "*FreezeRewardDispatcherTest*" --tests "*DailyCheckInTest*"`

**Dependencies:** Task 5, Task 6

**Files likely touched:**
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/rewards/FreezeRewardDispatcher.kt`
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/rewards/DailyCheckInUseCase.kt`
- `core/domain/src/test/kotlin/com/hoabh/chanko/domain/rewards/FreezeRewardDispatcherTest.kt`

**Estimated scope:** Medium (3 files)

---

### Checkpoint 2: Rewards, Economy & Ledger Verified
- [ ] Hệ kinh tế Hạt/Pate, công thức Unfreeze và Daily Check-in hoạt động chính xác.
- [ ] Số dư không bao giờ âm; Level Chanko không bị tụt.
- [ ] Toàn bộ unit tests của Phase 2 pass 100%.

---

## Phase 3: Item-Centric Budget Tracking & Offline Gate (`budget-item-tracking`)

### Task 8: Ghi nhận Giao dịch Ngân sách Chỉ theo Sản phẩm đã Thêm (Item Purchase Recorder)

**Description:**
Tích hợp giao dịch ngân sách với danh sách món đồ: Chanko chỉ trừ ngân sách chu kỳ hiện tại khi người dùng xác nhận đã mua sản phẩm đã có trong Chanko. Hỗ trợ 2 phương thức thanh toán: Pay Now và Thẻ tín dụng (đều ghi nhận chi tiêu ngay tại thời điểm mua). Hỗ trợ mua nhiều lần, hoàn tiền (refund) và điều chỉnh chi phí với nhật ký giao dịch minh bạch.

**Acceptance criteria:**
- [ ] Ngân sách chỉ bị trừ khi có sự kiện mua hàng của một sản phẩm hợp lệ trong hệ thống (không có Quick Purchase độc lập).
- [ ] Giao dịch Pay Now và Credit Card đều ghi nhận giảm ngân sách khả dụng của chu kỳ hiện tại ngay lập tức.
- [ ] Hỗ trợ hoàn tiền (refund): khôi phục lại ngân sách chu kỳ tương ứng và cập nhật trạng thái món đồ.
- [ ] Tất cả số tiền hạch toán đều là VNĐ nguyên vẹn, tách bạch hoàn toàn với số dư Hạt/Pate.
- [ ] Unit tests kiểm tra ghi nhận chi tiêu, hoàn tiền và tính nhất quán với chu kỳ hiện tại.

**Verification:**
- [ ] Tests pass: `./gradlew testDebugUnitTest --tests "*ItemPurchaseBudgetSyncTest*"`

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
- [ ] Khi offline, màn hình Home và Item Detail hiển thị đầy đủ thông tin từ cache, không bị lỗi hay reset số dư về 0.
- [ ] Khi bấm Unfreeze hoặc xác nhận mua lúc mất mạng: chặn thao tác ghi và hiển thị `UiText` hoặc Snackbar: *"Bạn đang ngoại tuyến. Vui lòng kết nối mạng để thực hiện thao tác này."*
- [ ] Khi có mạng trở lại, tự động mở khóa các thao tác ghi.
- [ ] Unit tests kiểm tra trạng thái chặn ghi và thông báo lỗi tương ứng.

**Verification:**
- [ ] Tests pass: `./gradlew testDebugUnitTest --tests "*OfflineWriteGateTest*"`

**Dependencies:** Task 8

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/base/NetworkConnectivityManager.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/itemdetail/ItemDetailViewModel.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/itemdetail/ItemDetailOfflineTest.kt`

**Estimated scope:** Small (2-3 files)

---

### Checkpoint 3: Budget Tracking & Offline Guard Verified
- [ ] Ngân sách chỉ trừ khi mua sản phẩm có trong app; hạch toán VNĐ chuẩn xác.
- [ ] Ngoại tuyến chỉ xem cache và chặn mọi thao tác ghi dữ liệu.

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
- [ ] TopBar hiển thị đầy đủ 2 chip thông tin: Hạt và Level Chanko (kèm tiến độ Pate).
- [ ] Format số lượng Hạt thân thiện (ví dụ: `150 Hạt`).
- [ ] Chạm vào chip hiển thị BottomSheet tóm tắt thông tin tài nguyên và lịch sử nhận thưởng gần nhất.
- [ ] Không làm xô lệch hoặc gián đoạn layout của Carousel ngân sách v2.2 và danh sách món đồ phía dưới.

**Verification:**
- [ ] Tests pass: `./gradlew testDebugUnitTest --tests "*HomeHeaderResourceTest*"`
- [ ] Build succeeds: `./gradlew compileDebugKotlin`

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
- [ ] Dialog Unfreeze hiển thị rõ ràng: số Hạt và Pate sẽ bị trừ, số dư còn lại sau khi trừ, và nút xác nhận Unfreeze.
- [ ] Văn bản trong dialog mang giọng điệu khách quan, hỗ trợ, không dùng từ ngữ tiêu cực hay tạo cảm giác tội lỗi.
- [ ] Khi lên cấp: hiển thị hiệu ứng thông báo chúc mừng Chanko đã lớn hơn (tăng Level).
- [ ] Unit tests kiểm tra trạng thái cảm xúc của Mascot theo từng sự kiện.

**Verification:**
- [ ] Tests pass: `./gradlew testDebugUnitTest --tests "*MascotEmotionStateTest*"`

**Dependencies:** Task 4, Task 6, Task 10

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/ui/mascot/ChankoMascot.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/itemdetail/components/UnfreezeConfirmDialog.kt`
- `app/src/test/java/com/hoabh/chanko/ui/mascot/MascotEmotionTest.kt`

**Estimated scope:** Medium (3 files)

---

### Checkpoint 4: Home & Mascot Interaction Verified
- [ ] Header hiển thị đầy đủ Hạt, Pate và Level Chanko.
- [ ] Mascot tương tác tích cực khi nhận thưởng và trung lập khi Unfreeze sớm.

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
- [ ] `NotificationScheduler` lên lịch các thông báo theo đúng kịch bản nghiệp vụ.
- [ ] Người dùng chạm vào thông báo hỏi mua hàng có thể chọn nhanh trạng thái hoặc mở ứng dụng để xác nhận.
- [ ] Nội dung thông báo không bao giờ khuyến khích mua hàng mà tập trung hỗ trợ tự phản tư.
- [ ] Unit tests kiểm tra điều kiện phát và nội dung thông báo cho từng kịch bản.

**Verification:**
- [ ] Tests pass: `./gradlew testDebugUnitTest --tests "*NotificationSchedulerTest*"`

**Dependencies:** Task 1, Task 7, Task 8

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/data/notification/NotificationDispatcher.kt`
- `app/src/main/java/com/hoabh/chanko/data/notification/DailyCheckInReminderWorker.kt`
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
- [ ] Toàn bộ luồng end-to-end từ thêm đồ đến thưởng hoặc unfreeze chạy thành công.
- [ ] Không có hồi quy trên `BudgetCreditCardCarousel` (BC22-01..BC22-24 vẫn PASS 100%).
- [ ] Chạy toàn bộ test suite thành công qua terminal `./gradlew testDebugUnitTest`.
- [ ] Biên dịch `assembleDebug` thành công 100% không cảnh báo nghiêm trọng.

**Verification:**
- [ ] Tests pass: `./gradlew testDebugUnitTest`
- [ ] Build succeeds: `./gradlew assembleDebug`

**Dependencies:** Tasks 1–12

**Files likely touched:**
- `app/src/test/java/com/hoabh/chanko/e2e/ChankoCoreE2ETest.kt`

**Estimated scope:** Medium (1-2 files)

---

### Checkpoint 5: Hoàn tất Toàn diện & Sẵn sàng Triển khai
- [ ] Đầy đủ 13 tasks qua 5 phases đã được định nghĩa với acceptance criteria và verification rõ ràng.
- [ ] Kế hoạch sẵn sàng để lập trình viên hoặc AI agent thực hiện từng bước.
