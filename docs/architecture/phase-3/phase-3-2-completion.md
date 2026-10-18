# Phase 3.2 — Data Repository Foundation

Date: 18 October 2026, Asia/Ho_Chi_Minh.
Branch: `feature/phase-3-2-data-foundation`.
Base: `ad69984784bcacb561232b690fa5363edae8eb55`, `dev-chanko-1x`.

## Prerequisites verified before implementation

Read project/repository AGENTS, all Phase 0 baseline documents, TECH-01/02/03 v1.1
extracted snapshots (including effective final amendments), Functional Specification
ADR-09/SPEC-09, and Phase 3.1 completion report. Snapshot limitations remain explicit;
no live Drive/OpenAPI/deployed backend verification is claimed.

GitHub PR #16 is closed/merged, targets `dev-chanko-1x`, head `11d7a03`, merge commit
`ad69984`. Git fetch updated the stale local development ref; ancestry check confirms
`11d7a03` is an ancestor of that fetched development base. Prior report's draft status
is historical. No claim that missing historical CI/reviews retroactively passed.
A new feature worktree/branch was created from the fetched base; dirty older checkouts
were preserved. No direct development/main commits or merges were performed.

## Implemented scope

- Domain-owned read repository contract, stale cached/fresh/failed results and typed
  failures; domain has no Android, Room, Retrofit or Hilt dependency.
- Separate remote read and account-scoped cache source contracts in data; explicit
  DTO -> domain -> entity and entity -> domain mapping boundaries.
- Small cold Flow read policy: emit potentially stale cache, perform one fetch,
  replace snapshot only after valid server response, preserve failures without retry.
  Empty lists replace cache; null body and invalid mappings cannot erase it.
- Cache scope mismatch is rejected. Cache IO/corruption allows remote recovery;
  cache write failure remains diagnostic alongside fresh server data. Cancellation
  and unexpected programmer/collector exceptions are not converted to success.
- HTTP metadata is mapped without server message UI copy or automatic replay.
- Separate local-only manual draft read/save/delete contract, with no submit API,
  outbox, connection observer or remote dependency.
- `core/data/README.md` documents server authority, source isolation, atomic/versioned
  cache replacement obligations, mapping, errors and integration blockers.

No production source adapters/endpoints or app DI bindings are invented. Existing
ItemRepository/DAOs/entities/mappers/product behavior remain byte-for-byte unchanged.
No UI/Splash/mascot, auth, scheduler, schema/migration or dependency version changes.

## Blockers: stop the affected integration

Current Room v2 lacks account/server IDs, versions, cached timestamps and a distinct
manual draft store. Actual Room cache/draft adapters and durable image storage cannot
be implemented safely within the no-schema-change constraint. They remain BLOCKED on
reviewed schema, legacy/account ownership and logout/retention policy. No in-memory
production fallback pretends to provide durability. Focused fake-source tests do not
prove restart/migration/draft image persistence. Concurrent refresh ordering must be
provided by a reviewed adapter (serialize or atomically compare server versions).
No mutation cache invalidation is wired without approved resource/write contracts.

## Validation

`assembleDebug testDebugUnitTest lint assembleDebugAndroidTest`: PASS,
`BUILD SUCCESSFUL in 46s`, 328 tasks executed, using JDK17 and existing Android SDK.
The initial sandbox attempt could not open Gradle's cache lock; the authorized
unsandboxed run completed successfully without source changes to work around it.

32 unit tests, zero failures/errors/skips: 10 new data policy tests, 11 network,
11 existing app. Cases: stale->fresh, empty replacement, all transport failure types,
HTTP metadata/conflict, offline miss, account mismatch, malformed/null wire response,
cache failure/corruption, cache-write diagnostic, cancellation, cold explicit refresh.
AndroidTest APKs built; no device/instrumentation execution is claimed.

Lint: zero errors, 39 app warnings: GradleDependency 14, NewerVersionAvailable 10,
UnusedResources 11, MonochromeLauncherIcon 2, AndroidGradlePluginVersion 1,
PluralsCandidate 1. Core data/database/network reports have zero issues.
No compiler warning/error appeared in the build log. No warning was suppressed or
unrelated dependency upgraded; no assertion that all 39 are historical baseline.

Compile dependency reports for all four core modules: PASS. New edge:
`data -> network` (implementation). Existing `data -> domain/database` and
`database -> domain` preserved; network has no project dependencies; domain contains
only Kotlin/coroutines/annotations. No core -> app imports or cycles.
`git diff --check`: PASS. Reviewed changed paths: data build/contracts/policy/tests,
domain contracts and documentation only. App/database/network diff against base empty.

## Merge and acceptance

Local foundation gates PASS; production persistence integration remains explicitly
blocked above. Remote CI/review and final acceptance must be verified after PR creation.
No all-gates-PASS or durable-cache feature completion is claimed. Leave PR pending if
CI/review/acceptance evidence is absent. Merge target may only be `dev-chanko-1x`,
never `main`. Stop after Phase 3.2; Phase 4 has not started.
