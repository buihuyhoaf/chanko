# Bản tổng hợp quy tắc có nguồn — không phải export34 quyết định

**REC là namespace mới cho traceability của bản tổng hợp**, không thay CHAT/GAP/FINAL, không giả nguyên văn hay nội dung đã khôi phục từng ID. Các rule bên dưới có nguồn đọc được, dùng làm nền tài liệu triển khai. Với phần không được nguồn chốt, tài liệu ghi P/PROPOSED và liên kết approved addendum A1–A5 v1.4.

Thứ tự thẩm quyền: chỉ thị trực tiếp mới nhất (visual1.2) → approved business appendix mới nhất → SPEC/TECH principles → derived screen/component contracts → proposals được gắn nhãn. Physical schema/endpoint ví dụ trong TECH không tự là accepted implementation.

| Ref | Quy tắc tổng hợp từ nguồn | Căn cứ | Tài liệu / acceptance |
|---|---|---|---|
| REC-01 | App shell bốn tab, Chat là route/entry, không tab5 | E01; MyItems ITEM-07 | [03_NAVIGATION/NAVIGATION.md](../03_NAVIGATION/NAVIGATION.md); AC01 |
| REC-02 | Có Free Chat và Item Chat;11 trạng thái UX yêu cầu, không database enum11 trạng thái | E01 | [05_STATE_MATRIX/UI_STATE_MATRIX.md](../05_STATE_MATRIX/UI_STATE_MATRIX.md); AC02–03 |
| REC-03 | Quick Check thường1–3 câu; cho verdict sớm và không ép chat trước mua | SPEC-01; TECH-03 §5.1–5.2 | [04_COMPONENTS/DECISION_AND_QUICK_REPLY_CONTRACTS.md](../04_COMPONENTS/DECISION_AND_QUICK_REPLY_CONTRACTS.md); AC21–22 |
| REC-04 | AI verdict BUY/WAIT/SKIP khác user decision/item status/purchase; không tự ghi tiền/status | SPEC-01; TECH-02 §4.5–4.6/§7; TECH-03 §5.3; E01 | [01_PRODUCT_UX/UX_SPECIFICATION.md](../01_PRODUCT_UX/UX_SPECIFICATION.md); AC04–07/23 |
| REC-05 | Evaluation history immutable, re-evaluate tạo record mới; persistence server không phải business mutation từ card | TECH-02 §4.6/§7; TECH-03 §5.3 | [04_COMPONENTS/LOGICAL_CONTRACTS.md](../04_COMPONENTS/LOGICAL_CONTRACTS.md); AC05/07/23 |
| REC-06 | Delete chat giữ official item decision/evaluation/purchase history; physical retention là proposal | E01; TECH-02 §4.5–4.6; TECH-03 §2.6 | [13_BACKEND_API_CONTRACTS/02_R07_DELETION_RETENTION.md](../13_BACKEND_API_CONTRACTS/02_R07_DELETION_RETENTION.md); AC11/24; IT01–06 |
| REC-07 | Offline Chat cache-only, không AI/mutation/outbox hoặc auto replay reconnect | E01; SPEC-09/ADR-09; TECH-02 §8; TECH-03 §4.3 | [06_EDGE_CASES/EDGE_CASES.md](../06_EDGE_CASES/EDGE_CASES.md); AC08/29 |
| REC-08 | MVP REST completed-response, không streaming thật; UI thinking hợp lệ, partial chỉ fixture | TECH-03 §5.1; TECH-01 | [13_BACKEND_API_CONTRACTS/07_OPTIONAL_STREAMING.md](../13_BACKEND_API_CONTRACTS/07_OPTIONAL_STREAMING.md); AC09–10 |
| REC-09 | Cùng intent giữ idempotency, stable IDs; owner/version và409/428; timeout không chứng minh chưa commit | TECH-02 §4.16/§9; TECH-03 §4.1–4.2/§5.3/§6.3; E01 | [13_BACKEND_API_CONTRACTS/03_OPERATIONS_RETRY_CONCURRENCY.md](../13_BACKEND_API_CONTRACTS/03_OPERATIONS_RETRY_CONCURRENCY.md); AC09/26–28 |
| REC-10 | Room chỉ read cache; cache owner-scoped, refresh open/foreground/user/mutation; item manual draft không auto submit | TECH-02 §8; TECH-03 §4.3 | [13_BACKEND_API_CONTRACTS/05_PAGINATION_AND_ROOM.md](../13_BACKEND_API_CONTRACTS/05_PAGINATION_AND_ROOM.md); AC16/29 |
| REC-11 | SASSY mặc định; Friendly/Sassy/Savage không đổi verdict từ cùng dữ liệu, không shaming | SPEC-07; TECH-03 §5.2 | [08_ASSETS/EMOTION_PRESENTATION.md](../08_ASSETS/EMOTION_PRESENTATION.md); AC07/17/18 |
| REC-12 | 9 emotion whitelist; predefined OptionB reaction không LLM mỗi tap; session/item memory tối thiểu không profile tâm lý | SPEC-07; TECH-01 | [04_COMPONENTS/MASCOT_INTERACTION_CONTRACT.md](../04_COMPONENTS/MASCOT_INTERACTION_CONTRACT.md); AC18; UI-P04 |
| REC-13 | Chanko canonical; BUY Neutral, không celebration;11 PNG v1.2 hiện là baseline visual chính thức | E01; current direct human visual approval E09 | [07_VISUAL_REFERENCES/VISUAL_BASELINE_APPROVAL.json](../07_VISUAL_REFERENCES/VISUAL_BASELINE_APPROVAL.json); AC05/18; visual inventory |
| REC-14 | Ready không clear vì mở/Back chat; purchase/cooling-off qua flow user confirm riêng, archive chat không restore item | MyItems recovery DETAIL-05/08/11/14; SPEC-05; E01 | [03_NAVIGATION/NAVIGATION.md](../03_NAVIGATION/NAVIGATION.md); AC06/25 |
| REC-15 | Giá chuẩn estimated_unit_price/actual_unit_price, VND nguyên và quantity INTEGER≥1; backend tính total | Phụ lục cuối SPEC/TECH-02/03 ngày17/10/2026 | [13_BACKEND_API_CONTRACTS/06_SOURCE_CONFORMANCE.md](../13_BACKEND_API_CONTRACTS/06_SOURCE_CONFORMANCE.md); AC05–06; IT02 |
| REC-16 | Structured output validate; thiếu/invalid không default BUY; authority từ server không prose/provider | SPEC-01; TECH-03 §5.2–5.3/§6.1 | [04_COMPONENTS/DECISION_AND_QUICK_REPLY_CONTRACTS.md](../04_COMPONENTS/DECISION_AND_QUICK_REPLY_CONTRACTS.md); AC19/23/26 |

E01 được giữ trong12/original_handoff_request.txt; E09 trong14/USER_VISUAL_APPROVAL.md; SPEC/TECH nguyên bản nằm12/sources và bản fetch hiện tại nằm12/evidence_v13. MyItems recovery là bản tổng hợp có provenance, không original34 Chat. Không có rule nào được tự gán sang ID CHAT/GAP/FINAL chỉ vì chủ đề có vẻ giống.

## Technical addendum đã được user phê duyệt

A1: xóa/retention/FK strategy; A2 amendment: FREE→ITEM cùng thread sau xác nhận, giữ transcript/no item create; A3: operation/lifecycle/race/window; A4 amendment: snapshot/cache freshness tách offline retention; retained cache offline không age cutoff; A5: dirty quick reply/draft navigation/mapped predefined reactions. REST non-stream đã là baseline, không đưa ra yêu cầu duyệt lại. R07 bảo toàn history là invariant đã biết; phương án vật lý được user approve trong Chat addendum1.4, không phải TECH source đã bị sửa.

Current authority: [direct technical approval](../14_APPROVALS_AND_RECOVERY/USER_TECHNICAL_APPROVAL_V14.md). A1/A3/A5 APPROVED, A2/A4 APPROVED WITH AMENDMENT; các trạng thái này không nâng VERIFIED cho34 IDs. [Conformance review](../13_BACKEND_API_CONTRACTS/06_SOURCE_CONFORMANCE.md) ghi rõ original-ID comparison chưa xác minh được.
