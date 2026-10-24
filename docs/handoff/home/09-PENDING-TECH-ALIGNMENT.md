> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Pending TECH alignment

Đây là danh sách cần xác nhận, không phải kết luận repo đang thiếu tính năng. Không đọc code app để suy đoán trạng thái thực hiện trong nhiệm vụ này. Owner dưới đây là vai trò cần chịu trách nhiệm, chưa gán cá nhân.

| ID | Câu hỏi / quyết định cần chốt | Owner đề xuất | Chặn gì |
|---|---|---|---|
| T01 | Repo/branch/module đích, Compose/theme/navigation conventions và existing components thực là gì? | TECH lead | Mọi thay đổi code |
| T02 | Domain status nào map Ready/Waiting/Considering/Saved? Unknown/duplicate/canonical ID xử lý ra sao? | Domain + TECH | Rank production |
| T03 | Thứ tự trong cùng status, query candidate toàn cục, pagination/limit và count semantics? | Backend + product | Không bỏ sót Ready |
| T04 | Trường budget/currency/minor units/refund/negative spent; cách tính remaining/ratio? | Domain + finance/product | Budget integration |
| T05 | Cycle anchor, timezone, Instant/date, clock source và refresh khi deadline hết? | Domain + TECH | Calendar/timer |
| T06 | Quick Check flow đầu vào/kết quả/persistence? Save Item flow/validation/draft liên quan? | Product + TECH | Entries đi đúng flow |
| T07 | Ý nghĩa/tap action/badge của Hourglass, Calendar, Wallet, Pause Jar; Pause Jar có dữ liệu gì? | Product + design + TECH | Props/context sheet final |
| T08 | Mỗi contextual sheet nội dung/CTA/empty/error; decision mở sheet hay detail route? | Product + navigation TECH | Sheets integration |
| T09 | Global Quick Action chỉ Quick Check/Save Item? Có capability/session gate nào? | Product + TECH | Global sheet final |
| T10 | Route của 4 tab, settings/profile, View all và back-stack/restoration? | Navigation TECH | Nav production |
| T11 | Dialogue trong scene: rule text/count/fallback; Observation Explain/Hide/X behavior/persistence và nguồn evidence? | Product + domain | Không fabricate copy/insight |
| T12 | Draft source, nhiều draft chọn cái nào, retention/stale/resume và dirty sheet dismiss? | Product + TECH | Optional/resume |
| T13 | Offline cache/write policy, freshness, partial errors, retries, auth expiry và process death? | Data TECH | Integration robustness |
| T14 | Ảnh chuẩn đã chốt; cần layer/source, cat pose nằm chống cằm, 4 prop đúng ảnh, responsive bounds, icon set và reduced motion? | Design | Visual sign-off |
| T15 | License/provenance của PNG/font, font Việt coverage và final optimization? | Asset owner + TECH | Phát hành resources |
| T16 | Baseline copy ảnh giữ Quick Check/Save an Item + nhãn Việt; localization khác và labels Considering/Saved cần chốt? | Product/content | Copy sign-off |
| T17 | Dark mode/tablet support, accessibility target, device/performance budget và test commands hiện có? | Design + QA + TECH | QA scope/sign-off |
| T18 | Mutation idempotency, effect consumption, sheet transition và analytics nếu có yêu cầu? | TECH | Duplicate action/data loss |

## Decision log cần điền ở Phase 0

Mỗi Txx ghi: quyết định, người xác nhận/vai trò, ngày, evidence thực, ảnh hưởng component, còn mở/đã chốt. Giải pháp đề xuất không tự chuyển thành approved. Không cần chặn việc dựng preview stateless vì chưa có backend, nhưng không nối nghiệp vụ dựa trên giả định.

Ví dụ record trống (không phải quyết định đã có): `T02 — OPEN — owner Domain/TECH — evidence chưa có — impact Phase 7/10`.

Hồ sơ tài liệu hiện có không xác nhận endpoint, database schema, package, file code, dependency version, route ID, build command hoặc phase implementation của repo. Phase 0 phải tìm chứng cứ trong repo được giao; gói không invent chúng.

Ảnh chuẩn Home/Global sheet đã được user xác nhận, không còn là pending chọn layout. T14 chỉ còn asset source/layers, responsive bounds và implementation measurements; không dùng pending này để đổi visual. Nội dung contextual sheets chưa có trong ảnh vẫn cần chốt.
