> HISTORICAL v1.2 QA. Conclusions about missingv0.2/runtime blocking documents are superseded by current user instruction and v1.3 FINAL_QA_REPORT. Links adjusted for archive location; original v1.2 ZIP remains unchanged.

# FINAL_QA_REPORT — Chanko Chat Handoff v1.2

25/10/2026 · Asia/Ho_Chi_Minh. Scope: full handoff asset/visual correction and reference verification. No Android code, no PR, no production writes; SPEC/TECH and approved baseline files remain unchanged.

## Kết luận nhanh

- **NOT READY FOR UI IMPLEMENTATION** ở phạm vi bản cuối đã đối chiếu đầy đủ: phần asset/visual đã sửa và đạt kiểm tra reference; còn thiếu nguyên bản 34 quyết định, nguồn visual v0.2 và kiểm chứng Android runtime. Có thể dùng cho nhiệm vụ UI prototype với fake data được giao riêng, nhưng chưa gọi COMPLETE.
- **BLOCKED FOR PRODUCTION INTEGRATION**: R07 và các contract/test theo capability vẫn mở.
- Canonical gốc giữ nguyên byte; đủ 8 cặp PNG/WebP, 11 visual chính + 4 biến thể, Nunito/OFL và mapping chính xác. Không thay đổi SPEC/TECH baseline; 34 quyết định vẫn PARTIAL, 0 VERIFIED.

## Two required conclusions

**NOT READY FOR UI IMPLEMENTATION — final verified scope.** Static asset/presentation corrections are delivered and the reference checks below pass. However, the original bodies/options of 34 approved decisions and original visual-v0.2 exports/layers/metadata are still unavailable. Thus this package cannot certify the full approved UI or original visual parity. Android runtime accessibility is also NOT TESTED. A separately authorized fake-data UI prototype can use the corrected references with these limits; that narrower permission is not a COMPLETE certification.

**BLOCKED FOR PRODUCTION INTEGRATION.** R07 remains OPEN. Accepted lifecycle/retention/FK/history-read/active-operation/multi-device semantics and integration tests are absent; applicable Q01–Q11 capability gates remain OPEN/PARTIALLY SPECIFIED. The presence of a button or fixture screenshot never enables its production capability.

Overall: **NOT COMPLETE**.34 decision rows remain **PARTIAL, 0 VERIFIED**. Existing UX approvals are respected; no new decisions or reapproval were inferred.

## What changed

- Copied the original user-approved `mascot_skeptical.png` without opening/re-saving it. SHA-256 verified against the actual chat attachment: `c4b0de06a1175464607e80372f9615060b89892a16b58e8e4f57087c74e947d7`. Compatibility alias `canonical/chanko_user_approved.png` now has identical bytes. The old v1.1 hash was a different rendition and is no longer described as the original.
- Preserved all 8 expression PNG/WebP pairs from v1.1 unchanged. Visual identity retains ivory soft3D material, pink ears/nose/cheeks, dark eyes, two cheek strokes, proportions and tail. Original source approval is not individual derivative-pose approval. No new cat, replacement pose, unapproved mascot placeholder or image generation was used.
- Replaced all 11 current visual PNGs with editable HTML/CSS compositions using exact shipped assets and real local Nunito text. Added4 substate references: pre-verdict, WAIT, SKIP, history empty. Historical board/schematics/logs remain clearly separated from current targets.
- BUY uses the exact Neutral file on a white card, with the same type/colors/outlined navigation actions as WAIT/SKIP. No confetti, glow, celebration, reward sound, sale message, direct purchase or implicit business mutation.
- Kept brand#8965F6, cream/white/lavender/pink and original mascot material. Primary labels now use dark ink#151329 instead of white; gradients are decorative thumbnails only, never text backgrounds. Added accessible borders/state labels, font wrapping and large-font layouts.
- Bundled unmodified Nunito normal variable font, upstream metadata and OFL license, plus original reference SVG control icons. Current manifests/provenance/mapping and active checklist/coding prompt now reflect actual delivery.

## Canonical, pairs and alpha

[Asset checks](../10_ACCEPTANCE_QA/evidence/asset_checks.json) verify original/alias SHA and all 8 pairs. PNGs are RGBA with true alpha0–255; transparent coverage42.30–52.22%. Seven PNGs are 1231×1278, Concerned 1230×1278. Every WebP has lossless VP8L and decoded RGBA equals its PNG exactly, including alpha. Every PNG and WebP hash equals its v1.1 counterpart.

Inspected all 8 expressions beside the byte-exact original and on cream/dark/pink backgrounds, including32/40px fit previews. Curious/Thinking and Relaxed can be subtle at avatar size; explicit state text carries meaning. Full-frame aspect fit is specified; no cropping, recoloring or alpha cleanup was silently applied. Some very-low-alpha fringe pixels reach canvas edges, but alpha>10 silhouettes remain inside the canvas and no visible rectangle or main-silhouette clipping was observed at inspected sizes. Preserve masters; downsample at decode in the native image pipeline and inspect actual device rendering.

Provenance correction: these derivatives were generated in v1.1 against its near-identical canonical rendition, not newly generated against the byte-exact original in v1.2. The historical log is retained without rewriting that fact. Original3D/vector/animation masters are unavailable; this release explicitly delivers static assets and reduced-motion fallback, not an animated mascot.

## 11-screen verification

| Screen | Exact asset roles in current source | Result |
|---|---|---|
| S01 Empty | Neutral header, Welcome hero | PASS reference |
| S02 Free | Neutral header, Curious assistants | PASS reference |
| S03 Item verdict | Neutral header/card; same pose for BUY/WAIT/SKIP | PASS reference |
| S04 History | Neutral header; separate empty variant uses Relaxed | PASS reference |
| S05 Picker | Neutral header, no sheet mascot | PASS reference |
| S06 Offline | Neutral header/assistant, Relaxed banner | PASS reference |
| S07 Pending | Neutral header/assistant, Thinking status | PASS reference |
| S08 UnknownOutcome | Neutral header/assistant, Concerned error | PASS reference; new Send/picker disabled, draft editable |
| S09 Options | Neutral header | PASS reference; ITEM fixture, FREE hides View item |
| S10 Delete | Neutral header; no guilt/reward mascot | PASS reference; sample-data notice/R07 guard |
| S11 Archived | Neutral header/assistant, Sleeping banner | PASS reference; sample restore, disabled composer |

[Screen/slot map](../08_ASSETS/screen_asset_mapping.csv) lists 31 placements across 15 sources with exact paths, hashes, fit/anchor and fallback. [Mapping check](../10_ACCEPTANCE_QA/evidence/mapping_checks.json) confirms every source image matches its declared slot and hash; no alternative mascot source. [Visual inventory](../07_VISUAL_REFERENCES/visual_manifest.csv) hashes all 15 current references. [Coverage](../07_VISUAL_REFERENCES/STATE_COVERAGE.md) names unpictured combinations; the 11-state model and domain/API enums are unchanged.

## Accessibility evidence and limits

Method follows W3C [text contrast](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html), [non-text contrast](https://www.w3.org/WAI/WCAG21/Understanding/non-text-contrast.html) and [resize text](https://www.w3.org/WAI/WCAG21/Understanding/resize-text.html). Ratios are compared unrounded. Every active reference text pair is held to4.5:1, including larger text; relevant control shapes/icons/borders require3:1. This tests selected criteria, not blanket WCAG conformance.

[Contrast evidence](../10_ACCEPTANCE_QA/evidence/contrast_checks.json):27 explicit token/control pairs and 14 distinct browser text pairs,0 failures. Lowest observed text ratio is approximately 4.54249:1 (#151329 on#8965F6). White on the primary was approximately 3.9955:1 and is no longer used for labels. Dark text remains readable on white/cream/lavender/status/disabled surfaces. State/selection also has text/shape, not only color. Decorative gradients/mascot are not required to convey state.

[Layout evidence](../10_ACCEPTANCE_QA/evidence/layout_checks.json):15 references ×390/320px ×100/200% font =60 renders, local Nunito loaded in all60. Across 436 measured control instances the minimum width and height are48px.0 horizontal-overflow, clipped-text, broken-image or undersized-target findings; [overlap audit](../10_ACCEPTANCE_QA/evidence/target_overlap_checks.json) finds0 active-target overlaps. Buttons grow/wrap, dialogs/sheets extend vertically, and large-font headers/composers switch layout to retain reading width. Screenshots are full content, not fixed-height844px app canvases.

These are CSS reference pixels mapped to proposed dp at scale1. They do **not** prove Android48dp targets, sp scaling, native focus trapping/return, TalkBack order, contrast after theme transformation, IME/insets, rotation, lifecycle or runtime image memory behavior. Those tests require an Android implementation and are NOT RUN.100% and200% are measured; intermediate text scales and arbitrary content lengths are not exhaustively tested. [Rendering harness](../10_ACCEPTANCE_QA/tools/README.md) and editable sources are bundled for reproduction; no production interactions are implemented in them.

## Completeness, links and preservation

Full deliverable includes all 13 original handoff groups, updated current documents/assets, original source snapshots/history and new QA evidence; not only changed files. [Baseline preservation](../10_ACCEPTANCE_QA/evidence/baseline_preservation.json) compares 27 protected files byte-for-byte, including all01_PRODUCT_UX files, SPEC/TECH/DS/MyItems snapshots/dependencies, original request, user board and previous ZIP:0 changes. The34-row register and traceability are untouched; no body/source was invented.

[Link audit](../10_ACCEPTANCE_QA/evidence/link_checks.json) checks every parsed local Markdown file/image link, HTML/SVG/XML src/href and CSS url in the unpacked package. No broken active local target remains after final packaging. Anchors, if any, are listed separately. External URLs are recorded rather than falsely certified available/authenticated; upstream font and W3C sources were accessed for this task. Plain historical source paths, old generation-machine paths and links inside the preserved history ZIP are archival evidence, not active package dependencies. The new active provenance/source paths are portable.

[Checksums](../00_README/checksums.json) cover every package file except the checksum file itself. Final packaging re-reads every entry, compares each manifest hash, runs ZIP CRC validation, checks no duplicate/unsafe paths and validates the nested v0.3 ZIP CRC. Archive SHA-256 and packaging results are delivered beside the ZIP as `.sha256` and `.verification.json` sidecars to avoid a self-referential archive hash. [Verification summary](../00_README/verification.json) records the internal evidence scope.

## P0/P1/P2 disposition

| Review issue | Status after v1.2 | Residual boundary |
|---|---|---|
| RV-01 P1 BUY | Fixed in references | Native rendering/business guards untested |
| RV-02 P1 mapping/pose | Fixed; exact map31 placements | State overrides/fallback are explicit presentation proposals, Q09 owner/runtime acceptance still open |
| RV-03 P1 contrast | Fixed in reference colors/layout | Native theme/font/contrast verification required |
| RV-04 P1 approved decision evidence | OPEN — source blocker | Need original34 bodies/options tied to IDs; keep PARTIAL |
| RV-05 P1 R07 | OPEN — production blocker | Retention/FK/history reads/active operations/concurrency contract +tests |
| RV-06 P1 capability gates | OPEN — production blocker | Accepted Q contracts by capability; no invented API/schema |
| RV-07 P2 canonical provenance | Fixed | Original bytes and alias verified; derivative history disclosed |
| RV-08 P2 stale active docs | Fixed | Historical evidence intentionally retained/labeled |
| RV-09 P2 export/portability | Fixed for static handoff scope | Portable active paths/icon resources/manifests; full masters preserved and decode-size guidance supplied; native memory/halo QA NOT RUN |
| RV-10 P2 visual state coverage | Fixed for requested11 states +4 variants | Remaining combinations listed in coverage, not all rasterized |
| RV-11 P2 font/license | Fixed | Original font/OFL/metadata bundled; Android load NOT RUN |

No P0 established in asset/document review. Remaining **P1 final-UI evidence blockers**: original decision bodies and original-v0.2 export/metadata; native accessibility/repo acceptance is also pending. Remaining **P1 production blockers**: R07 and applicable Q contracts/tests. **P2 limitation**: animation/3D masters and animated behavior are not delivered; static/reduced-motion scope only. No missing file or unapproved substitute is silently treated as complete.

To close final UI certification, retrieve the already-approved source evidence, reconcile by ID/version without reapproving UX, and run the target-repo native acceptance checks in a separately authorized implementation task. To close production readiness, obtain accepted contracts and real integration evidence by capability. This release changes only the derived handoff/presentation artifacts, not the baseline.
