# Tasks: Sticky & Collapsing Home Header

> **Plan tham chiếu:** [tasks/plan.md](plan.md) & [docs/plans/sticky-collapsing-home-header-plan.md](../docs/plans/sticky-collapsing-home-header-plan.md)  
> **Ý tưởng tham chiếu:** [docs/ideas/sticky-collapsing-home-header.md](../docs/ideas/sticky-collapsing-home-header.md)

---

## Phase 1: Foundation & Header Component (`sticky-header-component`)

### Task 1: Bổ sung Design Tokens kính mờ, kích thước chuẩn & hàm tính toán `collapseFraction`

**Description:**
Khai báo các giá trị kích thước chuẩn cho Header (chiều cao cơ sở khi thu gọn $\approx 56\text{ dp}$, chiều cao mở rộng ước tính, kích thước Avatar thu nhỏ $\approx 38\text{ dp}$) và các token màu sắc hoặc hiệu ứng kính mờ (frosted surface tint, alpha gradient) trong `Color.kt` hoặc `CandySpace`/theme. Xây dựng logic tính toán tỉ lệ `collapseFraction` từ `scrollOffset` để dùng chung.

**Acceptance criteria:**
- [x] Khai báo đầy đủ các hằng số kích thước chuẩn cho `HomeHeader` (Expanded & Collapsed) và màu sắc bề mặt kính mờ mà không vi phạm `ColorArchitectureTest`.
- [x] Có hàm tiện ích chuẩn hóa `calculateCollapseFraction(scrollOffset: Int, maxCollapseRange: Float): Float` đảm bảo giá trị trả về luôn nằm trong đoạn $[0.0f, 1.0f]$.

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`
- [x] Tests pass: `./gradlew :app:testDevDebugUnitTest --tests "*ColorArchitectureTest*" --no-configuration-cache`

**Dependencies:** None

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/ui/theme/Color.kt`
- `app/src/main/java/com/hoabh/chanko/ui/theme/CandySpace.kt`
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/HomeHeader.kt`

**Estimated scope:** Small (2-3 files)

---

### Task 2: Tái cấu trúc `HomeHeader.kt` hỗ trợ trạng thái co giãn (`collapseFraction`), thu nhỏ Avatar và chỉ báo Offline mini

**Description:**
Nâng cấp composable `HomeHeader`:
- Tiếp nhận tham số `collapseFraction: Float = 0f` (hoặc lambda trạng thái).
- Khi `collapseFraction == 0f` (Expanded): Giữ nguyên bố cục đầy đủ gồm lời chào, phụ đề, Avatar lớn 52dp và dải banner ngoại tuyến dài bên dưới nếu `isOffline == true`.
- Khi `collapseFraction > 0f` (Transition $\rightarrow$ Collapsed):
  + Phụ đề mờ dần (`alpha = (1f - collapseFraction * 2f).coerceAtLeast(0f)`) và tự thu hẹp không gian.
  + Tiêu đề lời chào co nhẹ kích thước chữ và neo thẳng hàng ngang với Avatar.
  + Avatar thu nhỏ mượt mà từ $52\text{ dp} \to 38\text{ dp}$.
  + Trạng thái ngoại tuyến: Khi banner dài mờ đi, một huy hiệu/chấm cảnh báo ngoại tuyến nhỏ gọn xuất hiện ngay cạnh Avatar hoặc lời chào.
  + Bề mặt nền chuyển dần sang màu kính mờ (`frosted surface`) với viền phân cách mỏng hoặc đổ bóng nhẹ để ngăn chữ bị lẫn khi cuộn.
- Bổ sung Preview cho cả hai trạng thái Expanded và Collapsed trong `HomeHeader.kt`.

**Acceptance criteria:**
- [x] `HomeHeader` render đúng đắn ở cả trạng thái `collapseFraction = 0f` (đầy đủ) và `collapseFraction = 1f` (thu gọn mỏng).
- [x] Nút bấm Avatar vẫn nhận diện cử chỉ click mở Profile (`onProfileClick`) chính xác ở cả hai trạng thái.
- [x] Chỉ báo ngoại tuyến hiển thị rõ ràng ở cả trạng thái mở rộng và thu gọn, có semantic TalkBack đầy đủ.
- [x] Giao diện co giãn mượt mà, không giật nhảy hay vỡ layout khi `collapseFraction` thay đổi liên tục.

**Verification:**
- [x] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`
- [x] Preview hiển thị chuẩn xác trong Compose Preview.

**Dependencies:** Task 1

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/components/HomeHeader.kt`
- `app/src/main/res/values/strings_home.xml`

**Estimated scope:** Medium (2 files)

---

### Checkpoint 1: Header Foundation & Component Ready
- [x] `HomeHeader` độc lập đáp ứng cả trạng thái Expanded và Collapsed.
- [x] Mã nguồn biên dịch sạch sẽ (`./gradlew :app:compileDevDebugKotlin`).

---

## Phase 2: HomeScreen Layering & Responsive Verification (`sticky-header-integration`)

### Task 3: Cập nhật `HomeScreen.kt` để ghim Header cố định, kết nối `derivedStateOf` cuộn và đệm lề an toàn

**Description:**
Tái cấu trúc bố cục trong `HomeScreen.kt`:
- Bỏ `item(key = "section_header")` ra khỏi `LazyColumn`.
- Xếp lớp trong `BoxWithConstraints`:
  + `LazyColumn` nằm bên dưới, thiết lập `contentPadding.top` tương ứng với chiều cao mở rộng của Header cộng lề an toàn status bar. Thiết lập `clipToPadding = false` để nội dung cuộn trượt ngầm phía sau Header.
  + `HomeHeader` neo ở `Alignment.TopCenter` với `zIndex = 1f`.
- Sử dụng `derivedStateOf` để tính `collapseFraction` từ `listState.firstVisibleItemScrollOffset` dựa trên khoảng cách cuộn (khoảng cách chuyển đổi $\approx 80\text{..}120\text{ dp}$).
- Đảm bảo cơ chế bảo toàn anchor cuộn (`scrollAnchorItemId` qua `rememberSaveable`) vẫn hoạt động chính xác sau process death (`AC-30`).

**Acceptance criteria:**
- [ ] `HomeHeader` luôn ghim cố định ở đỉnh màn hình khi cuộn `LazyColumn` xuống các card bên dưới.
- [ ] Khi cuộn từ đỉnh xuống, Header co lại mượt mà từ dạng mở rộng sang dạng thu gọn; khi cuộn ngược lại đỉnh, Header mở rộng trở lại trơn tru.
- [ ] Các thẻ (`BudgetCreditCard`, `ItemJourneySection`, `ObservationCard`) trượt mượt mà phía dưới Header với nền kính mờ che chắn, không bị đè chữ hay lộ khoảng trống lỗi.
- [ ] Cơ chế khôi phục vị trí cuộn / anchor `scrollAnchorItemId` không bị ảnh hưởng.

**Verification:**
- [ ] Build succeeds: `./gradlew :app:compileDevDebugKotlin --no-configuration-cache`
- [ ] Unit tests pass: `./gradlew :app:testDevDebugUnitTest --tests "com.hoabh.chanko.presentation.screens.home.*" --no-configuration-cache`

**Dependencies:** Task 2

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`

**Estimated scope:** Medium (1-2 files)

---

### Task 4: Bổ sung Previews mở rộng/thu gọn, kiểm thử trợ năng TalkBack, FontScale 2.0f và chạy Full Verification

**Description:**
Hoàn thiện kiểm thử nghiệm thu:
- Cập nhật các `@Preview` trong `HomeScreen.kt` để hiển thị cả trường hợp Header mở rộng (`collapseFraction = 0f`) và Header thu gọn khi đang cuộn (`collapseFraction = 1f`).
- Kiểm tra tương thích giao diện trên màn hình nhỏ `320dp` và khi bật trợ năng `fontScale = 2.0f` (chữ trong thanh thu gọn không bị đè lên Avatar).
- Chạy toàn bộ test suite và build release check để đảm bảo không phát sinh regression với 31 AC của Home v2.0.

**Acceptance criteria:**
- [ ] Có đầy đủ `@Preview` phản ánh trạng thái mở rộng và thu gọn trên `HomeScreen`.
- [ ] Màn hình hẹp `320dp` và `fontScale = 2.0f` hiển thị cân đối, không tràn màn hình hay che khuất Avatar.
- [ ] Toàn bộ unit tests `./gradlew :app:testDevDebugUnitTest` PASS 100%.
- [ ] Build thành công: `./gradlew :app:assembleDevDebug --no-configuration-cache`.
- [ ] Tuân thủ nghiêm ngặt quy tắc `AGENTS.md`: không cài đặt hay chạy trên thiết bị vật lý hoặc emulator.

**Verification:**
- [ ] Tests pass: `./gradlew :app:testDevDebugUnitTest --no-configuration-cache`
- [ ] Build succeeds: `./gradlew :app:assembleDevDebug --no-configuration-cache`

**Dependencies:** Task 3

**Files likely touched:**
- `app/src/main/java/com/hoabh/chanko/presentation/screens/home/HomeScreen.kt`
- `app/src/test/java/com/hoabh/chanko/presentation/screens/home/HomeViewModelTest.kt`

**Estimated scope:** Small-Medium (2 files)

---

## Checkpoint 2: Hoàn tất & Sẵn sàng Review
- [ ] Header luôn ghim cố định và co giãn mượt mà khi cuộn trên `HomeScreen`.
- [ ] Thẻ Budget Card và Item Journey cuộn êm bên dưới lớp kính mờ.
- [ ] Hiển thị đầy đủ trạng thái offline ở cả 2 chế độ.
- [ ] Unit tests và build `assembleDevDebug` xanh 100%.
