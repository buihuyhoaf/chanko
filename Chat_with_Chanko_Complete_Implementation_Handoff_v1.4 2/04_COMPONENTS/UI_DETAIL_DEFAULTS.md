# Default UI — APPROVED A5-A, revision1.4

Các ảnh chính thức giữ nguyên byte; user đã approve các event/guard A5. Không tự map sang CHAT/GAP/FINAL.

| Event/state | Default A được đề xuất | Không làm |
|---|---|---|
| Tap quick reply, composer rỗng/idle | FillComposer, giữ option ID nếu có; focus vào input, user Send riêng | Không auto send/business action |
| Tap quick reply, draft không rỗng | Giữ nguyên draft; inline “Bạn đang có bản nháp. Gửi hoặc xóa nội dung trước khi chọn gợi ý.” | Không overwrite, không sheet thứ hai |
| Back/context switch | Draft keyed owner+thread/logical unsent context; giữ trong session/rotation | Không cloud/durable draft guarantee hoặc đổi context thread |
| Delete confirmed | Clear thread draft sau ack/known deleted; unknown vẫn reconcile | Không xóa official history |
| Logout/account switch | Không render draft account trước; logout purge chat session/cache/journal | Không xóa Offline Manual Item Draft theo policy chat |
| Pending/UnknownOutcome | Draft in view được edit theo reference, Send/picker tắt; Check status logical event | Không key mới hoặc giả failed |
| Đang đọc trên timeline | Giữ scroll; affordance tin mới, user jump explicit | Không giật về cuối/announce từng token |
| Empty composer | Send disabled; reason/label accessible; trim validation | Không operation rỗng |

Copy là fixture tiếng Việt để implement/test, không thêm business promises. Source rules có nguồn trong01/RECOVERED_RULES_SYNTHESIS; A5 chỉ các default phía trên. Native semantics/TalkBack/IME/focus được xác minh sau implementation.

A2 amendment: existing-thread draft keyed owner+threadID và giữ qua bind; context change không clear/clone draft. A4 amendment: freshness/cursor expiry không chặn retained offline read. Pending/UnknownOutcome vẫn khóa send/picker cho tới reconcile, không TTL retention rule.
