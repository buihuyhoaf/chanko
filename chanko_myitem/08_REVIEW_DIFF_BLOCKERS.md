# Review và thay đổi v0.9 → v1.0 (Recovery) • 24/10/2026

## Kết luận

Đã đối chiếu đủ29 quyết định UX từ Recovery Baseline user cung cấp với Functional Specification v1.1 và TECH-02 vừa đọc lại Drive. Không còn blocker thiếu nội dung ITEM/DETAIL/EDGE/GAP. Các sửa đổi dưới đây thay thế phần Reference trái baseline; exact dimensions/copy/sort defaults chưa có nguồn vẫn Reference. Đây là bộ bàn giao hoàn chỉnh theo phạm vi tài liệu/assets, không phải chứng nhận app production-ready.

Input v0.9 là attachment được chat trả về; `/mnt/data` không tồn tại ở máy này. SHA256 `0ec26810cc85fdf5079525ba60c86ee7e49cfbea54e7d11f4b6a5e4f57c33cc6`,19 files, CRC hợp lệ, trùng bytes ZIP workspace. Bản v1.0 trước Recovery giữ tên `...v1.0_pre_recovery.zip`; ZIP v1.0 hiện tại đã thay nội dung. Không dùng bản trước recovery để triển khai.

## Diff chính so với v0.9

| Vùng | v0.9 | v1.0 Approved theo baseline |
|---|---|---|
| MyItems list | List Reference, card có More | List mặc định, horizontal/left thumbnail, compact read-only, tap detail; bỏ More/inline mutations — ITEM01/05 |
| Header | Hero chung, thiếu overview tiles | Compact3D Chanko Hero +3 tiles All Items/Cooling Off/Ready tap filter — ITEM02/03 |
| Filters |5 status chips/multi-select, Ready optional, không Postponed filter |2 cấp All/Active/Purchased/Skipped; Active: Saved/Considering/Cooling Off/Ready/Postponed +Search/Sort — ITEM04 |
| Empty/navigation | Empty chung, app bar Add | Chanko+hộp kẹo, Save an Item/Clear Filters;4 tabs+global+, không FAB/Add trùng — ITEM06/07 |
| Detail structure | Layout/CTA gần như toàn Reference | Stage/summary/expandable facts/bubble/Journey/sticky primary+More/contextual content Approved — DETAIL01..06 |
| Waiting actions | Adjust primary, Skip dễ lẫn nghĩa | Countdown nổi bật, Adjust/Skip Waiting trong More; Skip Waiting kết thúc phiên rồi mở lựa chọn, không tự Skip item — DETAIL07/GAP02 |
| Ready | Chỉ dựa server eligibility, resolution thiếu source | Ready duy trì đến action rõ, mindful review có thể bỏ qua; offline Ready tạm thời không mutate cache/server — DETAIL08/GAP01/EDGE01 |
| Need More Time/Postponed | Chỉ new wait; badge Postponed tạm ẩn |2 nhánh new ACTIVE hoặc Considering+Postponed; hiển thị next review khi có — DETAIL10/12 |
| Purchased/Skipped | Primary có thể thúc re-evaluation | Ưu tiên read-only; Buy Again/Direct Mark as Purchased trong More, không Quick Check bắt buộc — DETAIL09/13/EDGE04 |
| Purchase/transactions | Sheet/history/correction là đề xuất | Purchase Confirmation=Record sheet, History section riêng, tap mở transaction correction — DETAIL11/14/GAP04 |
| Archive | Chặn ACTIVE/restore-trước-ghi là guard đề xuất | Archive có purchase +Restore Approved; không coi guard đề xuất là policy; ACTIVE/permissions còn contract gaps — EDGE03/GAP03 |
| Visual | Product image hoặc placeholder; một số token bị Reference | Handoff hero dummy-only; canonical cat ngoài media, không tự thay; tokens/spacing/adaptive đúng Design System |

README,01/02/03/04/05/06/07 và manifest đã đồng bộ;09 lưu provenance; nguồn Recovery giữ nguyên trong sources. Detailed diff tại `qa/v0.9_to_v1.0.diff`.

## Các điểm Reference đã chuyển Approved

Đủ29 rows tại07 có nguồn và mapping acceptance. DETAIL03 giữ là tổng hợp contextual UI, không bịa option A/B. Mỗi placement/filter/interaction nêu trong baseline được nâng Approved; không nâng sort options/defaults, kích thước mới, microcopy hoặc guard backend không có nguồn. Giữ Approved5 status, purchase correction/accounting/offline của phụ lục, không mở lại vấn đề đã giải quyết.

## Đối chiếu nghiệp vụ trọng tâm

- Ready: giữ đến hành động rõ, xem/Back/bỏ review không clear. Backend COMPLETED history không bị viết lại chỉ để ẩn banner. Still Want It→mời AI1–2 câu có thể bỏ qua; timing marker clear chưa rõ. Need More Time→new ACTIVE giữ phiên cũ hoặc Considering+Postponed event.
- Offline: EDGE01 cho provisional Ready theo deadline cache; không official status mutation, purchase hoặc budget. Reconnect refetch; EDGE07 giữ form, không tự submit. Room read cache/manual item drafts như ADR09, không business outbox.
- Skip Waiting: GAP02 khác Skip item. Map cơ chế cancel ACTIVE→CANCELLED/Considering theo phụ lục rồi mở choices. Explicit Not Anymore mới Skipped; dismiss không tự Skip/mua. Không invent backend action enum.
- Archive/Restore: giữ transaction/history/budget và item identity/status. Chưa có rule ACTIVE/restore overdue/Ready/permissions; không auto-cancel/pause/reset Wishlist/cascade.
- Xóa purchase cuối: giữ status hiện tại bất kỳ, không hồi sinh timer, giữ evaluation/history; spent/count/cycles/recap tính lại. Không refund.
- Budget: actual unit×integer qty, server total; purchased_at gán [start,end), PAY_NOW=CREDIT optional card; amount ngay current cycle setting+snapshot atomically; closed snapshots giữ, recap tính lại; no rollover/debt. UI thresholds không chặn quyền mua.
- Multi-purchase: N transactions/item, Buy Again chủ động trong More; mỗi intent mới key mới, retry giữ key; count không quantity, tổng history không suy từ1 page. Frequent≥3 là presentation threshold tham khảo.
- Visual: Candy Pop3D/Nunito/M3/rounded, canonical white cat không chó/mèo thay; không trong/cạnh/chồng product media; dummy-only hero, không túi/tai nghe/giày; không celebration/shaming/upsell.

## Blockers còn thực sự tồn tại

| ID | Thiếu gì | Phạm vi bị chặn / cần cung cấp |
|---|---|---|
| C-01 | Backend Ready/Postponed persistence và resolution | Contract cho marker eligibility/clear sau Still Want It và Postponed, restart/thiết bị khác; event/next-review trigger/clear/precedence. UX Approved không cần duyệt lại |
| C-02 | Archive khi WAITING/ACTIVE và Restore lifecycle | Contract chọn rõ chặn/giữ/cancel, deadline/reminder/completion/Ready khi archived, restore overdue và archived write/correction permissions. Không tự chọn policy |
| A-01 | File mèo Chanko canonical, Empty State Chanko+hộp kẹo | Cần đúng source asset/pose/state/provenance/quyền/alpha/size. Chặn mascot visual final, không chặn dummy hero/text UI; không tạo mèo khác |

Integration prerequisites: repo/API thật để map routes/models/version/idempotency/timezone/limits và build/runtime tests; licensed Nunito/icons và contrast pairing. Approved screen images chưa có để pixel-match; baseline mô tả đủ bố cục nhưng không khẳng định đã match mockup. Các prerequisites này không phải thiếu nội dung29 mã hoặc lý do hỏi duyệt lại toàn bộ UX.

## Xác minh

Input CRC/safe paths/hash; Recovery Baseline lưu nguyên văn và đủ29 mappings; source Drive vừa fetch lại có modified time như trước;5 dummy assets giữ checksum/bytes, PNG512 đã xem, PNG chunks/dimensions/XML được kiểm tra. QA và checksums v1.0 được tái tạo, ZIP kiểm CRC và round-trip bytes. Không chạy Android build/aapt/runtime/TalkBack hoặc test backend vì không có repo. Không sửa app/Drive, không PR, không triển khai ứng dụng.
