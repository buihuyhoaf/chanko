# Specification: Màn hình Home (Trang chủ) — Chanko

> **Trạng thái:** APPROVED (Phase 1: Đã được người dùng phê duyệt vào 20/10/2026)  
> **Nhánh thực hiện:** `feature/home-screen` (tách từ `dev-chanko-1x`)  
> **Tài liệu tham chiếu:** `UI-HANDOFF.md` (bản chốt v5 tiếng Việt, 20/10/2026), `tokens.json`, `approved-home-v5.png`, `asset-manifest.json`  
> **Kiến trúc áp dụng:** Clean Architecture, MVVM, Jetpack Compose, Material 3, Hilt

---

## 1. Assumptions (Giả định triển khai)

1. **Nền tảng & Công nghệ:** Ứng dụng Android thuần (Native Kotlin), Jetpack Compose với Material 3, hỗ trợ Dark/Light Theme (MVP tối ưu hiển thị Light scheme theo Candy Pop 3D).
2. **Ngôn ngữ:** 100% tiếng Việt trên toàn bộ giao diện, định dạng số tiền VND chuẩn `#,### ₫` (ví dụ: `3.000.000 ₫`), không bao giờ cắt ngắn (ellipsis) số tiền.
3. **Phạm vi hiển thị & Dữ liệu:**
   - Màn hình Home được tích hợp làm màn hình khởi đầu của `MainNavigation` tại tab `TopLevelDestination.Home`.
   - Các danh sách "Sẵn sàng xem lại" (Ready for review) và "Đang cân nhắc" (Cooling-off) lấy dữ liệu thời gian thực từ `ItemRepository` (`CooldownItem` có trạng thái `COOLING_DOWN` và kiểm tra `canDecide(now)`).
   - Phần chu kỳ chi tiêu (Spending Cycle & Calendar) lấy ngày bắt đầu kỳ từ cấu hình ngân sách (`anchorDay`, ví dụ: ngày 15). Dữ liệu giao dịch từng ngày trong lịch ở giai đoạn này sẽ sử dụng Presentation Model / Mock Data Provider có thể tích hợp mượt mà với Room/Remote DataSource khi các API giao dịch hoàn thiện.
4. **Không phụ thuộc thư viện Lịch bên thứ ba:** Lưới lịch chu kỳ (Cycle Calendar) sẽ được tự viết bằng Compose thuần (`LazyColumn` / `Column` / `Row` / `Layout`) để đảm bảo linh hoạt: chu kỳ bắt đầu theo ngày neo (`anchorDay`), thứ bắt đầu tính từ ngày neo, không ép theo lịch tháng thông thường bắt đầu từ Thứ Hai.
5. **Đồ họa & Asset:** Các asset từ thư mục handoff (`mascot_crossed_arms.png`, `ready_open_door.png`, `cooling_snowflake.png`, cùng các ảnh sản phẩm mẫu) sẽ được đặt tại `app/src/main/res/drawable-nodpi/` để giữ nguyên tỷ lệ và alpha trong suốt. Bong bóng lời thoại của mèo được vẽ bằng Compose UI vector và text thật, không dùng ảnh tĩnh.
6. **Không cài đặt Emulator/Thiết bị:** Tuân thủ quy định dự án, chỉ build và chạy unit tests (`./gradlew testDebugUnitTest`, `./gradlew assembleDebug`), không chạy adb hay AVD.

---

## 2. Objective (Mục tiêu)

Xây dựng và tích hợp hoàn chỉnh màn hình Trang chủ (`HomeScreen`) của Chanko theo đúng thiết kế chốt v5 tiếng Việt:
- Giúp người dùng nắm bắt nhanh chóng tình hình ngân sách chu kỳ hiện tại (Đã tiêu, Ngân sách, Còn lại).
- Cung cấp lịch chi tiêu tương tác theo chu kỳ thực tế: xem nhanh ngày bắt đầu, ngày hôm nay, ngày đã chọn và các ngày phát sinh chi tiêu.
- Hiển thị phản hồi cảm xúc và lời khuyên từ linh vật mèo Chanko qua bong bóng thoại.
- Giúp người dùng quản lý các món đồ mua sắm bốc đồng:
  - Mục **Sẵn sàng xem lại** (Ready for review): Những món đã hết thời gian cooling-off, sẵn sàng đưa ra quyết định mua hay dừng.
  - Mục **Đang cân nhắc** (Cooling-off): Những món đang đếm ngược thời gian chờ suy nghĩ với timer trực quan.
- Tùy biến thanh điều hướng dưới cùng (`BottomBar`) với 4 tab chính và nút hành động thêm món đồ (+) nổi bật ở chính giữa.

---

## 3. Tech Stack & Dependencies

- **Ngôn ngữ:** Kotlin 2.x
- **UI Framework:** Jetpack Compose (BOM hiện hành), Material 3
- **Kiến trúc:** Clean Architecture + MVVM
- **State Management:** `StateFlow`, `collectAsStateWithLifecycle()`
- **DI:** Hilt (`@HiltViewModel`, `@Inject`)
- **Typography & Resource:** Nunito font family (weights 400, 500, 600, 700, 800), Material Symbols Rounded Icons.
- **Unit Testing:** JUnit 4, Kotlinx Coroutines Test, Turbine (nếu có).

---

## 4. Commands (Lệnh thực thi)

Toàn bộ lệnh thực thi qua terminal cục bộ với `JAVA_HOME` đã xác định:

- **Build mã nguồn:**  
  `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew assembleDebug`
- **Chạy Unit Tests:**  
  `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew testDebugUnitTest`
- **Kiểm tra Lint:**  
  `JAVA_HOME=/Users/buihoa/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home ./gradlew lintDebug`

---

## 5. Project Structure (Cấu trúc thư mục)

```
app/src/main/
├── res/
│   ├── drawable-nodpi/
│   │   ├── mascot_crossed_arms.png
│   │   ├── ready_open_door.png
│   │   ├── cooling_snowflake.png
│   │   ├── sample_jacket.png
│   │   ├── sample_headphones.png
│   │   └── sample_keyboard.png
│   └── values/
│       └── strings_home.xml (toàn bộ nhãn, tiêu đề, accessibility text tiếng Việt)
└── java/com/hoabh/chanko/
    ├── presentation/
    │   ├── navigation/
    │   │   ├── MainNavigation.kt        (Cập nhật BottomBar custom 4 tab + nút + trung tâm)
    │   │   └── Screen.kt
    │   └── screens/home/
    │       ├── HomeScreen.kt            (Tách biệt HomeRoute stateful & HomeScreen stateless)
    │       ├── HomeViewModel.kt         (Quản lý HomeUiState, xử lý logic chu kỳ, lọc items)
    │       ├── HomeUiState.kt           (Data classes và sealed interfaces cho UI state)
    │       ├── HomeUiEvent.kt           (Sealed interface cho các user actions)
    │       ├── model/
    │       │   ├── SpendingCycle.kt     (Mô hình tính toán chu kỳ, ngày bắt đầu/kết thúc)
    │       │   └── CalendarDayState.kt  (Trạng thái từng ô ngày: Normal, Start, Today, Selected, Purchases)
    │       └── components/
    │           ├── HomeHeader.kt        (Lời chào theo thời gian, gear cài đặt, avatar người dùng)
    │           ├── SpendingCalendarCard.kt (Container card lịch, nút Hôm nay, chi tiết ngày chọn)
    │           ├── CycleCalendarGrid.kt (Lưới ngày 7 cột bắt đầu theo ngày neo, legend)
    │           ├── MascotSpeechBanner.kt(Ảnh mèo mascot 3D và bong bóng thoại vẽ bằng Compose)
    │           ├── BudgetSummaryBar.kt  (3 cột số tiền: Ngân sách, Đã tiêu, Còn lại + thanh tiến độ)
    │           ├── ReadyReviewSection.kt(Header cửa mở + Card món đồ sẵn sàng xem lại + CTA button)
    │           └── CoolingOffSection.kt (Header bông tuyết + Danh sách card món đồ đang đếm ngược)
```

---

## 6. Design Tokens & Styling (Bộ thông số thiết kế v5)

### 6.1 Bảng màu (Color Tokens)
- `brandPurple`: `#8965F6` (Nút +, chỉ số hôm nay, progress bar fill, avatar)
- `brandPink`: `#FF85B3` (Viền ngày chọn, dot ngày mua hàng)
- `background`: `#FAF8F5` (Nền chính toàn màn hình)
- `backgroundLavender`: `#F0E7FF` (Quầng tím góc nền)
- `surface`: `#FFFFFF` (Nền bottom bar, dialogs, item cards)
- `calendarSurface`: `#F4EEFF` (Nền container thẻ lịch)
- `calendarInnerSurface`: `#FAF7FF` (Mặt lưới ngày)
- `primarySoft`: `#EBE1FF` (Nền nút "Hôm nay", tab Trang chủ đang active)
- `primaryInk`: `#5731C6` (Chữ/icon tím đậm trên nền sáng, chữ số tiền còn lại)
- `textPrimary`: `#151329` (Tiêu đề chính, giá tiền)
- `textSecondary`: `#585C82` (Mô tả, nhãn phụ, tên thứ)
- `divider`: `#E4DDF2` (Đường phân cách mềm trong lịch)
- `progressTrack`: `#E3DEEC` (Rãnh thanh tiến độ ngân sách)
- `readySurface`: `#D9FAE6` → `readySurfaceEnd`: `#CFF6DF` (Gradient thẻ Sẵn sàng xem lại)
- `readyAction`: `#00634F` / `onReadyAction`: `#FFFFFF` (Nút "Xem lại")
- `readyInk`: `#075C48` / `readyChip`: `#E6FFF0` (Chip "Đã hết thời gian chờ")
- `coolingBlue`: `#E0EBFF` / `coolingLavender`: `#E7E1FF` (Nền thẻ cooling-off)
- `coolingChip`: `#D3D9FF` (Chip đếm ngược thời gian)
- `speechSurface`: `#FFECE5` (Nền bong bóng thoại của mèo)

### 6.2 Typography Scale (Nunito)
- **Greeting / Tiêu đề:** 20sp–22sp, line-height 28sp, FontWeight 800 (ExtraBold).
- **Subheader / Tháng / Số tiền lớn:** 18sp–22sp, line-height 24–28sp, FontWeight 800.
- **Tên item / Giá item:** 16sp, line-height 22sp, FontWeight 800 (tên) & 600 (giá).
- **Body / Thứ / Nhãn phụ:** 14sp, line-height 20sp, FontWeight 500–600.
- **Timer / Chip trạng thái / Legend:** 12sp, line-height 18sp, FontWeight 600.
- **Nút bấm hành động:** 14sp, line-height 20sp, FontWeight 800.
- **Bottom Navigation:** 11–12sp, line-height 16sp, FontWeight 600–700.

### 6.3 Kích thước & Khoảng cách (Layout & Geometry)
- **Base Width:** 393dp chuẩn (hỗ trợ co giãn mượt từ 320dp đến màn hình lớn).
- **Spacing scale:** 4dp, 8dp, 12dp, 16dp, 20dp, 24dp, 32dp. Gutter 16dp.
- **Bo góc (Corner Radius):**
  - Container lịch: 24dp
  - Item cards: 20dp
  - Thumbnail ảnh món đồ: 14dp (kích thước 60–64dp)
  - Ô ngày trên lưới: 12dp
  - Bottom bar: 24dp (phía trên)
  - Action button / Pill chips: RoundedCornerShape(percent = 50) (full pill)
- **Vùng chạm tối thiểu (Touch Target):** Mọi nút bấm, ô ngày, tab, icon đều đạt tối thiểu 48dp x 48dp.
- **Nút Thêm (+) trung tâm:** Kích thước 56dp, icon 28dp, nằm chính giữa khoảng hở 64–72dp giữa 2 cặp tab bottom bar.

---

## 7. Code Style & Example Implementation

Mã nguồn phải tuân thủ nghiêm ngặt các quy tắc trong [CODING_STANDARDS.md](file:///Users/buihoa/AndroidStudioProjects/chanko/CODING_STANDARDS.md):
- Tách biệt Stateful Route (`HomeRoute`) và Stateless Screen (`HomeScreen`).
- Mọi chuỗi hiển thị đều qua `stringResource(R.string.*)`.
- Tham số `modifier: Modifier = Modifier` ở đầu các optional parameters.
- Khai báo state bất biến (`val`), không dùng `var` trong model.

### Đoạn mã mẫu minh họa phong cách (Code Style Sample):

```kotlin
@Composable
fun ReadyReviewItemCard(
    item: CooldownItem,
    onReviewClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 88.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD9FAE6) // Hoặc Brush gradient sang #CFF6DF
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail sản phẩm
            AsyncOrLocalImage(
                imageUri = item.imageUri,
                fallbackDrawable = R.drawable.sample_jacket,
                contentDescription = item.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.width(CandySpace.md))

            // Thông tin & Chip trạng thái
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight(800)),
                    color = Color(0xFF151329),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.price.toVndCurrency(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                    color = Color(0xFF151329)
                )
                Spacer(modifier = Modifier.height(CandySpace.xs))
                StatusChip(
                    text = stringResource(R.string.home_ready_chip_label),
                    containerColor = Color(0xFFE6FFF0),
                    contentColor = Color(0xFF075C48),
                    icon = Icons.Rounded.Check
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.sm))

            // Nút hành động chính
            Button(
                onClick = { onReviewClick(item.id) },
                modifier = Modifier
                    .defaultMinSize(minWidth = 80.dp, minHeight = 48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00634F),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = CandySpace.lg, vertical = CandySpace.sm)
            ) {
                Text(
                    text = stringResource(R.string.home_btn_review),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight(800))
                )
                Spacer(modifier = Modifier.width(CandySpace.xs))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
```

---

## 8. Testing Strategy (Chiến lược kiểm thử)

1. **Unit Tests (`HomeViewModelTest.kt`):**
   - **Tính toán Chu kỳ chi tiêu:**
     - Kiểm tra chu kỳ bắt đầu ngày 15: Xác nhận ngày đầu chu kỳ, ngày cuối chu kỳ (ngày 14 tháng sau).
     - Kiểm tra các trường hợp ngày đặc biệt (tháng 28/29/30/31 ngày).
     - Xác nhận thứ bắt đầu lưới lịch tương ứng với thứ của ngày neo (ví dụ: ngày 15 là Thứ Năm -> cột đầu tiên của lưới là T5).
   - **Tỉ lệ ngân sách & Tiến độ:**
     - Đã tiêu: 2.400.000 ₫, Ngân sách: 3.000.000 ₫ -> tiến độ đúng 80%.
     - Kiểm tra an toàn khi Ngân sách = 0 (tránh ngoại lệ chia cho 0).
     - Kiểm tra trường hợp chi tiêu vượt ngân sách (Số tiền còn lại âm hiển thị rõ ràng, không bị ép về 0).
   - **Phân loại Món đồ:**
     - Danh sách `readyItems`: Các món có `canDecide(now) == true`.
     - Danh sách `coolingItems`: Các món có `remainingMillis(now) > 0`.
   - **Tương tác UI Event:**
     - Nhấn chọn ngày: Cập nhật `selectedDate` và thông tin chi tiêu ngày đó.
     - Nhấn nút "Hôm nay": Tự động focus về ngày hiện tại.

2. **Compose Visual & Verification:**
   - Cung cấp `@Preview` đầy đủ cho `HomeScreen`: trạng thái có dữ liệu, trạng thái rỗng (empty ready/cooling), font scale 100% và 150%.
   - Chạy lệnh `./gradlew testDebugUnitTest` và `./gradlew assembleDebug` để bảo đảm không có lỗi biên dịch runtime.

---

## 9. Boundaries (Ranh giới thực thi)

- **Always (Luôn luôn thực hiện):**
  - Tuân thủ toàn bộ quy định trong `AGENTS.md` và `CODING_STANDARDS.md`.
  - Sử dụng toàn bộ chuỗi tiếng Việt trích xuất trong `res/values/strings_home.xml` (hoặc `strings.xml`).
  - Kiểm tra kích thước touch target đạt tối thiểu 48dp x 48dp.
  - Hỗ trợ responsive tốt trên màn hình hẹp (320dp) và chuẩn (393dp).
  - Đảm bảo tương phản màu chữ/nền đạt chuẩn WCAG 2.2 AA.
  - Chạy build và unit test kiểm chứng sau khi hoàn tất.

- **Ask first (Hỏi trước khi làm):**
  - Thêm bất kỳ thư viện ngoài mới nào vào `build.gradle.kts`.
  - Thay đổi cấu trúc cơ sở dữ liệu Room hay các interface của tầng `core/domain`.
  - Quyết định cách thức lưu trữ lịch sử giao dịch từng ngày (dùng Mock data provider hay bổ sung schema Database mới).

- **Never (Tuyệt đối không làm):**
  - Không cài đặt hoặc khởi chạy ứng dụng trên máy ảo/Android emulator hoặc thiết bị thật (`adb install`, `./gradlew installDebug`).
  - Không chỉnh sửa, di chuyển hay xóa bất kỳ agent skill nào trong `.agent/skills/`.
  - Không hardcode các giá trị chuỗi tiếng Việt hoặc mã màu hex trực tiếp trong file Compose.
  - Không hardcode ngày cố định (như 19/10/2026) vào logic xử lý của ứng dụng; luôn lấy ngày thực tế từ hệ thống hoặc state.
  - Không cắt ngắn (ellipsis) số tiền; luôn định dạng đầy đủ.
  - Không gom tab (+) vào hệ thống tab điều hướng thông thường (nút + là action mở modal thêm món đồ).

---

## 10. Success Criteria (Tiêu chí nghiệm thu)

1. **Hiển thị đầy đủ các khối nội dung UI:**
   - [ ] Top Header với câu chào linh hoạt, icon Cài đặt và Avatar người dùng.
   - [ ] Card "Lịch chi tiêu" bo góc 24dp chứa: tiêu đề, khoảng thời gian chu kỳ, nút "Hôm nay", lưới ngày theo thứ bắt đầu từ ngày neo, chỉ báo dot mua sắm, trạng thái ngày được chọn, ngày hôm nay.
   - [ ] Khối linh vật Mèo Chanko 3D kèm bong bóng thoại chứa lời khuyên tài chính theo tiến độ ngân sách.
   - [ ] Khối thống kê ngân sách 3 cột (Ngân sách, Đã tiêu, Còn lại) + thanh progress bar + ngày bắt đầu kỳ mới.
   - [ ] Khối "Sẵn sàng xem lại" với icon cửa mở 3D, số lượng món, danh sách card màu mint kèm nút "Xem lại" (≥48dp).
   - [ ] Khối "Đang cân nhắc" với icon bông tuyết 3D, số lượng món, danh sách card màu xanh nhạt kèm chip timer đếm ngược trực quan.
   - [ ] Custom `BottomBar` 4 tab (Trang chủ, Món đồ, Ngân sách, Phân tích) + Nút (+) nổi bật ở trung tâm.
2. **Hành vi tương tác (Interactions):**
   - [ ] Nhấn chọn từng ngày trên lưới lịch cập nhật chi tiết số giao dịch và số tiền của ngày đó.
   - [ ] Nhấn nút "Hôm nay" đưa ngày được chọn về ngày hiện tại.
   - [ ] Nhấn nút "Xem lại" hoặc card item kích hoạt callback tương ứng với item ID.
   - [ ] Nhấn nút (+) kích hoạt sự kiện thêm món đồ.
3. **Kỹ thuật & Kiến trúc:**
   - [ ] Toàn bộ chuỗi hiển thị lưu tại `strings_home.xml` (hoặc `strings.xml`), không hardcode string.
   - [ ] Mọi màu sắc và token ánh xạ từ `tokens.json` và hệ thống design tokens có sẵn.
   - [ ] Các asset PNG từ handoff được đặt đúng tại `drawable-nodpi`.
   - [ ] Unit tests cho `HomeViewModel` và các bộ tính toán chu kỳ/ngân sách chạy pass 100%.
   - [ ] Lệnh `./gradlew assembleDebug` và `./gradlew testDebugUnitTest` biên dịch và thực thi thành công.

---

## 11. Open Questions (Câu hỏi cần làm rõ trước khi lập kế hoạch)

1. **Dữ liệu giao dịch từng ngày trên Lịch (Daily Purchases):**  
   Hiện tại hệ thống `core/domain` mới có `ItemRepository` quản lý `CooldownItem` (chưa có repository riêng cho các khoản chi tiêu hàng ngày / purchases by date).  
   *Đề xuất:* Ở giai đoạn hiện tại, màn hình Home sẽ sử dụng một `HomeSpendingRepository` hoặc Mock Provider nội bộ để cung cấp chu kỳ mẫu (như ngày neo 15, các giao dịch mẫu trong kỳ giống handoff) song song với việc nạp dữ liệu thực tế từ `ItemRepository` cho 2 danh sách món đồ. Người dùng có đồng ý với phương án này không?

2. **Hành vi khi nhấn nút (+):**  
   Nút (+) ở giữa Bottom Bar có hành vi dự kiến là mở màn hình/bottom sheet thêm món đồ mới (`AddItem`). Hiện tại dự án đã có màn hình Add Item chưa hay chỉ cần emit event callback `onAddNewItemClick: () -> Unit`?

3. **Nhánh Git (Git Branch):**  
   Hiện tại repository đang ở nhánh `feature/remove-overview-viewmodel` với một số thay đổi dọn dẹp chưa commit. Sau khi spec được duyệt, bạn muốn commit hoàn tất nhánh hiện tại trước, hay tạo một nhánh mới từ `dev-chanko-1x` (ví dụ: `feature/home-screen`) cho tính năng Home Screen?
