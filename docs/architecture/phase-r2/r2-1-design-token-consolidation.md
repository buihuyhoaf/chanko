# R2.1 — Design Token Consolidation

Baseline: `origin/dev-chanko-1x` at `b80e4fe` (2026-10-19).
Branch: `refactor/r2-1-design-tokens`; PR targets `dev-chanko-1x`.

## Sources and review before editing

Read repository AGENTS.md, CODING_STANDARDS.md (the v1.1 baseline requested by the user; the file itself has no version header), Phase 0 README/source inventory and S5 `uiux-v1.0.extracted.txt` (UI-01–UI-09). Read original R1 attachment from the referenced conversation. R1 recommendations are proposals, not approval to change appearance. UI-10 Environmental Storytelling — Independent Scenes is approved per the user's current instruction; its full specification is not synced in this baseline. No scene changes are needed here. PV-01 foreground mappings remain pending.

Inventory: CandyColors already aliases coolingOff/readyForReview/skipped/postponed to palette values. CandySpace has no duplicate members. CandyShape's equal radii have distinct semantic roles (small card/primary button, secondary button/input/thumbnail); retain these contracts. CandyElevation has three distinct 2/6/8dp values. No token definition, brand value or new token is needed.

## Before / after and changed source files

Paths below are relative to `app/src/main/java/com/hoabh/chanko/`.

| File | Before | After |
| --- | --- | --- |
| presentation/screens/login/LoginScreen.kt | Private LoginCream #FAF8F5; literal spacing | CandyColors.background, including fade endpoint; 11 CandySpace references |
| presentation/screens/setup/SetupScreen.kt | Literal spacing; two input radii 16dp | 28 CandySpace references; CandyShape.input for amount field and day input selector |
| presentation/screens/dashboard/DashboardScreen.kt | Literal padding/gaps, 20dp horizontal inset | 10 CandySpace references, including screen inset |
| presentation/components/ItemUi.kt | Literal padding/gaps | 3 CandySpace references |
| presentation/screens/additem/AddItemScreen.kt | Literal form gap | 1 CandySpace reference |
| presentation/screens/decision/DecisionScreen.kt | Literal loading padding | 1 CandySpace reference |
| presentation/screens/itemdetail/ItemDetailScreen.kt | Literal loading padding | 1 CandySpace reference |
| presentation/screens/vault/VaultScreen.kt | Literal loading padding/list gap | 2 CandySpace references |

Total: 57 spacing usages migrated, two shape constructions replaced, one duplicate color declaration removed (two usages). All resolved values are identical. Existing 24dp page padding stays 24dp; it is not changed to the default 20dp inset. No component, API, state, business logic, module, dependency, typography, icon or resource changes.

## Deliberately retained exceptions

- Setup Ink #30233E, Secondary #62596D, OnPurple #15131D and inactive step #D9D1E4 differ from existing semantic tokens. Login plum, legal text and wordmark accent also differ. Do not promote these to global tokens or substitute brand/ink colors without design evidence.
- Google button keeps its typography, 54dp minimum height, branding colors and fully rounded shape; CandyShape.chip describes chips, not Google buttons.
- SetupButton keeps radius 18dp; ItemImageCard keeps 12dp radius and 4dp elevation. Existing tokens would change appearance; dead-component deletion belongs to a later PR.
- Illustration/viewport bounds, fades, step markers, progress indicators, icon sizes, strokes and the 3dp notice rule are layout/drawing dimensions, not spacing. Even equal numbers do not justify mapping them to CandySpace or CandyElevation.
- Setup 56dp row heights are not primary-button sizes; existing component tokens retain their semantic ownership. No unrelated API cleanup or component migration from R1.

## Verification and visual impact

- `git diff --check`: pass.
- Source equivalence check: for each of the eight changed Kotlin files, resolve the added CandySpace/CandyShape/CandyColors references back to their literal values and omit imports; resulting source equals baseline exactly (accounting for removal of LoginCream). This checks that all edits preserve values and no behavior statements changed.
- `ANDROID_HOME=/Users/buihoa/Library/Android/sdk ./gradlew testDebugUnitTest assembleDebug --offline`: BUILD SUCCESSFUL, 162 tasks executed; 85 tests, zero failures/errors/skips. Initial sandbox attempt could not write the Gradle cache lock; rerun with approved cache access succeeded. No dependency or build configuration changed.
- No emulator, VM, AVD, device installation or app launch performed. Runtime, screenshot comparison, TalkBack and font-scale visual verification are unverified under AGENTS.md restrictions.

Expected visual impact: none, based on identical resolved values and unchanged modifier order. Residual risk: runtime rendering has not been compared on a device. Existing visual/design discrepancies above are intentionally preserved, not certified compliant. Future changes to shared tokens will now propagate to these usages as intended.
