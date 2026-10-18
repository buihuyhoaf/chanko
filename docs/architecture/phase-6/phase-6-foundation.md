# Phase 6 — Presentation State Foundation

Date: 18 October 2026, Asia/Ho_Chi_Minh.
Base: `568952c1fe19993571300768b8844404e459c77e` (`origin/dev-chanko-1x`).
Branch: `feature/phase-6-presentation-state`; PR target: `dev-chanko-1x` only.

## Scope verification and selection

Repository AGENTS, Phase 0 README/source inventory/decision register/dependency
graph, Phase 1 summary, Phase 3.2/4/5 reports, existing domain read/session
contracts, data read implementation and app presentation were inspected.
Git fetch and GitHub PR readback confirm Phase 5 PR #19 merged on 18 October;
568952c has e901523 (Phase 5) and 9f56bde (Phase 4) as ancestors. Reports' older
pending-merge wording is historical, not current branch status or CI evidence.
There is no nested AGENTS or CI workflow in the base tree.

No numbered Phase 6 roadmap was found in repository docs or the referenced
conversation (all available turns read). The conversation calls it the next
foundation and asks Work to determine scope. Live Drive searches for Chanko,
Roadmap and Android Base and a listing of the Should I Buy It? project folder
found no separate phase roadmap. TECH-04 folder again returned files=[]. This is
bounded discovery with current access, not proof no roadmap exists elsewhere.
Live TECH-01 text was fetched; TECH-01/02/03 metadata matches the versions in the
Phase 0 inventory. TECH-02/03 and UI details used here remain the repository text
snapshots; no full live readback of those sources or backend contract is claimed.

Chosen implementation slice, not a previously approved roadmap title:
**Presentation State Foundation**, grounded in TECH-01 §3.3–3.5 (MVVM, StateFlow,
presentation using domain contracts), §5/11 (read cache and stale-while-revalidate)
and §10/12 (safe unified error handling). It fills the missing presentation
adapter over Phase 3.2 ReadRepository and Phase 4 SessionState. It does not claim
all Android Base acceptance gates are now complete.

## Implementation plan and acceptance

- [x] Define presentation-only state/error types with no transport dependencies.
- [x] Adapt repository emissions; preserve same-account content on read failure,
  expose refresh/source/cache timestamp, distinguish failure from empty success.
- [x] Bind reads to session identity; cancel and reset on account change/logout;
  make refresh explicit and keep cancellation out of error state.
- [x] Test successful/failed/incomplete reads, refresh, account isolation,
  cancellation and diagnostic sanitization; build and lint the app.
- [ ] Required CI and independent review acceptance for the published PR head.

## Contracts and integration boundary

`presentation/foundation/ReadPresentation.kt` stays inside app; no empty shared
module or new dependency. ReadUiState is SignedOut / Loading / Content / Failed.
An empty list is Content, never inferred from a failure. Content records cache
versus server provenance, cache timestamp when supplied, in-flight refresh, and a
sanitized error category. A successful server read with cache-write failure stays
Content with STORAGE; it is not a failed server operation or durable-cache claim.

`presentationStates(sessions, refreshRequests)` is a cold Flow. A ViewModel should
collect once in viewModelScope and expose it as StateFlow; Compose should observe
with lifecycle awareness. Keep that collection active while the ViewModel exists
(e.g. SharingStarted.Eagerly) so account changes clear state even with no UI
subscriber. Retain the existing root authentication gate; a saved state or UI
error never grants access. No production feature is bound here because the four
tabs are foundation placeholders and reviewed production read adapters are still
pending. Tests exercise the real adapter with controlled domain sources.

One initial read per account; another read only on an explicit refresh signal.
Latest refresh cancels the previous read. Per-account content lives inside the
account flow; account change/sign-out cancels the old flow and resets it. The
caller owns coroutine lifetime and refresh event delivery; use an event flow
without replay so an old tap cannot refresh a later account. Do not save these
snapshots to SavedStateHandle. Cache is potentially stale, with no invented TTL.
Content remains displayable during a same-account refresh/failure. Refresh errors
are cleared while a new read is pending. No-data/cache-only flow completion is
INVALID_DATA, rather than a permanent loading indicator.

HTTP 401/403/409/429/5xx map to presentation categories; other statuses map UNKNOWN.
No raw message/code/request ID/retry-after/exception text enters presentation
errors. A 401 does not itself revoke a session, refresh tokens, redirect, retry or
replay anything. Feature UI copy and action policies are future work. Cancellation
is rethrown inside the read transform and checked before accepting source emissions;
it never becomes a Failed state. Cancelling the owning collection cancels its reads. Unexpected exceptions
become UNKNOWN, preserving content if any; no sensitive logging is added.

No business screen/UI, navigation, auth policy, backend endpoint, network retry,
Room schema/migration, draft storage/submission, scheduler, environment, CI
configuration or protected skill changes. TECH-04 and production adapter/schema
policies remain pending; none is silently resolved by this phase.

## Verification and merge gates

Final command, using installed Android SDK and Gradle cache:
`./gradlew test :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest --offline`
PASS. XML results: app 30 (12 new), data 38, network 11 executions across
variants; total 79, zero failures/errors/skips. Domain/database have no unit cases
here. App lint: zero errors, 17 existing warnings. No suppression or dependency
upgrade. `git diff --check` PASS. Static self-review covered account-local content,
latest-read cancellation, absence of retry/mutation, and sanitized error metadata.
This is author review, not independent approval.

AndroidTest APK compilation passed; no installation/device test, process-death,
backend integration or visual/accessibility PASS is claimed. This phase adds no
screen or rendered UI. Foundation adapters remain unbound to production business
features until resource-specific sources/contracts are ready.

Remote base readback still 568952c, protected=false; repository rulesets=[].
Absence of protection does not waive the user's CI/review conditions. Published
head gates will be recorded in the PR body after readback. Local verification is
not independent review or remote CI success.
Only merge to dev-chanko-1x when every required condition is evidenced for the
published head; otherwise leave the PR open. Never merge directly to main.
