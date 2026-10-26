# Visual references — dùng đúng lớp
- `approved_original_3_panel.png`: bytes mockup user đính kèm ngày25/10, giữ nguyên để truy vết. Verdict BUY xanh/sparkles trong bản này KHÔNG là chỉ dẫn triển khai.
- `corrected_neutral_3_panel.png`: bản hiệu chỉnh bằng built-in imagegen, giữ bố cục3 panel, BUY lavender/mascot crossed-arms neutral, bỏ sparkle và green success trong verdict. Bản hiệu chỉnh không được user approve riêng sau khi tạo. Imagegen có thể làm khác một số nét/spacing; không lấy hình mascot được render lại trong ảnh UI làm canonical production.
- `CL_chat_list.png`, `CD_chat_detail.png`, `NC_new_chat.png`: ba reference Android390×844 logical px, export3x1170×2532; dùng Nunito thật, palette Approved, components/text thật. HTML đi kèm để inspect layout, không Android code, không production interaction demo.
- `android_three_screen_overview.png`: overview3 reference Android. Không có màn chính thứ4.

Nguồn ưu tiên: spec + canonical mascot và tokens cho implementation; ảnh hiệu chỉnh giúp giữ cảm giác của mockup gốc. Reference Android làm rõ accessibility/context selection, dummy product media, neutral Decision Card; không claim pixel parity với iOS chrome hoặc hình generated. Ảnh gốc có brand products và ví dụ lý do/budget, không chứng nhận thông số/giá/khả năng chi trả.

Các khác biệt có chủ ý được ghi đầy đủ trong CHAT-UX-REV-01. Checkmarks trong bản imagegen được đổi tím; reference Android dùng bullets trung tính. Ảnh gốc và bản hiệu chỉnh vẫn có sparkles ở hero CL như source; không có celebration ở verdict CD. Production asset mapping không dùng sparkles.

HTML là visual-only: nút/search không wire backend, lựa chọn đã seed để minh họa; không dùng để xác minh navigation/runtime. Bản font200 chỉ QA wrap/scroll, không mô phỏng đầy đủ TalkBack hoặc Android font metrics. CD footer ở trên IME theo spec; native insets cần kiểm implementation.
