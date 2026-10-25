# Dependency disposition — approved contractv1.4

[Approved index](../13_BACKEND_API_CONTRACTS/00_INDEX_AND_STATUS.md), [source conformance](../13_BACKEND_API_CONTRACTS/06_SOURCE_CONFORMANCE.md), [direct approval](../14_APPROVALS_AND_RECOVERY/USER_TECHNICAL_APPROVAL_V14.md).

| Former gate | Approval/document disposition | Runtime/implementation follow-through |
|---|---|---|
| R07/Q03/Q04 | A1/A3 APPROVED: deletion/retention/shell/no cascade/independent official records | Implement and test preservation/purge/replay/race; not pending user approval |
| Q01 | A2 APPROVED WITH AMENDMENT: FREE→ITEM confirmed same thread, no item create, preserved transcript | Implement bind with key/version/context revision and compatibility; AM14 |
| Q05/Q11 | A3 APPROVED; A2 bind uses same concurrency/identity guards | Multi-device/current context validation, parent replay and lifecycle fences; no auto replay |
| Q06 | A4 APPROVED WITH AMENDMENT: freshness distinct from offline retention | Stale retained offline reads/cursor reset/account isolation/capacity-only eviction |
| Q02 | Baseline REST retained; SSE excluded | No SSE approval needed for current MVP |
| Q07 visuals | Official11 corev1.2 baseline approved, no v0.2 dependency | Native rendering acceptance later |
| R01/Q07 archival34 | Exact per-ID missing-body ledger,34 PARTIAL | Restore historical source if available; not new UX approval/document completeness gate |
| Q08 | Normal repo/AGENTS/build/runtime inventory in implementation task | No claim repo-specific tests already pass |
| Q09 | Files/map complete; A5 APPROVED | Native reaction/accessibility tests later |
| Q10 item markers/permissions | Approved item-domain rules/capabilities kept, no chat-driven marker/status writes | No clear Ready/auto item restore or unapproved domain mutation |

Current user amendment supersedesv1.3 new-thread/displayTTL behavior. Historical proposal text retained as archival TXT undercontract_history_v13, not active instructions. SPEC/TECH source bytes unchanged. No A1–A5 reapproval request remains; runtime is an independent next stage.
