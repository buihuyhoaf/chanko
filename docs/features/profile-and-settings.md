# Spec: Profile & Settings Screen (Chanko)

## 1. Objective
Xây dựng màn hình **Profile & Settings** và màn hình con **Personality Selection** cho Chanko theo bộ tài liệu bàn giao `chanko-profile-handoff` (v1.0):
- **User Story:** Là người dùng Chanko, tôi muốn truy cập hồ sơ cá nhân và các cài đặt ứng dụng từ avatar trên Home, để tôi có thể xem thông tin tài khoản Google của mình, tương tác nhẹ nhàng với linh thú Chanko tại Cozy Corner, tùy biến tính cách mascot (Friendly / Sassy / Savage), bật/tắt nhắc nhở Ready for Review, xem thông tin Privacy/About và Đăng xuất an toàn.
- **Entry duy nhất:** Chỉ bấm vào Avatar trên Home để mở Profile & Settings. Không có icon Settings riêng trên Home.
- **Navigation scope:** Profile & Settings là child destination (không có bottom navigation). Back về Home bảo toàn nguyên vẹn context, tab và scroll position.

---

## 2. Tech Stack & Dependencies
- **Language / Framework:** Kotlin 2.x, Jetpack Compose, Material 3, AndroidX Lifecycle / ViewModel, StateFlow.
- **Design System:** Tuân thủ Candy Pop 3D, nền cream `#FAF8F5`, brand purple `#8965F6`, font Nunito duy nhất (các weight 400, 500, 600, 700, 800 trong `res/font/`), tokens từ `CandyColors`, `CandySpace`, `CandyShape`, `CandySize`.
- **Assets:**
  - `cozy_corner_candidate_v1.png` (tạm thời tích hợp cho Cozy Corner kèm fallback an toàn, whole-scene transform reaction).
  - Material Symbols Rounded vectors cho icons (`ic_arrow_back`, `ic_chevron_right`, `ic_notifications`, `ic_info`, v.v.).

---

## 3. Commands
- **Build check:** `./gradlew assembleDevDebug`
- **Unit test:** `./gradlew testDevDebugUnitTest`
- **Lint / Detekt:** `./gradlew lintDevDebug` (hoặc test suites tương đương)

---

## 4. Project Structure
```
app/src/main/java/com/hoabh/chanko/
├── presentation/
│   ├── navigation/
│   │   ├── Screen.kt                   # Bổ sung route ProfileSettings & Personality
│   │   └── NavGraph.kt                 # Khai báo composable cho ProfileRoute & PersonalityRoute
│   └── screens/
│       ├── profile/
│       │   ├── ProfileScreen.kt        # Stateless Profile & Settings Scaffold, List, Sections
│       │   ├── ProfileViewModel.kt     # State holder cho ProfileUiState, effects, logout handling
│       │   ├── ProfileUiState.kt       # Immutable UI State cho Profile
│       │   ├── components/
│       │   │   ├── ProfileTopBar.kt
│       │   │   ├── AccountIdentityCard.kt
│       │   │   ├── CozyCornerCard.kt   # Tap reaction animation (whole scene transform 1.0 -> 1.025 -> 1.0)
│       │   │   ├── PersonalityRow.kt
│       │   │   ├── ReadyForReviewRow.kt
│       │   │   ├── GoogleAccountCard.kt
│       │   │   ├── LegalAndAboutRows.kt
│       │   │   └── SignOutSection.kt
│       │   └── personality/
│       │       ├── PersonalityScreen.kt    # Radio options, static preview text, Save button, Back confirmation
│       │       ├── PersonalityViewModel.kt # Draft vs Committed state machine, online-only validation
│       │       ├── PersonalityUiState.kt
│       │       └── components/
│       │           ├── PersonalityOptionCard.kt
│       │           ├── PersonalityPreviewCard.kt
│       │           └── UnsavedChangesDialog.kt
```

---

## 5. Architectural & Behavioral Rules (From Handoff PROFILE-01–10)

1. **Gộp Profile & Settings (PROFILE-01, PROFILE-02, PROFILE-03):**
   - 1 màn hình duy nhất chứa Identity + Cozy Corner + Settings groups.
   - Không có bottom nav bar ở Profile.
   - Nút Avatar góc trên Home là cổng vào duy nhất (HomeHeader đã có `onProfileClick: () -> Unit`).
2. **Cozy Corner (PROFILE-04):**
   - Mèo trắng ngồi ôm cốc tím tại bàn trà nhỏ.
   - Tap vào card chỉ kích hoạt decorative feedback (co dãn nhẹ whole-scene 1.0 -> 1.025 -> 1.0 trong 300-450ms).
   - Tuyệt đối không gọi AI/LLM, không pet game/level/coins, không mutation business logic. Hỗ trợ Reduce Motion (pressed feedback nhẹ, không phóng to).
3. **Cat Personality (PROFILE-05, PROFILE-06):**
   - Màn riêng: `PersonalityRoute`.
   - 3 lựa chọn: **Friendly** ("Let's take a moment..."), **Sassy** (Default), **Savage** ("The cart can wait...").
   - Lựa chọn chỉ đổi draft preview tĩnh offline, không ghi server ngay.
   - Nút **Lưu thay đổi (Save)** ở cuối màn hình: chỉ bấm được khi dirty & online. Chặn double-click (idempotent intent).
   - Trạng thái Dirty khi Back (cả gesture và app bar): hiển thị `UnsavedChangesDialog` ("Hủy thay đổi?" / "Tiếp tục chỉnh sửa" / "Hủy bỏ").
   - Online-only: Nếu offline, khóa Save và cảnh báo yêu cầu kết nối mạng.
4. **Ready for Review (PROFILE-07):**
   - Chỉ quản lý Notification Preference của app cho đợt nhắc duyệt đồ hạ nhiệt (cooling-off).
   - Không chứa countdown, không chứa item list, không master notification switch ngoài scope.
   - Quản lý đồng bộ với quyền Notification của hệ thống Android (POST_NOTIFICATIONS nếu API >= 33).
5. **Google Account (PROFILE-08):**
   - Read-only card: hiển thị Avatar + Tên + Email Google từ phiên đăng nhập.
   - Không chevron, không click đổi tài khoản hay unlink.
6. **Privacy & About (PROFILE-09):**
   - Hiển thị Version thật từ `BuildConfig.VERSION_NAME` (`BuildConfig.VERSION_CODE`).
   - Mở modal/sheet/browser cho Điều khoản & Chính sách bảo mật.
7. **Sign Out (PROFILE-10 & AUTH-04 safety):**
   - Dialog xác nhận Đăng xuất rõ ràng ("Đăng xuất" / "Hủy").
   - Thu hồi phiên (gọi revoke/logout qua auth repository/viewmodel), dọn dẹp state phiên, điều hướng an toàn về `Screen.Login` xóa sạch backstack.
   - Không lộ chéo dữ liệu giữa các tài khoản.

---

## 6. Boundaries

- **Always:**
  - Touch target >= 48dp cho tất cả icon/button/switch.
  - Hỗ trợ font scale 200% không bị cắt chữ, scroll mượt mà không che content.
  - Thêm đầy đủ ContentDescription / Semantics cho TalkBack.
  - String tài nguyên đưa toàn bộ vào `strings.xml` (hỗ trợ Tiếng Việt làm ngôn ngữ gốc/chính theo quy chuẩn Chanko).
  - Xử lý System Insets (Edge-to-Edge) chuẩn Material 3.
- **Ask First:**
  - Thay đổi DB migrations hoặc thêm dependency Gradle ngoài mạng.
- **Never:**
  - Không cài đặt hay chạy emulator/AVD theo AGENTS.md.
  - Không nhúng LLM/AI prompt vào màn hình Profile.
  - Không thêm icon Settings riêng lên Home.
  - Không tự ý bịa thêm tính năng (như đổi avatar, đổi mật khẩu, dark mode riêng, pet game).

---

## 7. Success Criteria & Verification
1. `AC-01 -> AC-20` từ tài liệu handoff được thỏa mãn:
   - Tap avatar ở Home mở đúng 1 instance Profile.
   - Không có bottom bar ở Profile, Back về Home giữ đúng vị trí.
   - Cozy Corner tap có animation co dãn tinh tế.
   - Personality màn riêng có Preview tĩnh, dirty-back dialog, save online-only.
   - Ready for Review chỉ quản lý notification preference.
   - Google Account hiển thị read-only thông tin chính xác.
   - Sign Out đăng xuất phiên sạch sẽ, điều hướng về Login.
2. Build và Unit Tests pass: `./gradlew testDevDebugUnitTest assembleDevDebug`.
