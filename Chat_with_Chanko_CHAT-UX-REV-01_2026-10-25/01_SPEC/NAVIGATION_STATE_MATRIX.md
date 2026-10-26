# Navigation và state matrix
Route keys CL/CD/NC là logical IDs Reference. State/modal không là màn chính mới.

| Nguồn / event | Đích / effect | Back / invariant |
|---|---|---|
| Home: entry Chat hiện có | CL | Back về đúng Home shell, 4 tabs giữ |
| Item Detail: cân nhắc cùng Chanko | CD ITEM, reuse/new theo v1.4 direct-entry policy | Back về Item Detail, không ép đi CL |
| CL thread tap | CD theo threadId | Back phục hồi query/filter/scroll CL |
| CL fixed “+ Đoạn chat mới” | NC | Close về CL, không tạo thread |
| NC FREE + Start | CD unsent FREE | First valid send tạo thread; Back không empty-thread spam |
| NC ITEM + selected item + Start | CD unsent ITEM | Validate owner/version; không mutate item |
| CD FREE: liên kết existing item | Picker sheet → confirm → pending → same CD thread | Cancel không effect; ack cùng ID, transcript/draft giữ |
| CD ITEM: tap context/card “Xem món đồ” | Item Detail hiện có | Không set purchase/status/budget; không retarget thread |
| CL More/filter “Đã lưu trữ” | CL archived state | Same CL route, title/filter/state rõ |
| Archived row tap | CD read-only | Restore explicit, không restore item |
| Thread options Rename | Rename dialog | Cancel giữ tên; ack mới áp dụng, API gap xem TECH |
| Archive/Restore | Confirm khi cần → pending → ack refresh CL/CD | State transition, không màn lifecycle riêng |
| Delete | Confirm dialog → pending/unknown → ack CL | Không Back vào deleted CD; official records độc lập |
| Deep link thiếu quyền/missing | CD unavailable state | Không leak title/preview, Back safe origin |

## State matrix
| State | CL | CD | NC / picker | Retry / writes |
|---|---|---|---|---|
| Initial loading | Skeleton6 rows, search/CTA có state rõ | Header/timeline skeleton, composer unavailable | Mode cards hiện; item region skeleton | Không giả empty hoặc auto AI |
| Refresh loading | Giữ rows/query, small progress | Giữ transcript/anchor | Giữ selection, kiểm lại capability | Không duplicate intents |
| Empty | “Chưa có cuộc trò chuyện”, CTA fixed | Empty transcript + composer nếu online active | Free chọn được; Item “Chưa có món đồ” | Không tạo official record |
| Query empty | “Không tìm thấy kết quả” + Xóa tìm kiếm | Không áp dụng | “Không tìm thấy món đồ” + clear | Không xóa selection ngầm nếu còn hợp lệ |
| Offline có cache | Rows retained + cachedAt/stale | Transcript read-only; composer disabled + reason | Chỉ đọc cached item rows, Start/bind disabled | Không send/evaluate/rename/lifecycle/outbox |
| Offline no-cache | “Chưa có dữ liệu đã lưu” | Same unavailable content state, không fake history | Giải thích cần mạng để bắt đầu | Online RetryRead explicit |
| Read error | Giữ cached rows hoặc ErrorPanel | Giữ cached messages, retry vùng lỗi | Picker retry riêng, Free vẫn rõ | Network error không đồng nghĩa404 |
| Send pending | Preview chỉ trạng thái đang gửi khi có op | Một bubble pending, Send/picker disabled | Không bind trong active AI operation | Identity journal; không gửi key mới |
| UnknownOutcome | Có badge/summary an toàn nếu context | “Chưa xác nhận được kết quả” + Kiểm tra trạng thái | Confirm bind khóa cho tới reconcile | Không coi timeout là failed |
| Confirmed retryable failure | Thông báo theo operation | Retry same intent, giữ draft/bubble | Giữ selection, Retry cùng intent | Key+payload giữ; edit/new intent theo contract |
| Archived | Filter CL archived | Read-only + Restore explicit; composer disabled | Không bind archived thread | Restore online/capability/version |
| Known deleted/unavailable | Remove/tombstone, không resurrect | Clear content/draft đúng owner, unavailable | Reject missing item/context | Late response bị loại bằng generation |
| Stale item/version | Status snapshot marker | Card “dữ liệu trước đó”; refresh/context conflict | Revalidate selection, giữ form có giải thích | Không tự resend/re-evaluate |

## Modal/sheet contracts và copy
Options sheet radius28, đủ target48, giới thiệu thread name để tránh nhầm. Chỉ một modal tại một thời điểm; chọn delete đóng options rồi mở dialog. Modal có thể scroll và footer không bị IME che.

Rename: title “Đổi tên cuộc trò chuyện”; field label “Tên cuộc trò chuyện”; buttons “Hủy”/“Lưu”. Trim, required, max length từ server; báo field error và giữ input. RENAME-API chưa xác minh → demo fixture có thể thử, production không báo đã lưu cloud và không đặt field/endpoint mới. Không rename item.

Archive confirm: “Lưu trữ cuộc trò chuyện này? Bạn vẫn có thể xem trong bộ lọc Đã lưu trữ.” Buttons “Giữ lại”/“Lưu trữ”. Archive chat không archive item. Restore: explicit “Khôi phục cuộc trò chuyện”, ack mới chuyển CL active state.

Delete confirm: “Xóa cuộc trò chuyện này? Lịch sử quyết định chính thức, đánh giá của món đồ và lịch sử mua vẫn được giữ.” Buttons “Giữ lại”/“Xóa chat”. Không hứa undo nếu contract không có. Pending khóa submit, error giữ dialog, unknown hiện check status; ack mới xóa local projection/draft. Không hiện success trước ack hoặc clone thread để né tombstone.

Bind confirm: “Liên kết cuộc trò chuyện này với [tên món đồ]? Toàn bộ tin nhắn được giữ trong cùng cuộc trò chuyện. Tin nhắn tự do trước đây không trở thành lịch sử quyết định chính thức của món đồ.” Buttons “Hủy”/“Liên kết món đồ”. Row tap chỉ select, explicit confirm mới command; pending/unknown khóa send và bind. Dismiss sau submit không đồng nghĩa rollback: giữ pending identity và reconcile.

Không có route Welcome, ChatHistory, ArchivedThreads, Loading, Error hoặc picker thứ tư trong thiết kế mới. Có thể reuse component cũ, nhưng UI/back stack thể hiện đúng CL/CD/NC và state/modal.
