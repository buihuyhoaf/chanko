> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Fake-data scenarios

`fixtures/home-scenarios.json` là dữ liệu minh họa, không phải wire/API schema. Adapter preview tạo base cố định: Header greeting + subtitle như ảnh chuẩn; room dùng pose mèo nằm chống cằm trên đệm tím như ảnh (asset final thiếu, placeholder phải đánh dấu) và 4 prop label, không badge nếu unknown; dialogue nullable; Home selected; không sheet; section Content rỗng hoặc unknown rõ ràng. Merge từng scenario như override, không coi field bị thiếu là 0 hoặc absent.

Clock cố định `2026-10-24T03:00:00Z` (10:00 Việt Nam); zone `Asia/Ho_Chi_Minh`. VND minor unit có 0 chữ số thập phân. Không dùng tiền Float. Fixture có thể đổi copy bởi localization; ID và expected outcome phải giữ.

| Scenario | Mục tiêu |
|---|---|
| S01 | Happy path: dữ liệu minh họa, không là lịch sử tài khoản |
| S02 | Tất cả Ready |
| S03 | Bốn trạng thái và thứ tự trong nhóm |
| S04 | Chỉ Saved |
| S05 | Empty loaded |
| S06 | Budget vượt mức |
| S07 | Budget bằng 0 |
| S08 | Budget unknown |
| S09 | Observation + Draft |
| S10 | Observation only |
| S11 | Draft only |
| S12 | Timer hết nhưng source còn Waiting |
| S13 | Section lỗi độc lập |
| S14 | Offline snapshot |
| S15 | Tên/tiền dài, ảnh thiếu |
| S16 | Nút + và back |
| S17 | Context bị xóa |
| S18 | Duplicate/unknown status — chờ adapter TECH |
| S19 | Clock/timezone/ngày biên |
| S20 | Chỉ Considering và Saved |

S01–S04/S20 kiểm tra rank/cap; S06–S08 kiểm tra budget; S09–S11 kiểm tra optional; S12/S19 kiểm tra thời gian; S16–S17 kiểm tra sheet lifecycle. S18 là trường hợp phải resolve adapter contract, không cho test expected “first wins” tùy tiện.

Bổ sung trong preview base: loading toàn vùng dữ liệu, empty đủ section, lỗi toàn/partial, asset placeholder, thiếu avatar, reduced motion và currency không phải VND nếu TECH cho phép đa tiền tệ. Không mix currency thành tổng ngân sách nếu chưa có rule quy đổi. Tiền âm do refund và spent âm cũng cần rule TECH trước khi gán ý nghĩa.

## Baseline theo ảnh v2

S21 là fixture visual một Ready + hai Waiting, dialogue “1 món” bên trong scene, Budget 80%, Observation và không Draft. S22 mở Global modal trên S21. Dữ liệu ngày/đồng hồ trong ảnh chỉ minh họa; production không hardcode. Ảnh asset legacy và sample products không đảm bảo pixel-perfect source của screenshot. Có tổng 22 scenario.
