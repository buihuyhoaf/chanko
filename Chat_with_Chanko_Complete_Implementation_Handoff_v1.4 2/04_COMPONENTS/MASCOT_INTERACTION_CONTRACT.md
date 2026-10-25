# ChankoCompanion — asset, emotion, predefined reaction

Identity và11 core screen visual đã có approval; **reaction timing/defaults ở đây APPROVED A5-A**, không thay SPEC-07 OptionB/9 domain emotions. Dùng đúng8 supplied PNG/WebP; không yêu cầu 3D/Lottie master để bàn giao static component. Coding agent có thể tạo state transition bằng native UI sau khi được giao implementation; đợt này không có code/animation mới.

## Roles

Header/assistant/verdict/offline/error/archived mascots trong11 baseline là decorative, aspect-fit, không thêm focus hoặc tap action. BUY/WAIT/SKIP luôn Neutral kể cả domain emotion SUPPORTIVE/HAPPY; không confetti/sound/reward. Full hero vẫn có semantics trang; không bắt user hiểu emotion để dùng chức năng.

Nếu existing app companion surface đã hỗ trợ OptionB hoặc một nhiệm vụ riêng bật interaction, dùng predefined events bên dưới. Không tự thêm action vào product media hoặc một icon chưa có trong baseline. Không dùng Chat handoff để triển khai lại toàn bộ Home pet feature.

| Event được phép | Approved static pose | Predefined fixture text |
|---|---|---|
| Head tap | Curious | “Mình đang nghe đây.” |
| Belly tap | Relaxed | “Cứ từ từ suy nghĩ nhé.” |
| Repeated tap | Concerned rồi Neutral | “Cho mình nghỉ một nhịp nhé.” |
| Idle companion reaction | Neutral/Relaxed | Không tự mở chat/call provider |
| Supportive dialogue | Encouraging | “Bạn có thể dành thêm thời gian.” |

Text là canned fixture, không nhận định ngân sách/thực trạng không có dữ liệu. Không thay verdict hoặc thực hiện item/purchase/decision action. Không virtual-pet unlock/toy/drag/rotate theo OptionC.

Approved timing: crossfade150–200ms, static reaction tối đa1.200ms rồi về state thực, throttle tap reaction800ms; reduced motion thì đổi static ngay, không bounce/loop/sound. State pending/error/lifecycle có ưu tiên cao hơn canned reaction; không dùng Thinking nếu không có operation thật. Sound mặc định không autoplay; optional preference theo existing approved app settings, không bundle audio asset bắt buộc mới.

Interactive surface cần48dp-min target và accessible button action; head/belly areas chỉ khi từng vùng có đủ48dp, nếu không dùng một predefined default accessible action/các action có label của platform, không ép focus vào vùng32dp avatar. Decorative slots contentDescription=null. Labels ngắn, không announce mỗi frame/tap-loop.

[Emotion fallback](../08_ASSETS/EMOTION_PRESENTATION.md) giữ whitelist9 domain names, unsupported→Neutral/omit decoration; đủ8 presentation assets không đồng nghĩa tạo emotion enum thứ10. Runtime tests UI-P04 xác minh no-LLM/no-business-write, reduce-motion, tap throttling và semantics; không là điều kiện để file assets tồn tại.
