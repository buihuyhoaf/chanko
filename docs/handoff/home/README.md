> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Chanko Home — Compose implementation handoff

Ngày đóng gói: 24/10/2026 • Ngôn ngữ tài liệu: tiếng Việt • Phiên bản: 3.0 — image-aligned + generated assets

Bắt đầu tại [START-HERE](START-HERE.md). Đây là bộ tài liệu triển khai, không phải bản code đã tích hợp. Không xác nhận tình trạng repository, API hay build hiện tại. Không sửa code ứng dụng.

**Ảnh chuẩn:** [Home người dùng đã chốt](references/approved-home-user-reference.png). **v2 thay thế v1 về visual.**

## Nội dung

- [Visual reference và mapping theo ảnh](00-VISUAL-REFERENCE.md).

- [Approved UX spec](01-APPROVED-UX-SPEC.md): cấu trúc được chốt và giới hạn nguồn chứng cứ.
- [Component tree & contracts](02-COMPONENT-CONTRACTS.md): state, event, điều kiện và ownership.
- [Design tokens](03-DESIGN-TOKENS.md): Candy Pop/Nunito, phân biệt giá trị tham chiếu và đã duyệt.
- [Resource manifest](04-RESOURCE-MANIFEST.md), [manifest máy đọc](resource-manifest.json), [asset briefs](assets/ASSET-BRIEFS.md).
- [Roadmap Phase 0–11](05-ROADMAP.md), [prompts theo phase](prompts/README.md).
- [Fake-data scenarios](06-FAKE-DATA.md), [fixtures JSON](fixtures/home-scenarios.json).
- [Previews/tests](07-PREVIEWS-TESTS.md), [acceptance](08-ACCEPTANCE.md).
- [Pending TECH alignment](09-PENDING-TECH-ALIGNMENT.md), [traceability](10-TRACEABILITY.md).
- `references/`: ảnh cũ và ghi chú nguồn. `assets/`: asset thực có, không phải đầy đủ scene mới.
- `CHANKO-HOME-COMPLETE.md`: bản gộp Markdown để đọc một file.
- `SHA256SUMS.txt`: checksum mọi file nội dung (không chứa chính nó).

Tên file trong bộ ZIP là đường dẫn thật của gói này. Tên Compose là hợp đồng đề xuất, không khẳng định chúng tồn tại trong repo.

- [Asset delivery v3](11-ASSET-DELIVERY.md) — sáu PNG mới và preview.
