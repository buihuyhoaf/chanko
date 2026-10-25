# TC-CONTEXT — APPROVED WITH AMENDMENT A2, revision1.4

[User approval](../14_APPROVALS_AND_RECOVERY/USER_TECHNICAL_APPROVAL_V14.md); [normative amendment](08_APPROVED_AMENDMENTS_V14.md). Đây là approved Chat implementation addendum; không sửa TECH-02/03 snapshots hoặc tuyên bố endpoint đã deploy. B=surface trong TECH, E=approved extension/reference mapping; không còn PENDING cho A1–A5.

## Context, identity và history

FREE có item_id null; ITEM có item_id bắt buộc trong owner scope. Server validate cặp giá trị. Không item thì không official evaluation/DecisionRecord giả; evaluate FREE trả409 ITEM_CONTEXT_REQUIRED. Free thread **được chuyển thành Item Chat cùng thread** sau user selection+confirmation cho existing item, qua BindExistingItem. Transcript/message IDs/text/sequence/timestamps và route/draft identity giữ nguyên. Không tự tạo item, không clone/merge hoặc chuyển sang thread khác, không backfill official item history từ free transcript.

Context revision0→1/version tăng một lần khi bind. Historical messages/operations giữ provenance FREE rev0. Chỉ post-bind validated evaluate mới có thể tạo official EvaluationRecord; không user decision/purchase/budget/cooling-off mutation từ bind hoặc prose. Không automatic unbind/retarget Item→Item. Chi tiết transaction/replay/races/cursor trong08.

Free mới tạo thread khi first send. Empty chưa có ID có thể chọn/xác nhận existing item làm unsent ITEM context trước tạo. Direct Item Detail entry ngoài conversion có thể reuse active item thread gần nhất hoặc create khi cần theo A2 gốc, không restore archived. Khi **conversion từ FREE hiện hữu**, luôn ưu tiên same thread dù target item đã có thread khác.

## Data dictionary, không DDL đã chạy

| Model | Trường/ràng buộc |
|---|---|
| Conversation | id, owner từ session, context_kind, item_id theo FREE/ITEM check, context_revision, bound_at/bound_after_sequence khi bind, lifecycle, version, lifecycle_epoch, created/updated UTC, capabilities/contract_revision |
| Message | id, conversation_id, server sequence unique, role USER/ASSISTANT, text/timestamps, operation_id, context_revision_at_creation; bind không rewrite history |
| Evaluation | Immutable id, shopping_item_id, optional conversation ref, verdict/rationale/snapshot/context version, context_revision_at_evaluation, engine metadata; không tạo ở FREE hoặc tự backfill sau bind |
| ConversationSummary | Same thread ID, current context/lifecycle/version; DELETED không title/preview; list dedup theo threadID chứ không thread+context |
| Operation | Stable owner/key/client ID/payload hash, operation_kind bao gồm BIND_ITEM, captured context_revision +lifecycle_epoch, version/attempt/state/resource refs;03+08 |

Nullable item FK +context CHECK, bind audit metadata và epoch/version là approved implementation design trong addendum. Không chỉnh catalog17 bảng hoặc source schema để gọi nó đã được TECH phê duyệt trước đây; implementation review/migration vẫn thực hiện sau khi được giao code.

## API catalog

Base `/api/v1`, authenticated owner server-side, TECH envelope/status codes. Reference endpoint names trong E được map thành deployed contract revision trước integration, không dùng screenshot suy API tồn tại.

| Method/path | B/E | Contract |
|---|---|---|
| POST /ai/conversations | B ITEM/E FREE | context_kind/item_id theo mode; key+client operation ID.201; legacy item_id-only maps ITEM |
| GET /ai/conversations/{id} | B/E metadata | Current context_revision/version/lifecycle/capabilities, ETag; unavailable/other-owner404 chung |
| GET /ai/conversations?item_id=... | B | Owner/item-scoped list |
| GET /ai/conversations?context_kind=...&lifecycle=...&q=...&cursor=... | E | Current context membership checks, snapshot order/count05+08 |
| GET /ai/conversations/{id}/messages | B/E cursor | Full transcript including pre-bind messages; context-bound cursor reset không xóa history |
| POST /ai/conversations/{id}/messages | B/E headers | text, If-Match/key/client ID, captured context revision;200 after commit |
| POST /ai/conversations/{id}/evaluate | B/E headers | ITEM only, validated context/version/capability; no official user decision/business writes |
| POST /ai/conversations/{id}/bind-item | E amendment | item_id,item_version,confirmed=true; If-Match/key/client ID;200 BindItemResult/same thread ID +context_revision increment; current availability/redaction08; no item create |
| GET /shopping-items/{item_id}/evaluations | B | Independent official evaluation history, survives chat deletion |
| GET /ai/evaluations/{id} | B | Owner/item history read, chat need not be live |
| GET /shopping-items/{item_id}/decision-records | E reference path | Independent official DecisionRecords, not transcript JOIN |
| POST /ai/conversations/{id}/archive | E | If-Match/key/client ID;200 ARCHIVED |
| POST /ai/conversations/{id}/restore | E | ARCHIVED→ACTIVE explicit; no DELETED/item restore or auto resend |
| DELETE /ai/conversations/{id} | E |204 after logical-delete commit; no JSON204; status lookup when ack lost |
| GET /ai/operations/by-client-id/{client_id} | E | Reconcile intent before original response;404 not proof request can never arrive |
| GET /ai/operations/{operation_id} | E | Canonical owner-scoped state/resource refs, redaction wins replay |
| POST /ai/operations/{id}/retry | E | Parent intent stable, retry-command dedup; old-context failure cannot resume after bind |
| POST /ai/conversations/cache-validation | E read-only | Max100 IDs, current context_revision/kind/item/lifecycle/version/capabilities or UNAVAILABLE; no business mutation |
| GET /ai/capabilities | E | Contract revision1.4, enabled features/limits, freshness_ttl_seconds separate from offline_retention_age_limit=null |

## Validation và errors

Message limit4000 Unicode codepoints; whitespace-only/too long422 giữ input, không truncate. Owner/header/precondition checks theo03. Missing If-Match428; stale version409; different payload same key409 IDEMPOTENCY_CONFLICT; pending bind409 OPERATION_IN_PROGRESS; new bind when already ITEM409 ALREADY_ITEM_BOUND; unavailable404; context change409 CONTEXT_CHANGED; cursor context mismatch400 CURSOR_CONTEXT_CHANGED/reset.429 Retry-After,503/504 safe errors không chứng minh chưa commit. Không receive owner/AI verdict như writable client fields.

Tiền/quantity chuẩn appendix cuối SPEC/TECH: estimated_unit_price,actual_unit_price,VND integer strings,quantity INTEGER≥1; backend tính total. Context bind không clear Ready/restore item hoặc thay domain statuses. Item Detail purchase/skip/cooling-off vẫn explicit user flows riêng.

## First-send/create recovery

Create key gắn entry intent giữ qua retry; zero-message shell chưa admit message không hiện như cuộc trò chuyện có nội dung. Mất ack lookup/replay cùng key, không create lại với key mới. TECH cho item nhiều conversations: hai distinct explicit entry intents có thể tạo hai thread hợp lệ, không merge theo item ID; same-thread conversion không phát sinh một thread thứ hai.
