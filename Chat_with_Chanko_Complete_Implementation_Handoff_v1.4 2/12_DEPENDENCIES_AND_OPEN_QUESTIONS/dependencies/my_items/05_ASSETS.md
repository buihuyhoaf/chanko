# Asset manifest và tích hợp Android

Root bàn giao: thư mục `chanko_android_v1.0/` trong ZIP; mọi asset path tương đối với root này.

`assets/manifest.json` ghi path tương đối từ root, kích thước, dung lượng và SHA-256. Đây là resource bàn giao, không phải code ứng dụng đã implement.

| File từ root | Kích thước | Dùng cho |
|---|---|---|
| assets/source/product_placeholder.svg | viewBox512×512; source vector | Bản gốc hình học, chỉnh sửa/export; Android Image không mặc định đọc SVG |
| assets/android/res/drawable/ic_product_placeholder.xml | viewport512×512; intrinsic96×96dp | Android Vector Drawable ưu tiên, scale theo container; không cần thư viện SVG |
| assets/android/res/drawable-nodpi/product_placeholder_256.png | 256×256px RGB opaque | Thumbnail fallback, kích thước UI72dp Reference |
| assets/android/res/drawable-nodpi/product_placeholder_512.png | 512×512px RGB opaque | Card/preview trung bình |
| assets/android/res/drawable-nodpi/product_placeholder_1024.png | 1024×1024px RGB opaque | Detail hero, đủ nét ở điện thoại mật độ cao; không tự tải ảnh mạng để thay |

Placeholder là icon khung ảnh trung tính màu#77737E trên nền#F0EFED; không mô phỏng bất kỳ sản phẩm thật hoặc mascot. Không chứa text, nhãn giá, thương hiệu hoặc túi/tai nghe/giày. PNG cố ý opaque để fallback ổn định trên nền trắng/cream, không phải cutout nền trong suốt. SVG và Vector Drawable có góc ngoài bo; PNG phủ nền toàn hình, container UI clip radius để thống nhất. Hai nguồn vector và raster cùng motif, có thể có khác biệt nhỏ ở rasterization; không yêu cầu pixel-identical.

## Đặt vào app khi được giao triển khai

Sao chép tài nguyên được chọn từ `assets/android/res/` vào module Android có sẵn, giữ tên lowercase_underscore. Không cần ship cả ba PNG nếu Vector Drawable đủ. Dùng `ic_product_placeholder` cho fallback tại thumbnail/hero; nếu dùng PNG nodpi thì layout tự quy định dp, không giả đó là mdpi. Không nhét1024px vào drawable-mdpi hoặc tự suy kích thước UI từ pixel.

Hero ratio4:3 Reference dùng container neutral và **Fit**, không stretch ảnh vuông. Thumbnail72dp clip radius16; hero clip radius24. Trong handoff/preview Product Hero chỉ dùng dummy từ manifest. Khả năng dùng ảnh thật trong app theo SPEC nằm ngoài asset preview này; placeholder không tạo item_images record giả. Mất ảnh hoặc quyền URL hết hạn vẫn render fallback, không đổi item data. Không hard-code signed URL vào source/resource hoặc lưu vĩnh viễn trong Room.

## Mascot canonical

Không có ảnh mèo canonical trong bundle này. `manifest.mascot.status` ghi thiếu; đây là asset dependency còn mở, không phải lỗi cần thay bằng chó hay mèo mới sinh. Khi nhận nguồn, ghi vào manifest: filename/state/emotion, alpha, source size, export size, anchor/crop, checksum, version, tác giả/quyền sử dụng. Static transparent PNG/WebP pre-render phù hợp; không real-time3D engine. Fallback reduced motion dùng pose Neutral tĩnh đã duyệt; asset lỗi thì giữ speech text và bỏ hình.

Chỉ cho phép hero MyItems, empty state, supporting speech dưới status. Product media luôn riêng; không composite mèo vào/cạnh ảnh sản phẩm. Khoảng cách thị giác phải làm rõ đó là speech section, không thành nhân vật giới thiệu sản phẩm.

## Quyền sử dụng

Các5 placeholder files là hình học gốc được tạo cho bộ bàn giao, không dùng ảnh stock, brand, font hoặc tác phẩm bên thứ ba. Chúng được cung cấp theo CC0-1.0 trong phạm vi quyền có thể từ bỏ; xem `assets/LICENSE.txt`. Không bắt buộc attribution. Không khẳng định quyền cho mascot chưa được cung cấp.

Nunito và Material Symbols không nằm trong ZIP. Coding agent dùng bản đã có quyền của repo và giữ thông báo OFL/Apache tương ứng với tài nguyên thật nhận được; xác minh license package cụ thể. Không coi việc đề cập tên font/icon là đã bàn giao file hoặc giấy phép.

Snapshot Drive trong sources là nội dung dự án phục vụ nội bộ, không thuộc CC0 của placeholder. Không phân phối public hoặc đưa vào APK. Bộ âm thanh có sẵn không được sao chép, sửa hoặc tái cấp phép trong bundle này.

## Kết quả review v1.0

Đã xem trực tiếp PNG512: khung ảnh hình học trung tính, nền xám nhạt, không mascot hoặc sản phẩm cụ thể. Kiểm tra cả3 PNG: kích thước/IHDR, CRC, giải nén RGB, checksum khớp; SVG/Vector Drawable parse được. Chưa compile bằng Android aapt hoặc chạy UI vì không có repo/build.

**Cần asset mèo canonical**: file gốc được chủ sản phẩm xác nhận (cùng nhận dạng Chanko), phiên bản, state/pose, alpha, kích thước, nguồn/quyền sử dụng và reduced-motion pose. Chỉ mô tả “mèo trắng” không đủ xác nhận canonical. Không sinh/tải một con mèo khác để lấp slot. Thiếu asset chặn visual mascot final, không chặn placeholder hay review nghiệp vụ.

## Approved Empty State composition (Recovery Baseline)

ITEM-06 yêu cầu Chanko canonical và hộp kẹo. Bundle chưa có đúng composition/asset này: cần file canonical hoặc các lớp được xác nhận cùng provenance. Không dùng icon hộp kẹo thay mèo, không sinh một mèo mới để hoàn thiện composition. Thiếu asset chặn visual final của Empty State/hero/bubble, không chặn dummy Product Hero. Các5 placeholder giữ nguyên bytes/checksum và CC0 như v0.9; không có asset mascot mới được thêm trong recovery.
