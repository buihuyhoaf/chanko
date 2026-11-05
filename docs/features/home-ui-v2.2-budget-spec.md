# Spec: Chanko Home v2.2 (Tri-Cycle Budget Carousel & Budget Integration)

> **Capability Map tham chiếu:** [SPEC-home-ui-v2.2-budget.md](../../SPEC-home-ui-v2.2-budget.md)  
> **Nguồn chuẩn (Authoritative Sources):**
> - `Chanko-Home-v2.2 2/CHANKO-HOME-v2.2-HANDOFF.md` (Bàn giao tổng hợp UX, Technical, Accessibility & QA ngày 05/11/2026)
> - `Chanko-Home-v2.2 2/tokens/design-tokens.json` (Hệ thống Design Tokens chính thức: 12 Gradient Themes, Layout, Semantic)
> - `Chanko-Home-v2.2 2/STATE-MATRIX.csv` (Ma trận 19 trạng thái S01..S19) & `NAVIGATION-CONTRACT.csv` (13 quy tắc điều hướng N01..N13)
> - `Chanko-Home-v2.2 2/reference/approved-preview.png` (Visual design source of truth đã duyệt)
> - `Chanko-Home-v2.2-Implementation.docx` (Bản chốt kỹ thuật ban đầu ngày 04/11/2026)
> - `CODING_STANDARDS.md` (Quy chuẩn kiến trúc và Compose của dự án)
> - Quyết định kiến trúc dữ liệu: **Fake-First** (dùng chung `FakeBudgetRepository` trong Phase hiện tại; Room Entity, DAO và migration được quy hoạch cho Phase tích hợp dữ liệu thật tiếp theo).

---

## 0. Assumptions (Các giả định & Quyết định thẩm quyền đã chốt)

1. **Nguồn dữ liệu (Fake-First Architecture):**
   - Không tạo Room Entity, DAO hay Database Migration trong giai đoạn này.
   - Cả `HomeScreen` và `BudgetScreen` cùng quan sát một thể hiện dùng chung (shared singleton/instance) của `BudgetRepository` (thông qua `FakeBudgetRepository`) để đảm bảo: Thiết lập hạn mức kỳ sau $\to$ quay lại Home thấy ngay cập nhật; chuyển chu kỳ (rollover) giữ nguyên identity và theme.
   - Các hợp đồng dữ liệu cho Room Entity, DAO và Database Migration được đặc tả sẵn cho giai đoạn kết nối backend/DB thật.
2. **Quyết định `BC-CARD-LABEL-01` (Month Chip `MM` trên TopBar thẻ):**
   - Mỗi thẻ đã xác định chu kỳ hiển thị một Month Chip pill chứa 2 chữ số tháng `MM` (`01`..`12`) ở góc trên bên phải (ví dụ: `09`, `10`, `11`).
   - Năm (`2026`), khoảng ngày đầy đủ (`01/10 – 31/10`), và text badge trạng thái (`ĐANG DIỄN RA`, `ĐÃ KẾT THÚC`, `SẮP TỚI`) được **loại bỏ hoàn toàn** khỏi mặt thẻ (vẫn lưu trong domain data, TalkBack semantics và `BudgetDetailScreen`).
   - Giữ logo thương hiệu `CHANKO` và nhãn `SHOPPING BUDGET` cùng biểu tượng contactless/chip trang trí ở góc trên bên trái.
3. **Quyết định `BC-CARD-CONTENT-01` (Bỏ dòng "Đã chi X / Y" & Chuẩn hóa vai trò nhãn):**
   - Dòng `"Đã chi X / Y"` được **loại bỏ hoàn toàn** khỏi mặt tất cả các thẻ.
   - **Thẻ Previous (`ClosedCard`):** Nhãn chính là `"Còn lại cuối kỳ"`. Hiển thị số dư còn lại cuối kỳ + thanh tiến trình + nhãn tỷ lệ % chi thực tế. Mascot: `R.drawable.mascot_sleepy`.
   - **Thẻ Current (`ActiveCard`):** Nhãn chính là `"Ngân sách còn lại"`. Nếu chi trong hạn mức: số tiền còn lại + thanh tiến trình + tỷ lệ %. Nếu vượt hạn mức: nhãn `"Vượt ngân sách"` với số tiền vượt thực tế, nhãn tỷ lệ % thật > 100%, thanh tiến trình clamp ở 100%, cảnh báo Coral (`#9C2943`), nền thẻ giữ nguyên gradient của chu kỳ. Mascot: `R.drawable.mascot_happy` (hoặc `R.drawable.mascot_worried` khi vượt ngân sách).
   - **Thẻ Next Configured (`UpcomingConfiguredCard`):** Nhãn chính là `"Ngân sách dự kiến"`. Chỉ hiển thị số tiền hạn mức đã lập kế hoạch; **tuyệt đối không hiển thị chi tiêu hay thanh tiến trình giả định**.
   - **Thẻ Next Unconfigured (`BudgetPlaceholderCard`):** Tiêu đề `"Chu kỳ tiếp theo"`, phụ đề `"Chưa thiết lập ngân sách"`. Nút bấm dạng pill `+ Thiết lập ngân sách` (nền tím `#6326A8`, chữ trắng). Góc trên có Month Chip `MM` của kỳ tiếp theo. Nền thẻ trắng ngà/xám pastel (`#FBF9FD`), viền nét đứt native (`dash = 6.dp, gap = 4.dp`, màu `#82758E`).
   - **Thẻ Current Unconfigured:** Nhãn chính `"Chưa thiết lập ngân sách"`, không hiển thị 0 đ hoặc 0% giả định.
4. **Điều khiển Carousel & Nút "Xem tất cả":**
   - Carousel tối đa 3 vị trí: `[Previous? - Current - Next]`. Nếu không có dữ liệu kỳ trước, slot `Previous` bị loại bỏ (carousel còn 2 thẻ `[Current - Next]`).
   - Phía dưới Carousel có thanh điều hướng gồm: Nút mũi tên trái `<`, dải chấm chỉ báo trang (indicator dots, chấm active màu tím `#6326A8`), nút mũi tên phải `>`, và liên kết `"Xem tất cả →"` (mở lịch sử ngân sách đầy đủ trên `BudgetScreen`).
   - Mặc định khi mở hoặc quay lại Home, carousel luôn snap về thẻ `Current`.
5. **Độ tương phản và Kiểu chữ:**
   - Text trên thẻ: màu tối `#241332` trên nền pastel theo `design-tokens.json`. Trên gradient có độ tương phản cao, chữ trắng hoặc badge nền kem (`#FFF8F2`) đảm bảo tỷ lệ tương phản WCAG AA $\ge 4.5:1$.

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
$$\text{themeId} = \text{floorMod}(\text{ordinal}, 12) + 1 \quad (01..12)$$

Bảng 12 Theme theo chuẩn `design-tokens.json`:

| ID | Tên Theme | Color 1 (Start 0.0) | Color 2 (Mid 0.5) | Color 3 (End 1.0) | Foreground Text |
| :---: | :--- | :---: | :---: | :---: | :---: |
| **01** | Tangerine | `#FFD08A` | `#FFA33F` | `#F39350` | `#241332` |
| **02** | Lavender | `#E4D1FF` | `#B78AEF` | `#9C79D7` | `#241332` |
| **03** | Mint | `#CEF8E8` | `#73DEC0` | `#55C8AB` | `#241332` |
| **04** | Strawberry | `#FFD7E9` | `#F79AC3` | `#E47EAF` | `#241332` |
| **05** | Sky | `#D7EEFF` | `#8CCBFA` | `#72B3E4` | `#241332` |
| **06** | Lemon | `#FFF4B2` | `#FFE271` | `#EAC85F` | `#241332` |
| **07** | Peach | `#FFE2CE` | `#FFB599` | `#EEA083` | `#241332` |
| **08** | Grape | `#E0CBFF` | `#B386EC` | `#9E79D9` | `#241332` |
| **09** | Aqua | `#CDF7F3` | `#7ADAD9` | `#5ABBC5` | `#241332` |
| **10** | Rose | `#FFE1E8` | `#F8A5B9` | `#E488A5` | `#241332` |
| **11** | Pistachio | `#F2F8C7` | `#D4E878` | `#B1CB61` | `#241332` |
| **12** | Blueberry | `#DCE5FF` | `#98B4FB` | `#7998E9` | `#241332` |

- **Hướng Gradient:** Linear Gradient `startNormalized: [0, 0]` $\to$ `endNormalized: [1, 1]` qua 3 điểm `[0.0, 0.5, 1.0]`.

### 2.3. Layout & Semantic Tokens

#### Layout Tokens:
- `cardMinHeightDp`: `216.dp`
- `cardRadiusDp`: `28.dp`
- `contentPaddingDp`: `20.dp`
- `pagerSpacingDp`: `16.dp`
- `peekDp`: `16.dp`
- `touchTargetMinDp`: `48.dp`
- `amountSp`: `32.sp`
- `labelSp`: `16.sp`
- `monthSp`: `24.sp`
- `contentMaxWidthDp`: `600.dp`

#### Semantic Tokens:
- `text`: `#241332`
- `onBadge`: `#241332`
- `badgeFill`: `#FFF8F2`
- `warning`: `#9C2943` (Coral đậm cho text/icon cảnh báo vượt mức)
- `neutralFill`: `["#FBF9FD", "#F3EFF7", "#E8E2EF"]`
- `neutralBorder`: `#82758E` (Viền nét đứt cho Placeholder)
- `placeholderText`: `#4C2C70`
- `ctaFill`: `#6326A8` (Màu nền tím cho nút `+ Thiết lập ngân sách`)
- `ctaText`: `#FFFFFF`

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
> **Tuyệt đối không** chạy lệnh cài đặt hay khởi động máy ảo/máy thật (`adb install`, `installDebug`, AVD). Chỉ thực hiện build và unit test trên môi trường hiện tại theo quy định dự án (`AGENTS.md`).

---

## 4. State Matrix & Navigation Contract

### 4.1. Ma trận Trạng thái (Trích xuất từ `STATE-MATRIX.csv`)
Hệ thống xử lý đầy đủ 19 trạng thái vận hành (`S01`..`S19`):

| State ID | Mô tả Trạng thái | Hiển thị Carousel | Quyền thao tác (Permissions) |
| :---: | :--- | :--- | :--- |
| **S01** | Full 3 slots (Prev Closed, Curr Active, Next Setup) | 3 thẻ đầy đủ `[Prev, Curr, Next]` | Prev: Xem chi tiết; Curr: Mở Budget; Next: Xem/sửa kế hoạch |
| **S02** | Full 3 slots (Next chưa thiết lập) | `[Prev, Curr, Placeholder]` | Placeholder: Mở Setup kỳ sau |
| **S03** | New Account (Chưa có Previous) | 2 thẻ `[Curr, Next]` | Không có slot Prev, pager chặn ở biên |
| **S04** | Current vượt ngân sách (`spent > limit`) | Thẻ Curr cảnh báo Coral, clamp 100%, real % | Mở Budget xem chi tiêu vượt |
| **S05** | Current chưa có ngân sách | Thẻ Curr hiển thị Empty state | Nút thiết lập kỳ hiện tại |
| **S06** | Offline có dữ liệu Cache | Hiển thị đầy đủ thẻ kèm badge/chú thích cached | Cho phép xem chỉ đọc; chặn ghi/setup |
| **S07** | Offline thiếu Cache | Báo lỗi ngoại tuyến cục bộ khu vực Budget | Nút "Thử lại", không ảnh hưởng Journey |
| **S08** | Loading lần đầu (Cold Start) | 3 thẻ Skeleton loading với shimmer | Vô hiệu hóa thao tác bấm |
| **S09** | Lỗi đồng bộ một phần | Giữ snapshot hợp lệ gần nhất và báo lỗi nhỏ | Cho phép thao tác trên snapshot cũ |
| **S10..S19** | Rollover chu kỳ, đổi tài khoản, boundary leap, clamp ngày neo 28..31 | Đồng bộ lại cửa sổ, snap Current mới | Giữ scroll dọc và cập nhật mượt mà |

### 4.2. Hợp đồng Điều hướng (Trích xuất từ `NAVIGATION-CONTRACT.csv`)
13 quy tắc điều hướng (`N01`..`N13`):
- **N01:** Chạm thẻ Previous $\to$ Mở `BudgetRoute(cycleId, mode = ReadOnly)`.
- **N02:** Chạm thẻ Current $\to$ Mở `BudgetRoute(cycleId, mode = Active)`.
- **N03:** Chạm thẻ Next đã cấu hình $\to$ Mở `BudgetRoute(cycleId, mode = ViewOrEditPlan)`.
- **N04:** Chạm CTA `+ Thiết lập ngân sách` trên thẻ Next Placeholder $\to$ Mở `BudgetRoute(targetCycle, mode = SetupNext)`.
- **N05:** Chạm liên kết `"Xem tất cả →"` $\to$ Mở `BudgetRoute(mode = History)`.
- **N06..N13:** Chặn double tap khi đang điều hướng; chặn thao tác ghi khi Offline; tự động cập nhật và snap Current khi quay lại Home từ màn Setup.

---

## 5. Project Structure (Cấu trúc Thư mục & Tệp tin)

```
app/src/main/java/com/hoabh/chanko/
├── domain/budget/
│   ├── model/
│   │   ├── BudgetCycle.kt                 # Bổ sung ordinal, status, cycle window, labelMonth
│   │   ├── TriCycleWindow.kt              # Model cửa sổ 3 kỳ (previous, current, next)
│   │   ├── BudgetSnapshot.kt              # Snapshot chi tiêu nguyên tử
│   │   └── BudgetCalculation.kt           # Tính toán chi Pay Now/Credit, clamping, % thực
│   └── repository/
│       └── BudgetRepository.kt            # Contract lấy TriCycleWindow, setup Next, rollover
├── data/budget/
│   └── FakeBudgetRepository.kt            # Shared In-Memory Fake cài đặt đầy đủ logic v2.2
├── ui/theme/
│   ├── Color.kt                           # 12 CandyTheme tokens & Semantic/Neutral tokens
│   └── CandyBudgetThemes.kt               # Mapping floorMod(ordinal, 12) + 1 -> ThemeBrush
└── presentation/screens/
    ├── home/
    │   ├── HomeUiState.kt                 # BudgetCarouselUi, BudgetCardSlotUi, SectionState
    │   ├── HomeUiEvent.kt                 # HomeEntered, PagerSettled, CardActivated, BudgetSeeAllTapped
    │   ├── HomeUiEffect.kt                # NavigateToBudget(cycleId, mode), NavigateToBudgetSetup(target)
    │   ├── HomeViewModel.kt               # Xử lý luồng Carousel, snapshot observer, offline check
    │   ├── HomeScreen.kt                  # Bố cục Header Section "Xem tất cả" + Carousel
    │   └── components/
    │       ├── BudgetCreditCardCarousel.kt # HorizontalPager quản lý 1..3 slots + Bottom Pager Bar
    │       ├── BudgetCreditCard.kt        # Card thực tế (Previous/Current/Next-configured)
    │       └── BudgetPlaceholderCard.kt   # Thẻ trung tính viền đứt nét + icon '+'
    └── budget/
        ├── BudgetRoute.kt                 # Mở rộng đối số cycleId & mode (ReadOnly, Active, SetupNext)
        └── BudgetViewModel.kt             # Kết nối với FakeBudgetRepository dùng chung
```

---

## 6. Testing Strategy (Chiến lược Kiểm thử)

| Tầng kiểm thử | Đối tượng kiểm thử | Phạm vi ca kiểm thử |
| :--- | :--- | :--- |
| **Domain Unit Tests** | `BudgetCalculationTest`, `BudgetCycleTest` | - Ranh giới ngày neo: tháng 28, 29, 30, 31 ngày; năm nhuận.<br>- Phép tính Pay Now & Credit cùng cộng vào chi tiêu theo ngày mua.<br>- Tính toán phần trăm vượt mức (ví dụ: `110%`) và clamp thanh ở `1.0f`.<br>- Ánh xạ 12 themes qua `floorMod(ordinal, 12) + 1` (chu kỳ 0 và 12 cùng theme 01). |
| **Repository Tests** | `FakeBudgetRepositoryTest` | - Cửa sổ `TriCycleWindow`: trường hợp đủ 3 kỳ, trường hợp tài khoản mới không có `Previous`, trường hợp `Next` đã cấu hình và chưa cấu hình.<br>- Thiết lập kỳ sau lưu thành công và bảo toàn khi chuyển kỳ (rollover).<br>- Giả lập trạng thái offline có cache và offline thiếu cache. |
| **ViewModel Tests** | `HomeViewModelTest` | - Khởi tạo hoặc vào lại Home (`HomeEntered`): Pager luôn chọn `Current`.<br>- Lỗi khu vực Budget không xóa dữ liệu Journey hoặc Header.<br>- Sự kiện `BudgetSeeAllTapped` phát effect mở màn hình lịch sử.<br>- Thao tác chạm thẻ phát navigation đúng `cycleId` và `mode`. |
| **Compose UI Tests** | `BudgetCreditCardCarouselTest` | - Pager cuộn tối đa 1 trang mỗi cử chỉ vuốt.<br>- TalkBack đọc đầy đủ nhãn ngữ cảnh và số liệu theo thứ tự chuẩn.<br>- Thẻ Placeholder hiển thị đúng viền nét đứt và nút `+ Thiết lập ngân sách`.<br>- Dải điều hướng bên dưới hiển thị đúng nút `<`, `>`, dots và `"Xem tất cả →"`. |

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
  - Không cài đặt hoặc khởi chạy app trên máy ảo (AVD/Emulator) hay thiết bị thật (`AGENTS.md`).
  - Không tạo Room Database Entity, Room Migration hay chỉnh sửa tệp SQLite trong giai đoạn này.
  - Không cho phép người dùng ghi dữ liệu / thiết lập ngân sách khi ngoại tuyến (chặn từ UI và thông báo cần mạng).
  - Không xoá hoặc làm yếu đi các bài unit test hiện hữu để đối phó pass build.

---

## 8. Success Criteria (24 Tiêu chí nghiệm thu BC22-01..BC22-24)

- [ ] **BC22-01:** Carousel hiển thị tối đa 3 vị trí: Previous (nếu có), Current, Next.
- [ ] **BC22-02:** Khi mở app hoặc quay lại màn Home, carousel luôn snap về vị trí thẻ Current.
- [ ] **BC22-03:** Hướng vuốt đúng chuẩn: vuốt phải về Previous, vuốt trái sang Next; cuộn tối đa 1 trang mỗi lần vuốt.
- [ ] **BC22-04:** Nếu tài khoản mới chưa có chu kỳ quá khứ, slot Previous tự động bị loại bỏ, pager chỉ còn 2 slot `[Current, Next]`.
- [ ] **BC22-05:** Khi Next chưa được cấu hình, hiển thị thẻ Placeholder trung tính (nền trắng ngà/xám pastel, viền nét đứt native, nút `+ Thiết lập ngân sách`).
- [ ] **BC22-06:** Thao tác vuốt qua thẻ Placeholder không tự động phát sinh ghi dữ liệu hay tạo chu kỳ rác.
- [ ] **BC22-07:** Sau khi thiết lập ngân sách kỳ sau thành công, thẻ Next chuyển sang dạng hiển thị hạn mức với theme màu thật của kỳ đó (không có spent hay thanh progress giả).
- [ ] **BC22-08:** Có đầy đủ 12 theme Candy Gradient được gán theo `floorMod(ordinal, 12) + 1`.
- [ ] **BC22-09:** Màu nhận diện của một chu kỳ giữ nguyên bất biến khi chu kỳ chuyển dịch từ Sắp tới $\to$ Đang diễn ra $\to$ Đã kết thúc.
- [ ] **BC22-10:** Thẻ Previous ở trạng thái chỉ đọc (read-only), hiển thị `"Còn lại cuối kỳ"`, nhãn tỷ lệ % thật và giữ nguyên màu gradient gốc của kỳ đó.
- [ ] **BC22-11:** Số liệu trên thẻ Current phản ánh chính xác tổng chi tiêu của chu kỳ hiện tại (hợp nhất cả Pay Now và Credit theo ngày mua).
- [ ] **BC22-12:** Loại bỏ hoàn toàn dòng `"Đã chi X / Y"` trên mặt tất cả các thẻ (`BC-CARD-CONTENT-01`).
- [ ] **BC22-13:** Mặt thẻ hiển thị Month Chip pill `MM` (01..12); loại bỏ năm, khoảng ngày và text badge trạng thái (`BC-CARD-LABEL-01`).
- [ ] **BC22-14:** Khi vượt ngân sách (`spent > limit`), thanh tiến trình clamp ở 100%, nhãn hiển thị % thực tế (ví dụ: `110%`), chữ cảnh báo dùng màu Coral trên nền gradient của chu kỳ.
- [ ] **BC22-15:** Dải điều hướng bên dưới Carousel hiển thị nút `<`, dải chấm chỉ báo, nút `>`, và liên kết `"Xem tất cả →"`.
- [ ] **BC22-16:** Thao tác vuốt ngang carousel không kích hoạt sự kiện click thẻ và không làm gián đoạn cuộn dọc của màn hình Home.
- [ ] **BC22-17:** Khi mất mạng nhưng đã có dữ liệu cache, carousel cho phép xem các thẻ kèm chú thích thời điểm đồng bộ; không tự quy đổi thành 0 đ.
- [ ] **BC22-18:** Khi mất mạng và chạm vào thiết lập kỳ sau, ứng dụng hiển thị thông báo cần kết nối mạng và chặn thao tác ghi.
- [ ] **BC22-19:** Cơ chế chuyển chu kỳ (rollover) đảm bảo tính duy nhất của chu kỳ Active, không có khoảng ngày chồng lấn.
- [ ] **BC22-20:** Ngân sách được cấu hình trước cho kỳ tiếp theo tự động kích hoạt khi sang kỳ mới mà không làm thay đổi số dư hay chi tiêu của kỳ cũ.
- [ ] **BC22-21:** Hỗ trợ TalkBack đầy đủ: đọc vai trò thẻ, khoảng ngày, limit $\to$ spent $\to$ còn/vượt $\to$ tỷ lệ %, và chỉ báo trang hiện tại/tổng trang.
- [ ] **BC22-22:** Đảm bảo chuẩn tiếp cận: Touch target tối thiểu 48dp, hỗ trợ phóng to chữ `fontScale = 2.0f`, hỗ trợ `reduced motion`.
- [ ] **BC22-23:** Lỗi phát sinh tại khu vực Budget (nếu có) được cô lập hoàn toàn, không gây ảnh hưởng đến Header hay Item Journey.
- [ ] **BC22-24:** Kiểm tra tích hợp và hồi quy: Luồng mở `BudgetScreen`, thiết lập hạn mức và quay lại Home hoạt động mượt mà, phản ánh dữ liệu tức thì.

---

## 9. Open Questions & Assumptions (Các điểm xác nhận)

1. **Text Foreground Color trên Gradient:**
   - Trong `design-tokens.json`, foreground token mặc định là màu tối `#241332` trên nền pastel để đạt chuẩn tương phản WCAG AA $\ge 4.5:1$. Trong ảnh `approved-preview.png`, chữ số tiền màu trắng trên nền cam Tangerine. Chúng tôi đề xuất: Trên các theme sáng/pastel dùng `#241332`, trên theme đậm hoặc khi dùng chữ trắng sẽ áp dụng hiệu ứng đổ bóng / viền tương phản cục bộ để vừa thẩm mỹ như bản duyệt vừa đạt chuẩn WCAG.
2. **Mascot trên thẻ:**
   - Previous dùng `mascot_sleepy`, Current dùng `mascot_happy` (hoặc `mascot_worried` khi vượt hạn mức). Placeholder dùng icon `+` trung tính.

*(Tài liệu đã sẵn sàng để người dùng phê duyệt trước khi lập kế hoạch triển khai).*
