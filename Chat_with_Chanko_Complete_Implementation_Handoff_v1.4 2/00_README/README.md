# Chat with Chanko — Final Handoff v1.4

**Documentation & Assets: COMPLETE FOR HANDOFF. Technical Contracts: APPROVED FOR IMPLEMENTATION. Runtime Acceptance: NOT RUN — after implementation.** Không viết code hoặc tạo PR trong nhiệm vụ cập nhật tài liệu này; approval không phải deployed backend hoặc production test PASS.

A1/A3/A5 APPROVED; A2/A4 APPROVED WITH AMENDMENT theo [direct user message](../14_APPROVALS_AND_RECOVERY/USER_TECHNICAL_APPROVAL_V14.md). Không còn nhóm A1–A5 chờ approve. [Approved addendum13/08](../13_BACKEND_API_CONTRACTS/08_APPROVED_AMENDMENTS_V14.md) là current authority cho:

- Confirmed FREE→ITEM **cùng thread**, existing item only, giữ transcript/message IDs/order/draft, không clone/item create/backfill official history.
- Freshness24h chỉ mark stale/revalidate; retained cached data tiếp tục đọc offline, không TTL khóa đọc hoặc age purge. Cursor/retry expiry khác retention.
- Approved A1/A3: independent official Decision/Evaluation/Purchase records, no cascade; same-key replay, version/context/epoch guards, foreground sync/tombstones, no outbox/auto replay.

## Handoff entry points

1. [Final QA/status report](../FINAL_QA_REPORT.md), [approval register](../14_APPROVALS_AND_RECOVERY/approval_register.json), [conformance/34-ID evidence limit](../13_BACKEND_API_CONTRACTS/06_SOURCE_CONFORMANCE.md).
2. [Product UX](../01_PRODUCT_UX/UX_SPECIFICATION.md), [source-grounded REC synthesis](../01_PRODUCT_UX/RECOVERED_RULES_SYNTHESIS.md), [legacy decision register](../01_PRODUCT_UX/DECISION_BASELINE.md).
3. [11 Screen Specs](../02_SCREEN_SPECS/INDEX.md), [navigation](../03_NAVIGATION/NAVIGATION.md), [state matrix](../05_STATE_MATRIX/UI_STATE_MATRIX.md), [transitions](../05_STATE_MATRIX/TRANSITIONS.md).
4. [Components](../04_COMPONENTS/COMPONENT_SPECIFICATIONS.md), [logical seams](../04_COMPONENTS/LOGICAL_CONTRACTS.md), [UI defaults](../04_COMPONENTS/UI_DETAIL_DEFAULTS.md), [mascot](../04_COMPONENTS/MASCOT_INTERACTION_CONTRACT.md), [edges](../06_EDGE_CASES/EDGE_CASES.md).
5. [11 official visual baseline hashes](../07_VISUAL_REFERENCES/VISUAL_BASELINE_APPROVAL.json), [visual guide](../07_VISUAL_REFERENCES/README_CURRENT.md), [asset manifest](../08_ASSETS/asset_manifest.csv), [screen/slot map](../08_ASSETS/screen_asset_mapping.csv).
6. [Android guide](../09_ANDROID_IMPLEMENTATION/INTEGRATION_GUIDE.md), [phases](../09_ANDROID_IMPLEMENTATION/PHASES.md), [approved Backend/API contracts](../13_BACKEND_API_CONTRACTS/00_INDEX_AND_STATUS.md), [coding agent prompt](../11_CODING_AGENT_PROMPT/CODING_AGENT_PROMPT.md).
7. [AC01–29](../10_ACCEPTANCE_QA/AC01_29.md), [integration AC](../10_ACCEPTANCE_QA/INTEGRATION_ACCEPTANCE.md), [AM14 tests](../10_ACCEPTANCE_QA/AMENDMENT_ACCEPTANCE_V14.md), [checklist](../10_ACCEPTANCE_QA/FINAL_CHECKLIST.md).

11 core PNGv1.2 là visual baseline chính thức, byte-preserved; S05 confirmation behavior có approved textual amendment, không tự sửa ảnh.4 variant PNG hỗ trợ không được tự gắn separate approval. Canonical/8 pairs/Nunito/OFL/icons/source/reference QA giữ nguyên, không sourcev0.2 blocker.

34 original CHAT/GAP/FINAL bodies vẫn PARTIAL/0 VERIFIED. [Per-ID review](../14_APPROVALS_AND_RECOVERY/DECISION_CONFLICT_REVIEW_V14.md) không claim no-conflict34/34 khi thiếu body. Readable source rules và approved amendments đã đối chiếu; không sửa SPEC/TECH baseline snapshots, không mapping ID đoán, không cần approve lại UX.

15 folders00–14 là toàn bộ package, không diff-only. Code-like HTML/CSS/render tooling từv1.2 chỉ giữ như nguồn visual tham chiếu, không implementation mới. Optional SSE vẫn excluded/not approved, MVP REST unchanged. Real integration requires implementation/capabilities/revision+runtime evidence, không cần user chốt lại A1–A5.
