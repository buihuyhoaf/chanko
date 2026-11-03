# Tasks: Chanko Home UI v2.0 (Budget Credit Card & Item Journey)

> **Plan tham chiếu:** [tasks/plan.md](../../tasks/plan.md) & [docs/plans/home-ui-v2-plan.md](../plans/home-ui-v2-plan.md)  
> **Spec tham chiếu:** [SPEC-home-ui-v2.md](../../SPEC-home-ui-v2.md) & [docs/features/home-ui-v2-spec.md](../features/home-ui-v2-spec.md)

---

## Phase 1: Foundation, Tokens & Domain/State Engine (`home-v2-domain-state`)

### Task 1: Bổ sung Semantic Color Tokens v2.0, Strings & Domain Logic (`JourneySelector` + `BudgetV2Calculator`)
- [x] Toàn bộ mã màu Home v2.0 nằm trong `CandyPalette` và экспоrt qua `CandyColors.HomeV2` trong `Color.kt`; `ColorArchitectureTest` chạy PASS.
- [x] `JourneySelector` lọc bỏ `isArchived == true` trước khi lấy tối đa 5 món mới nhất theo `createdAt DESC` và tie-break ổn định theo `itemId ASC`; thay đổi `status` không làm đổi vị trí node (`AC-10`, `AC-11`, `AC-12`, `AC-25`).
- [x] `BudgetV2Calculator` tính nhất quán số còn lại, đã chi (`Pay Now` + `Credit`), tổng ngân sách, tỷ lệ `%` và chu kỳ; xử lý đúng `spent > limit` (giữ số âm, tính `overAmountMinor`), `limit <= 0` (`isConfigured = false`) và món thiếu giá (`hasMissingPriceItems = true`, hiển thị `"Chưa đủ dữ liệu giá"`) (`AC-03`, `AC-06`, `AC-07`, `AC-09`).
- [x] Verify: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

### Task 2: Nâng cấp `HomeUiState`, `HomeUiEvent`, `HomeUiEffect`, `HomeFakeDataSource` & `HomeViewModel` + Unit Tests
- [x] Lỗi nguồn Budget không ảnh hưởng `ItemJourney` và ngược lại; mỗi vùng có sự kiện `RetryBudget` / `RetryJourney` riêng (`AC-08`, `AC-22`).
- [x] Thêm món mới hoặc xóa/archive món trong lúc đang giữ ngón tay trên node (`isGestureActive == true`) không làm đổi `itemId` dưới ngón tay; chỉ cập nhật sau khi gesture kết thúc (`AC-23`, `AC-24`).
- [x] Phân biệt rõ `OfflineCache` (hiện dữ liệu cache + `syncedAt`), `OfflineNoCache` (hiện *"Chưa có dữ liệu"*), `Empty` (0 món) và `NoBudget` (`AC-07`, `AC-20`, `AC-21`).
- [x] Toàn bộ unit tests trong `HomeDomainLogicTest` và `HomeViewModelTest` chạy PASS.
- [x] Verify: `./gradlew :app:testDevDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.home.*" --no-configuration-cache`

### Checkpoint 1: Foundation & Domain/ViewModel Tests
- [x] Toàn bộ unit tests cho `HomeDomainLogicTest`, `HomeViewModelTest` và `ColorArchitectureTest` chạy PASS.

---

## Phase 2: Core v2.0 UI Components (`home-v2-budget-credit-card`, `home-v2-item-journey`)

### Task 3: Xây dựng `BudgetCreditCard.kt` 3D Candy Orange
- [x] `BudgetCreditCard` hiển thị đúng giao diện thẻ tín dụng mua sắm 3D Candy Orange, không có số thẻ/CVV/logo ngân hàng thật (`AC-05`).
- [x] Khi vượt ngân sách giữ nền cam và hiển thị số vượt màu Coral; thanh tiến trình không vượt miền `0..1` (`AC-06`).
- [x] Hiển thị đúng các trạng thái `NoBudget`, `MissingPrice`, `Skeleton`, `Error + Retry` và `OfflineNoCache` (`AC-07`..`AC-09`, `AC-18`, `AC-21`).
- [x] Chạm bất kỳ vùng nào trên thẻ phát duy nhất 1 sự kiện mở `BudgetScreen`; TalkBack đọc đầy đủ số còn lại, đã chi, tổng ngân sách và chu kỳ (`AC-04`, `AC-31`).
- [x] Verify: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

### Task 4: Xây dựng `ItemJourneySection.kt` (*"Món đồ gần đây"*)
- [x] Hiển thị đúng $\min(n, 5)$ node tròn `80–96dp` xếp ziczac với $n - 1$ đoạn nối nét đứt lavender ($n = 1$ không có đoạn nối, $n = 0$ hiện empty state) (`AC-12`, `AC-13`).
- [x] Đầy đủ 6 trạng thái có viền màu + badge icon riêng biệt (không chỉ dùng màu); TalkBack đọc *tên món + trạng thái + hành động Mở chi tiết* theo thứ tự `createdAt` (`AC-14`, `AC-31`).
- [x] Mascot Chanko xuất hiện 1 lần bên phải, cuộn cùng Journey và không chồng lên vùng chạm của node (`AC-17`).
- [x] Chạm node mở trực tiếp `ItemDetailScreen(itemId)` không qua bottom sheet; chạm *"Xem tất cả ›"* mở `MyItems` mặc định (`AC-15`, `AC-16`).
- [x] Verify: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

### Checkpoint 2: Core v2.0 UI Components
- [x] `BudgetCreditCard.kt` và `ItemJourneySection.kt` biên dịch sạch sẽ, không sử dụng mã màu hex trực tiếp ngoài `Color.kt`.

---

## Phase 3: Header, Observation, HomeScreen Assembly & Navigation (`home-v2-header-observation-shell`)

### Task 5: Cập nhật `HomeHeader.kt`, `ObservationCard.kt` & Lắp ghép `HomeScreen.kt` 4 Khu Vực (Dọn bỏ Component v1.0)
- [x] `HomeScreen` hiển thị đúng 4 khu vực theo thứ tự: `Header → Budget Credit Card → Item Journey → Observation` (`AC-01`).
- [x] Không còn `CompactLivingRoom`, `YourDecisionsSection`, `HomeSheetHost` hay `BudgetCard` cũ của v1.0.
- [x] `ObservationCard` ẩn hoàn toàn vùng và khoảng trống khi không có insight hợp lệ hoặc đã ẩn theo `insightId`; nhấn mũi tên `>` mở `InsightExplanationDialog` (`AC-26`, `AC-27`).
- [x] Verify: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`

### Task 6: Kết nối `MainNavigation.kt`, Hoàn thiện Responsive Previews v2.0 & Kiểm thử Nghiệm thu 31 AC
- [x] Chạm *"Xem tất cả ›"* mở `MyItems` ở bộ lọc mặc định (`ALL`); chạm node mở `ItemDetailScreen(itemId)`; chạm `+` khi online mở `URLInputDialog`, khi offline báo cần mạng (`AC-15`, `AC-16`, `AC-20`, `AC-28`, `AC-29`).
- [x] `./gradlew :app:testDevDebugUnitTest --no-configuration-cache` PASS 100%.
- [x] `./gradlew :app:assembleDevDebug --no-configuration-cache` BUILD SUCCESSFUL.
- [x] Tuân thủ nghiêm ngặt `AGENTS.md`: không cài đặt hay chạy trên thiết bị thật hoặc máy ảo/emulator.

### Checkpoint 3: Complete & Ready for Review
- [x] Toàn bộ 31 Acceptance Criteria (`AC-01`..`AC-31`) hoàn tất và được kiểm chứng tự động.
- [x] Unit tests và `assembleDevDebug` xanh 100%, sẵn sàng tạo PR.
