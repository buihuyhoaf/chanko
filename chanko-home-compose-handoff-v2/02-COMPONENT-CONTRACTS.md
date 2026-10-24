> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Component tree và contracts đề xuất

Tên dưới đây là logical API, không phải file path hoặc package đã có trong repository. Phase 0 mapping vào kiến trúc thực tế. Stateless component nhận state + callback; route/ViewModel xử lý dữ liệu và effect. UI không gọi repository, timer hệ thống, network hay navigation controller trực tiếp trong từng card.

```text
HomeRoute
└─ HomeScreen(HomeUiState, onAction)
   ├─ HomeHeader
   ├─ LazyColumn / HomeContent
   │  ├─ CompactLivingRoom
   │  │  ├─ ChankoCatArtwork
   │  │  ├─ RoomProp × 4 (Hourglass, Calendar, Wallet, PauseJar)
   │  │  └─ ChankoDialogue (bubble overlay trong scene)
   │  ├─ QuickActionsRow (QuickCheckEntry, SaveItemEntry)
   │  ├─ BudgetCard (BudgetAmounts, BudgetProgress)
   │  ├─ YourDecisionsSection → DecisionRow × 0..3
   │  ├─ ObservationCard? 
   │  └─ DraftResumeCard?
   ├─ ChankoBottomNavigation (4 tab + CenterAddAction)
   └─ HomeSheetHost
      ├─ GlobalQuickActionSheet
      └─ ContextualSheet (typed context, one at a time)
```

## State đề xuất, không phải domain schema

```kotlin
// Pseudocode, chưa phải đoạn drop-in hoặc API đã tồn tại.
HomeUiState(
  // dialogue được truyền vào CompactLivingRoom; không render thành row riêng
  header: HeaderUi,
  room: SectionState<LivingRoomUi>,
  dialogue: SectionState<DialogueUi?>,
  budget: SectionState<BudgetUi>,
  decisions: SectionState<List<DecisionUi>>,
  observation: ObservationUi?,
  draft: DraftUi?,
  selectedTab: TopLevelTab,
  sheet: HomeSheet?,
  capabilities: HomeCapabilities
)
SectionState<T> = Loading | Content(value: T, freshness) | Error(message, retryKey)
HomeSheet = Global | Context(kind: ContextKind, entityId: String?)
ContextKind = Hourglass | Calendar | Wallet | PauseJar | Decision
DecisionStatus = Ready | Waiting | Considering | Saved
```

`Content(emptyList())` khác Error/Loading. Optional null là không hiển thị; nếu optional cần loading/error độc lập, mở rộng SectionState thay vì null giả. Freshness gồm snapshot/asOf và stale indicator khi hợp đồng data hỗ trợ. Error text là thông điệp người dùng đọc được, không dump exception.

MoneyUi phải chứa amount chính xác và currency; implementation chọn minor units/decimal theo TECH, tuyệt đối không lấy Float làm tiền. Date/time phải phân biệt Instant với ngày địa phương, inject clock/timezone vào formatter hoặc presenter. Giá unknown không in `0 ₫`. Fixture không định nghĩa serialization production.

## Contract từng component

| Component | Input tối thiểu | Output intent | Invariant / owner |
|---|---|---|---|
| HomeHeader | greeting, subtitle, displayName?, avatar?, capabilities | SettingsTapped, ProfileTapped | fallback avatar bằng chữ; không tự fetch |
| CompactLivingRoom | scene artwork, cat asset key, 4 PropUi, dialogue state, reducedMotion | PropTapped(kind) | pose mèo nằm trên đệm tím; 4 prop đúng ảnh; dialogue trong scene, không đè target |
| RoomProp | kind, label, optional summary, enabled | PropTapped(kind) | summary unknown không = 0; disabled có lý do |
| ChankoDialogue | message?, supportingLine?, tone, optional supported CTA | DialogueActionTapped(id) | text Compose trong scene; không tự sinh copy hoặc gọi AI |
| QuickActionsRow | canQuickCheck, canSave, busy | QuickCheckTapped, SaveItemTapped | phân biệt hai intent, state loading không khóa nav |
| BudgetCard | limit?, spent?, remaining?, ratio?, cycle?, freshness | BudgetTapped, Retry(Budget) | ratio visual clamp; tiền âm/unknown chính xác |
| YourDecisionsSection | rankedVisibleItems, totalCount?, sectionState | DecisionTapped(id), ViewAllTapped, Retry(Decisions) | ≤3, stable keys=id, dedup trước rank |
| DecisionRow | id, title, image?, money?, status, countdown?, capability | DecisionTapped(id) | không tự chuyển status khi timer hết |
| ObservationCard | id, title, text, evidenceText?, supported CTAs, canDismiss | ObservationExplainTapped(id), ObservationHideTapped(id), ObservationDismissTapped(id) theo capability | absent → bỏ section/spacer; không fabricate insight |
| DraftResumeCard | id, summary, updatedLabel? | ResumeDraftTapped(id) | draft phải tồn tại; không tự discard |
| ChankoBottomNavigation | selectedTab, labels, capabilities | TabSelected(tab), AddTapped | + không phải tab; 4 selected semantics |
| HomeSheetHost | typed active sheet?, context data, busy/error | DismissSheet, SheetAction(id) | chỉ một active; không commit khi dismiss |
| GlobalQuickActionSheet | allowed entries | QuickCheckTapped, SaveItemTapped | chỉ entry được phép; back/dismiss restore focus vào + |
| ContextualSheet | kind, optional entity ID, section data | context-specific supported action | validate entity tồn tại; action chưa chốt không hiển thị |

## Event/effect ownership

- UI emit HomeAction một chiều. Route/ViewModel quyết định state; adapter điều phối navigation/effect theo conventions repo sau Phase 0.
- Open sheet là UI state có thể khôi phục theo policy; navigation là effect xử lý một lần theo lifecycle của app. Không phát điều hướng trong composition.
- Reducer đề xuất: AddTapped → Global; PropTapped → Context(kind); DecisionTapped → Context(Decision,id) nếu route này được TECH duyệt, hoặc effect mở detail đã tồn tại. Không làm cả hai.
- Khi item biến mất trong lúc sheet đang mở: hiển thị “Món đồ không còn khả dụng”, chặn thao tác ghi và cho đóng. Không crash hoặc giữ thao tác cho ID khác.
- Lặp click khi sheet active: cập nhật/giữ một sheet; quyết định sheet transition khi dirty form theo TECH, không bỏ dữ liệu âm thầm.
- Retry theo section, hủy/cancel theo lifecycle. Không triển khai polling/timer mỗi row. Presenter dùng clock chung và source status authoritative.
- Scroll restoration, process death, session expired và auth route theo hợp đồng app; Phase 0 phải ghi rõ lựa chọn.

## Thuật toán quyết định có thể kiểm thử độc lập

Input cần unique IDs, status hợp lệ và thứ tự ổn định upstream. Khi ID trùng xuất hiện, dùng canonical record từ adapter theo rule TECH (không tùy tiện “first wins” cho production). Với fixture hợp lệ: stableSort bằng priority rồi take(3). Unknown status phải được adapter báo/loại hoặc map theo quyết định đã ghi; không tự gán Ready.

Ví dụ input `[Saved:S1, Waiting:W1, Ready:R1, Considering:C1, Ready:R2]` → `[R1,R2,W1]`. Input 5 Ready → 3 Ready đầu theo upstream. Phải áp dụng trên tập ứng viên đầy đủ hoặc query đã đảm bảo priority toàn cục; rank sau khi paginate tùy tiện có thể che Ready ở trang khác.

## Visual contract bắt buộc v2

Đọc 00-VISUAL-REFERENCE.md và mở references/approved-home-user-reference.png ở mọi visual review. HomeHeader cần subtitle và decoration sun/heart. CompactLivingRoom sở hữu cả SceneArtwork, cat nằm chống cằm trên đệm tím, prop overlays và ChankoDialogue. HomeContent không có item ChankoDialogue riêng. BudgetCard cần title cycle + DetailAction, spent/limit, remaining, warning badge và percentage label. DecisionRow đặt status/countdown theo bố cục ảnh, cho reflow font lớn. ObservationCard có icon tile, title, close, body/evidence và hai action Explain/Hide theo capability. Global sheet có title/handle/close và icon family riêng cho hai tile như ảnh.

HomeAction bổ sung: BudgetDetailsTapped; ObservationExplainTapped(id); ObservationHideTapped(id); ObservationDismissTapped(id). Không tự chọn persistence cho Hide/X. Chốt tại T11/T12 trước integration. Header subtitle và image-matching copy do state/localization cung cấp.
