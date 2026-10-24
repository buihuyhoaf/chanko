> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Previews / tests — kế hoạch xác minh

Chưa chạy build, Compose Preview, unit test hoặc runtime test của app trong nhiệm vụ bàn giao tài liệu. Những tên bên dưới là mục tiêu đề xuất để agent triển khai, không phải file/test class được xác nhận có sẵn.

## Component previews

| Component | Tối thiểu phải thấy |
|---|---|
| Header | tên ngắn/dài, thiếu avatar, font 2.0 |
| LivingRoom | đủ 4 prop, unknown badge, asset missing, reduced motion, 320dp |
| Dialogue (bên trong scene) | absent, ngắn/dài, loading, CTA nếu supported |
| QuickActions | hai entry, xếp ngang/dọc, unavailable/busy |
| Budget | bình thường 80%, 120%, zero, unknown, tiền dài, error/retry |
| Decisions | 0/1/3 item, >3 input, cả bốn status, missing image, waiting expired |
| Observation/Draft | cả bốn tổ hợp presence; text dài và stale draft |
| Nav | mỗi tab selected, center +, label font lớn, insets |
| Sheets | Global và từng context đã chốt; empty/loading/error; IME, long content |
| Full Home | happy/loading/empty/partial-error/stale, scrolling tới cuối, modal mở |

Matrix chính: width 320/393/600dp × fontScale 1.0/1.5/2.0; chọn preview đại diện cho từng component và full-screen edge cases, không tạo test trùng chỉ để tăng số lượng. Thêm landscape/insets lớn theo device support TECH; không tự định nghĩa tablet layout mới.

## Unit / presenter tests có giá trị

- Stable rank toàn bộ candidate set: S01–S04/S20 đúng IDs và ≤3. Kiểm tra permutation nhóm status, ổn định trong nhóm theo upstream, duplicate/unknown sau khi TECH chốt.
- Money formatter, ratio 0/null/over-limit, remaining âm; không precision loss hoặc divide-by-zero. Test cycle/date biên tháng/năm/zone nếu Calendar được triển khai.
- Countdown không âm trong display và không đổi domain state; inject clock, không dùng thời gian máy test.
- Optional null không tạo section/spacer; retry scope chỉ refresh section liên quan.
- Reducer/effect: Add mở Global; repeated Add một sheet; context ID đúng; back/dismiss không commit; context bị xóa không thao tác lên ID sai.

## Compose UI / navigation checks

- `onNode` theo semantics/label ổn định thay vì tọa độ pixel: click Quick Check/Save Item phát hai intent riêng.
- Có bốn tab selectable, + có role button và không selectable như tab; mở sheet không đổi selected Home.
- Decision row/card và CTA không phát trùng navigation khi một chạm; keys giữ identity sau refresh.
- Sheet dismiss/back/focus return hoạt động; form có IME không che CTA và không tự submit.
- Scroll content cuối không bị bar che, không có nested scroll khóa màn; font lớn không mất action/tiền.

## Visual / accessibility / runtime

Screenshot dùng fixed clock, fixture và font bundled. Review bằng spec mới, không pixel-match layout calendar legacy. Design review so scene composition với ảnh chuẩn mới; responsive reflow phải giữ visual hierarchy. Kiểm tra TalkBack focus, labels của bốn prop, selected tab, count/status, price; icon trang trí không đọc lặp; contrast trên nền/gradient thật; hit target không giao nhau.

Đo decode/memory asset, jank khi scroll, recomposition/timer trên thiết bị thật phù hợp nếu có; không đặt ngưỡng performance giả rồi báo pass. Thống nhất benchmark với TECH. Kiểm tra lifecycle stop/resume, mất mạng, refresh, process recreation, double tap và entity removed khi sheet mở.

Không tạo/dùng emulator, AVD hay VM. Không thêm launcher riêng. Nếu không có thiết bị thật, ghi “runtime unverified” cùng phạm vi static/unit đã kiểm tra. Không thay bằng emulator hoặc retry install đã bị từ chối.

## Mẫu QA evidence

Mỗi check ghi: ID acceptance/scenario, commit/build thực, thiết bị/API hoặc preview config, locale/zone/font scale, kết quả pass/fail/not-run, screenshot/log đã được lưu ở vị trí thực, limitation và owner. Test plan không được gọi là test result.

## Visual regression bắt buộc v2

Review S21 Home closed và S22 Global sheet open cạnh ảnh chuẩn mới. Kiểm tra Header có subtitle; scene cat pose/đệm tím và bốn prop theo vị trí; dialogue trên-trái trong scene; Quick Check tím/Save sáng; Budget detail pill + warning badge; decisions status bên phải; Observation đủ X + Explain/Hide; nav bốn tab/+; Global scrim/handle/title/X và hai tiles. Không so toàn composite thành một screenshot runtime duy nhất.

Thêm semantics/layout check: HomeContent không render Dialogue thành row sau scene; bubble không che face/hit targets; Calendar label dynamic không bake OCT/24; Observation Explain/Hide/Dismiss phát đúng intent riêng; font lớn không làm hai sheet tiles mất nội dung.
