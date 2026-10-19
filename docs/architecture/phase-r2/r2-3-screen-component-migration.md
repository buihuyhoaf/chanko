# R2.3 — Selective screen component migration

## Baseline and scope

GitHub PR #24 was verified merged into `dev-chanko-1x` on 19 October 2026,
merge commit `0c4477c6ee29005b7b8ae59ad8606d639c70e3a5`. The fetched development
head equals that commit; this branch starts there. No implementation used an older baseline.

Read root AGENTS.md, CODING_STANDARDS.md (v1.1 baseline identified by R2.2),
R2.2 inventory, Phase 0 source inventory, preserved UI UX v1.0 snapshot,
Initial Setup and Login implementation documents, and mascot foundation report.
Checked actual post-R2.2 implementations in Components.kt, CardsAndChips.kt,
ListCells.kt, DialogsAndSheets.kt, ChankoMascot.kt and MascotAssets.kt against
screen call sites and ItemUi.kt. UI-10's full source is absent from this baseline,
as already recorded in R2.2; no scene or approved design interpretation changes.
Reference and Pending Verification values are not promoted to Approved.

## Migration matrix

Paths are relative to `app/src/main/java/com/hoabh/chanko/`.

| Usage | Before | After | Equivalence / disposition |
| --- | --- | --- | --- |
| presentation/screens/additem/AddItemScreen.kt: save action | CandyPrimaryButton imported as Button | Explicit CandyPrimaryButton | Same symbol, arguments, content, enabled predicate and callback; 1 call |
| presentation/screens/dashboard/DashboardScreen.kt: empty-state add action | CandyPrimaryButton imported as Button | Explicit CandyPrimaryButton | Same symbol and callback; 1 call |
| presentation/screens/decision/DecisionScreen.kt: buy action | CandyPrimaryButton imported as Button | Explicit CandyPrimaryButton | Same symbol, decision event and enabled predicate; 1 call |
| ItemDetail primary action | Already explicit after R2.2 | Unchanged | No duplicate migration |
| Setup retry / continue / save (2 source calls) | SetupButton | Retained | Deferred: 18dp radius, explicit OnPurple, lg content padding, weighted label, inline spinner. Candy primary uses 20dp radius, theme foreground, nested Row/Column and pending text loading |
| Dashboard mascot | Decorative cat_mascot Image, 96dp | Retained | Deferred: registry is empty; ChankoMascot produces Spacer. No equivalent registered state/asset; do not register legacy art or guess state |
| Shared ItemRow (Dashboard + Vault consumers) | Raw Card with optional clickable modifier, name + price | Retained | Deferred: CandyListCell changes text styles, spacing, bounds and actionable role. CandyCard adds 24dp shape, 2dp elevation and sm child spacing; CandyActionCard changes clickable semantics |
| Dashboard banner, two stat cards and empty-state card | Raw Material cards | Retained | Deferred: banner has primaryContainer/18dp inset; stat cards have no child gap; empty card uses section inset/md gap. CandyCard's fixed surface/shape/elevation/lg inset/sm gap does not preserve these contracts |
| Setup day selection sheet | Raw ModalBottomSheet + heading + LazyColumn/radio rows | Retained | Deferred: CandyBottomSheet changes cream container to surface, padding/spacing and lazy list sizing/scroll contract; wrapping weighted lazy content in its scrollable Column is not equivalent |
| Login unavailable-legal dialog | AlertDialog with text and one close action | Retained | Deferred: CandyConfirmationDialog requires title and two actions, stacked layout; adds different structure and semantics |
| Decision pass OutlinedButton | Raw Material outlined action | Retained | Deferred: Candy outlined introduces secondary shape, minimum bounds and nested content layout |
| Setup day selector OutlinedButton | Custom raw outlined selector | Retained | Deferred: custom selector shape, padding and label/trailing affordance cannot be passed unchanged through CandyButton's API |
| Login Google Sign-In | Branded OutlinedButton / icon / system font | Retained | Intentional branding exception; preserve loading, labels, bounds and interactions |
| Dashboard history / Login legal text actions and navigation controls | Material TextButton/IconButton/FAB | Retained | No equivalent contract established; outside the three safe primary symbol changes |

Source audit corrects the R2.2 count: Dashboard has **one** primary alias call,
not two. Three migrated calls plus unchanged ItemDetail = four feature calls.
This slice makes existing shared usages explicit; it does not claim any raw
Material usage was safely convertible. Deferred cases need a separately approved
visual/behavior contract or compatible primitive API before migration. No new
abstraction, breaking API, tokens, architecture, state, navigation, strings or
business changes were made. No incompatible swap was attempted.

## Validation and visual risk

`ANDROID_HOME=/Users/buihoa/Library/Android/sdk ./gradlew testDebugUnitTest assembleDebug --offline` passed: 162 tasks executed; XML reports confirm 85 tests, zero failures/errors/skips. `git diff --check` passed. The first sandbox attempt could not open the Gradle cache lock; the authorized rerun completed successfully. No tests were added for
symbol spelling: the resolved function and runtime arguments are identical.
No device/emulator installation or launch, instrumentation execution or screenshots.
Expected visual impact is none because the compiled call target remains identical;
visual/runtime parity remains unmeasured. Deferred visual inconsistencies predate
this PR. This is a partial R2.3 migration, with the remaining matrix explicitly deferred.
