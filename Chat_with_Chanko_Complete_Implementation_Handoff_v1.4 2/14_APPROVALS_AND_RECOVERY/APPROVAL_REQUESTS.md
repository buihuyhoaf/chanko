# Kết quả phê duyệt — không còn A1–A5 chờ user

| Nhóm | Quyết định trực tiếp | Hiệu lực |
|---|---|---|
| A1-A | APPROVED | Delete/purge/retention và official history độc lập, theo13/02 |
| A2-A | APPROVED WITH AMENDMENT | Free→Item cùng thread sau explicit confirmation, giữ transcript, không tạo item;13/01+08 |
| A3-A | APPROVED | Operation identity/idempotency/retry/concurrency/lifecycle/sync;13/03+04+08 |
| A4-A | APPROVED WITH AMENDMENT | Freshness TTL khác offline retention; cached data tiếp tục đọc offline;13/05+08 |
| A5-A | APPROVED | Quick reply không overwrite/auto send; session draft; predefined reaction semantics |

[Nguyên văn approval](USER_TECHNICAL_APPROVAL_V14.md), [register máy đọc](approval_register.json), [approved amendment addendum](../13_BACKEND_API_CONTRACTS/08_APPROVED_AMENDMENTS_V14.md).

Không cần approve lại5 nhóm hoặc visual baseline. Optional SSE chưa được phê duyệt và không thuộc REST MVP, không chặn triển khai.34 original source bodies là archival follow-up, không yêu cầu duyệt lại toàn UX hoặc nâng VERIFIED tự động. Các alternativesB v1.3 không được chọn; lịch sử đề xuất giữ ở12/contract_history_v13.

Runtime và backend implementation/deployed API conformance vẫn kiểm sau implementation. Không dùng những kiểm tra chưa thể chạy để biến approved contracts thành pending approval.
