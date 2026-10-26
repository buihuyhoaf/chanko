# Screen-by-screen layout và components
Mọi kích thước dưới đây là Reference triển khai Android, đơn vị dp; chữ sp. Viewport tham chiếu 390×844, nội dung nằm trong safe area, không bao gồm system bars. Responsive 320–599dp một cột; không hard-code tổng chiều cao. Nunito; Material 3 với shape/token Chanko. Safe area và IME lấy WindowInsets, không lấy kích thước ảnh làm thông số thiết bị.

## CL — Chat List
Thứ tự đọc: app bar → subtitle/hero → search/filter → thread list → fixed CTA. Không có input nhắn tin hoặc Send ở CL.

| Vùng | Layout và kích thước | Nội dung / event |
|---|---|---|
| Header | padding ngang20, top12; title24/32/700; More48×48 | “Chat với Chanko”; More mở sheet quản lý/lọc archived |
| Subtitle | body14/20, wrap; cách title4 | “Cùng suy nghĩ kỹ hơn trước mỗi quyết định” |
| Hero | min-height144, radius32, gradient pastel; mascot148 contain; text tối14/20 | Lời mời suy nghĩ trung tính; không LLM khi tap; hero scroll cùng nội dung |
| Search | cao tối thiểu48, radius16; icon24; clear48 target | Label “Tìm cuộc trò chuyện”; tìm tên thread và item/context theo contract; không mặc định full-text transcript |
| Filter | chips target48, gap8, horizontal scroll | Giữ như mockup: “Tất cả”, “Đang cân nhắc”, “Chờ xem lại”, “Đã mua”; menu “Đã lưu trữ”; clear/reset rõ |
| Secondary filter | sheet | Loại: Tất cả / Về món đồ / Tự do; kết hợp trạng thái item khi adapter hỗ trợ. Chờ xem lại/Ready là derived presentation, không lifecycle thread hoặc ItemStatus mới |
| Thread row | full width, min96, padding12, radius24; thumbnail56, gap12 | title17/24/700 tối đa2 dòng; preview14/20 tối đa2 dòng; timestamp12/16; type/status label; toàn row tap mở thread |
| Unread | dot8 và “Chưa đọc” semantics nếu server có | Không bịa unread/presence/typing/read receipts vì giống Messenger; nếu không có bỏ indicator |
| Footer | surface cream, padding20/12; CTA min56 radius20 | “+ Đoạn chat mới” cố định ở safe bottom, không đè row cuối; list content paddingBottom bằng footer đo thực tế+12 |

Status chips lọc theo item projection, không theo AI verdict; thread FREE chỉ match Tất cả khi status filter đang active, hoặc hiện khi chọn Tự do và reset status về Tất cả có thông báo. Đang cân nhắc map CONSIDERING; Đã mua map PURCHASED; Chờ xem lại dùng server/read-model Ready capability, không tự tạo ItemStatus hoặc suy từ chữ trong transcript. Adapter chưa hỗ trợ lọc global phải ghi gap/disable option có lý do, không sort/filter riêng từng page rồi giả toàn bộ.

Messenger-like là cấu trúc list: thread rows, thumbnail trái, title/preview/time, search và nhanh vào detail. Không sao chép logo Messenger hoặc thêm social/contact/presence features. Sort updatedAt theo server + stable ID; label “Hôm qua” theo timezone tài khoản; timestamp chính xác accessible. Không lấy page length làm tổng. Search/filter đổi query generation, reset cursor và scroll kết quả; Back từ CD phục hồi query/filter/scroll đã lưu.

Row More qua long-press hoặc nút More accessible mở sheet: Đổi tên / Lưu trữ hoặc Khôi phục / Xóa chat / Xem món đồ (nếu ITEM). Không swipe-only. Long title ellipsis ở list có thể chấp nhận nhưng tên đầy đủ đọc được ở detail/semantics. Query empty khác list empty; tạo CTA vẫn ở dưới.

## CD — Chat Detail
Thứ tự: app bar/context → connectivity/status → timeline → quick replies → composer. Chatbox duy nhất của tính năng ở đây; fixed trên IME/system bottom. Trong nested flow không có Chat tab hoặc bottom nav thứ hai.

| Vùng | Layout | Nội dung / event |
|---|---|---|
| App bar | min64; Back48, context avatar40, text flex, More48 | Item name hoặc “Trò chuyện tự do”; status/type phụ12/16; tap item context mở Item Detail; More thread sheet |
| Context provenance | row wrap14/20, padding12 | Sau bind hiện “Đã liên kết với [tên món đồ] từ [thời điểm]”; link item ID do repo, không match theo tên |
| Timeline | padding ngang20, gap12–16, lazy list | IDs và sequence server; ngày phân nhóm; tải cũ ở đầu; giữ anchor khi prepend |
| User bubble | tối đa82% width, padding12/16, radius20 | dark text trên lavender để đủ contrast, label semantics “Bạn”; timestamp12; trạng thái pending/unknown/error dạng text |
| Chanko bubble | avatar32 ngoài bubble, gap8; bubble flex | white surface; sender “Chanko”; body16/24; text chọn/copy theo app policy; render text an toàn, không raw HTML |
| Decision Card | trong assistant response hoặc timeline block, width available; min-height không cố định | title14/20, verdict20/28, rationale16/24; neutral mascot72 contain hoặc không ảnh; meta timestamp/item snapshot, CTA “Xem món đồ” min48 |
| Quick Replies | trước composer, chips min48, wrap hoặc horizontal scroll | text14/20; tap chỉ FillComposer; draft có nội dung thì giữ draft và hiện lý do, không overwrite/auto-send |
| Composer | surface trắng radius24, padding8; input min48/max120 rồi scroll nội bộ; Send48 | hint “Nhắn tin cho Chanko…” + accessible label; multiline; keyboard Send chỉ khi valid; khoảng8 với Send |
| Context add | icon add48 target khi FREE/capability hợp lệ | Mở item picker sheet và confirm same-thread; không upload ảnh, không tự tạo item hoặc là global + |

Send disabled nếu whitespace-only, offline, archived/unavailable, đang pending/unknown hoặc thiếu capability/precondition. Cho edit draft trong view khi phù hợp, không queue gửi. Sau Send tạo bubble optimistic có operation identity; không hiện “Đã gửi” trước ack. Focus không tự nhảy khỏi composer. Khi user đang đọc cũ: giữ scroll, hiển thị nút “Tin nhắn mới”; không kéo về cuối hoặc announce từng token.

Decision Card chỉ render từ structured evaluation đã complete/validate, ITEM context hợp lệ. FREE không có official verdict card. BUY label “BUY · Có thể cân nhắc mua”; WAIT “WAIT · Có thể chờ thêm”; SKIP “SKIP · Có thể bỏ qua”. Cả ba không đồng nghĩa quyết định user/status/purchase. BUY không dùng green success container, confetti, sparkles, âm thanh hoặc mascot vui. Dùng canonical static ở vai trò Neutral; không phát minh emotion asset khác. Đánh giá cũ giữ nguyên và hiện “Nhận định từ dữ liệu trước đó” nếu version item khác; re-evaluate tạo record mới theo server. Không tự render lời prose “mua” thành card hoặc tính verdict từ giá.

“Xem món đồ” mở Item Detail; mọi quyết định mua/chờ/bỏ qua hoặc ghi tiền phải ở flow nghiệp vụ riêng có xác nhận. Quick Check có thể yêu cầu nhận định sớm; control text “Xin nhận định lúc này” chỉ trong ITEM/online/capability, không bắt đủ số câu. Không thêm nút “Mua ngay”.

## NC — New Chat
Full-height sheet hoặc full-screen dialog như mockup, top radius28, drag handle chỉ trang trí, Close48. Không composer, không tạo server thread khi chỉ chọn option. Title24/32/700 “Đoạn chat mới”, subtitle16/24.

Hai context cards cạnh nhau khi đủ chiều ngang: content width=(W−40−12)/2; min-height160, padding16, radius24; icon48, title17/24, body14/20. Ở320dp hoặc font scale200% xếp dọc; không ép text/card cao cố định. Selection có stroke2 + radio/check + semantics. “Trò chuyện tự do” và “Về một món đồ”; tránh giải thích rằng AI đã đọc ngân sách thật.

Item Chat hiển thị item picker inline: title17/24, search min48, row min80, thumbnail48, tên tối đa2 dòng, metadata14/20, radio24 trong target48. Chọn theo itemId, chỉ một item. Không có item → “Chưa có món đồ phù hợp” và lối mở My Items theo app; không tạo item ngầm. Paging/loading/error độc lập picker, không chặn chọn Free khi picker lỗi. Nếu đang Free mà tap item row, chuyển rõ selection sang Item và preview item trước CTA; không giữ radio Free gây mơ hồ.

Footer fixed padding20, CTA56 “Bắt đầu trò chuyện” cho FREE hoặc “Trò chuyện về [tên món đồ]” wrap cho ITEM. Disabled khi ITEM chưa chọn, đang validate/pending, offline hoặc unavailable; reason nhìn thấy. Tap mở unsent CD đúng context, giữ selection trong phiên nếu Back. First send mới tạo thread theo create contract; không tạo empty FREE giả để bind. Backend có create behavior khác phải map theo contract hiện hành, không tự migration.

NC không phải conversion command cho một FREE thread đã có ID. Conversion khởi phát từ CD FREE mở cùng component picker dưới dạng sheet; CTA riêng “Liên kết món đồ”, sau đó confirmation hiển thị tên/ID và “Giữ toàn bộ tin nhắn”. Confirm ack giữ threadID và draft. Không push NC để clone thread.

## Shared components và trách nhiệm
| Component | Inputs | Events / guard |
|---|---|---|
| ChatThreadRow | threadId, title, context, preview, updatedAt, lifecycle, optional unread | OpenThread; OpenOptions; không mutate item |
| ContextModeSelector | FREE/ITEM, selectedItem | SelectMode/SelectItem; không gọi AI |
| ItemPicker | owner scoped items/query/cursor/loading/capabilities | selection chỉ local; ConfirmBind riêng; không unbind/retarget ITEM |
| MessageTimeline | stable IDs/sequence/cursor/delivery state | LoadOlder/JumpLatest/RetrySameIntent; dedup server IDs |
| DecisionCard | evaluationId/itemId/verdict/rationale/snapshot/version/time | OpenItem/RequestReevaluation; không purchase/decision write |
| Composer | draft/validation/online/lifecycle/op state | Edit/Send; không silently truncate |
| ThreadOptionsSheet | thread ID, version, lifecycle, capabilities | Rename/Archive/Restore/Delete; recheck trước submit |
| ConnectivityBanner | connectivity,cachedAt,knownUnavailable | “Đang xem dữ liệu đã lưu”; stale không no-cache |
| OperationFeedback | pending/unknown/terminal/error | CheckStatus/RetryAllowed; không thành công giả |

Thông số giới hạn tên/message, timeout và retry window lấy approved contract/adapter; không đặt số UI thành schema. Names trim/không toàn khoảng trắng; lỗi trả về được gắn field; không cắt nội dung âm thầm. Các component giữ semantic labels/error/disabled reason rõ.
