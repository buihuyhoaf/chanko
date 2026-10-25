# Acceptance criteria và edge cases

Các ID AC/EC dưới đây mới được tạo cho bàn giao; được map với Recovery Baseline trong07; không tạo thêm quyết định cho EDGE chưa được chọn. Cần chạy kiểm chứng khi có implementation; file QA bundle chỉ kiểm tra tài liệu/assets, không chứng nhận app.

| ID | Given / When | Then |
|---|---|---|
| AC-01 | MyItems có item ở cả5 status | Chỉ5 ItemStatus; Ready/Postponed không xuất hiện trong enum/payload status |
| AC-02 | Mở list, tìm tên, apply status và sort, vào detail rồi Back | Giữ query/filter/sort/scroll; kết quả đúng AND; không lặp/bỏ item giữa pages |
| AC-03 | Query/filter không có kết quả | FilteredEmpty + clear; không tuyên bố account chưa có item |
| AC-04 | Không có cache và offline | State chưa có dữ liệu thiết bị; không giả list rỗng chính thức |
| AC-05 | Load ProductMedia mọi màn | Handoff/preview chỉ dummy neutral của manifest; không ảnh sản phẩm mẫu, mèo/chó/túi/tai nghe/giày, không text thiết yếu raster |
| AC-06 | Mascot asset canonical thiếu/lỗi | Ẩn hình mascot, giữ speech; không thay chó hoặc sinh mèo khác; product media không đổi |
| AC-07 | AI trả BUY/WAIT/SKIP | Không tự đổi status, cooling-off hoặc chi tiêu; evaluation history giữ riêng |
| AC-08 | User ghi purchase dù verdict SKIP | Được phép; giữ verdict cũ; không shaming |
| AC-09 | Item WAITING + ACTIVE, user mua | Sau ack: đúng1 purchase, item PURCHASED, period RESOLVED (kết quả PURCHASED) trong kết quả nhất quán |
| AC-10 | ACTIVE hết deadline online | Server COMPLETED, item CONSIDERING; không purchase/chi tiêu; Ready chỉ là view dẫn xuất theo contract |
| AC-11 | ACTIVE hết deadline offline | Hiển thị Ready tạm thời + chưa đồng bộ; cache ItemStatus/CoolingOffStatus không đổi; reconnect refetch trước mutation |
| AC-12 | COMPLETED, user Need More Time | Nhánh new wait tạo ACTIVE mới, giữ COMPLETED cũ, item WAITING; nhánh Postponed giữ/chuyển Considering + event, không ACTIVE giả |
| AC-13 | User cancel hoặc move WAITING→Wishlist | Period CANCELLED; status target đúng; không chi |
| AC-14 | User skip khi ACTIVE | RESOLVED (kết quả SKIPPED); item SKIPPED; không purchase |
| AC-15 | Nhập đơn giá200000, qty2 | Preview400000; server total400000 VND; không float/rounding |
| AC-16 | Giá0, qty1 hợp contract | Total0; không ép lớn hơn0; count vẫn là1 giao dịch hợp lệ |
| AC-17 | Qty0, âm,1.5, exponential, overflow | Chặn/hiện lỗi dễ hiểu; server vẫn validate; không truncate thành số khác |
| AC-18 | Nhập CREDIT, không card | Save hợp lệ; chi tiêu giống PAY_NOW cùng giá/ngày |
| AC-19 | Đổi CREDIT đã chọn card→PAY_NOW | Không gửi card CREDIT cũ; history snapshot giao dịch cũ giữ nguyên |
| AC-20 | Double tap/retry sau timeout cùng ý định | Một purchase; giữ key; chỉ success sau xác minh; Buy Again chủ động mới tạo key mới |
| AC-21 | Edit purchase đổi giá/qty | Cùng purchaseId, revision mới, total tính lại; không tạo giao dịch thứ hai |
| AC-22 | Edit purchased_at sang cycle khác | Spent cycle cũ giảm, mới tăng đúng1 lần; timezone tài khoản và [start,end) |
| AC-23 | Xóa purchase cuối của item PURCHASED | Count0, empty history; status vẫn PURCHASED; evaluation/cooling history giữ nguyên |
| AC-24 | Archive item hợp lệ | Ẩn MyItems mặc định, có ở ArchivedItems; status và purchases/budget không đổi |
| AC-25 | Restore archived item | Giữ itemId/status/history; không tạo hoặc hồi sinh cooling-off; không tạo chi |
| AC-26 | Archive ACTIVE | Pending Verification: expected chưa có nguồn; chặn chỉ là đề xuất preview, không dùng làm acceptance production. Không tự cancel/pause timer |
| AC-27 | Sửa transaction thuộc archived item | Pending Verification quyền thao tác; khi được phép correction phải cập nhật accounting, không tự restore. Không đánh pass khi chỉ dùng guard đề xuất |
| AC-28 | User mất mạng khi form đang nhập | Giữ input; không queue mutation hoặc gửi ngầm khi online trở lại |
| AC-29 | Draft manual có trên thiết bị, mạng trở lại | Không tự tạo item/purchase; phải review+submit rõ; draft không xuất hiện như item chính thức |
| AC-30 | Font200%, screen nhỏ, IME mở | Không cắt tiền/error/CTA; focus đúng, touch≥48dp, insets đầy đủ |
| AC-31 | Reduce Motion / sound off | Không bounce/trang trí động/âm thanh; cùng nội dung và kết quả |
| AC-32 | Purchase success | Copy trung tính; không confetti, không prompt mua thêm |
| AC-33 | Chỉnh estimated_unit_price item | Các purchase cũ không bị sửa giá/tổng |
| AC-34 | History chỉ load1 page trong tổng nhiều pages | Count/total dùng aggregate server; không tính từ page rồi gắn nhãn all-time |

## Tình huống cạnh tranh và dữ liệu xấu

| ID | Edge case | Xử lý mong đợi |
|---|---|---|
| EC-01 | Purchase commit nhưng response mất | UnknownOutcome; reconcile bằng cơ chế contract hoặc retry cùng key; không tạo key mới tự động |
| EC-02 | Cùng key khác payload | Conflict, giữ input, giải thích; không overwrite giao dịch cũ |
| EC-03 | Thiết bị khác sửa purchase trước Save | Stale version; lấy bản mới, cho user so sánh/review; không silently overwrite |
| EC-04 | Extend và mua race nhau | Server đảm bảo invariant; refresh item/period/history sau conflict; không ACTIVE cùng item PURCHASED do UI stale |
| EC-05 | Hai request start cooling | Tối đa1 ACTIVE; không tạo phiên trùng; UI dùng state server |
| EC-06 | Request pagination cũ về sau query mới | Bỏ kết quả cũ theo query generation; không chèn sai list |
| EC-07 | Item archive/remove ở thiết bị khác khi sheet mở | Revalidate trên save, conflict/not writable; giữ form, không lưu lên item khác |
| EC-08 | Signed image URL hết hạn hoặc ảnh404 | Refresh quyền ảnh qua app, fallback neutral; không crash, không log token/URL nhạy cảm |
| EC-09 | Card đã archive/đổi tên | Giao dịch cũ vẫn đọc snapshot/label lịch sử theo contract; không mất purchase; picker loại card không còn dùng được |
| EC-10 | purchased_at đúng đầu/cuối cycle | Đúng biên thuộc cycle mới; không double count; chọn31→tháng2 clamp→tháng3 lại31 |
| EC-11 | Đổi timezone thiết bị | Không tự gán lại chu kỳ lịch sử; account timezone là nguồn nghiệp vụ |
| EC-12 | Remaining âm, budget0/missing | Hiện âm đúng; không clamp0, NaN hoặc chia0; không chặn ghi mua theo ngân sách |
| EC-13 | Status Purchased nhưng không purchase; status Skipped nhưng có purchase cũ | Cả hai có thể hợp lệ; không “sửa” bằng cách tạo/xóa dữ liệu |
| EC-14 | Correction làm valid purchase count3→2 | Frequent indicator/count/last purchase/tổng được tính lại; không dùng quantity làm count |
| EC-15 | Item không còn quyền đọc/account đổi | Dọn scope cache/focus theo repo; không rò tên/ảnh/purchase từ account trước |
| EC-16 | App chết lúc đang submit | Không submit ngầm khi mở lại; đối chiếu request đã xác nhận theo key/policy để biết outcome |
| EC-17 | Note/tên rất dài, Unicode/dấu Việt | Wrap, scroll, validate giới hạn thật; không mất text hoặc lỗi layout |
| EC-18 | Server trả status/verdict không hỗ trợ | Fallback đọc trung tính + refresh/error phù hợp; không map bừa thành PURCHASED hoặc BUY |
| EC-19 | Restore thành công nhưng filter MyItems không match | Archived row biến mất; không ép clear filter; giải thích nếu cần |
| EC-20 | Local clock sai | Countdown chỉ tham khảo từ server deadline/time offset nếu có; mutation và completion do server xác nhận |

## Bộ dữ liệu kiểm thử Reference

Tạo fixtures/test account riêng:5 status; item0/1/3 purchases; qty2; giá0; Credit không card; archived Purchased; completed period + chưa review; ACTIVE gần hết; tên tiếng Việt dài; note dài; cache stale. Mỗi fixture phải đánh dấu dữ liệu test, không đưa vào dữ liệu người dùng thật. Không đưa ảnh thương hiệu/sản phẩm cũ làm fixture.

## Acceptance bổ sung theo Recovery Baseline

| ID | Given / When | Expected / nguồn |
|---|---|---|
| AC-35 | Mở MyItems mặc định | List card ngang, thumbnail trái; read-only, tap detail; không inline More/mutation — ITEM01/05 |
| AC-36 | Xem header/navigation | Compact Chanko Hero;3 tiles All Items/Cooling Off/Ready;4 tabs+global+; không FAB trùng — ITEM02/03/07 |
| AC-37 | Lọc Active | Saved/Considering/Cooling Off/Ready/Postponed đúng UI mapping; Search/Sort cùng state; không thêm ItemStatus — ITEM04 |
| AC-38 | Empty global/filtered empty | Chanko + hộp kẹo khi có đúng canonical asset; Save an Item hoặc Clear Filters; thiếu asset báo gap — ITEM06 |
| AC-39 | Ready rồi xem detail/Back/bỏ qua review/restart | Ready vẫn tồn tại đến action rõ; không tự clear do bỏ UI; backend persistence cần contract — DETAIL08/GAP01 |
| AC-40 | Skip Waiting khi ACTIVE | Online ack kết thúc phiên→Considering rồi mở lựa chọn; dismiss không tự SKIPPED, không purchase; không lẫn Skip item — GAP02/phụ lục |
| AC-41 | Need More Time chọn hoãn không timer | Considering + Postponed event; next review nếu có, không bịa ngày hoặc tạo ACTIVE — DETAIL10/12 |
| AC-42 | Direct Mark as Purchased / Buy Again | Trong More, mở cùng Record a Purchase sheet; không Quick Check bắt buộc; mỗi xác nhận intent mới→purchase mới — DETAIL11/13,EDGE04 |
| AC-43 | Detail Purchased/Skipped | Ưu tiên read-only, Edit trong More; Purchase History riêng, transaction tap mở detail correction — DETAIL02/09/14,GAP04 |
| AC-44 | Decision Journey và status content | Collapsible từ lịch sử thật; Chanko bubble dưới status; sticky primary+More; countdown nổi bật, Adjust/Skip Wait trong More — DETAIL03..07 |
| AC-45 | Archive/restore item có purchase | Menu Archived Items và Restore; giữ ID/status/history/transactions/budget; ACTIVE/lifecycle chưa có expected cuối — EDGE03/GAP03 |
| AC-46 | Still Want It rồi bỏ AI | Không purchase/BUY tự động; thời điểm Ready clear **Pending contract**, không tự chọn — GAP01/SPEC05 |
| AC-47 | Budget amount đổi giữa cycle | Setting+current snapshot cập nhật cùng transaction; closed snapshot/purchases không đổi |
| AC-48 | Budget80%,100%,>100%,0,missing | Warning/Warning/Over; không NaN/chia0, không khóa mua; thresholds UI khác backend policy |
| AC-49 |2 purchases, mỗi qty2 | Count2 giao dịch, tổng qty4, keys riêng; mỗi total tính1 lần; retry cùng key không tạo lần3 |
| AC-50 | Xóa purchase cuối của item đã thành SKIPPED | Giữ SKIPPED, count0; spent giảm đúng cycle, không hồi sinh timer |
| AC-51 | Correction cycle đã đóng | Recompute spent/remaining/recap khi xem; budget snapshot giữ nguyên |
| AC-52 | Compact/Medium/Expanded và font200% | Adaptive đúng Design System, không cắt tiền/CTA; không ép mọi thiết bị1 cột |
| AC-53 | Hero loaded/loading/error/empty | Dummy neutral cả4 trạng thái preview; không ảnh mẫu/mèo/chó/túi/tai nghe/giày |

## Test gaps và fixtures

AC26/27, phần persisted marker của AC39/41/46 và archive ACTIVE/restore của AC45 cần backend contract để có expected toàn vẹn; không coi UX Approved bị thiếu phê duyệt. Không ghi pass bằng fixture tự chọn.

Fixtures cần đủ:5 statuses, Ready unresolved sau restart, Ready provisional offline, cả2 nhánh Need More Time, Postponed có/không next review, Skip Waiting rồi dismiss/Not Anymore/Purchase, archived có history, multi-purchase qty2, xóa cuối status Purchased/Skipped, correction qua cycle đóng. Fixtures không phải dữ liệu thực và không dùng ảnh sản phẩm mẫu. Chạy runtime tests sau khi có app; QA bundle chỉ kiểm tra tài liệu/assets.
