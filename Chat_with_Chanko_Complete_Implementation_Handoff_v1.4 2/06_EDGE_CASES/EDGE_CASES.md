# Edge Cases
A1–A5 đã approved theo14, A2/A4 amendments normative13/08. Fixture/mock boundary vẫn cần khi backend chưa triển khai, không giả deployed capability.11 visuals đã approved; asset load failure là runtime edge, không file thiếu trong gói.

| ID | Tình huống | Kết quả mong đợi | Bảo toàn |
|---|---|---|---|
| EC01 | Gửi whitespace, paste dài vượt limit thật | Không gửi rỗng; length error giữ input, không tự cắt | Không tạo operation vô nghĩa |
| EC02 | Double tap Send / rotate khi pending | Một user bubble và một operation | Cùng key, không duplicate |
| EC03 | Response mất sau commit | UnknownOutcome, reconcile cùng intent | Không gửi lại key mới |
| EC04 | Stream demo ngắt/duplicate/out-of-order | Partial chưa có verdict; replay dedup/sequence | Không hai assistant bubbles/terminal |
| EC05 | Back khi đang trả lời | Rời view an toàn; load lại/reconcile khi quay lại | UI cancel không tuyên bố backend canceled |
| EC06 | Offline trước send | Disable + lý do, giữ text đang nhập | Không message/outbox/AI giả online |
| EC07 | Mất mạng giữa request | Cache đọc được; pending/unknown khi chưa biết kết quả | Không auto replay reconnect |
| EC08 | Offline no-cache | “Chưa có dữ liệu lưu trên thiết bị” | Không gọi account empty |
| EC09 | Item sửa giá/status trong chat | Refresh context; verdict cũ ghi lịch sử/stale | Không silently recompute verdict |
| EC10 | Item deleted/unavailable/khác owner | Safe unavailable, không leak title; không send/evaluate | Không tự tạo lại item |
| EC11 | Item archived / Waiting hết hạn | Chat hiển thị snapshot có nhãn; quyền theo contract | Không cancel timer/clear Ready tự động |
| EC12 | Thread archived trên thiết bị khác | Refresh version/capability trước send | Không tự restore |
| EC13 | Delete đang pending bị timeout | Giữ UnknownOutcome, reconcile; không báo xóa chắc chắn | Official history/purchases còn nguyên |
| EC14 | Delete Cancel/Back | Đóng dialog về thread | Không mutation |
| EC15 | Delete chat có evaluation | Transcript bị xóa theo retention contract; item history vẫn đọc được | Link conversation mất có fallback, không cascade |
| EC16 | Restore archived chat | Message/context/history giữ, item archive/status không đổi | Không tạo thread clone |
| EC17 | Free Chat nói “mua rồi” | Chỉ message, mời dùng flow ghi nhận riêng nếu có | Không purchase/decision/budget |
| EC18 | Free Chat chọn existing item +confirm | Bind same thread, giữ transcript/draft; Cancel FREE, no item create | No clone/reuse another thread or backfill official history |
| EC19 | BUY trái quyết định SKIP của user | Giữ cả advice và official choice đúng nguồn | Không shaming/ghi đè history |
| EC20 | Personality đổi giữa thread | Giọng mới cho phản hồi mới, kết luận lịch sử giữ nguyên | Không sửa old verdict |
| EC21 | Payload invalid/unknown verdict/emotion | Reject invalid verdict; emotion fallback canonical neutral | Không default BUY/mèo khác |
| EC22 | 401, 403, 404, 409, 422, 428, 429, 503/504 | Reauth theo app; 403 safe forbidden; safe unavailable; reconcile conflict; inline validation; 428 lấy lại precondition và xem lại thay đổi, không retry mù; rate limit retry theo server; AI unavailable | Không retry vô hạn, không expose raw error |
| EC23 | Account switch/logout | Hủy subscriptions, clear rendered data/drafts theo policy, caches owner-scoped | Không rò thread giữa accounts |
| EC24 | Long title/text, font200%, keyboard | Wrap/scroll, CTA tiếp cận được, tiền/lỗi không cắt | Touch≥48dp/TalkBack |
| EC25 | Pagination refresh/query đổi | Reset cursor phù hợp, bỏ stale response, stable dedup | Không thiếu/trùng thread/message |
| EC26 | Empty rationale/missing budget/history | Nêu thiếu dữ liệu hoặc lỗi schema; không bịa con số | Không insight/decision giả |
| EC27 | Mascot/font asset thiếu | Text-only báo gap; theme/font fallback chỉ trong preview có ghi rõ | Không dùng mèo ngẫu nhiên |
| EC28 | New message khi đọc history phía trên | Giữ scroll, indicator jump to latest | Không giật màn, announce không mỗi token |

## Edge cases bổ sung (coverage; không policy chưa chốt)
| ID | Case | Kết quả / gate |
|---|---|---|
| EC29 | Item Chat idle đang Quick Check, chưa verdict | Timeline/composer hiện, card vắng; không spinner vô hạn |
| EC30 | Early verdict trước3 câu, lỗi/timeout | Gửi theo capability/operation, không default BUY; UnknownOutcome không key mới |
| EC31 | Thiết bịB archive/delete khi A đang soạn/gửi | Recheck/refetch quyền/lifecycle, không auto restore; operation outcome/retention theo approved13/02–05+08 |
| EC32 | Hai thiết bị gửi/evaluate đồng thời | Không chọn winner bằng timestamp client; server concurrency/order theo approved13/03+08, hiển thị conflict/reconcile khi được xác định |
| EC33 | Page cursor cũ sau archive/delete/restore và refresh | Stable IDs, bỏ stale query response; cursor invalid theo server, không đếm total bằng page length |
| EC34 | Process death/account switch với cache/pending draft | Owner isolation, không leak; không tự resend khi mất identity; retention/freshness/purge theo approved13/05+08 |

## Amendment edge cases v1.4
Cached data48h+ đọc offline với stale label, không no-cache chỉ vì TTL. Two-device bind khác item/version: one CAS winner, loser conflict/refetch same thread. Old FREE retry/page/response after bind không overwrite ITEM context hoặc tạo official records. Known delete tombstone prevents stale response resurrect; offline chưa biết remote deletion vẫn retained read, no writes/replay.
