# Spec: Chanko Home UI v2.0 (Budget Credit Card & Item Journey)

> **Capability Map tham chiếu:** [SPEC-home-ui-v2.md](../../SPEC-home-ui-v2.md)  
> **Nguồn chuẩn (Authoritative Sources):**
> - `Chanko_Home_v2.0_Handoff/UX-SPEC.md`
> - `Chanko_Home_v2.0_Handoff/ACCEPTANCE-CRITERIA.md` (31 AC: `AC-01`..`AC-31`)
> - `Chanko_Home_v2.0_Handoff/STATE-MATRIX.csv` (23 trạng thái vận hành)
> - `Chanko_Home_v2.0_Handoff/NAVIGATION-CONTRACT.csv` & `NAVIGATION-NOTES.md`
> - `Chanko_Home_v2.0_Handoff/ANDROID-COMPOSE-GUIDE.md`
> - `Chanko_Home_v2.0_Handoff/DESIGN-TOKENS.json` & `DESIGN-TOKENS-NOTES.md`
> - `Chanko_Home_v2.0_Handoff/VISUAL-RECONCILIATION.md` & `reference/home-v2-approved.png`

---

## 0. Assumptions (Các giả định cần xác nhận)

1. **Loại bỏ hoàn toàn thành phần riêng của Home v1.0:** `CompactLivingRoom.kt`, `YourDecisionsSection.kt` và `HomeSheetHost.kt` chỉ dùng tại màn Home v1.0 nên sẽ được thay thế hoàn toàn bằng `BudgetCreditCard.kt` và `ItemJourneySection.kt` theo đúng chỉ dẫn tại `SOURCE-AND-DECISIONS.md` (*"Không kế thừa contract Living Room, + bottom sheet, 4 vật thể hoặc feed actionable... Không còn Living Room, 4 vật thể tương tác hay HomeSheetHost của v1.0"*).
2. **Đích điều hướng khi nhấn giải thích trên `ObservationCard` (`AC-27`, `observation_explain`):** Vì trong `Chanko_Home_v2.0_Handoff/UX-SPEC.md` quy định *"Không còn HomeSheetHost của v1.0"* và *"Đích giải thích theo luồng insight hiện hành, không tự tạo route nghiệp vụ mới"*, cần xác nhận hành vi mong muốn khi người dùng nhấn vào thẻ/nút mũi tên `>` của *Góc nhìn từ Chanko*:
   - **Phương án A (Khuyến nghị):** Mở một hộp thoại/modal giải thích gọn nhẹ (`InsightExplanationDialog`) hoặc chuyển sang tab `Insights` (`TopLevelDestination.Insights`).
   - **Phương án B:** Điều hướng sang tab `Trò chuyện` (`TopLevelDestination.Chat`).
3. **Wording cho trạng thái thiếu dữ liệu giá trên `BudgetCreditCard` (`AC-09`):** Sử dụng đúng chuỗi đề xuất trong `UX-SPEC.md`: `"Chưa đủ dữ liệu giá"` để chú thích khi có món phát sinh chi tiêu nhưng thiếu giá (`priceMinor == null`), tuyệt đối không ngầm cộng là `0đ`.
4. **Nhãn Bottom Navigation tiếng Việt (`VISUAL-RECONCILIATION.md`):** Dù ảnh mockup `home-v2-approved.png` minh họa chữ tiếng Anh (`Home / My Items / Chat / Insights`), ứng dụng giữ nguyên hệ thống localization tiếng Việt hiện hành (`Trang chủ / Món đồ / Trò chuyện / Phân tích`) theo đúng quy định tại `VISUAL-RECONCILIATION.md` mục 4.
5. **Artwork Mascot trong `ItemJourney` (`ASSET-HANDOFF.md`):** Sử dụng asset mascot chuẩn sẵn có của dự án (`R.drawable.img_chanko_mascot` / `mascot-canonical.png`) cho mascot đứng bên phải đường ziczac, và vẽ thẻ `BudgetCreditCard` (nền cam gradient, chip vàng, sóng contactless, mèo trắng ló đầu) bằng Compose Canvas/Vector + drawable chuẩn để sắc nét ở mọi mật độ màn hình và co giãn linh hoạt khi `fontScale = 2.0f`.

---

## 1. Objective (Mục tiêu & Phạm vi)

### 1.1. Tầm nhìn sản phẩm
Nâng cấp màn hình **HomeScreen** từ phiên bản v1.0 (Living Room + Những món cần chú ý) lên **Home v2.0 (Budget Credit Card + Item Journey)** nhằm:
- Giúp người dùng **nắm bắt ngay ngân sách mua sắm còn lại** thông qua chiếc thẻ **3D Candy Orange Budget Credit Card** nổi bật ngay dưới Header.
- Giúp người dùng **nhận diện tối đa 5 món đồ gần đây nhất** dưới dạng **Hành trình món đồ (Item Journey)** gồm các node tròn xếp ziczac trực quan cùng mèo Chanko đồng hành, chạm trực tiếp vào node để mở `ItemDetailScreen(itemId)` tiếp tục cân nhắc mà không qua bottom sheet trung gian.
- Duy trì triết lý cốt lõi của Chanko: **không tạo áp lực mua sắm, không tự động chuyển sang `BUY`**.

### 1.2. Cấu trúc 4 Khu Vực Bắt Buộc (`AC-01`)
Nội dung cuộn trong `LazyColumn` tuân thủ thứ tự bất biến từ trên xuống dưới:
1. **`HomeHeader`**: Lời chào theo buổi + phụ đề *"Hôm nay mình cùng cân nhắc nhé!"* + nút Avatar tròn mở `Profile & Settings` gộp + trạng thái Offline/SyncedAt khi mất mạng (`AC-02`, `AC-20`).
2. **`BudgetCreditCard`**: Thẻ ngân sách 3D Candy Orange — lối vào `BudgetScreen` duy nhất trên Home (`AC-03`..`AC-09`).
3. **`ItemJourneySection` (*"Món đồ gần đây"*)**: Đường ziczac tối đa 5 món đồ mới nhất (sau khi loại `Archived`) + nút *"Xem tất cả ›"* mở `MyItems` mặc định + 1 mascot Chanko bên phải (`AC-10`..`AC-17`).
4. **`ObservationCard` (*"Góc nhìn từ Chanko"* — có điều kiện)**: Tối đa 1 insight từ dữ liệu thực, có nút giải thích và nút ẩn bền vững theo `insightId`; ẩn toàn bộ vùng và khoảng trống nếu không đủ dữ liệu hoặc đã ẩn (`AC-26`, `AC-27`).
- **Bottom Navigation cố định** bên ngoài vùng cuộn: `Trang chủ` / `Món đồ` / `+` (hành động mở `URLInputDialog`) / `Trò chuyện` / `Phân tích` (`AC-28`, `AC-29`).

---

## 2. Tech Stack & Design Tokens

- **Ngôn ngữ & UI:** Kotlin, Jetpack Compose (Material 3), Coroutines + `StateFlow` / `SharedFlow`, Hilt DI.
- **Quản lý màu sắc tập trung (`Color.kt` & `ColorArchitectureTest`):**
  - Mọi mã màu mới trong `DESIGN-TOKENS.json` bắt buộc khai báo trong `CandyPalette` và ánh xạ qua `CandyColors` trong `app/src/main/java/com/hoabh/chanko/ui/theme/Color.kt` (tuyệt đối không viết `Color(0x...)` trực tiếp trong file UI):
    - `candyOrange = #FFB665`, `candyOrangeShadow = #F59145`, `chipGold = #F4CF71`
    - `coral = #B83248` (chỉ dùng cho số vượt / cảnh báo khi `spent > limit`, giữ nguyên nền thẻ cam — `AC-06`)
    - `lavenderConnector = #BDA8EB` (đường nét đứt nối các node Journey)
    - Status palette cho 6 trạng thái của `ItemJourney` (`Saved #665A75`, `Considering #6740B3`, `Waiting #855200`, `Ready #246B55`, `Purchased #24627A`, `Skipped #665A75`).
- **Kích thước & Layout Tokens (`DESIGN-TOKENS.json`):**
  - `horizontalPadding = 16.dp`, `cardRadius = 24.dp`, `minimumTouchTarget = 48.dp`
  - `BudgetCreditCard`: `minHeight = 192.dp` (tự giãn chiều cao khi `fontScale` lớn, không khóa cứng `aspectRatio` khi text tràn).
  - `ItemJourney`: `nodeMin = 80.dp`, `nodeMax = 96.dp`, `nodeVerticalStep = 120.dp`, `connectorStroke = 2.dp`, `dashLength = 6.dp`, `dashGap = 6.dp`, `mascotPreferredWidth = 112.dp`.

---

## 3. Commands

```bash
# Biên dịch kiểm tra lỗi Kotlin & KSP
./gradlew :app:compileDevDebugKotlin --no-configuration-cache

# Chạy unit tests riêng cho Home v2.0
./gradlew :app:testDevDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.home.*" --no-configuration-cache

# Chạy toàn bộ unit tests (bao gồm ColorArchitectureTest)
./gradlew :app:testDevDebugUnitTest --no-configuration-cache

# Build gói APK DevDebug kiểm chứng tổng thể (không cài đặt lên thiết bị/máy ảo theo AGENTS.md)
./gradlew :app:assembleDevDebug --no-configuration-cache
```

---

## 4. Project Structure

```text
app/src/main/java/com/hoabh/chanko/
├── ui/theme/
│   └── Color.kt                                        # Bổ sung semantic tokens Home v2.0 (CandyOrange, Coral, Connector, 6 Statuses)
├── presentation/navigation/
│   └── MainNavigation.kt                               # Kết nối HomeRoute v2.0, URLInputDialog (+), MyItems default filter
└── presentation/screens/home/
    ├── HomeScreen.kt                                   # HomeRoute + HomeScreen 4 sections + Previews v2.0
    ├── HomeViewModel.kt                                # Quản lý SectionState độc lập, gesture lock, refresh, insight hide
    ├── HomeUiState.kt                                  # SectionState<T>, BudgetCreditCardUi, JourneyNodeUi, ObservationUi
    ├── HomeUiEvent.kt                                  # AvatarTap, BudgetTap, ItemTap(id), SeeAllTap, GestureStart/End, Retry...
    ├── HomeUiEffect.kt                                 # NavigateToProfile, NavigateToBudget, NavigateToItemDetail, NavigateToMyItems...
    ├── domain/
    │   └── HomeDomainLogic.kt                          # JourneySelector (exclude Archived, sort createdAt DESC + itemId ASC, take 5) & BudgetV2Calculator
    ├── data/
    │   └── HomeFakeDataSource.kt                       # Các kịch bản v2.0 (Default 5 nodes, 0/1/2-4/6+ nodes, OverBudget, MissingPrice, Offline, Errors)
    └── components/
        ├── HomeHeader.kt                               # Lời chào theo buổi + phụ đề v2.0 + Avatar + Offline badge
        ├── BudgetCreditCard.kt                         # Thẻ 3D Candy Orange (Normal, OverBudget, NoBudget, MissingPrice, Skeleton, Error)
        ├── ItemJourneySection.kt                       # Ziczac 1..5 nodes + Lavender dashed Canvas connector + Mascot bên phải
        └── ObservationCard.kt                          # Thẻ Góc nhìn từ Chanko có mèo góc trái dưới + mũi tên giải thích + nút ẩn
```

---

## 5. Code Style & Architectural Contracts

### 5.1. State độc lập từng khu vực (`SectionState<T>`) & Metadata kết nối
Không dùng một enum toàn màn hình làm mất dữ liệu của khu vực thành công khi khu vực kia lỗi (`AC-08`, `AC-22`):

```kotlin
sealed interface SectionState<out T> {
    data object InitialLoading : SectionState<Nothing>
    data class Content<T>(
        val data: T,
        val isFromCache: Boolean = false
    ) : SectionState<T>
    data object Empty : SectionState<Nothing>
    data class Error<T>(
        val message: String,
        val cachedData: T? = null
    ) : SectionState<T>
}
```

### 5.2. Luật lọc & sắp xếp ổn định cho `ItemJourney` (`AC-10`, `AC-11`, `AC-12`)
```kotlin
object JourneySelector {
    private const val MAX_JOURNEY_NODES = 5

    fun selectRecentNodes(candidates: List<JourneyItemCandidate>): List<JourneyItemCandidate> {
        return candidates
            .asSequence()
            .filterNot { it.isArchived }
            .sortedWith(
                compareByDescending<JourneyItemCandidate> { it.createdAtEpochMillis }
                    .thenBy { it.itemId } // Tie-break ổn định theo itemId ASC
            )
            .take(MAX_JOURNEY_NODES)
            .toList()
    }
}
```

### 5.3. Khóa Snapshot theo Gesture (`AC-23`, `AC-24`)
Khi người dùng đặt ngón tay lên một node (`ItemGestureStarted(itemId)`), `HomeViewModel` giữ `activeInteractionSnapshot`: mọi bản cập nhật danh sách mới (do thêm món, đồng bộ nền, hoặc archive) được xếp hàng vào `pendingJourneyCandidates` và chỉ áp dụng sau khi gesture kết thúc (`ItemGestureEnded`), đảm bảo không bao giờ đổi món ngay dưới ngón tay.

---

## 6. Testing Strategy

- **Unit Tests (`app/src/test/java/com/hoabh/chanko/presentation/screens/home/`):**
  1. `HomeDomainLogicTest.kt`:
     - Kiểm thử `JourneySelector`: lọc bỏ `Archived` trước khi `take(5)` (`AC-10`), giữ nguyên thứ tự khi đổi `status` (`AC-11`), tie-break theo `itemId ASC` khi `createdAt` trùng nhau (`AC-11`), trả về đúng `min(n, 5)` với $n \in \{0, 1, 2, 3, 4, 5, 7\}$ (`AC-12`).
     - Kiểm thử `BudgetV2Calculator`: tính `remaining = limit - spent` (gồm cả `Pay Now` và `Credit` — `AC-03`), giữ số âm và tính `overAmount` khi `spent > limit` đồng thời clamp `progress` trong `0f..1f` (`AC-06`), xử lý `limit <= 0` không chia cho 0 (`AC-07`), và đánh dấu `hasMissingPriceItems` khi có món chưa có giá (`AC-09`).
  2. `HomeViewModelTest.kt`:
     - Kiểm thử cô lập lỗi từng vùng: Budget lỗi vẫn giữ Journey hoạt động và ngược lại (`AC-08`, `AC-22`).
     - Kiểm thử Offline có cache (`AC-20`) vs Offline không có cache (`AC-21`).
     - Kiểm thử Gesture Lock: thêm món mới hoặc archive món trong lúc đang giữ ngón tay trên node không làm đổi `itemId` cho tới khi nhả tay (`AC-23`, `AC-24`).
     - Kiểm thử ẩn `Observation` theo `insightId` không xuất hiện lại sau khi refresh/khởi động lại (`AC-26`).
     - Kiểm thử chạm nút `+` khi online vs offline (`AC-20`, `AC-29`).
  3. `ColorArchitectureTest.kt`:
     - Đảm bảo 100% không có mã màu hex trực tiếp ngoài `Color.kt`.

---

## 7. Boundaries (Ranh giới thực thi)

- **Always do (Luôn thực hiện):**
  - Làm việc trên nhánh `feature/home-ui-v2` (tách từ `dev-chanko-1x`).
  - Khai báo toàn bộ màu mới trong `Color.kt` (`CandyPalette` $\rightarrow$ `CandyColors`).
  - Đảm bảo vùng chạm tối thiểu `48.dp`, gộp semantics cho từng node Journey (đọc tên món + trạng thái + hành động *"Mở chi tiết"*), và đặt `contentDescription = null` cho toàn bộ chi tiết trang trí (chip vàng, sóng contactless, mèo trang trí, đường nét đứt).
  - Chạy `./gradlew :app:testDevDebugUnitTest` và `./gradlew :app:assembleDevDebug` trước khi hoàn tất mỗi pha.
- **Ask first (Hỏi trước khi làm):**
  - Thay đổi schema cơ sở dữ liệu Room hoặc thêm thư viện bên thứ ba vào `build.gradle.kts`.
  - Thay đổi luồng nghiệp vụ của `URLInputDialog` hoặc `BudgetScreen`.
- **Never do (Tuyệt đối không làm):**
  - **Không cài đặt hoặc chạy ứng dụng trên thiết bị thật hoặc máy ảo/emulator** (`AGENTS.md`).
  - Không sửa đổi bất kỳ file nào trong `.agent/skills/`.
  - Không dùng ảnh crop tham khảo có chứa chữ/số tiền demo (`assets/raster-reference/*.png`) làm ảnh hiển thị trực tiếp lúc runtime.
  - Không tự động chuyển trạng thái món đồ sang `BUY` khi hết thời gian chờ (`AC-25`).

---

## 8. Success Criteria (31 Tiêu chí nghiệm thu `AC-01`..`AC-31`)

| Mã AC | Nhóm | Điều kiện nghiệm thu cụ thể |
| :--- | :--- | :--- |
| **`AC-01`** | Cấu trúc | Thứ tự hiển thị trên Home: `Header → Budget Credit Card → Item Journey → Observation`; Bottom Navigation cố định. |
| **`AC-02`** | Header | Chạm Avatar mở `Profile & Settings` gộp; không có nút Settings riêng. |
| **`AC-03`** | Budget | Số còn lại, đã chi, tổng ngân sách, tiến độ và chu kỳ nhất quán từ cùng nguồn dữ liệu (gồm `Pay Now` & `Credit`). |
| **`AC-04`** | Budget entry | Chạm bất kỳ vùng nào trên thẻ mở `BudgetScreen` đúng 1 lần; không còn lối vào Budget nào khác trên Home. |
| **`AC-05`** | Budget visual | Thẻ 3D Candy Orange (`#FFB665`); có chip vàng, contactless, `CHANKO SHOPPING BUDGET`, mèo trang trí; không có số thẻ, CVV hay logo ngân hàng thật. |
| **`AC-06`** | Over budget | Khi `spent > limit`: giữ nền thẻ cam, hiển thị cảnh báo/số vượt bằng màu Coral (`#B83248`); thanh tiến trình clamp trong `0..1`. |
| **`AC-07`** | No budget | Khi chưa thiết lập ngân sách: giữ khung thẻ cam và hiển thị hướng dẫn thiết lập; không báo `0đ` như dữ liệu thật. |
| **`AC-08`** | Budget error | Khi nguồn Budget lỗi: hiển thị thông báo lỗi + nút `Thử lại` cục bộ ngay trên thẻ; `ItemJourney` bên dưới vẫn hoạt động bình thường. |
| **`AC-09`** | Missing price | Khi có món thiếu giá (`priceMinor == null`): không ngầm coi giá là `0đ`; hiển thị chú thích dữ liệu giá chưa đầy đủ (`"Chưa đủ dữ liệu giá"`). |
| **`AC-10`** | Filtering | Khi có món `Archived` xen kẽ: loại bỏ `Archived` trước rồi mới lấy tối đa 5 món mới nhất theo `createdAt DESC`. |
| **`AC-11`** | Stable ordering | Đổi `status` không làm thay đổi thứ tự node; khi `createdAt` trùng nhau thì tie-break ổn định theo `itemId ASC`. |
| **`AC-12`** | Count | Với $n \in \{0..6+\}$ món: hiển thị $\min(n, 5)$ node thực; $n = 0$ hiển thị empty state; $n = 1$ hiển thị 1 node không có đường nối; không tạo node giả cho đủ 5. |
| **`AC-13`** | Node geometry | Node tròn đường kính `80–96dp`, bố trí ziczac dọc bên trái, nối với nhau bằng $n - 1$ đoạn nét đứt màu lavender (`#BDA8EB`). |
| **`AC-14`** | Node status | Đầy đủ 6 trạng thái (`Saved`, `Considering`, `Waiting`, `Ready`, `Purchased`, `Skipped`) có viền màu + badge icon góc trên phải phân biệt rõ (không chỉ dùng màu). |
| **`AC-15`** | Item detail | Chạm vào node mở trực tiếp `ItemDetailScreen(itemId)`, không mở bottom sheet trung gian. |
| **`AC-16`** | See all | Chạm *"Xem tất cả ›"* mở màn hình `MyItems` ở chế độ mặc định (`PrimaryFilter.ALL`), không ép lọc actionable. |
| **`AC-17`** | Mascot | Một mascot Chanko canonical đứng ở khoảng trống bên phải, cuộn cùng Journey, tự thu/dịch khi màn hẹp và không che vùng chạm của các node. |
| **`AC-18`** | Loading | Khi chưa có snapshot đầu tiên: hiển thị Skeleton tỷ lệ thẻ cho Budget và trạng thái đang tải cho Journey; không vẽ node giả. |
| **`AC-19`** | Refreshing | Khi đã có snapshot và đang làm mới: giữ nguyên nội dung hiển thị và vị trí cuộn hiện tại. |
| **`AC-20`** | Offline cache | Khi mất mạng nhưng có cache: hiển thị dữ liệu cache kèm thời điểm đồng bộ (`syncedAt`); chạm nút `+` không mở `URLInputDialog` mà báo cần mạng. |
| **`AC-21`** | Offline no cache | Khi mất mạng và không có cache: hiển thị trạng thái *"Chưa có dữ liệu"* (phân biệt rõ với `Empty` 0 món và `NoBudget`, không báo `0đ` giả). |
| **`AC-22`** | Partial error | Khi chỉ 1 nguồn lỗi (Budget hoặc Journey): hiển thị thông báo + nút `Thử lại` cục bộ tại vùng lỗi; vùng thành công giữ nguyên. |
| **`AC-23`** | Live add | Khi thêm món mới lúc Home đang mở: món mới chỉ đẩy lên đầu danh sách sau khi gesture hiện tại kết thúc; không tự động cuộn màn hình. |
| **`AC-24`** | Archive / delete | Khi món đang hiển thị bị archive/xóa: loại bỏ node đó và bù món thứ 6 lên; không đổi `itemId` ngay dưới ngón tay khi đang chạm. |
| **`AC-25`** | Domain changes | Khi `Cooling-off` hết hạn hoặc sang chu kỳ ngân sách mới: cập nhật sang `Ready` / chu kỳ mới theo domain; tuyệt đối không tự chuyển thành `BUY`. |
| **`AC-26`** | Observation | Khi không đủ dữ liệu hoặc người dùng đã ẩn: bỏ hoàn toàn vùng `Observation` và khoảng trống; trạng thái ẩn theo `insightId` được lưu bền vững qua khởi động lại. |
| **`AC-27`** | Observation detail | Khi có insight hợp lệ: hiển thị tối đa 1 thẻ *"Góc nhìn từ Chanko"* kèm nút mở giải thích và nút ẩn có nhãn trợ năng. |
| **`AC-28`** | Bottom navigation | Chạm 4 tab (`Trang chủ`, `Món đồ`, `Trò chuyện`, `Phân tích`) điều hướng đúng đích; tab `Trò chuyện` mở danh sách hội thoại (`ConversationsList`). |
| **`AC-29`** | Plus action | Khi online chạm `+`: mở duy nhất 1 `URLInputDialog`; khi đóng dialog giữ nguyên tab và vị trí cuộn; `+` không phải là tab được chọn. |
| **`AC-30`** | Restoration | Khi quay lại Home hoặc sau process death: làm mới dữ liệu liên quan, giữ vị trí cuộn theo anchor hợp lệ; không phát lại sự kiện điều hướng hay tự mở lại dialog. |
| **`AC-31`** | Accessibility | Trên màn hình `320dp`, `fontScale = 2.0f`, TalkBack và Reduced Motion: không mất nội dung/CTA, vùng chạm $\ge 48\text{dp}$, TalkBack đọc *tên món + trạng thái* trên từng node theo thứ tự `createdAt`, thành phần trang trí không nhận focus, tắt chuyển động khi bật Reduced Motion. |

---

## 9. Open Questions (Câu hỏi cần người dùng xác nhận trước khi lập Plan)

1. **Hành vi khi nhấn giải thích (`>`) trên thẻ `ObservationCard` (*"Góc nhìn từ Chanko"*) ở v2.0 (`AC-27`):**
   - Vì v2.0 đã loại bỏ `HomeSheetHost` của v1.0, khi người dùng nhấn vào thẻ/nút mũi tên `>` của *Góc nhìn từ Chanko*, bạn muốn:
     - **Phương án 1 (Khuyến nghị):** Hiển thị một Dialog giải thích chi tiết gọn nhẹ (`InsightExplanationDialog`) ngay trên Home (kèm dẫn chứng số liệu và nút *"Đã hiểu"* / *"Ẩn gợi ý này"*).
     - **Phương án 2:** Điều hướng sang tab `Phân tích` (`Insights`) hoặc `Trò chuyện` (`Chat`).
2. **Xác nhận Capability Map:** Bạn có đồng ý với 4 module trong Capability Map ([SPEC-home-ui-v2.md](../../SPEC-home-ui-v2.md)) và toàn bộ 5 giả định kỹ thuật ở Mục 0 không?
