# R2.4 — Compose performance & UI state

## Entry and audit

- GitHub PR #25 was merged into `dev-chanko-1x` at `3b2b66a7e00220d37d89db7c26f59996a24ee6e6` (2026-10-19 13:57:04 UTC). This branch starts at that merge.
- Read root AGENTS.md, CODING_STANDARDS.md, phase-0 README/source inventory and current ItemDetail, ItemViewModel, AddItem, Decision, Dashboard, ItemPage, domain model and existing tests. No R1 audit finding is assumed current.
- Standards provenance: the checked-in CODING_STANDARDS.md has no explicit version header. Its Git blob is `7494c749683e0cc479428cc54d536a36d8a297d6`; followed this current file, without asserting independently that it is labelled v1.1.
- Before editing: ItemDetailRoute reads the collected clock via delegated `now` while calling ItemDetailScreen. Screen uses that changing primitive for countdown and `canDecide`. ItemPage's content also formats price. Only route/previews call this screen. AddItem's four field keys are used only by its route/screen; action drives DONE navigation. Planned change: defer only the clock read; retain public screen APIs and all field/event APIs; add boundary/restored-handle tests.

## Evidence before / after

| Observation | R2.3 | R2.4 |
| --- | --- | --- |
| Snapshot clock read | ItemDetailRoute call argument | ItemDetailTimer, through a stable provider |
| Clock collection/lifecycle | collectAsStateWithLifecycle at route | Same; no new collector or change to STARTED/default policy |
| Clock source | Wall-clock sample, delay(1000), WhileSubscribed(5000) | Unchanged |
| Countdown/action | `(remainingMillis(now) + 999) / 1000`, `canDecide(now)` | Identical, one provider sample shared by both |
| Static content | In ItemPage content alongside timer | Same layout/content, outside timer restart scope |
| Public ItemDetailScreen | state, Long, callbacks | Signature retained for callers/previews; private provider path for route |

Compose compiler 2.2.10 reports, generated from local builds of both revisions:

```text
Before:
restartable skippable ItemDetailRoute
restartable skippable ItemDetailScreen (stable now: Long)
restartable skippable ItemPage
restartable skippable CandyPrimaryButton

After:
restartable skippable ItemDetailRoute
restartable skippable ItemDetailScreen (stable now: Long)
restartable skippable ItemDetailContent (stable now: Function0<Long>)
restartable skippable ItemDetailTimer (unstable item: CooldownItem, stable now: Function0<Long>)
```

These are compiler properties, not runtime recomposition counters. The original route is invalidated by each changed clock read; changing Long arguments prevent the screen call from skipping on that value. Descendants may still skip. No evidence supports calling the original a full-screen expensive recomposition. After the change, the clock snapshot read is inside the new restartable Timer scope. Inference from read placement and compiler scopes: clock-only changes invalidate that subtree rather than the route/static content. No claim of measured frame time, energy savings, or exact runtime recomposition counts. CooldownItem remains compiler-unstable across the module boundary; existing skipping support is sufficient here, so no speculative stability annotations/configuration.

Reproduce compiler evidence without repository build changes using a temporary Gradle init script:

```groovy
allprojects {
    plugins.withId('org.jetbrains.kotlin.plugin.compose') {
        composeCompiler {
            reportsDestination.set(file(System.getProperty('chanko.reports')))
            metricsDestination.set(file(System.getProperty('chanko.reports')))
        }
    }
}
```

Run `:app:compileDebugKotlin --rerun-tasks --offline --no-configuration-cache -I <script> -Dchanko.reports=<separate-output-directory>` for each revision. Reports were captured from R2.3 parent `075cd80` (ItemDetail source identical to merge base) and this branch. Compile only: unit-test compilation writes reports with the same module name and can overwrite the app text report.

## AddItem decision

Keep current flows and public API. A typed AddItemUiState would give named grouping and reduce five route collectors, but every edit already needs the form's validation and callback capture to update. No measured bottleneck or inconsistent-state requirement justifies combine/stateIn, a second initial/restored snapshot or changed subscription timing in this small form. A typed screen-only wrapper would duplicate the current values without a clear benefit. This is an explicit evaluation, not a requirement to migrate later.

Preserved exactly: SavedStateHandle keys `name`, `price`, `hours`, `reason`; empty defaults; raw text including whitespace/leading zeroes; setField synchronous update; validation/trimming on submit; duplicate-submit guard; transient action; LaunchedEffect(DONE) navigation; lifecycle collection; domain ItemRepository dependency. No pass-through use cases.

## Verification and changed files

- Baseline `testDebugUnitTest assembleDebug --offline`: PASS, 85 tests; 162 tasks executed.
- After `testDebugUnitTest assembleDebug --offline`: PASS, 87 tests, zero failures/errors/skips; 12 tasks executed, 150 up-to-date.
- Separate before/after compiler report builds: PASS (40 tasks executed each).
- `git diff --check`: PASS.
- No installation/device/emulator execution. Runtime behavior, Android process-death Bundle restoration, screenshot parity, frame timing and recomposition counts unverified.

Files:
- ItemDetailScreen.kt: private restart scope for countdown/action, deferred clock read, existing public API retained.
- CooldownItemTest.kt: countdown rounding around deadline and matching decision eligibility; existing tests cover finished status, restart and backwards clock.
- ViewModelTest.kt: existing-key initialization, raw values, live field update, reconstructed handle values and transient action reset. This simulates restored handle contents, not a framework Bundle save/restore.
- This report: audit, evidence, decisions and limitations.

## Risks and deferred findings

Low risk: only the read location changes. Timer uses the same snapshot once for both outputs; no layout node/wrapper, token, text, navigation, business, architecture, dependency or clock scheduling change. Loading/error/missing-item content does not read clock, while route collection remains active as before. The public preview screen still accepts a Long and is not intended as the optimized live clock entry point.

DecisionRoute also reads clock at route scope but has only action eligibility, not a countdown; a derived boolean could narrow invalidation. Deferred to keep this slice focused: no measured performance problem and changing action-state scoping would expand review. Dashboard/ItemRow review found no concrete low-risk performance defect warranting a change; no blanket remember/derivedStateOf or new abstraction. AddItem typed state, clock injection, runtime profiling and true Android restoration tests remain deferred unless separately justified. No unavoidable behavior/API break encountered.
