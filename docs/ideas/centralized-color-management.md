# Kế hoạch Gom Quản lý Mã màu Tập trung (Centralized Color Management)

## 1. Problem Statement
Làm thế nào để đưa toàn bộ các mã màu đang khai báo rải rác ngoài `Color.kt` về một nơi quản lý duy nhất, bảo toàn tuyệt đối 100% giá trị màu sắc và độ trong suốt (alpha) tại từng vị trí sử dụng, đồng thời thiết lập cơ chế kiểm thử tự động ngăn chặn tái xuất hiện `Color(0x...)` trong tương lai?

---

## 2. Số liệu Khảo sát Hiện trạng Codebase
*(Lấy mốc từ working tree hiện tại, bao gồm các file chưa commit như `Color.kt` và `UrlInputDialog.kt`)*

- **Tổng số lần gọi `Color(0x...)`:** **185 lần** trên **18 file**.
  - **Trong `ui/theme/Color.kt`:** **102 lần khai báo** (đang định nghĩa **107 thuộc tính** trong `CandyColors`).
  - **Tại 17 file còn lại:** **83 lần khai báo** (cần được đưa về quản lý tập trung).
- **Lưu ý quan trọng về số liệu:** Đây là **số lần khai báo** (declaration count), không phải số lượng màu duy nhất (unique colors). Rất nhiều mã hex đang bị khai báo lặp lại ở nhiều nơi.
- **Ngoại lệ màu động (Dynamic Runtime Colors):**
  - Có **4 vị trí** sử dụng hàm dựng từ dữ liệu runtime: `Color(color.colorLong)` (hoặc tương đương) tại các component hiển thị thẻ (`CardPreview.kt`, `PaymentMethodsSection.kt`, v.v.).
  - Các vị trí này đại diện cho màu động từ entity/model của người dùng, không phải mã hex cố định trên giao diện, và **nằm ngoài phạm vi** của đợt gom này (phù hợp với quyết định giữ nguyên `CreditCard.CardColor` ở Domain).

---

## 3. Kiến trúc Đề xuất (2 Tầng & Độc lập Ngữ nghĩa)

Triển khai cấu trúc màu trong file duy nhất: `app/src/main/java/com/hoabh/chanko/ui/theme/Color.kt`:

### 3.1. Tầng 1: `private object CandyPalette` (Kho màu thô)
- Chứa các hằng số màu gốc định dạng `Color(0xFF...)`.
- **Nguyên tắc bất biến:** Mỗi giá trị hex duy nhất chỉ được khai báo **ĐÚNG 1 LẦN** trong `CandyPalette`.
- Đặt tên theo họ màu và sắc độ trung tính (ví dụ: `Purple600`, `Lavender50`, `Slate900`, `Emerald500`), không đặt tên gắn liền với một màn hình cụ thể.

### 3.2. Tầng 2: `object CandyColors` (Token ngữ nghĩa)
- **Bảo toàn tương thích ngược:** Giữ nguyên 107 thuộc tính hiện có ở cấp gốc của `CandyColors` để không làm xáo trộn các màn hình đã ổn định (`HomeScreen`, `ChankoTheme`, `Type.kt`, v.v.). Các thuộc tính này sẽ được trỏ vào các hằng số tương ứng trong `CandyPalette`.
- **Phân vùng ngữ nghĩa bằng Sub-objects:**
  - `CandyColors.Wallet.*`: Màu đặc thù cho cụm Ngân sách & Ví (các thẻ, dialogs, timeline lịch sử).
  - `CandyColors.Chat.*`: Màu đặc thù cho cụm Trò chuyện (bong bóng chat, sheet tùy chọn, nhãn ngữ cảnh).
  - `CandyColors.UrlImport.*`: Màu đặc thù cho modal nhập URL (`UrlInputDialog.kt`).
  - `CandyColors.Auth.*`: Màu đặc thù cho Luồng đăng nhập & Thiết lập ban đầu (`LoginScreen.kt`, `SetupTheme.kt`).
  - `CandyColors.Status.*`: Màu đặc thù cho các huy hiệu trạng thái (status badges, chips, item context).
- **Nguyên tắc Độc lập Ngữ nghĩa:**
  - Hai token có ngữ nghĩa khác nhau (ví dụ: nền bong bóng tin nhắn và nền ô lịch) dù ở thời điểm hiện tại có cùng mã hex `#FAF7FF`, vẫn phải được khai báo thành 2 token riêng biệt cùng trỏ vào `CandyPalette.Lavender50`.
  - Điều này đảm bảo sau này khi một màn hình thay đổi thiết kế màu sắc, màn hình còn lại hoàn toàn không bị ảnh hưởng phụ (no side-effects).

---

## 4. Tiêu chí Hoàn thành (Definition of Done)

1. **Tuyệt đối không còn mã màu trực tiếp:** Không còn bất kỳ khai báo `Color(0x...)` nào trong toàn bộ hai thư mục `presentation/` và `ui/` (ngoại trừ bên trong `Color.kt`). Tất cả mapping trạng thái phải dùng token ngữ nghĩa (ví dụ `CandyColors.Status.*`), không có ngoại lệ.
2. **Không trùng lặp trong Palette:** Trong `CandyPalette`, không có bất kỳ giá trị màu hex nào được khai báo quá 1 lần.
3. **Bảo toàn 100% giá trị màu hiển thị:** Mọi vị trí thay thế phải được đối chiếu 1:1 qua Bảng Checklist Đối chiếu (Audit Checklist), bảo đảm khớp tuyệt đối:
   - Mã màu Hex (RGB).
   - Độ trong suốt Alpha (bao gồm cả `.copy(alpha = ...)`).
   - Thứ tự màu trong mảng gradient (Brush).
4. **Guard Test tự động hoạt động nghiêm ngặt:**
   - Bộ Unit Test (`./gradlew testDebugUnitTest`) có thêm `ColorArchitectureTest.kt` nhằm **ngăn tái xuất hiện `Color(0x...)`**.
   - Kiểm tra đệ quy cả hai thư mục `presentation/` và `ui/`.
   - Regex nhận diện chặt chẽ: `\bColor\s*\(\s*0[xX]`, bắt cả trường hợp có khoảng trắng trước dấu ngoặc `(` và xử lý cả `0x` / `0X`.
   - Xử lý bóc tách/bỏ qua comment (`//`, `/* ... */`) và chuỗi string literal.
   - **Fail-fast:** Test bắt buộc phải fail nếu thư mục nguồn không tồn tại hoặc không tìm thấy file nào để quét, chống hiện tượng false green.
5. **Build & Test xanh hoàn toàn:** Lệnh `./gradlew testDebugUnitTest` và `./gradlew assembleDebug` đều hoàn thành thành công (không cài đặt lên thiết bị/máy ảo).

---

## 5. Kế hoạch Triển khai (Thin Slices)

Quá trình refactor được chia thành các lát cắt mỏng độc lập, lấy mốc working tree hiện tại:

- **Lát cắt 1: Tái cấu trúc nền tảng tại `Color.kt`**
  - Tạo `private object CandyPalette`, chuẩn hóa các mã màu hiện có trong `CandyColors` về palette.
  - Khai báo trước các sub-object (`Wallet`, `Chat`, `UrlImport`, `Auth`, `Status`) trỏ vào palette tương ứng.
  - Verify build và unit test hiện tại.
- **Lát cắt 2: Dọn dẹp cụm Ngân sách & Ví (Wallet)**
  - Xử lý 4 file: `AdjustmentHistorySection.kt`, `BudgetSettingsSection.kt`, `AddCreditCardDialog.kt`, `RenameCreditCardDialog.kt`.
  - Giữ nguyên các lệnh gọi `Color(color.colorLong)` hợp lệ.
  - Đối chiếu bảng giá trị màu trước/sau.
- **Lát cắt 3: Dọn dẹp cụm Trò chuyện (Chat)**
  - Xử lý 7 file: `ChatListScreen.kt`, `ChatMessageBubble.kt`, `ChatDetailScreen.kt`, `NewChatScreen.kt`, `ChatDialogs.kt`, `ChatStatusComponents.kt`, `ThreadOptionsSheet.kt`.
  - Thay thế các màu cứng bằng `CandyColors.Chat.*` hoặc `CandyColors.Status.*`.
  - Đối chiếu bảng giá trị màu trước/sau.
- **Lát cắt 4: Dọn dẹp cụm Add Item & Url Dialog**
  - Xử lý file `UrlInputDialog.kt` (bảo toàn các thay đổi chưa commit hiện tại).
  - Thay các biến cục bộ `Ink`, `Purple`, `Outline` bằng `CandyColors.UrlImport.*`.
- **Lát cắt 5: Dọn dẹp cụm Auth & Setup**
  - Xử lý 2 file: `LoginScreen.kt`, `SetupTheme.kt`.
  - Thay thế các màu cục bộ bằng `CandyColors.Auth.*`.
- **Lát cắt 6: Dọn dẹp các vị trí lẻ còn lại trong `presentation/`**
  - Xử lý `MyItemsOverviewTiles.kt`, `CoolingOffCountdownCard.kt`.
- **Lát cắt 7: Tách Compose `Color` ra khỏi `MyItemsUiState.kt`**
  - Tách thành bước riêng biệt: chuyển `bgColor` và `textColor` từ enum state sang hàm UI helper/extension mapping sử dụng `CandyColors.Status.*`.
- **Lát cắt 8: Thiết lập `ColorArchitectureTest.kt`**
  - Viết test tự động với regex đa dòng, lọc comment/string, fail-fast khi thiếu thư mục.
  - Chạy test suite và assemble verify toàn diện.

---

## 6. Những điều KHÔNG làm (Not Doing)

- **Không tự ý gộp các mã màu lệch nhau (No normalization):** Các màu như `#151329` và `#24183E` dù nhìn gần giống nhau nhưng vẫn là 2 mã hex riêng biệt trong `CandyPalette` để tránh bất kỳ sự sai lệch nào về mặt thị giác.
- **Không can thiệp Domain Model (`CreditCard.CardColor`):** Giữ nguyên kiến trúc domain và cơ chế lưu trữ mã màu hex/Long của thẻ tín dụng.
- **Không triển khai `CompositionLocal` hay Multi-theme:** Giữ nguyên singleton `CandyColors` đơn giản, tránh trừu tượng hóa sớm khi chưa có yêu cầu về Dark mode.
- **Không coi Unit Test là bằng chứng thị giác duy nhất:** Việc bảo toàn giá trị màu được xác nhận bằng checklist đối chiếu tường minh; bước kiểm tra hiển thị thực tế trên thiết bị vật lý là một bước nghiệm thu độc lập sau đó.
