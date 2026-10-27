# Spec: Chanko All 74 Icons System Migration

## Objective
Thay thế hoàn toàn bộ thư viện Google Material Icons (`androidx.compose.material.icons.extended`) trong toàn bộ ứng dụng Chanko bằng bộ thiết kế độc quyền **Chanko All 74 Icons** (bao gồm 66 icon 2D Candy Duotone v2.0-r1 và 8 icon 3D Signature). 

Mục tiêu nhằm:
1. Đồng bộ 100% ngôn ngữ thị giác ấm cúng, thân thiện (*cozy, emotional*) với linh vật mèo Chanko.
2. Thiết lập hệ thống Design Token `ChankoIcon` nội bộ, tự chủ hoàn toàn tài nguyên đồ hoạ.
3. Tối ưu dung lượng APK và build time bằng cách loại bỏ triệt để dependency `material-icons-extended`.

---

## Tech Stack & Source Assets
- **Nền tảng**: Android Jetpack Compose (Material3), Kotlin.
- **Nguồn tài nguyên đã có**: Thư mục [Chanko-All-74-Icons/](file:///Users/buihoa/HocTap/Android/chanko/Chanko-All-74-Icons/):
  - **2D Assets**: 66 icon IDs × 3 trạng thái (`default`, `selected`, `disabled`) = 198 Android Vector Drawable XMLs tại `Chanko-All-74-Icons/2D/android/res/drawable/`.
  - **3D Signature Assets**: 8 file WebP lossless 512×512 tại `Chanko-All-74-Icons/3D/assets/webp/` (C01 - C08).
  - **Design Token & Registry**: [registry-all-74.json](file:///Users/buihoa/HocTap/Android/chanko/Chanko-All-74-Icons/registry-all-74.json) và [ChankoIcon.kt](file:///Users/buihoa/HocTap/Android/chanko/Chanko-All-74-Icons/2D/android/ChankoIcon.kt).

---

## Commands
```bash
# Build check (kiểm tra biên dịch sau khi tích hợp drawable & wrapper)
./gradlew assembleDevDebug

# Unit test check (đảm bảo không gãy test hiện có)
./gradlew testDevDebugUnitTest

# Lint check
./gradlew lintDevDebug
```

---

## Project Structure
```text
app/src/main/
├── res/
│   ├── drawable/
│   │   ├── chanko_nav_01_default.xml ... chanko_sys_18_disabled.xml (198 vector files)
│   │   └── chanko_icon_colors.xml (bảng màu tham khảo Candy Duotone)
│   └── drawable-nodpi/
│       └── chanko_c01_quick_check.webp ... chanko_c08_chanko_chat.webp (8 file 3D WebP lossless)
└── java/com/hoabh/chanko/presentation/
    ├── designsystem/
    │   └── icon/
    │       ├── ChankoIconId.kt      // Enum 66 IDs với ánh xạ drawable (normal, selected, disabled)
    │       ├── ChankoIcon.kt        // Composable wrapper chuẩn a11y (24dp, Color.Unspecified)
    │       └── ChankoSignature3D.kt // Enum và Composable hiển thị 8 artwork 3D
    ├── navigation/
    │   └── MainNavigation.kt        // Chuyển BottomBar sang ChankoIconId (NAV_01 .. NAV_04)
    └── screens/                     // Thay thế rải rác trong Home, ItemDetail, AddItem, Vault, Profile...
```

---

## Code Style & Conventions

### 1. 2D Vector Icons (`ChankoIcon`)
Tất cả các icon vector 2D dùng component `ChankoIcon` với kích thước mặc định 24dp, giữ nguyên màu đa lớp bằng `tint = Color.Unspecified`.

```kotlin
// Example: Navigation Bar Item
NavigationBarItem(
    selected = isSelected,
    onClick = onTabClick,
    icon = {
        ChankoIcon(
            id = ChankoIconId.NAV_01,
            contentDescription = null, // decorative vì đã có label text bên dưới
            selected = isSelected,
        )
    },
    label = { Text(text = stringResource(R.string.home)) }
)

// Example: Icon Button với Action độc lập
IconButton(
    onClick = onBackClick,
    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
) {
    ChankoIcon(
        id = ChankoIconId.ACT_02,
        contentDescription = stringResource(R.string.back),
    )
}
```

### 2. 3D Signature Artwork (`ChankoSignature3D`)
Các icon 3D (C01 - C08) là hình ảnh minh họa cảm xúc cao, không áp dụng tint màu:

```kotlin
@Composable
fun ChankoSignatureImage(
    id: ChankoSignatureId,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Image(
        painter = painterResource(id.drawableRes),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit,
    )
}
```

---

## Testing & Quality Strategy
1. **Compilation & Resource Linker**: Chạy `./gradlew assembleDevDebug` để đảm bảo 198 vector drawables và 8 webp compile thành công vào `R.drawable.*`.
2. **TalkBack & Accessibility**:
   - Icon đi kèm Text nhãn: `contentDescription = null` (tránh đọc đúp).
   - Icon độc lập (IconButton): Bắt buộc có `contentDescription` bằng tiếng Việt rõ ràng (`stringResource(...)`).
   - Touch target: Nút bấm bọc ngoài đảm bảo tối thiểu `48.dp × 48.dp`.
3. **Dependency Integrity**: Sau khi chuyển đổi sạch sẽ, xoá `libs.androidx.compose.material.icons.extended` và biên dịch thử. Nếu còn bất kỳ màn hình nào compile lỗi do thiếu Material Icon thì truy vết và thay thế bằng `ChankoIconId` tương ứng.

---

## Boundaries
- **Always do**:
  - Tuân thủ quy chuẩn `AGENTS.md`: Chỉ build và test qua terminal, cấm cài đặt lên máy ảo/emulator hay thiết bị không cho phép.
  - Giữ toàn bộ text UI và contentDescription bằng **Tiếng Việt**.
  - Đảm bảo tính trung lập: Các quyết định BUY/WAIT/SKIP đều dùng chung chuẩn size/palette, không dùng icon gây áp lực tâm lý mua sắm.
- **Ask first**:
  - Bất kỳ thay đổi cấu trúc navigation flow hoặc sửa đổi các màn hình không liên quan đến icon.
- **Never do**:
  - Không tự ý sửa đổi hình học vector gốc của bộ 74 icons.
  - Không nạp lại bất kỳ thư viện icon ngoài nào của Google.
  - Không đặt các file 3D WebP 512×512 vào thư mục có mật độ điểm ảnh co giãn (`drawable-xhdpi`,...) mà phải đặt tại `drawable-nodpi/` để chống tràn RAM.

---

## Success Criteria
- [ ] 198 file vector 2D và bảng màu `chanko_icon_colors.xml` được tích hợp vào `app/src/main/res/drawable/`.
- [ ] 8 file 3D WebP lossless được tích hợp vào `app/src/main/res/drawable-nodpi/`.
- [ ] Module Design System `com.hoabh.chanko.presentation.designsystem.icon` được tạo hoàn chỉnh.
- [ ] 100% các icon Material trong toàn bộ app được thay thế bằng `ChankoIcon` / `ChankoSignatureImage`.
- [ ] Dependency `androidx.compose.material.icons.extended` được gỡ bỏ hoàn toàn khỏi `app/build.gradle.kts`.
- [ ] `./gradlew assembleDevDebug` và `./gradlew testDevDebugUnitTest` chạy PASS 100%.

---

## Open Questions for Approval
1. **Trạng thái Dark Mode**: Gói 2D hiện tại được tối ưu và phê duyệt cho **Light Theme** (nền sáng). Đối với Dark Mode, palette sẽ dùng màu mặc định của Light Theme hay cần xử lý động ngay đợt này? *(Khuyến nghị: Giữ snapshot light surface như thiết kế của tác giả gói v2.0-r1, hoãn Midnight Candy sang giai đoạn Dark Theme riêng).*
2. **Phạm vi hiển thị 3D (C01 - C08)**: 8 icon 3D sẽ được áp dụng ngay cho các card đặc trưng trên màn hình Home (Quick Check, Cooling-off, Pause Jar, v.v.) hay chỉ chuẩn bị sẵn wrapper trước?
