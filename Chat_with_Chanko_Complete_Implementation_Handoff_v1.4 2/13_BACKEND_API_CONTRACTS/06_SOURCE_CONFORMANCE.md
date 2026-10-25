# Conformance và conflict review — approved contractv1.4

**Kết luận trong phạm vi bằng chứng:** approved amendments không mâu thuẫn các business invariants đọc được từ source SPEC/TECH-02/03 và direct user requirements. A2 là explicit extension của item-bound API/schema gốc, không giả có sẵn trong TECH. A4 bỏ read-cutoff của đề xuấtv1.3, giữ offline read cache theo MVP. Không thể chứng nhận “34/34 không mâu thuẫn” khi body34 IDs chưa có; từng row được ghi UNVERIFIABLE ở ledger, không tự VERIFIED.

SPEC/TECH source snapshots giữ nguyên byte. Bản fetch25/10/2026 được thực hiện trongv1.3 và đã so khớp normalize BOM/newlines/trim;v1.4 tái sử dụng, không claim fetch mới. [Source comparison](../12_DEPENDENCIES_AND_OPEN_QUESTIONS/evidence_v13/source_comparison.json), [metadata](../12_DEPENDENCIES_AND_OPEN_QUESTIONS/evidence_v13/source_metadata.json), [TECH-02](../12_DEPENDENCIES_AND_OPEN_QUESTIONS/sources/tech02.txt), [TECH-03](../12_DEPENDENCIES_AND_OPEN_QUESTIONS/sources/tech03.txt), [SPEC](../12_DEPENDENCIES_AND_OPEN_QUESTIONS/sources/functional.txt).

| Source / invariant | Đối chiếu v1.4 | Disposition |
|---|---|---|
| TECH-02 §4.14 conversation gắn item; schema catalog phần3–4 là proposed | FREE cần nullable item link +mode check; existing ITEM create vẫn compatible. Approved bind gán existing item cho cùng conversation, không tạo item | APPROVED EXTENSION via A2 amendment; TECH source không sửa, không claim tính năng có sẵn |
| TECH-03 §2.6 ITEM create/messages/evaluate/list | Giữ existing routes; bind/context_revision/global FREE capabilities là addendum. ITEM only evaluate sau bind, server validates | BACKWARD-COMPATIBLE SURFACE +DECLARED EXTENSION |
| TECH-02 §4.5–4.6/§7, TECH-03 §5.3; verdict khác user decision/item/purchase | Bind không backfill official history; evaluation chỉ từ validated post-bind intent; AI prose không writes | CONSISTENT WITH READABLE BASELINE |
| User invariant delete chat giữ official history; TECH item-domain history readers | Tombstone shell/no cascade; Decision/Evaluation/Purchase IDs/item links giữ, independent item query, purge transcript không official records | CONSISTENT; R07 CONTRACT APPROVED, runtime chưa đo |
| TECH-02 §4.16/§9, TECH-03 §4.1–4.2/§5.3 | Same-key replay before current-version comparison, stable payload/admission context, unique intent/records, bind/AI epoch+context fences | CONSISTENT; A3 approved, amendment clarifies bind |
| TECH-03 §4.2/§6.1 version/owner/409/428 | Explicit confirm không thay auth; same-thread bind CAS/thread+item validation, two devices one winner, no clock winner | CONSISTENT |
| TECH-02 §8, TECH-03 §4.3 /SPEC-09 offline read cache |24h freshness mark STALE only; retained cache đọc offline không age cutoff, no write/outbox/replay | CONSISTENT; supersedes v1.3 display TTL restriction |
| TECH-03 §2.1 stable pagination/owner/filter | Snapshot15m expiration không xóa cache; bind rechecks current mode/filter, context-bound cursor resets without transcript loss | CONSISTENT +APPROVED DETAIL |
| TECH-03 §5.1 REST no streaming | Giữ REST; optional SSE excluded và chưa approve | UNCHANGED BASELINE |
| Business appendix prices/status/budget/cooling-off | No item/Purchase/Decision/budget/cooling-off mutation from bind/delete/cache or advice; standardized money/quantity unchanged | UNCHANGED BASELINE |
| MyItems Ready/archive permissions | Chat bind chỉ existing item có quyền; không clear Ready/auto restore hoặc force item write | UNCHANGED READABLE RULES |
|11 visual baseline v1.2 | Same image/asset bytes. S05 confirmed conversion behavior ghi textual amendment; không silently alter reference pixels | PRESERVED +EXPLICIT BEHAVIOR AMENDMENT |
| CHAT-01–20,GAP-01–08,FINAL-01–06 | Approval ranges có, per-ID topic/body/option chưa đọc được |34 PARTIAL; ID-LEVEL COMPARISON NOT VERIFIABLE, không gán rule đoán |

## Conflict resolution đã thực hiện

1. “Context bất biến sau tạo / chọn item mở thread riêng” là đề xuấtv1.3 đã bị A2 amendment supersede. Active UX/API/navigation/state/QA/coding prompt nay cùng một rule: confirmed bind FREE→ITEM **cùng thread**, giữ transcript, no item create.
2. “Cache hết24h thì cần mạng để đọc” là đề xuấtv1.3 bị A4 amendment supersede. Active docs nay tách freshness24h, offline retention không age cutoff, cursor/retry windows độc lập. Server delete/purge của A1 vẫn giữ; offline device chưa biết remote deletion không được hứa cutoff24h.
3. Pending-approval markers A1–A5 được thay bằng actual user statuses. R07 approval gate đóng, runtime/release conformance riêng, không gọi đã tested hoặc production ready.

[Per-ID conflict/evidence review](../14_APPROVALS_AND_RECOVERY/DECISION_CONFLICT_REVIEW_V14.md), [approved source](../14_APPROVALS_AND_RECOVERY/USER_TECHNICAL_APPROVAL_V14.md). Không có yêu cầu user phê duyệt thêm cho A1–A5 trong phạm vi task này. Nếu nguyên bản34 xuất hiện, đối chiếu divergence theo ID và current explicit user amendments, không sửa SPEC/TECH ngầm.
