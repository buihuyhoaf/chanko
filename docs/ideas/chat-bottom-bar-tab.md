# Đưa Chat xuống Bottom Bar & Tinh giản Home Header

## Problem Statement
Làm thế nào để đưa tính năng trò chuyện cùng AI Chanko thành điểm chạm điều hướng chính ở Bottom Bar, thay thế tab Ngân sách (chưa phát triển), đồng thời loại bỏ icon chat trên Header màn hình Home để giao diện gọn gàng và tập trung hơn?

## Recommended Direction
Chúng ta sẽ chuyển tính năng Chat từ một nút phụ trên `HomeHeader` thành một `TopLevelDestination` chính trên Bottom Navigation Bar của ứng dụng Chanko.

Cụ thể, tab **Ngân sách (Budget)** (hiện đang là stub) sẽ được thay thế bằng tab **Trò chuyện (Chat)** với icon `ChatBubbleOutline`. Cấu trúc Bottom Bar vẫn giữ nguyên quy tắc 4 tab cân xứng xung quanh nút trung tâm Floating Action Button (+):
1. **Trang chủ (Home)**
2. **Món đồ (MyItems)**
3. `[+]` Phím tắt tạo nhanh
4. **Trò chuyện (Chat)** (kế bên nút `+`)
5. **Phân tích (Insights)** (ngoài cùng bên phải)

Trên màn hình Home, icon Chat ở góc trên bên phải cạnh avatar sẽ được loại bỏ hoàn toàn. Góc trên bên phải của Header giờ đây chỉ hiển thị duy nhất nút Avatar Profile, đảm bảo bố cục thoáng đãng và phân định rạch ròi: Header dành cho thông tin định danh & trạng thái; Bottom Bar dành cho toàn bộ điều hướng các luồng tính năng.

Khi người dùng nhấn vào tab **Trò chuyện**, ứng dụng sẽ chuyển tới màn hình danh sách hội thoại (`ChatListScreen`) với đầy đủ lịch sử tư vấn và nút tạo cuộc trò chuyện mới.

## Key Assumptions to Validate
- [ ] Người dùng có nhu cầu truy cập Chat thường xuyên hơn Ngân sách ở giai đoạn này và cảm thấy tự nhiên khi Chat nằm ngay cạnh nút (+) ở Bottom Bar.
- [ ] Loại bỏ icon Chat trên Home Header không làm giảm tần suất sử dụng tính năng Chat nhờ độ nổi bật của tab trên Bottom Bar.
- [ ] Trải nghiệm giữ lại Bottom Bar khi duyệt danh sách chat giúp người dùng dễ dàng chuyển đổi qua lại giữa danh sách đồ cần mua và cuộc trò chuyện tư vấn.

## MVP Scope
- **Gỡ bỏ Chat ở Home Header**: Loại bỏ nút Chat (`ChatBubbleOutline`) và vùng chạm tương ứng trong `HomeHeader.kt`, chỉ giữ lại Avatar Profile. Cập nhật `HomeScreen` và `HomeRoute` tương ứng.
- **Cập nhật TopLevelDestination**: Thêm/thay thế `TopLevelDestination.Chat` (route: `"chat"`, nhãn: `"Trò chuyện"`, icon: `ChatBubbleOutline`) thay cho `Budget`.
- **Cập nhật ChankoBottomBar & MainNavigation**:
  - Gán tab thứ 3 cho `TopLevelDestination.Chat`.
  - Tích hợp `ChatListScreen` vào NavHost của `MainNavigation` với các callback chuyển tiếp sang `ChatDetailScreen` và `NewChatScreen`.
- **Cập nhật Unit / Navigation Tests**: Đảm bảo toàn bộ test case liên quan đến `MainNavigationTest`, `HomeHeaderTest`, và các assertions không bị gãy.

## Not Doing (and Why)
- **Không giữ cùng lúc cả 5 tab trên thanh điều hướng**: Việc dồn 5 tab cùng với nút FAB 56dp ở giữa sẽ làm hẹp vùng chạm tối thiểu (touch target < 48dp) theo chuẩn Material 3 trên các màn hình hẹp.
- **Không mở trực tiếp ChatDetailScreen 1-1 khi nhấn tab**: Người dùng cần nhìn thấy danh sách các chủ đề tư vấn cũ để tiếp tục hoặc mở chat mới có chủ đích, tránh việc tự động tạo hội thoại rác.
- **Không can thiệp vào nghiệp vụ logic của Chat**: Giữ nguyên toàn bộ implementation hiện tại của `ChatListViewModel` và `ChatViewModel`.

## Open Questions
- Khi từ `ChatListScreen` nhấn vào một cuộc trò chuyện cụ thể (vào `ChatDetailScreen`), thanh Bottom Bar nên được ẩn đi để tối ưu diện tích bàn phím/soạn tin nhắn (đã hỗ trợ trong kiến trúc điều hướng cấp cao `AppNavGraph`).
