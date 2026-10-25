# Backend/API Integration Contracts — APPROVED revision1.4

| Group | User status | Scope |
|---|---|---|
| A1-A | APPROVED | R07 deletion, retention, independent official history |
| A2-A | APPROVED WITH AMENDMENT | FREE→ITEM confirmed same-thread bind; retain transcript; no item create |
| A3-A | APPROVED | Operation/retry/idempotency/concurrency/lifecycle/multi-device |
| A4-A | APPROVED WITH AMENDMENT | Cache freshness khác offline retention; retained cache đọc offline không age cutoff |
| A5-A | APPROVED | Quick reply/session draft/predefined reactions |

[Direct user approval](../14_APPROVALS_AND_RECOVERY/USER_TECHNICAL_APPROVAL_V14.md), [machine register](../14_APPROVALS_AND_RECOVERY/approval_register.json). Không còn approval A1–A5 pending. Approval không phải bằng chứng endpoint deployed, DDL đã chạy hoặc runtime pass.

## Thứ tự thẩm quyền

Source SPEC/TECH/business appendices được bảo toàn. Human-approved Chat addendumv1.4 cụ thể hóa extensions; [08 amendments](08_APPROVED_AMENDMENTS_V14.md) thắng hai đề xuất đã superseded trongv1.3. [Conformance review](06_SOURCE_CONFORMANCE.md) ghi rõ phần tương thích, phần mở rộng và giới hạn kiểm tra34 IDs.

## Tài liệu contract

1. [Context/API](01_CONTEXT_AND_API.md).
2. [R07 deletion/official history](02_R07_DELETION_RETENTION.md).
3. [Operations/retry/concurrency](03_OPERATIONS_RETRY_CONCURRENCY.md).
4. [Lifecycle/multi-device](04_LIFECYCLE_AND_MULTI_DEVICE.md).
5. [Pagination/cache/offline retention](05_PAGINATION_AND_ROOM.md).
6. [Source conformance](06_SOURCE_CONFORMANCE.md).
7. [Optional streaming](07_OPTIONAL_STREAMING.md) — EXCLUDED/NOT APPROVED, chỉ áp dụng nếu có ADR override tương lai.
8. [Approved amendment detail](08_APPROVED_AMENDMENTS_V14.md).

MVP giữ REST completed-response theo TECH-03, không yêu cầu phê duyệt streaming để triển khai REST. Capability phải có contract_revision1.4/backend support khi wire thật; absence/unknown→disabled with reason, không đoán từ API khác. Runtime [AC/IT/amendment tests](../10_ACCEPTANCE_QA/AMENDMENT_ACCEPTANCE_V14.md) chạy sau implementation.

34 original CHAT/GAP/FINAL bodies chưa truy xuất đủ vẫn PARTIAL; approval mới không tự làm VERIFIED. Rules có nguồn/approved current addendum dùng để implementation; archival mapping không chặn tài liệu hoàn chỉnh và không cần user approve lại.
