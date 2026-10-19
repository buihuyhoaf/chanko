# R2.2 — Shared Component Consolidation

Baseline verified on 19 October 2026: GitHub PR #23 merged into `dev-chanko-1x` at `d2d00b40a06517e7d8c83df1db4430d132b07c8a`. Branch `refactor/r2-2-shared-components` starts at that merge. Follow root AGENTS.md and CODING_STANDARDS.md v1.1. R1 attachment “Văn bản đã dán (1)(5).txt” was read as audit evidence, then checked against source; its recommendations are not approvals to change visuals. No architecture, dependency, token, navigation or business changes.

## Inventory and contracts (before → after)

Paths below are relative to `app/src/main/java/com/hoabh/chanko/`. Counts are source call sites, not runtime instance counts; previews and tests are separate from feature consumers.

| Component / definition | Feature consumers | Before → after contract |
|---|---|---|
| CandyButton — ui/theme/Components.kt | Indirectly via primary wrapper | Unchanged: onClick; modifier first optional; five variants; enabled && !loading gates activation; loading retains label and adds localized pending text/live region. RowScope content is rendered inside an inner Row. |
| CandyPrimaryButton — same file | 5 calls across AddItem, Dashboard (2), Decision, ItemDetail | Unchanged primary convenience wrapper; delegates to CandyButton. Keep both entry points, no deprecation. ItemDetail now names it explicitly; other 3 screens retain `as Button` alias for R2.3. |
| CandyCard — ui/theme/CardsAndChips.kt | 0 | Unchanged passive Card, ColumnScope, card shape, surface/onSurface, low elevation, lg padding and sm spacing. Duplicate inner Column now delegates to private CandyCardContent. No public generic card abstraction. |
| CandyActionCard — same file | 0 | Same content contract as passive card; clickable Material Card overload retains enabled/focus/action semantics and touch bounds. Uses the same private content function; no delegation between different Material overloads. |
| CandyListCell — ui/theme/ListCells.kt | 0 | Unchanged text-only title, optional supportingText/value, optional click, enabled applies to action; vertical text and button role when actionable. Not an ItemRow replacement. |
| CandyConfirmationDialog — ui/theme/DialogsAndSheets.kt | 0 | Unchanged controlled title/message/two labels/two callbacks; modifier first optional; destructive, enabled, loading, errorMessage. Loading blocks back/outside/dismiss action; enabled gates confirmation only. Stacked actions and scroll remain. |
| CandyBottomSheet — same file | 0 | Unchanged title/onDismissRequest; modifier then SheetState default; scrollable ColumnScope content, heading, Candy surface/insets. Caller owns visibility. |
| ChankoMascot — ui/mascot/ChankoMascot.kt | 0 | Unchanged state + modifier; decorative 120dp default Fit image, missing asset reserves Spacer. Asset registry is empty; do not swap Dashboard image into this API. |
| ItemPage — presentation/components/ItemUi.kt | 4: AddItem, Vault, Decision, ItemDetail | Adds first optional modifier on root Scaffold, default Modifier; title/onBack/ColumnScope content and content insets unchanged. All existing calls use trailing content and compile unchanged. This is an app-internal source API; positional third-argument content/function references outside this repository would need migration. None found. |
| ItemRow — same file | 3 calls: Dashboard, Vault (2) | Unchanged nullable trailing onClick, domain item and raw Card. CandyListCell adds role, bounds, different text order/styles; Candy cards add shape/elevation/spacing. No behavior-equivalent swap established. Modifier addition before optional callback could disrupt positional callers; defer API work rather than add overloads without need. |
| ItemImageCard — presentation/components/ItemImageCard.kt | 0, also no preview/test callers | Retain unchanged after assessment: imageUri/contentDescription, modifier, size default 120dp; 12dp crop card, 4dp elevation, blank URI cart placeholder. Unique image/placeholder behavior, not duplication of CandyCard; deletion is unnecessary to this thin slice. No dependency removal. |

Preview/test coverage inventory: CandyButton has 2 preview call sites plus wrapper and dialog internals; primary has 2 preview and 2 instrumentation call sites; CandyCard has none; action card has 1 preview/1 instrumentation call; list cell 3/2; dialog 2/2; sheet 1/1; mascot 1 preview call plus asset resolver unit tests. Existing previews are source artifacts, not evidence of rendered visual verification.

## R1 migration proposals re-evaluated / remaining usages

- SetupScreen: 2 SetupButton calls remain. Its 18dp radius, content padding, weighted label and busy spinner differ from CandyButton's shapes, inner Row and pending text. Consolidating requires an explicit visual/API decision; no such change is needed here.
- SetupScreen day sheet remains raw ModalBottomSheet: no heading, different container and content padding/scroll contract. Keep radio selection behavior.
- LoginScreen legal AlertDialog remains single-action, unlike two-action CandyConfirmationDialog. Google sign-in and legal text buttons retain their branding/layout.
- DecisionScreen OutlinedButton remains: Candy adds minimum bounds, secondary shape and inner content layout. Do not assume parity from the variant name.
- Dashboard keeps MascotBanner image, 3 raw cards (banner and 2 stats), floating action and history button; ItemRow keeps its raw Card. CandyCard has explicit low elevation/insets/spacing that these consumers do not share. Splash's scene image is feature-specific and remains unchanged.
- ItemPage's four consumers demonstrate default modifier compatibility through compilation. Only ItemDetail button spelling changes, resolving to the exact same function as the prior alias. No mass screen migration.
- CandyStatusChip and CandyFilterChip remain unchanged; no new public component, variants, domain coupling or state controller was introduced.

UI-10 is approved per user context, but its full source is not in the synced baseline. R2.2 does not edit scenes or reinterpret this missing source as absence of approval. R1's recomposition and state/repository findings are outside this PR.

## Verification and risk

`ANDROID_HOME=/Users/buihoa/Library/Android/sdk ./gradlew testDebugUnitTest assembleDebug --offline` passed: 162 tasks, 12 executed, 150 up-to-date; 85 tests, zero failures/errors/skips from XML results. `git diff --check` passed. No new tests mirror the extracted layout; existing behavior tests remain intact. All four ItemPage consumers compiled. No device/emulator installation or launch; instrumentation and runtime/screenshot comparison unverified.

Expected visual impact is none: card Column modifier order, arrangement, receiver and content stay identical; the helper adds a Compose call boundary without adding layout/semantics nodes. ItemPage defaults to the same empty modifier. ItemDetail resolves the same primary wrapper. Residual risk: Compose runtime rendering/skipping and screenshots were not measured. Current legacy image/placeholder and component inconsistencies remain visible debt, not regressions introduced here.
