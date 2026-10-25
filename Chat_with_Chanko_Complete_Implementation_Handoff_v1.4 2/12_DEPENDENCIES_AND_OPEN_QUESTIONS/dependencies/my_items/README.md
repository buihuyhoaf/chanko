# Chanko • Android My Items & Item Detail • v1.0

Bản hoàn thiện theo Approved UX Decisions — Recovery Baseline do chủ sản phẩm cung cấp ngày24/10/2026. Phạm vi: tài liệu và assets, 4 màn + 3 sheets. Không sửa ứng dụng, không PR, không deploy.

## Bắt đầu

1. Đọc01 UX baseline và02 state matrix; UI Approved khác với API/schema chưa xác nhận.
2. Dùng03 cho MyItems, ItemDetail, TransactionDetail, ArchivedItems và Filter & Sort, More Options, Record a Purchase.
3. Dùng04 cho acceptance/edge cases,05 và manifest cho assets,06 làm prompt coding agent.
4. 07 map đủ29 quyết định;08 là báo cáo thay đổi so v0.9 và blockers còn lại;09 là evidence.
5. `sources/approved_ux_recovery_baseline.md` giữ nguyên nguồn khôi phục. Snapshot Drive nội bộ trong sources; không ship APK/public.
6. `qa/v0.9_to_v1.0.diff`, `qa/bundle_integrity.json`, `qa/checksums.sha256` phục vụ kiểm tra tài liệu/ZIP, không chứng nhận runtime app.

## Nguồn và hiệu lực

Approved Recovery Baseline là nguồn quyết định UX được user cung cấp trực tiếp, đủ ITEM01–07, DETAIL01–14, EDGE01/03/04/07 và GAP01–04. Không còn blocker thiếu nội dung29 mã. Functional Specification v1.1/TECH-02 là chuẩn backend; phụ lục có hiệu lực17/10/2026 ưu tiên các đoạn cũ. Đã fetch lại cả2 từ Drive sau khi nhận baseline, nguồn không thay đổi. Design System v1.0 dùng snapshot đã đối chiếu Drive trong lần review trước.

Ready tạm thời offline là UI theo EDGE-01, không chuyển state server hoặc tạo transaction. Skip Waiting là kết thúc phiên và mở lựa chọn, không tự Skip item. Postponed có nhánh Considering + event, không thêm ItemStatus.

Approved không đồng nghĩa contract đã implement: persistence/clear marker của Ready/Postponed, archive khi ACTIVE và quyền ghi archived còn cần contract cụ thể (08). Không chặn archive ACTIVE hoặc bắt restore-trước-edit như một policy đã được duyệt.

## Assets và mức sẵn sàng

Product Hero/preview dùng dummy trung tính trong bundle, không mèo trong/cạnh/đè ảnh, không túi/tai nghe/giày. **Cần asset mèo trắng Chanko canonical**, gồm Empty State Chanko + hộp kẹo; không tự thay mèo khác. Thiếu thì giữ text/slot trống cho review, không gọi đó là visual hoàn chỉnh. Nunito/Material Symbols chưa bundle; dùng resource có quyền của repo.

Bộ bàn giao đã hoàn thiện nội dung theo baseline, đủ để triển khai phần đã có contract. Các backend policy còn mở và visual canonical chưa đủ điều kiện nghiệm thu cuối. Không có repo Android ở workspace nên chưa build/test runtime. Bản ZIP v1.0 trước recovery được giữ riêng dưới tên `...v1.0_pre_recovery.zip`; ZIP v1.0 hiện tại là bản mới cần dùng.
