# Phase 2.2b — List Cells, Dialogs & Bottom Sheets

18/10/2026, Asia/Ho_Chi_Minh. Branch `feature/phase-2-2b-components`, based on fetched `origin/dev-chanko-1x` at `90622e2` (merged PR #11). Implementation and local automated gates complete. PR targets dev-chanko-1x; merge held for manual acceptance and remote review/check evidence. Main is untouched.

## Sources and scope

Read AGENTS.md, Phase 0 README/source inventory/decision register, preserved S5 UI v1.0 snapshot (UI-08, UI-09, accessibility and PV-01), Phase 2.1/2.2a reports and theme/component sources. Source snapshot is not a fresh canonical Drive readback. B10 remains Approved; V04/PV-01 remain Pending Verification. User explicitly authorized starting coding while they handle manual accessibility and PV-01. No pending item has been marked PASS or Approved.

A separate Git worktree was created beside chanko-work to preserve that checkout's uncommitted Phase 2.2a files. No synced sources changed.

## Implementation

- CandyListCell: neutral text-only cell, optional supporting text/value, optional caller-owned action, disabled semantics and suppression. No nested interactive slots; names and money use vertical flow to avoid competing horizontal constraints. Material/foundation provides focus, ripple and button semantics. Static cells have no click semantics.
- CandyConfirmationDialog: controlled modal with heading, message, optional announced error, primary/destructive confirmation and cancel. Stacked full-width actions; scrollable body; no fixed text heights or line limits. Loading preserves action label and pending semantics from Phase 2.2a, blocks both callbacks and Back/outside dismissal. Confirm never implicitly closes or mutates business state. Caller owns success/error state and dialog removal.
- CandyBottomSheet: Material modal, 28dp upper corners, heading and scrollable ColumnScope content. Material retains drag, dismiss, modal accessibility, system/IME insets and focus handling. Caller owns visibility, sheet state and all child actions; no blanket semantics merge hides child controls. Content is intended for finite column content, not nested unbounded LazyColumn layouts.
- Previews: cells/dialog/sheet at 100%, 150%, 200% (320dp narrow at 200%) and pending dialog. Sources compile; previews have NOT been rendered or visually verified.
- Five focused Android UI tests cover disabled cell/action/bounds at 200%, active cell callback, explicit confirmation/cancel behavior, pending action suppression and independent sheet child actions. Compiled into AndroidTest APK; NOT executed on a device.

Tokens from Phase 2.1 and CandyButton from 2.2a are reused. Interactive targets minimum 48dp, primary 56dp with growth. Body typography uses sp without scale override. Internal gaps/paddings, text-only vertical cell arrangement and stacked dialog actions are Reference implementation choices, not new Approved design decisions. Dialog uses platform modal behavior; sheet uses Material transitions/reduced-motion behavior, without custom decorative animation. Motion and focus restoration still need runtime validation.

No business/domain/data/auth/Room/navigation/cooling-off or screen adoption changes. No Gradle/dependency or palette change. New APIs alone do not expose a demo route in the app; inspect via Compose previews or the focused tests before integrating screens.

## Colors

Only existing measured theme pairs are consumed: black/white 21:1, muted #595959/white 7.005:1, error #AA2635/white 6.913:1, black/primary #8965F6 5.256:1; destructive white/#AA2635 6.913:1. Surface tint is transparent in the existing theme. No new foreground/pastel token or approval claim. Disabled controls retain Material behavior. Rendered pressed/focused/disabled states, sheet scrim/drag handle and PV-01 approval remain manual checks; opaque pair measurements do not certify the rendered UI.

## Verification and diff review

Final run of :app:assembleDebug, :app:testDebugUnitTest, :app:lintDebug, :app:assembleDebugAndroidTest: BUILD SUCCESSFUL. Unit tests 8/8, zero failures/errors/skips. Lint 0 errors, 31 warnings, delta 0 from Phase 2.2a: GradleDependency13, UnusedResources9, NewerVersionAvailable5, MonochromeLauncherIcon2, AndroidGradlePluginVersion1, PluralsCandidate1.

Initial preview compilation needed Material experimental API opt-in; corrected before final successful run. git diff --check passed. Reviewed all added source/tests and allowed path scope: only UI primitives/previews, focused instrumentation and this report. Dependency graph unchanged. Review is local author review, not independent GitHub approval. AndroidTest APK compilation is not passing instrumentation evidence. No device installation attempted, respecting prior installation cancellation recorded by Phase 2.2a.

No tracked GitHub Actions workflow exists in this base checkout. Remote checks and reviews must be read on the PR; absence of a workflow or status is not a CI PASS.

## Manual acceptance / merge hold

User to verify and confirm:

1. PV-01: approve/resolve existing Reference foreground choices and check rendered contrast for every interactive state. Do not silently change brand colors.
2. TalkBack: names, role/disabled/pending/error announcements, heading and reading order; separate sheet child focus, modal focus containment, Back/outside/drag dismissal and focus restoration.
3. 100/150/200% font scales and narrow/landscape windows: long Vietnamese names, money, errors and action labels remain readable; dialog/sheet scroll to all content/actions; no clipping; actual target bounds >=48dp.
4. Keyboard/system insets for sheet content, modal transitions with reduced motion; loading prevents duplicate action and dismissal, errors keep caller data, cancel remains usable once loading ends.
5. Run CandyListDialogSheetTest and existing CandyComponentsTest on an allowed test device. Both compilation and runtime results must be distinguished.

PV-01 and manual accessibility still affect acceptance under UI-09/B10. Therefore create PR and stop merge pending user confirmation, even though local automated gates passed. Do not enable auto-merge while this hold remains. Remote review/CI must also satisfy the user's conditions before any later merge.

## PR status

Created [PR #12](https://github.com/buihuyhoaf/chanko/pull/12), open, targeting dev-chanko-1x. Implementation commit `00fa631d59ef962b53c166d3c4a570cb2b3a5cf8`: GitHub checks/statuses and PR-triggered workflow runs are empty at inspection; CI is unverified, not PASS. No independent review approval obtained. No merge or auto-merge enabled. Merge remains held for the acceptance items above and review/CI requirements.
