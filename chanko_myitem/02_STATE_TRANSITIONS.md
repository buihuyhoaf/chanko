# State và transition matrix • Recovery Baseline

## Các trục độc lập

ItemStatus chỉ WISHLIST/CONSIDERING/WAITING/PURCHASED/SKIPPED. UI Saved→WISHLIST, Cooling Off→WAITING+ACTIVE. CoolingOffStatus chỉ ACTIVE/COMPLETED/CANCELLED/RESOLVED; PURCHASED/SKIPPED là kết quả resolve, không status ghép. AI verdict BUY/WAIT/SKIP riêng; không tự mutate item/tiền. Ready derived UI; Postponed event/presentation, không status thứ6/7. Archived là visibility, không reset status và không xóa purchase/history.

## Approved transition

Mọi mutation chính thức cần online, owner/version hợp lệ, server commit các dữ liệu liên đới atomically; khi timeout giữ UnknownOutcome và reconcile, không tạo ý định mới.

| Trước | Trigger | Item sau | Cooling-off / history | Purchase/budget |
|---|---|---|---|---|
| Chưa item | Save Wishlist | WISHLIST | Không tự tạo phiên | Không chi |
| Item được phép | User xác nhận start | WAITING | ACTIVE mới; tối đa1 ACTIVE/item | Không chi |
| WAITING+ACTIVE | Adjust extend/shorten | WAITING | Cập nhật ACTIVE và adjustment | Không chi |
| WAITING+ACTIVE | Server xác nhận hết deadline | CONSIDERING | COMPLETED | Không chi; Ready dẫn xuất |
| WAITING+ACTIVE | Cancel | CONSIDERING | CANCELLED | Không chi |
| WAITING+ACTIVE | Skip Waiting | CONSIDERING | Kết thúc ACTIVE bằng cơ chế cancel theo phụ lục; mở lựa chọn quyết định | Không tự SKIPPED, không purchase |
| WAITING+ACTIVE | Chuyển Wishlist/Considering | Nhãn đã chọn | CANCELLED | Không chi |
| WAITING+ACTIVE | User rõ ràng chọn Skip item/Not Anymore | SKIPPED | RESOLVED, kết quả SKIPPED | Không chi |
| Item writable, kể cả WAITING | Xác nhận Record Purchase/Direct Mark as Purchased/Buy Again | PURCHASED | Resolve ACTIVE nếu có, kết quả PURCHASED | Tạo1 purchase; tính đúng cycle |
| CONSIDERING+COMPLETED | Need More Time, chọn thời gian mới | WAITING | ACTIVE mới; giữ COMPLETED cũ | Không chi |
| Ready/CONSIDERING | Need More Time, chọn hoãn không timer | CONSIDERING | Ghi Postponed event; next review khi có; không tạo ACTIVE giả | Không chi |
| Ready | Not Anymore | SKIPPED | Giữ lịch sử COMPLETED | Không chi |
| Ready | Still Want It | Không tự đổi nhãn thành Purchased | Mời re-evaluation1–2 câu, có thể bỏ qua và tự quyết | Không tự purchase |
| Ready | Xem detail/Back/bỏ qua review UI | Không đổi | Ready duy trì đến action xử lý rõ | Không chi |
| Có purchase | Edit thông tin nhập sai | Giữ status hiện tại | Revision server; không sửa evaluations/periods | Cùng purchaseId; recompute cycle cũ/mới |
| Có purchase, kể cả cuối | Delete nhập sai | Giữ status hiện tại bất kỳ | Revision; không hồi sinh ACTIVE | Loại khỏi spent/count, không refund |
| Bất kỳ | AI evaluate | Không đổi | Append evaluation, giữ history | Không chi |
| Offline, cache ACTIVE đến deadline | Local countdown hết | Cache status không đổi | Ready tạm thời có nhãn chưa đồng bộ | Không mutation/chi |

Skip Waiting ≠ Skip item: cancel được server ack rồi mở lựa chọn ở detail (component, không route mới). User đóng lựa chọn sau đó thì item vẫn Considering, không tự Skip. Nếu request cancel chưa xác nhận thì không báo phiên đã kết thúc. GAP-02 chốt UX; wiring endpoint/decision event thực theo contract, không tự tạo enum SKIP_WAITING trong backend.

## Ready persistence và resolution

GAP-01 Approved: Ready duy trì đến action xử lý rõ, không tự mất chỉ vì mở màn hoặc bỏ qua mindful review (DETAIL-08). Server completion giữ lịch sử COMPLETED; không sửa thành RESOLVED chỉ để tắt banner. Need More Time có2 nhánh theo DETAIL-12. UI hiển thị Ready không đòi thêm ItemStatus.

| Action | UX đã duyệt | Contract còn phải xác định |
|---|---|---|
| Xem/Back/dismiss/bỏ qua review | Giữ Ready | Cách đọc eligibility và bảo toàn qua restart/thiết bị khác |
| Not Anymore | Item Skipped, Ready được xử lý bằng quyết định rõ | Marker liên kết review/decision và atomicity |
| Need More Time→new wait | Item Waiting, ACTIVE mới, giữ COMPLETED | Marker xử lý Ready và reference phiên mới |
| Need More Time→Postponed | Considering + event; next review nếu có | Marker Ready clear, event fields, tái hiện Ready khi đến next review; không tự lên lịch server |
| Still Want It | Cho re-evaluation hoặc bỏ AI/tự quyết, không bắt mua | Chính xác clear ngay lúc chọn hay sau quyết định tiếp: chưa được baseline/Drive đặc tả |
| Purchase | Purchased và1 transaction | Consistent clear/eligibility sau ack |
| Archive/restore | Preserve history/budget | Ready bị ẩn/khôi phục như thế nào |

Predicate Reference: item visibility hợp lệ + phiên kết thúc chưa có action xử lý tương ứng. Không chỉ suy `ever COMPLETED` hoặc `latest COMPLETED`; không dùng marker chỉ trong UI memory. Không invent field reviewed_at, TTL, deadline hay endpoint. Các UX đã có nguồn giữ Approved; phần còn mở là persistence/contract, không phải thiếu quyết định29 mã.

EDGE-01: offline hiển thị Ready tạm thời dựa deadline đã cache (có thể stale), không hoàn tất period chính thức. Có mạng lại refetch item/period/decisions/capabilities; thay provisional UI bằng kết quả server. Không gửi draft hoặc replay form mua khi reconnect.

## Postponed

DETAIL-10/12 và ITEM-04 Approved: Postponed nằm trong Active subfilter; là decision/event hoặc label, status Considering. Có next review time thì hiển thị ngày/giờ/timezone, không có thì không bịa deadline. Không tạo CoolingOffPeriod chỉ để có countdown cho Postponed. Chính sách edit/remove/clear event, precedence Ready/Postponed, tái đến hạn và persistence còn cần backend contract. Không map mọi Considering thành Postponed, không suy từ Skipped.

## Archive/restore

EDGE-03/GAP-03 Approved: Archive/Hide item có purchase, Archived Items trong menu và Restore. Bảo toàn ID, status, purchases, evaluations, decision/cooling history và budget. Restore không mặc định Wishlist, không tạo purchase, không hồi sinh phiên đã kết thúc. Counts/visibility cập nhật sau ack; filters đang chọn vẫn được giữ.

**Chưa chốt:** archive khi WAITING/ACTIVE sẽ bị chặn, giữ timer chạy hay cancel; completion/reminder/Ready trong archived; restore khi deadline đã qua; quyền edit/AI/purchase/correction khi archived. Không áp guard chặn ACTIVE hoặc bắt restore trước mọi mutation như Approved. UI đọc capability contract; chưa có contract thì thể hiện integration gap, không fake success hoặc tự quyết backend policy. Không hard-delete/cascade thay archive.

## Manual labels, accounting và concurrency

SPEC-03 cho chuyển nhãn linh hoạt; phụ lục chốt rõ WAITING transitions ở trên. Các cặp khác theo contract, không dựng quyền từ màu/chip. Direct Mark as Purchased luôn đi qua purchase confirmation; không ép Quick Check. Đổi nhãn không tự tạo/xóa transaction; đổi estimated price không sửa purchase cũ.

VND nguyên exact, actual_unit_price≥0, quantity INTEGER≥1 default1; server total=unit×qty, Android preview. Một item nhiều purchases, count khác quantity. Buy Again chủ động mới dùng key mới; retry cùng intent giữ key, khác payload/key cũ conflict; stale version không overwrite.

PAY_NOW=CREDIT, Credit optional card, ngày purchased_at theo timezone nghiệp vụ gán [start,end), không created_at/ngày trả thẻ. Cycle start day1–31 cố định; clamp tháng ngắn giữ anchor; remaining được âm. Budget amount cập nhật ngay setting+current snapshot cùng transaction; closed snapshots giữ nguyên, không rollover. Correction chuyển ngày mua recompute cả cycle cũ/mới và recap khi xem; không refund âm.
