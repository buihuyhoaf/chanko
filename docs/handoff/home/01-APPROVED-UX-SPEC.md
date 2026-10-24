> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Approved UX spec — theo ảnh người dùng

Nguồn chuẩn: [ảnh Home đã chốt](references/approved-home-user-reference.png), [mapping từng vùng](00-VISUAL-REFERENCE.md). Phiên bản này thay thế bố cục v1. Ảnh v5 calendar-led trong references là nguồn lịch sử, không dùng làm chuẩn layout mới.

## 1. Header

Greeting lớn đậm bên trái, biểu tượng mặt trời cạnh lời chào; dòng phụ muted bên dưới như ảnh. Gear trong vòng lavender và avatar tròn tím phía phải. Giữ typography Nunito, chữ dark ink, tim tím trang trí. Tên/ngày/chào động từ state; mặt trời/tim không thay text quan trọng. Gear/profile có label và touch target riêng. Tên dài/font lớn được reflow, không che nút.

## 2. Compact Living Room, chứa Chanko Dialogue

Một scene phòng pastel 3D bo góc, nằm ngay dưới Header. Mèo trắng nằm chống cằm trên đệm tím giữa-phải, không phải mèo khoanh tay từ asset legacy. Hourglass vàng trên sách ở trái; Calendar bàn tím bên cạnh; Wallet mint giữa lịch và mèo; Pause Jar thủy tinh tim hồng ở phải. Kệ/cây/cửa sổ tạo background như ảnh. Không thay bằng dashboard icon/card hoặc hero lịch.

Bubble cream nằm góc trên-trái của scene, đuôi hướng mèo; text thật hai mức nhấn, baseline “Bạn có 1 món sẵn sàng xem lại đó!” / “Cùng xem thử nhé? 💜”. Đây là fixture, count và message phải khớp state; không bịa số món. Dialogue là child/overlay của Living Room, không là section dưới scene. Cho scene tăng chiều cao/chừa chỗ khi text lớn; bubble không che target/face. Message absent thì bỏ bubble và bảo toàn composition; rule message/fallback chờ TECH/product.

Scene raster chỉ là artwork, không bake text động. Calendar tháng/ngày lấy từ date state đã chốt; `OCT 24` trong ảnh là minh họa. Mapping prop đề xuất: Hourglass → waiting context, Calendar → calendar context, Wallet → budget context, Pause Jar → pause context; nghĩa nghiệp vụ và sheet final chờ TECH. Pause Jar theo visual là hũ tim, không suy diễn số tiền tiết kiệm/reward.

## 3. Quick Check + Save an Item

Hai card ngang dưới scene. Trái tím gradient, icon search trong vòng trắng, title “Quick Check”, subtitle “Hỏi Chanko trước khi mua”, chevron trắng. Phải nền sáng lavender, + trắng trong vòng tím, title “Save an Item”, subtitle “Lưu để xem lại sau”, chevron muted. Giữ đúng hierarchy/icon/card treatment trong ảnh. Font lớn/màn hẹp có thể xếp dọc.

Hai intent độc lập. Bấm không tự save hay tự đánh giá; route/flow/form và validation do TECH chốt. Loading/disabled phải có semantics, không ảnh hưởng khả năng chuyển tab ngoài capability cần thiết.

## 4. Budget

Card sáng bo tròn. Title “Ngân sách tháng 10 – 11/2026” là ví dụ cycle; “Chi tiết” pill + chevron góc phải. Hàng số: “Đã chi” và spent lớn / limit nhỏ hơn bên trái; divider; “Còn lại” tím bên phải và badge cam “Đã tiêu 80%”. Dưới cùng progress tím với nhãn 80% ngoài track như ảnh.

Tiền/cycle/% lấy từ state, không hardcode. VND fixture: limit 3.000.000 ₫, spent 2.400.000 ₫, remaining 600.000 ₫. Unknown không thành 0; limit 0/null không chia cho 0; remaining âm hiển thị đúng; visual progress clamp 0..1 nhưng nhãn thực có thể >100%. Policy badge khi unknown/negative chờ TECH; không invent text. Detail mở budget context/route theo decision log, không tự thêm calendar hero.

## 5. Your Decisions — “Những món cần chú ý”

Heading có checklist tím, count chip và “Xem tất cả”. Rows theo ảnh: thumbnail trái; title/price; status pill vùng phải; Waiting có clock + remaining dưới pill; chevron ngoài cùng. Ready dùng mint, Waiting lavender sáng. Considering/Saved chưa có style riêng trong ảnh: giữ cùng family, bổ sung nhãn chữ rõ theo product, không tự gọi style mở rộng là đã có trong ảnh.

Tối đa 3 item, ưu tiên Ready → Waiting → Considering → Saved. Sau canonicalization theo TECH, stable sort priority rồi take(3), giữ upstream order trong nhóm. Input phải đảm bảo candidate toàn cục; không rank trên trang tùy ý có thể bỏ sót Ready. Count chip total/visible semantics chờ TECH, nhưng không ghi 3 nếu thực tế rỗng/lỗi. Row/tap CTA không double-navigate. Ready không là lời khuyên nên mua; Waiting vẫn mở được. Countdown về 0 không tự đổi domain status hoặc tự mua.

Empty có lời trung tính và Save entry; loading/error không giả empty. Unknown price không là 0. Thumbnail fallback khi ảnh thiếu. “Xem tất cả” route/filter cần TECH chốt.

## 6. Optional Observation / Draft

Observation theo ảnh: tile bóng đèn trái, title “Góc nhìn hôm nay”, X góc phải; nhận xét bold và evidence muted; hai pill “Giải thích thêm”, “Ẩn gợi ý này”. Nội dung “Bạn thường mua các món công nghệ vào cuối tuần” / “3 trong 5…” chỉ là fixture, không được suy diễn từ dữ liệu không có. Explain, Hide và X có intent riêng, behavior/persistence chờ TECH. Không gán tất cả thành cùng một click.

Chỉ render khi có observation thật. Draft không hiện trong ảnh; chỉ thêm nếu có draft thật, nằm sau Observation theo hợp đồng optional, với resume đúng ID. Không có card demo/dummy mặc định; null bỏ cả spacer. Draft selection/retention/discard chưa chốt; không thêm xóa.

## 7. Bottom navigation

Thanh trắng bo tròn, bốn tab như ảnh: Trang chủ, Món đồ, Ngân sách, Phân tích. Trang chủ selected có tile lavender và icon/label tím. + lớn tím ở giữa, cân đối hai cặp tab, là action không phải tab. Mở + giữ selected tab hiện tại và mở Global Quick Action Bottom Sheet. Insets chừa nội dung cuối, bar cố định ngoài scroll.

## 8. Global Quick Action Bottom Sheet

Ảnh dưới minh họa sheet ở trạng thái mở: scrim, sheet bo góc trên, drag handle, title “Bạn muốn làm gì?”, X phải; hai tile ngang. Quick Check: icon bubble/question tím, title, mô tả “Cùng Chanko cân nhắc trước khi quyết định mua.” và chevron. Save an Item: icon bookmark/add hồng, title, mô tả “Lưu một món đồ để xem lại sau.” và chevron. Không dùng icon card Home thay cho icon sheet nếu muốn bám ảnh.

App dùng modal overlay, không đặt sheet thành block tiếp theo dưới nav như composite ảnh. Một active sheet; repeated + không stack. Back/X/gesture đóng sheet, trả focus về +; dismiss không ghi dữ liệu. Chọn tile đi flow tương ứng theo TECH, không mở hai modal chồng. Long content/font lớn/IME reflow và scroll để CTA đọc được.

## 9. Contextual sheets

Global sheet trong ảnh đã có chuẩn visual. Ảnh không cho biết nội dung chi tiết của Hourglass/Calendar/Wallet/Pause Jar/Decision sheets; cần TECH/product bổ sung, không invent field, CTA hoặc layout rồi gọi là approved. Context được typed và truyền ID đúng. Entity bị xóa khi mở sheet phải chặn mutation, báo không còn khả dụng và cho đóng.

## Responsive / state / a11y

Candy Pop/Nunito theo ảnh, không đổi visual language. Thông số dp/sp/HEX từ v5 cũ là khởi điểm đề xuất, không là số trích từ ảnh mới. Đo preview 320/393/600dp × scale 1/1.5/2; không cắt tiền/action. Header/actions/budget/rows reflow khi cần, dialogue vẫn bên trong scene. Hit target ≥48dp khi có thể, TalkBack order từ header tới scene/props/dialogue theo nghĩa rồi actions; decorative artwork không đọc lặp. Giữ text labels và selected semantics.

Loading theo section; error retry cục bộ; cache/stale/absence trung thực theo data contracts. Reduced motion dùng artwork tĩnh. Không network hoặc gọi AI từ composable. Không dùng ảnh toàn màn làm UI, không cắt text/icon/button từ PNG để giả component.
