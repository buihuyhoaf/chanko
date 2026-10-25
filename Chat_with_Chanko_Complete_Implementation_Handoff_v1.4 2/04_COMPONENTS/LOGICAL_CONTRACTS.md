> v1.4: logical component contracts below remain UI-facing; technical A1/A3/A5 APPROVED and A2/A4 APPROVED WITH AMENDMENT. Deployed API/runtime evidence is separate. Missing original34 ID mapping is archival, not missing component content. Normative13/08 applies.

# Component Contracts
Tên component/read models là UI adapter seams; approved API reference/schema extension nằm13v1.4, không claim DTO đã deployed. Không viết source trong nhiệm vụ bàn giao.

## Components
| Component | Inputs tối thiểu | Events | Invariants / semantics |
|---|---|---|---|
| ChatScaffold | title, context, dataSource, connectivity, content state | Back, History, More | Demo badge rõ; 4-tab app shell giữ nguyên |
| ChankoCompanion | canonical asset ref, allowed emotion, reduceMotion | predefined interaction | Không LLM tap; fallback canonical neutral hoặc không ảnh |
| ContextHeader | itemId nullable, name, estimated unit price, status, stale marker | OpenItem, OpenPicker | Không nối item khác theo tên giống; product media không mascot |
| MessageTimeline | stable message IDs, sequence, request states, paging cursor | LoadOlder, JumpLatest, RetryOperation | Stable order; dedup IDs; không coi partial là completed |
| MessageBubble | role, text, timestamp, delivery status | Retry theo capability | Render text an toàn, không execute instructions/raw HTML |
| Composer | text, enabled reason, pending operation, limits từ contract | Edit, Send | Trim check; multiline; không silently truncate; giữ text khi fail |
| SuggestedReplies | options từ fixture/validated response | FillComposer (P) | Không tự submit purchase hay user decision |
| VerdictCard | evaluationId, itemId, verdict, rationale, evaluatedAt, contextVersion | OpenItem, RequestReevaluation | Immutable history; BUY trung lập; không mutation callback ghi tiền |
| HistoryRow/List | threadId, context, preview, updatedAt, lifecycle, cursor | Open, LoadMore | Total riêng; unknown title không bịa |
| ItemPicker | owner-scoped items, query, selection/paging state | Select, Cancel, Search | FREE→ITEM same-thread bind chỉ sau user confirmation; giữ transcript, no item create; không implicit retarget ITEM |
| OfflineBanner | cachedAt nullable, connectivity | RetryRead | Không có cache nói rõ; không hứa latest |
| ThreadOptionsSheet | threadId, current capabilities, online | Archive, Delete, OpenItem, Close | Capability recheck trước action; no Rename/Pin tự thêm |
| DeleteDialog | thread identity, pending/error | Confirm, Cancel | Không cascade decision/evaluation/purchase; ack mới success |
| ArchivedView | archived read model, capabilities | Open, Restore, Delete | Chat archive và item archive độc lập |
| ErrorPanel | error category, safe copy, retry policy | Retry/Reconcile | Không log/display token, prompt bí mật, provider raw error |

## Read models tối thiểu
Thread: opaque stable ID, contextRevision, initialContextKind, owner scope do repository/session kiểm soát, context kind FREE/ITEM, itemId nullable cho FREE theo approved extension1.4, ITEM bắt buộc itemId, lifecycle, updatedAt, version/capabilities nếu có.
Message: ID, thread ID, role USER/ASSISTANT theo whitelist, sequence/order, text, createdAt, client operation ID khi pending, delivery state.
Evaluation: immutable ID, shoppingItemId bắt buộc cho official record, verdict BUY/WAIT/SKIP, rationale, context/item version, timestamp, optional conversation reference.
Decision: chỉ đọc từ Shopping Item domain. Không tạo decision bằng chat role, “tôi sẽ mua” trong text hoặc click verdict.
Money: VND integer; estimated_unit_price khác actual_unit_price; quantity integer ≥1, backend tính total khi purchase flow xác nhận.

## Repository boundaries để coding agent implement sau
- ChatRepository: đọc/list thread và messages, gửi turn, đọc kết quả operation/reconcile; evaluate item; lifecycle/bind theo approved1.4; dùng fake khi backend chưa implementation, không pending approval.
- ShoppingItemReadRepository: item summary, version, quyền; không cho UI Chat ghi item/purchase.
- OfficialHistoryReadRepository: đọc Item Decision/Evaluation History, không xóa cùng chat.
- Fake repositories implement cùng logical interface; seed/reset deterministic, error/delay/offline scenarios có kiểm soát. Không import network/AI SDK, không secret/provider key, không report server success.
- Fake writes chỉ cập nhật fixture store. Room production vẫn read cache, không dùng local fake store làm authoritative business database.
- Real adapter chỉ wire sau phase gate; interface thay đổi nếu contract thực cần, không biến field P thành yêu cầu migration.

## Operation identity và chống duplicate
Một user Send tạo clientOperationId và idempotency key cố định cho intent; giữ payload hash, thread/context/version liên quan. Hiện user bubble một lần, map local ID sang server ID khi ack.
Retry cùng intent giữ key và payload; đổi text sau confirmed failure là intent mới có key mới; UnknownOutcome phải reconcile trước. Không dùng message text/timestamp làm dedup key.
TECH-03 REST là transport hiện hành. P demo có thể phát partial chunks để QA UI, label “Mô phỏng phản hồi”, không coi là streaming backend thật.
Nếu sau này streaming được duyệt: event cần operationId + messageId + event sequence/cursor; bỏ event đã nhận, upsert cùng assistant message, không append bubble mỗi reconnect; completion terminal một lần, reject chunks sau terminal, không tạo evaluation từ partial. Resume/replay/cancel semantics và retention window là Q. Reconnect không POST lại một intent bằng key mới.
Giữ ownership/thread version; concurrent turn xung đột đọc lại/reconcile. Không giữ database transaction trong lúc chờ LLM (TECH-02).

## Contracts logical bổ sung v0.3
| Component / seam | Inputs | Event / kết quả | Căn cứ và giới hạn |
|---|---|---|---|
| EarlyVerdictControl (P tên/placement) | item/conversation identity, online, capability, active operation | RequestEarlyVerdict | SPEC-01; TECH-03 §5.2 minh họa evaluate request_early_verdict=true, không DTO cuối |
| ItemChatContent | messages, evaluation state logical, context | Render pre-verdict hoặc completed card | Không tạo card từ partial hoặc prose, không cần loading khi idle |
| ChatRepository evaluate | owner-scoped item/conversation + operation identity | Persist immutable evaluation/messages hợp lệ qua server | Cho phép history persistence, không user decision/purchase/status/budget/cooling-off mutation |
| Thread refresh/reconcile | server identities/lifecycle/version/cursor nếu contract có | Recheck capability, unavailable/stale display | A3/A2 amendment đã chốt server ordering/version/context/lifecycle conflicts ở13/03+04+08; không tự thêm transport |

403 phải thông báo không có quyền; 428 phải xử lý thiếu precondition theo TECH-03, không bỏ If-Match hoặc tự retry mù. Retry key chỉ trong cửa sổ server bảo đảm; timeout ngoài cửa sổ không được giả đã thất bại để gửi intent mới. Không bịa lookup endpoint/TTL. Một operation active/thread là P cho demo, không policy concurrency server đã duyệt.

## BindExistingItem event/read-model seam
ItemPicker receives threadID/current kind/version/contextRevision, selected existing itemID/version và operation state. Row tap=Select only; Confirm emits BindExistingItem với stable key/clientOperationID; repository applies ack/reconcile to same thread entity. Preserve transcript/draft, reject stale/pending/deleted/archive, no ShoppingItemWrite use case. UI sees context provenance, không generate official history từ old FREE text.

Cache read accepts retained stale content independent of freshness24h; no-cache không được suy từ TTL. Current server capability/version/context validation vẫn cần trước writes, offline no bind/send.
