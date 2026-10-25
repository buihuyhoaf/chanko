# Phases cho coding agent — future task, không code trong handoff task

| Phase | Output cần có | Exit gate |
|---|---|---|
|0 Repo inventory | AGENTS/theme/module/routes/DI/cache/serializer/build/test conventions; mapping existing domain services | Không sửa baseline để hợp thức fixture; không bịa project-specific conventions |
|1 UI components/resources | Native surfaces/text/assets theo11 official baseline, navigation shell giữ4tabs; source-grounded rules | Visible fake badge, no provider/business writes; approved1.4 defaults and amendment cases applied |
|2 State/flow fixtures |11 states và supporting variants, empty/no-cache/errors/unknown/dirty/stale/account/rotation fixtures; stable IDs | AC01–29 fixture results, no screenshot-as-UI, no fake online AI |
|3 Native acceptance | Device/screenshots/TalkBack/48dp/font/IME/reduced-motion; tests có ý nghĩa | Runtime results thật PASS/FAIL, không reference-check claimed app test |
|4 Accepted adapter contracts | A1–A5 approval already recorded; backend schema/API/capabilities/revision1.4 implemented and conformance reviewed | Không blanket enable lifecycle vì send chạy; no source TECH mutation |
|5 Integration | Real read/send/evaluate/lifecycle theo capability, race/idempotency/history/cache tests | IT acceptance theo contract, no secret/transcript leaks; production readiness riêng |

Không còn A1–A5 pending. UI fake bắt đầu khi được giao implementation; production adapter chờ backend availability và runtime evidence, không xin approval lại.34 legacy-ID recovery không chặn source-grounded UI, vẫn lưu evidence ledger. Khi nguồn gốc bổ sung thì đối chiếu divergence; không tự map ID hoặc mở lại UX. REST đã baseline không chờ user phê duyệt streaming. Chỉ user yêu cầu SSE mới tạo ADR riêng từ13/07.

Task hiện tại không viết implementation code, không PR. Những phases này là nội dung bàn giao, không instructions để chạy code ngay.

Effectivev1.4: confirmed same-thread FREE→ITEM, no item create; freshness24h never offline read expiry. Phase2/3 include AM14 cases và update same entity projection across devices, no context clone/TTL purge.
