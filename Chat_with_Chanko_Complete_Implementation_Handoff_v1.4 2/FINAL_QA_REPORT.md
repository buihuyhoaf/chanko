# FINAL_QA_REPORT — approved final handoff v1.4

25/10/2026 · Asia/Ho_Chi_Minh. Cập nhật theo direct user approval A1–A5 với amendments A2/A4. Không viết application code, tạo PR hoặc sửa SPEC/TECH baseline sources.

## Trạng thái cuối cùng

| Lớp | Kết luận |
|---|---|
| Documentation & Asset Completeness | **COMPLETE FOR HANDOFF** — toàn bộ UX/screens/visuals/assets/components/navigation/states/Android guides/API contracts/AC/QA/prompt |
| Technical Contract Approval | **APPROVED FOR IMPLEMENTATION**: A1/A3/A5 APPROVED; A2/A4 APPROVED WITH AMENDMENT |
| R07 | **CONTRACT APPROVAL CLOSED**; official records độc lập chat deletion. Runtime/release conformance vẫn kiểm sau implementation |
| Runtime Acceptance | **NOT RUN — AFTER IMPLEMENTATION**, không dùng làm lý do tài liệu chưa hoàn chỉnh |
| Legacy34 evidence | **34 PARTIAL,0 VERIFIED**; không tự chứng nhận nguyên văn hoặc no-conflict34/34 |
| Optional SSE | **EXCLUDED / NOT APPROVED**; REST MVP theo TECH-03 giữ nguyên |

[Nguyên văn user approval](14_APPROVALS_AND_RECOVERY/USER_TECHNICAL_APPROVAL_V14.md), [approval register](14_APPROVALS_AND_RECOVERY/approval_register.json), [approved contract index](13_BACKEND_API_CONTRACTS/00_INDEX_AND_STATUS.md). Không còn A1–A5 cần user phê duyệt lại. Approval là hiệu lực đặc tả, không chứng minh backend deployed hoặc runtime PASS.

## A2 amendment — same-thread confirmed binding

[Normative approved addendum](13_BACKEND_API_CONTRACTS/08_APPROVED_AMENDMENTS_V14.md) và [context/API](13_BACKEND_API_CONTRACTS/01_CONTEXT_AND_API.md) nay yêu cầu:

- Chọn existing item trong owner scope rồi **xác nhận rõ**. Row selection/cancel không mutate. Không tạo ShoppingItem hoặc chuyển sang thread item khác.
- Bind FREE→ITEM trên **cùng threadID**, giữ mọi message ID/role/text/sequence/timestamp và current session draft. Không clone/merge/reuse another thread, không backfill official Decision/Evaluation/Purchase history từ free prose.
- Reference action BindExistingItem có stable key/client operation ID, thread If-Match và item version; transaction tăng context_revision/version đúng một lần và lưu operation result atomically. No LLM call hoặc business item write từ bind.
- Pending/UnknownOutcome reconcile trước bind. Two-device conflicting binds chỉ một version winner; old FREE send/retry/late output bị context/epoch fence, không re-interpret sang ITEM context.
- Replay dùng recorded admission context/hash trước current-version rejection; lost ack không cấp key mới. Old success replay là historical outcome, redaction/current lifecycle vẫn thắng; deleted thread không bị resurrect.
- List/filter/cursor/cache generation cập nhật current context nhưng entity/route/draft identity vẫn owner+thread. Message cursor cũ reset không làm mất transcript; same thread chỉ một history entity.

S02/S03/S05, navigation, state transitions, components, guide/prompt và QA đã đồng bộ. S05 confirmation được mô tả bằng approved behavioral amendment trong picker surface hiện có, không hai modal đồng thời hoặc thêm UX state thứ12.11 PNG baseline giữ nguyên byte; caption trong ảnh cũ không override yêu cầu xác nhận mới đã được user approve.

## A4 amendment — freshness khác offline retention

[Pagination/Room contract](13_BACKEND_API_CONTRACTS/05_PAGINATION_AND_ROOM.md) phân biệt:

| Policy/clock | Hiệu lực |
|---|---|
| Cache freshness24h | Mark STALE, giữ cached_at và revalidate khi online; **không xóa/ẩn/chặn đọc offline** |
| Offline retention | Data còn cache đúng owner và chưa known unavailable tiếp tục đọc offline sau24h/48h/lâu hơn; không age cutoff |
| Snapshot cursor15 phút | Hết hạn chỉ reset online page fetch; không xóa messages đã tải |
| Retry/result24h | Giới hạn operation replay safety, không tuổi thọ transcript |
| Delete purge24h/backup30 ngày | A1 phía server/storage, không hứa máy hoàn toàn offline biết deletion tức thì |

Capacity-only LRU limits A4 giữ, protect open transcript; không eviction chỉ vì cũ. Explicit clear/logout/known deletion/owner invalidation hoặc actual capacity/OS removal là retention boundaries thật. Network failure không thành deleted/no-cache; retain stale readable content. Offline vẫn no writes/AI/bind/outbox/auto replay. Token expiry không được biến thành freshness-driven purge; local owner/security policy và online server authorization được phân biệt.

Khi reconnect/foreground/open, validate current thread/context/lifecycle/pending IDs trước enable actions. Known delete purge chat transcript/summary/draft và durable owner tombstone chống resurrect; archive read-only, remote bind update cùng thread projection. Cached offline device chưa biết deletion có thể tiếp tục đọc, nhưng server permission/purge và official-record retention của A1 không đổi.

## R07, idempotency và multi-device invariants

[Approved R07](13_BACKEND_API_CONTRACTS/02_R07_DELETION_RETENTION.md) giữ official DecisionRecords/EvaluationRecords/Purchases gắn item và đọc bằng independent item readers. Không cascade hoặc đặt TTL official history từ chat deletion; không đổi spent/remaining/item status/cooling-off. Bind chỉ thay conversation context, không tạo official record từ lịch sử FREE. Post-bind evaluation chỉ qua validated explicit evaluation intent, giữ immutable history.

[Approved operations](13_BACKEND_API_CONTRACTS/03_OPERATIONS_RETRY_CONCURRENCY.md) và [sync](13_BACKEND_API_CONTRACTS/04_LIFECYCLE_AND_MULTI_DEVICE.md) nêu same-key replay, unique intent/result identities, one active AI operation/thread, context_revision+lifecycle_epoch+version/fencing checks và consistent short DB lock order. Không DB lock trong provider call, no client-clock last-write-wins, no automatic resends/restores. Replay deleted content bị redaction; current availability/lifecycle thắng stale client projections.

Các invariant được đặc tả và có acceptance cases; chưa gọi là đã chứng minh bằng chạy hệ thống khi không có implementation trong task này.

## Đối chiếu nguồn và 34 quyết định

[Source conformance](13_BACKEND_API_CONTRACTS/06_SOURCE_CONFORMANCE.md) đối chiếu với SPEC-01/07/09, TECH-02 §§4.5–4.6/4.14/4.16/7–9 và TECH-03 §§2.1/2.6/4/5/6 cùng các business appendices có hiệu lực:

- A2 là **declared approved extension** của item-bound surface/schema gốc, vẫn compatible existing ITEM requests; không giả FREE/bind endpoint có sẵn trong TECH.
- A4 phù hợp retained offline read-cache MVP; bỏ display-cutoff của đề xuấtv1.3, không sửa TECH source để hợp thức hóa.
- Official history, owner/version/idempotency, REST/no streaming, money/quantity và no business writes từ AI/bind/delete giữ invariants đọc được. Không phát hiện xung đột với các rules có nguồn trong phạm vi review.
- 34 IDs có approval-range evidence nhưng chưa original body/option riêng. [Per-ID review](14_APPROVALS_AND_RECOVERY/DECISION_CONFLICT_REVIEW_V14.md) và [CSV](14_APPROVALS_AND_RECOVERY/decision_conflict_review_v14.csv) ghi từng dòng NOT VERIFIABLE AT ID LEVEL; không claim no-conflict34/34 hoặc nâng VERIFIED. Current approval không tự trở thành nguyên văn lịch sử.

SPEC/TECH/DS/MyItems snapshots giữ nguyên byte; bản fetch25/10/2026 từv1.3 được tái sử dụng, không claim fetch mới. Current user approval/addendum được lưu riêng, chỉ supersede các derived defaults đối lập của v1.3. Original sources không bị chỉnh sửa.

## File/asset QA và acceptance boundary

11 official core visual PNGs,4 supporting variants, canonical gốc/alias,8 PNG/WebP pairs, fonts/OFL/icons/decorations và visual-source inputs giữ nguyên từ release đã kiểm. Canonical SHA-256 `c4b0de06a1175464607e80372f9615060b89892a16b58e8e4f57087c74e947d7`; alpha thật, lossless WebP decoded pixel equality kiểm lại. No new image generation hoặc code assets.

[Verification summary](00_README/verification.json), [asset checks](10_ACCEPTANCE_QA/evidence/asset_checks_v14.json), [preservation](10_ACCEPTANCE_QA/evidence/baseline_preservation.json), [consistency review](10_ACCEPTANCE_QA/evidence/consistency_review_v14.json), [local links](10_ACCEPTANCE_QA/evidence/link_checks.json), [checksums](00_README/checksums.json) ghi kết quả kiểm thực tế. Package đầy đủ không delta-only, final ZIP CRC/full-entry SHA/unsafe-path/duplicate checks và input archive preservation được ghi trong sidecars cạnh ZIP.

Reference60-view/font/contrast/layout QA được kế thừa vì render inputs không đổi; không biến nó thành native runtime PASS. [AM14-01–16](10_ACCEPTANCE_QA/AMENDMENT_ACCEPTANCE_V14.md), [AC01–29](10_ACCEPTANCE_QA/AC01_29.md) và [integration tests](10_ACCEPTANCE_QA/INTEGRATION_ACCEPTANCE.md) là kế hoạch sau implementation: confirmed bind/cancel/no-item-create, same-key/races/context provenance, stale48h offline read, known-delete sync, independent official history và native accessibility.

## Bàn giao

Dùng [Coding Agent Prompt v1.4](11_CODING_AGENT_PROMPT/CODING_AGENT_PROMPT.md) trong implementation task được user giao. Không còn technical choice A1–A5 chờ approve; cần hiện thực hóa backend/app/capabilities đúng contract rồi chạy runtime acceptance, không tự gọi production ready từ approval tài liệu. Không viết code, tạo PR hoặc thay SPEC/TECH baseline trong đợt hoàn thiện handoff này.
