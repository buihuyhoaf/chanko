# Inventory tài liệu và bằng chứng

## Quy ước hiệu lực

Approved: nguồn ghi rõ đã duyệt cho quyết định tương ứng. Proposed: thiết kế/chi tiết triển khai đề xuất.
Pending: chưa có bằng chứng hoặc hợp đồng còn thiếu. Superseded: có nguồn chỉ rõ nội dung thay thế.
Không gán Approved toàn bộ chi tiết chỉ vì tiêu đề tài liệu là Approved. Reference của UI được giữ nguyên nhãn nguồn; khi cần quyết định triển khai thì xử lý như đề xuất, không biến thành phê duyệt.

## Nguồn đã truy cập ngày 18/10/2026

| ID | Tài liệu / liên kết gốc | Phiên bản, hiệu lực | Đã đọc và giới hạn |
|---|---|---|---|
| S1 | [TECH-01](https://docs.google.com/document/d/1iedDkUbbOWThMm_vDs5RRQ-BKJ6NMDLTkb4YhcTapEI/edit?usp=drivesdk) | v1.1, 17/10; Approved consolidated baseline | Nguyên tắc §1, Android §3, data §5, auth §6, phạm vi §14–15; cây module §3.2 là đề xuất logic |
| S2 | [TECH-02](https://docs.google.com/document/d/1nLLLfrWpU0tKbrL9YhjKsTZiVcpYZ_5lQ5fgHKY-0dQ/edit?usp=drivesdk) | v1.1, 17/10; mixed Approved/Proposed/Pending | §1 phân biệt hiệu lực; §2,5–8 baseline; 17 bảng, physical keys/indexes §3–4,9–11 Proposed; §12 cần đọc cùng phụ lục cuối |
| S3 | [TECH-03](https://docs.google.com/document/d/1BA6OjK-vDcXEVz0Ejcao7Krg4D9i6qqc5XNMMkSTkDI/edit?usp=drivesdk) | v1.1, 17/10; baseline + cụ thể hóa kỹ thuật | §1–4 kiến trúc/API/auth/transactions; §6 security/tests; tên/path ví dụ không được duyệt riêng toàn bộ; phụ lục cuối có hiệu lực |
| S4 | [Functional Specification](https://docs.google.com/document/d/1I-H3HP8LwBXYNBkAX85IQFMt3-346t3B-Kak246M8Go/edit?usp=drivesdk) | v1.1 consolidated, Approved | SPEC-01–09, INPUT-01–04, ADR-09, AUTH-05 và phụ lục chốt nghiệp vụ cuối; giữ lịch sử trước phụ lục |
| S5 | [UI UX Design System & Visual Guidelines](https://drive.google.com/file/d/1OPZt4JU1WpGYJHL5RIjJYmmx847Cg7Yg/view?usp=drivesdk) | v1.0, 18/10; Approved / Reference / Pending Verification | UI-01–09, §15–17 và bảng quyết định; không nâng cấp Reference hoặc PV thành Approved |
| S6 | [TECH-04 folder](https://drive.google.com/drive/folders/1NUrEJ01QyXenhQ6qS2jf5rvqcgnbPRCt) | Pending; chưa đọc được tài liệu nội dung | Drive search “TECH-” và “Environment”, fetch folder trả files=[]; không khẳng định tài liệu không tồn tại ở mọi nơi |
| S7 | [Hội thoại tiếp nối](chatgpt-conversation://6ad440b9-efb8-83ec-bb87-ae8c82e59b77) | Historical reference, không là nguồn phê duyệt kỹ thuật độc lập | read_thread 10 turns; một số nội dung dài bị cắt. Audit attachment gốc chưa đọc được; không dùng làm chứng cứ test đã chạy |
| R1 | Repository Chanko, HEAD 10fb6e10c92ed86c2531fcf9b59263d0754c6358 | Observed implementation; không là phê duyệt mục tiêu | Đọc source/build/README/AGENTS trực tiếp; xem gap matrix |

Repository root do user chỉ ra qua thư mục app:
`/Users/buihoa/Học tập/Android/chanko`.
Branch lúc khảo sát: `dev-chanko-1x`. AGENTS duy nhất tìm thấy trong repo là root; không thấy AGENTS ở các thư mục cha đã kiểm tra. Quy tắc Git được giữ nguyên.
Workspace ban đầu là mirror ChatGPT, không có checkout Android. Các file synced trong sources/ và outputs/qa hiện có được bảo toàn.

## Bảo toàn nguyên bản

Dùng liên kết canonical trong bảng làm tham chiếu bản gốc, không ghi lại hoặc thay đổi tài liệu Drive.
Năm file references/*.extracted.txt lưu nguyên nội dung text do connector search best_effort_fetch trả về, chỉ chuẩn hóa CRLF thành LF và thêm newline cuối file; bảng/định dạng có thể bị làm phẳng.
Đây không phải bản sao DOCX/PDF gốc và không bảo đảm text extractor giữ mọi hình/bảng. Metadata và nguồn ở manifest.json; SHA256SUMS xác thực bản text đã lưu, không chứng minh phiên bản gốc bất biến.
Không sửa các đoạn lịch sử trong snapshot; hiệu lực được giải thích riêng ở decisions.md.
Không lưu các kết quả tìm kiếm không liên quan dự án.

## Quy tắc đối chiếu

Trong S2/S3/S4, “PHỤ LỤC CHỐT NGHIỆP VỤ & HỢP ĐỒNG TRIỂN KHAI — 17/10/2026 (CÓ HIỆU LỰC)” ghi rõ thay nội dung cũ trái ngược.
Không đơn thuần chọn tài liệu mới hơn để bỏ nguồn nghiệp vụ. UI không tự thay state machine; các PV UI giữ mở.
TECH-03 ghi TECH-02 trống là lịch sử: phụ lục đối chiếu của chính S3 xác nhận đã có nội dung.
TECH-04 thiếu không cho phép tự chọn production URL, flavors, CI/CD, secrets hoặc notification scheduler.
