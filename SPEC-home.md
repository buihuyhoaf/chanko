# Spec: Chanko Home — Compose Implementation Specification v2.0 (Image-Aligned)

> **Trạng thái:** APPROVED (v2.0 + Cập nhật Asset Pack v3)  
> **Ngày cập nhật:** 24/10/2026  
> **Nguồn visual chuẩn duy nhất:** [`chanko-home-compose-handoff-v2/references/approved-home-user-reference.png`](file:///Users/buihoa/HocTap/Android/chanko/chanko-home-compose-handoff-v2/references/approved-home-user-reference.png)  
> **Gói Asset Living Room (v3):** [`chanko-home-compose-handoff-v2/11-ASSET-DELIVERY.md`](file:///Users/buihoa/HocTap/Android/chanko/chanko-home-compose-handoff-v2/11-ASSET-DELIVERY.md) & [`assets/living-room/`](file:///Users/buihoa/HocTap/Android/chanko/chanko-home-compose-handoff-v2/assets/living-room/README.md) (`room_background.png`, `cat_on_cushion.png`, `hourglass_books.png`, `calendar_blank.png`, `wallet_mint.png`, `pause_jar_heart.png`, `scene-layout.json`)  
> **Tài liệu nguồn:** `chanko-home-compose-handoff-v2/START-HERE.md`, `00-VISUAL-REFERENCE.md` → `11-ASSET-DELIVERY.md`, `AGENTS.md`, `CODING_STANDARDS.md`

---

## 0. Phase 0 — Discovery, Repo Mapping & TECH Alignment (`T01`–`T18`)

### 0.1 Mapping Kiến trúc & Component vào Repository Thực tế
| Handoff v2 Logical Component | Vị trí thực tế / Đề xuất trong Repo (`:app`) | Hiện trạng trong Repo (Observed) |
|---|---|---|
| **Theme / Tokens / Fonts** | [`Color.kt`](file:///Users/buihoa/HocTap/Android/chanko/app/src/main/java/com/hoabh/chanko/ui/theme/Color.kt), [`Tokens.kt`](file:///Users/buihoa/HocTap/Android/chanko/app/src/main/java/com/hoabh/chanko/ui/theme/Tokens.kt), [`Type.kt`](file:///Users/buihoa/HocTap/Android/chanko/app/src/main/java/com/hoabh/chanko/ui/theme/Type.kt), `res/font/nunito_*.ttf` | Đã có `CandyColors`, `CandySpace`, `CandyShape`, `CandySize`, và 5 font files `nunito_400..800.ttf` trong `app/src/main/res/font/`. Cần bổ sung semantic color tokens cho v2 (Quick Check gradient, Observation bulb tile, Budget warning orange badge, Sheet tiles). |
| **HomeRoute & HomeScreen** | [`HomeScreen.kt`](file:///Users/buihoa/HocTap/Android/chanko/app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt) | Đã có khung P1 (`HomeRoute` + `HomeScreen` + `LazyColumn`). Cần mở rộng các slot theo đúng v2. |
| **HomeHeader** | [`HomeHeader.kt`](file:///Users/buihoa/HocTap/Android/chanko/app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/HomeHeader.kt) | Đã có khung cơ bản; cần cập nhật visual khớp ảnh chuẩn v2: icon `☀️` cạnh lời chào, câu phụ `"Hôm nay là một ngày tuyệt vời để mua sắm có suy nghĩ nhé 💜"`, nút Gear trên nền tròn lavender (`primarySoft`) và Avatar tròn tím (`brandPurple`). |
| **CompactLivingRoom + ChankoDialogue** | `presentation/screens/home/components/CompactLivingRoom.kt` | Chưa có. Lưu ý quan trọng từ `START-HERE.md`: **`ChankoDialogue` là bubble overlay nằm ở góc trên-trái BÊN TRONG `CompactLivingRoom`**, không phải một row độc lập bên dưới scene. |
| **QuickActionsRow** | `presentation/screens/home/components/QuickActionsRow.kt` | Chưa có. Gồm 2 thẻ ngang: `Quick Check` (nền tím gradient, icon search trong vòng trắng, chữ `"Quick Check"` / `"Hỏi Chanko trước khi mua"`) và `Save an Item` (nền sáng, icon `+` trong vòng tím, chữ `"Save an Item"` / `"Lưu để xem lại sau"`). |
| **BudgetCard** | `presentation/screens/home/components/BudgetCard.kt` | Chưa có. Card sáng gồm tiêu đề chu kỳ + pill `"Chi tiết >"`, cột `"Đã chi"` (`2.400.000 ₫ / 3.000.000 ₫`), vạch chia dọc, cột `"Còn lại"` (`600.000 ₫`) + badge cam `"Đã tiêu 80%"`, thanh progress tím + nhãn `"80%"` bên phải track. |
| **YourDecisionsSection & DecisionRow** | `presentation/screens/home/components/YourDecisionsSection.kt` | Chưa có. Header có icon checklist tím + `"Những món cần chú ý"` + count chip (`3`) + `"Xem tất cả >"`. Tối đa 3 `DecisionRow` (`Ready` nền mint + pill xanh lá `"Sẵn sàng xem lại"`; `Waiting` nền lavender sáng + pill `"Đang chờ"` + đồng hồ đếm ngược bên dưới). |
| **ObservationCard & DraftResumeCard** | `presentation/screens/home/components/ObservationCard.kt`, `DraftResumeCard.kt` | Chưa có. `ObservationCard` có icon bóng đèn trong tile lavender, `"Góc nhìn hôm nay"`, nút `X`, câu nhận xét đậm, câu bằng chứng nhạt, 2 pill `"Giải thích thêm"` & `"Ẩn gợi ý này"`. `DraftResumeCard` chỉ hiện khi `draft != null`. |
| **ChankoBottomNavigation & HomeSheetHost** | [`MainNavigation.kt`](file:///Users/buihoa/HocTap/Android/chanko/app/src/main/java/com/hoabh/chanko/presentation/navigation/MainNavigation.kt), `presentation/screens/home/components/HomeSheetHost.kt` | `ChankoBottomBar` đã có 4 tab (`Trang chủ`, `Món đồ`, `Ngân sách`, `Phân tích`) + nút `+` trung tâm. Cần nối `+` với `GlobalQuickActionSheet` (modal overlay có scrim, drag handle, `"Bạn muốn làm gì?"`, nút `X`, và 2 tile `Quick Check` / `Save an Item` với bộ icon riêng như ảnh chuẩn). |

### 0.2 Bảng Decision Log & TECH Alignment (`T01`–`T18`)
| ID | Hạng mục | Trạng thái | Quyết định / Bằng chứng trong Repo & Handoff v2 | Owner / Phạm vi ảnh hưởng |
|---|---|---|---|---|
| **T01** | Repo, branch, module, conventions | **RESOLVED (Repo)** | Module `:app`, nhánh làm việc tách từ `dev-chanko-1x` (`refactor/clean-home-screen`), MVVM + `BaseViewModel`, `CandyColors`/`CandySpace`/`CandyShape`, `CODING_STANDARDS.md`. | TECH / Phase 0–11 |
| **T02** | Mapping Domain Status & Duplicate/Unknown ID (`S18`) | **UI-FIRST CONTRACT / TECH PENDING** | `CooldownItem` trong `:core:domain` hiện chỉ có `COOLING_DOWN, BOUGHT, PASSED`. Ở tầng UI/Adapter của Home: định nghĩa `DecisionStatus { Ready, Waiting, Considering, Saved }`. Với `S18`: dedup theo ID (giữ bản ghi hợp lệ đầu tiên) và **loại bỏ/báo lỗi các item có status `Unknown`** (tuyệt đối không tự nâng `Unknown` thành `Ready`). | Domain + TECH / Phase 7, 10 |
| **T03** | Thứ tự trong nhóm, candidate toàn cục & count chip | **UI-FIRST CONTRACT / TECH PENDING** | Stable sort trên toàn bộ tập candidate theo thứ tự `Ready → Waiting → Considering → Saved`, giữ nguyên thứ tự upstream trong cùng nhóm, sau đó `take(3)`. Count chip hiển thị số lượng hiển thị (`visibleItems.size`, tối đa 3) hoặc `totalCount` từ state; không bao giờ hiển thị `3` khi danh sách rỗng/lỗi. | Backend + Product / Phase 7 |
| **T04** | Tiền tệ, minor units, remaining âm, ratio (`S06–S08`) | **RESOLVED FOR UI** | Dùng `Long` (`priceMinor` / VND với 0 chữ số thập phân, không dùng `Float`). Khi `limit == null || spent == null` (`S08`): hiển thị trạng thái chưa thiết lập/không xác định, không bịa `0 ₫`. Khi `limit == 0` (`S07`): `ratio = null`, `visualProgress = null`, không chia cho 0. Khi `spent > limit` (`S06`): `remaining` âm hiển thị đúng, `ratioLabelPercent = 120%`, `visualProgress` clamp trong `0f..1f`. | Domain + Finance / Phase 6 |
| **T05** | Cycle anchor, timezone, clock source (`S12, S19`) | **RESOLVED FOR UI** | Inject `Clock` / `ZoneId` (fixture cố định `2026-10-24T03:00:00Z`, zone `Asia/Ho_Chi_Minh`). Khi countdown về 0 (`S12`), hiển thị không âm (`0 phút` / hết giờ chờ) nhưng **không tự đổi status sang `Ready` hoặc tự mua**; source status là authoritative. | Domain + TECH / Phase 3, 7, 10 |
| **T06** | Flow của `Quick Check` & `Save an Item` | **PENDING ROUTE** | Bấm phát đúng một-lần `HomeUiEffect` (`OpenQuickCheck`, `OpenSaveItem`). Chưa tự lưu item khi chỉ bấm nút mở. | Product + TECH / Phase 5, 9 |
| **T07** | Ý nghĩa & tap action của 4 Props (`Hourglass`, `Calendar`, `Wallet`, `PauseJar`) | **UI-FIRST CONTRACT / TECH PENDING** | Chạm từng prop phát `PropTapped(kind)` và mở `ContextualSheet(kind)` tương ứng. `PauseJar` vẽ đúng hũ thủy tinh có trái tim hồng bên trong (không biến thành hũ tiền). | Product + Design + TECH / Phase 3, 9 |
| **T08** | Nội dung Contextual Sheets & Decision tap | **UI-FIRST CONTRACT / TECH PENDING** | `GlobalQuickActionSheet` có thiết kế chuẩn trong ảnh (`S22`). Các `ContextualSheet` (`Hourglass`, `Calendar`, `Wallet`, `PauseJar`, `Decision`) dùng layout sạch chỉ hiển thị dữ liệu thật từ state + xử lý an toàn khi entity không tồn tại (`S17`: báo `"Món đồ không còn khả dụng"`, khóa action ghi, cho phép đóng). | Product + Navigation / Phase 9 |
| **T09** | Global `+` Quick Action Sheet entries | **RESOLVED (v2)** | Chỉ gồm 2 entries: `Quick Check` và `Save an Item` theo đúng ảnh chuẩn `S22`. | Product + TECH / Phase 9 |
| **T10** | Routes của 4 tab, Settings, Profile, View All | **PARTIALLY RESOLVED** | 4 tab đã có trong [`TopLevelDestination.kt`](file:///Users/buihoa/HocTap/Android/chanko/app/src/main/java/com/hoabh/chanko/presentation/navigation/TopLevelDestination.kt). `Settings`, `Profile`, `View All`, `Budget Details` phát `HomeUiEffect` rõ ràng từ `HomeRoute`. | Navigation TECH / Phase 2, 6, 7, 9 |
| **T11** | Rule text của Dialogue & hành vi `Explain / Hide / X` của Observation | **UI-FIRST CONTRACT / TECH PENDING** | Dialogue lấy trực tiếp từ state (`DialogueUi?`), nếu `null` thì ẩn bubble. Observation tách biệt 3 intent: `ObservationExplainTapped(id)`, `ObservationHideTapped(id)`, `ObservationDismissTapped(id)`. Ở local state, `Hide` và `Dismiss (X)` ẩn card ngay; persistence chờ backend. | Product + Domain / Phase 4, 8 |
| **T12** | Nguồn Draft, chọn draft, resume (`S09, S11`) | **UI-FIRST CONTRACT / TECH PENDING** | `DraftUi?` hiển thị sau `ObservationCard` khi khác `null`; bấm phát `ResumeDraftTapped(id)` với đúng `draft.id`. Khi `null` bỏ hoàn toàn cả card và spacer. | Product + TECH / Phase 8 |
| **T13** | Offline cache, freshness, partial error (`S13, S14`) | **RESOLVED FOR UI** | Mỗi section (`room`, `dialogue`, `budget`, `decisions`) dùng `SectionState<T>` độc lập (`Loading`, `Content(value, freshness)`, `Error(messageRes, retryKey)`). Retry chỉ tải lại đúng section bị lỗi (`Retry(section)`). | Data TECH / Phase 2, 10 |
| **T14** | Scene layers, cat pose nằm chống cằm & 4 prop assets | **UPDATED (Asset Pack v3 Delivered)** | Đã có đủ **6 PNG tái tạo (`generated-implementation-candidate`)** tại `chanko-home-compose-handoff-v2/assets/living-room/`: `room_background.png` (1983×793 RGB), `cat_on_cushion.png` (1254×1254 RGBA), `hourglass_books.png` (1186×1327 RGBA), `calendar_blank.png` (1314×1197 RGBA), `wallet_mint.png` (1351×1164 RGBA), `pause_jar_heart.png` (1296×1213 RGBA) kèm tọa độ chuẩn hóa trong `scene-layout.json`. Thay thế hoàn toàn placeholder cho Phase 3/4 (vẫn giữ ghi chú cần visual QA trên thiết bị thật trước sign-off production). | Design / Phase 1, 3, 11 |
| **T15** | License/provenance của font Nunito & PNG | **OPEN (Release Gate)** | Font `nunito_400..800.ttf` đã có trong `app/src/main/res/font/`; 3 ảnh thumbnail mẫu (`sample_jacket.png`, `sample_headphones.png`, `sample_keyboard.png`) từ `chanko-home-compose-handoff-v2/assets/` chỉ dùng cho demo/preview fixture `S21`. Cần xác nhận license trước khi release production. | Asset Owner + TECH / Phase 1, 11 |
| **T16** | Baseline copy tiếng Anh/Việt theo ảnh chuẩn | **RESOLVED (v2)** | Giữ nguyên `"Quick Check"` và `"Save an Item"` bằng tiếng Anh theo đúng ảnh chuẩn; các nhãn còn lại bằng tiếng Việt như ảnh. | Product / Phase 2–9 |
| **T17** | Preview matrix & запрет Emulator/AVD | **RESOLVED (Repo)** | Kiểm chứng qua Compose Previews (`320dp`, `393dp`, `600dp` × fontScale `1.0`, `1.5`, `2.0`) và Unit Tests (`./gradlew assembleDebug :app:testDevDebugUnitTest`). Không dùng emulator/AVD/VM; báo `runtime unverified` khi không có thiết bị thật. | QA + TECH / Phase 1–11 |
| **T18** | Chống double-tap, single active sheet | **RESOLVED FOR UI** | `HomeUiState.activeSheet: HomeSheet?` là nguồn sự thật duy nhất (tối đa 1 sheet tại mọi thời điểm); bấm `+` liên tiếp (`S16`) vẫn chỉ giữ `HomeSheet.Global`. | TECH / Phase 9, 10 |

---

## 1. Assumptions (Giả định triển khai)

1. **Bám sát 100% ảnh chuẩn [`approved-home-user-reference.png`](file:///Users/buihoa/HocTap/Android/chanko/chanko-home-compose-handoff-v2/references/approved-home-user-reference.png):** Mọi chi tiết bố cục, lời thoại, màu sắc, vị trí 4 vật thể trong phòng, 2 thẻ Quick Actions, thẻ Ngân sách, 3 dòng Quyết định, thẻ Góc nhìn hôm nay và Global Quick Action Bottom Sheet đều tuân theo ảnh chuẩn v2.
2. **Bong bóng thoại (`ChankoDialogue`) nằm BÊN TRONG `CompactLivingRoom`:** Không tách thành section riêng bên dưới phòng khách.
3. **Asset Pack v3 (`assets/living-room/`) + Thumbnail mẫu (`S21`):**
   - Copy **6 PNG của Living Room** (`room_background.png`, `cat_on_cushion.png`, `hourglass_books.png`, `calendar_blank.png`, `wallet_mint.png`, `pause_jar_heart.png`) và **3 ảnh sản phẩm mẫu** (`sample_jacket.png`, `sample_headphones.png`, `sample_keyboard.png`) vào `app/src/main/res/drawable-nodpi/`.
   - Ghép lớp trong `CompactLivingRoom` theo đúng thứ tự `z = 0..6` và tỷ lệ chuẩn hóa trong `assets/living-room/scene-layout.json`: `Background (z=0) → Hourglass/books (z=1) → Calendar + Compose dynamic month/day xoay -3° (z=2) → Wallet (z=3) → Cat/cushion (z=4) → Pause Jar (z=5) → ChankoDialogue Compose bubble (z=6)`.
   - Tách biệt vị trí hiển thị sprite với 4 vùng chạm tương tác độc lập ($\ge 48\text{dp}$, không chồng lấn, có TalkBack semantics riêng).
4. **Thực thi theo Roadmap v2 (`Phase 1` → `Phase 11`):** Sau khi bạn duyệt đặc tả v2.0 này, chúng ta sẽ cập nhật `tasks/plan.md` và `tasks/todo.md` theo đúng 11 phases của v2 (kèm 22 scenarios `S01`–`S22`).

---

## 2. Objective (Mục tiêu)

Xây dựng màn hình `HomeScreen` và `GlobalQuickActionSheet` trên Jetpack Compose bám sát ảnh chuẩn người dùng đã duyệt (`v2.0 — image-aligned`), đảm bảo:
- Trải nghiệm **Mindful Shopping** ấm áp với linh vật mèo Chanko nằm chống cằm trên đệm tím trong căn phòng pastel.
- Hỗ trợ đầy đủ **22 kịch bản dữ liệu (`S01`–`S22`)** từ `fixtures/home-scenarios.json` với đồng hồ tiêm vào (`Clock` / `ZoneId`) và trạng thái độc lập theo từng khu vực (`SectionState<T>`).
- Đạt toàn bộ tiêu chí nghiệm thu **A01–A18** và **V01–V05** trong `08-ACCEPTANCE.md`.

---

## 3. Tech Stack & Commands

- **Tech Stack:** Kotlin 2.x, Jetpack Compose, Material 3, Clean Architecture + MVVM (`BaseViewModel`), Hilt, Coroutines, `StateFlow`, `java.time.Instant` / `ZoneId` / `LocalDate`, JUnit 4 + `kotlinx-coroutines-test`.
- **Commands:**
  - **Build Debug APK:**
    ```bash
    JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew assembleDebug
    ```
  - **Run Unit Tests (`:app:testDevDebugUnitTest`):**
    ```bash
    JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :app:testDevDebugUnitTest
    ```
  - **Run All Unit Tests:**
    ```bash
    JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew testDebugUnitTest
    ```
  - **Run Android Lint:**
    ```bash
    JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew lintDebug
    ```

---

## 4. Project Structure (Cấu trúc thư mục theo Component Tree v2)

```text
app/src/main/
├── res/
│   ├── drawable-nodpi/
│   │   ├── room_background.png          (Living Room v3 z=0, 1983x793 RGB)
│   │   ├── hourglass_books.png          (Living Room v3 z=1, 1186x1327 RGBA)
│   │   ├── calendar_blank.png           (Living Room v3 z=2, 1314x1197 RGBA, trống chữ)
│   │   ├── wallet_mint.png              (Living Room v3 z=3, 1351x1164 RGBA)
│   │   ├── cat_on_cushion.png           (Living Room v3 z=4, 1254x1254 RGBA)
│   │   ├── pause_jar_heart.png          (Living Room v3 z=5, 1296x1213 RGBA)
│   │   ├── sample_jacket.png            (Demo fixture S21)
│   │   ├── sample_headphones.png        (Demo fixture S21)
│   │   └── sample_keyboard.png          (Demo fixture S21)
│   ├── font/
│   │   └── nunito_400..800.ttf          (Đã có sẵn)
│   └── values/
│       └── strings_home.xml             (Copy chuẩn theo ảnh v2: Quick Check, Save an Item, Ngân sách, Những món cần chú ý, Góc nhìn hôm nay...)
└── java/com/hoabh/chanko/
    ├── ui/theme/
    │   ├── Color.kt                     (Bổ sung semantic tokens v2: QuickCheck gradient, Budget warning badge, Observation tile, Sheet tiles)
    │   └── Tokens.kt                    (CandySpace, CandyShape, CandySize, CandyElevation)
    └── presentation/
        ├── navigation/
        │   └── MainNavigation.kt        (ChankoBottomNavigation 4 tabs + Center '+' mở GlobalQuickActionSheet)
        └── screens/home/
            ├── HomeScreen.kt            (HomeRoute stateful & HomeScreen stateless + Previews S01..S22)
            ├── HomeViewModel.kt         (Reducer xử lý HomeAction, Section retry, Sheet lifecycle, Decision ranking)
            ├── HomeUiState.kt           (HomeUiState, SectionState<T>, HeaderUi, LivingRoomUi, DialogueUi, BudgetUi, DecisionUi, ObservationUi, DraftUi, HomeSheet)
            ├── HomeUiEvent.kt           (Sealed interface HomeAction / HomeUiEvent cho toàn bộ tương tác v2)
            ├── HomeUiEffect.kt          (One-off navigation effects)
            ├── domain/
            │   ├── DecisionRanker.kt    (Pure Kotlin stable ranker: dedup, filter valid status, Ready->Waiting->Considering->Saved, take(3))
            │   └── BudgetCalculator.kt  (Pure Kotlin money/ratio/remaining calculator cho S01, S06, S07, S08)
            ├── data/
            │   └── HomeScenarioFixtures.kt (22 deterministic scenarios S01..S22 khớp 100% với fixtures/home-scenarios.json)
            └── components/
                ├── HomeHeader.kt            (Phase 2: Greeting + ☀️, subtitle + 💜, lavender gear, purple avatar)
                ├── CompactLivingRoom.kt     (Phase 3 & 4: Pastel room scene, Chanko lying chin-on-paws on lavender cushion, 4 RoomProps, ChankoDialogue overlay top-left)
                ├── QuickActionsRow.kt       (Phase 5: Quick Check purple gradient card & Save an Item light card)
                ├── BudgetCard.kt            (Phase 6: Cycle header + Chi tiết pill, Đã chi / Còn lại + Đã tiêu % badge + progress bar)
                ├── YourDecisionsSection.kt  (Phase 7: Checklist header + count chip + Xem tất cả + 0..3 DecisionRows)
                ├── ObservationCard.kt       (Phase 8: Bulb tile, Góc nhìn hôm nay, X, body, evidence, Giải thích thêm / Ẩn gợi ý này)
                ├── DraftResumeCard.kt       (Phase 8: Optional local draft resume card)
                └── HomeSheetHost.kt         (Phase 9: GlobalQuickActionSheet modal overlay + ContextualSheet)
```

---

## 5. Code Style & Contract Sample (v2 Component Contracts)

```kotlin
@Immutable
data class DialogueUi(
    val headline: String,
    val supportingLine: String? = null,
    val actionId: String? = null
)

@Immutable
enum class RoomPropKind { Hourglass, Calendar, Wallet, PauseJar }

@Immutable
enum class DecisionStatus(val priority: Int) {
    Ready(0),
    Waiting(1),
    Considering(2),
    Saved(3)
}

/**
 * Thuật toán sắp xếp ưu tiên độc lập (Phase 7 — S01..S04, S18, S20):
 * - Loại bỏ duplicate ID (giữ bản ghi hợp lệ đầu tiên theo canonical rule)
 * - Stable sort theo priority: Ready -> Waiting -> Considering -> Saved
 * - Giữ nguyên thứ tự upstream trong cùng nhóm và lấy tối đa 3 phần tử.
 */
fun rankVisibleDecisions(candidates: List<DecisionUi>, maxItems: Int = 3): List<DecisionUi> =
    candidates
        .distinctBy { it.id }
        .sortedBy { it.status.priority }
        .take(maxItems)
```

---

## 6. Testing Strategy (Chiến lược kiểm thử 22 Scenarios `S01`–`S22`)

1. **Unit Tests (`HomeViewModelTest.kt`, `DecisionRankerTest.kt`, `BudgetCalculatorTest.kt`):**
   - **Ranking & Cap (`S01`–`S04`, `S18`, `S20`, `S21`):**
     - `S01`: `[S1, W1, R1, C1, R2]` → `["R1", "R2", "W1"]`.
     - `S02`: 5 `Ready` → `["R1", "R2", "R3"]`.
     - `S03`: `[S1, C1, W1, W2, R1]` → `["R1", "W1", "W2"]`.
     - `S04`: `[S1]` → `["S1"]`.
     - `S20`: `[S1, C1, C2, S2]` → `["C1", "C2", "S1"]`.
     - `S18`: Duplicate ID `"X"` được dedup an toàn; status `"Unknown"` bị loại bỏ, không bao giờ tự biến thành `Ready`.
   - **Budget & Money (`S01`, `S06`, `S07`, `S08`):**
     - `S01` / `S21`: `limit = 3_000_000`, `spent = 2_400_000` → `remaining = 600_000`, `ratio = 0.8f` (`80%`).
     - `S06`: `limit = 3_000_000`, `spent = 3_600_000` → `remaining = -600_000`, `ratioLabelPercent = 120`, `visualProgress = 1.0f`.
     - `S07`: `limit = 0`, `spent = 100_000` → `remaining = -100_000`, `ratio = null`, `visualProgress = null` (không chia cho 0).
     - `S08`: `limit = null`, `spent = null` → không bịa số `0 ₫`, `ratio = null`.
   - **Time & Authoritative Status (`S12`, `S19`):**
     - `S12`: Khi `now > deadline` nhưng source vẫn là `Waiting`, giữ nguyên `status = Waiting`, không tự mua, countdown hiển thị không âm.
     - `S19`: `2026-10-24T17:30:00Z` tại `Asia/Ho_Chi_Minh` → ngày địa phương là `2026-10-25` (Calendar hiển thị `OCT 25`).
   - **Optional Sections (`S09`–`S11`):**
     - Kiểm tra số lượng optional section hiển thị (`S09 -> 2`, `S10 -> 1`, `S11 -> 1`, `S01 -> 0`) và event `ResumeDraftTapped("D1")`.
   - **Section Isolation & Sheet Lifecycle (`S13`, `S14`, `S16`, `S17`, `S22`):**
     - `S13`: `Budget = Error`, `Decisions = Loading` → Header và BottomNav vẫn hiển thị bình thường, nút retry của Budget chỉ tải lại Budget.
     - `S16`: Bấm `AddTapped` 2 lần liên tiếp chỉ mở 1 `GlobalQuickActionSheet` và giữ `selectedTab = Home`; bấm `Back` đóng sheet.
     - `S17`: Khi mở `ContextualSheet(Decision, "R1")` mà `"R1"` không còn tồn tại, vô hiệu hóa action ghi, cho phép đóng, không crash.
2. **Compose Previews (`07-PREVIEWS-TESTS.md`):**
   - Previews cho từng component và full screen (`S21` Home closed, `S22` Global sheet open, `S15` 320dp + fontScale 2.0, `S05` Empty, `S13` Partial Error).

---

## 7. Boundaries (Ranh giới thực thi)

- **Always:**
  - Đối chiếu trực tiếp với [`approved-home-user-reference.png`](file:///Users/buihoa/HocTap/Android/chanko/chanko-home-compose-handoff-v2/references/approved-home-user-reference.png) ở mọi phase.
  - Đặt `ChankoDialogue` nằm **bên trong** `CompactLivingRoom` (góc trên-trái), không tạo row riêng ngoài scene.
  - Giữ nguyên chữ `"Quick Check"` và `"Save an Item"` như ảnh chuẩn.
  - Chạy `./gradlew assembleDebug :app:testDevDebugUnitTest` sau mỗi phase.
- **Ask First:**
  - Thay đổi bất kỳ module `:core:*` hoặc thêm thư viện mới vào `build.gradle.kts`.
- **Never:**
  - Không dùng `mascot_crossed_arms.png` làm pose mèo final trong phòng khách (ảnh chuẩn là mèo nằm chống cằm trên đệm tím).
  - Không cắt ảnh chụp màn hình có sẵn chữ để giả làm UI component.
  - Không đặt `GlobalQuickActionSheet` thành một phần tử cuộn bên dưới Bottom Navigation (phải là modal overlay có scrim).
  - Không tạo/dùng máy ảo (emulator/AVD/VM) hay cài đặt APK tự động.

---

## 8. Roadmap & Success Criteria (Phase 0 → Phase 11)

| Phase | Trọng tâm | Điều kiện nghiệm thu (Exit Gate) |
|---|---|---|
| **Phase 0** | Discovery & TECH alignment | Hoàn tất mapping repo, bảng `T01`–`T18` và danh sách asset blocker (Hoàn tất trong mục 0 tài liệu này). |
| **Phase 1** | Candy Pop foundation | Map semantic tokens v2 vào `CandyColors`/`CandyShape`, kiểm tra font Nunito tiếng Việt và copy 3 sample thumbnails cho fixture `S21`. |
| **Phase 2** | Home shell & Header | `HomeHeader` có icon `☀️`, dòng phụ kèm `💜`, nút Gear tròn lavender, Avatar tím; `HomeScreen` giữ đúng thứ tự slot v2. |
| **Phase 3** | Compact Living Room | Scene phòng pastel bo tròn, mèo trắng nằm chống cằm trên đệm tím (placeholder được gắn nhãn rõ), 4 `RoomProp` (`Hourglass`, `Calendar` ngày động, `Wallet`, `PauseJar` hũ tim hồng) với hit target ≥ 48dp không đè nhau. |
| **Phase 4** | Chanko Dialogue | Bubble thoại màu kem nằm góc trên-trái **bên trong** `CompactLivingRoom`, chữ 2 cấp độ đậm/nhạt, `fontScale = 2.0` không cắt chữ, ẩn gọn khi `null`. |
| **Phase 5** | Quick Check + Save Item | 2 thẻ ngang (`Quick Check` tím gradient + icon search; `Save an Item` nền sáng + icon `+` tím), tự xếp dọc trên màn hẹp/font lớn, phát 2 intent riêng biệt. |
| **Phase 6** | Budget Card | Header chu kỳ + pill `"Chi tiết >"`, `"Đã chi"` / `"Còn lại"` + badge cam `"Đã tiêu 80%"` + thanh progress tím; pass tests `S01`, `S06`, `S07`, `S08`. |
| **Phase 7** | Your Decisions | Header checklist tím + `"Những món cần chú ý"` + count chip + `"Xem tất cả >"`, tối đa 3 `DecisionRow` (`Ready` mint, `Waiting` lavender + countdown); pass tests `S01`–`S04`, `S12`, `S18`, `S20`, `S21`. |
| **Phase 8** | Optional Content | `ObservationCard` (bóng đèn vàng, `"Góc nhìn hôm nay"`, `X`, 2 pill `"Giải thích thêm"` / `"Ẩn gợi ý này"`) và `DraftResumeCard`; pass tests `S09`–`S11`. |
| **Phase 9** | Navigation & Sheets | 4 tab + nút `+` mở `GlobalQuickActionSheet` modal overlay (`S22`: `"Bạn muốn làm gì?"`, `X`, 2 tile với icon riêng) và `ContextualSheet` (`S16`, `S17`). |
| **Phase 10** | State & Data Integration | Tích hợp toàn bộ 22 scenarios `S01`–`S22` vào `HomeViewModel`, kiểm chứng retry cục bộ (`S13`), offline freshness (`S14`), boundary timezone (`S19`). |
| **Phase 11** | QA & Handoff Sign-off | Kiểm tra toàn bộ Previews matrix (`320/393/600dp` × `1.0/1.5/2.0`), acceptance `A01`–`A18` & `V01`–`V05`, chạy full test suite & build. |

---

## 9. Open Questions (Xác nhận trước khi qua Phase 1)

1. Bạn có đồng ý với kết quả **Phase 0 — Discovery & TECH Alignment (`T01`–`T18`)** và bản đặc tả [`SPEC-home.md`](file:///Users/buihoa/HocTap/Android/chanko/SPEC-home.md) (v2.0 — bám sát ảnh chuẩn [`approved-home-user-reference.png`](file:///Users/buihoa/HocTap/Android/chanko/chanko-home-compose-handoff-v2/references/approved-home-user-reference.png)) không?
2. Về phần **Artwork của `CompactLivingRoom` (Phase 3 — `T14`)**: Do gói v2 chưa có các file PNG tách lớp riêng cho mèo nằm chống cằm và 4 prop (`Hourglass`, `Calendar`, `Wallet`, `PauseJar`), chúng ta sẽ vẽ bộ Composable/Canvas/Vector 3D-styled bám sát bố cục, màu sắc và tư thế trong ảnh chuẩn (kèm nhãn `placeholder` trong manifest/preview cho tới khi Design xuất file PNG tách lớp cuối cùng) và khôi phục 3 ảnh thumbnail mẫu (`sample_jacket.png`, `sample_headphones.png`, `sample_keyboard.png`) cho `S21` — bạn đồng ý phương án này chứ?
