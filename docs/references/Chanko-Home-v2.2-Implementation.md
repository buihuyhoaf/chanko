Triển khai ngân sách trên Chanko Home v2.2

Dành cho Android developer và QA. Tài liệu hướng dẫn tích hợp Budget Card Carousel và các luồng ngân sách từ Home. Baseline UX là bản Product UX đã chốt ngày 04/11/2026. Các tên lớp, API, khóa lưu trữ và cách tích hợp dưới đây là thiết kế triển khai đề xuất. Palette Candy cần duyệt visual trước khi phát hành.

Tổng quan thay đổi ngân sách

Home v2.2 thu gọn carousel thành tối đa ba vị trí: kỳ liền trước, kỳ hiện tại và kỳ tiếp theo. Khi mở hoặc quay lại Home, người dùng thấy ngay kỳ hiện tại. Kỳ trước chỉ xem và chỉ xuất hiện nếu có dữ liệu. Kỳ tiếp theo hiển thị hạn mức đã cấu hình hoặc thẻ trung tính để bắt đầu thiết lập. Lịch sử đầy đủ chuyển sang Budget qua nút Xem tất cả.

Màu thẻ đi theo chu kỳ. Một kỳ giữ nguyên Candy Gradient khi chuyển từ sắp tới sang hiện tại rồi thành kỳ đã kết thúc. Mười hai theme lặp lại theo thứ tự chu kỳ. Khi vượt ngân sách, cảnh báo dùng Coral và thẻ giữ màu của kỳ.

Home lấy số liệu từ Budget theo đúng chu kỳ. Pay Now và Credit cùng tính vào tổng chi theo ngày mua. Khi sang kỳ mới, cấu hình đã lưu cho kỳ tiếp theo có hiệu lực nếu có. Số dư thuộc về kỳ cũ. Khi ngoại tuyến, người dùng có thể xem dữ liệu đã lưu và cần kết nối để thiết lập hoặc chỉnh sửa.

1. Phạm vi và thứ tự ưu tiên

Nội dung v2.1 bị thay

Baseline Home v2.2

Tối đa ba kỳ cũ và duyệt lịch sử trên Home

Chỉ kỳ liền trước. Lịch sử đầy đủ đi qua Xem tất cả.

Màu theo vai trò thẻ và nền đơn giản

12 Candy Gradient, gắn với identity của chu kỳ.

Next chưa thiết lập màu Lavender

Trắng ngà hoặc xám pastel, viền nét đứt, dấu +.

Thiếu lối vào lịch sử ngoài thẻ

Xem tất cả nằm ở header của khu vực Budget.

Budget v2.1 mô tả selector hiện tại và quá khứ cho fake UI. Điểm vào lịch sử đầy đủ và cấu hình kỳ sau là yêu cầu tích hợp v2.2. Giữ editor Budget hiện tại cho hạn mức kỳ đang diễn ra, audit điều chỉnh, quyền sửa và vòng đời modal. Dùng target riêng cho cấu hình kỳ sau để tránh ghi vào kỳ đang mở.

2. Kiến trúc và điểm tích hợp

Trong checkout Android, presentation/screens/home đã có HomeScreen, HomeViewModel, HomeUiState, HomeUiEvent, HomeUiEffect, BudgetCreditCard. Home đang lấy fixture từ HomeFakeDataSource. Budget có BudgetRoute, BudgetViewModel, BudgetRepository và FakeBudgetRepository. NavGraph.kt đã có route Budget, Profile và Item Detail. Đây là các điểm nối ưu tiên.

Compose thu thập StateFlow bằng collectAsStateWithLifecycle, vẽ nội dung theo state và gửi intent. Route xử lý navigation và snackbar. Composable giữ offset kéo, pager state, focus và scroll dọc.

ViewModel dùng @HiltViewModel và constructor injection, giữ BaseViewModel nếu phù hợp. Quan sát Budget độc lập để lỗi dữ liệu chỉ ảnh hưởng khu vực ngân sách. UiState bất biến. Dùng một collector lifecycle-aware cho effect một lần, tránh phát lại navigation khi xoay máy.

Domain tái sử dụng BudgetRepository và phép tính Budget đã có khi contract phù hợp. Use case quan sát cửa sổ chu kỳ chỉ đọc. Use case đồng bộ và command setup/edit đi qua Budget. Clock, múi giờ nghiệp vụ và nguồn kết nối được inject để test.

Data dùng Room làm nguồn đọc cho UI, backend là nguồn quyết định mutation và chu kỳ. Hilt bind implementation qua module repository/database hiện có. Fake giữ vai trò fixture/test. Tích hợp theo tài khoản và dọn dữ liệu quan sát khi đổi tài khoản.

Hợp đồng dữ liệu đề xuất

Room lưu snapshot Budget theo account và cycle identity, có start/endExclusive, anchor hoặc tham chiếu policy, limit, tổng chi hợp lệ, version và lastSyncedAt. Chu kỳ đã cấu hình cần ordinal/theme key bất biến hoặc dữ liệu đủ để tái tạo theme ổn định. Cache cần phân biệt ba tình huống gồm snapshot đã xác nhận thiếu dữ liệu, chưa tải và dữ liệu có thật. Nếu summary hoặc Next chưa tải xong, biểu diễn phần thiếu bằng trạng thái tải/lỗi riêng.

Query theo tài khoản và trả một snapshot nhất quán của cửa sổ. Upsert một lần trong transaction sau khi nhận response hợp lệ. Loại response cũ theo version hoặc generation, gồm cả response đến sau khi đổi account. Unique index cho identity ổn định và khóa chu kỳ theo tài khoản với ngày bắt đầu. Kiểm tra overlap và một Active trong transaction nghiệp vụ có thẩm quyền. Nếu cache hiện hữu chưa có theme identity, migration bảo toàn ID và dữ liệu chi tiêu, backfill một lần theo quy tắc được duyệt. Kiểm thử migration từ schema có dữ liệu.

3. UI state và events

Giữ SectionState cho mỗi khu vực. Thay payload thẻ đơn của Budget bằng BudgetCarouselUi gồm danh sách slot hợp lệ, selectedSlotKey, currentCycleKey và metadata đồng bộ. Slot có role và key ổn định. Dùng payload riêng cho kỳ đã đóng, kỳ active, kỳ sau đã cấu hình, placeholder kỳ sau và Current empty. Tách connectivity và quyền thao tác khỏi loại thẻ. Previous/Current thật dùng summary cùng cycleId. Next configured chỉ cần plan và range.

Intent

State hoặc effect

HomeEntered(entryToken)

Chọn Current một lần khi vào hoặc quay lại Home. Giữ anchor dọc.

PagerSettled(slotKey, generation)

Cập nhật selection nếu key thuộc snapshot đang dùng.

CardActivated(slotKey)

Kiểm tra key, quyền và kết nối mới nhất, rồi phát một effect điều hướng hoặc báo lý do bị chặn.

BudgetSeeAllTapped

Mở lịch sử ngân sách.

RetrySection(section) / RefreshHome

Đồng bộ phần tương ứng hoặc refresh toàn màn. Giữ dữ liệu còn hợp lệ trong khi chờ.

Snapshot mới / ConnectivityChanged

Tính lại nội dung và quyền. Reconcile selection theo key. Khi Current đổi, chọn Current mới.

State và effect dùng dữ liệu định danh, tránh truyền snapshot tiền qua navigation. Dùng BudgetSeeAllTapped để mở lịch sử ngân sách. Chặn double tap khi effect đang được xử lý, giải phóng khi route nhận hoặc từ chối. Khi process death, dựng lại cửa sổ từ nguồn hợp lệ, mặc định Current và khôi phục scroll dọc nếu anchor còn tồn tại. Setup chỉ mở từ thao tác hợp lệ của người dùng.

Trạng thái

Hiển thị và thao tác

Loading lần đầu

Skeleton thẻ, chưa có hành động theo kỳ.

Có snapshot

Render đúng slot. Refresh giữ nội dung và thời điểm đồng bộ.

Current chưa có ngân sách

Empty trên Current, hướng dẫn thiết lập kỳ hiện tại khi đủ điều kiện.

Không có kỳ liền trước

Bỏ slot Previous. Pager chặn ở biên.

Offline có cache

Snapshot kèm thời điểm đồng bộ nếu biết. Xem dữ liệu và lịch sử đã cache.

Offline thiếu cache

Báo chưa có dữ liệu để xem ngoại tuyến. Cho Thử lại theo kết nối.

Lỗi một phần

Giữ snapshot đúng cycle nếu có và báo lỗi. Nếu thiếu dữ liệu thì hiển thị lỗi cục bộ.

Active mới từ nguồn có thẩm quyền

Cập nhật ba slot cùng snapshot, snap vào Current mới.

4. Carousel và gesture

Slot

Nội dung

CTA

Previous

Kỳ liền trước nếu tồn tại. Tên, khoảng ngày, limit, đã chi, còn lại hoặc vượt, progress, Đã kết thúc.

Xem chi tiết

Current

Tên, khoảng ngày, limit, đã chi, còn lại hoặc vượt, progress, Đang diễn ra.

Xem ngân sách

Next configured

Tên, khoảng ngày và hạn mức đã cấu hình, Sắp tới.

Xem/chỉnh sửa

Next unconfigured

Credit card trắng ngà/xám pastel, viền nét đứt, dấu +, Chu kỳ tiếp theo, ngày nếu xác định được.

Thiết lập ngân sách

Dùng HorizontalPager trong danh sách cuộn dọc hiện tại. Thứ tự slot Previous → Current → Next. Trên UI tiếng Việt, vuốt phải về Previous và vuốt trái sang Next. Tối đa một trang mỗi gesture, kể cả fling nhanh. Dùng cơ chế snap một trang của phiên bản Compose đang dùng. Chỉ commit selection khi pager settle. Khi thiếu Previous, Current là trang 0. Khởi tạo và cập nhật theo slot key thay vì lưu index cố định. Gesture ở biên dừng tại slot đầu/cuối.

Dùng xử lý touch slop và phân trục mặc định của Compose. Kéo ngang đã được pager nhận thì hủy press/click. Kéo dọc ưu tiên danh sách Home và cơ chế refresh hiện hành. Không thêm recognizer drag song song trên thẻ. Card và CTA đại diện một hành động, chỉ expose một đích focus cho hành động đó. Kiểm tra thao tác tap, kéo ngắn, kéo chéo, fling và cancel từ cùng vị trí CTA. Mọi thao tác đổi trang chỉ cập nhật pager, không gọi scroll dọc.

Khi dữ liệu đổi giữa gesture, giữ snapshot hiển thị đến lúc settle nếu an toàn, sau đó reconcile theo key. Với rollover hoặc đổi tài khoản, hủy tương tác cũ, snap Current và bỏ intent stale theo generation. Khi quay lại Home, reset pager theo entry token của navigation, không reset mỗi lần recomposition hoặc refresh. Lưu anchor và offset dọc độc lập bằng state holder của Home. Chừa không gian cho chữ lớn, nội dung thẻ dài và insets.

5. Candy Gradient và theme identity

Số

Theme

Chính

Sáng

Đậm

01

Tangerine

#FF963C

#FFCB83

#E86C25

02

Lavender

#AC86F5

#E2CEFF

#7950C9

03

Mint

#70D9B0

#C8F7E3

#299D79

04

Strawberry

#F58FA9

#FFD1DB

#D9557E

05

Sky Blue

#7CBDF3

#D0E9FF

#408BCE

06

Lemon

#F7D55E

#FFF1A9

#D9A52E

07

Peach

#FFAE8C

#FFE0C8

#E77B65

08

Grape

#A184DE

#DCCBFA

#7051AE

09

Aqua

#73D5DB

#C5F4F4

#319FAF

10

Rose

#EBA6D0

#FCE0F1

#C36BA5

11

Pistachio

#B5D57A

#E5F1BC

#779F48

12

Blueberry

#929FEB

#D3DAFF

#596BBE

Đặt palette trong design tokens chung. Gradient đề xuất chạy sáng → chính → đậm. Kiểm tra màu chữ ở các điểm sáng, giữa và tối, kể cả khi thêm overlay. Dùng Coral cho cảnh báo và số tiền vượt cùng nhãn chữ. Previous giữ gradient gốc. Placeholder có token riêng, độc lập với palette và chỉ dùng dữ liệu đã xác nhận.

Phương án lưu theme là server gán ordinal theo thứ tự chu kỳ của tài khoản, bắt đầu từ 0 và bất biến theo cycle identity. Tra palette theo floorMod(ordinal, 12). Kỳ thứ 13 trở lại Tangerine. Thiết lập Next phải nhận cùng identity và ordinal khi thành Current. Không suy ordinal từ vị trí pager, tháng dương lịch hoặc số bản ghi còn trong cache. Với lịch sử cũ, cần chốt mốc ordinal/backfill trước rollout và kiểm tra không đổi màu khi tải thêm kỳ cũ. Giữ toàn bộ lịch sử, kể cả sau khi palette quay vòng.

6. Navigation và quyền thao tác

Điểm chạm

Đích và đối số

Current thật

Budget Overview theo cycleId.

Previous

Budget Overview theo cycleId, read-only.

Current empty

Setup cho kỳ hiện tại qua luồng đủ điều kiện.

Next configured

Cấu hình kỳ tiếp theo theo cycleId hoặc khóa kỳ ổn định. Offline chỉ xem nếu đích hỗ trợ.

Next placeholder

Setup Budget với target kỳ sau, kiểm tra lại target trước khi ghi. Offline hiện thông báo cần kết nối.

Budget Xem tất cả

Danh sách đầy đủ chu kỳ từ mới đến cũ, cho phép mở từng kỳ.

Mở rộng contract của BudgetRoute bằng đối số định danh và mode có kiểu, hoặc thêm entry trong cùng Budget graph nếu cần. Budget resolve cycle theo tài khoản và quyền hiện hành ở đích. Khi đã mở setup mà target vừa chuyển vai trò, giữ draft, khóa lưu và yêu cầu tải lại. Kiểm tra đích trước khi phát effect và tại thời điểm submit. Lịch sử dùng phân trang nếu cần, bảo đảm thứ tự start giảm dần và cache row đang mở. Các kỳ chưa cache khi offline cần disabled hoặc unavailable rõ ràng.

7. Offline đồng bộ và rollover

Offline read-only áp dụng cho Budget, thiết lập Next và các thao tác ghi ngân sách từ Home. Cache hiển thị thời điểm đồng bộ thật. Các lệnh write cần chặn ở UI, repository/use case và backend theo quyền. Nếu mất mạng khi đang mở editor, giữ draft hợp lệ và báo lý do, online lại thì revalidate và để người dùng tự gửi. Dùng idempotency key cho retry cùng yêu cầu, kiểm tra server khi kết quả request không rõ. Không tạo row hay queue mutation khi người dùng chỉ vuốt qua placeholder. Cache miss và request lỗi có thông điệp riêng, không quy thành số tiền 0 hay placeholder đã xác nhận.

Ranh giới chu kỳ. Giữ ngày neo 1–31 theo thiết lập ban đầu. Mỗi tháng tính ngày bắt đầu bằng min(ngày neo, số ngày trong tháng), từ ngày neo gốc. Với neo 31, năm nhuận có boundary 31/01 → 29/02 → 31/03. Khoảng nghiệp vụ là [start, nextStart), nhãn ngày cuối là nextStart trừ một ngày. Dùng clock có thể inject và múi giờ nghiệp vụ thống nhất với backend, gồm cả khi đổi múi giờ thiết bị.

Mở kỳ mới. Rollover ở Home là chuyển vai trò chu kỳ khi đến boundary. Quy tắc Budget không chuyển số dư vẫn áp dụng. Backend trong một transaction khóa phạm vi tài khoản, resolve kỳ theo thời điểm có thẩm quyền, đóng kỳ cũ và activate đúng Next đã cấu hình hoặc upsert kỳ mới theo chính sách tự động mở hiện hành. Bảo toàn identity và theme của Next đã cấu hình. Retry, nhiều client và foreground cùng lúc phải hội tụ về một Active và không có khoảng ngày chồng lấn. Nếu bỏ lỡ nhiều kỳ, lấy lịch sử theo backend. Reconcile khi app foreground, refresh hoặc đến boundary trong foreground. Khi offline qua boundary, giữ snapshot có nhãn cache và khóa ghi, chờ snapshot xác nhận để chuyển Current. Sau đồng bộ, commit Previous/Current/Next cùng nhau và giữ scroll dọc.

Tổng chi. Pay Now và Credit tính giống nhau theo ngày mua thực tế. Dùng số nguyên đơn vị tiền nhỏ nhất, tổng từ purchase hợp lệ. Sửa/xóa giao dịch nhập sai cập nhật đúng các kỳ bị ảnh hưởng và audit theo contract Budget, gồm cả khi đổi ngày mua sang kỳ khác. Previous chỉ xem từ Home. Không ghi nhận chi tiêu tương lai trước khi kỳ bắt đầu. Credit là phương thức có tên. Refund, quản lý nợ và chuyển số dư nằm ngoài MVP. Khi vượt, giữ phần trăm thật và clamp thanh về 100%. Ví dụ limit 5.000.000đ và spent 5.500.000đ hiển thị 110% cùng Vượt ngân sách 500.000đ. Chỉ dùng 0 khi dữ liệu xác nhận bằng 0. Với limit bằng 0 hoặc thiếu limit, chỉ hiện tỷ lệ nếu domain có quy tắc hợp lệ.

8. Accessibility của Carousel

TalkBack đọc role thẻ, tên và khoảng ngày, trạng thái, số liệu hiện có theo thứ tự limit → spent → còn/vượt → tỷ lệ, rồi CTA. Nêu trang hiện tại và tổng số trang. Cung cấp hành động Trước/Tiếp theo hoặc điều khiển tương đương, chỉ khi có đích hợp lệ. Nhãn trợ năng của Xem tất cả nêu rõ đích là lịch sử ngân sách.

Dùng text resources tiếng Việt, định dạng tiền và ngày theo locale. Mọi target chạm ít nhất 48dp. Thử font 100/150/200%, màn hẹp và insets. Mục tiêu contrast 4.5:1 với chữ thường, 3:1 với chữ lớn và thành phần quan trọng. Reduced motion dùng snap, bỏ parallax và hiệu ứng trang trí, giữ điều khiển trực tiếp của gesture.

9. Trình tự bàn giao và kiểm thử

Chốt hợp đồng cycle identity, timezone, next-plan API, lịch sử đầy đủ, backfill và quyền ghi với Budget/backend. Duyệt token production.

Viết projection/domain và Room adapter, test cửa sổ chu kỳ, migration, cache và transaction. Tích hợp ViewModel và route trước khi đổi visual.

Tích hợp pager và thẻ vào Home, sau đó hoàn thiện semantics của Carousel. Kiểm tra regression Home/Budget tại entry point bình thường.

Chạy unit test và build debug theo quy định repo. Chuẩn bị Compose UI test và kịch bản TalkBack trên thiết bị thật qua quy trình QA được phép. Ghi rõ trạng thái runtime nếu chưa có lượt xác minh trên thiết bị.

Lớp kiểm thử

Ca cần có

Domain và mapper

Thiếu Previous, Current empty, Next có/không, cache thiếu so với zero thật, 110%, palette 12→01, theme khi Active thành Previous, vùng [start, nextStart), neo 29/30/31 và năm nhuận.

Repository và Room

Migration giữ history, offline cache/no-cache, stale response, account isolation, hai lệnh rollover đồng thời, timeout rồi retry, Next cấu hình đồng thời với rollover, correction chuyển kỳ.

ViewModel

Retry cục bộ, effect một lần, tap stale, mạng rớt khi submit, snapshot đến giữa gesture, Home return và process death, lỗi Budget chỉ ảnh hưởng khu vực ngân sách.

Compose và navigation

Vuốt nhanh một trang, drag từ CTA không click, kéo dọc không đổi trang, scroll anchor giữ nguyên, boundary không có Previous, đủ đích theo từng thẻ và nút Xem tất cả của Budget.

Visual và accessibility

12 gradient và neutral, Coral, chữ lớn, màn hẹp, TalkBack, focus, reduced motion.

10. Checklist nghiệm thu

☐  BC22-01–04 · Tối đa Previous/Current/Next. Current mặc định và khi quay lại. Hướng vuốt đúng. Không có Previous thì bỏ slot.

☐  BC22-05–07 · Placeholder neutral, không phát write khi xem. Setup thành công chuyển Next sang theme thật.

☐  BC22-08–10 · Đủ 12 gradient. Identity giữ màu qua đổi trạng thái và quay vòng sau 12. Previous chỉ xem và giữ màu.

☐  BC22-11–13 · Current lấy số liệu đúng kỳ. Next không có spent/progress giả. Cảnh báo Coral giữ nguyên nền.

☐  BC22-14–16 · Budget Xem tất cả mở đầy đủ lịch sử. Swipe không click và không đổi scroll dọc.

☐  BC22-17–18 · Offline cache chỉ xem kèm thời điểm đồng bộ. Thiếu cache không giả 0đ. Setup offline báo cần mạng.

☐  BC22-19–20 · Rollover idempotent, duy nhất Active, không trùng ngày. Next không tác động số chi hiện tại.

☐  BC22-21–22 · TalkBack đọc đúng thẻ, số liệu và hành động. Font scaling, target 48dp và reduced motion đạt.

☐  BC22-23–24 · Lỗi Carousel cô lập. Kiểm tra các hành vi Home v2.0 liên quan ngân sách vẫn đạt.

☐  Budget integration · Đúng route theo cycle identity, kiểm tra quyền tại đích, snapshot cùng kỳ, retry an toàn và correction có audit.

Nguồn đối chiếu

Chanko Home UX Spec v2.2 trong hội thoại Cập nhật UI UX Home, bản chốt ngày 04/11/2026, phần 1–8.

Repo Android Chanko, docs/features/home-ui-v2-spec.md, budget-ui-v2-spec.md, budget-v2.1-ui-contract.md và budget-v2.1-state-matrix.md. Các tài liệu Budget UI mô tả fake UI và contract màn hình.

Bản trích Phase 0 của Functional Specification v1.1 và TECH-02/TECH-03 v1.1, đặc biệt phụ lục nghiệp vụ ngày 17/10/2026. Dùng để đối chiếu ngày neo, khoảng chu kỳ, correction và cache read-only.