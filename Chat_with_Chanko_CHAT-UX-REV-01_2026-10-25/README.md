# Chat with Chanko — CHAT-UX-REV-01
Ngày bàn giao: 25/10/2026, Asia/Ho_Chi_Minh. Ngôn ngữ: tiếng Việt. Phạm vi: UI/UX, assets, hướng dẫn triển khai Android; không có Android source hoặc PR.

## Đọc theo thứ tự
1. `01_SPEC/CHAT-UX-REV-01.md`: hiệu lực, thay đổi, bằng chứng và ranh giới.
2. `01_SPEC/SCREEN_SPEC_VI.md`: ba màn và component contracts.
3. `01_SPEC/NAVIGATION_STATE_MATRIX.md`: trạng thái, điều hướng, dialogs/sheets.
4. `01_SPEC/DESIGN_SYSTEM.md` + `design_tokens.json`: token Approved và lựa chọn Reference.
5. `01_SPEC/TECH_BOUNDARY_AND_EDGE_CASES.md`: adapter, retry, offline, provenance.
6. `03_VISUAL_REFERENCES`: ảnh gốc được user gửi, bản hiệu chỉnh và ba reference Android có chữ thật.
7. `02_ASSETS`: mascot PNG/WebP, icon SVG/PNG, decorations, font + licenses, manifest + mapping.
8. `04_QA`: acceptance checklist, kết quả QA tài liệu/assets và blockers.
9. `05_ANDROID_AGENT/PHASED_PROMPT_VI.md`: prompt theo phase, dùng riêng khi bắt đầu triển khai.

## Hiệu lực
Chỉ có 3 màn chính: CL Chat List, CD Chat Detail, NC New Chat. New Chat có thể được trình bày full-height sheet như mockup; vẫn là một trong ba bề mặt chính, không bổ sung một màn picker/history/archived riêng. Không Welcome Chat riêng, không route Chat History riêng. Chat List là entry quản lý hội thoại. Chatbox chỉ có ở Chat Detail.

Giữ Home / My Items / Budget / Insights và nút + toàn cục của app. Không thêm Chat tab. Flow chat là nested destination từ Home/Item Detail; không tạo một bottom navigation mới trong Chat. Khi quay về shell, đủ 4 tabs nguyên trạng. Đây là cách trình bày Reference Android vì mockup gốc không vẽ shell app.

Ảnh gốc là evidence, KHÔNG dùng nguyên verdict xanh/sparkles làm production. Bản hiệu chỉnh trung tính và spec thắng những chi tiết đó. Ảnh UI là tham chiếu, không phải image button hoặc text raster dùng trong app. Giá/nhu cầu/ngân sách trong fixture không phải dữ liệu thật hoặc lời khuyên mua.

## Tình trạng
Yêu cầu UX mới được chốt qua chỉ thị trực tiếp và ảnh user cung cấp. Exact Android dimensions, microcopy, text/on-color pairing và các fixture là Reference để triển khai/QA. Không giả là đã approve riêng hoặc đã deploy. Rename có UX theo yêu cầu mới nhưng contract server còn phải xác minh. 34 body quyết định cũ: 0 VERIFIED / 34 PARTIAL, giữ approval riêng với mức xác minh nguồn.

Deliverable cuối nằm trên Google Drive. Thư mục `/tmp` chỉ dùng xử lý/kiểm tra; không ghi vào Documents/ChatGPT/Chanko hoặc thư mục project ChatGPT. Các source đồng bộ được đọc nguyên trạng.
