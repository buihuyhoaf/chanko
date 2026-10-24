# Tasks: Chanko Home — Compose v2.0 (`SPEC-home.md`)

Nhánh: `refactor/clean-home-screen` | Kế hoạch: [`tasks/plan.md`](file:///Users/buihoa/HocTap/Android/chanko/tasks/plan.md) | Đặc tả: [`SPEC-home.md`](file:///Users/buihoa/HocTap/Android/chanko/SPEC-home.md)

---

## Phase 0: Clean Slate & Discovery (`T01`–`T18`)
- [x] **Task 0: Clean Slate Teardown & Phase 0 Discovery (`SPEC-home.md` v2.0)**
  - **Acceptance:**
    - [x] Xóa toàn bộ code/resource legacy của Home v5 cũ, giữ repo biên dịch xanh.
    - [x] Hoàn tất báo cáo khảo sát Phase 0, mapping component và bảng quyết định `T01`–`T18` trong `SPEC-home.md`.
  - **Verify:**
    - [x] `./gradlew assembleDebug :app:testDevDebugUnitTest` pass.
  - **Files:** `SPEC-home.md`, `HomeScreen.kt`, `RepositoryModule.kt`

---

## Phase 1 & 2: Foundation, State Contracts & Header (v2)

- [ ] **Task 1 (Phase 1): Candy Pop v2 Semantic Tokens, Strings & Asset Pack v3 Drawables (6 Living Room PNGs + 3 Sample Thumbnails)**
  - **Description:** Bổ sung các semantic color tokens mới cho v2 vào `Color.kt` (Quick Check gradient, Budget warning orange badge, Observation bulb tile, Global sheet tiles), cập nhật `strings_home.xml` bám sát copy trong ảnh chuẩn v2 (`Quick Check`, `Save an Item`, `Những món cần chú ý`, `Góc nhìn hôm nay`, `Bạn muốn làm gì?...`), và copy trọn bộ **6 file PNG Living Room v3** (`room_background.png`, `cat_on_cushion.png`, `hourglass_books.png`, `calendar_blank.png`, `wallet_mint.png`, `pause_jar_heart.png` từ `chanko-home-compose-handoff-v2/assets/living-room/`) cùng **3 ảnh thumbnail mẫu** (`sample_jacket.png`, `sample_headphones.png`, `sample_keyboard.png` từ `chanko-home-compose-handoff-v2/assets/`) vào `res/drawable-nodpi/`.
  - **Acceptance criteria:**
    - [ ] `CandyColors` có đầy đủ semantic colors cho v2 mà không phá vỡ màn hình khác.
    - [ ] `strings_home.xml` chứa đầy đủ chuỗi tiếng Việt + giữ nguyên `"Quick Check"` / `"Save an Item"` theo ảnh chuẩn.
    - [ ] Đủ 6 PNG Living Room v3 + 3 ảnh thumbnail mẫu trong `res/drawable-nodpi/`.
  - **Verification:**
    - [ ] Build succeeds: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew assembleDebug`
  - **Dependencies:** Task 0
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/ui/theme/Color.kt`
    - `app/src/main/res/values/strings_home.xml`
    - `app/src/main/res/drawable-nodpi/room_background.png`
    - `app/src/main/res/drawable-nodpi/cat_on_cushion.png`
    - `app/src/main/res/drawable-nodpi/hourglass_books.png`
    - `app/src/main/res/drawable-nodpi/calendar_blank.png`
    - `app/src/main/res/drawable-nodpi/wallet_mint.png`
    - `app/src/main/res/drawable-nodpi/pause_jar_heart.png`
    - `app/src/main/res/drawable-nodpi/sample_jacket.png`
    - `app/src/main/res/drawable-nodpi/sample_headphones.png`
    - `app/src/main/res/drawable-nodpi/sample_keyboard.png`
  - **Estimated scope:** Medium (11 files)

- [ ] **Task 2 (Phase 2a): Full v2 State/Event Contracts & Pure Calculators (`DecisionRanker`, `BudgetCalculator`)**
  - **Description:** Định nghĩa đầy đủ mô hình trạng thái `HomeUiState` theo `02-COMPONENT-CONTRACTS.md` (`HeaderUi`, `SectionState<T>`, `LivingRoomUi`, `DialogueUi`, `BudgetUi`, `DecisionUi`, `ObservationUi`, `DraftUi`, `HomeSheet`, `ContextKind`, `HomeCapabilities`), toàn bộ `HomeUiEvent` / `HomeUiEffect`, cùng 2 bộ xử lý pure Kotlin: `DecisionRanker` (dedup, lọc status, stable rank `Ready -> Waiting -> Considering -> Saved`, `take(3)`) và `BudgetCalculator` (tính `remaining`, `ratio`, `visualProgress`, format tiền VND không cắt ngắn, xử lý `S06`–`S08`).
  - **Acceptance criteria:**
    - [ ] `DecisionRanker` pass toàn bộ quy tắc sắp xếp của `S01`–`S04`, `S18`, `S20`.
    - [ ] `BudgetCalculator` tính đúng `S01` (`80%`), `S06` (`120%`, clamp `1f`, âm `-600.000 ₫`), `S07` (limit `0` không chia 0), `S08` (unknown `null` không thành `0 ₫`).
  - **Verification:**
    - [ ] Tests pass: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :app:testDevDebugUnitTest`
  - **Dependencies:** Task 1
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiState.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiEvent.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiEffect.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/domain/HomeDomainLogic.kt`
    - `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeDomainLogicTest.kt`
  - **Estimated scope:** Medium (5 files)

- [ ] **Task 3 (Phase 2b): `HomeHeader` v2 & `HomeScreen` Shell**
  - **Description:** Cập nhật `HomeHeader.kt` khớp ảnh chuẩn v2: lời chào `"Chào buổi sáng, Hoa"` kèm icon mặt trời `☀️`, dòng phụ `"Hôm nay là một ngày tuyệt vời để mua sắm có suy nghĩ nhé 💜"`, nút Gear nằm trong hình tròn nền lavender (`primarySoft`) và nút Avatar tròn tím (`brandPurple`) với chữ cái đầu. Cập nhật `HomeScreen.kt` với đầy đủ các slot theo thứ tự v2.
  - **Acceptance criteria:**
    - [ ] `HomeHeader` hiển thị đúng icon `☀️`, dòng phụ có `💜`, nút Gear tròn lavender và Avatar tròn tím với vùng chạm $\ge 48\text{dp}$.
    - [ ] Tên dài hoặc `fontScale = 2.0` tự động xuống dòng, không đè lên 2 nút bên phải.
  - **Verification:**
    - [ ] Build & tests pass: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew assembleDebug :app:testDevDebugUnitTest`
  - **Dependencies:** Task 2
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/HomeHeader.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
  - **Estimated scope:** Small (2 files)

### Checkpoint 1: After Tasks 1–3
- [ ] All unit tests pass (`./gradlew :app:testDevDebugUnitTest`)
- [ ] Application builds without errors (`./gradlew assembleDebug`)

---

## Phase 3, 4 & 5: Living Room Scene, Dialogue Overlay & Quick Actions

- [ ] **Task 4 (Phase 3): `CompactLivingRoom` Scene Assembly (`scene-layout.json`, 6 PNG Layers, `-3°` Dynamic Calendar Overlay & 4 Hit Targets)**
  - **Description:** Xây dựng `CompactLivingRoom.kt` lắp ráp 6 lớp PNG từ `assets/living-room/` theo đúng tọa độ chuẩn hóa trong `scene-layout.json` (`aspectRatio = 2.5f`, bo góc `24dp` clip):
    - `z = 0`: `room_background.png` (`[0, 0, 1, 1]`, `ContentScale.FillBounds`)
    - `z = 1`: `hourglass_books.png` (`[0.005, 0.395, 0.215, 0.60]`, `ContentScale.Fit`)
    - `z = 2`: `calendar_blank.png` (`[0.19, 0.51, 0.20, 0.46]`, `ContentScale.Fit`) kèm 2 lớp `Text` Compose xoay `-3°` (`rotationZ = -3f`): `month` (`[0.35, 0.25, 0.42, 0.11]`, chữ trắng đậm) và `day` (`[0.34, 0.45, 0.45, 0.29]`, chữ `#585066` extra-bold)
    - `z = 3`: `wallet_mint.png` (`[0.35, 0.55, 0.18, 0.43]`, `ContentScale.Fit`)
    - `z = 4`: `cat_on_cushion.png` (`[0.47, 0.12, 0.35, 0.875]`, `ContentScale.Fit`, trang trí non-clickable)
    - `z = 5`: `pause_jar_heart.png` (`[0.78, 0.48, 0.215, 0.515]`, `ContentScale.Fit`)
    - Thiết lập 4 vùng chạm (`HitTarget`) tách biệt, không chồng lấn ($\ge 48\text{dp}$) với TalkBack semantics riêng cho `Hourglass`, `Calendar`, `Wallet`, `PauseJar`.
  - **Acceptance criteria:**
    - [ ] 6 lớp PNG hiển thị đúng z-order và tọa độ chuẩn hóa trong `scene-layout.json`; tuyệt đối không dùng `mascot_crossed_arms.png`.
    - [ ] Đủ 4 `RoomProp` có vùng chạm $\ge 48\text{dp}$ không chồng lấn và TalkBack semantics riêng.
    - [ ] `Calendar` hiển thị tháng/ngày động từ state (`S19`) xoay `-3°` trên `calendar_blank.png`, không bake cứng `"OCT 24"`.
    - [ ] Bấm từng prop phát đúng `HomeUiEvent.PropTapped(kind)`.
  - **Verification:**
    - [ ] Build & tests pass: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew assembleDebug :app:testDevDebugUnitTest`
  - **Dependencies:** Task 3
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/CompactLivingRoom.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
  - **Estimated scope:** Small (2 files)

- [ ] **Task 5 (Phase 4): `ChankoDialogue` Bubble Overlay bên trong `CompactLivingRoom`**
  - **Description:** Tích hợp `ChankoDialogue` làm bubble màu kem (`speechSurface`) ở góc trên-trái **bên trong** `CompactLivingRoom` với đuôi hướng về phía mèo Chanko, hiển thị 2 dòng chữ (`headline` đậm và `supportingLine` nhạt hơn như `"Bạn có 1 món sẵn sàng xem lại đó!"` / `"Cùng xem thử nhé? 💜"`).
  - **Acceptance criteria:**
    - [ ] `ChankoDialogue` là con bên trong `CompactLivingRoom`, tuyệt đối không phải một item/row riêng trong `LazyColumn`.
    - [ ] Khi `dialogue == null`, ẩn hoàn toàn bubble mà không tạo khoảng trống thừa.
    - [ ] Ở `fontScale = 2.0` hoặc `320dp`, bubble tự giãn chiều cao hợp lý, không cắt chữ và không đè lên vùng chạm của 4 prop.
  - **Verification:**
    - [ ] Build & tests pass: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew assembleDebug :app:testDevDebugUnitTest`
  - **Dependencies:** Task 4
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/CompactLivingRoom.kt`
  - **Estimated scope:** XS (1 file)

- [ ] **Task 6 (Phase 5): `QuickActionsRow` (`Quick Check` & `Save an Item`)**
  - **Description:** Xây dựng `QuickActionsRow.kt` gồm 2 thẻ hành động cạnh nhau như ảnh chuẩn: trái là `Quick Check` (nền tím gradient, icon kính lúp trong vòng tròn trắng, tiêu đề `"Quick Check"`, phụ đề `"Hỏi Chanko trước khi mua"`, chevron trắng); phải là `Save an Item` (nền sáng lavender/trắng, icon `+` trắng trong vòng tròn tím, tiêu đề `"Save an Item"`, phụ đề `"Lưu để xem lại sau"`, chevron nhạt). Hỗ trợ tự động xếp dọc trên màn hẹp `320dp` hoặc `fontScale >= 1.5f`.
  - **Acceptance criteria:**
    - [ ] Bấm `Quick Check` phát `QuickCheckTapped`, bấm `Save an Item` phát `SaveItemTapped` (không tự động tạo item khi chỉ bấm mở).
    - [ ] Không bị cắt chữ ở `320dp` hoặc `fontScale = 2.0`.
  - **Verification:**
    - [ ] Build & tests pass: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew assembleDebug :app:testDevDebugUnitTest`
  - **Dependencies:** Task 3
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/QuickActionsRow.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
  - **Estimated scope:** Small (2 files)

### Checkpoint 2: After Tasks 4–6
- [ ] All unit tests pass (`./gradlew :app:testDevDebugUnitTest`)
- [ ] Application builds without errors (`./gradlew assembleDebug`)

---

## Phase 6, 7 & 8: Budget, Your Decisions & Optional Sections

- [ ] **Task 7 (Phase 6): `BudgetCard` (`BudgetAmounts`, `BudgetProgress`, `S06`–`S08`, `S13`)**
  - **Description:** Xây dựng `BudgetCard.kt` khớp ảnh chuẩn v2: hàng đầu gồm tiêu đề chu kỳ (`"Ngân sách tháng 10 – 11/2026"`) và pill `"Chi tiết >"` (`BudgetDetailsTapped`); hàng giữa gồm cột `"Đã chi"` (`2.400.000 ₫` đậm + `/ 3.000.000 ₫` nhạt), vạch chia dọc, cột `"Còn lại"` (`600.000 ₫` tím đậm) và badge cam `"Đã tiêu 80%"`; hàng dưới gồm thanh progress tím trên nền xám nhạt và nhãn `"80%"` ở bên phải. Hỗ trợ đầy đủ trạng thái `Loading`, `Error` (kèm nút Thử lại cục bộ `Retry(Budget)`), `Unknown` (`S08`), `Zero` (`S07`) và `OverBudget` (`S06`).
  - **Acceptance criteria:**
    - [ ] Không bao giờ cắt ngắn (ellipsis) số tiền; tự reflow khi số tiền dài hoặc màn hẹp (`S15`).
    - [ ] Hiển thị đúng cho `S01`/`S21` (`80%`), `S06` (`120%`, remaining âm `-600.000 ₫`), `S07` (limit `0`), `S08` (unknown) và `S13` (error retry).
  - **Verification:**
    - [ ] Build & tests pass: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew assembleDebug :app:testDevDebugUnitTest`
  - **Dependencies:** Task 2, Task 3
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetCard.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
  - **Estimated scope:** Small (2 files)

- [ ] **Task 8 (Phase 7): `YourDecisionsSection` & `DecisionRow` (`S01`–`S05`, `S12`, `S15`, `S20`, `S21`)**
  - **Description:** Xây dựng `YourDecisionsSection.kt`: header gồm icon checklist tím, tiêu đề `"Những món cần chú ý"`, count chip tròn lavender (`3`) và nút `"Xem tất cả >"` (`ViewAllTapped`). Danh sách hiển thị tối đa 3 `DecisionRow` với `key = item.id`: ảnh thumbnail bo góc `14dp` bên trái (kèm fallback khi `image == null` ở `S15`), tên và giá ở giữa, bên phải là status pill (`Ready`: nền dòng xanh mint nhạt, pill xanh lá có icon check `"Sẵn sàng xem lại"`; `Waiting`: nền dòng lavender nhạt, pill tím có icon đồng hồ `"Đang chờ"` + dòng đếm ngược `"Còn 18 giờ 24 phút"` bên dưới; `Considering`: `"Đang cân nhắc"`; `Saved`: `"Đã lưu"`) và chevron `>`. Hỗ trợ trạng thái `Loading`, `Empty` (`S05` kèm nút gợi ý lưu món đồ) và `Error` (`Retry(Decisions)`).
  - **Acceptance criteria:**
    - [ ] Hiển thị tối đa 3 dòng đúng thứ tự `Ready -> Waiting -> Considering -> Saved`.
    - [ ] Khi timer hết hạn (`S12`), vẫn giữ trạng thái `Waiting` theo source, không tự đổi sang `Ready`.
  - **Verification:**
    - [ ] Build & tests pass: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew assembleDebug :app:testDevDebugUnitTest`
  - **Dependencies:** Task 2, Task 3
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/YourDecisionsSection.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
  - **Estimated scope:** Small (2 files)

- [ ] **Task 9 (Phase 8): `ObservationCard` & `DraftResumeCard` (`S09`–`S11`)**
  - **Description:** Xây dựng `ObservationCard.kt` khớp ảnh chuẩn v2: icon bóng đèn vàng trong tile vuông bo góc lavender bên trái, tiêu đề `"Góc nhìn hôm nay"`, nút đóng `X` (`ObservationDismissTapped(id)`) góc phải, dòng nhận xét in đậm (`"Bạn thường mua các món công nghệ vào cuối tuần."`), dòng bằng chứng màu nhạt (`"3 trong 5 món gần đây được mua vào Thứ 7 hoặc Chủ nhật."`), và 2 nút pill bên dưới: `"Giải thích thêm"` (`ObservationExplainTapped(id)`) & `"Ẩn gợi ý này"` (`ObservationHideTapped(id)`). Xây dựng `DraftResumeCard.kt` cho bản nháp cục bộ (`ResumeDraftTapped(id)`).
  - **Acceptance criteria:**
    - [ ] Khi `observation == null` hoặc `draft == null`, không render item vào `LazyColumn` (không tạo khoảng trắng thừa).
    - [ ] 3 nút `Giải thích thêm`, `Ẩn gợi ý này` và `X` phát 3 sự kiện tách biệt.
  - **Verification:**
    - [ ] Build & tests pass: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew assembleDebug :app:testDevDebugUnitTest`
  - **Dependencies:** Task 2, Task 3
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/ObservationCard.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/DraftResumeCard.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
  - **Estimated scope:** Medium (3 files)

### Checkpoint 3: After Tasks 7–9
- [ ] All unit tests pass (`./gradlew :app:testDevDebugUnitTest`)
- [ ] Application builds without errors (`./gradlew assembleDebug`)

---

## Phase 9, 10 & 11: Navigation, Sheets, 22 Scenarios & QA Sign-Off

- [ ] **Task 10 (Phase 9): `HomeSheetHost` (`GlobalQuickActionSheet` `S22` & `ContextualSheet` `S17`) + `MainNavigation` Wiring (`S16`)**
  - **Description:** Xây dựng `HomeSheetHost.kt` hiển thị modal overlay sheet:
    1. `GlobalQuickActionSheet` (khớp ảnh chuẩn `S22`): drag handle, tiêu đề `"Bạn muốn làm gì?"`, nút đóng `X`, và 2 tile ngang: `Quick Check` (icon bong bóng thoại dấu `?` tím trên tile lavender, tiêu đề `"Quick Check"`, mô tả `"Cùng Chanko cân nhắc trước khi quyết định mua."`, chevron) và `Save an Item` (icon bookmark dấu `+` hồng trên tile hồng phấn, tiêu đề `"Save an Item"`, mô tả `"Lưu một món đồ để xem lại sau."`, chevron).
    2. `ContextualSheet` cho `Hourglass`, `Calendar`, `Wallet`, `PauseJar`, `Decision` (xử lý an toàn `S17` khi entity bị xóa: báo `"Món đồ không còn khả dụng"`, khóa thao tác ghi, cho phép đóng).
    Nối nút `+` của `ChankoBottomBar` trong `MainNavigation.kt` để mở `GlobalQuickActionSheet` mà không đổi tab đang chọn (`S16`).
  - **Acceptance criteria:**
    - [ ] Nhấn `+` mở `GlobalQuickActionSheet` dạng modal overlay có scrim, giữ nguyên tab `Home` đang chọn (`S16`, `S22`).
    - [ ] Nhấn `+` nhiều lần vẫn chỉ có tối đa 1 sheet; nhấn `Back` hoặc `X` đóng sheet và không tự tạo dữ liệu.
    - [ ] `S17`: Khi entity không tồn tại trong `ContextualSheet`, nút ghi bị vô hiệu hóa và không crash.
  - **Verification:**
    - [ ] Build & tests pass: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew assembleDebug :app:testDevDebugUnitTest`
  - **Dependencies:** Tasks 4–9
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/HomeSheetHost.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/navigation/MainNavigation.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
  - **Estimated scope:** Medium (3 files)

- [ ] **Task 11 (Phase 10): `HomeScenarioFixtures` (22 Scenarios `S01`–`S22`) & `HomeViewModel` Full Integration**
  - **Description:** Xây dựng `HomeScenarioFixtures.kt` cài đặt đầy đủ **22 kịch bản (`S01`–`S22`)** khớp 100% với `fixtures/home-scenarios.json` (kèm clock cố định `2026-10-24T03:00:00Z` và múi giờ `Asia/Ho_Chi_Minh`). Cập nhật `HomeViewModel.kt` xử lý toàn bộ `HomeUiEvent` (retry theo section, mở/đóng sheet, ẩn/giải thích observation, tiếp tục draft, chuyển đổi scenario).
  - **Acceptance criteria:**
    - [ ] Cả 22 scenarios `S01`–`S22` đều khởi tạo state chính xác theo đúng `expected` trong `fixtures/home-scenarios.json`.
    - [ ] `HomeViewModel` xử lý mượt mà mọi event và phát đúng `HomeUiEffect`.
  - **Verification:**
    - [ ] Tests pass: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :app:testDevDebugUnitTest`
  - **Dependencies:** Task 10
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/data/HomeFakeDataSource.kt`
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeViewModel.kt`
  - **Estimated scope:** Small (2 files)

- [ ] **Task 12 (Phase 11): Preview Matrix (`S21`, `S22`, `320/393/600dp`, `FontScale 2.0`) & Comprehensive Unit Test Suite**
  - **Description:** Bổ sung đầy đủ `@Preview` trong `HomeScreen.kt` (bao gồm `S21` Screenshot Baseline, `S22` Global Sheet Open Baseline, `S05` Empty, `S06` OverBudget, `S09` Observation + Draft, `S13` Section Error, `S15` 320dp + FontScale 2.0, `600dp`) và mở rộng `HomeViewModelTest.kt` kiểm tra tự động toàn bộ kịch bản `S01`–`S22`.
  - **Acceptance criteria:**
    - [ ] Unit tests trong `HomeViewModelTest.kt` và `HomeDomainLogicTest.kt` kiểm chứng toàn bộ `S01`–`S22` pass 100%.
    - [ ] `./gradlew assembleDebug :app:testDevDebugUnitTest` hoàn tất không lỗi.
  - **Verification:**
    - [ ] Full check: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew assembleDebug :app:testDevDebugUnitTest`
  - **Dependencies:** Task 11
  - **Files likely touched:**
    - `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
    - `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeViewModelTest.kt`
  - **Estimated scope:** Small (2 files)

### Checkpoint 4: Final Sign-off (After Tasks 10–12)
- [ ] All unit tests pass (`./gradlew :app:testDevDebugUnitTest`)
- [ ] Application builds cleanly (`./gradlew assembleDebug`)
- [ ] Ready for user review against `A01`–`A18` and `V01`–`V05`
