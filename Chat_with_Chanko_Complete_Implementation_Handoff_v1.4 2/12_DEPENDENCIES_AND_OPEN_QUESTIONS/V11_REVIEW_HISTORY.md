# Review trực tiếp — Chat with Chanko Handoff v1.1

Ngày: 25/10/2026, Asia/Ho_Chi_Minh. Review độc lập trong Work hiện tại. Không sửa ZIP, baseline, SPEC/TECH, tài liệu bàn giao hoặc asset nguồn; không Android code, không PR. Thư mục `extracted/` chỉ là bản giải nén phục vụ kiểm tra.

## Kết luận readiness

| Phạm vi | Đánh giá |
|---|---|
| Kiểm kê / toàn vẹn gói | PASS: 113 file, 13 thư mục cấp đầu, CRC ZIP tốt, 112 checksum đúng |
| Static mascot deliverables | Có đủ 8 PNG + 8 WebP; transparency thật, WebP lossless và decoded RGBA bằng PNG |
| Nhận diện mascot | Nhất quán bằng quan sát với mascot_skeptical.png; chưa phải chứng nhận từng derivative đã được user duyệt |
| UI prototype với fake data | Có đủ nền tài liệu để triển khai có điều kiện trong một nhiệm vụ được giao riêng; cần giải quyết các P1 UI dưới đây trước khi nhận visual acceptance |
| UI final / parity v0.2 / 34 decisions | CHƯA READY để chứng nhận: mockup còn lệch asset và BUY, font/contrast chưa khép, không có source v0.2 và body 34 quyết định |
| Production integration | BLOCKED: R07 cùng các Q áp dụng vẫn mở. UI đủ tài liệu không đồng nghĩa backend đủ contract |

Không có P0 được xác lập từ gói tài liệu/ảnh này. Không có ứng dụng/backend để kiểm tra lỗi production. Không đổi trạng thái 34 quyết định: **34 PARTIAL, 0 VERIFIED**; các phê duyệt được ghi nhận trước đó vẫn được tôn trọng.

## 1. Nguồn kiểm tra và toàn vẹn

Đường dẫn `/mnt/data/Chat_with_Chanko_Complete_Implementation_Handoff_v1.1.zip` không tồn tại trong phiên desktop. Đã dùng `read_thread` của chat được tham chiếu để lấy chính attachment:

- ZIP: `/var/folders/c9/hjc8lf515r9484v683sk2qwh0000gn/T/codex-file-preview-LArZQM/Chat_with_Chanko_Complete_Implementation_Handoff_v1.1.zip`.
- Canonical attachment: `/var/folders/c9/hjc8lf515r9484v683sk2qwh0000gn/T/codex-file-preview-j8dDui/mascot_skeptical.png`.
- ZIP dài 32.374.414 byte, SHA-256 `088897747e56184a69c2686b79ecd940b5487cef9ee89f97f221d03169634a91`.
- Bản ZIP trong `handoff/` có cùng kích thước và SHA-256: review đúng payload, không thay bằng gói gần giống.

| Kiểm tra thực tế | Kết quả |
|---|---|
| ZIP ngoài | 113 entries/file; CRC không lỗi |
| Folder cấp đầu | Đủ 00–12, tổng 13 |
| Checksums | 112/112 đúng; chỉ checksums.json không tự hash chính nó, hợp lý |
| Asset manifest | 21 rows: canonical + 4 decoration + 16 expression files; mọi hash đúng, mọi kích thước raster khớp |
| Screens | 11 PNG RGB, mỗi file 853×1844; đủ S01–S11; kèm 11 SVG schematic và user board |
| Markdown local links | 145 lượt target tồn tại, 0 broken theo cú pháp link/image Markdown thông thường |
| Nested previous_v0.3.zip | 86 entries, CRC không lỗi |
| Decision register / traceability | 34 rows mỗi bảng; register toàn bộ PARTIAL |

Phạm vi link check: target file tương đối trong Markdown của ZIP đã giải nén; không xác minh URL ngoài, anchor heading, đường dẫn plain text, quyền truy cập nguồn hoặc link bên trong nested ZIP. `generation_files.json` chứa đường dẫn tuyệt đối máy tạo, là provenance log chứ không phải tài nguyên portable. Các tham chiếu `evidence/live_*.txt` trong lịch sử không được cung cấp như file độc lập ở root gói; không xem là bằng chứng live đã tái kiểm trong review này. Checksum đúng chứng minh toàn vẹn nội bộ, không chứng minh tài liệu đúng hoặc source được giữ nguyên.

## 2. Canonical và 8 expressions

**Phát hiện provenance:** canonical trong ZIP không bằng attachment được duyệt ở mức byte hoặc toàn bộ RGBA.

| File | SHA-256 |
|---|---|
| mascot_skeptical.png đính kèm | `c4b0de06a1175464607e80372f9615060b89892a16b58e8e4f57087c74e947d7` |
| 08_ASSETS/canonical/chanko_user_approved.png | `4f27d394b220f9f54669f4ccec500fb6a0f397bd2b935a79670b086445b9ad6a` |

Cùng 1231×1278; alpha giống hệt. R/G/B lần lượt có 4.439/8.541/14.174 pixel khác, độ lệch tối đa mỗi channel 2/255; sai khác trung bình rất nhỏ. Không có cơ sở gọi đây là mascot khác hay suy ra nguyên nhân chuyển đổi. Tuy nhiên `canonical_bytes_preserved: true` và README “saved unchanged” không được đối chiếu attachment hiện có hỗ trợ. Cần bảo quản nguyên file attachment và phân biệt bản dẫn xuất.

| Expression | Kích thước PNG/WebP | Alpha min–max | Pixel hoàn toàn trong suốt | RGBA bằng nhau | Đánh giá hình |
|---|---:|---:|---:|---|---|
| Concerned | 1230×1278 | 0–255 | 48,78% | Có | Lông mày lo lắng, hai tay sát nhau; hợp error |
| Curious | 1231×1278 | 0–255 | 52,22% | Có | Nghiêng đầu/tay gần miệng; khá gần Thinking nhưng mắt mở hơn |
| Encouraging | 1231×1278 | 0–255 | 44,73% | Có | Mở bàn chân trấn an; không dùng cạnh BUY |
| Neutral | 1231×1278 | 0–255 | 42,30% | Có | Ngồi, chân nghỉ, mắt mở, nụ cười nhẹ; phù hợp neutral hơn pose skeptical |
| Relaxed | 1231×1278 | 0–255 | 45,61% | Có | Mắt nửa khép; có thể đọc thành hơi skeptical ở avatar nhỏ |
| Sleeping | 1231×1278 | 0–255 | 43,31% | Có | Mắt nhắm, cuộn người; hợp archived/idle |
| Thinking | 1231×1278 | 0–255 | 50,34% | Có | Tay dưới cằm, mắt nhìn lên |
| Welcome | 1231×1278 | 0–255 | 47,74% | Có | Một chân giơ chào, mỉm cười vừa phải |

Mọi WebP có VP8L lossless. Alpha thật, không có nền chữ nhật bị bake; đã xem trên nền xám và mẫu canonical/Neutral trên nền tối. Các ảnh giữ màu ivory, tai/mũi/má hồng, mắt tối bóng, hai nét râu mỗi bên, đầu lớn và đuôi ngắn mềm. Thay đổi pose/thân/mắt vẫn nằm trong cùng họ thiết kế; mức tương đồng nhận diện là nhận xét thị giác, không phải pixel parity hay user approval bổ sung.

7/8 ảnh có pixel alpha rất nhỏ chạm mép canvas; Welcome không có. Mask alpha >10 nằm trong canvas ở cả 8 ảnh: chưa có bằng chứng clipping silhouette chính. Vì vậy không được suy “bị cắt chân” chỉ từ bbox alpha >0. Có nhiễu alpha thấp cần làm sạch ở bản export runtime nếu thấy halo; giữ file gốc riêng. Concerned lệch 1 px chiều rộng đúng như manifest. Nên chuẩn hóa padding/anchor khi xuất nhiều size, kiểm lại 32/40dp để phân biệt Curious–Thinking và sắc thái Relaxed. Full character tại 32dp sẽ làm mặt nhỏ; slot cần chọn theo kiểm tra thị giác thực tế, không tự crop canonical.

## 3. Review 11 visual references và mapping đề xuất sửa

Toàn bộ mockup có nền cream, surface trắng/bo tròn, tím/lavender, vật liệu mascot mềm 3D: **đạt hướng Candy Pop ở mức phong cách tổng thể**. Không thể chứng nhận chính xác Nunito, token màu, kích thước dp hay 200% font từ ảnh raster AI. Các pose minh họa trong ảnh chưa dùng đúng asset standalone đã giao; README có cảnh báo điều này nhưng chưa giải quyết visual acceptance.

Mapping dưới đây là đề xuất review dựa trên manifest/contract hiện có, không thay baseline hay thêm domain enum. Header mặc định Neutral áp dụng cho mọi màn hình có header; không yêu cầu đổi enum 9 emotions thành 8 presentation labels.

| Screen | Quan sát thực tế | Mapping / sửa cụ thể |
|---|---|---|
| S01 Empty | Hero cười mắt nhắm, hai chân trước; khác Welcome giơ chân | Dùng chanko_welcome cho hero, Neutral header; dựng lại reference bằng đúng asset |
| S02 Free Chat | Header/avatar trông skeptical, không phải Curious | Chọn rõ Curious cho dialogue nếu theo manifest; Neutral header; chốt ưu tiên giữa default header và custom slot |
| S03 Verdict | Không sao/confetti hay nút mua trực tiếp; BUY tím rất lớn trên panel lavender, mèo nằm mặt skeptical | Thay đúng chanko_neutral, surface trắng theo DecisionCard spec; giảm framing nhấn mạnh BUY và giữ BUY/WAIT/SKIP cùng cấp độ nhấn. View item / Discuss more giữ nguyên ý nghĩa không mutation |
| S04 History | Danh sách có dữ liệu, thumbnail abstract; không có empty state | Neutral header; thêm riêng reference empty với chanko_relaxed, không chèn inset vào danh sách có data |
| S05 Picker | Sheet, radio, Select/Cancel rõ; không implicit create | Neutral header nền; không cần mascot trong sheet. Giữ lựa chọn item/context theo contract, không suy linking policy từ PNG |
| S06 Offline | Banner/cached transcript và composer disabled đúng hướng; mèo nằm mắt nhắm khác Relaxed ngồi | Dùng chanko_relaxed; thống nhất README mapping Relaxed với MISSING_ASSETS ghi Concerned/offline |
| S07 Loading | Timeline vẫn thấy, trạng thái thinking cục bộ; mèo nằm trên đệm không có trong bộ asset | Dùng chanko_thinking, bỏ đệm không được giao; ghi rõ pending/duplicate-send disabled bằng state, không suy từ màu nút |
| S08 Error | Panel Result not confirmed/Check status có; mèo skeptical tay cằm khác Concerned; send vẫn tím | Dùng chanko_concerned. Đặc tả rõ composer edit/send policy khi UnknownOutcome còn active; không để mockup khiến người implement gửi intent mới trước reconcile |
| S09 Options | View item/Archive/Delete/Cancel; không Rename/Pin/Export | Neutral header. Ẩn/disable action theo context FREE và capability; PNG ITEM không đại diện mọi context |
| S10 Delete | Text-focused; cảnh báo demo rõ; Keep chat nhấn mạnh, Delete restrained | Neutral header; câu giữ official history là invariant demo, chưa phải production guarantee trước R07 |
| S11 Archived | Transcript đọc được, composer disabled; Restore vẫn prominent; mèo ngủ nằm khác Sleeping cuộn ngồi | Dùng chanko_sleeping; Restore theo capability demo/production, không tự enable production |

**BUY neutral: PARTIAL.** Đã loại ăn mừng, màu xanh thành công, sparkle và direct purchase. Nhưng pose skeptical không phải Neutral đã giao; panel lavender và BUY quá trội so với card trắng trong spec làm mockup chưa khớp hợp đồng thị giác. Không kết luận chỉ màu tím là shopping encouragement. Sửa bằng canonical Neutral thật và hệ phân cấp đồng đều cho ba verdict, không cần xin duyệt lại UX.

## 4. P0 / P1 / P2 và đề xuất sửa

Priority là mức chặn acceptance theo phạm vi, không gán mọi thiếu sót thành lỗi production.

| ID | Mức / phạm vi | Bằng chứng và ảnh hưởng | Sửa cụ thể / điều kiện đóng |
|---|---|---|---|
| RV-01 | P1 UI visual | S03 dùng skeptical, panel lavender khác spec trắng, chưa khớp mapping Neutral | Dựng reference bằng Neutral thật, white card và kiểm BUY/WAIT/SKIP cùng treatment; review lại không reward/mutation |
| RV-02 | P1 UI handoff | Screen specs còn generic “Welcome/header” ở S03; offline Concerned/Relaxed mâu thuẫn; mockup pose không có trong assets | Tạo bảng screen/state/slot → file → size/fit/anchor/fallback; sửa nội dung active cho đồng nhất, dùng đúng 8 file trong composite tham chiếu |
| RV-03 | P1 UI a11y acceptance | Nút chữ trắng trên tím/gradient, chưa có on-color đủ contrast; token white/#8965F6 chỉ khoảng 4,00:1, dưới 4,5:1 cho text thường | Chốt on-primary/background pair, đo điểm xấu nhất của gradient; không tự đổi token Approved. LabelLarge 16sp/700 không mặc nhiên được miễn theo large-text; test scale/runtime. White/#BA85F5 ≈2,71 và white/#FFB4CF ≈1,66 nếu dùng làm nền text trắng càng không đạt |
| RV-04 | P1 UI final evidence | 34 body/option chưa truy xuất; traceability có bảng nhưng chưa chứng minh rules | Khôi phục source nguyên bản gắn ID; đối chiếu từng dòng và AC, chỉ VERIFIED khi có evidence. Không yêu cầu phê duyệt lại và không đoán nội dung |
| RV-05 | P1 production: R07 OPEN | Contract lifecycle/delete retention/FK/history/active operation chưa đủ; câu giữ history trong S10 chưa được test | Owner Backend/Data/Product chốt scope transcript/summary/evaluation, FK/tombstone, query history độc lập, outcome operation đang chạy và multi-device race; API/schema evidence và AC24/27/28 phù hợp. Không chọn hard/soft delete thay owner |
| RV-06 | P1 production theo capability | Q01/05/06/11 FREE/global history/reconcile/cache/concurrency còn mở; PNG có action không chứng minh API tồn tại | Giữ fixture có badge; chỉ wire capability đủ contract/version/evidence, giữ REST completed baseline. Không tạo endpoint lookup, fake item hay outbox để lấp gap |
| RV-07 | P2 provenance | Canonical không nguyên bytes/pixels với attachment, trái verification.json/README | Lưu attachment nguyên byte/hash làm source immutable trong bản bổ sung; gắn tên/provenance bản đã chuyển đổi, sửa claim và rehash gói mới. Không sửa ZIP review hiện tại |
| RV-08 | P2 docs | FINAL_CHECKLIST vẫn BLOCKED canonical, REFERENCE_REVIEW vẫn Missing Thinking; 11 reference row chưa phản ánh PNG; historical preamble chỉ giảm nhầm lẫn | Cập nhật bảng active sang PRESENT/DERIVED, tách visual parity/body/R07 còn mở; ghi rõ lịch sử. README nên dẫn review PNG hiện hành thay correction board cũ là entry chính |
| RV-09 | P2 export / portability | Full-resolution files ~1,2–1,36 MB PNG, ~0,68–0,82 MB WebP; alpha thấp có noise; provenance absolute paths không portable; screen PNG thiếu inventory riêng | Thêm export runtime theo density/size sau khi có task; giữ master, alpha QA nền tối/cream, manifest variant; thêm visual inventory/source hash và liên kết tương đối. Không ship screen raster hoặc generated_files paths vào app |
| RV-10 | P2 visual coverage | Chưa có reference riêng pre-verdict/WAIT/SKIP/history empty/font scale; 11 ảnh không bao hết UI_STATE_MATRIX | Bổ sung substate reference/criteria cho state đã có; không invent UX mới. Xác nhận generic spec đủ để implement và đánh dấu visual coverage từng state |
| RV-11 | P2 typography | Nunito binary/license chưa bundled; PNG chỉ Nunito-style | Bổ sung font/license/provenance hoặc chỉ ra dependency repo đã được xác minh; không tự gọi fallback font là final |

RV-03 là phép tính trên token màu được ghi trong gói, không phải đo accessibility runtime hoặc chứng nhận màu pixel trong ảnh AI. Body văn bản spec đã yêu cầu 4,5:1 và gradient minimum; phần thiếu là một cặp màu thực thi đáp ứng yêu cầu đó.

## 5. Document completeness và gate triển khai

Đủ nhóm tài liệu: UX/recovery, 11 screen specs, navigation, components/logical contracts, state/transitions, edges/a11y, visual references, actual assets/provenance, implementation phases, AC01–29/checklist, coding prompt, dependency/source snapshots. Đây là độ đầy đủ cấu trúc; không phải toàn bộ source/design/API đã hoàn chỉnh.

Điểm mạnh: phân biệt evaluation persistence với business mutation; fake data có badge; REST non-stream được giữ; operation identity/idempotency và UnknownOutcome được mô tả; lifecycle capability gated; không tự invent enum/API/FK; source snapshots được phân biệt với original approvals. README hiện hành đã ghi NOT COMPLETE rõ.

Các hạn chế còn lại: một số active checklist/mapping chưa cập nhật theo v1.1; visual refs generated có identity/pose drift và text fixture; không có visual v0.2 nguyên bản/layers; chưa đối chiếu 34 decision bodies; source freshness trong lịch sử không đồng nghĩa review này fetch lại nguồn. Acceptance test list đầy đủ không chứng minh test đã chạy. 8 presentation expressions chưa đóng Q09 emotion→asset mapping của 9 domain emotions, nhất là fallback và reduced motion theo state.

**Lộ trình đóng review:**

1. Tạo bản correction phụ lục/phiên bản mới, không sửa baseline; xử lý RV-01/02/03 và đồng bộ tài liệu RV-08. Khóa một mapping duy nhất, dùng asset thật, chốt foreground/gradient contrast.
2. Bổ sung source provenance đúng, font/license và visual state coverage. Original-v0.2 parity vẫn ghi UNVERIFIED cho tới khi có export/metadata nguồn.
3. Khi được giao task implementation riêng, có thể dựng native UI/fake fixtures theo written contracts sau khi xử lý các P1 UI; kiểm 320dp/200%font/IME/TalkBack/reduced motion và error/unknown state. Đợt review này không tạo code hoặc chạy app test.
4. Production integration tách theo capability; R07 và Q áp dụng chỉ đóng bằng contract/schema/API/test evidence tương ứng. Source 34 decisions khôi phục theo từng ID; không blanket VERIFIED hoặc tự bật Delete vì real send đã hoạt động.

## Evidence kèm theo

- `audit_results.json`: số liệu ZIP/checksum/manifest/link/raster/alpha/decision.
- `canonical_difference.json`: đối chiếu channel giữa attachment và canonical trong gói.
- `expressions.jpg`: contact sheet canonical attachment + 8 expressions.
- `screens.jpg`: contact sheet 11 PNG, đọc ảnh gốc trong extracted để xem chi tiết.
- `alpha_dark.jpg`: canonical/Neutral trên nền tối và alpha mask.

Không sửa gói bàn giao hay baseline; tất cả phát hiện là review và đề xuất, chưa tự áp dụng correction vào artifact nguồn.
