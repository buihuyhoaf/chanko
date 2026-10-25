> v1.2 presentation: [accessible correction](V12_ACCESSIBLE_PRESENTATION.md) and exact [mapping](../08_ASSETS/screen_asset_mapping.csv) supersede asset/contrast/font gaps below. Reference values remain proposals; DS/SPEC/TECH baselines unchanged.

# Component visual specifications
A = source DS1.0 Approved value. R = reference proposed placement/dimension/copy, phân biệt visual approvalv1.2 với native dp/sp runtime. Source tại12/dependencies/design_system_v1_0.txt. Không đo screenshot để gọi dp approved.

| Token/component | Size/spacing/shape | Color/type | Behavior |
|---|---|---|---|
| Surface | gutter20, spacing4/8/12/16/24/32/48 (DS); 390dp canvas R | background#FAF8F5, surface#FFFFFF A | Adapt320dp+, no fixed page raster |
| Brand | Primary#8965F6, Secondary#FF85B3 A | Gradient stops#8965F6/#BA85F5/#FFB4CF A; direction/stops positions R | Không gradient cho money/data-heavy content |
| AppBar R | min56dp, icon visual24, hit48; header mascot40 R | TitleLarge20/28 weight700 A; foreground#151329 R | Back source; History/More labels; avatar decorative no extra focus |
| Timeline R | gap12, side gutter20, bubble max85%width; padding12–16 | BodyLarge16/24 w400 A; white bubble; user brand-soft container R | Stable IDs; wrap; no auto jump while reading |
| DecisionCard | radius24 A; padding16 R; min-height wrap | TitleLarge20/28; body16/24; white/neutral | BUY/WAIT/SKIP text label; no confetti, glow reward or celebratory mascot; no mutation on render |
| QuickReply | fully rounded A; hit≥48dp; gaps8 R | LabelMedium14/20 w600 A, purple-outline R | Horizontal wrap; fill composer P default, separate send; preserve text |
| Composer R | min56dp; multi-line expands to160dp then scroll R; Send48hit | Input radius16 A; BodyLarge16/24 | Trim validation; disable reason; IME safe area; no fake spinner |
| Sheet/dialog | top radius28 / dialog28 A; padding20 R | White; TitleLarge/BodyMedium14/20 | Focus trap, close returns focus; no two modals |
| Buttons | primary min56 A; radius20 A; secondary16 A; hit48 A | LabelLarge16/24 w700 A | Reflow200%; no auto business action from AI |
| History/Picker row R | min72; thumb48; gap12; padding12 | title17/24 weight700; meta12/16 | Tap ID, owner scope, explicit selection; total separate from loaded count |
| Error/offline R | padding16; icon24; radius20 | Readable neutral foreground; warning/error icon+text, not color only | Inline retains content; 403/428/unknown separated |
| Mascot slots R | header40; assistant32; hero160–220 | Approved asset identity required | Supplied exact static files; load failure→omit decoration with state text, never another cat |

Typography additional A: DisplayLarge36/44 w800; DisplayMedium32/40 w800; HeadlineLarge28/36 w800; HeadlineMedium24/32 w700; TitleSmall15/22 w600; BodySmall12/16 w500; LabelSmall12/16 w700. Foregrounds/on-colors not all finalized in DS; #151329 text used in schematics is R, contrast must be measured for actual pairs. Nunito normal variable font and OFL license are supplied in08_ASSETS/fonts; native loading remains untested. No library/version choice inferred.
