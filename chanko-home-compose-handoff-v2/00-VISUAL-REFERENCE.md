> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Ảnh chuẩn và mapping visual — v2

![Home Chanko được người dùng chốt](references/approved-home-user-reference.png)

Ảnh này được người dùng gửi trực tiếp ngày 24/10/2026 và yêu cầu “làm đúng theo ảnh kia”. Đây là chuẩn hình thức/layout. Ảnh raster không cung cấp dp/sp, layer, font metadata hoặc resource rời; các thông số kỹ thuật phải đo/chốt qua Compose previews, không tuyên bố là số đo thiết kế gốc.

## Bố cục phải tái hiện

| Vùng | Hình thức theo ảnh | Compose ownership |
|---|---|---|
| Header | “Chào buổi sáng, Hoa” + biểu tượng mặt trời; dòng phụ màu muted với tim tím; gear trong vòng lavender và avatar tím bên phải | HomeHeader gồm GreetingRow, Subtitle, SettingsAction, AvatarAction |
| Living Room | Một khung bo tròn ngang; phòng pastel ấm, kệ sách/cây trái, cửa sổ vòm/cây phải; không là lịch lớn | CompactLivingRoom chứa SceneArtwork và interactive overlay |
| Mèo | Mèo trắng 3D nằm chống cằm trên đệm lavender, mắt mở/nháy, vị trí giữa-phải | ChankoCatArtwork; không dùng mèo khoanh tay legacy làm final |
| Hourglass | Đồng hồ cát vàng trên chồng sách, phía dưới-trái | RoomProp(Hourglass) |
| Calendar | Lịch bàn tím bên phải đồng hồ cát, phía dưới-trái | RoomProp(Calendar), ngày/tháng dynamic |
| Wallet | Ví xanh mint giữa Calendar và mèo | RoomProp(Wallet) |
| Pause Jar | Hũ thủy tinh nắp hồng, tim hồng bên trong, phía phải mèo | RoomProp(PauseJar); không tự đổi thành hũ tiền |
| Dialogue | Bubble cream góc trên-trái trong khung scene, đuôi hướng về mèo; hai đoạn với trọng lượng chữ khác nhau | ChankoDialogue overlay là con của CompactLivingRoom |
| Quick actions | Hai thẻ cạnh nhau; Quick Check tím, icon search trong vòng trắng; Save an Item nền sáng, + trong vòng tím; subtitle và chevron | QuickActionsRow |
| Budget | Card sáng; title chu kỳ trái, pill “Chi tiết” phải; spent/limit trái, remaining phải, badge cam; progress dưới và % ở cuối | BudgetCard |
| Decisions heading | Icon checklist tím, “Những món cần chú ý”, count chip, “Xem tất cả” bên phải | YourDecisionsSection header |
| Decision rows | Thumbnail trái, tên/giá giữa, status pill và countdown ở vùng phải, chevron ngoài cùng; ready mint, waiting lavender sáng | DecisionRow; tối đa 3 |
| Observation | Icon bóng đèn trong tile lavender; title + close; nhận xét bold + evidence muted; hai pill “Giải thích thêm”, “Ẩn gợi ý này” | ObservationCard, action theo capability |
| Nav | Thanh trắng bo tròn; Trang chủ, Món đồ, + tím lớn, Ngân sách, Phân tích; selected Home tile lavender | ChankoBottomNavigation |
| Global sheet | Scrim, top rounded sheet, drag handle, title “Bạn muốn làm gì?”, close phải; hai action tile Quick Check/Save an Item | GlobalQuickActionSheet |

## Điều cần hiểu đúng khi implement

- Ảnh trình bày cả Home và sheet như một composite để minh họa hai trạng thái. App render sheet bằng modal overlay trên Home với scrim; không nối screenshot sheet thành nội dung dưới nav hoặc làm trang dài thứ hai.
- Bubble không là section thứ ba sau scene. Thứ tự content: Header → Living Room chứa Dialogue → QuickActions → Budget → Decisions → optional Observation/Draft.
- Status/timer/money/count/tiêu đề/ngày tháng là dữ liệu động; con số trong ảnh là fixture. `24 OCT`, budget tháng 10–11/2026 và ba món không được hardcode production.
- Giữ wording tiếng Anh “Quick Check”, “Save an Item” và các nhãn Việt như ảnh làm baseline copy. Localization ngoài baseline chờ xác nhận; không tự đổi nhãn visual thành “Kiểm tra nhanh” trong bản bám ảnh.
- Không gắn hành động mua hàng lên biểu cảm mèo hoặc màu ready. Priority/rank và max 3 giữ đúng request trước dù ảnh chỉ minh họa Ready/Waiting.
- Draft không có trong ảnh; chỉ render khi có draft thật theo quy tắc optional, không chèn card demo vào mặc định.
- Observation trong ảnh có thể hiện khi dữ liệu nguồn hỗ trợ. Không tự tính “3 trong 5” nếu chưa có observation thật. Close/Hide/Explain cần contract riêng, không phải ba nút chỉ mở cùng một flow.

## Responsive giữ thiết kế

Baseline mobile giữ tỉ lệ/thứ tự tương đối trong ảnh: gutter đều, scene ngang, action cards cạnh nhau, budget và decision rows rộng bằng content. Scene không stretch ảnh toàn màn hình. Background decorative không có semantics; text và interaction là UI thật. Tách các prop nếu cần ngày động/hit target độc lập, hoặc dùng background không chứa prop rồi overlay; không chồng asset trùng lên composite có sẵn.

Ở 320dp hoặc font 1.5–2.0, cho text/action/budget/status reflow để đọc được. Reflow được phép khi cần accessibility nhưng không chuyển dialogue xuống thành section khác, đổi pose mèo, bỏ prop hoặc đổi semantic action. Scene container có thể tăng chiều cao/chừa vùng bubble theo nội dung; tránh fixed pixel offset làm bubble che mặt mèo/prop.

Target tương tác có bounds Compose/semantics độc lập và được kiểm tra overlay bằng debug preview; không render debug bounds trong app. Không gán click theo tọa độ ảnh tùy ý. Tối thiểu 48dp khi có thể, không giao nhau. Mỗi prop chỉ mở context sau khi mapping TECH đã chốt.

## Sai khác v1 đã sửa

v1 dựa mô tả và ảnh calendar-led cũ, đặt dialogue thành section riêng và chưa có ảnh chuẩn mới. v2 đưa ảnh người dùng vào nguồn chuẩn, đặt dialogue trong scene, chốt pose mèo/đệm tím/vị trí bốn prop, Header subtitle, Budget detail pill, Observation actions và Global sheet theo ảnh. v1 bị thay thế cho triển khai visual.
