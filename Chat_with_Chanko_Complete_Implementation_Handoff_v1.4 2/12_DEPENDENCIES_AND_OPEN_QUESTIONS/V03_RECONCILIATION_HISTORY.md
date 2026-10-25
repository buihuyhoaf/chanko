> HISTORICAL EVIDENCE. v1.2 asset/font status is in00_README and08_ASSETS. Original decision/API/source gaps are not closed by this update.

# Reconciliation Report v0.3

**NOT READY FOR IMPLEMENTATION.** V0.3 hoàn tất cấu trúc recovery/traceability và hợp nhất sửa tài liệu có căn cứ; chưa khôi phục body34 quyết định. Không xác nhận đủ34 content decisions, không yêu cầu duyệt lại.

## 7 P1 và2 P2
| Issue | Mức | Trạng thái | Bằng chứng xử lý trong v0.3 | Điều chưa được đóng |
|---|---|---|---|---|
| R01 | P1 | OPEN — EVIDENCE BLOCKED | 09 và decision_baseline.csv đủ34 PARTIAL; trace204 cells insufficient | Body/option từng ID, mapping34/34 |
| R02 | P1 | RESOLVED — DOCUMENT COVERAGE ONLY | 03 S03 giữ tên gốc, pre-verdict subcase; 05 EC29; AC21 | Body Approved state detail và runtime chưa verified |
| R03 | P1 | RESOLVED — DOCUMENT COVERAGE ONLY | 01/02/04 early intent; EC30, AC22; nguồn SPEC01/TECH03 §5.2 | Placement/copy P; actual adapter/runtime chưa kiểm |
| R04 | P1 | RESOLVED — DOCUMENT GATE LIST ONLY | AC20/Phase4/12/13 bao Q01–11 theo capability | Không Q nào được tự đóng vì có checklist |
| R05 | P1 | RESOLVED — KNOWN PACKAGING GAP ONLY | DS/MyItems recovery/dummy/asset license local; hashes QA | Canonical/v0.2/font chưa có; reference docs giữ path lịch sử |
| R06 | P1 | RESOLVED — WORDING ONLY | 01/04 và AC05 phân biệt evaluate persistence với cấm business mutation | Không chứng nhận backend persist/idempotency đúng |
| R07 | P1 | OPEN — CONTRACT BLOCKED | 01/12 production lifecycle guard và AC24 | Retention/FK/history reader, concurrent deletion semantics/test |
| R08 | P2 | RESOLVED — DOCUMENT COVERAGE ONLY | EC22 và AC26 có403/428 | Runtime/error adapter chưa kiểm |
| R09 | P2 | RESOLVED — WORDING ONLY | AC09 tối đa1 terminal thành công; failure có thể0 | Runtime send/retry chưa kiểm |

Không đánh dấu R01/R07 closed. Các resolved ở trên chỉ đóng lỗi tài liệu đã quan sát và sửa; không đóng production dependencies hoặc thiếu bằng chứng34 quyết định. Không phát hiện P0 có bằng chứng ở review tài liệu; chưa audit app/backend.

## Product UX và technical contracts
Free Chat có trong yêu cầu UX/state list, nhưng nullable item/new schema chưa được approve bởi UX. Retry streaming tránh duplicate không override REST no-stream MVP. Delete giữ history là invariant, không phải lựa chọn physical FK. Archive screen không tự xác định lifecycle enum/retention. Multi-device UX không tự quy định realtime transport hoặc last-write-wins. Q11 bổ sung diện kiểm tra, không scope commitment mới.

## Tìm recovery nguồn lần này
E01: đọc Work “Chuẩn bị bàn giao implementation”, user message đầy đủ có ranges và guards nhưng không body34. E03: chat “Thiết kế tính năng chat” trả5 turns mới nhất, hasMore=false/nextCursor=null, assistant chỉ references. E04: rg workspace,3 thư mục v0.1 và ZIP/evidence chỉ có register rỗng body. E05: Drive query Chat/Chanko; metadata Chat/Decision/Recovery; fullText CHAT-01/FINAL-06; tên Handoff/Baseline; thư mục design. Không tìm được source gắn34 body; query Chat có unrelated results nên không dùng làm evidence quyết định. Search empty không chứng minh file không tồn tại ngoài phạm vi truy cập.

## Điều kiện khép recovery
Cần source chứa nguyên văn34 body và option đã chọn, gắn ID/namespace; có thể là export lịch sử hoặc Recovery Baseline có provenance. Đây là yêu cầu khôi phục nội dung đã duyệt, không yêu cầu người dùng duyệt lại. Khi nhận nguồn: lưu nguyên văn/hash, cập nhật từng row VERIFIED nếu đủ, map6 tài liệu và AC, ghi divergence; không sửa source SPEC/TECH hoặc tự approve contract mới.

V0.1/v0.2 giữ nguyên bytes; bản đối chiếu v0.2 nằm references/v0.2. Source SPEC/TECH gốc không sửa. Các file01–08 v0.3 là derived handoff copy, không baseline override.
