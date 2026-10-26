# QA tài liệu và asset — CHAT-UX-REV-01
25/10/2026. QA này kiểm bộ bàn giao, không chứng nhận app Android đã chạy.

## Đã kiểm
- Đã đọc mockup user gửi và canonical cat; giữ ảnh gốc nguyên bytes, SHA256 tại source_hashes.json.
- Đã xem bản imagegen hiệu chỉnh: center verdict dùng lavender, “Có thể cân nhắc mua”, canonical-style crossed-arms neutral, không sparkle/green success trong verdict. Hero CL vẫn giống source. Ảnh hiệu chỉnh là visual reference, không dùng mascot re-render thay canonical.
- Đã render/xem3 reference Android dùng Nunito thật. `layout_probe.json` ghi fontLoaded=true và không document horizontal overflow ở390×844 và320×844 với font scale200%; vùng content scroll được. Đây là HTML layout probe, chưa là native Android/TalkBack pass.
- Reference CL/NC không textarea; CD có một textarea/composer. Buttons/search ở HTML là visual-only, không giả runtime interaction.
- Đã kiểm neutral Decision Card ở reference Android: text có điều kiện, không budget claims, không green checkmarks/sparkles, CTA Xem món đồ. Product media dùng inventory placeholder, không mascot.
- Đã kiểm đủ34 provenance rows PARTIAL và12 revision mapping riêng; không nâng VERIFIED hoặc chỉnh TECH source.
- Contrast: 12 semantic pairs đạt ngưỡng tính toán; white-on-brand bị REJECTED và không dùng trong reference Android. Native gradient sampling/focus/disabled variants còn phải kiểm sau implementation.
- Đã đóng gói 54 actual assets (canonical variants, icons SVG/PNG, decorations, font); licenses kèm, manifest có SHA256/bytes/dimensions/source/usage. Master canonical được giữ nguyên, WebP là lossless format conversion.

## Giới hạn và nghiệm thu tiếp
Acceptance40 scenarios đều NOT_RUN_NO_ANDROID_IMPLEMENTATION. Không chạy API, DB migration, TalkBack, keyboard/insets native hoặc multi-device/runtime. HTML font200 chỉ kiểm wrap/scroll và ảnh review. RENAME-API và runtime evidence là integration gates; provenance34 là archival gap, không cản bàn giao tài liệu hiện tại. Bản corrected/reference mới không được user approve riêng sau xuất.

Không Android code hoặc PR. Không ghi vào Documents/ChatGPT/Chanko hay local ChatGPT project. File xử lý ở /tmp, final ở Drive. Inventory/Drive readback được ghi riêng sau upload để tránh tự nhận đã upload khi chưa có xác nhận connector.
