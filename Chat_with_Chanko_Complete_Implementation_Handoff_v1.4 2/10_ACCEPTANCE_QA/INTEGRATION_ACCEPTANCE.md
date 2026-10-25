# Acceptance bổ sung — approved contract conformance và runtime sau implementation

Tài liệu/test plan đầy đủ, **tests dưới đây NOT RUN** trong handoff task. Mục nào reference asset/docs có thể check hiện tại; IT/UI-P cần app/backend sau implementation. Baseline invariant không phụ thuộc việc proposal được approve; A1–A5 đã approved với A2/A4 amendments; dùng effective contract revision1.4.

| ID | Given/When | Expected evidence | Gate |
|---|---|---|---|
| IT01 | Delete chat có official Decision/Evaluation/Purchase records, chat summaries | Official IDs/counts/values/item links/budget/status không đổi; independent history GET vẫn đọc được, transcriptGET404 | A1,AC11/24 |
| IT02 | Evaluate BUY/WAIT/SKIP, tap/render, user mua trái advice | Evaluation immutable; no user Decision/Purchase/budget/cooling writes từ chat; tiền/quantity names đúng appendix | Baseline,AC05–07/23 |
| IT03 | Lost DELETE ack rồi replay cùng key | Same deletion outcome204, lookup xác nhận, no extra effect; no JSON204; raw reply không resurrect qua replay | A1/A3 |
| IT04 | Provider complete race với delete/archive, thử cả2 commit orders | Commit-before lifecycle giữ evaluation hợp lệ; lifecycle-before result fence không assistant/evaluation mới | A1/A3 |
| IT05 | Purge job thất bại/chạy lại, search cache/op replay/backup restore | Deny reads ngay; purge idempotent trong approved24h, no raw summaries/replay/index; restore backup applies deletion ledger trước traffic | A1; backup ops evidence |
| IT06 | Official input_snapshot từng có raw transcript copy | Allowlist/backfill reviewed, giữ minimal official explanation; không xóa Evaluation/DecisionRecords để né retention | A1/Data |
| IT07 | Double tap/cùng key đồng thời, key khác payload, crash từng boundary | Một operation/user message/assistant success/evaluation; payload conflict409; rollback không result nửa chừng | A3,AC09/23 |
| IT08 | Timeout sau commit; lookup404 trước delayed admission; window expiry | Không key mới/false failure; lookup canonical result; expired safe no auto rerun | A3 |
| IT09 | Hai devices send/evaluate khác key cùng version, item đổi khi provider chạy | One active/thread; loser409; CONTEXT_CHANGED không publish như fresh; no client-clock winner | A3,AC27–28 |
| IT10 | RETRYABLE_FAILURE rồi explicit retry, command loss/replay | Parent key/op/user message giữ; one retry attempt/command, one successful finalization; lifecycle/precondition recheck | A3 |
| IT11 | Archive/restore/deleted restore/race | Read-only archived; restore explicit không clone/resend/item restore; DELETED không revive | A3,AC13 |
| IT12 | Foreground/reconnect qua remote delete/archive; offline retained cache beyond freshness24h | Validate IDs/current thread before enable actions; purge known deleted; stale banner, offline no send; freshness does not renew on local read; expiry never denies retained offline reads | A3/A4,AC27/29 |
| IT13 | Snapshot paging giữa remote writes/delete/filter/search/account switch | Stable snapshot membership/order, current authorization, no duplicate IDs; stale generation discarded; no count from page length | A4,AC15/29 |
| IT14 | Cursor forged/filter mismatch/expired/Redis eviction/unavailable |400 invalid/expired hoặc503 snapshot unavailable; reset page1 thích hợp, không offset fallback hoặc giả empty account | A4 |
| IT15 | Room capacity/freshness versus offline retention/restart/logout/account change | LRU bounds apply, no cross-owner leak; pending ID journal lookup no auto POST; manual item draft không bị xóa bởi chat policy | A4,AC16/29 |
| IT16 | Cross-owner endpoint/operation/cache validation; revoked session |404 chung/no title leak;401 flow, no raw provider errors/secrets, deleted content inaccessible even idempotent replay | Baseline+A1/A3 |
| IT17 | FREE create/send/attempt evaluate/select item/reuse/new/legacy ITEM request | No fake item/eval inFREE; confirmed same-thread FREE→ITEM bind retains transcript, no item create/backfill; legacy ITEM body compatible; stable keys/version/context | A2 |
| UI-P01 | Actual Android320dp/200%font/landscape/IME long message/title/error/money | No clipped essential text/CTA; wrapping/scroll;48dp actual targets, baseline hierarchy preserved | Runtime;AC17 |
| UI-P02 | TalkBack/modal focus/Back/new-message announcements | Sender/text/actions order, state read, decorative art skipped; focus trapped/returned; no token announcements | Runtime;AC17 |
| UI-P03 | Quick reply with empty/dirty draft, Back/context switch/rotation | Behavior A5, no overwrite/auto send; session draft scope correct; no false durable guarantee | A5 |
| UI-P04 | Interaction-enabled companion tap/rapid tap/reduce-motion/no-op decorative slot | Predefined/static reactions only, no LLM/business write; no BUY reward; role/target/timing perA5 | A5;SPEC07 |
| UI-P05 | Source assets decoded in native app at target size and theme applied | Correct canonical identity/pose, no alpha rectangle/halo at inspected sizes, memory bounded, real pair contrast≥AA | Runtime;asset acceptance |

## Evidence recording

Mỗi test lưu target repo/commit, app/backend contract revision, environment/device, setup steps, actual result PASS/FAIL/BLOCKED, resource IDs/counts trước/sau và source-safe screenshot/log. Không log transcript/secrets chỉ để chứng minh test. Snapshot source checks chỉ chứng minh files/colors/reference layout, không đứng thay IT/UI-P.

## Traceability

AC01–29 và EC01–34 hiện có vẫn áp dụng; IT01–17/UI-P01–05 bổ sung seam cụ thể. Không gán AC/IT vào CHAT/GAP/FINAL khi chưa original body; [REC traceability](../01_PRODUCT_UX/recovered_rule_traceability.csv) là mapping sử dụng ngay. Optional SSE tests ở13/07 chỉ cần nếu ADR override được duyệt; không chặn REST MVP.

## Effective amendment tests
[AM14-01–16](AMENDMENT_ACCEPTANCE_V14.md) bổ sung confirmed same-thread binding/idempotent races/context provenance, stale48h offline read, cursor/retry independence và remote known-deletion purge. Original A2 immutable/new-thread và A4 displayTTL conditions không còn expected outcomes. Tất cả runtime tests vẫn NOT RUN; contract approval đã ghi trong14, không pending.
