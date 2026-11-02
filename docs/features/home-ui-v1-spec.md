# Đặc Tả Kỹ Thuật: Chanko Home UI v1.0 — Candy Pop 3D (Spec-Driven Development)

> **Mã tính năng:** `chanko-home-ui-v1.0`  
> **Nguồn bàn giao chính thức:** `Chanko-Home-Design-Handoff-v1.0` (02/11/2026 · 22 Frozen Decisions · 22 Acceptance Criteria · Mockup Candy Pop 3D)  
> **Bản đồ năng lực (Capability Map):** [SPEC-home-ui-v1.md](../../SPEC-home-ui-v1.md)  
> **Trạng thái:** SPECIFICATION APPROVED FOR PLANNING

---

## 0. Các Giả Định Kỹ Thuật (Assumptions Surface Upfront)

Trước khi đi vào chi tiết đặc tả, dưới đây là các giả định được rút ra từ việc đối chiếu gói bàn giao `Chanko-Home-Design-Handoff-v1.0` (đặc biệt là `OPEN-DETAILS.md` và `ASSET-HANDOFF.md`) với codebase hiện tại của dự án:

1. **Ánh xạ bộ lọc điều hướng sang `MyItemsScreen` (`AC-06`, `OPEN-DETAILS.md`):**
   - *Đồng hồ cát ("Đang chờ")* $\rightarrow$ Điều hướng sang tab `MyItems` với `PrimaryFilter.ACTIVE` + `ActiveSubFilter.COOLING_OFF`.
   - *Lịch ("Cần xem lại")* $\rightarrow$ Điều hướng sang tab `MyItems` với `PrimaryFilter.ACTIVE` + `ActiveSubFilter.READY`.
   - *Pause Jar ("Đã bỏ qua")* $\rightarrow$ Điều hướng sang tab `MyItems` với `PrimaryFilter.SKIPPED` + `ActiveSubFilter.ALL_ACTIVE`.
   - *Nút "Tất cả ›" ở mục Những món cần chú ý* $\rightarrow$ Điều hướng sang tab `MyItems` ở trạng thái mặc định (`PrimaryFilter.ALL`), không tự áp filter con.
2. **Xử lý Asset 3D của Little Room & Mascot (`ASSET-HANDOFF.md`):**
   - Gói bàn giao quy định không cắt trực tiếp ảnh phẳng `home-candy-pop-3d-approved.png` làm UI và danh mục 10 layer 3D (`SCENE-01`..`DECOR-02`) là tài nguyên cần sản xuất riêng. Trong phạm vi code Android hiện tại, chúng ta **tái sử dụng bộ layer PNG tách rời sẵn có trong `res/drawable`** của dự án (`room_background`, `hourglass_books`, `calendar_blank`, `wallet_mint`, `cat_on_cushion`, `pause_jar_heart`, mascot trạng thái) kết hợp với **pill nhãn, badge số lượng, bóng thoại và khung card hoàn toàn bằng Compose native** theo đúng bố cục mockup mới.
3. **Lưu trữ trạng thái ẩn Observation (`AC-14`, `OPEN-DETAILS.md`):**
   - Lưu tập hợp `hiddenInsightIds: Set<String>` trong kho lưu trữ cục bộ theo phạm vi tài khoản hiện tại (DataStore / SharedPreferences / FakeRepository in-memory hỗ trợ persistence qua refresh và quay lại màn hình). Khi đang offline, thao tác ẩn chỉ cập nhật local state nếu không yêu cầu mutation mạng, không tạo offline write queue.
4. **Hành vi nút `"Giải thích thêm"` trên Observation (`OPEN-DETAILS.md` — Đã chốt với người dùng):**
   - Khi nhấn nút `"Giải thích thêm"` trên thẻ *Góc nhìn từ Chanko*, mở ngay **Contextual Bottom Sheet tại Home** (`ContextKind.Observation`) hiển thị nội dung giải thích chi tiết và dẫn chứng dữ liệu (`evidence`) của gợi ý đó, kèm nút đóng hoặc ẩn gợi ý; không chuyển hướng sang màn hình Chat.
5. **Quy tắc xếp hạng cùng nhóm (Tie-break) trong `Những món cần chú ý` (`AC-09`, `OPEN-DETAILS.md`):**
   - Ưu tiên tuyệt đối các món cần hành động: (1) Nhóm *Cần xem lại* (bao gồm cả các món vừa hết hạn `Cooling-off`) $\rightarrow$ (2) Nhóm *Đang chờ (Cooling-off)*. Trong cùng một nhóm ưu tiên, giữ nguyên thứ tự ổn định (stable sort) từ nguồn dữ liệu domain mà không áp dụng công thức xếp hạng 4 bậc cũ (`Ready -> Waiting -> Considering -> Saved`).

---

## 1. Objective (Mục Tiêu & 22 Quyết Định Đóng Băng)

Cập nhật toàn diện giao diện và nghiệp vụ màn hình **`HomeScreen`** theo chuẩn thiết kế **Candy Pop 3D v1.0**, tuân thủ tuyệt đối **22 quyết định UX đã đóng băng (Frozen Decisions)** và **22 tiêu chí nghiệm thu (`AC-01` đến `AC-22`)**:

### 1.1. Bảng 22 Quyết Định UX Đã Chốt (Frozen Decisions)

| ID | Phương án | Nội dung đã chốt | AC liên quan |
| :--- | :---: | :--- | :--- |
| **HOME-REV-01** | A | Bỏ hai thẻ Quick Actions (`HOME-03` cũ) | `AC-02` |
| **HOME-REV-02** | A | Thứ tự nội dung: `Header → Little Room → Những món cần chú ý → Budget → Observation` | `AC-01` |
| **HOME-REV-03** | A | Lịch trong Little Room đại diện các món đến hạn xem lại (*"Cần xem lại"*) | `AC-04`, `AC-06`, `AC-07`, `AC-08` |
| **HOME-REV-04** | A | Bottom Bar cố định 5 vị trí: `Home / My Items / + / Chat / Insights` (*Trang chủ / Món đồ / + / Trò chuyện / Phân tích*) | `AC-01`, `AC-17` |
| **HOME-REV-05** | A | Observation (*"Góc nhìn từ Chanko"*) chỉ hiển thị insight dựa trên dữ liệu thực | `AC-14` |
| **HOME-INT-01** | A | Chạm từng vật thể (và nhãn) trong Little Room mở contextual bottom sheet tương ứng | `AC-04`, `AC-05` |
| **HOME-INT-02** | A | Hành động chính trong contextual sheet điều hướng sang `My Items` với bộ lọc tương ứng | `AC-06` |
| **HOME-STATE-01**| A | Giữ empty state nhỏ bên trong khu vực khi không có món cần chú ý | `AC-09` |
| **HOME-STATE-02**| A | Offline cho phép xem cache, chặn mọi thao tác cần mạng | `AC-16`, `AC-17` |
| **HOME-BUD-01** | A | Chạm toàn bộ `Budget Card` (hoặc bất kỳ vùng tương tác nào bên trong) để mở `BudgetScreen` | `AC-12` |
| **HOME-OBS-01** | A | Ghi nhớ `insightId` đã ẩn để không tự xuất hiện lại khi refresh hoặc quay lại Home | `AC-14` |
| **HOME-EDGE-01** | A | Món hết hạn `Cooling-off` tự động cập nhật sang nhóm *Cần xem lại* và cập nhật số lượng | `AC-07`, `AC-08` |
| **HOME-EDGE-02** | A | Lỗi độc lập theo từng khu vực (Section-level error isolation) | `AC-15` |
| **HOME-EDGE-03** | A | Giữ vị trí cuộn và cập nhật dữ liệu mới khi quay lại Home | `AC-18` |
| **HOME-EDGE-04** | A | Giá thiếu (`null`) hiển thị `"Chưa có giá"`, không coi là `0đ` | `AC-09`, `AC-10` |
| **HOME-EDGE-05** | A | Giữ cache offline và hiển thị thời điểm đồng bộ thực tế | `AC-16` |
| **HOME-EDGE-06** | A | Không tự mở lại contextual sheet sau khi Android hủy tiến trình (process death) | `AC-19` |
| **HOME-GAP-01** | A | Chỉ ưu tiên món cần hành động, đặc biệt món đến hạn xem lại (tối đa 3 món) | `AC-09` |
| **HOME-GAP-02** | A | `Budget Card` là lối vào chính, chiếc ví trong Little Room là shortcut phụ (mở sheet Ngân sách rồi vào `BudgetScreen`) | `AC-04`, `AC-06`, `AC-12` |
| **HOME-GAP-03** | A | Nút `+` khi offline thông báo cần kết nối mạng, không mở Add Item, không tạo draft | `AC-17` |
| **HOME-GAP-04** | A | Tự động cập nhật `Budget Snapshot` trên Home khi đổi chu kỳ ngân sách | `AC-11`, `AC-13` |
| **HOME-GAP-05** | A | Bóng thoại trong Little Room ngắn gọn; `Observation` chứa insight chi tiết | `AC-14`, `AC-22` |

---

## 2. Tech Stack & Commands

* **Ngôn ngữ & UI Toolkit:** Kotlin 2.0+, Jetpack Compose (Material 3 + Candy Pop 3D Design Tokens).
* **Typography & Icons:** Font `Nunito`, màu chủ đạo `#8965F6`, bộ icon vector `Chanko Candy Duotone v2.0-r1` (`NAV-01`, `NAV-02`, `NAV-04`, `ACT-01`, `ACT-03`, `ACT-11`, `ACT-18`, `ITM-04`, `ITM-05`, `ITM-06`, `BUD-01`, `SYS-01`, `SYS-03`, `SYS-08`, `SYS-10`, `SYS-11`).
* **Kiến trúc:** MVVM + Clean Architecture (`StateFlow`, Hilt DI, Section-aware `SectionState<T>`).

### Executable Commands:
```bash
# Biên dịch bản devDebug
./gradlew :app:assembleDevDebug --no-configuration-cache

# Chạy toàn bộ Unit Test cho devDebug
./gradlew :app:testDevDebugUnitTest --no-configuration-cache

# Kiểm tra Lint
./gradlew :app:lintDevDebug --no-configuration-cache
```

---

## 3. Project Structure (Tổ Chức Mã Nguồn)

```
app/src/main/java/com/hoabh/chanko/
├── presentation/screens/home/
│   ├── HomeScreen.kt                        # HomeRoute (Stateful) & HomeScreen (Stateless 5 khu vực)
│   ├── HomeViewModel.kt                     # Quản lý state, reconcile cooling-off, ẩn insight, sheet state
│   ├── HomeUiState.kt                       # Immutable UI models (Header, LittleRoom, Attention, Budget, Observation)
│   ├── HomeUiEvent.kt                       # Sự kiện tương tác từ người dùng (không còn QuickActions/GlobalSheet)
│   ├── HomeUiEffect.kt                      # Side-effects điều hướng (kèm filter MyItems, ItemDetail, Budget, Offline toast)
│   ├── components/
│   │   ├── HomeHeader.kt                    # Khu vực 1: Lời chào theo buổi, phụ đề, offline sync timestamp, Avatar 48dp
│   │   ├── CompactLivingRoom.kt             # Khu vực 2: Little Room 4 vật thể + pill nhãn + badge động + bóng thoại ngắn
│   │   ├── YourDecisionsSection.kt          # Khu vực 3: Card bao lớn "Những món cần chú ý" (tối đa 3 món, giá null -> "Chưa có giá")
│   │   ├── BudgetCard.kt                    # Khu vực 4: Card "Ngân sách mua sắm" (click toàn card mở BudgetScreen)
│   │   ├── ObservationCard.kt               # Khu vực 5: Card "Góc nhìn từ Chanko" (ẩn/đóng lưu theo insightId)
│   │   └── HomeSheetHost.kt                 # ContextualBottomSheet cho 4 vật thể (không có GlobalQuickActionSheet)
│   ├── domain/
│   │   └── HomeDomainLogic.kt               # Reconcile cooling-off hết hạn -> Review, chống đếm trùng, BudgetCalculator, Formatter
│   └── data/
│       └── HomeFakeDataSource.kt            # Fixtures kiểm thử đầy đủ các kịch bản và trạng thái AC-01..AC-22
├── presentation/navigation/
│   ├── MainNavigation.kt                    # ChankoBottomBar 5 vị trí, chặn nút + khi offline, truyền filter sang MyItems
│   └── NavGraph.kt                          # Kết nối điều hướng ItemDetailScreen, ProfileScreen, BudgetScreen
└── app/src/test/java/com/hoabh/chanko/presentation/screens/home/
    ├── HomeDomainLogicTest.kt               # Unit test cho cooling-off reconcile, deduplication, null price, budget math
    └── HomeViewModelTest.kt                 # Unit test kiểm chứng 22 quyết định Frozen & AC-01..AC-22
```
*(Lưu ý: File `QuickActionsRow.kt` sẽ được gỡ bỏ khỏi luồng render `HomeScreen` và dọn dẹp theo `AC-02`).*

---

## 4. Code Style & Architectural Conventions (Quy Chuẩn Thiết Kế & Code)

### 4.1. Thứ Tự 5 Khu Vực Nội Dung & Tách Biệt Trạng Thái Sheet Khỏi Process Death (`AC-01`, `AC-02`, `AC-18`, `AC-19`)
* `LazyColumn` sử dụng `rememberLazyListState()` để tự động giữ vị trí cuộn khi điều hướng đi và quay lại (`AC-18`).
* `activeSheet` chỉ giữ trong bộ nhớ của `HomeViewModel` (không lưu vào `rememberSaveable` hay `SavedStateHandle`), đảm bảo khi Android hủy tiến trình (process death) và khôi phục lại, sheet luôn ở trạng thái đóng (`AC-19`).

```kotlin
// Trong HomeScreen.kt — Thứ tự 5 khu vực đã chốt (AC-01, AC-02)
LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(
        start = horizontalPadding,
        top = statusBarTopPadding + CandySpace.xs,
        end = horizontalPadding,
        bottom = BottomNavClearanceHeight
    ),
    verticalArrangement = Arrangement.spacedBy(CandySpace.md)
) {
    // 1. Header (AC-03, AC-16)
    item(key = "home_section_1_header") {
        HomeHeader(
            header = state.header,
            onProfileClick = { onEvent(HomeUiEvent.ProfileClick) }
        )
    }

    // 2. Little Room (AC-04, HOME-REV-03, HOME-GAP-05)
    item(key = "home_section_2_little_room") {
        CompactLivingRoom(
            sectionState = state.livingRoom,
            reducedMotion = state.isReducedMotionEnabled,
            onPropTapped = { kind -> onEvent(HomeUiEvent.PropTapped(kind)) },
            onRetryClick = { onEvent(HomeUiEvent.RetrySection(HomeSection.LivingRoom)) }
        )
    }

    // 3. Những món cần chú ý (AC-07, AC-08, AC-09, AC-10, HOME-STATE-01, HOME-EDGE-04)
    item(key = "home_section_3_attention") {
        YourDecisionsSection(
            sectionState = state.decisionsSection,
            onDecisionClick = { itemId -> onEvent(HomeUiEvent.DecisionTapped(itemId)) },
            onViewAllClick = { onEvent(HomeUiEvent.ViewAllTapped) },
            onRetryClick = { onEvent(HomeUiEvent.RetrySection(HomeSection.Decisions)) }
        )
    }

    // 4. Budget Snapshot (AC-11, AC-12, AC-13, HOME-BUD-01)
    item(key = "home_section_4_budget") {
        BudgetCard(
            sectionState = state.budgetSection,
            onCardClick = { onEvent(HomeUiEvent.BudgetCardTapped) },
            onRetryClick = { onEvent(HomeUiEvent.RetrySection(HomeSection.Budget)) }
        )
    }

    // 5. Observation — Góc nhìn từ Chanko (AC-14, HOME-REV-05, HOME-OBS-01)
    state.visibleObservation?.let { observation ->
        item(key = "home_section_5_observation_${observation.id}") {
            ObservationCard(
                observation = observation,
                onExplainClick = { id -> onEvent(HomeUiEvent.ObservationExplainTapped(id)) },
                onHideClick = { id -> onEvent(HomeUiEvent.ObservationHideTapped(id)) },
                onDismissClick = { id -> onEvent(HomeUiEvent.ObservationDismissTapped(id)) }
            )
        }
    }
}
```

### 4.2. Đồng Bộ Phân Nhóm & Hết Hạn Cooling-off (`AC-07`, `AC-08`, `AC-09`, `HOME-EDGE-04`)
* Khi một món đồ đang ở trạng thái chờ (`Waiting` / `CoolingOff`) có `remainingSeconds <= 0` (đã qua thời điểm kết thúc làm nguội), bộ phân nhóm chuyển món đó sang nhóm **Cần xem lại (`Review`)**, nhưng **giữ nguyên `verdict` gốc (ví dụ `WAIT`), tuyệt đối không tự đổi `verdict` sang `BUY`**.
* Nhãn hiển thị trên thẻ đối với món vừa hết hạn chờ là `"Vừa hết thời gian chờ"` (icon đồng hồ cát `ITM-04` hoặc lịch xem lại `ITM-05`), còn món đến hạn xem lại định kỳ hiển thị `"Đến hạn xem lại"`.
* Cùng một `itemId` chỉ thuộc duy nhất 1 nhóm tại mọi thời điểm; số đếm badge trên Little Room (`Đang chờ`, `Cần xem lại`, `Đã bỏ qua`) và danh sách `Những món cần chú ý` được tính từ **cùng một snapshot đã reconcile**.
* Giá sản phẩm `priceMinor: Long?`: nếu `null` hiển thị `"Chưa có giá"`; nếu bằng `0L` hiển thị `"0 ₫"` (`HOME-EDGE-04`).

---

## 5. Testing Strategy (Chiến Lược Kiểm Thử)

* **Unit Tests (`app/src/test/.../home/`):**
  - `HomeDomainLogicTest`: Kiểm thử quá trình reconcile khi `remainingSeconds <= 0` chuyển nhóm từ `Waiting` sang `Review` mà không đổi `verdict` thành `BUY` (`AC-07`); kiểm thử không trùng lặp `itemId` giữa hai nhóm (`AC-08`); kiểm thử chọn tối đa 3 món ưu tiên cần hành động (`AC-09`); kiểm thử `priceMinor == null` trả về `"Chưa có giá"` (`HOME-EDGE-04`); kiểm thử tính toán ngân sách còn lại/vượt mức/chưa thiết lập (`AC-11`).
  - `HomeViewModelTest`: Kiểm thử đầy đủ các sự kiện mở 4 contextual sheet (`AC-04`), empty state của sheet (`AC-05`), điều hướng đúng bộ lọc (`AC-06`), đồng bộ khi đổi chu kỳ ngân sách (`AC-13`), ẩn `Observation` theo `insightId` không xuất hiện lại sau khi refresh (`AC-14`), cô lập lỗi từng khu vực (`AC-15`), chặn thao tác mạng & chặn nút `+` khi offline (`AC-16`, `AC-17`).
* **Preview & Semantics Verification:**
  - Bộ `@Preview` kiểm tra các kích thước cửa sổ `320dp`, `360dp`, `412dp`, `600dp`, `840dp` và `fontScale = 1.0f / 1.5f / 2.0f`, chế độ giảm chuyển động (`reducedMotion = true`) và TalkBack semantics (`Role.Button`, vùng chạm $\ge 48\text{dp}$, không lồng clickable).

---

## 6. Boundaries (Ranh Giới Thực Thi)

* **Always do (Luôn thực hiện):**
  - Tuân thủ 100% 22 quyết định Frozen và 22 Acceptance Criteria trong `Chanko-Home-Design-Handoff-v1.0`.
  - Sử dụng bộ icon `Chanko Candy Duotone v2.0` đã ánh xạ trong `ICON-MAPPING.csv`.
  - Đảm bảo mọi vùng chạm tương tác đạt tối thiểu `48dp` và gộp ngữ nghĩa TalkBack rõ ràng.
  - Chạy `./gradlew :app:testDevDebugUnitTest` và `./gradlew :app:assembleDevDebug` xanh hoàn toàn trước khi hoàn tất mỗi bước.
* **Ask first (Hỏi trước khi làm):**
  - Thay đổi schema cơ sở dữ liệu Room toàn cục hoặc thay đổi hợp đồng của các màn hình ngoài phạm vi liên kết Home (`AddItem`, `Budget`, `MyItems`).
  - Bổ sung thư viện bên thứ ba mới vào `build.gradle.kts`.
* **Never do (Tuyệt đối không làm):**
  - Không cài đặt hoặc chạy ứng dụng trên thiết bị thật hoặc máy ảo/emulator (tuân thủ quy tắc dự án `AGENTS.md`).
  - Không giữ lại 2 thẻ Quick Actions cũ (`HOME-03`) hoặc tạo bản thay thế.
  - Không tự đổi verdict sang `BUY` khi hết hạn `Cooling-off`, không dùng ngôn từ/màu sắc/biểu cảm thúc ép mua sắm (`AC-22`).
  - Không tạo bản nháp (draft) hoặc hàng đợi ngoại tuyến (offline queue) khi bấm nút `+` lúc mất mạng (`AC-17`).
  - Không nhúng nguyên ảnh phẳng `home-candy-pop-3d-approved.png` làm giao diện màn hình.

---

## 7. Success Criteria (22 Tiêu Chí Nghiệm Thu Chính Thức)

1. **`AC-01`**: Home hiển thị đúng thứ tự 5 khu vực đã chốt (`Header → Little Room → Những món cần chú ý → Budget → Observation`; `Bottom Bar` cố định riêng).
2. **`AC-02`**: Không còn hai thẻ Quick Actions cũ (`HOME-03`) trên màn hình Home.
3. **`AC-03`**: Header hiển thị lời chào theo buổi và chạm avatar mở `Profile & Settings`.
4. **`AC-04`**: Cả 4 vật thể trong Little Room (*Đang chờ*, *Cần xem lại*, *Ngân sách*, *Đã bỏ qua*) mở đúng contextual bottom sheet tương ứng.
5. **`AC-05`**: Contextual sheet hiển thị empty state đúng ngữ cảnh khi nhóm không có dữ liệu.
6. **`AC-06`**: Hành động chính trong sheet điều hướng đến đúng màn hình và bộ lọc (`MyItems` kèm filter *Đang chờ / Cần xem lại / Đã bỏ qua*, hoặc `BudgetScreen` cho *Ví*).
7. **`AC-07`**: Món hết `Cooling-off` tự chuyển sang nhóm *Cần xem lại* mà không tự đổi verdict thành `BUY`.
8. **`AC-08`**: Một món không bị tính trùng giữa nhóm *Đang chờ* và *Cần xem lại*.
9. **`AC-09`**: *Những món cần chú ý* hiển thị tối đa 3 món, ưu tiên món cần hành động; giữ empty state nhỏ khi có 0 món.
10. **`AC-10`**: Chạm thẻ món mở đúng `ItemDetailScreen` theo `itemId`; giá thiếu hiển thị `"Chưa có giá"`.
11. **`AC-11`**: `Budget Card` hiển thị đúng chu kỳ, số còn lại hoặc số vượt và tiến độ từ domain.
12. **`AC-12`**: Chạm bất kỳ vùng tương tác nào của `Budget Card` đều mở `BudgetScreen` (1 action semantic duy nhất).
13. **`AC-13`**: Home tự cập nhật `Budget Snapshot` khi chu kỳ Budget thay đổi.
14. **`AC-14`**: `Observation` (*Góc nhìn từ Chanko*) chỉ hiện khi có insight đủ dữ liệu; insight đã ẩn (`insightId`) không tự xuất hiện lại.
15. **`AC-15`**: Lỗi một khu vực không làm các khu vực khác ngừng hoạt động.
16. **`AC-16`**: Offline hiển thị dữ liệu cache và thời điểm đồng bộ thực tế; các thao tác cần mạng bị chặn.
17. **`AC-17`**: Nút `+` trên Bottom Bar khi offline thông báo cần kết nối mạng, không mở Add Item, không tạo draft.
18. **`AC-18`**: Quay lại Home từ màn hình khác giữ nguyên vị trí cuộn và cập nhật dữ liệu mới.
19. **`AC-19`**: Không tự khôi phục contextual sheet sau khi Android hủy tiến trình (process death).
20. **`AC-20`**: Tất cả thành phần tương tác hỗ trợ TalkBack, vùng chạm tối thiểu `48dp` và trạng thái focus rõ ràng.
21. **`AC-21`**: Animation tôn trọng thiết lập giảm chuyển động (Reduced Motion) của thiết bị.
22. **`AC-22`**: Không có thông điệp, màu sắc hoặc biểu cảm gây áp lực mua sắm.

---

## 8. Open Questions (Đã Giải Quyết Toàn Bộ)

1. **Capability Map & 5 Giả định kỹ thuật:** Đã được người dùng xác nhận đồng ý (02/11/2026).
2. **Hành vi nút `"Giải thích thêm"` trên thẻ `Góc nhìn từ Chanko`:** Đã được người dùng chốt phương án hiển thị nội dung giải thích chi tiết ngay trong **Contextual Sheet tại Home** (`ContextKind.Observation`).
