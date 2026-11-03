# Implementation Plan: Chanko Home UI v2.0 (Budget Credit Card & Item Journey)

> **Spec tham chiếu:** [SPEC-home-ui-v2.md](../SPEC-home-ui-v2.md) · [docs/features/home-ui-v2-spec.md](../docs/features/home-ui-v2-spec.md)  
> **Nguồn bàn giao:** `Chanko_Home_v2.0_Handoff` (31 Acceptance Criteria `AC-01`..`AC-31` · 23 State Matrix rows · `reference/home-v2-approved.png`)  
> **Task List Target:** [tasks/todo.md](todo.md) & [docs/tasks/home-ui-v2-tasks.md](../docs/tasks/home-ui-v2-tasks.md)

---

## 1. Overview

Triển khai toàn diện màn hình **HomeScreen v2.0** theo đặc tả đã phê duyệt tại [docs/features/home-ui-v2-spec.md](../docs/features/home-ui-v2-spec.md), thay thế hoàn toàn kiến trúc Little Room + 4 vật thể + Contextual Sheet + danh sách *Những món cần chú ý* của v1.0 bằng cấu trúc **4 khu vực cuộn (`Header → Budget Credit Card → Item Journey → Observation`)**:
1. **`HomeHeader`**: Lời chào theo buổi + phụ đề *"Hôm nay mình cùng cân nhắc nhé!"* + nút Avatar tròn mở `Profile & Settings` gộp + huy hiệu Offline kèm `syncedAt` (`AC-02`, `AC-20`).
2. **`BudgetCreditCard`**: Thẻ tín dụng mua sắm **3D Candy Orange (`#FFB665` / `#F59145`)** với logo dấu chân + `CHANKO SHOPPING BUDGET`, chip vàng, sóng contactless, mèo trắng ló đầu trang trí, số tiền *Ngân sách còn lại* `32sp ExtraBold`, thanh tiến trình capsule (`0..1`) + pill `%`, dòng *Đã chi / tổng ngân sách* (bao gồm cả `Pay Now` & `Credit`) và khoảng ngày chu kỳ; giữ nền cam khi vượt hạn mức kèm cảnh báo màu **Coral (`#B83248`)**; hỗ trợ đầy đủ `NoBudget`, `MissingPrice` (*"Chưa đủ dữ liệu giá"*), `Skeleton`, `Error + Retry cục bộ`, `OfflineCache` và `OfflineNoCache` (`AC-03`..`AC-09`, `AC-18`..`AC-22`).
3. **`ItemJourneySection` (*"Món đồ gần đây"*)**: Hiển thị $\min(n, 5)$ món đồ mới nhất sau khi lọc bỏ `Archived` (`createdAt DESC`, tie-break `itemId ASC`, đổi `status` không đổi thứ tự — `AC-10`, `AC-11`, `AC-12`); các node tròn `80–96dp` xếp ziczac dọc bên trái nối bằng $n - 1$ đoạn nét đứt lavender (`#BDA8EB`), mỗi node có ảnh sản phẩm + viền màu + badge icon góc trên phải cho đủ **6 trạng thái** (`Saved`, `Considering`, `Waiting`, `Ready`, `Purchased`, `Skipped` — `AC-13`, `AC-14`); chạm node mở trực tiếp `ItemDetailScreen(itemId)` (`AC-15`), chạm *"Xem tất cả ›"* mở `MyItems` mặc định (`AC-16`); 1 mascot Chanko canonical đứng bên phải cuộn cùng Journey (`AC-17`).
4. **`ObservationCard` (*"Góc nhìn từ Chanko"*)**: Thẻ có mèo Chanko góc trái dưới + icon bóng đèn + nhận xét + nút mũi tên `>` mở `InsightExplanationDialog` + nút ẩn bền vững theo `insightId`; bỏ hoàn toàn vùng và khoảng trống khi không có dữ liệu (`AC-26`, `AC-27`).
5. **Điều hướng, Đồng bộ Gesture-Safe & Trợ năng (`AC-23`..`AC-25`, `AC-28`..`AC-31`)**: Khóa snapshot `itemId` từ `pointer-down` đến `click/cancel`, bảo toàn vị trí cuộn & anchor hợp lệ qua back/process death, chặn nút `+` khi offline, và đảm bảo tương thích `320dp`, `fontScale = 2.0f`, TalkBack và Reduced Motion.

---

## 2. Dependency Graph & Architecture Decisions

```mermaid
graph TD
    T["Task 1: Color Tokens & Domain Logic<br/>(Color.kt, HomeDomainLogic.kt: JourneySelector + BudgetV2Calculator)"]
    S["Task 2: State Model, Gesture Lock & ViewModel<br/>(HomeUiState, HomeUiEvent, HomeUiEffect, HomeFakeDataSource, HomeViewModel)"]
    B["Task 3: BudgetCreditCard Component<br/>(3D Candy Orange Card, Normal/Over/NoBudget/MissingPrice/Skeleton/Error)"]
    J["Task 4: ItemJourneySection Component<br/>(Zig-zag 1..5 Nodes, Dashed Lavender Connector, 6 Status Badges, Mascot)"]
    H["Task 5: HomeHeader, ObservationCard + Dialog & HomeScreen v2.0<br/>(4-Section Layout, Remove v1 LivingRoom/Sheet/Decisions)"]
    N["Task 6: MainNavigation, Previews & Full 31-AC Verification<br/>(MyItems Default Route, Online/Offline +, Anchor Restore, Unit Tests)"]

    T --> S
    S --> B
    S --> J
    B --> H
    J --> H
    H --> N
```

### Các Quyết Định Kiến Trúc Cốt Lõi:
1. **Cô lập Trạng thái Từng Khu vực (`SectionState<T>` — `AC-08`, `AC-18`..`AC-22`):**
   - `budgetState: SectionState<BudgetCreditCardUi>` và `journeyState: SectionState<ItemJourneyUi>` hoạt động độc lập. Khi Budget lỗi, người dùng có nút `Thử lại` ngay trên khung thẻ cam trong khi `ItemJourney` vẫn hiển thị và bấm vào node bình thường (và ngược lại).
   - Phân biệt tường minh giữa `Empty` (đã tải thành công và có 0 món), `NoBudget` (chưa thiết lập ngân sách), `OfflineCache` (mất mạng nhưng có dữ liệu cache + `syncedAt`) và `OfflineNoCache` (mất mạng và chưa có cache $\rightarrow$ hiển thị *"Chưa có dữ liệu"*, không báo 0 món hay `0đ` giả).
2. **Định danh Ổn định & Khóa Snapshot Theo Gesture (`AC-10`, `AC-11`, `AC-23`, `AC-24`):**
   - `JourneySelector.selectRecentNodes` loại bỏ `isArchived == true` trước, sau đó sắp xếp theo `createdAt DESC` rồi `itemId ASC`, lấy tối đa 5 phần tử. Mọi thay đổi `status` không bao giờ làm xáo trộn thứ tự các node.
   - Khi người dùng bắt đầu chạm vào một node (`HomeUiEvent.JourneyNodeGestureStarted(itemId)`), `HomeViewModel` bật cờ `activeGestureItemId`: mọi thay đổi danh sách đến trong lúc ngón tay chưa nhấc lên (`Live add`, `Sync`, `Archive/Delete`) được giữ trong hàng đợi `pendingCandidates` và chỉ cập nhật lên UI sau khi nhận `JourneyNodeGestureEnded` hoặc `JourneyNodeClicked(itemId)`. Nếu món đang chạm vừa bị xóa trước khi click hoàn tất, click đó bị hủy an toàn thay vì mở nhầm món kế tiếp.
3. **Vẽ Native `BudgetCreditCard` & `ItemJourneySection` bằng Compose (`AC-05`, `AC-13`, `AC-17`, `AC-31`):**
   - `BudgetCreditCard` dùng `Surface` / `Box` với gradient cam Candy (`CandyColors.HomeV2.candyOrange` $\rightarrow$ `CandyColors.HomeV2.candyOrangeShadow`), vẽ chip EMV vàng, sóng contactless và hoạ tiết mèo trắng bằng Compose `Canvas` / vector, dùng `heightIn(min = 192.dp)` để thẻ tự giãn chiều cao khi `fontScale = 2.0f`.
   - `ItemJourneySection` dùng `BoxWithConstraints` để tính đường kính node (`80.dp..96.dp`) và tọa độ tâm $(x_i, y_i)$ theo mẫu ziczac bên trái (`leftColumnX`, `midColumnX`), vẽ $n - 1$ đoạn đường nối nét đứt màu lavender (`#BDA8EB`) bằng `Canvas` phía sau các node (từ mép đường tròn $i$ tới mép đường tròn $i+1$, $n < 2$ không vẽ dây nối), và đặt 1 mascot Chanko (`R.drawable.img_chanko_mascot`) ở khoảng trống bên phải không chồng lên vùng chạm của bất kỳ node nào.
4. **Thứ tự Đọc TalkBack & Reduced Motion (`AC-31`):**
   - Mỗi node Journey dùng `semantics(mergeDescendants = true)` đọc đầy đủ: *"[Tên món], trạng thái [Nhãn trạng thái tiếng Việt], chạm đúp để mở chi tiết"*, được sắp xếp theo đúng thứ tự `createdAt DESC` trong cây Compose.
   - Chip vàng, sóng contactless, mèo trang trí trên thẻ cam, mascot Journey và đường nối `Canvas` đều đặt `contentDescription = null` / `clearAndSetSemantics {}` để TalkBack không dừng vào các phần tử trang trí.

---

## 3. Task List

### Phase 1: Foundation, Tokens & Domain/State Engine (`home-v2-domain-state`)
- [x] **Task 1:** Bổ sung Semantic Color Tokens v2.0 (`Color.kt`), Strings (`strings_home.xml`) & Domain Logic (`JourneySelector` + `BudgetV2Calculator` trong `HomeDomainLogic.kt`) (`AC-03`, `AC-06`..`AC-12`, `AC-25`)
- [x] **Task 2:** Nâng cấp `HomeUiState`, `HomeUiEvent`, `HomeUiEffect`, `HomeFakeDataSource` & `HomeViewModel` (SectionState độc lập, Gesture Snapshot Lock, Offline Cache/NoCache, Insight Hide) + Unit Tests (`AC-08`, `AC-18`..`AC-27`, `AC-30`)

### Checkpoint 1: Foundation & Domain/ViewModel Tests
- [x] Toàn bộ unit tests cho `HomeDomainLogicTest`, `HomeViewModelTest` và `ColorArchitectureTest` chạy PASS (`./gradlew :app:testDevDebugUnitTest`).

### Phase 2: Core v2.0 UI Components (`home-v2-budget-credit-card`, `home-v2-item-journey`)
- [x] **Task 3:** Xây dựng `BudgetCreditCard.kt` 3D Candy Orange với đầy đủ trạng thái (`Normal`, `OverBudget` Coral, `NoBudget`, `MissingPrice`, `Skeleton`, `Error + Retry`, `OfflineNoCache`) (`AC-03`..`AC-09`, `AC-18`..`AC-22`, `AC-31`)
- [x] **Task 4:** Xây dựng `ItemJourneySection.kt` (*"Món đồ gần đây"*): Ziczac 1..5 Nodes tròn `80–96dp`, Đường nối nét đứt Lavender, 6 Status Badges, Mascot bên phải & Gesture-Safe Touch (`AC-10`..`AC-17`, `AC-23`, `AC-24`, `AC-31`)

### Checkpoint 2: Core v2.0 Components
- [x] Các component mới biên dịch sạch sẽ (`./gradlew :app:compileDevDebugKotlin`), tuân thủ tuyệt đối `ColorArchitectureTest`.

### Phase 3: Header, Observation, HomeScreen Assembly & Navigation (`home-v2-header-observation-shell`)
- [x] **Task 5:** Cập nhật `HomeHeader.kt`, `ObservationCard.kt` (kèm `InsightExplanationDialog`), lắp ghép `HomeScreen.kt` 4 khu vực chuẩn v2.0 và xóa bỏ `CompactLivingRoom.kt`, `YourDecisionsSection.kt`, `HomeSheetHost.kt`, `BudgetCard.kt` cũ (`AC-01`, `AC-02`, `AC-26`, `AC-27`)
- [x] **Task 6:** Kết nối `MainNavigation.kt`, Hoàn thiện bộ Responsive Previews v2.0 & Kiểm thử Nghiệm thu Toàn diện 31 AC (`AC-15`, `AC-16`, `AC-28`..`AC-31`)

### Checkpoint 3: Complete & Ready for Review
- [x] Tất cả 31 Acceptance Criteria (`AC-01`..`AC-31`) vượt qua kiểm thử, `./gradlew :app:testDevDebugUnitTest` và `./gradlew :app:assembleDevDebug` xanh 100%.

---

## 4. Risks and Mitigations

| Rủi ro | Mức độ | Biện pháp giảm thiểu |
| :--- | :---: | :--- |
| Mascot bên phải đè lên vùng chạm của node Journey trên màn hình hẹp `320dp` | Cao | Đo `maxWidth` bằng `BoxWithConstraints`: khi `maxWidth < 360.dp`, thu đường kính node về `80.dp`, thu nhỏ chiều rộng mascot và dịch sát lề phải; đặt các node sau mascot trong thứ tự vẽ `Box` (z-index cao hơn) và tắt pointer input trên mascot (`AC-17`, `AC-31`). |
| Đổi `itemId` ngay dưới ngón tay khi có món mới hoặc đồng bộ nền lúc người dùng đang nhấn giữ node | Cao | Dùng `pointerInput` lắng nghe `awaitFirstDown` để phát `JourneyNodeGestureStarted(itemId)` trước khi click; `HomeViewModel` hoãn áp dụng danh sách mới cho tới khi gesture kết thúc (`AC-23`, `AC-24`). |
| Vi phạm `ColorArchitectureTest` khi thêm màu thẻ cam, Coral, Lavender connector và 6 màu trạng thái | Trung bình | Khai báo toàn bộ mã hex một lần duy nhất trong `CandyPalette` và экспоrt qua `CandyColors.HomeV2` trong `Color.kt`. |
| Tràn chữ trên `BudgetCreditCard` khi bật `fontScale = 2.0f` trên màn `320dp` | Trung bình | Sử dụng `heightIn(min = 192.dp)` thay vì khóa cứng `aspectRatio`, cho phép các dòng *Đã chi / tổng ngân sách* và chu kỳ tự xuống dòng (`FlowRow` / `Column`) (`AC-31`). |

---

## 5. Open Questions

- Không còn câu hỏi mở (người dùng đã phê duyệt Capability Map, Spec chi tiết và phương án mở `InsightExplanationDialog` cho *Góc nhìn từ Chanko*).
