# Tasks: Màn hình Home (Chanko Home v5)

Nhánh: `feature/home-screen` | Theo dõi: `tasks/plan.md` | Tài liệu: `SPEC-home.md`

---

- [x] Task 1: Assets, Strings & Design Tokens Setup
  - Acceptance: 6 file PNG được đặt tại `drawable-nodpi/`. File `strings_home.xml` đầy đủ chuỗi tiếng Việt. Bổ sung token màu trong `Color.kt`.
  - Verify: Build assembleDebug succeeds.
  - Files: `app/src/main/res/drawable-nodpi/*`, `app/src/main/res/values/strings_home.xml`, `app/src/main/java/com/hoabh/chanko/ui/theme/Color.kt`

---

## Task 2: Home Domain Models & Cycle Calculation Logic

**Description:** Xây dựng các data model cho chu kỳ ngân sách, trạng thái từng ô ngày trong lịch, và logic tính toán ngày/thứ bắt đầu chu kỳ cùng bộ Unit Tests kiểm chứng.

**Acceptance criteria:**
- [ ] `SpendingCycle.kt`: Chứa thông tin ngày bắt đầu, ngày kết thúc, hàm tính 7 nhãn thứ tương ứng bắt đầu từ thứ của ngày neo (ví dụ: ngày 15 bắt đầu T5 $\rightarrow$ T5, T6, T7, CN, T2, T3, T4).
- [ ] `CalendarDayState.kt`: Mô hình trạng thái từng ngày trên lưới lịch (`Normal`, `CycleStart`, `Today`, `Selected`, `HasPurchases`) và chi tiết giao dịch.
- [ ] `HomeSpendingRepository.kt`: Interface và In-Memory Mock Implementation cung cấp chu kỳ mẫu (15/10 - 14/11) và các giao dịch mẫu (16/10 và 18/10).
- [ ] Unit test `SpendingCycleTest.kt`: Kiểm chứng thuật toán tính ngày/thứ chu kỳ và an toàn chia tiến độ.

**Verification:**
- [ ] Tests pass: `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew testDebugUnitTest --tests com.hoabh.chanko.presentation.screens.home.SpendingCycleTest`
- [ ] Build succeeds: `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew assembleDebug`

**Dependencies:** Task 1
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/model/SpendingCycle.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/model/CalendarDayState.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/data/HomeSpendingRepository.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/SpendingCycleTest.kt`

**Estimated scope:** M (4 files)

---

## Checkpoint 1: Foundation Complete
- [ ] Toàn bộ asset PNG và strings sẵn sàng.
- [ ] Unit tests cho logic chu kỳ chi tiêu pass 100%.
- [ ] Build dự án thành công không lỗi.

---

## Task 3: HomeUiState, HomeUiEvent, HomeViewModel & Unit Tests

**Description:** Xây dựng tầng Presentation State Engine: `HomeUiState`, `HomeUiEvent`, và `HomeViewModel` (kết nối `ItemRepository` + `HomeSpendingRepository`), xử lý chọn ngày, nhảy về hôm nay, phân loại ready/cooling items, cùng bộ test `HomeViewModelTest`.

**Acceptance criteria:**
- [ ] `HomeUiState.kt`: Quản lý trạng thái màn hình (`Loading`, `Success`, `Error`), bao gồm chu kỳ, ngày đang chọn, danh sách ready items (`canDecide == true`), danh sách cooling items (`remainingMillis > 0`), thông điệp mèo.
- [ ] `HomeUiEvent.kt`: Định nghĩa các sự kiện người dùng (`SelectDate`, `JumpToToday`, `OpenItem`, `ReviewItem`, `AddItem`, `SettingsClick`, `ProfileClick`).
- [ ] `HomeViewModel.kt`: Tích hợp Hilt, expose `StateFlow<HomeUiState>`, xử lý logic chuyển ngày chọn và cập nhật chi tiết giao dịch.
- [ ] `HomeViewModelTest.kt`: Kiểm thử nạp dữ liệu, chọn ngày, nhảy về hôm nay, lọc danh sách món đồ, tính tiến độ an toàn khi ngân sách = 0.

**Verification:**
- [ ] Tests pass: `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew testDebugUnitTest --tests com.hoabh.chanko.presentation.screens.home.HomeViewModelTest`
- [ ] Build succeeds: `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew assembleDebug`

**Dependencies:** Task 2
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiState.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeUiEvent.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeViewModel.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeViewModelTest.kt`

**Estimated scope:** M (4 files)

---

## Checkpoint 2: State Engine Complete
- [ ] `HomeViewModel` hoạt động trơn tru với cả 2 repository.
- [ ] Unit tests pass 100%, bảo đảm không có exception khi xử lý date hoặc chia tiến độ.

---

## Task 4: Calendar & Budget UI Components

**Description:** Xây dựng cụm thành phần UI thẻ lịch chi tiêu: Header lời chào, Lưới lịch chu kỳ 7 cột, Linh vật Mèo 3D kèm bong bóng thoại, Thống kê ngân sách 3 cột và Thẻ container `SpendingCalendarCard`.

**Acceptance criteria:**
- [ ] `HomeHeader.kt`: Câu chào thời gian + nút Cài đặt và Avatar (touch target $\ge 48\text{dp}$).
- [ ] `CycleCalendarGrid.kt`: Hàng thứ theo chu kỳ, lưới các ô ngày có outline tím (Start), tím đặc (Today), viền hồng (Selected), chấm hồng (Purchase).
- [ ] `MascotSpeechBanner.kt`: Ảnh mèo `mascot_crossed_arms.png` + bong bóng thoại nền `#FFECE5` vẽ bằng Compose UI với text thật.
- [ ] `BudgetSummarySection.kt`: 3 cột thông số (Ngân sách, Đã tiêu, Còn lại) + progress bar bo góc full + ngày bắt đầu kỳ mới.
- [ ] `SpendingCalendarCard.kt`: Kết hợp tiêu đề, nút "Hôm nay", lưới lịch, chi tiết ngày chọn, mascot và thống kê ngân sách trong card container radius 24dp `#F4EEFF`.

**Verification:**
- [ ] Build succeeds: `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew assembleDebug`
- [ ] Manual check: Khối lịch hiển thị đúng tỷ lệ và các trạng thái ngày theo mockup v5.

**Dependencies:** Task 1, Task 2
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/HomeHeader.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/CycleCalendarGrid.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/MascotSpeechBanner.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/BudgetSummarySection.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/SpendingCalendarCard.kt`

**Estimated scope:** M (5 files)

---

## Task 5: Review & Cooling Item UI Components

**Description:** Xây dựng 2 section danh sách món đồ: "Sẵn sàng xem lại" (mint card + nút Xem lại $\ge 48\text{dp}$) và "Đang cân nhắc" (card xanh nhạt + timer chip).

**Acceptance criteria:**
- [ ] `ReadyReviewSection.kt`: Header icon cửa mở 3D `ready_open_door.png` + số lượng món. Card mint gradient (`#D9FAE6` $\rightarrow$ `#CFF6DF`), thumbnail 64dp, chip "Đã hết thời gian chờ", nút Xem lại $\ge 48\text{dp}$ (`#00634F`).
- [ ] `CoolingOffSection.kt`: Header icon bông tuyết 3D `cooling_snowflake.png` + số lượng món. Danh sách card nền xanh nhạt (`#E0EBFF`), thumbnail 64dp, chip đếm ngược thời gian ("Còn X ngày Y giờ").
- [ ] Hỗ trợ trạng thái Empty State nhẹ nhàng khi không có món đồ nào ở từng section.

**Verification:**
- [ ] Build succeeds: `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew assembleDebug`
- [ ] Manual check: Card hiển thị đúng font Nunito, không ellipsis giá tiền.

**Dependencies:** Task 1, Task 2
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/ReadyReviewSection.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/CoolingOffSection.kt`

**Estimated scope:** S (2 files)

---

## Checkpoint 3: UI Slices Complete
- [ ] Toàn bộ các component UI con đã sẵn sàng và biên dịch sạch.
- [ ] Mọi nút bấm và tương tác đều đạt kích thước chuẩn $\ge 48\text{dp}$.

---

## Task 6: Screen Assembly (`HomeScreen`), Stateful Route & Previews

**Description:** Lắp ghép toàn bộ các component vào `HomeScreen` (Stateless) sử dụng `LazyColumn`, nền cream `#FAF8F5` với gradient nhẹ, hỗ trợ responsive $320\text{dp} - 393\text{dp}$ và xây dựng `HomeRoute` (Stateful).

**Acceptance criteria:**
- [ ] `HomeScreen.kt`: Chứa Stateless `HomeScreen` nhận `HomeUiState` và emit các callback. Sử dụng `LazyColumn`, xử lý khoảng cách padding chuẩn 16dp.
- [ ] Xử lý các trạng thái: `Loading` (hiển thị skeleton/loading indicator), `Error` (thông báo lỗi và nút thử lại), `Success` (nội dung đầy đủ).
- [ ] `HomeRoute`: Kết nối với `HomeViewModel` qua `collectAsStateWithLifecycle()`, truyền callback xuống `HomeScreen`.
- [ ] Cung cấp `@Preview` cho Light scheme với sample mock data.

**Verification:**
- [ ] Build succeeds: `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew assembleDebug`
- [ ] Manual check: Previews hiển thị trọn vẹn màn hình Home v5.

**Dependencies:** Task 3, Task 4, Task 5
**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`

**Estimated scope:** S (1 file)

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
