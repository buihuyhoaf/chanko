# Screen và component specifications

Approved UX theo29 mã ở07; chỉ exact dimensions, microcopy và sort defaults ngoài baseline là Reference. Backend gaps theo02/08.

## Navigation và state restoration

MyItems thuộc tab thứ hai của Home / My Items / Budget / Insights. Giữ nút+ toàn cục; không thêm FAB hoặc nút Add trùng ở app bar. Archived Items vào từ menu. MyItems → ItemDetail(itemId) → TransactionDetail(purchaseId). MyItems → ArchivedItems → ItemDetail(itemId, archiveContext). Route chỉ truyền opaque ID, không truyền object snapshot làm source of truth. Bottom sheets nhận ID/ngữ cảnh và đọc state hiện tại.

Back đóng sheet/dialog trước, rồi quay lại route nguồn. Dữ liệu form chưa lưu: hỏi “Bỏ thay đổi?” trước khi đóng; không hỏi nếu không dirty. Back từ detail giữ query, filters, sort, vị trí scroll ở list nguồn. Khi item không còn match filter sau mutation, list bỏ item và cập nhật count; không tự đổi filter của user. Sau process death phục hồi view state và form đang nhập theo chính sách bảo mật của app; không tự submit request.

### MyItems

Approved ITEM-01..07:

| Thứ tự | Component | Hành vi |
|---|---|---|
| 1 | App bar My Items + menu | Archived Items trong menu; không inline add trùng nút+ toàn cục |
| 2 | Compact3D Chanko Hero | Canonical cat, không product media; copy/kích thước cụ thể Reference |
| 3 | Overview tiles All Items / Cooling Off / Ready | Counts thật của tập non-archived, không dùng page length; tap lọc tương ứng |
| 4 | Search + hai cấp filter + Sort | Cấp1 All/Active/Purchased/Skipped; cấp2 trong Active: Saved/Considering/Cooling Off/Ready/Postponed |
| 5 | Result count, applied filters | Query AND filter; giữ khi Back; tile và filters đồng bộ |
| 6 | List mặc định card ngang compact read-only | Thumbnail trái, tên/giá/status rõ; tap mở detail; không More, checkbox, swipe mutation hoặc Buy Again trên card |
| 7 |4 navigation tabs + global+ | Không FAB lặp; system insets đầy đủ |

Reference selection mapping để implement nhất quán: tile All Items→All; Cooling Off→Active/Cooling Off; Ready→Active/Ready; giữ query/sort. Cấp2 single selection, Active chưa chọn subfilter nghĩa tất cả active items. Saved→WISHLIST; Considering→CONSIDERING; Cooling Off→WAITING+ACTIVE; Ready→derived review pending; Postponed→event/label hợp lệ (CONSIDERING). Active không phải enum server. Derived subfilters có thể giao nhau (Considering/Ready/Postponed); không cộng counts subfilters thành All Items hoặc tự hard-code exclusive precedence chưa có nguồn. Count/category/filter semantics toàn cục theo contract; marker còn thiếu thì báo integration gap, không giấu feature đã Approved bằng cách coi optional.

Default List View và thumbnail trái là Approved. Width72dp/title2 dòng/spacing12dp là Reference; cho wrap/grow ở font200%, không ellipsis tiền. Frequent indicator dùng transactions hợp lệ, không quantity, không khuyến khích mua thêm. Adaptive theo01.

Empty global: Chanko canonical + hộp kẹo, Save an Item; filtered empty: Clear Filters (copy Việt Reference), không thêm FAB. Chưa có canonical composition thì giữ text/slot trống có ghi thiếu asset, không tự vẽ mèo/hộp để giả approved art. Offline có cache giữ banner/timestamp; no-cache không tuyên bố account rỗng. Đến hạn offline có Ready tạm thời và label chưa đồng bộ. Loading/cache refresh/error/pagination/retry không xóa phần dữ liệu còn dùng được; result query cũ không ghi đè query mới.

### ItemDetail

| Thứ tự | Thành phần | Dữ liệu và action |
|---|---|---|
| 1 | Top bar Back / “Chi tiết món đồ” / More | Back về đúng list nguồn; More Options theo quyền hiện tại |
| 2 | Pastel Candy Product Stage Approved; ratio4:3 Reference, radius24 | Trong handoff/preview chỉ dùng neutral dummy placeholder (kể cả trạng thái loaded); Fit, không stretch; không mèo hoặc minh họa túi/tai nghe/giày |
| 3 | Tên / giá dự kiến mỗi đơn vị / category optional | Title wrap, không đồng nhất giá dự kiến với giao dịch gần nhất |
| 4 | ItemStatus chip + CoolingOffPanel khi có | Status đọc đúng server; Ready hiển thị “Đến lúc xem lại” riêng, có provisional label khi offline đến hạn; deadline có ngày giờ/timezone dễ hiểu |
| 5 | SpeechBubble dưới status | Text riêng; mascot supporting ngoài media container; có thể không có hình khi thiếu canonical |
| 6 | Actions theo status | Xem bảng CTA; hành động secondary đủ tương phản |
| 7 | Expandable item details | Note, lý do, source link nếu có; thiếu thì ẩn hàng, không bịa; external link mở theo cơ chế an toàn app |
| 8 | AI evaluation section | Latest verdict có label “Nhận định của Chanko”, thời gian; xem lại history; không gọi BUY thành Purchased |
| 9 | Purchase history | Count giao dịch hợp lệ, tổng chi lịch sử nếu server cung cấp, list mới nhất trước; tap TransactionDetail |
| 10 | Collapsible Decision Journey | Chronological events, phiên cũ giữ nguyên; rút gọn mặc định và “Xem thêm” |

Approved DETAIL-06: sticky Primary CTA + More Options vùng đáy có nền surface, insets, một primary; phần cuối cuộn được lên trên CTA. Không để Buy Again là primary nổi bật kiểu thúc đẩy mua. Không autoplay âm thanh.

| Item state | Primary | Secondary / More |
|---|---|---|
| Wishlist | “Cân nhắc cùng Chanko” → AI existing | “Ghi nhận mua”, “Bắt đầu chờ”, Edit, Skip |
| Considering thường | “Xem lại quyết định” → AI existing | “Ghi nhận mua”, “Bắt đầu chờ”, Skip |
| Waiting ACTIVE | CTA contextual; countdown nổi bật | Adjust / Skip Waiting trong More; Direct Mark as Purchased mở sheet; Skip item riêng và rõ nghĩa |
| Ready derived | “Xem lại nhu cầu” → inline review block | Review ngắn có thể bỏ qua (giữ Ready); Not Anymore / Need More Time2 nhánh / Still Want It; ghi mua vẫn chủ động |
| Purchased | “Xem lịch sử mua” → scroll section | Buy Again trong More → Record Purchase; Edit trong More |
| Skipped | Ưu tiên đọc lịch sử, không thúc đẩy mua | Reconsider/Direct Mark as Purchased trong More theo quyền |
| Postponed (Considering + event) | Contextual review; next review time khi có | Need More Time2 nhánh; không timer/status mới |
| Archived | “Khôi phục món đồ” | View item/history; quyền edit/correction theo capability contract, các guard archived còn Pending Verification |

Speech Reference: Wishlist “Cứ để ở đây, bạn có thể cân nhắc sau.”; Waiting “Bạn vẫn có quyền quyết định trong thời gian chờ.”; Ready “Bạn còn muốn món này không?”; Purchased “Lịch sử mua được lưu ở bên dưới.”; Skipped “Bạn có thể xem lại khi cần.” Không bịa lý do hoặc khen mua hàng.

Purchase row: purchased_at theo timezone tài khoản, actual_unit_price×quantity, total_amount, PAY_NOW/CREDIT + snapshot card label nếu có. Item hiện Purchased nhưng zero purchase là state hợp lệ sau correction: hiện empty history, không dựng giao dịch từ estimated_unit_price. History partial load/lỗi không làm cả detail fail; hiển thị retry section. Not found/forbidden không rò tên/ảnh từ account khác; Back về list và refresh phù hợp.

### TransactionDetail

Route purchaseId; item reference do server trả. Header “Chi tiết giao dịch”, Back; body card tên item + thumbnail trung tính 56dp, ngày mua/timezone, đơn giá thực tế, quantity, tổng, payment type/card label, note nếu có; created/updated metadata khi contract cung cấp. Link “Xem món đồ” mở ItemDetail kể cả archived.

Hai action rõ: “Sửa thông tin nhập sai” và “Xóa giao dịch nhập sai”. Không gọi Delete là hoàn tiền; không thêm refund amount hoặc repayment. Edit chuyển sang inline form trong cùng màn, giữ purchaseId và expectedVersion. Trường như Record Purchase; title “Sửa giao dịch”, CTA “Lưu thay đổi”; không dùng create endpoint hoặc tạo Purchase mới. Tổng preview riêng, tổng chính thức lấy response server.

Delete dialog nêu ngày/tổng để xác nhận đúng giao dịch: “Giao dịch sẽ được loại khỏi chi tiêu. Trạng thái món đồ và lịch sử đánh giá vẫn được giữ.” CTA “Giữ giao dịch” / “Xóa giao dịch”. Sau ack pop về nguồn, refresh purchases/count/budget/cycles/recap liên quan. Không có undo khôi phục purchase nếu contract chưa hỗ trợ. Audit revision lưu phía server; màn chỉ hiển thị lịch sử correction nếu contract có read model, không tự dựng audit từ cache.

Archived parent có banner “Món đồ này đang được lưu trữ”; correction không tự restore. Nếu giao dịch đã xóa ở thiết bị khác, hiện “Giao dịch không còn tồn tại” và Back; stale edit giữ dữ liệu user để so sánh với bản mới, không overwrite im lặng.

### ArchivedItems

App bar Back, “Đã lưu trữ”, search theo tên Reference. List dùng ItemCard + archived indicator; status vẫn là status cũ. Action rõ “Khôi phục”; tap mở archived detail. Reference sort archived_at mới nhất trước, tie itemId. Không dùng filter của MyItems làm mất item ở đây; giữ query riêng. Empty “Chưa có món đồ được lưu trữ”, action Back; query-empty cho clear search. Offline vẫn đọc cache, restore disabled kèm lý do. Sau restore ack remove row, snackbar “Đã khôi phục món đồ”; quay MyItems phải tôn trọng filters đang chọn nên item có thể chưa xuất hiện nếu không match.

## Ba bottom sheets

### Filter & Sort

Sheet top radius28, drag handle trang trí, title “Lọc & sắp xếp”, close action có label. Nội dung scroll; footer “Đặt lại” + “Áp dụng”, không bị IME/system bars che. Selection tạm chỉ có hiệu lực sau Apply; Back/dismiss hủy thay đổi chưa apply. Apply giữ nguyên search.

Approved ITEM-04: hai cấp All/Active/Purchased/Skipped và Active→Saved/Considering/Cooling Off/Ready/Postponed, dùng chung filter state với chips/overview tiles. Không thay bằng5 status chips/multi-select status hoặc Ready optional toggle. Ready/Postponed chỉ filter UI không payload ItemStatus. Selection cơ chế single-select là Reference triển khai, xem MyItems mapping; Apply giữ query, reset vềAll/default sort. Category nếu có contract là phần Reference, không thêm taxonomy.

Sort single-select: cập nhật gần nhất (default), thêm mới gần nhất, giá dự kiến thấp→cao/cao→thấp, tên A→Z. Stable tie-breaker itemId; missing price cuối list cho cả hai chiều. Nếu backend không hỗ trợ một sort thì không sort riêng từng trang rồi giả global order: thêm contract hoặc ẩn option chưa hỗ trợ. Reset về All/default, chờ Apply. Query/filter/sort thay đổi reset pagination cursor; count lấy đúng tập kết quả, không số phần tử page hiện tại.

### More Options

Item context hiển thị tên + status để tránh thao tác nhầm. Approved placements: Edit Item, Direct Mark as Purchased, Buy Again (khi phù hợp), Adjust/Skip Waiting đều ở More; Direct Mark as Purchased và Buy Again mở sheet không bắt Quick Check. Các action khác contextual theo02 và contract. Skip Waiting phải tách Skip item: kết thúc phiên rồi mở lựa chọn, không tự SKIPPED. Ẩn action không liên quan, nhưng action cần online nên disabled + lý do khi offline. Đừng dồn tất cả action vào một màn không phân nhóm. Chọn action đóng sheet rồi điều hướng/hỏi xác nhận.

Archive mở dialog mô tả bảo toàn history/budget. Archive ACTIVE chưa chốt: không hard-code guard hoặc tự cancel; theo capability/contract thực, chưa có thì báo gap. Hard delete item không nằm trong Reference menu này; SPEC có quyền xóa nhưng chi tiết retention chưa chốt. Nếu repo có delete flow, giữ ngoài scope bàn giao cho đến đối chiếu quyết định gốc, tuyệt đối không cascade purchases.

### Record Purchase

Mục tiêu ghi một lần mua thủ công vào itemId hiện có; không Quick Add trong sheet này. Header “Ghi nhận mua hàng” hoặc “Ghi nhận lần mua mới”; summary tên item, thumbnail 48dp dummy khi thiếu ảnh; không mascot. Có text “Thao tác này chỉ ghi lại giao dịch bạn đã thực hiện.”

| Field | Default / validation | Error / semantics |
|---|---|---|
| actual_unit_price | Bắt buộc; VND nguyên ≥0; Reference blank để user nhập giá thực | “Nhập đơn giá thực tế bằng đồng Việt Nam”; không lấy estimated làm số tiền xác nhận ngầm |
| quantity | Default1, INTEGER≥1 | “Số lượng phải là số nguyên từ 1”; keyboard số, vẫn validate paste |
| purchased_at | Reference thời điểm hiện tại theo timezone tài khoản, editable picker | Hiện ngày/giờ/timezone; server quyết định giới hạn backdate/future, không tự đặt policy |
| payment_type | PAY_NOW mặc định | PAY_NOW / CREDIT, có text loại thanh toán |
| card | Optional khi CREDIT | “Không chỉ định thẻ”; chỉ card cùng owner đúng loại; PAY_NOW xóa card selection |
| note | Optional | Length lấy contract; không cắt âm thầm |
| total preview | Read-only = unit×qty | “Tổng dự kiến ghi nhận”; tiền chính thức sau response |

Validation local phục vụ UX; server kiểm tra lại. Không chấp nhận decimal, scientific notation, số âm, overflow hoặc text không parse được; format separator locale không được làm thay đổi trị số. Nếu price=0 được server nhận thì total=0 hợp lệ; không ép lớn hơn0 do cảm tính. Currency VND, không thêm FX.

CTA “Ghi nhận giao dịch” enabled khi fields hợp lệ + online + item writable + không pending. Pending không cho gửi thêm nhưng vẫn có feedback; close trong khi outcome unknown không được tự phát request mới. Server error hiện inline/global phù hợp và giữ dữ liệu. Timeout giữ key cùng ý định; sau reconcile thấy purchase đã ghi thì đóng sheet success một lần. Explicit new Buy Again có key mới và form mới, không replay tự động.

Khi thành công: item PURCHASED, ACTIVE resolved nếu có, list/history/count/budget refresh từ server. Không bắn confetti/âm thanh mua thành công; thông báo trung tính. Không hiển thị prompt mua thêm. Dismiss form dirty hỏi bỏ thay đổi; không biến form này thành Offline Manual Draft vì draft item là khái niệm khác.

## Component contracts chung

| Component | Input tối thiểu | Trách nhiệm |
|---|---|---|
| ProductMedia | image reference?, load state, semantic name | Handoff/preview dummy-only; cơ chế ảnh thật hiện hữu nếu triển khai riêng theo SPEC: tối đa1 ảnh/item, URL hết hạn refresh qua app, không persist signed URL vĩnh viễn |
| ItemStatusChip | ItemStatus | Label/icon/color; không tự chuyển state |
| ReadyReviewBanner | server-derived eligibility + deadline | Render derived view, review choices; không mutation từ countdown |
| CoolingOffPanel | periodId/status/ends_at/version + connection | countdown display; actions theo period thật; giữ lịch sử riêng |
| MascotSpeech | text, canonical asset optional, reduceMotion | Render tách media; asset thiếu vẫn có text; không gọi LLM vì tap trang trí |
| PurchaseHistory | records, valid count, loading/error, cursor | count transactions; item total không suy từ loaded page; mỗi row dùng purchaseId |
| MoneyText | exact integer + VND + semantic label | Locale format, full amount, TalkBack đọc đơn vị |
| MutationFeedback | pending/ack/error/unknown | Không success trước ack, retry key và expected version đúng contract |
| ConnectivityBanner | online/cacheAt | Giải thích cache stale, không giả dữ liệu server đã mới |

## Accessibility và kiểm tra bố cục

Focus theo thứ tự đọc, khi sheet mở focus vào title/control đầu; đóng trả focus về nút mở. Modal không cho TalkBack chạm nội dung phía dưới. Error được đọc và gắn field; selected chips/radio có state semantics. Product placeholder trang trí không đọc “image” dư thừa; tên item text đã đủ, ảnh chỉ thêm content description nếu cần phân biệt. Action icon luôn có label.

Test 200% font, width320dp và360dp Reference, portrait/landscape, IME, gesture/3-button navigation. CTA tăng cao nếu wrap; không cố định chiều cao card có text. Không chỉ phân biệt status bằng màu. Reduced Motion dùng ảnh tĩnh/transition nhẹ; không đổi kết quả nghiệp vụ. Mọi pending state có thông báo rõ, không loading spinner vô hạn thiếu retry/reconcile.

## Ranh giới triển khai v1.0

Mindful Ready review có thể bỏ qua; Ready duy trì đến action xử lý rõ, không tự clear do mở/Back/dismiss. Still Want It không mua hoặc đổi verdict tự động; thời điểm clear sau lựa chọn này cần contract. Need More Time2 nhánh Approved: new wait hoặc Considering+Postponed. Next review time hiển thị khi có; chưa có không bịa ngày.

Purchase Confirmation Bottom Sheet DETAIL-11 chính là Record a Purchase trong3 sheets, không thêm sheet thứ4. Mark as Purchased là user mở sheet rồi xác nhận transaction; không ép Quick Check hoặc đặt nhãn Purchased thiếu giao dịch. Decision Journey/Purchase History là section, không route riêng.

Product Hero mọi fixtures/preview dùng dummy manifest; canonical cat chỉ ở MyItems hero/empty/bubble dưới status, không trong/cạnh/chồng media. Assets thiếu thì báo thiếu, không tự tạo mascot.
