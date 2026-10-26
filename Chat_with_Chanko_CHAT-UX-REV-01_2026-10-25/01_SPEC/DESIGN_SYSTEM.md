# Design System và accessibility
Nguồn: Design System v1.0 trên Drive đã đọc. Candy Pop 3D: hero/mascot có khối mềm; control/text là Material 3 thật. Light-only MVP. Không thêm dark setting hoặc 3D runtime.

## Approved tokens giữ nguyên
Brand primary #8965F6; secondary #FF85B3; yellow #FFD75E; mint #78DCC3; blue #8DCDF8; orange #FFAA61; background #FAF8F5; surface #FFFFFF. Primary gradient #8965F6→#BA85F5→#FFB4CF; Warm #FFAA61→#FFD75E; Cool #8DCDF8→#78DCC3. Stops Approved, góc/placement Reference.

Semantic: coolingOff #8DCDF8, readyForReview #FFD75E, purchased #6888C9, skipped #78DCC3, postponed #FFAA61, budgetWarning #E9A23B, overBudget #E56B73, error #D94B57, success #269C78. BUY không map sang success/purchased. Màu trạng thái cần icon/label và foreground contrast riêng.

Nunito ONLY: displayLarge36/44/800, displayMedium32/40/800, headlineLarge28/36/800, headlineMedium24/32/700, titleLarge20/28/700, titleMedium17/24/700, titleSmall15/22/600, bodyLarge16/24/400, bodyMedium14/20/400, bodySmall12/16/500, labelLarge16/24/700, labelMedium14/20/600, labelSmall12/16/700. Không khóa font scale. Gói font variable và OFL đi kèm.

Radius: hero32, card24, small20, primaryButton20, secondaryButton16, input16, chip pill, dialog28, sheet top28, thumbnail16. Spacing4/8/12/16/24/32/48, screen horizontal20, section24. Elevation2/6/8dp; shadow CSS chỉ Reference visual. Primary button min56; mọi target≥48×48; icons20/24/32, Material Symbols Rounded weight400, fill0 mặc định.

## Semantic pairing Reference mới
onSurface #241B35, textSecondary #544866, onBrand #171020, outline #7D6C99, softPurple #F0E9FF, softPink #FFF0F6. Chữ tối trên brand/pastel là lựa chọn accessibility Reference, không thay brand HEX. Error text dùng #A32232 trên #FFF0F2; error icon/outline dùng tone đủ 3:1 khi cần, không mặc định semantic red gốc đủ contrast. Focus ring #5B3BB5 trên surface; focus offset2, width2. Disabled vẫn có text lý do đọc được, không chỉ opacity thấp.

Gradient chỉ dùng hero/nút theo vùng chọn lọc. Nút brand reference dùng onBrand tối; không dùng white16 trên #8965F6 vì chưa đạt4.5:1. Tất cả pairing được tính trong `04_QA/contrast_report.csv`; công thức contrast là sRGB relative luminance, không tự chứng nhận WCAG toàn app từ palette. Kiểm native focus/motion/semantics vẫn cần implementation.

## Accessibility acceptance
- Text thường≥4.5:1; text lớn theo WCAG≥3:1; essential icons/borders≥3:1. Test vùng xấu nhất dưới gradient. Không coi mọi title là “large text”.
- TalkBack đọc sender, message, thời gian, delivery state; tên thread/type/status; tên item đầy đủ; selected chip/tab/radio; disabled action kèm lý do. Không đọc mascot/decorative blobs, không double announce text trùng.
- Focus thứ tự header→content→footer. Khi dialog/sheet mở trap focus, title được announce, đóng trả focus về trigger. Back đóng IME rồi modal rồi route theo native behavior; không mất draft.
- Font scale100/150/200%, width320/360/390/412, landscape, display size, IME, gesture/3-button navigation. Card/CTA tăng cao khi wrap, scroll tới được mọi nội dung. Không cắt giá, error hoặc tên món ở confirmation.
- Status không dựa màu; target48 dù icon24. Không action chỉ bằng long press/swipe. Không xác nhận destructive bằng gesture vô tình.
- Reduced motion dùng mascot tĩnh, tắt bouncy/transforms. Không confetti/sound BUY ở bất kỳ mode nào. Normal duration150/250ms Reference; baseline press scale0.96 chỉ khi phù hợp/reduced motion off.
- Nhận phản hồi announce theo đoạn hoàn tất; không theo token, không countdown live mỗi giây. Người đọc timeline cũ không bị giật xuống cuối.
