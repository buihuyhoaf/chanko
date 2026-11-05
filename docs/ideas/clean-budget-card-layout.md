# Tinh gọn thông tin và chuẩn hóa hiển thị Thẻ Ngân Sách Carousel

## Problem Statement
Làm thế nào để Thẻ Ngân Sách trên Home truyền tải tức thì số dư khả dụng trong 1 giây lướt nhìn, loại bỏ toàn bộ thông tin trùng lặp, trong khi vẫn giữ trọn vẹn thẩm mỹ thẻ tín dụng Candy Pop độc đáo (Chip EMV, sóng Contactless, linh vật Chanko)?

## Recommended Direction
Triển khai phương án **"The Balanced Clean Card"**:
- **Gộp thông tin Chu kỳ & Trạng thái vào TopBar:** Sử dụng `BudgetCycleBadge` tích hợp `cycleLabel` (ví dụ: `Tháng 10/2026 · ĐANG DIỄN RA`, `Tháng 11/2026 · SẮP TỚI`, `Tháng 09/2026 · ĐÃ KẾT THÚC`) đặt cạnh cụm thương hiệu thẻ.
- **Tập trung số dư còn lại (Hero):** Giữ số tiền còn lại lớn (32sp ExtraBold) làm thông tin trung tâm thu hút ánh nhìn đầu tiên.
- **Tinh giản chân thẻ:** Loại bỏ hoàn toàn dòng icon lịch và ngày tháng dài lặp lại ở đáy thẻ (`01/10/2026 – 31/10/2026`). Chuyển dòng tóm tắt chi tiêu sang dạng văn bản phụ thanh thoát `Đã chi: X / Y`, bỏ icon rườm rà.
- **Bảo toàn chi tiết đồ họa thẻ tín dụng:** Giữ nguyên Chip vàng EMV, sóng Contactless, nhãn `CHANKO SHOPPING BUDGET`, viền nổi 3D và linh vật Mèo Chanko.

## Key Assumptions to Validate
- [x] Người dùng nhận biết chu kỳ qua tên tháng (`Tháng 10/2026`) trên Badge TopBar mà không cần chuỗi ngày chi tiết trên mặt thẻ.
- [x] Số tiền còn lại là số liệu quan trọng nhất cho quyết định chi tiêu hàng ngày.
- [x] Bỏ bớt 2 dòng icon và ngày tháng ở đáy thẻ giúp thẻ thoáng hơn mà không làm mất thông tin cần thiết (ngày tháng chi tiết xem tại màn Budget/History).

## MVP Scope
- Cập nhật `BudgetCycleBadge.kt`: Hỗ trợ tham số `cycleLabel: String? = null` để hiển thị nhãn gộp `"$cycleLabel · ${stringResource(labelRes)}"`.
- Cập nhật `BudgetCreditCard.kt`:
  - `ActiveCardContent`: Gộp Badge TopBar, lược bỏ icon và dòng ngày tháng ở đáy, tinh gọn dòng `spentSummaryText`.
  - `ClosedCardContent`: Tương tự `ActiveCardContent`.
  - `UpcomingConfiguredCardContent`: Gộp Badge TopBar, lược bỏ dòng ngày tháng ở đáy.
- Cập nhật unit test và chạy toàn bộ kiểm thử xác thực.

## Not Doing (and Why)
- **Không bỏ các chi tiết thẻ tín dụng (Chip EMV, Sóng Contactless, Brand Title):** Giữ bản sắc thiết kế đồ chơi Candy Pop của Chanko.
- **Không đưa ngày tháng chi tiết (dd/MM/yyyy - dd/MM/yyyy) lên mặt thẻ Carousel:** Tránh làm chật chội màn hình Home; chi tiết ngày tháng thuộc về màn hình chi tiết ngân sách.
- **Không đảo "Đã chi" làm số chính:** Quyết định chi tiêu phụ thuộc vào "còn bao nhiêu tiền để tiêu".

## Open Questions
- Không có. Thiết kế đã được chốt và thống nhất.
