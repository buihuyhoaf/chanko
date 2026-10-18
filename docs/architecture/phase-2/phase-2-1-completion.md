# Phase 2.1 — Design System Foundation

18/10/2026, Asia/Ho_Chi_Minh. Branch `feature/phase-2-1-design-system`, base `5c6c980` from fetched `origin/dev-chanko-1x`. Status: implementation/build verification complete; STOPPED BEFORE COMMIT/PUSH/PR/MERGE for review.

## Checkout and integration

Work is in the isolated `chanko-work` checkout inside the ChatGPT project workspace. Original Android checkout is unchanged: it was on dev-chanko-1x at dc33400 with an untracked PNG and core/data build leftovers. No reset, clean, deletion or checkout was performed there. Synced sources are untouched. See ../phase-1/phase-1-summary.md for integration and runtime limitations.

## Source and scope

Baseline: Phase 0 source inventory S5, approved UI UX Design System v1.0, local preserved uiux-v1.0.extracted.txt (including flattened tables). UI-01–09 and PV-01/04/05/07 were read. No claim of fresh canonical Drive readback; local source is the previously recorded snapshot. Values below match that snapshot and the user's explicit requested baseline.

- UI-01/02: Light-only Material 3; dynamic/system dark overrides ignored through compatibility parameters. Brand #8965F6/#FF85B3, cream #FAF8F5, four accents and nine semantic signal colors. Signal colors do not automatically become small text colors. White/cream surfaces; no speculative pastel palette or new gradient surfaces.
- UI-03: all 13 approved Nunito sizes/weights/line heights mapped exactly. DisplaySmall/HeadlineSmall fallback to HeadlineLarge/TitleLarge (Reference). Financial styles preserve approved size/weight; their line heights and all zero letter spacing are Reference. No tabular-figure claim.
- Nunito from Google Fonts repository ofl/nunito/Nunito[wght].ttf, OFL license copied to Nunito-OFL.txt. Locally instantiated static 400/500/600/700/800 fonts avoid variable font support limitations on API24–25. Vietnamese letters and dong glyph checked in cmap; visual shaping still needs device checks. No network font provider.
- UI-04/08: component shape tokens, 4/8/12/16/24/32/48dp spacing, 20dp screen padding, 24dp section, 2/6/8dp elevation, 48dp touch token. Material shape mapping is Reference; Primary uses explicit 20dp radius and minimum 56dp height, permits expansion. Only existing primary buttons adopt this wrapper. Fields adopt input16; other layouts remain unchanged.
- UI-09: no scale override; styles use sp. Existing Material interaction semantics retained. No new animation or decorative motion; primary wrapper stays static, including reduced-motion settings. Tactile scale0.96/spring and full motion policy deferred until device validation; foundation does not claim the entire motion system is complete.

No new splash/mascot assets, tabs, routes, network/auth, Room/schema/migrations, DI, repositories, tests, ViewModels or cooling-off logic. Candy Pop 3D establishes color/type/shape here; clay assets and screen composition remain later work.

## Contrast decisions and visual effects

PV-01 remains open for final design approval. Black, #595959 muted, #767676 outline and #AA2635 error text are explicitly Reference implementation choices, not approved foreground HEX. Error #D94B57 remains a signal/container; Material error text uses the darker paired variant. Brand palette is unchanged.

Verification used WCAG sRGB luminance: all selected opaque text pairs >=5.094:1, above 4.5. Black on approved purple is ~5.26:1; white on purple is ~4.00:1 and is not used for new primary button text. TextButton/OutlinedButton labels and focused field labels use onSurface instead of purple on light surfaces. Outline #767676 against white/cream and focused purple against white/cream exceed 3:1. This measures token pairs, not every rendered state or overlay.

Expected visible changes: Nunito throughout theme consumers, cream/white surfaces, brighter purple banner/FAB, dark primary labels, larger primary buttons, updated card/input rounding, light presentation even with system dark enabled. No screenshot equivalence is intended. Existing splash gradients/mascot and manually specified screen colors remain legacy; no full-app AA certification is made. Review legacy splash alpha text separately. Standard Material components and existing single-line form inputs still need font scaling verification.

## Verification

Final run: assembleDebug, testDebugUnitTest, lintDebug, assembleDebugAndroidTest and debugCompileClasspath dependencies — BUILD SUCCESSFUL. Unit tests 8/8, no failures/errors/skips. Lint 0 errors/31 warnings, same categories/counts as Phase 1.3: GradleDependency13, UnusedResources9, NewerVersionAvailable5, MonochromeLauncherIcon2, AndroidGradlePluginVersion1, PluralsCandidate1. Core source is unchanged. No dependency upgrade. AndroidTest APK built; instrumented tests were not executed in this phase.

Temporary build logs and QA JSON were removed after review; key results are retained in this report. Phase 1 historical evidence and the Phase 0 source manifest remain intact. `git diff --check` passed. Earlier compile errors during implementation were corrected before the final successful run.

## Review and smoke gate

Before integration, run the new debug APK on a device at font scales 100%, 150%, 200% and with system light/dark settings. Check Dashboard, Add, Detail, Decision, Vault, keyboard/insets, back/restart/persistence and countdown. Inspect button multiline height, financial values, wrapping, input scrolling, focused/error/disabled controls, actual 48dp bounds, TalkBack labels/focus and system animation scale0. Existing splash motion and contrast are separate legacy concerns; the new foundation adds no motion. Measure rendered state-layer contrast before calling accessibility complete. Run pending migration/DAO instrumentation when installation permission is available, without clearing user data.

Review foreground/reference choices and visible theme changes first. Phase 2.1 smoke is pending; Phase 1.3 user-confirmed PASS does not certify this build. No Phase 2.1 commit/push/merge was made.

### Retained contrast measurements

Opaque pairs, WCAG sRGB relative luminance; these measurements do not certify rendered interaction states.

| Foreground | Background | Contrast |
|---|---|---|
| #000000 | #8965F6 | 5.256:1 |
| #000000 | #FF85B3 | 9.258:1 |
| #000000 | #8DCDF8 | 12.221:1 |
| #000000 | #FFD75E | 15.134:1 |
| #000000 | #6888C9 | 5.954:1 |
| #000000 | #78DCC3 | 12.824:1 |
| #000000 | #FFAA61 | 11.175:1 |
| #000000 | #E9A23B | 9.696:1 |
| #000000 | #E56B73 | 6.682:1 |
| #000000 | #D94B57 | 5.094:1 |
| #000000 | #269C78 | 6.109:1 |
| #000000 | #FAF8F5 | 19.810:1 |
| #000000 | #FFFFFF | 21.000:1 |
| #595959 | #FFFFFF | 7.005:1 |
| #595959 | #FAF8F5 | 6.608:1 |
| #AA2635 | #FFFFFF | 6.913:1 |
| #AA2635 | #FAF8F5 | 6.521:1 |
