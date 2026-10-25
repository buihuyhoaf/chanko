> HISTORICAL v1.2 derived UX copy. Current Product UX and official visual authority are in01/07; source SPEC/TECH snapshots remain immutable.

# UX Specification

## Mục tiêu và phạm vi
U/B: Chat hỗ trợ suy ngẫm trước mua, không discovery, affiliate, chào bán hay mua tự động. Hai ngữ cảnh: Free Chat và Chat gắn Shopping Item. History, Item Picker, Thread Options, Delete Confirmation và Archived là các bề mặt cần bàn giao; không đổi bottom navigation Home / My Items / Budget / Insights.

U: Candy Pop 3D v0.2 đã duyệt. B: Design System v1.0 cung cấp Nunito, Material 3, rounded surfaces, cream/purple/pastel, controls 2D và a11y. Chưa có ảnh/tokens v0.2 để đối chiếu chi tiết, không tự tạo palette hoặc kích thước mới rồi gọi Approved. Dùng theme registry của dự án; xung đột token ghi Q, không sửa baseline.

## Cấu trúc màn chat (P về bố cục chi tiết)
1. App bar: Back, “Chat with Chanko”, lối History, Thread Options khi đã có thread.
2. Nhãn “Bản mẫu · Phản hồi được dựng sẵn” luôn thấy trong fake mode.
3. Context panel: “Trò chuyện tự do” hoặc item chip với tên/status/giá dự kiến đọc từ item repository. Nhấn chip mở Item Detail; không biến giá dự kiến thành chi tiêu.
4. Timeline: message theo thứ tự ổn định; user/Chanko phân biệt bằng label và semantics, không chỉ màu. Thời gian và thông báo lỗi là text thật.
5. Suggested replies nếu fixture/server có; tap chỉ điền hoặc gửi theo hành vi đã xác minh; mặc định P: điền composer, user nhấn gửi.
6. Composer multiline + Send; keyboard/insets không che message và CTA. Whitespace-only không gửi, double tap chỉ một operation.
7. Verdict card chỉ khi kết quả có cấu trúc hoàn chỉnh và hợp lệ cho item; không parse từ chữ “buy” trong prose.

## Free Chat
Không có Shopping Item thì không tạo official decision/evaluation giả. Không tạo item ngầm từ lời chat. Có lối mở Item Picker (P: “Chọn món đồ để cân nhắc”). Khi chọn item, P: mở thread item riêng, giữ Free Chat cũ; không di chuyển message hoặc gán retroactive history. Q: mapping thread/reuse/new từ Approved body cần khôi phục.
Free Chat mock trả câu dựng sẵn liên quan chủ đề, không nói đã đọc budget/history thật. Nhãn ví dụ: “Đây là phản hồi mẫu để kiểm tra giao diện, chưa kết nối AI.”

## Item Chat và verdict
B: Quick Check thường 1–3 câu, có thể verdict sớm; re-evaluation giữ history, Still Want It có thể 1–2 câu và được bỏ qua. Fixture thể hiện flow nhưng không tính verdict bằng giả rule giá.
Card: label “Nhận định của Chanko”, BUY / WAIT / SKIP, rationale, thời điểm đánh giá, item context/version khi có, bước tiếp theo rõ. BUY: “Bạn có thể cân nhắc mua” (P copy), neutral mascot, không confetti, âm thanh chiến thắng, “chúc mừng”, sale hoặc nút mua nổi bật.
B/U: card không tự set PURCHASED/WAITING/SKIPPED, không tạo Purchase/Decision Record và không trừ budget. Lối “Xem món đồ” đi Item Detail; ghi nhận mua hoặc bắt đầu chờ dùng flow nghiệp vụ hiện có, với xác nhận riêng. Không tạo bản sao purchase sheet trong Chat.
Latest verdict và các verdict cũ không bị viết lại khi personality đổi hoặc user làm trái advice. Nếu item version thay đổi, card cũ là lịch sử; không trình bày như lời khuyên vừa đánh giá từ dữ liệu mới.

## History, quản lý thread
P: History là danh sách thread, title/context, last updated, preview tối thiểu và Archived entry. Empty khác với chưa tải/no cache. Cursor/count do repository cung cấp, không lấy page length làm total.
U: Archived là state cần hỗ trợ; Q: chat archive/restore chưa có API/chính sách. P cho demo: archived thread read-only, Restore rõ ràng, giữ message/context/history. Không đánh đồng archived thread và archived Shopping Item.
P: menu chỉ Archive, Delete, xem item khi có. Không tự thêm Rename/Pin/Export khi chưa có quyết định nguồn.
Delete Confirmation: “Xóa cuộc trò chuyện này? Lịch sử quyết định chính thức của món đồ và lịch sử mua vẫn được giữ.” Buttons “Giữ lại” / “Xóa chat”. Chỉ success sau repository ack; demo ack chỉ thay fixture store, ghi rõ chưa đồng bộ server.

## Offline, loading, error
U/B: offline chỉ xem cache, có “Đang xem dữ liệu đã lưu trên thiết bị” + thời điểm cache nếu biết. Send/evaluate/delete/archive/restore và fetch picker cần mạng bị disable kèm lý do. Draft composer đang có có thể giữ trong view state; không hứa lưu bền vững chat draft khi chưa có retention contract. Không tạo offline message hoặc auto-send reconnect.
Loading phân biệt initial load, refresh, send pending và response partial. Giữ nội dung đọc được khi refresh lỗi; không kéo dài loading giả để giống AI.
Timeout sau send là UnknownOutcome; giữ operation key, message pending/uncertain và cho reconcile/retry cùng intent. Chỉ dữ liệu hoàn tất hợp lệ mới tạo verdict card. Copy “Chưa xác nhận được kết quả” thay vì khẳng định chắc chắn thất bại.

## Mascot và accessibility
Chỉ Chanko canonical, cùng hình dáng/material/lighting; personality chỉ đổi giọng, không đổi verdict. Thiếu asset dùng text/slot không ảnh, báo dependency, không dùng mèo khác. Mascot không trong/cạnh product media. Emotion whitelist, unsupported → canonical neutral tĩnh nếu có. Option B tap dùng predefined reaction, không gọi LLM mỗi tap.
Touch ≥48dp; font scale 200%, text wrap, không cắt tiền/lỗi; TalkBack đọc sender/text/action; không đọc decoration; focus trả về trigger khi đóng sheet. Reduced motion dùng static, không bounce; không autoplay sound. Announce response theo đoạn hoàn tất, không mỗi token. Khi user đang đọc phía trên, P: giữ scroll, hiện “Tin nhắn mới”, không giật về cuối.

## Hiệu chỉnh v0.3 có nguồn (R03/R06/R07)
User được yêu cầu verdict sớm trong Quick Check theo SPEC-01; không bắt đủ3 câu. Tên/placement control là P cho đến khi khôi phục body quyết định. Intent RequestEarlyVerdict đi repository/orchestrator, online/item/owner/capability hợp lệ; Free Chat không tạo dummy item để evaluate.

Server orchestration được lưu messages và Evaluation Record hợp lệ, mỗi lần đánh giá lại tạo bản ghi mới. Render/tap card không tạo thêm evaluation. “Không mutation” chỉ ranh giới không tự đổi item status, official user decision, purchase, budget hoặc cooling-off do AI advice/prose; không cấm persistence evaluation theo TECH-02 §4.6 và TECH-03 §5.

Production Delete/Archive/Restore chưa được enable bởi handoff này. Invariant giữ official history vẫn áp dụng, nhưng cần contract retention/FK/independent history read trước khi bảo đảm bằng hành vi production. Demo giữ nhãn fixture, không báo xóa cloud. Không tự chọn hard/soft delete/tombstone/FK action. Multi-device refetch và stale checks không đồng nghĩa đã có realtime sync; xem ../12_DEPENDENCIES_AND_OPEN_QUESTIONS/CONTRACT_REVIEW.md.
