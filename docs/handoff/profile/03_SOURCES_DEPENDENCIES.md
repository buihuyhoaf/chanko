# Đối chiếu nguồn và dependency

Đã đọc ngày 24/10/2026 qua Google Drive. Nội dung nguồn là tài liệu tham chiếu, không chứng minh API/source code đã tồn tại. Bản text đọc được lưu trong evidence/ để đối chiếu, không thay bản gốc có bảng/ảnh.

| Nguồn | Kết quả |
|---|---|
| [UI UX Design System & Visual Guidelines v1.0](https://docs.google.com/document/d/1OPZt4JU1WpGYJHL5RIjJYmmx847Cg7Yg/edit) | DOCX 18/10/2026; đọc được bản text, có UI-01–09; các bảng số liệu có thể không được hydrate đầy đủ. Chưa coi đủ 13 typography styles là đã trích xuất chính xác |
| [TECH-01 System Architecture](https://docs.google.com/document/d/1iedDkUbbOWThMm_vDs5RRQ-BKJ6NMDLTkb4YhcTapEI/edit) | Đã đọc: Native Android, Compose/M3, MVVM/Clean, Hilt/StateFlow, online-first, Room cache, không general outbox; core mascot |
| [TECH-02 Data Architecture](https://docs.google.com/document/d/1nLLLfrWpU0tKbrL9YhjKsTZiVcpYZ_5lQ5fgHKY-0dQ/edit) | Đã đọc nội dung hiện có: user_preferences có personality/default SASSY và version dạng đề xuất; cache tách account; retention/draft logout chưa chốt |
| [TECH-03 API & Backend](https://docs.google.com/document/d/1BA6OjK-vDcXEVz0Ejcao7Krg4D9i6qqc5XNMMkSTkDI/edit) | Đã đọc: auth/logout revoke phiên idempotent; secure tokens; backend-authoritative. Chưa có preferences endpoint/DTO cụ thể đủ để code Save |
| [TECH-04 folder](https://drive.google.com/drive/folders/1NUrEJ01QyXenhQ6qS2jf5rvqcgnbPRCt) | Đã list: files=[]; không xác minh được deployment/environment/notification delivery |
| Chat 6adc3ce3-3b14-83ec-a634-ee28430cb6f6 | Đọc 10 turn-limit, hết lịch sử trả về; user xác nhận thiết kế và mascot, assistant response là content-reference. Lấy được mascot attachment, không lấy được mockup hoặc sổ PROFILE gốc |

## Coverage UI-01–10

| ID | Nội dung đã xác minh từ nguồn | Áp dụng |
|---|---|---|
| UI-01 | Candy Pop 3D; một trọng tâm; 2D sạch + 3D chọn lọc | Cozy Corner là illustration, Settings native |
| UI-02 | Brand purple; Light-only MVP; on-colors còn pending | Cream theo yêu cầu mới; contrast đo thực tế |
| UI-03 | Nunito ONLY, 13 styles; không default font | Mapping exact sizes từ bảng gốc còn dependency |
| UI-04 | Soft radius/elevation; shadow specifics chưa chốt | Component dims đề xuất có nhãn rõ |
| UI-05 | Mèo trắng nhất quán; pre-render alpha; không real-time 3D | Candidate theo identity; fallback tĩnh |
| UI-06 | Material Symbols Rounded; 40dp container vẫn cần 48dp hit | Rounded icons toàn màn |
| UI-07 | Soft/bouncy/responsive; duration tham khảo; Reduce Motion | Tap reaction ngắn, không LLM |
| UI-08 | Spacing/layout/components; 20dp token, Primary 56dp | Scroll/insets, reuse primitives |
| UI-09 | States, accessibility, architecture; 4 Home tabs | Profile child route không bottom nav theo yêu cầu mới |
| UI-10 | Không tìm thấy khi search UI-10 | Chưa đối chiếu; không tự tạo nội dung |

TECH-03 có câu lịch sử “TECH-02 còn trống tại thời điểm đối chiếu”, nhưng lần đọc này TECH-02 có nội dung. Không lặp lại kết luận lịch sử đó như tình trạng hiện tại. Tài liệu UI mô tả ReadyForReviewCard toàn app; yêu cầu hiện tại chỉ giới hạn Profile row là notification preference, không xóa review flow ở các màn khác.

## Dependency register

| ID | Cần xác minh | Mức ảnh hưởng / hướng xử lý |
|---|---|---|
| D-01 | Mockup Profile gần nhất | Gate pixel-match và asset approval; làm layout theo handoff nhưng ghi visual sign-off pending |
| D-02 | AUTH-04 đầy đủ | Gate Sign Out release: offline behavior, cleanup tokens/cache/draft/ảnh, Google credential/session, confirmation; không suy từ logout endpoint |
| D-03 | Personality read/update API, DTO enum/version/error contract | Gate integration Save; dùng repository interface/preview state theo project, không bịa endpoint trong production |
| D-04 | Ready for Review storage scope + delivery/channel/permission policy | Gate notification integration; không tạo scheduler thứ hai |
| D-05 | UI-10 và các bảng tokens nguyên bản | Gate tuyên bố đủ design-system compliance; thông số đề xuất không gọi Approved |
| D-06 | Nunito binaries/license, icon pipeline, current dependencies/target SDK | Agent kiểm tra repository khi được giao implement; handoff này không kiểm tra repo |
| D-07 | Privacy URL/content, About copy/build metadata | Gate release các link/nội dung; không dùng placeholder |
| D-08 | Asset candidate halo, alpha, match mockup, size budget | Gate production art; không crop screenshot để chữa thiếu layer |
| D-09 | TECH-04 deployment/environment | Gate xác minh host/API env và reminder delivery; folder hiện trống |

Không dependency nào là lý do viết lại nghiệp vụ đã chốt. Tiếp tục các phần độc lập, báo chính xác phần chưa tích hợp/kiểm chứng. Không gửi email/comment hay sửa tài liệu Drive trong lần bàn giao này.
