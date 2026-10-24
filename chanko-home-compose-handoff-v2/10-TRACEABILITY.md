> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Source / traceability — v2

| Source thực | Evidence | Sử dụng |
|---|---|---|
| Ảnh người dùng đính kèm 24/10/2026 | references/approved-home-user-reference.png copy nguyên byte | Nguồn chuẩn Home layout, scene và Global sheet |
| Chỉ dẫn “làm đúng theo ảnh kia” | User xác nhận sửa gói theo ảnh | v2 thay thế v1 visual |
| Yêu cầu gốc | max 3 priority Ready→Waiting→Considering→Saved, optional Observation/Draft, contextual sheets | Giữ nguyên invariant nghiệp vụ/structural |
| Conversation 6ada2298-b430-83ec-9edf-ceb4868856a2 | read_thread chỉ có content-reference, attachments=[] | Không suy diễn tài liệu bị thiếu |
| outputs/chanko-home-handoff/approved-home-v5.png | Đã xem; calendar-led cũ | references/home-v5-legacy.png, historical only |
| outputs/chanko-home-handoff/UI-HANDOFF.md và tokens.json | Đã đọc | Tài liệu/token legacy tham chiếu, không override ảnh mới |
| outputs/chanko-home-handoff/assets/ | 6 PNG thực | Giữ demo/provenance; mèo khoanh tay không đúng pose final |
| outputs/initial-setup-v1/assets/ | 4 font Nunito thực | Copy, license/coverage pending |
| PROJECT_MEMORY.md | Không VM/emulator/AVD; runtime thiết bị thật, không launcher phụ | QA guardrails |
| AGENTS.md | sources/ read-only | Không sửa synced sources |

Đã có approved raster mới; chưa có scene layers, asset final rời hoặc nguồn font/license đủ. Các contextual sheets không xuất hiện trong ảnh, không được tự gọi là đã duyệt. Token dp/sp/HEX chính xác không được trích từ file vector; cần implementation review.

Không sửa repository, không xác nhận trạng thái code/phase/build. v2 chỉ cập nhật artifact. Không cắt/tái tạo ảnh mới; giữ file ảnh gốc nguyên byte.

## Request → deliverable

Ảnh chuẩn/mapping → 00; UX → 01; component tree/contracts → 02; Candy Pop/Nunito → 03; manifest/resources → 04; roadmap Phase 0–11 → 05 + 12 prompts; 22 scenarios → 06 + fixtures; previews/tests → 07; acceptance → 08; pending TECH → 09. README/START-HERE định hướng; bản gộp Markdown và ZIP tải được.
