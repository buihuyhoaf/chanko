# R2.5 — Cleanup & Final Validation

## Baseline and evidence

Reviewed 19 October 2026 (Asia/Ho_Chi_Minh). GitHub PR #26 reports merged=true,
base dev-chanko-1x, merged_at 2026-10-19T14:09:03Z; merge SHA
0b6c0cc303236474cfff8533eb3a7f97a0d8059f. Fetch confirmed that development head.
Branch refactor/r2-5-cleanup starts there. Git history includes R2.1 #23/d2d00b4,
R2.2 #24/0c4477c, R2.3 #25/3b2b66a and R2.4 #26/0b6c0cc.

Read AGENTS.md, current CODING_STANDARDS.md, phase-0 README/source inventory,
UI UX v1.0 extracted reference, Login/Initial Setup implementation documents and
R2.1–R2.4 reports. Standards blob: 7494c749683e0cc479428cc54d536a36d8a297d6.
The checked-in standards have no version label: independent v1.1 provenance is
unverified. The referenced chat's available turns contain R2 handoffs but no R1
attachment; R1 topics were re-audited from current source and prior R2 matrices,
not certified as a complete line-by-line reconciliation with the original R1.
UI-10 Environmental Storytelling full specification remains unsynced in this
checkout. Earlier reports record its approval; absence of text is not revocation.
No scene/design change or claim of UI-10 compliance is made.

## Audit before changes

- Repository-wide symbol search found ItemImageCard only at its declaration:
  no production, test or preview caller. It is an internal app source function,
  not a published library API. Its unique URI/placeholder layout is unused.
- SetupScreen imports foundation.border without using it. AddItem imports
  CandyShape yet spells the same fully-qualified input token four times.
- Four feature primary-button calls already use CandyPrimaryButton explicitly.
  Shared card content was consolidated in R2.2. No additional behavior-equivalent
  duplicate composable warrants a shared wrapper; similar loading/error branches
  alone do not justify an abstraction.
- CandyCard has a preview at ComponentPreviews.kt:21 (correcting R2.2's inventory
  entry claiming none). Zero feature consumers for library primitives does not
  prove dead code: documented design-system APIs, previews and instrumentation
  consumers remain. ChankoMascot is an intentional empty-asset foundation.
- ItemRow is shared by Dashboard and Vault; CandyListCell/Card replacements change
  shape, insets, text, elevation or semantics. SetupButton/day sheet, Login legal
  dialog/Google button and Decision outlined action remain valid incompatible
  contracts. R2.3 is selective migration, not complete visual convergence.
- Token values and semantic aliases remain centralized. Equal radii have distinct
  roles; illustration dimensions, stroke widths, viewport bounds and row heights
  are not automatically spacing tokens. Setup reference foregrounds, Login brand
  typography/colors and Google branding are not safely replaceable with equal or
  nearby tokens. PV-01 remains open. No new token or arbitrary dp conversion.
- Presentation imports domain repositories/models, not data/database implementations;
  app DI legitimately binds data implementations. Domain is Kotlin/JVM without
  Android/Compose imports. Database entity and data-layer mapper files share a
  basename but do not duplicate the entity declaration. No boundary repair needed.
- Lifecycle-aware flow collection, lazy-list keys, ItemDetail's private timer read
  scope and AddItem SavedStateHandle keys/flows remain. Domain repository injection
  is valid. No use case, typed state wrapper, stability annotation or derivedStateOf
  is added without a demonstrated need.
- Main screens and design-system catalogs provide previews. Theme intentionally
  ignores darkTheme/dynamicColor for light-only MVP; a preview named Dark does not
  prove a second palette. Text/icons in reviewed production screens use resources
  or supplied data; sample strings are preview-only. The chip checkmark is a
  selection glyph with Material selected semantics, not untranslated microcopy.

## Changes and preservation

1. Delete app/src/main/java/com/hoabh/chanko/presentation/components/ItemImageCard.kt
   (61 lines; zero callers). Keep Coil dependency per requested dependency freeze.
2. app/src/main/java/com/hoabh/chanko/presentation/screens/setup/SetupScreen.kt:
   remove one unused border import.
3. app/src/main/java/com/hoabh/chanko/presentation/screens/additem/AddItemScreen.kt:
   four fully-qualified CandyShape.input references use the existing import.
4. This report.

No live component implementation, modifier order, value, resource, business rule,
navigation, SavedStateHandle, architecture, dependency or build configuration is
changed. No test change is warranted for an unreachable function deletion and
same-symbol/import cleanup. Existing restoration/deadline/submit tests are retained.
Expected visual impact: none by source equivalence and absence of callers;
runtime screenshot parity has not been measured.

## Validation checklist

| Check | Result and limits |
| --- | --- |
| R2.4 merged into required base | PASS, GitHub metadata + fetched Git head |
| Dead component cleanup | PASS for identified ItemImageCard; no exhaustive reachability proof |
| Duplicate/shared adoption audit | PASS scoped review; incompatible migrations explicitly retained |
| Tokens/resources/naming/API | PARTIAL, no new violations; retained findings below |
| Module boundaries | PASS scoped source/build review; domain repository injection valid |
| State/performance preservation | PASS source review + regression tests; runtime profiling unverified |
| Unit tests/build | See commands/results below |
| Lint/instrumentation compilation | See commands/results below |
| Accessibility/runtime/preview rendering | UNVERIFIED: no device launch, TalkBack, screenshot or 200% runtime test |
| UI-10 / labelled standards v1.1 | UNVERIFIED source/version coverage |

## Remaining findings and deferred work

Paths below are repository-relative; severities express impact in the next UI phase.
These pre-exist this PR, and none is introduced by the cleanup.

| Severity | Evidence | Disposition |
| --- | --- | --- |
| P3 API consistency | app/src/main/java/com/hoabh/chanko/presentation/components/ItemUi.kt:43 — ItemRow has no modifier | Retain callback signature; change only with a caller need and source-compatibility review |
| P3 preview coverage | app/src/main/java/com/hoabh/chanko/presentation/components/ItemUi.kt:24 and :43 — no dedicated ItemPage/ItemRow preview | Screen previews cover indirect use; add focused states when redesigning these components |
| P3 naming | app/src/main/java/com/hoabh/chanko/presentation/screens/setup/SetupScreen.kt:99 — InitialSetupScreen lives in SetupScreen.kt | Low-impact file-name mismatch; defer rename-only churn |
| P3 performance opportunity | app/src/main/java/com/hoabh/chanko/presentation/screens/decision/DecisionScreen.kt:35 — clock read at route | No measured bottleneck; retain R2.4 deferral instead of expanding state scope |
| P2 production integration, intentional dummy exception | app/src/main/java/com/hoabh/chanko/presentation/screens/login/LoginScreen.kt:71; presentation/navigation/NavGraph.kt:82 — bypass and dummy_account | UI exploration only; restore verified session flow before production |
| P2 end-to-end readiness | app/src/main/java/com/hoabh/chanko/presentation/screens/setup/SetupViewModel.kt:25 — unconfigured gateway returns UNAVAILABLE | Production navigation cannot demonstrate a complete setup journey; use previews/test fixtures for UI work pending separately scoped dummy-flow work |
| P2 verification gap | app/src/main/java/com/hoabh/chanko/ui/theme/Color.kt:23 — reference foregrounds/PV-01 | Full states/gradient contrast, TalkBack and large-font runtime checks deferred; not a proven contrast failure |

Lint findings deferred (all Warning / P3 in this review): GradleDependency at
`gradle/libs.versions.toml:3,7,8` (dependency freeze); PluralsCandidate at
`app/src/main/res/values/strings.xml:15` (review Vietnamese quantity copy before
resource API change); UnusedResources at `app/src/main/res/values/colors.xml:3–9`,
`app/src/main/res/values/strings.xml:25`, `app/src/main/res/drawable/ic_launcher_foreground.xml:1`,
plus binary `drawable-nodpi/chanko_splash_mascot.png` and
`drawable-nodpi/ic_launcher_foreground_img.webp` (no line numbers apply to binary
assets; retain design/launcher inventory pending ownership review).
MonochromeLauncherIcon at `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml:2`
and `ic_launcher_round.xml:2` is launcher enhancement work outside behavior-preserving
cleanup. These warnings are not build failures and are not claimed resolved.

Retained design differences: SetupScreen.kt:238 SetupButton 18dp radius; DashboardScreen.kt:95
raw mascot banner; LoginScreen.kt:118 branded button. These are design-contract
exceptions/deferred migrations, not cleanup defects. Missing mascot assets
(ui/mascot/MascotAssets.kt:30), UI-10 text, true Android process-death restoration,
recomposition counts/frame times and real-device UI acceptance require separate evidence.
No speculative abstraction, API/backend redesign, asset registration or design change.

## Commands/results

Working directory: chanko-r2-5. Logs retained outside repository under /private/tmp.

- git fetch origin dev-chanko-1x: PASS after network-enabled execution; fetched 0b6c0cc.
- Baseline: ANDROID_HOME=/Users/buihoa/Library/Android/sdk ./gradlew testDebugUnitTest assembleDebug --offline:
  PASS, BUILD SUCCESSFUL, 162 tasks executed, 24s.
- Final: ANDROID_HOME=/Users/buihoa/Library/Android/sdk ./gradlew testDebugUnitTest assembleDebug lintDebug assembleDebugAndroidTest --offline:
  PASS, BUILD SUCCESSFUL in 52s, 331 tasks (183 executed, 148 up-to-date).
  XML unit results: 87 tests, zero failures/errors/skips. Instrumentation APKs
  compiled only; tests were not executed. App lint: zero errors, 17 warnings;
  data/database/network lint: zero issues. Offline dependency-version suggestions
  are cached lint findings, not independently verified latest-version advice.
- Final XML aggregation used all **/build/test-results/testDebugUnitTest/TEST-*.xml;
  lint counts used each module build/reports/lint-results-debug.xml.
- git diff --check: PASS.
- Repository-wide rg caller/import/token/boundary searches and direct source review:
  findings/dispositions above. Import text scans are heuristics: delegated getValue
  imports are required even without literal getValue calls and were retained.
- Initial sandbox Gradle attempt could not open the cache lock. Approved cache
  access allowed execution; this was an environment restriction, not a failing test.
- No emulator/VM/AVD, install, device launch or connected test command executed.

## Readiness verdict

CONDITIONALLY READY for incremental UI/UX implementation with explicit dummy/sample
data. The foundation builds and preserves existing behavior; no observed cleanup
blocker requires another broad refactor. This is not production readiness or 100%
standards/design compliance. Sync UI-10 before scene work; resolve the relevant
visual contracts/PV items per screen. Existing unconfigured setup and dummy auth
mean a complete navigable dummy journey needs its own scoped implementation.
Keep backend integration and real-device acceptance as separate gates.
