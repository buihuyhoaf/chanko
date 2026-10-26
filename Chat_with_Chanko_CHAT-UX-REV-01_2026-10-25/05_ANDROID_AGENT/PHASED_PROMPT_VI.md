# Prompt cho Android coding agent — theo phase
Bạn triển khai UI Chat with Chanko theo CHAT-UX-REV-01. Đọc toàn bộ README, SCREEN_SPEC, NAVIGATION_STATE_MATRIX, DESIGN_SYSTEM, TECH_BOUNDARY, QA và assets/mapping trước khi sửa code. Tìm AGENTS.md và kiến trúc repo thực. Đây là prompt bàn giao, không phải source code Android.

Giữ đúng3 màn CL/CD/NC. CL kiểu Messenger có search/filter/thread list/fixed “+ Đoạn chat mới”, KHÔNG composer. CD có bubbles/quick replies/Decision Card/composer fixed trên IME. NC chọn Free/Item + inline item picker, không composer. Archived/empty/loading/offline/error/rename/archive/delete/confirm là state/dialog/sheet, không màn chính thứ4. Bỏ entry Welcome và Chat History riêng ở flow mới. App vẫn Home/My Items/Budget/Insights, không thêm Chat tab hoặc duplicate global +. Entry Home→CL; Item Detail→CD ITEM; Back đúng origin.

## Phase0 — Audit và contract map
Đọc repo, chỉ ra UI/navigation/repository/domain/cache seams, existing Home/Item Detail entry và tabs. Đối chiếu approved v1.4 addendum với contract thực, không tự đổi TECH-02/03/schema/endpoint. Ghi file-impact plan, reuse policy direct ITEM, operation reconciliation và Rename gap. Không lấy ảnh làm DTO. Gate: có mapping rõ, contract gaps ghi đúng; phần UI/fake có thể tiến hành dù backend chưa deployed.

## Phase1 — Theme và assets
Dùng Nunito/Material3, token Approved giữ nguyên; semantic foreground Reference đủ contrast. Import canonical PNG/WebP giữ identity; dùng static Neutral ở verdict, không redraw mèo/không swap asset shopping-happy. Material Symbols Rounded SVG có license. ProductMedia dùng product repo/dummy; mascot không ở thumbnail sản phẩm. UI text là native text, không screenshot button. Gate:3 layouts widths320/390 và 200% scale không overlap, missing assets có fallback text, BUY không sparkles/success green/sound.

## Phase2 — UI với fake repository
Làm CL/CD/NC và tất cả state/modal theo spec, fake repository deterministic; luôn hiện “Bản mẫu · Phản hồi dựng sẵn”. Không provider/network/secret trong fake. Threads/messages stable IDs, pagination, draft/scroll state. Quick reply FillComposer, không overwrite draft. Rename/archive/delete/restore chỉ fake fixtures và label rõ; không báo đã sync cloud. Gate: screenshot + interaction demo3 màn; no Welcome/History route riêng; no composer CL/NC; accessibility/focus/back/IME pass theo checklist.

## Phase3 — Operation và offline semantics
Tích hợp UI state machine: pending/unknown/failed/acked, identity journal, retry cùng key+payload, version/context guard. FREE→ITEM explicit confirm giữ cùng thread/transcript/draft; no clone/merge, no official records từ pre-bind FREE history. Retained cache offline đọc được sau24/48h; no writes/outbox/auto resend. Account isolation, tombstone/late responses, scroll anchor, process recreation. Gate: race/offline/retry acceptance scenarios trong QA có bằng chứng; không success giả.

## Phase4 — Production adapter theo contract thật
Chỉ wire ChatRepository khi endpoints/capabilities/auth/version/operation lookup triển khai thực khớp approved contract; nếu thiếu ghi blocker cụ thể và giữ fake preview tách biệt. Rename chưa có contract thì không invent endpoint/schema hoặc giả persisted rename. Dùng backend REST orchestration, không gọi provider trực tiếp, không secret client, không SSE ngoài scope. Evaluation persistence có thể qua server nhưng chat không ghi user Decision/Purchase/status/budget/cooling-off. Delete/archive giữ official history độc lập. Gate: integration evidence với adapter thật cho từng operation và lỗi403/404/409/428/timeout; không gắn PASS cho test chưa chạy.

## Phase5 — Nghiệm thu
Chạy test phù hợp repo và QA checklist; screenshot Native100/150/200%, TalkBack/focus/IME/insets, offline no-cache/stale, races2 devices/process death. Báo changed files, checks đã chạy, failure/gap còn lại và ảnh3 màn. Không tạo PR hoặc sửa backend/TECH source trừ khi human giao thêm trong nhiệm vụ triển khai. Không suy “approval34 IDs” thành body VERIFIED; giữ PARTIAL ledger. Không claim production-ready từ fake UI hoặc tài liệu/ảnh.

## Invariants bắt buộc tại mọi phase
BUY là advice trung tính, mascot Neutral, không chúc mừng/sparkles/green victory/CTA mua. “Xem món đồ” đi flow nghiệp vụ có confirm riêng. Verdict ≠ user decision ≠ status ≠ purchase. Official Decision/Evaluation/Purchase history không bị xóa theo chat. Personality chỉ đổi giọng, không sửa verdict lịch sử. Hạn chế text/name/retry lấy contract, không tự đặt số thành policy. Không tự thêm màn chính/thư viện/feature ngoài scope.
