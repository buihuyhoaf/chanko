# Phase 1 — Core Modularization plan (Proposed)

Chưa triển khai, chưa được phê duyệt thực thi qua tài liệu này. Phase1 là extraction tăng dần, giữ nghiệp vụ/route/schema/theme hiện hành để tránh trộn thay đổi cấu trúc và sản phẩm.
Nguồn: S1 §3.2 cho phép tách dần; graph compile ở dependency-graph.md. Phần legacy còn mâu thuẫn Approved target phải được nhìn thấy qua gap matrix, không trở thành kiến trúc đích.

## Điều kiện vào

1. Review baseline/graph và chốt phạm vi extraction. Đọc lại AGENTS ở checkout thật; branch từ dev-chanko-1x, PR về cùng nhánh; không commit trực tiếp base/main.
2. Ghi HEAD, working tree và dữ liệu cần bảo toàn; không đụng PNG untracked có sẵn.
3. Chạy baseline assembleDebug/testDebugUnitTest/lintDebug; build AndroidTest và chạy connected tests khi có emulator/device. Nếu baseline fail phải phân loại và báo cáo, không nâng Gradle/thư viện ngoài phạm vi để che lỗi.
4. Chốt policy bảo toàn local dữ liệu. Core extraction không được tự upload/chuyển CooldownItem thành official server item.

TECH-04 thiếu không nhất thiết chặn move thuần cấu trúc, nhưng chặn các giả định env/URL/flavor/deployment. Network/auth/cache schema mới cần phase riêng và giải quyết Pending tương ứng.

## Thứ tự PR hoặc commit nhỏ đề xuất

| Bước | Thay đổi chỉ sau review | Điều kiện ra / rollback |
|---|---|---|
| 1.1 Domain | Tách core:domain thuần Kotlin: CooldownItem/ItemStatus/ItemRepository; giữ package public khi thực tế cho phép; Flow dependency tối thiểu | Compile consumers + 8 unit tests hiện có; domain không Android/Room/Hilt. Revert commit nếu boundary kéo Android vào |
| 1.2 Database | Tách core:database: AppDatabase, DAO/entity/converter và migration1->2; giữ tên DB, table/column, enum persisted, version2 | Instrumented migration/DAO trên device; không schema diff hoặc fallback destructive. Revert extraction, không rollback database bằng xóa dữ liệu |
| 1.3 Data | Tách core:data: ItemRepositoryImpl và entity-domain mappers; data->database/domain, Hilt binding composition ở app | Existing ViewModel tests và DAO behavior không đổi; không import app từ core. Revert bước nếu DI cycle |
| 1.4 Theme | Tách core:designsystem khi extraction có lợi thực tế; chuyển theme nguyên trạng, không đổi màu/font/dark behavior | Compile + smoke/screenshot equivalence các màn hiện có; nếu tài nguyên liên kết phức tạp, để package trong app và ghi lý do thay vì module rỗng |
| 1.5 Consolidate | Kiểm tra graph, package ownership, build config/version catalog thay tối thiểu phục vụ modules; không upgrade stack | assembleDebug, unit, lint và device checks; cập nhật graph theo cấu trúc thật và PR review |

Mỗi bước chạy kiểm tra thích hợp; chỉ tiếp tục khi regression đã giải quyết hoặc limitation được ghi rõ, không tuyên bố pass khi thiếu device.
Không thêm use case chỉ để forward một repository method; package layout mới chỉ tạo phần có code.
Domain/models + repository contracts thuộc domain; persistence thuộc database; coordination/mappers thuộc data; DI composition/navigation thuộc app.

## Test và data preservation gates

- Giữ migration1->2, dữ liệu VND/status/ID và filename DB như hiện có. Việc schema export hoặc schema mới là công việc sau review, không tự thêm trong extraction.
- Verify migration giữ record và DAO decision atomic hiện hành; tests deadline/savings là legacy characterization, không bằng chứng đúng SPEC mới.
- Smoke: Splash/Dashboard/Add/Detail/Decision/Vault, back stack, restart countdown, validation/error/retry; không có visual redesign.
- Graph không cycle; domain không import framework; no core->app; không lộ entity/DTO mới lên UI.
- Dùng revert từng commit cấu trúc nếu lỗi, bảo toàn database/user files; không git reset/clean để rollback.

## Sau Phase1, chỉ lập backlog

Network/auth với approved Google flow + serializer/secure storage; cache/draft schema account-scoped; legacy data policy; domain/purchase/cooling business migration; 4-tab navigation/design tokens/accessibility.
Từng phần cần contract và approval phạm vi tương ứng; không gộp tính năng đó vào extraction Phase1.

## Definition of done Phase1 (đề xuất)

PR về dev-chanko-1x, module graph thực tế được cập nhật; build/unit/lint đạt baseline; device migration/smoke có kết quả hoặc trạng thái pending rõ; không mất dữ liệu, không đổi nghiệp vụ/route/theme; không module rỗng hoặc vòng phụ thuộc. Người review quyết định phạm vi tiếp theo.
