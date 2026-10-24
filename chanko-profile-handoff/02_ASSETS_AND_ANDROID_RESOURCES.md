# Asset inventory & production plan

## Phân biệt rõ ba loại

1. **Reference:** mascot_crossed_arms.png là ảnh người dùng cung cấp, giữ nguyên byte. Là nguồn identity; tư thế khoanh tay không dùng thay Cozy Corner. Quyền sử dụng thương mại không thể suy ra chỉ từ upload; giữ provenance người dùng cung cấp.
2. **Approved mockup:** chưa truy xuất được file a_clean_cute_pastel_3d_cg_ui_mockup_scene_a_sma.png. Không có screenshot trong gói. Khi lấy được, chỉ dùng đối chiếu composition/layout; không crop nguyên screenshot/card/text thành asset production.
3. **Production candidate:** cozy_corner_candidate_v1.png được tạo mới bằng built-in ImageGen từ mascot gốc; không phải ảnh đã duyệt. Một scene phẳng gồm mèo+cốc+bàn, không có UI/text. Có thể animate toàn scene bằng transform; không animate riêng tay, cốc, mắt từ file này.

Đã xem candidate: mèo trắng kem, mắt đen tròn, tai/mũi hồng, ria đen, cốc tím tại bàn; không còn gối/exclamation. Có quầng sáng và bóng rộng ngoài silhouette; cần compositing QA trên cream/white/pastel trước khi dùng release. Không tuyên bố màu cốc khớp chính xác HEX chỉ từ prompt. Kích thước/mode/alpha được ghi trong assets/inspection.json; Candidate có alpha range 0–254, không có pixel hoàn toàn opaque; cần kiểm tra độ trong của chủ thể và quầng trước release. Có alpha không đồng nghĩa viền đã sạch. Không tự tẩy quầng bằng threshold vì có thể phá lông trắng.

## Inventory

| Tên dự kiến Android | Loại / đích dự kiến | Tình trạng | Spec và QA |
|---|---|---|---|
| chanko_profile_cozy_corner | Alpha PNG hoặc lossless WebP, drawable-nodpi | Candidate PNG có trong gói; chưa có bản release WebP | Source landscape; fit toàn scene, không crop đầu/tai/bàn; transparent padding; test cream #FAF8F5 |
| mascot_crossed_arms | Reference source, không import tự động vào app | Có, giữ nguyên | Không thay hero Profile bằng tư thế này |
| chanko_profile_cozy_corner_static | Static fallback | Dùng cùng scene sau khi asset đạt QA | Reduce motion / decode error không cần sprite |
| chanko_profile_cozy_corner_reaction | Motion | Chỉ có spec whole-scene transform; không có Lottie/Rive/sprite | Không khai báo asset animation giả; không cần real-time 3D |
| ic_arrow_back / ic_chevron_right | Material Symbols Rounded vector | Dùng thư viện/icon pipeline dự án; chưa export | 24dp glyph, ≥48dp touch; không trộn bộ icon |
| ic_personality / ic_notifications / ic_privacy / ic_info / ic_logout | Rounded vector | Glyph đề xuất, cần chọn trong cùng bộ | Reuse existing resource nếu có; contrast/focus QA |
| google_account_avatar | Remote account data | Không tạo ảnh mẫu | Fallback neutral; không đổi avatar qua Profile |
| nunito_regular / semibold / bold / extra_bold | res/font | Chưa có font binary trong gói | Reuse font có license dự án; kiểm tra Vietnamese glyph, weight mapping, license; không tải font ngẫu nhiên |
| profile_privacy_content | Legal content / verified HTTPS URL | Chưa xác minh | Không dùng lorem ipsum/URL mẫu ở release |
| profile_about_content | App metadata + text | Cần build metadata và nội dung thật | Version lấy build; không bịa company/contact |

Không đưa shadow/card/gradient/label vào raster. Cream background, corner radius, border, section heading, divider, switch, radio và button do Compose dựng. Không có icon Settings mới trên Home. Không có bottom-nav asset riêng cho Profile.

## Resource inventory cho coding agent — không phải file code đã triển khai

**Colors/theme:** background cream, brand primary purple, surface white, text primary/secondary, outline/focus, destructive, disabled, notification status; semantic on-colors phải có contrast evidence. Chỉ hai HEX trong yêu cầu hiện tại được chốt; các token khác phải lấy theme xác minh.

**Strings cần tạo/reuse (tên gợi ý):** profile_settings_title, profile_default_name, cozy_corner_title, cozy_corner_play_animation, cat_personality_title, personality_friendly, personality_sassy, personality_savage, personality_friendly_description, personality_sassy_description, personality_savage_description, personality_preview_label, personality_preview_friendly/sassy/savage, action_save, action_retry, action_back, action_keep_editing, action_discard, unsaved_changes_title/message, personality_saved, personality_save_error, personality_save_unconfirmed, personality_conflict, online_required, offline_cached_banner, google_account_title, ready_for_review_title/description, notification_blocked_message, action_open_system_settings, privacy_title, about_title, sign_out_title/confirmation/action, action_cancel, loading_label, session_expired_message, profile_load_error.

Tên là Reference; không hardcode string trong Composable. English copy trong handoff là đề xuất; strings tiếng Việt cần thống nhất ngôn ngữ sản phẩm, không trộn hai ngôn ngữ trên cùng màn. ContentDescription không lặp label đã được semantics đọc.

**Dimens/motion:** outer padding, section gap, row min height, card radius/padding, touch target, hero aspect ratio, primary min height, animation duration/scale; dùng token chung và trạng thái reduce motion. **Fonts:** quản lý đầy đủ weight của Nunito, không để Material mặc định Roboto. **Navigation:** internal route identifiers, không cần exported deep link cho Profile. **Notification:** channel ID, permission strings và scheduler thuộc dependency hệ thống, chưa được chốt tại handoff.

## Brief làm asset release

Input identity bắt buộc: assets/reference/mascot_crossed_arms.png. Input composition bắt buộc để visual-match: mockup gần nhất, hiện thiếu. Giữ đầu/tai/mắt/mũi/ria/màu lông/tỷ lệ như reference; pose ngồi, hai chân trước ôm cốc tím tại bàn; không nằm gối. Ánh sáng mềm cùng hướng; không thêm décor hoặc vật thể có chức năng. Không text/logo/watermark/UI. Alpha sạch, không nền baked-in/viền trắng/quầng đen. Đề xuất master rộng 1536px trở lên; xuất runtime theo kích thước hiển thị đã đo (không phóng vượt nguồn), benchmark memory/decode trên thiết bị mục tiêu. Budget byte chưa chốt, cần đo trước release.

Nếu cần animation riêng bộ phận, phải sản xuất thật các layer từ cùng scene/camera: cat body/head/eyes/paws, mug, table, shadow với common canvas/anchor, occlusion chuẩn; không tự cắt tay/cốc từ screenshot thiếu phần bị che. Với scope tap hiện tại, một scene tĩnh + transform là đủ. Không thêm chi phí rig chỉ vì có khả năng.

## Prompt đã dùng cho candidate

Built-in ImageGen; reference image là mascot_crossed_arms.png; transparent_background=true.

“Use case: identity-preserve. Production app illustration, not UI mockup. Use the supplied image as the exact white cat identity reference: same plush cream-white fur, round dark glossy eyes, pink inner ears and nose, black paired cheek whiskers, rounded proportions. Change pose to cat seated behind a small rounded cream table, both paws hugging a purple #8965F6 mug on the table. Cozy Corner, Candy Pop 3D soft rounded clay/plush style. Transparent background genuine alpha, no room, no card, no text, no UI, no floor rectangle, no extra characters, no cushion, no yellow exclamation. Full scene uncut with generous transparent padding. Soft warm light and gentle contact shadow limited to objects. Landscape composition. Single flattened illustration intended for whole-scene subtle tap animation; do not fake separate layers.”

Kết quả vẫn cần QA quầng/alpha và đối chiếu mockup; prompt không bảo đảm tự động đạt spec. Chưa tạo font/icon binaries hay animation layers; inventory phân biệt rõ available/candidate/pending.
