# Chanko — Phase 0 Architecture Baseline

Ngày đối chiếu: 18/10/2026 (Asia/Ho_Chi_Minh). Phạm vi: documentation only.
Trạng thái: baseline tài liệu đã lập; kế hoạch Phase 1 là Proposed, chưa triển khai.

## Điểm bắt đầu

- [Inventory và nguồn](source-inventory.md): hiệu lực tài liệu, liên kết gốc và giới hạn bản trích xuất.
- [Repository baseline và gap matrix](gap-matrix.md): bằng chứng code tại commit khảo sát.
- [Dependency graph](dependency-graph.md): phân biệt hiện trạng, luồng chạy và phụ thuộc đề xuất.
- [Migration Phase 1](phase-1-plan.md): trình tự, điều kiện vào, kiểm chứng và rollback.
- [Decision register](decisions.md): Approved / Proposed / Pending / Superseded.
- [Manifest nguồn](references/manifest.json): ID, URL, thời điểm và tên bản trích xuất.
- [Báo cáo hoàn thành](completion.md).

## Ranh giới

Không sửa Kotlin, Gradle, Room schema, UI hoặc nghiệp vụ. Không chạy Phase 1.
Các bản trích xuất chỉ hỗ trợ truy vết, không là bản gốc byte-for-byte hay thay thế phê duyệt.
Nguồn gốc trên Drive và phần/phụ lục được dẫn phải được dùng cùng nhau; không lấy đoạn cũ đã Superseded làm contract mới.
