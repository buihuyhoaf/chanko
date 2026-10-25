# TC-LIFECYCLE — archive, restore, multi-device

**APPROVED A3-A, deletion A1-A và A4 amendment áp dụng.** Approval gate đã đóng bởi user; runtime tests là bước sau implementation. [Approved addendum](08_APPROVED_AMENDMENTS_V14.md) bổ sung same-thread bind/context revision và offline retention.

## Approved state và capability

| Lifecycle | Read | Send/evaluate | Archive | Restore | Delete |
|---|---|---|---|---|---|
| ACTIVE | Theo owner/context | Online +item/context capability +không AI op active | Online +If-Match | Không áp dụng | Online +If-Match |
| ARCHIVED | Theo owner, read-only | Không | Idempotent replay action đã commit; action mới recheck | Online +If-Match | Online +If-Match |
| DELETED |404 chung, kể cả transcript/search | Không | Không | Không | Chỉ replay intent đã commit được204; request mới recheck404 |
| UNKNOWN/offline cache | Có cache hợp lệ và nhãn stale | Không | Không | Không | Không |

Lifecycle chat không sửa item archive/status, Ready marker, cooling-off hoặc official history. Restore không reset conversation messages/evaluations. Nếu item không còn khả dụng, restore chat có thể mở đọc lịch sử nhưng send/evaluate vẫn tắt theo item capabilities; không tự tạo/restore item.

## Transaction archive/restore

Auth/owner → validate/replay key theo03 → lock thread/active operation → If-Match/capability/lifecycle → tăng version+lifecycle_epoch → ghi lifecycle/result+invalidation metadata → commit →200 Conversation. Provider calls không nằm trong transaction. Archive thắng trước AI finalization thì operation INTERRUPTED_LIFECYCLE, không assistant/evaluation mới. Nếu AI đã commit trước thì giữ successful history rồi archive.

Restore chỉ ARCHIVED→ACTIVE. Không resume provider hoặc tự gửi draft. Retry operation bị archive ngắt không được tự chạy lại sau restore; user gửi intent mới nếu muốn tiếp tục. Source transcript và context identity không clone. Archive cùng version ở hai máy: một mutation thắng; replay cùng key nhận cùng ack; key khác/version cũ409.

## Approved MVP sync: refresh có kiểm soát

Giữ baseline TECH-03 §4.3: refresh khi mở màn hình, foreground, user refresh và sau mutation. Không bắt buộc WebSocket, push, periodic WorkManager hoặc general outbox.

Khi foreground/reconnect:
1. Auth/session hiện hành; không render owner cũ. Hiển thị retained cache có timestamp/stale label kể cả freshness hết24h; không age cutoff cho offline read; Send/mutations tắt cho tới khi xác minh thread/capability.
2. Đọc active thread GET và operation lookup của pending intent; validate tối đa100 cached thread IDs/batch qua cache-validation.200 results chỉ gồm requested IDs với lifecycle/version/context_revision/kind/item hoặc UNAVAILABLE, không title của resource không thuộc owner.
3. DELETED/UNAVAILABLE: purge local transcript/summary/current draft và pending raw UI content của thread; giữ journal ID/hash cần reconcile đã redaction. Không tự restore/clone/resend.
4. ARCHIVED: read-only, giữ draft session nếu chưa delete nhưng không tự gửi; user chủ động restore khi có capability. Server terminal result/epoch thắng UI cũ.
5. Refresh history từ page1 với query-generation mới; discard response generation/owner cũ. Reconcile server message IDs để xóa local pending duplicate, không append theo timestamp.

POST cache-validation là endpoint đọc bổ sung (không mutation): body thread_ids unique tối đa100. Response từng requested opaque ID: AVAILABLE +lifecycle/version/context_revision/context_kind/item_id/capabilities, hoặc UNAVAILABLE chung. Ownership kiểm tra từng ID, không lộ existence. Backend error không được diễn giải thành deleted; giữ stale/cache hoặc khóa action cho tới xác minh.

## Approved guarantees và giới hạn

Máy online foreground cập nhật state sau refresh thành công; không hứa realtime giữa hai lần refresh. Máy offline tiếp tục đọc dữ liệu cache retained dù quá24h, có stale/cached_at label; chưa thể biết remote delete/bind. Khi biết deletion/owner invalidation thì purge và giữ tombstone chống resurrect. A4 freshness24h chỉ đánh dấu cần refresh khi online; không TTL khóa đọc offline. A1 server purge/backup không hứa xóa tức thì trên máy offline.

Send sau remote archive/delete vẫn bị server reject khi version/lifecycle check; không dựa vào UI để bảo vệ. Dữ liệu chat không là nguồn authoritative cho item/budget. Row change count hoặc list absence trên một page không đủ để purge resource; cần explicit validation/thread read hoặc freshness. Refresh thất bại là lỗi mạng, không phải empty history.

## Race matrix

| Case | Approved outcome |
|---|---|
| A send, B archive | Nếu archive trước final commit, result bị fence; nếu result commit trước thì giữ rồi archive |
| A draft, B delete | A purge khi biết deleted; offline không gửi; retained cache vẫn đọc không age cutoff, purge khi biết deletion |
| A restore, B delete | Version/transaction winner; deleted không được revive bởi restore cũ |
| A/B send khác key | Một active/thread; loser409/reload, không clock winner |
| Response cũ tới sau account switch | Bỏ hoàn toàn theo owner+query generation; không ghi cache account mới |
| Item version đổi khi provider chạy | CONTEXT_CHANGED, không publish nhận định như dữ liệu mới nhất |
| Read cache sau logout/login khác | Không lộ title/preview/messages/draft/journal account trước |

Tests IT04/IT09/IT11/IT12/IT15. Không có transport đồng bộ mới được coi là phê duyệt chỉ từ visual baseline.

## Confirmed bind trên thiết bị khác

Nếu B bind FREE→ITEM, A refetch/cache-validation phải cập nhật **cùng thread ID** với context_revision mới, giữ loaded transcript và session draft. Đổi query generation/filter projection, không create/reuse thread khác hoặc duplicate history row. Old FREE send/evaluate/retry/picker confirmation với version cũ bị server conflict trước AI call, không auto resend. Operation replay dùng captured admission context; old successful response không được overwrite current ITEM projection.

List/message cursors và cache generations dùng08. Known deletion tombstone thắng mọi page/response cũ; network failure không phải deletion. Retained offline read từ old context vẫn có stale label và không writes; khi online phải current capability/context/version validation.
