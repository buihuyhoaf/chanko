# Phase 5 — Navigation Foundation

Date: 18 October 2026, Asia/Ho_Chi_Minh.
Base: `9f56bde750dbafeaa9a24a9fad732c3252734002`, fetched development branch.
Branch: `feature/phase-5-navigation`. Target: `dev-chanko-1x` only.

## Baseline and scope

Inspected repository AGENTS (no nested AGENTS), Phase 0 README/source inventory/
decision register/dependency graph, TECH-01 Android stack and UI UX section 10.2
snapshots, Phase 1 integration summary, Phase 3.2 and Phase 4 reports, current auth,
DI, navigation, Activity and tests. Cached prior conversation contains only the
Phase 5 request. Live GitHub branch readback agrees with fetched history: Phase 4
PR #18 merged at 9f56bde; preceding Phase 3.2 #17 and Phase 3.1 #16 are ancestors.
Earlier phase merges #6–14 are in history. Historical reports' pending merge notes
are not current branch state or retrospective evidence that CI/review passed.
No live Drive or backend verification claimed; source snapshot limits still apply.

Approved: Kotlin/Compose/Material 3, MVVM/Clean Architecture, Hilt, StateFlow and
four tabs in order Home / My Items / Budget / Insights. Routes, glyphs and precise
stack behavior below are implementation choices, not new approved product rules.
One cohesive PR covers the small shell and its auth integration/tests; AGENTS does
not prescribe a numerical PR split. No new modules or dependencies are needed.

## Implemented contracts and behavior

- `TopLevelDestination` owns stable `home`, `my_items`, `budget`, `insights` route
  contracts and labels. No fifth tab, business actions or route payloads.
- Existing `dashboard` route is retained as authenticated shell entry for
  compatibility with the root contract; it no longer renders legacy business UI.
  Legacy Add Item/detail/decision/vault source and Screen constants are retained
  but not registered or reachable from this foundation host. No new business UI.
- Nested Navigation Compose host uses singleTop and saveState/restoreState on tab
  switches, anchored at Home. Reselect does not duplicate a destination. Back from
  another tab returns Home, then normal Activity back behavior applies. Material 3
  navigation items provide selected semantics, icon and label. Glyphs are Reference.
- Root keeps Splash/Login. Hilt session ViewModel observes the Phase 4 repository
  StateFlow and waits for restore before redirecting a restored protected stack.
  Protected content is not composed while restoration is pending or signed out.
  Repository availability, not a saved route or Login callback, grants access.
- LoginViewModel delegates to the same AuthenticationRepository, replacing the
  disconnected legacy interface in this consumer. No endpoint/credential policy
  added. Unconfigured backend still fails closed. Provider cancellation now uses
  the existing foundation GOOGLE failure mapping; corresponding test updated.
- Login clears Splash/Login history. Losing the session clears root history to
  Login; account identity keys the nested host so tab stacks cannot cross accounts.
  Same-account navigation save state is restorable only after repository restore.
- Navigation is app presentation/composition code. No domain rules in Composables;
  domain/data/network/database boundaries, Room schema, assets and skills unchanged.

## Verification

Final command (offline, local SDK/cache):
`./gradlew test :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest --offline`
PASS. XML results: app 18, data 38, network 11 test executions (including variants),
zero failures/errors/skips. Domain/database have no unit test result cases here.
App lint: zero errors, 17 warnings. No suppression or dependency upgrade added.
`git diff --check` PASS. Static review covered root history clearing, auth callback
bypass, account change keys, restoration timing and tab save/restore options.
This is self-review, not independent approval.

Four device test cases compile: tab order/selection/reselect/Back, saved-instance
restoration, and two startup cases covering data ready/failure, unauthenticated
protected navigation, callback-only denial, accepted repository session, account
switch reset, and sign-out history clearing. They have NOT executed successfully.
Connected Android 16 device was detected. Gradle connected test installation failed
with INSTALL_GRANT_RUNTIME_PERMISSIONS SecurityException despite reporting BUILD
SUCCESSFUL. Alternative `adb install -r -t` was also refused with
`INSTALL_FAILED_USER_RESTRICTED: Install canceled by user`. No retry after refusal.
No device smoke, actual process-death, accessibility or backend login PASS claimed.

## Merge gates

Live branch metadata: protected=false; repository rulesets=[] at inspection.
No CI workflows exist in this checkout. Neither absence of protection nor missing
checks counts as CI success. Publish PR, inspect checks/statuses/workflow runs and
reviews for its exact head. Leave open unless CI and independent review acceptance
are evidenced. Device smoke remains a validation blocker. No merge authorized
based solely on local checks. Remote gate evidence will be recorded in PR body.
