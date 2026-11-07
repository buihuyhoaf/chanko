# Repository baseline và architecture gap matrix

## Bằng chứng hiện trạng

Khảo sát trực tiếp tại HEAD `10fb6e10c92ed86c2531fcf9b59263d0754c6358`, branch `dev-chanko-1x`, ngày 18/10/2026.
Working tree ban đầu không có tracked change; có một PNG untracked tên “Mèo đáng yêu và lựa chọn mua sắm sáng suốt.png”, giữ nguyên.
Đường dẫn trong bảng là relative với repository root để dùng khi chuyển tài liệu vào repo.

- `settings.gradle.kts`: chỉ include :app. `gradle/libs.versions.toml`: AGP9.4.1, Kotlin2.2.10, Room2.7.1, Hilt2.60.1, Navigation2.9.0, Coroutines1.10.2, KSP2.2.10-2.0.2.
- `app/build.gradle.kts`: com.hoabh.chanko, minSdk24, target/compile37, JVM17; Compose/Hilt/Room/Navigation/Coil; chưa khai báo Retrofit/OkHttp/auth/DataStore.
- Package gốc trong các đường dẫn code dưới đây: `app/src/main/java/com/hoabh/chanko/`.
- `data/local/AppDatabase.kt`: Room v2, exportSchema=false, migration1->2 copy bảng và round REAL thành INTEGER; `di/DatabaseModule.kt` đăng ký migration, không destructive fallback.
- Có 8 unit test declarations trong CooldownItemTest/ViewModelTest; 2 database instrumentation tests và 1 ExampleInstrumentedTest. Chỉ đếm source, không khẳng định pass.
- Không chạy Gradle/build/test trong Phase0; kết quả audit cũ không được xác nhận lại.

## Gap matrix

Priority là đánh giá rủi ro Proposed; quyết định mục tiêu trỏ nguồn Approved. Không có sửa chữa implementation trong Phase0.

| Gap | Priority | Hiện trạng / bằng chứng | Mục tiêu, nguồn | Hướng xử lý và điều kiện kiểm chứng |
|---|---|---|---|---|---|
| G01 Boundaries | Medium | :app duy nhất; presentation/domain/data/di đã tách package | S1 §3 Clean, feature-based incremental | Phase1 tách domain/data/database/designsystem theo graph; build và không cycle |
| G02 Authority | Critical | data/repository/ItemRepositoryImpl.kt chỉ gọi ItemDao; local mutation là kết quả chính thức | S1 §5; S4 ADR-09 server authoritative | Phase1 chỉ cách ly legacy; pha sau thêm remote theo contract, test không báo success trước server |
| G03 Model | Critical | domain/model/CooldownItem.kt: Int ID, 3 status COOLING_DOWN/BOUGHT/PASSED, gộp thời gian và decision | S2 §3, S4 invariants/phụ lục: 5 item status, 4 cooling states, N purchases/item | Tách legacy khỏi model mới; không mapping PASSED/BOUGHT thành server purchase tự động |
| G04 Storage/data safety | Critical | cooldown_items id auto, không owner/server ID; v2 migration; không có draft/cache tách riêng | S2 §8,11; ADR-09 | Chốt policy legacy/account trước schema mới; giữ migration1->2, draft/ảnh bền vững; test upgrade/logout/account switch |
| G05 Cooling permission | Critical | CooldownItem.canDecide, ItemDetailScreen:46, DecisionScreen:54; ItemDao.updateItemStatus có điều kiện deadline | S4 SPEC-05 + phụ lục §3 cho mua/skip trước hạn | Đổi cả UI/domain/DAO ở phase nghiệp vụ riêng; test transaction resolve và expiry offline không mutate |
| G06 “Savings” | High | ItemDao.getTotalSavedAmount SUM(price) PASSED; OverviewViewModel và Vault dùng tổng | S4 SPEC-06 không coi giá skip là savings chắc chắn | Giữ legacy hiện trạng trong extraction; phase feature sửa cách gọi/tính, không tạo insight giả |
| G07 Networking/auth | High | catalog/build/source chưa có Retrofit/OkHttp, auth session/nonce/secure storage | S1 §6,12; S3 §2–3,6 | Ngoài core extraction Phase1; chốt serializer/contract/secure storage; test refresh single-flight và owner |
| G08 Navigation | High | presentation/navigation/NavGraph.kt + Screen.kt: Splash/Dashboard/AddItem/Detail/Decision/Vault, Int IDs | S5 UI-08 4 tabs; S1 Navigation Compose | Giữ route cũ Phase1; navigation sản phẩm và opaque IDs thay sau review contract |
| G09 Design system | High | ui/theme/Color.kt primary #7B5BE6/background #F9F8FD; Type FontFamily.Default; Theme theo system dark | S5 UI-02/03/09: #8965F6, #FAF8F5, Nunito, light-only MVP | Phase1 có thể move theme nguyên trạng; áp tokens/Light-only ở phase UI riêng với PV/contrast/accessibility review |
| G10 Presentation/DI | Low | Routes collectAsStateWithLifecycle; ViewModels + StateFlow; Hilt @Binds repo, @Provides DB | S1 §3.3–3.5 | Tận dụng; use cases chỉ khi nghiệp vụ cần, không generic BaseViewModel; test state/error/retry giữ nguyên |
| G11 Budget/Purchase/AI/input | High | Không thấy implementation tương ứng trong source inventory; model hiện tại là cooldown item | S4 SPEC-01–09, AUTH-05; S2/S3 phụ lục cuối | Triển khai sau foundation; canonical fields, purchase riêng, draft xác nhận thủ công, AI không mutate |
| G12 Environment | Pending | Build hiện có SDK/JVM/catalog; chưa có S6 nội dung | S1 §13; TECH-04 Pending | Không nâng dependency/thêm flavors/URL/secret/CI theo suy đoán |
| G13 Verification | Medium | Test declarations tồn tại; test deadline/savings phản ánh legacy; exportSchema=false | S2 §11; S3 §6.5 là test plan | Phase1 chạy baseline rồi so sau từng move; đổi kỳ vọng nghiệp vụ ở phase tương ứng, không sửa tests để che regression |

## Những gì chưa xác minh

Build/8 unit tests pass, instrumentation runtime, configuration cache, app startup và UI rendering chưa chạy ở phiên này.
Không có backend checkout/deployed API hoặc migration server để xác minh. Không kết luận backend đã triển khai từ tài liệu thiết kế.
Không kiểm tra lại mọi yêu cầu accessibility bằng thiết bị; chỉ xác định gap từ code/theme.
