# TC-STREAM-OPTION — chỉ khi thay đổi baseline transport

**OPTIONAL PROPOSAL / NOT APPROVED / DISABLED.** Không nằm trong gói A1–A5. TECH-03 §5.1 hiện là REST completed-response không SSE/WebSocket. Không cần quyết định mới để giữ REST. Cả REST interruption recovery và retry đã đặc tả trong03, không phải thiếu streaming contract của MVP.

Nếu sau này user chọn streaming thật, phải ADR override với Backend/Android owners; phương án cụ thể để review như sau, không tự sửa TECH:

1. POST messages/evaluate vẫn admission theo same parent intent/key, operation ID, context/version/epoch. Tách stream read khỏi command để reconnect không POST một intent mới.
2. Đề xuất authenticated GET `/ai/operations/{operation_id}/events` bằng HTTP client có Bearer, không token query string. SSE event gồm operation_id, attempt_no, message_id ổn định, event_sequence tăng, type, payload schema và lifecycle epoch. Event types STARTED, DELTA, COMPLETED, FAILED, INTERRUPTED. Event ID gồm operation+attempt+sequence; whitelist schema.
3. DELTA chỉ provisional assistant text, không chính thức Evaluation/DecisionRecord và không suy BUY từ prose. UI upsert cùng bubble, bỏ event đã nhận, không announce từng token. COMPLETED chỉ sau DB commit hợp lệ và gồm resource IDs/version; client GET canonical result trước khi thay provisional thành final nếu payload thiếu/không chắc.
4. Reconnect Last-Event-ID cùng operation/attempt, không key mới. Đề xuất replay window5 phút, max2.048 events/5MiB; expired/gap→RESYNC_REQUIRED rồi GET operation/result, không fabricate missing chunks. Tham số là proposal của ADR tương lai.
5. Client đóng view/stream chỉ hủy subscription; không hứa cancel provider/backend. Explicit cancel nếu cần là action mới phải được thiết kế/phê duyệt riêng; không thêm ngầm trong MVP.
6. Timeout/connection interruption giữ UnknownOutcome; terminal result có thẩm quyền ở operation record. Sau COMPLETED bỏ delta muộn. Attempt retry được03 admit với fencing token; event attempt cũ không overwrite bubble/result attempt mới. Tối đa một successful intent finalization.
7. Auth/owner/lifecycle check trước mở và trước phát nội dung; delete/archive epoch change ngắt stream và redaction theo A1. Replay buffer chat text phải bị invalidated ngay khi delete, không chờTTL5m.
8. Structured verdict chỉ có sau complete validation/commit; invalid output/partial không default BUY. Long-lived connections có quota/backpressure/timeout, đo tải và security review trước release; không tự chọn hosting/platform TECH-04.

Acceptance tương lai: ordered/duplicate/gap replay; disconnect-before/after-commit; same-key reconnect; expired replay; account switch; delete trong stream; unauthorized resume; output invalid; reduce-motion/TalkBack; no mutation from deltas. Chưa triển khai/chạy test và không được gọi server streaming có sẵn.
