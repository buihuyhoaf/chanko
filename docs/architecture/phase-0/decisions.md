# Decision register

Đây là sổ đối chiếu, không tạo ADR độc lập mới hoặc yêu cầu duyệt lại quyết định đã Approved.
S1–S6 tham chiếu [inventory](source-inventory.md).

| ID | Quyết định | Hiệu lực | Nguồn / phạm vi |
|---|---|---|---|
| B01 | Kotlin, Compose Material 3, MVVM, Clean Architecture, Hilt, Flow/StateFlow; feature-based modularization | Approved | S1 §1,3,15; tách module dần, không tạo hết ngay |
| B02 | PostgreSQL/server authoritative; Room read cache + manual offline draft; không auto-submit/replay | Approved | S1 §5,11; S2 §2,8; S4 ADR-09/SPEC-09 |
| B03 | AI verdict, user decision, item status, Purchase là khái niệm riêng; 1 item nhiều Purchase | Approved | S4 invariants; S2 §3,5 |
| B04 | estimated_unit_price, actual_unit_price; VND nguyên; quantity INTEGER >=1 default1; backend tính total_amount | Approved | S2/S3/S4 phụ lục cuối §1,6; UI chỉ preview |
| B05 | Purchase mới -> PURCHASED; xóa Purchase cuối giữ status, tính lại chi và audit | Approved | Phụ lục cuối §1 |
| B06 | cycle_start_day cố định 1–31; tháng ngắn dùng ngày cuối giữ anchor; đổi amount có hiệu lực ngay cycle hiện tại và snapshot atomically; closed cycle giữ snapshot | Approved | Phụ lục cuối §2 |
| B07 | Hết hạn: COMPLETED + WAITING->CONSIDERING do server; Need More Time sau completed tạo phiên ACTIVE mới; mua/skip khi ACTIVE được phép và resolve atomically | Approved | Phụ lục cuối §3; không khóa quyền mua |
| B08 | Story Recap tính lại khi xem từ lịch sử, không lưu recap snapshot cố định; PAY_NOW/CREDIT cùng tác động chi | Approved | Phụ lục cuối §4–5 |
| B09 | Google Sign-In challenge/nonce; access 15 phút, refresh rotation idle30d/absolute90d; token secure storage ngoài Room | Approved baseline | S1 §6; S3 §3; không thêm email/password từ prompt cũ |
| B10 | Candy Pop 3D, Nunito, Light-only MVP, 4 tabs Home/My Items/Budget/Insights; accessibility WCAG2.2AA, touch48dp, scale200%, reduced motion | Approved | S5 UI-01–09, §10–12,16 |
| P01 | Tên/ràng buộc physical schema và 17 bảng backend | Proposed | S2 §1,3–4,9–11; không sao chép cả backend vào Room |
| P02 | Từng API path/field example/error detail ngoài quyết định explicit trong phụ lục | Proposed technical detail | S3 mở đầu; cần contract review, không tuyên bố backend đã có |
| P03 | Module graph, thứ tự extraction Phase1, package ownership | Proposed | dependency-graph.md, phase-1-plan.md; chưa phê duyệt thực thi |
| V01 | Serialization: Kotlin Serialization hay Moshi; secure token storage implementation | Pending | S1 §3.1 chưa chốt serializer; không lấy prompt cũ làm quyết định |
| V02 | TECH-04, env URLs/variants, CI/CD, secrets, backups, scheduler/notification behavior implementation | Pending | S6 rỗng trong quyền hiện tại; S1 §13 |
| V03 | Legacy dữ liệu local -> server, ownership, retention/logout/đổi account; migration tương thích | Pending | R1 model không có owner/server ID; S2 §8,11–12; không tự biến local thành official |
| V04 | UI on-colors/contrast, budget0 và threshold contract, mapping 9 business emotions -> 8 UI states, line heights, fallback styles, analytics definitions/assets/readback | Pending Verification | S5 PV-01–08 §17; threshold UI đã Approved, contract còn mở |
| V05 | API deployed/OpenAPI readback, TTL/limits/idempotency, deletion/retention/backdate policy | Pending | S2 §12/S3; phụ lục đã giải quyết quantity/budget hiệu lực/Need More Time, không mở lại |
| X01 | Full offline-first, general outbox, client-authoritative mutations | Superseded | S1 §15, S4 ADR-09 |
| X02 | 1 Purchase/item, refunds MVP, debt/credit-limit tracking | Superseded | S1 §15; không xóa dữ liệu lịch sử dựa trên nhãn này |
| X03 | expected_unit_price / estimated_price / expected_price, actual_price cho contract mới; quantity fractional; budget hiệu lực và Need More Time “chưa chốt” | Superseded | S2/S3/S4 phụ lục cuối §6; giữ nguyên snapshot lịch sử |
| X04 | TECH-03 nói TECH-02 còn trống; “offline/account chưa chốt” ở STATUS & NEXT PHASE cũ | Superseded historical note | S3 phụ lục đối chiếu; S4 Amendment/ADR-09 |
| X05 | Mô tả hiện tại “phải hết giờ mới mua/skip”, tổng giá PASSED là tiền tiết kiệm chắc chắn | Current implementation conflicts with Approved target | R1; S4 SPEC-05/06. Không tự sửa code ở Phase0 |
