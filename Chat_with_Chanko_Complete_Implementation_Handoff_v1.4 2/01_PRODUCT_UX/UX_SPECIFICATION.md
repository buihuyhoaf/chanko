> v1.4 authority: [official visual baseline](../07_VISUAL_REFERENCES/VISUAL_BASELINE_APPROVAL.json), [REC rules có nguồn](RECOVERED_RULES_SYNTHESIS.md). Approved business rules giữ nguyên. A1/A3/A5 approved, A2/A4 approved with amendments theo14/USER_TECHNICAL_APPROVAL_V14; approved addendum13/08 thắng defaults cũ, không đổi source SPEC/TECH. Runtime chưa chạy không làm thiếu tài liệu.

# UX Specification

## Mục tiêu và phạm vi
U/B: Chat hỗ trợ suy ngẫm trước mua, không discovery, affiliate, chào bán hay mua tự động. Hai ngữ cảnh: Free Chat và Chat gắn Shopping Item. History, Item Picker, Thread Options, Delete Confirmation và Archived là các bề mặt cần bàn giao; không đổi bottom navigation Home / My Items / Budget / Insights.

U v1.3:11 core visual references trong v1.2 là baseline visual chính thức thay v0.2, đã có approval/hash registry tại07. Không tìm nguồn v0.2 nữa. B: Design System v1.0 giữ Nunito/Material3/cream/purple/rounded surfaces/a11y. Asset và accessible presentation v1.2 đã cung cấp; native dp/sp/theme mapping được kiểm tra khi implementation, không sửa SPEC/TECH source.

## Cấu trúc màn chat (P về bố cục chi tiết)
1. App bar: Back, “Chat with Chanko”, lối History, Thread Options khi đã có thread.
2. Nhãn “Bản mẫu · Phản hồi được dựng sẵn” luôn thấy trong fake mode.
3. Context panel: “Trò chuyện tự do” hoặc item chip với tên/status/giá dự kiến đọc từ item repository. Nhấn chip mở Item Detail; không biến giá dự kiến thành chi tiêu.
4. Timeline: message theo thứ tự ổn định; user/Chanko phân biệt bằng label và semantics, không chỉ màu. Thời gian và thông báo lỗi là text thật.
5. Suggested replies nếu fixture/server có; tap chỉ điền hoặc gửi theo hành vi đã xác minh; mặc định P: điền composer, user nhấn gửi.
6. Composer multiline + Send; keyboard/insets không che message và CTA. Whitespace-only không gửi, double tap chỉ một operation.
7. Verdict card chỉ khi kết quả có cấu trúc hoàn chỉnh và hợp lệ cho item; không parse từ chữ “buy” trong prose.

## Free Chat
Không có Shopping Item thì không tạo official decision/evaluation giả. Không tạo item ngầm từ lời chat. Có lối mở Item Picker (P: “Chọn món đồ để cân nhắc”). Khi chọn existing item, user phải xác nhận chuyển; server bind FREE→ITEM trên cùng threadID sau version/owner/capability checks. Giữ nguyên mọi message ID/text/sequence/timestamp và session draft; không clone/reuse thread khác hoặc tự tạo item. Không tự biến lịch sử FREE thành official item Decision/Evaluation/Purchase records. Cancel/no ack giữ FREE cho tới reconcile; chi tiết approved13/08.
Free Chat mock trả câu dựng sẵn liên quan chủ đề, không nói đã đọc budget/history thật. Nhãn ví dụ: “Đây là phản hồi mẫu để kiểm tra giao diện, chưa kết nối AI.”

## Item Chat và verdict
B: Quick Check thường 1–3 câu, có thể verdict sớm; re-evaluation giữ history, Still Want It có thể 1–2 câu và được bỏ qua. Fixture thể hiện flow nhưng không tính verdict bằng giả rule giá.
Card: label “Nhận định của Chanko”, BUY / WAIT / SKIP, rationale, thời điểm đánh giá, item context/version khi có, bước tiếp theo rõ. BUY: “Bạn có thể cân nhắc mua” (P copy), neutral mascot, không confetti, âm thanh chiến thắng, “chúc mừng”, sale hoặc nút mua nổi bật.
B/U: card không tự set PURCHASED/WAITING/SKIPPED, không tạo Purchase/Decision Record và không trừ budget. Lối “Xem món đồ” đi Item Detail; ghi nhận mua hoặc bắt đầu chờ dùng flow nghiệp vụ hiện có, với xác nhận riêng. Không tạo bản sao purchase sheet trong Chat.
Latest verdict và các verdict cũ không bị viết lại khi personality đổi hoặc user làm trái advice. Nếu item version thay đổi, card cũ là lịch sử; không trình bày như lời khuyên vừa đánh giá từ dữ liệu mới.

## History, quản lý thread
P: History là danh sách thread, title/context, last updated, preview tối thiểu và Archived entry. Empty khác với chưa tải/no cache. Cursor/count do repository cung cấp, không lấy page length làm total.
U: Archived là state cần hỗ trợ; A3 đã approve lifecycle contract13/04; archived thread read-only, implementation/server availability được kiểm sau, Restore rõ ràng, giữ message/context/history. Không đánh đồng archived thread và archived Shopping Item.
A3: menu chỉ Archive, Delete, xem item khi có và capability cho phép. Không tự thêm Rename/Pin/Export khi chưa có quyết định nguồn.
Delete Confirmation: “Xóa cuộc trò chuyện này? Lịch sử quyết định chính thức của món đồ và lịch sử mua vẫn được giữ.” Buttons “Giữ lại” / “Xóa chat”. Chỉ success sau repository ack; demo ack chỉ thay fixture store, ghi rõ chưa đồng bộ server.

## Offline, loading, error
U/B: offline chỉ xem cache, có “Đang xem dữ liệu đã lưu trên thiết bị” + thời điểm cache nếu biết. Send/evaluate/delete/archive/restore và fetch picker cần mạng bị disable kèm lý do. Draft composer đang có có thể giữ trong view state; không hứa raw chat draft durable sau process death theo approved session-draft contract A4/A5. Không tạo offline message hoặc auto-send reconnect.
Loading phân biệt initial load, refresh, send pending và response partial. Giữ nội dung đọc được khi refresh lỗi; không kéo dài loading giả để giống AI.
Timeout sau send là UnknownOutcome; giữ operation key, message pending/uncertain và cho reconcile/retry cùng intent. Chỉ dữ liệu hoàn tất hợp lệ mới tạo verdict card. Copy “Chưa xác nhận được kết quả” thay vì khẳng định chắc chắn thất bại.

## Mascot và accessibility
Chỉ Chanko canonical, cùng hình dáng/material/lighting; personality chỉ đổi giọng, không đổi verdict. Thiếu asset dùng text/slot không ảnh, báo dependency, không dùng mèo khác. Mascot không trong/cạnh product media. Emotion whitelist, unsupported → canonical neutral tĩnh nếu có. Option B tap dùng predefined reaction, không gọi LLM mỗi tap.
Touch ≥48dp; font scale 200%, text wrap, không cắt tiền/lỗi; TalkBack đọc sender/text/action; không đọc decoration; focus trả về trigger khi đóng sheet. Reduced motion dùng static, không bounce; không autoplay sound. Announce response theo đoạn hoàn tất, không mỗi token. Khi user đang đọc phía trên, P: giữ scroll, hiện “Tin nhắn mới”, không giật về cuối.

## Hiệu chỉnh v0.3 có nguồn (R03/R06/R07)
User được yêu cầu verdict sớm trong Quick Check theo SPEC-01; không bắt đủ3 câu. Tên/placement control là P cho đến khi khôi phục body quyết định. Intent RequestEarlyVerdict đi repository/orchestrator, online/item/owner/capability hợp lệ; Free Chat không tạo dummy item để evaluate.

Server orchestration được lưu messages và Evaluation Record hợp lệ, mỗi lần đánh giá lại tạo bản ghi mới. Render/tap card không tạo thêm evaluation. “Không mutation” chỉ ranh giới không tự đổi item status, official user decision, purchase, budget hoặc cooling-off do AI advice/prose; không cấm persistence evaluation theo TECH-02 §4.6 và TECH-03 §5.

A1/A3 contract Delete/Archive/Restore đã approved; source approval không phải deployed functionality. Invariant giữ official history hiện được cụ thể hóa bằng retention/FK shell/independent history readers ở13/02; runtime production behavior phải được kiểm sau implementation. Demo giữ nhãn fixture, không báo xóa cloud. Không tự chọn hard/soft delete/tombstone/FK action. Multi-device refetch và stale checks không đồng nghĩa đã có realtime sync; xem ../12_DEPENDENCIES_AND_OPEN_QUESTIONS/CONTRACT_REVIEW.md.

## Approved cache retention amendment A4
Freshness24h chỉ mark stale và trigger online revalidation. Dữ liệu retained trong cache đúng account vẫn đọc offline sau24h/48h hoặc lâu hơn, không TTL khóa đọc/xóa tự động. Known delete/owner invalidation, logout/clear hoặc actual capacity eviction xử lý riêng; không no-cache chỉ vì stale. Offline luôn no writes/AI/bind/outbox; reconnect current-context/lifecycle validation trước actions.
