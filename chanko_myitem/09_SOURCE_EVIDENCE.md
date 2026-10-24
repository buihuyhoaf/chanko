# Source evidence • 24/10/2026 • Asia/Ho_Chi_Minh

## Nguồn trực tiếp đã đọc lại (cập nhật sau Recovery)

| Source | URL / ID | Tình trạng |
|---|---|---|
| S1 Functional Specification | https://docs.google.com/document/d/1I-H3HP8LwBXYNBkAX85IQFMt3-346t3B-Kak246M8Go/edit | Fetch thành công, modified2026-10-17T10:10:59.447Z; phụ lục có hiệu lực đọc đủ |
| S2 TECH-02 | https://docs.google.com/document/d/1nLLLfrWpU0tKbrL9YhjKsTZiVcpYZ_5lQ5fgHKY-0dQ/edit | Fetch thành công, modified2026-10-17T10:11:01.531Z; phụ lục có hiệu lực đọc đủ |
| S3 Design System v1.0 | https://docs.google.com/document/d/1OPZt4JU1WpGYJHL5RIjJYmmx847Cg7Yg/edit | Fetch readable text thành công, modified2026-10-18T03:50:21.858Z; provider báo Word MIME; không giả đây là native Google Doc mới |
| S4 Chat Thiết kế màn hình Món đồ | 6adcb6ee-a8f4-83ec-b3e0-16d60e40d5ae | read_thread trả5 lượt, hasMore=false,nextCursor=null; nhiều agent messages chỉ content-reference; attachment v0.9 truy cập được |
| S5 Chat Soạn bàn giao Android My Items | 01a16e40-2c53-7b81-95cd-8402efa9c877 | Xác minh request trước và giới hạn truy xuất nguồn; không có body29 mã bổ sung |
| S6 Yêu cầu review hiện tại | Chat Review Chanko handoff ZIP | Dummy-only hero, canonical/no-dog/no-cat-in-product, scope documents/assets, no app/PR |

Các query Drive `Chanko` và metadata name Chanko/ITEM/My Items/mascot không có kết quả; không suy điều đó có nghĩa các file không tồn tại. Fetch trực tiếp3 URL đã biết thành công. Không truy xuất được canonical bằng các nguồn này, không tự dùng ảnh mèo từ chat khác chưa được xác nhận.

## Mốc hiệu lực và mapping

S1/S2 phụ lục cuối17/10/2026 ưu tiên các đoạn cũ trái ngược:

- Mục1 và6: estimated_unit_price, actual_unit_price; VND nguyên, quantity INTEGER≥1; backend total; xóa cuối giữ item status.
- Mục2: cycle start day cố định, clamp giữ anchor, amount tức thì current cycle trong cùng transaction, closed snapshots giữ nguyên.
- Mục3: ACTIVE completion→COMPLETED/CONSIDERING; Need More Time tạo phiên mới; các nhánh cancel/buy/skip/manual từ WAITING atomically.
- Mục4: Story Recap tính lại khi xem, không snapshot recap cố định.
- Mục5: Credit=PAY_NOW, không debt/refund/discovery/rollover.
- S1 SPEC-03/04: multi-purchase, correction, Frequent UI; SPEC-05: review3 lựa chọn; ADR-09/SPEC-09: cache/draft/no-outbox.
- S2 mục5/6/8/9: history/count, cycles, draft, concurrency/authorization/idempotency. Đề xuất vật lý trong TECH-02 không tự thành endpoint hoặc schema đã triển khai.
- S3 mục15/16 và bảng UI-01..09: Approved tokens/spacing/adaptive/a11y; mục17/PV giữ phần chưa xác minh. Không dùng ngưỡng budget UI để tạo business restriction.

Ba snapshot nguồn giữ trong sources, nội dung khớp v0.9 sau chuẩn hóa BOM/newline. Bảng tuyến tính hóa không mang ảnh/visual gốc. Không public snapshot hoặc đưa vào APK; không gán license CC0 của placeholder cho tài liệu nguồn.

## Lời user truy xuất được

Chat S4 có yêu cầu: “ảnh chi tiết món đồ sẽ ko có con mèo nằm cạnh sản phẩm đâu nhé, chỉ cần hiển thị ảnh của sản phẩm”; sau đó: “về ảnh chi tiết sản phẩm thì nên để ảnh dummy thôi nhé, không cần để ảnh túi và tai nghe như thế kia đâu.” Request S6 nhắc thêm không giày và phải canonical white Chanko, không chó. Đây là căn cứ hero dummy-only và cấm mascot trong/cạnh media; không phải bằng chứng nội dung ITEM/DETAIL cụ thể.

Baseline recovery user cung cấp sau báo cáo trước đã khôi phục đủ29 body, thay thế giới hạn thiếu nội dung lịch sử cho mục đích đối chiếu UX. Các câu ghi thiếu29 mã trong bản pre-recovery nay đã được giải quyết; không dùng chúng như blocker còn mở.


## S7 — User Approved UX Decisions — Recovery Baseline

Nguồn file user cung cấp: `Văn bản đã dán.txt`, lưu nguyên văn tại `sources/approved_ux_recovery_baseline.md`. Status Approved UX, đủ29 rows; không thay Functional/TECH-02 làm chuẩn backend. Source này cho phép khôi phục Approved filter/tiles/card/navigation/detail/Ready/Postponed/Skip Waiting/archive/transaction UI như07. Không suy diễn lựa chọn cho các EDGE khác.

Sau khi nhận S7, fetch trực tiếp lại S1/S2 từ Drive thành công, modified times giữ nguyên2026-10-17T10:10:59.447Z /10:11:01.531Z. S3 dùng bản đã fetch/so khớp trước trong cùng phiên. Recovery reconciles EDGE01 như presentation-only và GAP02 với cancel ACTIVE; không tìm thấy rule backend rõ cho C01/C02. Canonical file/approved screen image không nằm trong S7; A01 vẫn cần asset.
