# Change note CHAT-UX-REV-01
25/10/2026 · UI/UX revision bổ sung vào handoff v1.4. Không ghi đè các baseline SPEC/TECH hoặc chỉnh tài liệu nguồn trên Drive.

## Thứ tự thẩm quyền
Chỉ thị hiện tại của user và mockup vừa gửi chốt cấu trúc mới; ràng buộc BUY neutral trong chỉ thị thắng hình BUY ăn mừng trong mockup. Quy tắc nghiệp vụ đọc được và approved amendment v1.4 giữ hiệu lực. Design System v1.0 giữ palette/Nunito/Material 3/spacing/shapes. Các kích thước Android, cách trình bày nested route và microcopy chi tiết là Reference, không phải approval mới cho backend.

## Diff và mapping
| Trước revision | Sau revision | Target | Ảnh hưởng kỹ thuật |
|---|---|---|---|
| Welcome Chat là entry riêng | Chat List là entry quản lý hội thoại | CL | Chuyển entry Home; không tạo entity mới |
| App bar mở Chat History riêng | Thread list nằm ngay CL, CD Back về CL khi vào từ list | CL/CD | Giữ thread IDs, paging, query và scroll |
| Nhiều visual màn/state S01–S11 | Chỉ CL/CD/NC; loading/offline/archived là state | cả 3 | Không đổi lifecycle enum |
| Picker riêng | Item picker inline trong NC hoặc sheet của CD FREE | NC/CD | Same-thread bind giữ approved v1.4 |
| Verdict xanh, “Nên mua”, sparkles, khẳng định ngân sách | Neutral card, “Có thể cân nhắc mua”, lý do có nguồn, không celebration | CD | Không đổi verdict BUY hoặc tạo purchase |
| Menu baseline không Rename | Thêm Rename dialog theo yêu cầu UX hiện tại | CL/CD | Chưa có contract rename: gap RENAME-API, không tự thêm endpoint/schema |
| Filter trạng thái trong mockup | Giữ Tất cả / Đang cân nhắc / Chờ xem lại / Đã mua; loại Free/Item trong sheet lọc | CL | Dùng item projection/capability; không suy Ready/Postponed thành ItemStatus mới |

## Khác biệt có chủ ý so với ảnh gốc
1. CD Decision Card: bỏ nền xanh thành công, chữ xanh lớn, sparkles và mascot được dùng như ăn mừng. Dùng canonical cat tĩnh/crossed-arms ở vai trò Neutral, nền lavender nhẹ, chữ tối. Không tự sửa mặt mascot gốc. “BUY · Có thể cân nhắc mua” là nhận định có điều kiện, không lệnh mua.
2. Bỏ câu “phù hợp ... ngân sách hiện tại” nếu không có structured snapshot ngân sách hợp lệ. Fixture thay bằng “Hãy kiểm tra lại nhu cầu và ngân sách trước khi quyết định”. Không bịa hiệu năng/giá hoặc budget facts từ chat.
3. Các dấu check xanh trong lý do đổi thành bullet trung tính trong reference Android; lý do thật chỉ lấy structured response đã validate. “Xem món đồ” là lối sang flow nghiệp vụ riêng, không CTA mua.
4. Giữ white/cream/purple, hero mascot, hàng thread kiểu Messenger, fixed “+ Đoạn chat mới”, bubbles, quick replies và composer. Chữ trên pastel/gradient dùng foreground tối đủ contrast; không mặc định chữ trắng trên tím brand.
5. Reference Android không vẽ iOS notch/status bar/home indicator. Mọi hệ thống insets do Android cấp; New Chat giữ cách trình bày sheet như ảnh gốc nhưng không coi item picker là màn thứ tư.
6. Ảnh gốc có sản phẩm thương mại để minh họa; bộ triển khai dùng dummy product placeholder trong fixtures. Không dùng mascot trong product media; thumbnail thật do item repository cung cấp khi app chạy.
7. Ảnh gốc đang chọn Free Chat mà vẫn có item rows. Reference làm rõ: chọn item sẽ chọn Item Chat; CTA nêu đúng context trước khi mở CD, không gắn item ngầm.

## Phạm vi đã chốt / chưa chứng nhận
Đã chốt UX mới: 3 màn, chatbox đúng vị trí, dialogs/states, entry points, same-thread conversion, neutral BUY. Tài liệu này không phê duyệt migration/DTO mới, triển khai backend, rename API, realtime/SSE, durable chat draft hoặc runtime app. Các contract v1.4 đã được phê duyệt trong nguồn riêng; approval không phải deployed evidence.

## Nguồn đã đối chiếu trên Drive
| Nguồn | URL | Kết luận áp dụng |
|---|---|---|
| Project “Should I Buy It?” chứa tài liệu Chanko | https://drive.google.com/drive/folders/1vSP7XEKoB6LH5WTS3JF0NYSMwq-6PZYl | Folder cha được xác minh bằng metadata, chứa SPEC/TECH/Design và handoff cũ |
| Handoff Chat v1.4 | https://drive.google.com/drive/folders/1hNuBnOWckpsIAKamdyi4KflIs4zneCOn | Nguồn trước revision; không overwrite |
| UX_SPECIFICATION | https://drive.google.com/file/d/10RZ09m2r8N1PfLexrHkO6p0kGYiVvu40/view | Welcome/History cũ bị thay ở UI; business invariants giữ |
| NAVIGATION | https://drive.google.com/file/d/1o8ZVIqLLEsVFGDVZCMvHPhHWkMkoADrI/view | Entry Home/Item Detail, back stack giữ; History gộp CL |
| LOGICAL_CONTRACTS | https://drive.google.com/file/d/1UddiZYxAo1be4VrRHL_3zwe7qTGS2UCe/view | Read models, retry, repository seams giữ; no-Rename cũ superseded UX-only |
| UI_DETAIL_DEFAULTS | https://drive.google.com/file/d/1_wHCGj-4D8vwLD0eld2JN0RdkEMhFKda/view | FillComposer, draft, scroll, pending giữ |
| Approved amendments v1.4 | https://drive.google.com/file/d/1oz12esHXjdVhtCwAwHwIBpSe3vSVHR3s/view | Same-thread bind, provenance, retained cache, version guards thắng câu cũ |
| DECISION_BASELINE | https://drive.google.com/file/d/14Ozir8lpBege5KALP-Sehs10ksNxRRl0/view | 34 bodies vẫn PARTIAL; không map bịa |
| TECH-02 v1.1 | https://docs.google.com/document/d/1nLLLfrWpU0tKbrL9YhjKsTZiVcpYZ_5lQ5fgHKY-0dQ/edit | PostgreSQL authority, Room cache, evaluation/decision/purchase độc lập |
| TECH-03 v1.1 | https://docs.google.com/document/d/1BA6OjK-vDcXEVz0Ejcao7Krg4D9i6qqc5XNMMkSTkDI/edit | Backend orchestration/REST, idempotency/preconditions/auth; không gọi provider trực tiếp |
| Design System v1.0 | https://docs.google.com/document/d/1OPZt4JU1WpGYJHL5RIjJYmmx847Cg7Yg/edit | Palette, Nunito, radii, spacing, a11y giữ |

Hội thoại được đọc lại có nội dung assistant là content-reference markers, không có body design để trích nguyên văn. Canonical mascot lấy được từ attachment thật. Mockup được user đính kèm bổ sung trong phiên này; tên clipboard khác tên `/mnt/data` nhưng user cung cấp để đối chiếu cùng request. Giữ bytes ảnh mới như source evidence; không khẳng định hash trùng tệp `/mnt/data` chưa truy cập được.
