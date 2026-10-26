# Asset handoff
Canonical PNG giữ nguyên bytes user upload; không dùng ảnh mèo mua sắm trong synced sources. Các bản128/256/512 PNG/WebP là resize/format conversion lossless giữ nguyên nội dung/alpha/aspect ratio, không AI redraw. Neutral là vai trò trình bày tĩnh của canonical crossed-arms cat; chưa có sprite animation mới. Dùng contain, không stretch/crop mặt. Canonical gốc có alpha fringe từ nguồn; giữ nguyên thay vì tự sửa identity.

Material Symbols Rounded:21 SVG gốc tải từ google/material-design-icons, font weight400/fill0 theo family default; PNG72px render3x cho icon24dp, foreground#241B35. SVG giữ path upstream, native tint theo semantic foreground. Bản gốc/PNG đều kèm Apache2 license. Không gọi custom SVG decoration là Material icon.

Nunito-Variable.ttf lấy từ fonts folder của handoff v1.4 trên Drive, kèm SIL OFL. Không font fallback ngoài Nunito trong output đã render. Android kiểm glyph tiếng Việt/variable-font support của minSdk thực trước wire; không tự thay font.

Decorations `candy_stage` và `free_chat_badge` là vector gradient mới cho nền/biểu tượng tự do, SVG+PNG. Không hình sản phẩm thật hoặc happy BUY cat, không sparkle. Vai trò decorative, accessibility hidden. Clay3D identity đến từ canonical cat, không từ icon chức năng.

Manifest ghi path/hash/bytes/dimensions/mime/source/license/usage. screen_asset_mapping.csv là mapping CL/CD/NC và modal states. Asset ID không là Android resource name; coding agent chuyển theo naming repo, giữ mapping nguồn. Font/icon license phải giữ cùng ứng dụng/bundle theo yêu cầu license.

Không nhúng ảnh3 màn vào app làm UI. Không lấy mascot đã render lại trong corrected mockup làm canonical. Khi asset không tải: text fallback, không thay bằng con mèo khác. Product placeholder là icon inventory_2 trong pastel container; app thật dùng ảnh item theo capability riêng, không mascot.
