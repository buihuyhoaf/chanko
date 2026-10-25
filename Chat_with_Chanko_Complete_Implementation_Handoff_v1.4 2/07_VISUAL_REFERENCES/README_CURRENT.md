# Visual baseline chính thức — v1.2, hiệu lực tại handoff v1.3

11 core S01–S11 PNG là **USER_APPROVED_OFFICIAL_VISUAL_BASELINE**, thay visualv0.2 theo chỉ thị trực tiếp mới nhất. Không còn blocker original-v0.2/parity hoặc việc tìm source v0.2. [Approval record](../14_APPROVALS_AND_RECOVERY/USER_VISUAL_APPROVAL.md), [11 exact hashes](VISUAL_BASELINE_APPROVAL.json), [visual inventory](visual_manifest.csv).

Giữ nguyên byte core PNG, editable HTML/CSS và resources từ ZIPv1.2. Tên file `_canonical_reconstruction.png` là lịch sử tạo ảnh, không phủ nhận approval mới.9 domain emotions/8 presentation poses không đổi. Header và verdict dùng Neutral, offline/history empty Relaxed, loading Thinking, error Concerned, archived Sleeping. [Slot mapping](../08_ASSETS/screen_asset_mapping.csv) là exact file paths/hashes.

4 extra PNG [pre-verdict](S03_PRE_VERDICT.png), [WAIT](S03_WAIT.png), [SKIP](S03_SKIP.png), [history empty](S04_EMPTY.png) là supporting references của states đã có, không được tự gắn approval riêng từ câu11 core PNG. Implement business-whitelisted verdict states theo component chung; không tự thêm UX state hoặc database enum.

[Source HTML/CSS](source/S01.html) và [render harness](../10_ACCEPTANCE_QA/tools/README.md) là tài liệu tham chiếu, không app code. Use native text/controls; không crop screen raster làm app.1 CSS px→1 proposed dp ở scale1 là convention tham chiếu, không kết quả Android runtime. Các label Bản mẫu thể hiện fake data mode, không cho phép gọi fixture thành AI thật. Production copy/capability phải truthful theo accepted backend.

Historical user board/SVG schematics giữ làm provenance, không current target. [Coverage](STATE_COVERAGE.md), [reference QA](VISUAL_QA.md). Runtime sau implementation không ảnh hưởng độ đầy đủ asset/documents.
