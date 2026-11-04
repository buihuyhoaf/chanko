# Spec: Chanko Home v2.2 (Tri-Cycle Budget Carousel & Budget Integration)

> **Capability Map tham chiếu:** [SPEC-home-ui-v2.2-budget.md](../../SPEC-home-ui-v2.2-budget.md)  
> **Nguồn chuẩn (Authoritative Sources):**
> - `Chanko-Home-v2.2-Implementation.docx` (Bản chốt kỹ thuật ngày 04/11/2026)
> - `docs/features/home-ui-v2-spec.md` (Đặc tả Home v2.0 hiện hành)
> - `docs/features/budget-ui-v2-spec.md` & `budget-v2.1-ui-contract.md` (Đặc tả Budget hiện hành)
> - `CODING_STANDARDS.md` (Quy chuẩn kiến trúc và Compose của dự án)
> - Quyết định triển khai: **Fake-First** (dùng chung `FakeBudgetRepository` trong Phase 1; thiết kế Room Entity, DAO, migration được quy hoạch cho Phase tích hợp dữ liệu thật tiếp theo).

---

## 0. Assumptions (Các giả định & Quyết định phạm vi đã chốt)

1. **Nguồn dữ liệu (Fake-First Architecture):**
   - Không tạo Room Entity, DAO hay Database Migration trong giai đoạn này.
   - Cả `HomeScreen` và `BudgetScreen` cùng quan sát một thể hiện dùng chung (shared singleton / instance) của `BudgetRepository` (thông qua `FakeBudgetRepository`) để đảm bảo: Thiết lập hạn mức kỳ sau $\to$ quay lại Home thấy ngay cập nhật; chuyển chu kỳ (rollover) giữ nguyên identity và theme.
   - Các thuộc tính Room Entity, DAO và Database Migration được đặc tả sẵn như hợp đồng kỹ thuật cho giai đoạn kết nối backend/DB thật.
2. **Vị trí nút "Xem tất cả":**
   - Đặt tại Header của khu vực Ngân sách (`HomeSection.Budget`), nằm ngang hàng với tiêu đề `"Ngân sách mua sắm"` và bên ngoài vùng vuốt của `HorizontalPager`.
   - Nút luôn hiển thị và có thể bấm được bất kể người dùng đang dừng ở thẻ nào trong carousel. Chạm nút sẽ mở danh sách toàn bộ chu kỳ từ mới nhất đến cũ nhất trên `BudgetScreen`.
3. **Hiển thị vượt ngân sách (`spent > limit`):**
   - Thanh tiến trình (progress bar) clamp ở `100%` (không vẽ tràn hay gây lỗi giao diện).
   - Nhãn phần trăm hiển thị tỷ lệ thực tế (ví dụ: chi 5.500.000 đ trên hạn mức 5.000.000 đ hiển thị `110%`).
   - Cảnh báo vượt dùng màu Coral (`#B83248` / `CandyColors.HomeV2.coral`) cùng nhãn `"Vượt ngân sách: 500.000 đ"`.
   - Nền thẻ **giữ nguyên màu Candy Gradient** đại diện cho chu kỳ đó, không biến đổi toàn bộ màu thẻ sang đỏ.
4. **Phạm vi Carousel 3 thẻ:**
   - Tối đa 3 vị trí: `[Previous? - Current - Next]`.
   - Nếu không có dữ liệu kỳ trước, slot `Previous` bị loại bỏ (carousel chỉ còn 2 thẻ `[Current - Next]`).
   - Mặc định khi mở hoặc quay lại Home, carousel luôn snap về thẻ `Current`.

---

## 1. Objective (Mục tiêu & Phạm vi)

### 1.1. Tầm nhìn sản phẩm
Nâng cấp khu vực Ngân sách trên `HomeScreen` từ thẻ đơn `BudgetCreditCard` v2.0 thành **`BudgetCreditCardCarousel` v2.2** với các mục tiêu:
1. **Chủ động lập kế hoạch tài chính (Proactive Budgeting):** Cho phép người dùng nhìn thấy và chuẩn bị trước ngân sách cho chu kỳ tiếp theo ngay trên màn Home mà không cần đợi sang tháng mới.
2. **Minh bạch bối cảnh thời gian:** Nhìn lại nhanh kết quả kỳ trước (chỉ đọc) và đối chiếu với kỳ hiện tại thông qua trải nghiệm vuốt ngang tối đa 3 chu kỳ.
3. **Nhận diện chu kỳ qua màu sắc (Theme Identity):** Hệ thống 12 Candy Gradient Themes gắn chặt với identity của từng chu kỳ theo chỉ số `ordinal`, giúp người dùng cảm nhận rõ ranh giới thời gian trôi qua mỗi tháng.
4. **Trải nghiệm nhất quán & tin cậy:** Tính toán thống nhất Pay Now và Credit theo ngày mua thực tế; cô lập lỗi độc lập cho khu vực Budget; hỗ trợ đầy đủ các trạng thái ngoại tuyến (Offline có cache / thiếu cache).

---

## 2. Tech Stack & Design Tokens

### 2.1. Tech Stack
- **Ngôn ngữ:** Kotlin 1.9+ / Coroutines / StateFlow.
- **UI Framework:** Jetpack Compose, Material 3, Compose Foundation (`HorizontalPager`, `rememberPagerState`).
- **Dependency Injection:** Hilt.
- **Testing:** JUnit 4, Truth / AssertJ, Turbine (StateFlow testing), Compose UI Test.

### 2.2. Hệ thống 12 Candy Gradient Themes (`candy-budget-tokens`)
Mỗi chu kỳ được gán một số nguyên `ordinal` bất biến (bắt đầu từ 0). Theme màu của chu kỳ được tra cứu theo công thức:
$$\text{themeIndex} = \text{floorMod}(\text{ordinal}, 12)$$

| Index | Tên Theme | Màu Chính (Main) | Màu Sáng (Light) | Màu Đậm (Dark) |
| :---: | :--- | :---: | :---: | :---: |
| **00** | Tangerine | `#FF963C` | `#FFCB83` | `#E86C25` |
| **01** | Lavender | `#AC86F5` | `#E2CEFF` | `#7950C9` |
| **02** | Mint | `#70D9B0` | `#C8F7E3` | `#299D79` |
| **03** | Strawberry | `#F58FA9` | `#FFD1DB` | `#D9557E` |
| **04** | Sky Blue | `#7CBDF3` | `#D0E9FF` | `#408BCE` |
| **05** | Lemon | `#F7D55E` | `#FFF1A9` | `#D9A52E` |
| **06** | Peach | `#FFAE8C` | `#FFE0C8` | `#E77B65` |
| **07** | Grape | `#A184DE` | `#DCCBFA` | `#7051AE` |
| **08** | Aqua | `#73D5DB` | `#C5F4F4` | `#319FAF` |
| **09** | Rose | `#EBA6D0` | `#FCE0F1` | `#C36BA5` |
| **10** | Pistachio | `#B5D57A` | `#E5F1BC` | `#779F48` |
| **11** | Blueberry | `#929FEB` | `#D3DAFF` | `#596BBE` |

- **Hướng Gradient:** Linear Gradient theo trục chéo hoặc dọc (Sáng $\to$ Chính $\to$ Đậm).
- **Thẻ Placeholder (Next Unconfigured):** Nền trắng ngà/xám pastel (`#F7F5FA` / `#EFEBFA`), viền nét đứt (`dash = 6.dp, gap = 4.dp`), icon `+` trung tính (`#8A7D9D`), không dùng gradient màu chu kỳ.
- **Cảnh báo vượt mức:** Text/icon dùng Coral (`#B83248`). Đảm bảo độ tương phản text WCAG AA $\ge 4.5:1$.

---

## 3. Commands (Lệnh thực thi)

Mọi thao tác kiểm tra, biên dịch và chạy test sử dụng terminal:

```bash
# 1. Chạy toàn bộ Unit Tests của khu vực Home và Budget
./gradlew testDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.home.*" --tests "com.hoabh.chanko.domain.budget.*"

# 2. Chạy toàn bộ Unit Tests trong toàn bộ ứng dụng
./gradlew testDebugUnitTest

# 3. Kiểm tra biên dịch mã nguồn Android Debug
./gradlew assembleDebug

# 4. Kiểm tra Lint / Code style (nếu áp dụng)
./gradlew lintDebug
```

> [!IMPORTANT]
> **Tuyệt đối không** chạy lệnh cài đặt hay khởi động máy ảo/máy thật (`adb install`, `installDebug`, AVD). Chỉ thực hiện build và unit test trên môi trường hiện tại theo quy định dự án.

---

## 4. Project Structure (Cấu trúc Thư mục & Tệp tin)

```
app/src/main/java/com/hoabh/chanko/
├── domain/budget/
│   ├── model/
│   │   ├── BudgetCycle.kt                 # Bổ sung ordinal, status, cycle window
│   │   ├── TriCycleWindow.kt              # Model cửa sổ 3 kỳ (previous, current, next)
│   │   ├── BudgetSnapshot.kt              # Snapshot chi tiêu nguyên tử
│   │   └── BudgetCalculation.kt           # Tính toán chi Pay Now/Credit, clamping, % thực
│   └── repository/
│       └── BudgetRepository.kt            # Contract lấy TriCycleWindow, setup Next, rollover
├── data/budget/
│   └── FakeBudgetRepository.kt            # Shared In-Memory Fake cài đặt đầy đủ logic v2.2
├── ui/theme/
│   ├── Color.kt                           # Bổ sung 12 CandyTheme tokens & Neutral placeholder tokens
│   └── CandyBudgetThemes.kt               # Mapping floorMod(ordinal, 12) -> Brush gradient
└── presentation/screens/
    ├── home/
    │   ├── HomeUiState.kt                 # BudgetCarouselUi, BudgetCardSlotUi, SectionState
    │   ├── HomeUiEvent.kt                 # HomeEntered, PagerSettled, CardActivated, BudgetSeeAllTapped
    │   ├── HomeUiEffect.kt                # NavigateToBudget(cycleId, mode), NavigateToBudgetSetup(target)
    │   ├── HomeViewModel.kt               # Xử lý luồng Carousel, snapshot observer, offline check
    │   ├── HomeScreen.kt                  # Bố cục Header Section "Xem tất cả" + Carousel
    │   └── components/
    │       ├── BudgetCreditCardCarousel.kt # HorizontalPager quản lý 1..3 slots
    │       ├── BudgetCreditCard.kt        # Card thực tế (Previous/Current/Next-configured)
    │       └── BudgetPlaceholderCard.kt   # Thẻ trung tính viền đứt nét + icon '+'
    └── budget/
        ├── BudgetRoute.kt                 # Mở rộng đối số cycleId & mode (ReadOnly, Active, SetupNext)
        └── BudgetViewModel.kt             # Kết nối với FakeBudgetRepository dùng chung
```

---

## 5. Code Style (Chuẩn viết Code & Triển khai Mẫu)

Tuân thủ nghiêm ngặt `CODING_STANDARDS.md`: Không hardcode Color, không hardcode String tiếng Việt, Composable stateless có `modifier: Modifier = Modifier` ở đầu danh sách tham số tùy chọn.

### Ví dụ mẫu chuẩn (Pattern Example):

```kotlin
/**
 * Thẻ Ngân Sách Chu Kỳ Thuộc Carousel (Home v2.2).
 */
@Composable
fun BudgetCreditCard(
    uiModel: BudgetCardSlotUi.ActiveCycle,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientBrush = remember(uiModel.themeOrdinal) {
        CandyBudgetThemes.getGradientBrush(uiModel.themeOrdinal)
    }

    Surface(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 196.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBrush)
                .padding(CandySpace.lg)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                // Header thẻ: Tên kỳ + Badge trạng thái
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiModel.cycleLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    BudgetCycleStatusBadge(status = uiModel.cycleStatus)
                }

                // Số tiền & Cảnh báo Coral khi vượt hạn mức
                if (uiModel.isOverBudget) {
                    Text(
                        text = stringResource(
                            R.string.home_budget_over_amount_format,
                            uiModel.overAmountFormatted
                        ),
                        color = CandyColors.HomeV2.coral,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tiến trình: Clamped 0f..1f, nhãn hiển thị % thật
                BudgetProgressRow(
                    clampedProgress = uiModel.clampedProgress,
                    realPercentText = uiModel.realPercentText
                )
            }
        }
    }
}
```

---

## 6. Testing Strategy (Chiến lược Kiểm thử)

| Tầng kiểm thử | Đối tượng kiểm thử | Phạm vi ca kiểm thử |
| :--- | :--- | :--- |
| **Domain Unit Tests** | `BudgetCalculationTest`, `BudgetCycleTest` | - Ranh giới ngày neo: tháng 28, 29, 30, 31 ngày; năm nhuận.<br>- Phép tính Pay Now & Credit cùng cộng vào chi tiêu theo ngày mua.<br>- Tính toán phần trăm vượt mức (ví dụ: `110%`) và clamp thanh ở `1.0f`.<br>- Ánh xạ 12 themes qua `floorMod(ordinal, 12)` (chu kỳ 0 và 12 cùng theme). |
| **Repository Tests** | `FakeBudgetRepositoryTest` | - Cửa sổ `TriCycleWindow`: trường hợp đủ 3 kỳ, trường hợp tài khoản mới không có `Previous`, trường hợp `Next` đã cấu hình và chưa cấu hình.<br>- Thiết lập kỳ sau lưu thành công và bảo toàn khi chuyển kỳ (rollover).<br>- Giả lập trạng thái offline có cache và offline thiếu cache. |
| **ViewModel Tests** | `HomeViewModelTest` | - Khởi tạo hoặc vào lại Home (`HomeEntered`): Pager luôn chọn `Current`.<br>- Lỗi khu vực Budget không xóa dữ liệu Journey hoặc Header.<br>- Sự kiện `BudgetSeeAllTapped` phát effect mở màn hình lịch sử.<br>- Thao tác chạm thẻ phát navigation đúng `cycleId` và `mode`. |
| **Compose UI Tests** | `BudgetCreditCardCarouselTest` | - Pager cuộn tối đa 1 trang mỗi cử chỉ vuốt.<br>- TalkBack đọc đầy đủ nhãn ngữ cảnh và số liệu theo thứ tự chuẩn.<br>- Thẻ Placeholder hiển thị đúng viền nét đứt và nút `+`. |

---

## 7. Boundaries (Ranh giới hành vi của Agent)

- **ALWAYS (Bắt buộc luôn làm):**
  - Giữ nguyên các comment và docstring hiện hữu không liên quan đến thay đổi.
  - Sử dụng String resource tiếng Việt trong `res/values/strings.xml` cho mọi nhãn hiển thị mới.
  - Sử dụng Token màu tập trung trong `Color.kt` / `CandyPalette`.
  - Chạy `testDebugUnitTest` và `assembleDebug` để kiểm chứng mã nguồn biên dịch sạch.
- **ASK FIRST (Phải hỏi người dùng trước khi làm):**
  - Thêm bất kỳ thư viện bên thứ ba (dependency) mới nào vào `build.gradle.kts`.
  - Thay đổi kiến trúc điều hướng toàn cục ngoài phạm vi các route liên quan đến Home và Budget.
- **NEVER (Tuyệt đối không làm):**
  - Không cài đặt hoặc khởi chạy app trên máy ảo (AVD/Emulator) hay thiết bị thật.
  - Không tạo Room Database Entity, Room Migration hay chỉnh sửa tệp SQLite trong giai đoạn này.
  - Không cho phép người dùng ghi dữ liệu / thiết lập ngân sách khi ngoại tuyến (chặn từ UI và thông báo cần mạng).
  - Không xoá hoặc làm yếu đi các bài unit test hiện hữu để đối phó pass build.

---

## 8. Success Criteria (24 Tiêu chí nghiệm thu BC22-01..BC22-24)

- [ ] **BC22-01:** Carousel hiển thị tối đa 3 vị trí: Previous (nếu có), Current, Next.
- [ ] **BC22-02:** Khi mở app hoặc quay lại màn Home, carousel luôn snap về vị trí thẻ Current.
- [ ] **BC22-03:** Hướng vuốt đúng chuẩn: vuốt phải về Previous, vuốt trái sang Next; cuộn tối đa 1 trang mỗi lần vuốt.
- [ ] **BC22-04:** Nếu tài khoản mới chưa có chu kỳ quá khứ, slot Previous tự động bị loại bỏ, pager chỉ còn 2 slot `[Current, Next]`.
- [ ] **BC22-05:** Khi Next chưa được cấu hình, hiển thị thẻ Placeholder trung tính (nền trắng ngà/xám pastel, viền nét đứt, icon `+`).
- [ ] **BC22-06:** Thao tác vuốt qua thẻ Placeholder không tự động phát sinh ghi dữ liệu hay tạo chu kỳ rác.
- [ ] **BC22-07:** Sau khi thiết lập ngân sách kỳ sau thành công, thẻ Next chuyển sang dạng hiển thị hạn mức với theme màu thật của kỳ đó.
- [ ] **BC22-08:** Có đầy đủ 12 theme Candy Gradient được gán theo `floorMod(ordinal, 12)`.
- [ ] **BC22-09:** Màu nhận diện của một chu kỳ giữ nguyên bất biến khi chu kỳ chuyển dịch từ Sắp tới $\to$ Đang diễn ra $\to$ Đã kết thúc.
- [ ] **BC22-10:** Thẻ Previous ở trạng thái chỉ đọc (read-only) và giữ nguyên màu gradient gốc của kỳ đó.
- [ ] **BC22-11:** Số liệu trên thẻ Current phản ánh chính xác tổng chi tiêu của chu kỳ hiện tại (hợp nhất cả Pay Now và Credit theo ngày mua).
- [ ] **BC22-12:** Thẻ Next không hiển thị số tiền đã chi hay thanh tiến trình giả định.
- [ ] **BC22-13:** Khi vượt ngân sách (`spent > limit`), thanh tiến trình clamp ở 100%, nhãn hiển thị % thực tế (ví dụ: `110%`), chữ cảnh báo dùng màu Coral trên nền gradient của chu kỳ.
- [ ] **BC22-14:** Nút "Xem tất cả" nằm ở Header của section Budget, bấm vào mở danh sách đầy đủ lịch sử các chu kỳ trên Budget.
- [ ] **BC22-15:** Thao tác vuốt ngang carousel không kích hoạt sự kiện click thẻ và không làm gián đoạn cuộn dọc của màn hình Home.
- [ ] **BC22-16:** Vị trí neo cuộn dọc của màn Home được bảo toàn khi người dùng chuyển đổi giữa các thẻ trong carousel.
- [ ] **BC22-17:** Khi mất mạng nhưng đã có dữ liệu cache, carousel cho phép xem các thẻ kèm chú thích thời điểm đồng bộ; không tự quy đổi thành 0 đ.
- [ ] **BC22-18:** Khi mất mạng và chạm vào thiết lập kỳ sau, ứng dụng hiển thị thông báo cần kết nối mạng và chặn thao tác ghi.
- [ ] **BC22-19:** Cơ chế chuyển chu kỳ (rollover) đảm bảo tính duy nhất của chu kỳ Active, không có khoảng ngày chồng lấn.
- [ ] **BC22-20:** Ngân sách được cấu hình trước cho kỳ tiếp theo tự động kích hoạt khi sang kỳ mới mà không làm thay đổi số dư hay chi tiêu của kỳ cũ.
- [ ] **BC22-21:** Hỗ trợ TalkBack đầy đủ: đọc vai trò thẻ, khoảng ngày, limit $\to$ spent $\to$ còn/vượt $\to$ tỷ lệ %, và chỉ báo trang hiện tại/tổng trang.
- [ ] **BC22-22:** Đảm bảo chuẩn tiếp cận: Touch target tối thiểu 48dp, hỗ trợ phóng to chữ `fontScale = 2.0f`, hỗ trợ `reduced motion`.
- [ ] **BC22-23:** Lỗi phát sinh tại khu vực Budget (nếu có) được cô lập hoàn toàn, không gây ảnh hưởng đến Header hay Item Journey.
- [ ] **BC22-24:** Kiểm tra tích hợp và hồi quy: Luồng mở `BudgetScreen`, thiết lập hạn mức và quay lại Home hoạt động mượt mà, phản ánh dữ liệu tức thì.

---

## 9. Open Questions (Các câu hỏi còn mở)

*Hiện tại không còn câu hỏi chặn. Tất cả các quyết định phạm vi (Fake-First, Section Header "Xem tất cả", Vượt ngân sách 100% clamping + real %, và 12 Theme Ordinal) đã được người dùng chốt đầy đủ.*
