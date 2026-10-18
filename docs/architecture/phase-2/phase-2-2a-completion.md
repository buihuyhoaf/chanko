# Phase 2.2a — Buttons, Cards & Chips

18/10/2026, Asia/Ho_Chi_Minh. Branch `feature/phase-2-2a-components`.
Status: implementation and build verification complete; STOP BEFORE COMMIT/PUSH/PR/MERGE.

## Phase 2.1 integration and review

Fetched origin before starting. PR #10 (https://github.com/buihuyhoaf/chanko/pull/10) is integrated in `origin/dev-chanko-1x`: merge `8067f05609d06a006577cc8fe34e9d0b87d579d0`, containing feature commit `38a80a7`. All 17 paths differing between the old local HEAD and integration were compared against the working files, including untracked fonts/tokens/docs: byte-identical. Local files were reconciled with the already integrated tree without creating a duplicate commit. This feature branch starts at that merge. No merge was performed in this task.

Read Phase 2.1 completion report, Phase 0 README/source inventory, local UI v1.0 snapshot (UI-08 and accessibility requirements), complete theme diff and screen changes. Scope matches theme/font/token foundation and primary-button/input styling; no business/data/navigation changes. No blocking issue found in that scope. Foregrounds and mapping remain Reference; do not relabel PV-01 as Approved. User confirmed Phase 2.1 smoke PASS; the historical report predates this confirmation. TalkBack and full-screen 150%/200% checks remain pending.

Reverified Phase 2.1 before branching: assembleDebug, testDebugUnitTest, lintDebug, assembleDebugAndroidTest PASS; 8 unit tests, zero failures. Lint: zero errors / 31 warnings.

## Survey and implementation

Existing `ui/theme/Components.kt` contains CandyPrimaryButton; `presentation/components/ItemUi.kt` contains business ItemRow; ItemImageCard is an existing screen-specific image component. Extend the existing primary wrapper instead of adding a second primary API. Leave screen-specific cards unchanged to avoid changing screens or business behavior.

- Components.kt: CandyButton with Primary, Secondary, Outlined, Text and Destructive variants; backward-compatible CandyPrimaryButton delegates to it. Explicit enabled/loading inputs; loading blocks duplicate activation, preserves action label, shows localized pending text below it and exposes polite state semantics. Static feedback adds no decorative motion. Material supplies click/focus/disabled semantics and interaction feedback.
- CardsAndChips.kt: neutral CandyCard and clickable/disableable CandyActionCard; labeled status chip for five existing UI statuses, plus selectable filter chip with visible check and selected semantics. Status enum is presentation-only, does not map or mutate domain state.
- Theme typography, color scheme, spacing and shape tokens reused. Primary minimum 56dp, other interactive primitives minimum 48x48dp; no fixed text heights/maxLines/font scale override. Noninteractive status chips need no touch target.
- ComponentPreviews.kt: gallery at 100%, 150%, 200%; source compiled, previews were not rendered/visually inspected in this task.
- CandyComponentsTest.kt: four focused instrumented tests for loading click suppression/label/bounds, filter selection/bounds, disabled card action and primary expansion at 200%. Compiled into AndroidTest APK; NOT executed successfully.
- strings.xml: localized pending label.

Reference implementation choices: neutral card padding16/gap8/elevation2; secondary radius16 reused for unfinalized Outlined/Text/Destructive radii; 1dp filter border; check glyph and pending microcopy. Only Standard White card foundation is implemented. Colorful Pastel and Gradient Hero variants, named business cards, screen adoption and tactile motion await their scoped follow-ups; no invented pastel/gradient tokens or mascot assets.

## Final verification

After final code edits: `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebugAndroidTest` succeeded. Unit tests 8/8, zero errors/failures/skips. `git diff --check` passed. No new dependency or Gradle setting.

Lint 0 errors / 31 warnings, delta 0 versus Phase 2.1. Categories unchanged: GradleDependency13, UnusedResources9, NewerVersionAvailable5, MonochromeLauncherIcon2, AndroidGradlePluginVersion1, PluralsCandidate1.

Connected-test attempt: Gradle runner failed to install test APK because device rejected INSTALL_GRANT_RUNTIME_PERMISSIONS. Its overall BUILD SUCCESSFUL is NOT evidence of passing instrumented tests. Normal `adb install -r` installed debug app successfully without clearing data; normal installation of test APK was rejected with `INSTALL_FAILED_USER_RESTRICTED: Install canceled by user`. No retry after device cancellation, no permission/security-setting changes. Four new UI tests remain unexecuted. Existing DAO/migration tests were not run.

Opaque text contrast uses the verified Phase 2.1 palette: black on primary5.256, secondary9.258, white21; status backgrounds minimum5.954; destructive white on errorText6.913. Existing outline/surface pairs exceed3. Disabled appearance is Material default, exempt from WCAG active-control contrast; rendered pressed/focus/selected overlays still need inspection. This is not full accessibility certification.

## Files changed

1. app/src/main/java/com/hoabh/chanko/ui/theme/Components.kt
2. app/src/main/java/com/hoabh/chanko/ui/theme/CardsAndChips.kt (new)
3. app/src/main/java/com/hoabh/chanko/ui/theme/ComponentPreviews.kt (new)
4. app/src/main/res/values/strings.xml
5. app/src/androidTest/java/com/hoabh/chanko/CandyComponentsTest.kt (new)
6. docs/architecture/phase-2/phase-2-2a-completion.md (new)

No Home/Splash redesign, mascot, Room/schema, navigation, cooling-off or business change. Synced sources untouched. No Phase 2.2a commit, push or PR made.

## Review/runtime gate

Allow test APK installation before running only CandyComponentsTest. Inspect long Vietnamese labels and money at 100/150/200%, narrow widths, keyboard/focus and actual bounds; verify all variants' disabled/loading/pressed/focus states and contrast, TalkBack announcements and reading order. Check that status labels convey meaning without color. Destructive confirmation belongs to each business caller and is not triggered by this primitive. Full-screen font-scale and TalkBack acceptance remain pending even after focused tests pass.
