> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Roadmap Phase 0–11 theo component

Mỗi phase là một đơn vị review. Không suy ra các phase cùng số trong checkout khác là đã xong. UI fixture có thể tiến hành sau contract cần thiết; production integration không vượt qua pending business rules.

| Phase | Component / trọng tâm | Phụ thuộc | Gate |
|---|---|---|---|
| 0: Discovery & TECH alignment | Contracts, nguồn chuẩn, mapping repo | Không | Không sửa UX; mọi quyết định blocking có owner và trạng thái; không tự nhận backend đã có. |
| 1: Candy Pop foundation | Theme tokens, Nunito, primitives | 0–10 | Chữ Việt đủ glyph; contrast và target được kiểm tra; phân biệt token đề xuất với approved. |
| 2: Home shell & Header | HomeRoute, HomeScreen, HomeHeader | 0–1 | Tên dài/font lớn không tràn; nav không che nội dung; chưa gắn network vào composable. |
| 3: Compact Living Room | CompactLivingRoom, ChankoCatArtwork, RoomProp | 0–2 | Đủ 4 prop, hit target không đè nhau, reduced motion tĩnh; missing asset vẫn là blocker visual final. |
| 4: Chanko Dialogue | ChankoDialogue, bubble bên trong Living Room | 0–3 | Font 2.0 không cắt text; bỏ spacer khi absent; không suy diễn AI cho phép mua. |
| 5: Quick Check + Save Item | QuickActionsRow, QuickCheckEntry, SaveItemEntry | 0–2 | Chạm đúng intent, không tự save; unavailable có giải thích; navigation vẫn dùng được. |
| 6: Budget | BudgetCard, BudgetAmounts, BudgetProgress | 0–2 | Không chia 0, không cắt tiền, không biến null thành 0; over-budget 120% vẫn đọc đúng. |
| 7: Your Decisions | YourDecisionsSection, DecisionRow | 0–2 | Không vượt 3, đúng ưu tiên toàn tập; countdown không tự đổi domain status; mọi row ID đúng. |
| 8: Optional content | ObservationCard, DraftResumeCard | 0–2 | Không có khoảng trống khi absent; draft stale xử lý an toàn; không bịa observation. |
| 9: Navigation & sheets | ChankoBottomNavigation, HomeSheetHost, GlobalQuickActionSheet, ContextualSheet | 0–8 | + mở Global và giữ selected; context đúng; dismiss không commit; back đóng modal trước. |
| 10: State & data integration | HomeRoute/presenter/ViewModel adapter theo repo | 0–9 | Không network trong UI, không fake-data rò production; unknown không thành số 0; stale/time không thay authoritative status. |
| 11: QA & handoff sign-off | Toàn Home và checklist phát hành | 0–10 | P0 acceptance đạt, blocker TECH/asset đóng trước production; không báo test pass nếu chưa chạy, không dùng emulator/VM/AVD. |

## Phase 0 — Discovery & TECH alignment

Công việc: Khảo sát repo đích chỉ khi đã được giao triển khai; lập map module/design system/navigation/data có bằng chứng. Chốt status mapping, rank toàn cục, budget/date, sheet routes, asset ownership. Ghi unknown và owner, không đoán file path.

Bàn giao: Decision log + mapping các component vào kiến trúc thực + blocker list.

Exit: Không sửa UX; mọi quyết định blocking có owner và trạng thái; không tự nhận backend đã có.

Prompt: [phase-00](prompts/phase-00.md)

## Phase 1 — Candy Pop foundation

Công việc: Map token vào theme thật, tạo primitive card/text/action theo conventions, font fallback, semantics, reduced motion. Không đổi theme toàn app ngoài scope.

Bàn giao: Theme mapping + preview primitive, inventory asset/license.

Exit: Chữ Việt đủ glyph; contrast và target được kiểm tra; phân biệt token đề xuất với approved.

Prompt: [phase-01](prompts/phase-01.md)

## Phase 2 — Home shell & Header

Công việc: Dựng Scaffold/content scroll/insets và header stateless bằng fixtures; reserved section slots đúng thứ tự. Không thêm hero calendar cũ.

Bàn giao: Shell + header previews và event wiring seam.

Exit: Tên dài/font lớn không tràn; nav không che nội dung; chưa gắn network vào composable.

Prompt: [phase-02](prompts/phase-02.md)

## Phase 3 — Compact Living Room

Công việc: Dựng scene compact, mèo trắng 3D nằm chống cằm trên đệm tím đúng ảnh, asset final còn thiếu và 4 prop đúng nhãn. Asset thiếu dùng placeholder được đánh dấu trong preview. Emit typed PropTapped, không tự tạo dữ liệu pause.

Bàn giao: Scene preview 320/393/600dp + prop event contracts.

Exit: Đủ 4 prop, hit target không đè nhau, reduced motion tĩnh; missing asset vẫn là blocker visual final.

Prompt: [phase-03](prompts/phase-03.md)

## Phase 4 — Chanko Dialogue

Công việc: Bubble và text thật nằm ở góc trên trái bên trong Living Room như ảnh chuẩn; mèo ở giữa-phải, không biến dialogue thành section bên dưới. State absent/loading/content; chỉ CTA đã hỗ trợ; không gọi AI trực tiếp.

Bàn giao: Scene + dialogue overlay previews ngắn/dài/absent + action seam.

Exit: Font 2.0 không cắt text; bỏ spacer khi absent; không suy diễn AI cho phép mua.

Prompt: [phase-04](prompts/phase-04.md)

## Phase 5 — Quick Check + Save Item

Công việc: Hai entry độc lập, reflow dọc khi chật. Emit intents, capability state và chống tap lặp tại tầng xử lý. Flow cụ thể theo Phase 0.

Bàn giao: Entry previews + kiểm tra hai intent.

Exit: Chạm đúng intent, không tự save; unavailable có giải thích; navigation vẫn dùng được.

Prompt: [phase-05](prompts/phase-05.md)

## Phase 6 — Budget

Công việc: Render tiền/currency chuẩn, unknown/zero/negative/over-budget, progress clamp visual và nhãn thực; cycle chỉ khi có. Presenter tính theo quy tắc TECH.

Bàn giao: Budget previews + formatter/ratio tests cần thiết.

Exit: Không chia 0, không cắt tiền, không biến null thành 0; over-budget 120% vẫn đọc đúng.

Prompt: [phase-06](prompts/phase-06.md)

## Phase 7 — Your Decisions

Công việc: Adapter canonical IDs theo policy; stable rank Ready→Waiting→Considering→Saved rồi take 3; stable key. Action theo capability và route đã chốt.

Bàn giao: Ranking tests + previews 0/1/3/>3 và trạng thái.

Exit: Không vượt 3, đúng ưu tiên toàn tập; countdown không tự đổi domain status; mọi row ID đúng.

Prompt: [phase-07](prompts/phase-07.md)

## Phase 8 — Optional content

Công việc: Render chỉ dữ liệu thật từ state, null bỏ cả spacer; resume draft truyền ID. Không thêm discard hoặc insight tự tạo.

Bàn giao: 4 tổ hợp presence previews + resume event test.

Exit: Không có khoảng trống khi absent; draft stale xử lý an toàn; không bịa observation.

Prompt: [phase-08](prompts/phase-08.md)

## Phase 9 — Navigation & sheets

Công việc: Bốn tab + action trung tâm; Global có Quick Check/Save Item; typed contextual entry cho bốn prop và decision theo contracts đã chốt. Một sheet tại một thời điểm, back/dismiss/focus/IME.

Bàn giao: Nav và sheet previews + transition tests.

Exit: + mở Global và giữ selected; context đúng; dismiss không commit; back đóng modal trước.

Prompt: [phase-09](prompts/phase-09.md)

## Phase 10 — State & data integration

Công việc: Thay fixtures tại route bằng nguồn data được xác nhận, giữ stateless children. Lifecycle, error/retry theo section, freshness/offline, injected clock/timezone, duplicate click, process recreation theo policy.

Bàn giao: Data mapping log + state integration tests và tình trạng runtime.

Exit: Không network trong UI, không fake-data rò production; unknown không thành số 0; stale/time không thay authoritative status.

Prompt: [phase-10](prompts/phase-10.md)

## Phase 11 — QA & handoff sign-off

Công việc: Chạy matrix phù hợp dự án, screenshot review mới với design/product, kiểm tra thiết bị thật khi có. Đóng pending có bằng chứng, ghi limitation còn lại.

Bàn giao: QA report, screenshot index, asset/license status và acceptance sign-off.

Exit: P0 acceptance đạt, blocker TECH/asset đóng trước production; không báo test pass nếu chưa chạy, không dùng emulator/VM/AVD.

Prompt: [phase-11](prompts/phase-11.md)

## Visual gate áp dụng mọi phase v2

Mở ảnh chuẩn người dùng. Phase 3 reserve bubble trong scene; Phase 4 tích hợp bubble child, không tạo section riêng. Phase 11 đối chiếu bố cục/pose/color hierarchy của ảnh mới, không layout v5 cũ. Chi tiết mapping tại 00-VISUAL-REFERENCE.md.
