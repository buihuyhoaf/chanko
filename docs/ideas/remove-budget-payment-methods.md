# Loại bỏ Quản lý Thẻ tín dụng & Section Phương thức thanh toán khỏi Budget Screen

## Problem Statement
How Might We tinh gọn màn hình Ngân sách (Budget) bằng cách loại bỏ catalog thẻ tín dụng mô phỏng không liên kết, tập trung hoàn toàn vào việc quản lý hạn mức, chu kỳ chi tiêu và lịch sử điều chỉnh ngân sách mà không làm gián đoạn luồng mua hàng và dữ liệu lịch sử?

## Recommended Direction
- Loại bỏ hoàn toàn Section "Phương thức thanh toán" và các Dialog thêm/sửa/xóa thẻ trong `BudgetScreen.kt`.
- Tách `NotableItem.kt` thành Domain Model riêng; loại bỏ `PaymentMethodBreakdown` khỏi `BudgetCycleOverview` và `BudgetUiState`.
- Giữ nguyên `Screen.WalletSettings` phục vụ điều hướng tương thích ngược (mở Budget / Lịch sử).
- Bảo toàn tuyệt đối `BudgetCreditCard` trên Home, luồng ghi nhận mua sắm `RecordPurchaseBottomSheet` (PAY_NOW / CREDIT), Room Database và converters.

## Key Assumptions to Validate
- [ ] Logic Discard Changes của `EditBudgetSheet` hoạt động độc lập và chính xác sau khi gỡ bỏ drafts quản lý thẻ.
- [ ] Luồng load `BudgetCycleOverview` ở ViewModel tải nguyên tử dữ liệu ngân sách, snapshot và notable items khi không còn breakdown phương thức thanh toán.
- [ ] Dữ liệu giao dịch cũ có `PaymentType.CREDIT` và `cardLabel` vẫn hiển thị đúng trên TransactionDetailScreen.

## MVP Scope
- **Xóa UI components**: `PaymentMethodsSection.kt`, `CardPreview.kt`, `AddCreditCardDialog.kt`, `RenameCreditCardDialog.kt`, `DeleteCardConfirmDialog.kt`.
- **Dọn dẹp State/Event**: Gỡ bỏ các event/state/effect liên quan đến thẻ khỏi `BudgetViewModel`, `BudgetUiState`, `BudgetUiEvent`.
- **Dọn dẹp Domain/Repo**: Xóa `getCreditCards`, `addCreditCard`, `renameCreditCard`, `archiveCreditCard`, `getPaymentBreakdown` trong `BudgetRepository` và `FakeBudgetRepository`. Xóa `CreditCard.kt`, `CreditCardUnavailableException`.
- **Tách model**: Tạo riêng `NotableItem.kt`.
- **Cập nhật Unit Tests**: Bỏ `CreditCardManagementTest.kt`, cập nhật `BudgetViewModelTest.kt` và `FakeBudgetRepositoryTest.kt`.

## Not Doing (and Why)
- **Không đổi tên hay xóa route `Screen.WalletSettings`**: Tránh gây vỡ các điểm gọi điều hướng hoặc deep link hiện có; việc hợp nhất route sẽ thực hiện trong một tác vụ điều hướng riêng.
- **Không thay đổi `RecordPurchaseBottomSheet` hay schema Database**: Luồng phân loại giao dịch (Trả ngay / Thẻ tín dụng) là dữ liệu cốt lõi của giao dịch và tiếp tục được lưu trữ bình thường.
- **Không can thiệp vào `BudgetCreditCard` / Carousel ở Home**: Đây là thành phần hiển thị chu kỳ ngân sách, không phải thẻ thanh toán.
- **Không tự ý commit, push hay tạo PR**: Theo đúng nguyên tắc làm việc đã quy định.

## Open Questions / Pending Items
- Backend Chanko thật hiện chưa có repository/source trong dự án (Android đang bind `FakeBudgetRepository`), cần lưu ý kiểm tra tương thích API khi tích hợp backend production trong tương lai.
