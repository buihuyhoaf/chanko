> **Cập nhật v3 — asset đã có:** Sáu PNG Living Room tái tạo đã được bàn giao tại `assets/living-room/`: background, mèo/đệm, hourglass/books, calendar trống, wallet và jar tim. Các câu “asset thiếu” trong tài liệu v2 dưới đây được thay thế bởi cập nhật này đối với sáu asset đó. Layer/source 3D gốc, license/provenance fonts cũ, TECH alignment và runtime/design QA vẫn chưa được xác nhận. Dùng PNG mới, không dùng mèo khoanh tay legacy. Mở `assets/living-room/PREVIEW.html` và đọc `11-ASSET-DELIVERY.md`.

# Phase 0 — Discovery & TECH alignment

Bạn được giao triển khai Phase 0 của Home Chanko trong repository đích đã được người dùng chỉ định. Nếu chưa có repo đích/quyền thay đổi, chỉ hoàn thành kế hoạch mapping và báo thiếu thông tin; không chọn checkout tùy tiện.

## Visual chuẩn bắt buộc

Mở `references/approved-home-user-reference.png` và đọc `00-VISUAL-REFERENCE.md`. Bám ảnh người dùng, không ảnh v5 lịch lớn. Dialogue là bubble bên trong Living Room. Mèo nằm chống cằm trên đệm tím; bốn prop đúng vị trí. Không dùng pose khoanh tay legacy làm final. Header có subtitle; hai Home action cards và sheet action tiles có icon/style khác nhau như ảnh.

## Đọc trước

README.md; START-HERE.md; 01-APPROVED-UX-SPEC.md; 02-COMPONENT-CONTRACTS.md; 09-PENDING-TECH-ALIGNMENT.md. Đọc AGENTS.md của repo đích và các hướng dẫn áp dụng. User request/spec hiện tại ưu tiên hơn layout lịch lớn trong ảnh legacy.

## Phạm vi

Contracts, nguồn chuẩn, mapping repo.

Khảo sát repo đích chỉ khi đã được giao triển khai; lập map module/design system/navigation/data có bằng chứng. Chốt status mapping, rank toàn cục, budget/date, sheet routes, asset ownership. Ghi unknown và owner, không đoán file path.

## Không thay đổi

Giữ Header có dòng phụ → Compact Living Room (mèo trắng 3D + Hourglass/Calendar/Wallet/Pause Jar + Dialogue bên trong scene) → Quick Check + Save Item → Budget → Your Decisions tối đa 3, ưu tiên Ready→Waiting→Considering→Saved → optional Observation/Draft; bốn tab + action trung tâm mở Global Quick Action; Candy Pop/Nunito. Không tự thêm nghiệp vụ, route, dependency hoặc asset path không có chứng cứ.

## Deliverables

Decision log + mapping các component vào kiến trúc thực + blocker list.

## Kiểm chứng / exit gate

Không sửa UX; mọi quyết định blocking có owner và trạng thái; không tự nhận backend đã có.

Chọn test liên quan phase từ 07-PREVIEWS-TESTS.md, báo rõ đã chạy/chưa chạy và bằng chứng. Không tạo emulator/AVD/VM; runtime chỉ thiết bị thật. Asset placeholder chỉ phục vụ preview và phải được gắn nhãn; không báo visual final. Không sửa sources/ synced read-only.

## Báo cáo cuối phase

Nêu component hoàn thành, thay đổi thực và vị trí đã xác minh, test kết quả, screenshot/preview bằng chứng, các quyết định TECH mới, pending có owner, điều kiện phase sau. Không tuyên bố toàn Home xong chỉ vì phase pass.
