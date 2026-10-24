> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Acceptance criteria

Checklist mặc định chưa đánh dấu: artifact bàn giao không khẳng định implementation đã đạt. P0 là điều kiện trước sign-off production; asset/TECH unresolved phải được ghi blocker.

| ID | Priority | Điều kiện chấp nhận | Bằng chứng cần |
|---|---|---|---|
| A01 | P0 | Bám ảnh người dùng mới; Header có subtitle; không đưa lịch lớn cũ trở lại hero | Screenshot full scroll + review |
| A02 | P0 | Living Room compact có mèo trắng 3D nằm chống cằm trên đệm tím, bốn prop đúng vị trí và dialogue trong scene; không placeholder final | Design sign-off + asset manifest final |
| A03 | P0 | Dialogue text thật, nằm trong scene trên-trái, không là section riêng; absent không khoảng trống | Preview + semantics |
| A04 | P0 | Quick Check và Save Item riêng, đúng intent, không tự tạo item khi tap entry | UI/event test |
| A05 | P0 | Budget đúng tiền, null/zero/negative/over-limit không sai hoặc crash | S06–S08 + formatter tests |
| A06 | P0 | Your Decisions ≤3, Ready→Waiting→Considering→Saved, stable order và ID | S01–S04/S20 + rank tests |
| A07 | P0 | Waiting hết thời gian không tự Ready/mua; source status authoritative | S12 + integration check |
| A08 | P0 | Observation/Draft optional thật; resume đúng ID | S09–S11 + UI check |
| A09 | P0 | Bốn tab và + trung tâm; + mở Global giữ selected tab | S16 + navigation check |
| A10 | P0 | Context sheet đúng context/entity; một modal; back/dismiss không commit | S17 + transition tests |
| A11 | P0 | Loading/empty/error/stale phân biệt, retry cục bộ, unknown không thành 0 | S05/S13/S14 |
| A12 | P0 | Candy Pop/Nunito, chữ Việt đúng; asset/fonts có quyền dùng đã được xác nhận | Design + license review |
| A13 | P0 | 320/393/600dp, scale 1/1.5/2 không cắt tiền/action, bottom inset/IME đúng | Matrix screenshots |
| A14 | P0 | TalkBack/labels/selected semantics/hit targets/contrast đạt tiêu chí đã chốt | Accessibility check |
| A15 | P0 | TECH contracts blocking được chốt; data thật không fixture leakage | Decision log + integration review |
| A16 | P0 | Build/test theo repo có bằng chứng; runtime thiết bị thật hoặc explicitly unverified | QA report; unverified không được gọi runtime pass |
| A17 | P1 | Asset decode và scroll performance được đo trên device mục tiêu | Performance evidence |
| A18 | P0 | Không sửa sources synced, không giả asset path, không claim repo phase đã hoàn thành | Handoff/change review |

## Phân loại hoàn thành

- Documentation handoff complete: gói có đầy đủ spec/contracts/roadmap/prompts/fixtures/resources/QA/pending, link nội bộ và ZIP được kiểm tra.
- UI implementation ready-for-review: component đã render bằng fixtures, placeholder và pending được báo thật.
- Production accepted: A01–A16/A18 có evidence và blocker đóng; P1 có disposition. Không nhập nhằng ba trạng thái này.

## Visual acceptance bổ sung v2

| ID | Priority | Điều kiện | Evidence |
|---|---|---|---|
| V01 | P0 | Pose mèo nằm chống cằm trên đệm tím; hourglass vàng/sách trái, calendar tím, wallet mint, jar tim hồng phải; background phòng theo ảnh | Design review cạnh reference |
| V02 | P0 | Quick action cards, Budget và rows đúng hierarchy/color treatment; không tự thay bằng layout card khác | S21 screenshot |
| V03 | P0 | Observation có title/close, bulb tile, body/evidence và hai pill; Hide/Explain/X riêng theo capability | Preview + events |
| V04 | P0 | Global sheet modal overlay đúng ảnh, không là block trong Home; icon tile tương ứng | S22 screenshot + transition test |
| V05 | P0 | Copy baseline “Quick Check” / “Save an Item”, nhãn Việt còn lại như ảnh; ngày/tiền/count động | Content review + fixtures |

V01–V05 thuộc production sign-off cùng A01–A16/A18. Tài liệu và ảnh chuẩn có sẵn không đồng nghĩa đã có asset final hoặc app đạt visual.
