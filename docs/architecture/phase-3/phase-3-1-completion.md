# Phase 3.1 — Network Foundation

Date: 18 October 2026 (Asia/Ho_Chi_Minh).
Base: `dev-chanko-1x` at `b378a52` (Phase 2.3 merged).
Branch: `feature/phase-3-1-network-foundation`.

## Scope and evidence

Read root AGENTS, Phase 0 README/source inventory/decision register/dependency graph,
gap matrix/plan/completion, TECH-01 v1.1, TECH-03 v1.1 (including final amendments),
and Functional Specification ADR-09. These are the checked-in extracted snapshots;
no claim that live Drive documents or deployed OpenAPI have been reverified.
The pre-existing dirty Phase 2 checkout and Splash checkout were preserved. A fresh
worktree was created from the fetched development branch before implementation.

Implemented `:core:network`, Retrofit 3.0.0, OkHttp 4.12.0, Kotlin Serialization
1.9.0 (compiler plugin follows existing Kotlin 2.2.10), HTTPS client factory,
generic JSON envelopes, bounded unified error decoding, typed HTTP/transport errors,
cancellation propagation, INTERNET permission, and app-root singleton Hilt providers.
Kotlin Serialization is the implementation choice allowed by TECH-01 §3.1 and this
Phase 3.1 authorization; it does not resolve unrelated TECH-04 decisions.

The app supplies disabled configuration. No approved environment URLs exist in the
available baseline: no production/staging/local backend address is invented. Replace
`NetworkModule.config()` only with a reviewed HTTPS URL later. See
[network configuration and semantics](../../../core/network/README.md).
Timeout defaults remain library defaults, not approved operational settings.
No logging, automatic mutation replay, redirects, authentication or refresh logic.
One-shot mutation bodies prevent even OkHttp's immediate 503 follow-up from sending
another write. Bodyless read status retries can follow OkHttp's built-in behavior;
there is no application retry scheduler.

## Boundaries and preservation

Actual new edge: `app -> core:network`. Network depends only on transport/serialization
libraries, with no domain/database/data/app imports. Existing edges remain unchanged.
DI composition stays in app; DTOs/results are transport contracts, not domain models.
No endpoint interfaces in production source. Test-only fixture endpoints are local HTTPS.
No repository migration, sync/outbox, authentication, Room/schema/migration changes,
UI/Splash/mascot/cooling-off changes, flavor changes, or TECH-04 CI/deployment decisions.
Phase 3.2 has not started.

## Validation

- Initial `assembleDebug testDebugUnitTest lint assembleDebugAndroidTest`: PASS
  (`BUILD SUCCESSFUL`, 321 tasks). This preceded the complete focused-test suite.
- Final `assembleDebug testDebugUnitTest lint assembleDebugAndroidTest`: PASS
  (`BUILD SUCCESSFUL in 3m 23s`, 324 tasks; 323 executed, 1 up-to-date).
  No compiler warnings/errors appeared in the final console log. Merged debug manifest
  includes INTERNET permission.
- Total unit tests: 22 executed, zero failures/errors/skips (11 existing + 11 new).
- Network focused tests: 11 executed, zero failures/errors/skips, HTTPS MockWebServer.
  Covers converter/envelopes/pagination, unknown error codes and eleven HTTP statuses,
  Retry-After, invalid/missing/oversized errors, 204, invalid success JSON, cancellation,
  connection/timeout results, disabled config, URL validation and containment,
  redirect prevention and immediate-503 mutation replay prevention.
- `git diff --check`: PASS. Changed paths reviewed against scope; no existing
  product Kotlin/resource or persistence files changed.
- Lint findings: zero network/data/database issues. App: zero errors, 36 warnings
  in the final report (dependency updates, plurals, unused resources, monochrome icons).
  Dependency notices include new serializer/OkHttp entries, not all legacy warnings.
  Existing stack is not upgraded and warnings are not suppressed.
- `assembleDebugAndroidTest` builds APKs; no device/instrumentation execution claimed.
- No backend/deployed API, environment URL, idempotency SLA or live contract acceptance
  is asserted by fixture tests.

## Conditional merge gate

Implementation is reviewable; acceptance is not globally PASS until final gates, CI
and review evidence are available. Base tree has no `.github` workflow. No speculative
TECH-04 CI pipeline is introduced. Check remote PR checks/reviews after creation.
Do not merge unless all automated/CI/review/acceptance gates are evidenced PASS.
No merge to `main`, and stop at Phase 3.1.

## Publication status

Implementation commit: `687e892`. Local gates passed as recorded above.
The user explicitly approved public publication of the Phase 3.1 code and report to
`https://github.com/buihuyhoaf/chanko`, branch
`feature/phase-3-1-network-foundation`, and a PR targeting `dev-chanko-1x`.
The previous automatic publication-approval blocker is resolved by that authorization.

Publication proceeds under the original conditional merge rule: no CI or PR review
PASS is claimed until verified remotely. No auto-merge is enabled without all required
acceptance evidence. Neither development nor main has been merged by this task.
