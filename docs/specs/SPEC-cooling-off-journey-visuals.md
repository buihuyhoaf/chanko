# SPEC-cooling-off-journey-visuals: Chuẩn hóa Trạng thái & Thị giác Item Journey theo Cooling-off

**Mã phân hệ:** `cooling-off-journey-visuals`  
**Ngày tạo:** 07/11/2026  
**Trạng thái:** Proposed (Chờ duyệt theo Spec-Driven Development)  
**Tài liệu liên quan:** 
- `docs/architecture/phase-0/README.md`
- `core/domain/src/main/kotlin/com/hoabh/chanko/domain/model/FreezeModels.kt`
- `docs/ideas/item-journey-visual-effects.md`

---

## 1. Objective (Mục tiêu)
Loại bỏ hoàn toàn các trạng thái và màu viền tàn dư cũ (`Saved`, `Considering`, và verdict `BUY/WAIT/SKIP`) khỏi Item Journey trên màn hình Home. Đồng bộ 100% UI của Node với máy trạng thái Cooling-off (`ItemFreezeStatus`):
- Mọi sản phẩm sau khi thêm vào app đều bước vào **Cooling-off bắt buộc**.
- Tinh gọn hệ thống màu viền từ 6 màu phức tạp xuống còn **3 nhóm thị giác rõ ràng**:
  1. **Đang hạ nhiệt (Cooling-off / Freeze Active):** Viền thanh đếm ngược hình vòng cung (`Circular Progress Arc`), tông màu dịu mát, không xoay tít gây sốt ruột.
  2. **Sẵn sàng xem xét (Ready for Review):** Viền khép kín nổi bật (Mint Green), hiệu ứng nhịp thở nhẹ (Breathing Pulse) để mời gọi người dùng đưa ra quyết định mua hay bỏ qua.
  3. **Đã kết thúc hành trình (Purchased / Skipped):** Viền nét liền tĩnh, sắc thái trung tính nhẹ nhàng.

---

## 2. Commands (Lệnh thực thi & kiểm thử)
- **Unit Tests:**  
  `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:testDevDebugUnitTest --tests "*Journey*"`
- **Color & Architecture Gate:**  
  `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:testDevDebugUnitTest --tests "*ColorArchitectureTest*"`
- **Build Compilation Check:**  
  `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDevDebug`

---

## 3. Project Structure (Cấu trúc & Tập tin tác động)
```
app/src/main/java/com/hoabh/chanko/
├── presentation/screens/home/
│   ├── HomeUiState.kt                      # Cập nhật JourneyNodeStatus (loại bỏ Saved, Considering)
│   ├── components/
│   │   ├── ItemJourneySection.kt           # Canvas vẽ Circular Progress Arc & Ready Pulse
│   │   └── ItemJourneyNodeStyles.kt        # Cấu hình màu viền & badge tinh gọn
│   ├── domain/
│   │   └── HomeDomainLogic.kt              # Mapping từ candidate sang JourneyNodeUi mới
│   └── data/
│       └── HomeFakeDataSource.kt           # Cập nhật kịch bản Home (chỉ dùng CoolingOff & Ready)
app/src/test/java/com/hoabh/chanko/presentation/screens/home/
├── HomeDomainLogicTest.kt                  # Cập nhật test mapping trạng thái mới
└── HomeViewModelTest.kt                    # Xác minh kịch bản reconciled Cooling-off
```

---

## 4. State Machine & Visual Language Specification

### 4.1. Khai tử trạng thái cũ (Deprecations)
- **LOẠI BỎ:** `JourneyNodeStatus.Saved` ("Đã lưu").
- **LOẠI BỎ:** `JourneyNodeStatus.Considering` ("Đang cân nhắc").
- **LOẠI BỎ:** Khái niệm `DecisionVerdict.BUY/WAIT/SKIP` gắn liền trước Freeze.
- **LOẠI BỎ:** Animation xoay 360 độ liên tục vô tận (`rotationAngle`).

### 4.2. Bảng ánh xạ trạng thái mới (Mapping Matrix)

| Trạng thái Domain (`ItemFreezeStatus`) | Trạng thái UI (`JourneyNodeStatus`) | Nhãn tiếng Việt | Kiểu viền (Border Visual) | Màu viền (`CandyColors`) | Huy hiệu Badge (Góc trên phải) |
|---|---|---|---|---|---|
| `FREEZE_ACTIVE` | `CoolingOff` | "Đang hạ nhiệt" | Thanh tiến trình tròn (`Circular Arc`) giảm theo $\frac{t_{\text{remaining}}}{t_{\text{total}}}$ | `CandyPalette.Amber400` / `Orange400` trên track mờ | Icon Đồng hồ cát ⏳ (Sandglass) |
| `READY_FOR_REVIEW` | `Ready` | "Sẵn sàng xem xét" | Viền khép kín + Nhịp thở nhẹ (`Breathing Pulse` 2.5s) | `CandyColors.HomeV2.statusReadyRing` (Mint500) | Icon Dấu tích xanh ✓ (Checkmark) |
| `BOUGHT` / `UNFROZEN_EARLY` (khi mua) | `Purchased` | "Đã mua" | Viền nét liền tĩnh | `CandyPalette.Blue300` / `Slate400` | Icon Túi mua sắm 🛍️ |
| `PASSED` | `Skipped` | "Đã bỏ qua" | Viền nét liền tĩnh | `CandyPalette.Slate400` | Icon Dấu trừ − |

---

## 5. Visual Rendering Requirements (Yêu cầu hiển thị)

### 5.1. Circular Countdown Arc (`CoolingOff`)
- **Tọa độ bắt đầu:** Đỉnh trên cùng (-90° / 12 giờ).
- **Góc quét (Sweep Angle):**
  $$\text{sweepAngle} = 360^\circ \times \text{clamp}\left(\frac{\text{remainingSeconds}}{\text{totalDurationSeconds}}, 0f, 1f\right)$$
- **Nét vẽ:** `Stroke(width = 3.dp, cap = StrokeCap.Round)`.
- **Rãnh nền (Track):** Vẽ vòng tròn khép kín mờ bên dưới với `alpha = 0.15f` cùng màu để người dùng thấy rõ tỷ lệ đã hoàn thành.
- **Không xoay:** Tuyệt đối không xoay toàn bộ Canvas 360°, chỉ thay đổi `sweepAngle`.

### 5.2. Ready Node Breathing Pulse (`Ready`)
- Khi `isReducedMotionEnabled == false`:
  - `infiniteRepeatable` với `tween(1200ms, easing = FastOutSlowInEasing)` dao động `pulseAlpha` từ `0.4f` đến `0.9f` và độ dày bóng đổ từ `2.dp` đến `6.dp`.
- Khi `isReducedMotionEnabled == true`:
  - Giữ viền nét liền tĩnh không chuyển động, bảo đảm tiêu chuẩn tiếp cận WCAG.

---

## 6. Acceptance Criteria (Tiêu chí nghiệm thu)
- [ ] `JourneyNodeStatus` chỉ còn 4 giá trị: `CoolingOff`, `Ready`, `Purchased`, `Skipped`.
- [ ] Tất cả các sản phẩm trong kịch bản mẫu của Home đều bắt đầu bằng `CoolingOff` (với countdown) hoặc `Ready`.
- [ ] Viền của item `CoolingOff` thể hiện đúng tỷ lệ đếm ngược `remainingSeconds`.
- [ ] Item `Ready` có viền Mint nổi bật với hiệu ứng nhịp thở nhẹ nhàng.
- [ ] `./gradlew :app:testDevDebugUnitTest` PASS 100%.
- [ ] `./gradlew :app:testDevDebugUnitTest --tests "*ColorArchitectureTest*"` PASS 100%.
- [ ] `./gradlew assembleDevDebug` biên dịch thành công.
