# Home Screen Clean Slate & Rebuild (Spec v1.1)

## Problem Statement
Làm thế nào để dọn sạch toàn bộ di sản UI, state, model và tài nguyên của màn Home v5 cũ thành một "Clean Slate" biên dịch xanh 100%, không làm gãy `MainNavigation` hay Hilt DI, và tạo bệ phóng tối giản nhất cho lộ trình `P1 → P6` của **Chanko Home Specification v1.1**?

## Recommended Direction
Thực hiện **Clean Shell + Resource Purge (Hướng A)** thành một bước độc lập trước khi bắt đầu `P1`:
- Xóa toàn bộ `components/*`, `model/*`, `data/*`, `HomeViewModel.kt`, `HomeUiState.kt` và 2 file unit test cũ trong package `presentation/screens/home`.
- Gỡ binding `bindHomeSpendingRepository` khỏi `RepositoryModule.kt`.
- Xóa 6 drawable mẫu chỉ dùng cho Home v5 cũ (`mascot_crossed_arms.png`, `cooling_snowflake.png`, `ready_open_door.png`, `sample_jacket.png`, `sample_headphones.png`, `sample_keyboard.png`) và thu gọn `strings_home.xml` chỉ còn chuỗi đang dùng (`home_cd_add_item` và `home_placeholder_title`).
- Giữ lại duy nhất `HomeScreen.kt` với `HomeRoute` + `HomeScreen` tối giản để `MainNavigation.kt` hoạt động bình thường.

## Key Assumptions to Validate
- [x] **Không gãy Hilt DI:** Gỡ `bindHomeSpendingRepository` trong `RepositoryModule.kt` giúp KSP/Hilt biên dịch sạch trên cả 3 flavor (`dev`, `staging`, `prod`).
- [x] **Không gãy `MainNavigation`:** Giữ chữ ký `HomeRoute(onAddNewItemClick, modifier)` và `R.string.home_cd_add_item` đảm bảo `MainNavigation.kt` không cần chỉnh sửa ở bước dọn dẹp.
- [x] **Bảo toàn tài nguyên màn khác:** Các drawable của Splash, Login, Setup (`chanko_splash_*`, `chanko_login_door`, `setup_*`) hoàn toàn không bị ảnh hưởng.

## MVP Scope
- Xóa 23 files legacy của Home v5 (`HomeUiState`, `HomeViewModel`, 7 components, `HomeSpendingRepository`, 5 models, 2 test files, 6 PNG drawables).
- Giữ shell tối giản trong `HomeScreen.kt` để pass `./gradlew assembleDebug testDebugUnitTest`.
- Sau khi Clean Slate được commit riêng, triển khai tuần tự các phase `P1 → P6` theo `SPEC-home.md`.

## Not Doing (and Why)
- **Không sửa `ChankoBottomBar` trong `MainNavigation.kt` ở bước Clean Slate** — Việc gắn Global `+` Action Bottom Sheet thuộc phạm vi **Phase P3** của Spec v1.1.
- **Không xóa các token màu trong `Color.kt`** — `MainNavigation.kt` và các phase `P1 → P6` vẫn sử dụng bảng màu `CandyColors`.
- **Không chạm vào `:core:domain` / `:core:data` / `:core:database`** — Giữ nguyên ranh giới kiến trúc, UI-first bằng deterministic fake data theo đúng Spec v1.1.

## Open Questions
- Đã giải quyết toàn bộ (Xác nhận lưu one-pager, commit Clean Slate và triển khai tuần tự từ P1).
