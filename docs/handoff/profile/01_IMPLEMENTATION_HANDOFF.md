# Chanko Profile & Settings — Implementation handoff

Phiên bản 1.0 • 24/10/2026 • Android / Kotlin / Jetpack Compose / Material 3

## 1. Phạm vi và độ tin cậy

Tài liệu bàn giao, không chứa implementation. Không đọc/sửa source repository. Các thông số có nhãn **Đề xuất triển khai** là cụ thể hóa để coding agent làm việc, chưa phải thông số pixel đã được duyệt.

**Đã chốt từ yêu cầu hiện tại:** Profile và Settings chung một màn; chỉ avatar trên Home mở màn này; bỏ Settings icon trên Home; không bottom navigation ở Profile. Cozy Corner là mèo trắng ngồi ôm cốc tím tại bàn, khác tư thế nằm trên gối ở Home. Personality có màn riêng, chọn → preview → Save, online-only, xác nhận khi Back có thay đổi. Ready for Review chỉ là notification preference. Google Account read-only. Có Privacy, About và Sign Out theo AUTH-04.

**Ảnh đã kiểm tra:** mascot_crossed_arms.png lấy từ attachment chat 6adc3ce3-3b14-83ec-a634-ee28430cb6f6; không có /mnt/data trong môi trường này. Mèo trắng kem, mắt tròn đen bóng, tai/mũi hồng, má hồng, hai nét ria đen mỗi bên, đầu tròn lớn, chất liệu lông mềm. Đây là reference identity, không phải tư thế Cozy Corner.

**Ảnh chưa có:** a_clean_cute_pastel_3d_cg_ui_mockup_scene_a_sma.png. Lịch sử chat chỉ trả content-reference cho câu trả lời trước, không trả ảnh mockup. Vì vậy không tuyên bố đã kiểm tra pixel/layout của mockup gần nhất. Bố cục bên dưới là reconstruction theo yêu cầu, cần visual diff khi có ảnh. Không tạo ảnh mockup mới rồi gọi là ảnh đã duyệt.

## 2. Sổ PROFILE-01–10

Mã dưới đây là cách đánh số lại để trace yêu cầu trong handoff; chưa đọc được sổ mã gốc. Không coi mapping này là trích nguyên văn chat trước.

| Mã | Quyết định ràng buộc | Nơi áp dụng |
|---|---|---|
| PROFILE-01 | Gộp Profile + Settings | ProfileRoute |
| PROFILE-02 | Home avatar là entry duy nhất; bỏ Home Settings icon | Home integration |
| PROFILE-03 | Profile không có bottom nav; Back về Home giữ context | Navigation |
| PROFILE-04 | Cozy Corner decorative tap animation; không LLM, không pet game | CozyCornerCard |
| PROFILE-05 | Personality màn riêng; lựa chọn có preview rồi Save | PersonalityRoute |
| PROFILE-06 | Personality online-only; Back dirty có confirm | Save/Back state machine |
| PROFILE-07 | Ready for Review chỉ quản lý notification | ReviewNotificationRow |
| PROFILE-08 | Google Account chỉ đọc | GoogleAccountCard |
| PROFILE-09 | Privacy / About | InformationSection |
| PROFILE-10 | Sign Out tuân AUTH-04; không tự suy chính sách xóa draft | SignOutAction |

## 3. Design tokens và layout

Candy Pop 3D, nền cream **#FAF8F5**, brand purple **#8965F6**, Nunito duy nhất, Light Mode MVP. Card trắng, các điểm nhấn pastel tiết chế; component 2D native, illustration 3D có chọn lọc. Dùng Material Symbols Rounded cùng một bộ nét. Mèo giữ identity qua các màn; đổi personality không đổi giống/màu/tỷ lệ mascot hoặc logic verdict.

Thông số sau là **Đề xuất triển khai**, phải ưu tiên token hiện có đã xác minh trong design system: padding ngang 20dp; khoảng section 24dp; gap nội bộ 8/12/16dp; card radius 24dp; padding card 16dp; top bar 64dp; icon 24dp trong touch target ≥48dp; row min-height 64dp, tự giãn theo chữ; Primary Save 56dp tối thiểu. Không khóa tổng chiều cao màn. Spacing 20dp và Primary 56dp được tài liệu UI hỗ trợ, nhưng mapping vào từng component ở đây là đề xuất.

Typography fallback đề xuất nếu project chưa có token: tiêu đề màn 24sp/32sp Bold; section 18sp/26sp ExtraBold; row 16sp/24sp SemiBold; supporting 14sp/20sp Regular; button 16sp/24sp Bold. Không gắn các giá trị này là 13 typography tokens Approved của UI-03; agent phải lấy bảng nguồn nguyên vẹn trước khi mapping.

Không mặc định chữ trắng trên purple đạt AA. Đo cặp foreground/container thực tế; text thường ≥4.5:1, chữ lớn ≥3:1, điều khiển/focus thiết yếu ≥3:1. HEX foreground, outline, disabled chưa được chốt trong nguồn đọc được. Dùng semantic token đã kiểm chứng, không âm thầm đổi màu brand.

Mobile một cột cuộn, edge-to-edge với system insets. Trên tablet, đề xuất max content width 600dp, canh giữa. Ở font 200%, cho row/card tăng chiều cao và email xuống dòng; thu gọn phần trang trí trước khi làm khó đọc nội dung. Không scale nguyên screenshot thành UI.

Thứ tự nội dung đề xuất: TopBar → AccountIdentity → Cozy Corner → Personality → Notifications / Ready for Review → Google Account → Privacy & About → Sign Out → bottom safe inset. AccountIdentity dùng display name thực nếu có, fallback “Your profile”; không bịa tên/avatar. Google email chỉ nằm trong GoogleAccountCard để tránh lặp.

## 4. Component contract

Tên Compose là gợi ý phân rã, không bắt buộc package/class. Route giữ ViewModel và side effects; content nhận state/callback; không cho component gọi network trực tiếp.

| Component | Nội dung / bố cục | Input và event | State / semantics |
|---|---|---|---|
| ProfileRoute | Điều phối auth, load/revalidate profile, navigation | account-scoped state; refresh / navigate / sign out | Loading, cached, ready, error, session expired |
| ProfileScreen | Scaffold + nội dung cuộn; không bottomBar | ProfileUiState, callbacks | Giữ scroll qua child navigation; insets không bị tính hai lần |
| ProfileTopBar | Back; “Profile & Settings” | onBack | Back 48dp; heading; không thêm Settings action |
| AccountIdentity | Tên hiển thị, avatar Google nếu có | displayName, avatar | Không có edit avatar/name; placeholder neutral khi ảnh lỗi |
| CozyCornerCard | Heading “Cozy Corner”; mèo ôm cốc tím tại bàn; nền/card do Compose dựng | imageStatus, reduceMotion; onDecorativeTap | Illustration không có text raster; action semantic “Play cat animation”; không focus thêm vào từng vật trang trí |
| PersonalitySettingRow | Icon, “Cat personality”, giá trị Friendly/Sassy/Savage, chevron | committedPersonality; onOpen | Row toàn vùng clickable; giá trị unknown báo unavailable, không tự ghi Sassy lên server |
| SettingsSection | Heading + Surface chứa các row | title, children | Heading semantics; không merge làm mất switch/action con |
| ReviewNotificationRow | “Ready for Review”; mô tả “Remind me when a cooling-off period is ready to review.”; switch | effective notification state; onToggle/onOpenSystemSettings | Toggle preference không thay item/period; permission denied có lời giải thích, không hiển thị enabled giả |
| GoogleAccountCard | Google Account, email, provider Google; optional avatar | account read model | Read-only, không chevron, không onClick, không đổi account/unlink/password |
| InformationRow | Privacy / About + chevron | onOpenPrivacy/onOpenAbout | 48dp target, rõ đích đến; không mở URL placeholder |
| SignOutAction | “Sign out” dạng destructive text/outlined theo token dự án | onSignOut | Không dùng icon-only; pending ngăn gửi trùng; tuân AUTH-04 trước release |
| ProfileStatusBanner | Offline/cache/loading refresh/error | freshness, retry | Inline, không che row; chỉ công bố trạng thái hữu ích qua live region |
| PersonalityScreen | Back, title “Cat personality”; 3 lựa chọn; preview; Save cuối màn | committed, draft, load/save state; select/preview/save/back | Save sticky hoặc cuối cuộn đều phải tránh che nội dung, touch ≥48dp |
| PersonalityOptionCard | Friendly / Sassy / Savage; mô tả ngắn; radio | selected; onSelect | Một selectable group, role RadioButton; không chỉ dùng màu để chỉ selected |
| PersonalityPreviewCard | Ví dụ thoại offline tĩnh theo lựa chọn, nhãn “Preview” | draft personality | Không gọi AI; không ghi lựa chọn; không hiển thị verdict giả |
| UnsavedChangesDialog | “Discard changes?”; “Keep editing” / “Discard” | onKeep/onDiscard | Dismiss/Back ngoài dialog tương đương Keep editing; focus trả hợp lý |
| AboutContent | Tên app/version từ build; thông tin thật đã duyệt | build metadata, verified content | Không hardcode version giả hoặc invent legal/entity |

Cozy tap đề xuất: toàn illustration co nhẹ 1.00→1.025→1.00 trong khoảng 300–450ms; không lặp idle liên tục, một animation tại một thời điểm, tap dồn không xếp hàng. Đây là whole-scene transform, không giả vờ tay/mắt/cốc là layer độc lập. Reduced motion: giữ ảnh tĩnh, cho feedback nhẹ qua trạng thái pressed nếu phù hợp; không rung/âm thanh bắt buộc. Tap không mở route, không đổi personality, không tạo reward, không gọi AI. Ảnh lỗi có fallback vùng trang trí trung tính, mọi Settings vẫn dùng được.

## 5. Navigation

Tên route đề xuất: home → profile_settings → personality; profile_settings → privacy / about. Không đăng ký Profile thành top-level tab; Home vẫn có bốn tab theo hệ thống hiện tại. Chỉ avatar Home gọi navigation tới Profile; tap nhanh nhiều lần chỉ push một instance. System Back và app-bar Back cùng hành vi.

Back từ Profile trả Home đúng tab/context/scroll hiện có. Back từ Personality clean pop về Profile. Dirty gồm draft khác committed; chọn lại committed thì clean. Dirty Back mở confirm; Keep editing ở lại; Discard bỏ draft và pop. Predictive Back commit phải đi qua cùng guard; không pop trước rồi hỏi. Save thành công đề xuất pop về Profile và cập nhật row từ kết quả server. Khi saving, Back không được làm mất kết quả request; đề xuất giữ màn với progress cho đến terminal state hoặc cơ chế cancellation đã được repository hỗ trợ rõ.

Privacy/About là child route hoặc nội dung browser phù hợp quy ước dự án, không tạo tab. Không chặn read-only chỉ vì offline nếu nội dung đã đóng gói/cache hợp lệ. Nếu legal URL chưa xác minh, ghi dependency trong báo cáo; không đưa link mẫu vào release. Notification deep link từ thông báo review thuộc luồng item/review riêng, không làm Profile thành review dashboard.

## 6. State và data flow

ProfileUiState tối thiểu (khái niệm, không phải schema API): account identity; committedPersonality; notification appPreference + permission + channel state; connectivity; cache freshness; load error; signOut progress. Chỉ render email từ account đã xác thực, cache phân tách account. Không cho email/token vào route, analytics hoặc log.

Đọc: vào màn hiển thị cache nếu có rồi revalidate; no-cache loading dùng skeleton trung tính; lỗi không cache có Retry. Refresh lỗi giữ dữ liệu cũ và banner stale, không biến thành màn trống. Session invalid chuyển qua auth coordinator; không rơi về tài khoản khác. Các row Privacy/About vẫn hoạt động khi profile load lỗi nếu nội dung đủ.

PersonalityUiState: loading / ready-clean / ready-dirty / saving / save-error / conflict / offline / session-expired. Committed là dữ liệu server; draft là lựa chọn chưa lưu. Sassy chỉ là default sản phẩm khi tài khoản mới/chưa có giá trị được contract xác nhận; không dùng default để che lỗi đọc.

| Trigger | Hành vi UI | Kết quả có thẩm quyền |
|---|---|---|
| Open Personality offline | Hiển thị giá trị cache + thông báo cần mạng; khóa chọn/preview action/Save | Không mutation, không outbox |
| Select online | Đổi draft và preview tĩnh | Không lưu |
| Save clean | Disabled | Không request |
| Save dirty online | Pending, chặn duplicate; giữ draft | Chỉ cập nhật committed sau response server |
| Mất mạng trước Save | Khóa Save, giữ draft trong phiên | Không tự gửi khi mạng trở lại |
| Mất mạng/timeout khi đang Save | Thông báo chưa xác nhận; đọc lại khi có mạng | Không suy là thành công hoặc thất bại chắc chắn; đối soát server trước retry |
| Save validation/server error | Giữ draft; inline error + Retry chủ động | Committed giữ nguyên |
| Version conflict | Fetch current, giải thích đã thay đổi ở nơi khác; cho chọn lại rõ ràng | Không last-write-wins âm thầm |
| Session expired | Auth flow theo project; draft không sang account khác | Không replay Save dưới account mới |
| Reconnect | Revalidate read state, giữ draft trong phiên nếu account không đổi | Không auto-save |

Draft có thể giữ qua rotation/process recreation bằng cơ chế UI-state account-scoped; đây không phải hàng đợi mutation. Khi restore phải đối soát committed/version mới. Retry Save dùng contract chính thức; không tự đặt endpoint/DTO/idempotency behavior. TECH-02 chỉ có cột personality đề xuất; TECH-03 chưa cung cấp contract preferences cụ thể trong nguồn đã đọc.

## 7. Personality preview và copy

Friendly: “Let’s take a moment and see if this fits your plans.”
Sassy (default): “Cute find. Let’s give your wallet a second opinion.”
Savage: “The cart can wait. Let’s check whether this earns its place.”

Đây là **copy đề xuất**, cùng ngữ cảnh chung, không chứa verdict/giá/ngân sách bịa đặt; không shaming, xúc phạm thu nhập hay chẩn đoán. Preview không chạy evaluation và không viết lại lịch sử chat. Save chỉ đổi presentation của phản hồi tương lai theo contract; không sửa verdict cũ hoặc logic quyết định. Ngôn ngữ app hiện tại ưu tiên theo dự án; mọi text phải vào resources để hỗ trợ localization.

## 8. Ready for Review notification

Chỉ một loại notification preference trong màn này, không thêm review list/countdown, budget alerts, marketing, daily reminder hoặc master notification switch ngoài yêu cầu. Không thay deadline, item state hoặc quyết định mua khi bật/tắt. Theo baseline, tối đa một reminder/period nếu được phép; tắt rồi bật không được nhân đôi hay gửi dồn reminder cũ.

Đề xuất UX: On khi permission/channel cho phép và preference bật. Người dùng bật khi chưa có quyền: giải thích ngắn theo quy ước dự án rồi dùng platform permission flow nếu phiên bản Android yêu cầu. Denied: hiện trạng thái “Notifications are blocked” và action mở system settings khi thích hợp, không xin quyền lặp liên tục. App/channel bị tắt cần phản ánh đúng khi quay lại foreground. Permission là trạng thái hệ điều hành, không đồng nhất với preference server/device.

Dependency chưa xác minh: preference lưu theo account hay device, scheduler/backend delivery, channel ID, Android target SDK, quy tắc reminder khi đổi máy/sign out. Coding agent cần nối vào notification system có sẵn, không tạo scheduler song song. Khi offline, chỉ cho đổi nếu contract xác nhận preference local; nếu chưa rõ hiển thị unavailable/cần mạng thay vì giả save thành công.

## 9. Sign Out và AUTH-04

**Chưa đọc được AUTH-04; đây là release gate riêng.** Không tự quyết offline logout, xóa cache, draft/ảnh draft, analytics hoặc chọn account Google. Không coi sign out là xóa tài khoản hay revoke quyền Google toàn cục.

Đã xác minh TECH-03: POST /auth/logout thu hồi phiên hiện tại, idempotent; server chặn session revoked. Token không lưu Room; cache phân tách theo account. Đây chỉ là phần kỹ thuật hỗ trợ, không đủ thay AUTH-04.

UI đề xuất để review với AUTH-04: confirm Sign out / Cancel; pending disable repeat; thành công reset authenticated back stack tới login; Back không vào được dữ liệu cũ. Lỗi không báo đã sign out nếu contract chưa hoàn tất. Nếu AUTH-04 cho phép local sign out khi offline/revocation fail, phải có chính sách rõ cho revoke pending và secure cleanup; không tự thêm outbox. Giữ draft cho đến khi chính sách đã được xác minh; ngăn lộ dữ liệu giữa tài khoản là bắt buộc. Agent báo blocked chỉ phần Sign Out chưa rõ, vẫn hoàn tất các phần độc lập.

## 10. Acceptance criteria

| ID | Given / When | Then |
|---|---|---|
| AC-01 | Từ Home tap avatar / tap liên tiếp | Mở đúng một Profile; Settings icon trên Home không còn |
| AC-02 | Profile hiển thị / Back | Không bottom nav; Back về Home giữ context |
| AC-03 | Cozy Corner tap nhiều lần | Một decorative reaction; không request AI, không mutation, không route mới |
| AC-04 | Reduce motion bật | Ảnh tĩnh, không mất chức năng Settings |
| AC-05 | Open Personality online | Màn riêng, đúng committed mode; Sassy default chỉ đúng account mới |
| AC-06 | Chọn mode khác | Preview đổi; Profile/server chưa đổi trước Save |
| AC-07 | Back khi dirty; chọn Keep/Discard | Keep giữ draft; Discard bỏ draft; system/app-bar Back giống nhau |
| AC-08 | Chọn lại committed rồi Back | Không confirm thừa |
| AC-09 | Save success / double tap | Một intent lưu; row Profile phản ánh server; không thay verdict/history |
| AC-10 | Offline/mất mạng/conflict/500 | Không save giả; draft giữ trong phiên; lỗi có retry/reconcile, không auto replay |
| AC-11 | Google Account | Đúng email account; không edit/chevron/unlink/change-account |
| AC-12 | Toggle Ready for Review | Chỉ đổi notification preference; permission denied rõ, không đổi cooling-off/item |
| AC-13 | Bật/tắt reminder nhiều lần | Không duplicate reminder/period; không gửi dồn cũ trái contract |
| AC-14 | Privacy/About | Nội dung/link thật; version lấy build; Back đúng |
| AC-15 | Sign Out | Có evidence AUTH-04; token/session/cache/draft/backstack đúng policy, không lộ chéo account |
| AC-16 | Font 200%, 320dp width, email dài | Chữ không bị cắt, scroll tới Sign Out/Save, touch ≥48dp, system bars không che |
| AC-17 | TalkBack / keyboard focus | Thứ tự tự nhiên; radio/switch có state; dialog focus đúng; illustration không đọc dư |
| AC-18 | Ảnh/Google avatar lỗi | Settings dùng được, không layout jump nghiêm trọng, không thay mèo bằng mascot khác |
| AC-19 | Process recreation / account đổi | Khôi phục UI hợp lệ theo account, không replay mutation/draft sang account khác |
| AC-20 | Visual QA | Nền cream, brand purple, Nunito; alpha sạch; đối chiếu mockup thật trước tuyên bố pixel-match |

Kiểm thử implementation nên tập trung state reducer/navigation guard, save duplicate/reconcile, permission matrix, account isolation và AUTH-04. Compose UI test cho các hành vi AC quan trọng; kiểm tra thủ công accessibility, font scale, alpha trên cream/white/pastel và visual diff. Chưa chạy các test này vì handoff không triển khai ứng dụng.
