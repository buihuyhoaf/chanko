# Acceptance và blockers
40 scenario trong acceptance_checklist.csv là tiêu chí nghiệm thu cho phase triển khai. Tất cả runtime Android/integration tests là NOT_RUN_NO_ANDROID_IMPLEMENTATION. Không có Android code/PR trong đợt này.

## Release gates cho đội triển khai
Gate UI: đúng3 màn và composer placement, fixed footer/insets, Nunito/palette/radii, neutral BUY, native a11y/font scale. Gate state: phân biệt loading/empty/error/offline/cache/archived/unknown; no fourth main route. Gate domain: same-thread confirmed bind, official history độc lập, no money/status mutation từ advice. Gate integration: approved v1.4 contracts được deploy và test retry/version/lifecycle/account isolation. Demo UI có thể nghiệm thu riêng trước production adapter, không report demo như deployed.

## Unresolved
1. RENAME-API — dialog Rename có trong UX revision mới; baseline readable contract không cung cấp endpoint/field/capability/length/version semantics đã xác minh. Cần backend contract owner cung cấp/chốt trước wire production. Không tự sửa TECH hoặc đặt endpoint trong tài liệu này.
2. RUNTIME-EVIDENCE — chưa có app/repository/deployment trong scope, nên chưa chạy TalkBack/IME/Android lifecycle/multi-device/API tests. Không là blocker để bàn giao tài liệu/assets; là gate trước production integration.
3. PROVENANCE-34 — thiếu nguyên bản body/option/revision của CHAT01–20/GAP01–08/FINAL01–06. Giữ34 PARTIAL, approval ledger không bị hạ; archival source recovery chỉ nâng từng row đủ bằng chứng. Có thể triển khai current readable-source rules + REV01 mapping, không bịa34-body mapping.
4. SOURCE-PATH-PARITY — `/mnt/data` không tồn tại trên host; mascot đã lấy từ attachment thật và mockup đã được user gửi lại. Không thiếu visual source cho đợt này; chỉ không claim byte parity với hai path không truy cập được. Hash của bytes thực dùng ghi source_hashes.json.

Không còn blocker quyền Drive hoặc thiếu mockup/mascot. Chưa có user approval riêng cho bản hiệu chỉnh mới hoặc các dimension Reference; bản hiệu chỉnh thực hiện yêu cầu neutral BUY đã được giao. Không cần phê duyệt lại các baseline TECH hiện hữu.
