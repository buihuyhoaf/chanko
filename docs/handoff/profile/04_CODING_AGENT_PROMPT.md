# Prompt giao coding agent

Bạn triển khai màn Chanko Profile & Settings đã được chốt. Đọc toàn bộ 01_IMPLEMENTATION_HANDOFF.md, 02_ASSETS_AND_ANDROID_RESOURCES.md, 03_SOURCES_DEPENDENCIES.md và asset inspection trước. Đọc AGENTS.md cùng kiến trúc/token/component hiện có trong repository trước khi sửa. Tôn trọng permission và hướng dẫn repository. Đây là task implement ở phiên của bạn; bộ handoff nguồn không chứa code đã thực hiện.

Phạm vi bắt buộc:
- Gộp Profile và Settings. Home chỉ avatar mở Profile; bỏ Settings icon Home. Profile là child destination, không bottom navigation. Back về Home giữ context.
- Candy Pop 3D, cream #FAF8F5, purple #8965F6, Nunito ONLY, Material 3. Reuse theme/components của dự án. Không dựng màn bằng screenshot raster.
- Cozy Corner: mèo trắng identity từ reference, ngồi ôm cốc tím tại bàn; tap chỉ decorative reaction, no LLM/no business mutation. Candidate trong gói chưa được duyệt release, cần kiểm tra alpha/quầng và đối chiếu mockup thật khi có. Không dùng tư thế khoanh tay thay scene đã chốt.
- Personality màn riêng: Friendly / Sassy default / Savage; chọn thay draft, preview tĩnh, Save mới ghi chính thức. Online-only. Dirty Back confirm Keep editing/Discard. Network timeout cần reconcile; không auto-save/replay sau reconnect; verdict/history không đổi.
- Ready for Review chỉ notification preference; phối hợp app preference, permission và channel state, không đổi cooling-off/item. Không thêm notification types mới.
- Google Account read-only. Privacy/About nội dung thật. Sign Out phải tuân AUTH-04, không suy chính sách xóa draft/cache từ một endpoint.

Trình tự làm:
1. Inspect repository, xác nhận navigation graph, modules, auth/preferences/notification services, tokens, Nunito và image pipeline thực có. Báo mismatch có evidence.
2. Đọc nguồn Drive nếu truy cập được, ưu tiên lấy AUTH-04, UI-10, bảng tokens và mockup còn thiếu. Đừng gọi mapping PROFILE-01–10 trong handoff là trích nguyên văn sổ cũ.
3. Triển khai các phần đã rõ bằng cấu trúc hiện có: route/content, immutable state/StateFlow, one-shot navigation effects, repository/use-case theo project. Không thêm thư viện/engine 3D tùy tiện.
4. API preference và notification chưa xác minh: nối contract thật; nếu thiếu, hoàn thiện UI/state bằng preview/fake chỉ trong môi trường test, báo integration blocked cụ thể. Không invent endpoint hoặc cho production báo Save thành công giả. Gate riêng Sign Out theo D-02.
5. Chỉ import art release khi QA đạt. Không cắt nguyên mockup/card/text làm asset. Scene phẳng chỉ animate toàn scene; chuyển động riêng cần layer thật.
6. Test theo AC-01–20: tập trung navigation guard, dirty/save/timeout/conflict, offline, permission matrix, session/account isolation, AUTH-04, font 200%, TalkBack, system insets và asset alpha. Chạy check dự án phù hợp.
7. Bàn giao thay đổi + ảnh màn thực tế + test results + dependency unresolved. Phân biệt implemented/verified/pending. Không tuyên bố pixel-match khi chưa có mockup duyệt, không tuyên bố logout compliant khi chưa có AUTH-04.

Không thêm Edit Profile, unlink/change Google account, dark mode toggle, pet game, review list ở Profile, marketing notifications, account deletion, bottom tab mới hoặc feature ngoài scope. Không đổi nghiệp vụ AI/purchase/budget. Nếu thiếu thông tin, tiến hành phần độc lập và hỏi đúng dependency, tránh hỏi lại quyết định đã chốt.
