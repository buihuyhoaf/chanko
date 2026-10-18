# Phase 4 — Authentication Foundation

Date: 18 October 2026, Asia/Ho_Chi_Minh.
Branch: `feature/phase-4-auth-foundation`.
Base: `9e035aeb4f5be3f78455e312c43022d0dd5ee773`, fetched `origin/dev-chanko-1x`.

## Prerequisites and evidence

Read workspace/repository AGENTS, Phase 0 README/inventory/decision register and
baseline references TECH-01/02/03 v1.1, effective amendments, ADR-09/AUTH-05,
Phase 3.2 completion report and relevant existing auth/network/data code.
Historical conversation readback was also inspected. Snapshot extraction limitations
remain; no live Drive/OpenAPI readback or deployed backend verification is claimed.
AUTH-01..04 labels were not found in available snapshots or accessible bounded
conversation history. Their original separately approved texts remain unverified;
do not reconstruct them from assumptions. Google-only, challenge/nonce and token
policy are corroborated by TECH-01 section 6, TECH-03 section 3 and the current task.
AUTH-05 is explicitly present in Functional Specification. No client PAY_NOW creation.

PR #17 is merged: GitHub merge commit and fetched development history agree.
Ancestry includes actual implementation `f327ad4` and report `4171fa8`; the report's
older implementation SHA/draft status are historical, not current branch evidence.
PR #17 checks/statuses/annotations and submitted reviews were empty at inspection
(head `4171fa8`). Missing historical gates are not retroactively claimed PASS.
A separate feature checkout was created from the fetched development base.

## Implementation

- Domain repository, explicit loading/success/failure sign-in operation, account-only
  observable session state and sign-in/restore/refresh/logout use cases. No secrets
  in state, no Android/Retrofit/Room/Hilt imports in domain.
- Data-owned backend challenge/exchange/rotate/revoke ports. Internal timestamp and
  session models are adapter requirements, **not approved JSON schemas**.
- Nonce passed unchanged to Google provider; reject malformed/expired challenges,
  including expiry during credential acquisition. Backend must validate signature,
  issuer, audience, expiry, nonce binding and one-use challenge atomically.
- Session lifetimes bounded to access15min, refresh idle30d/absolute90d. Rotation
  preserves account and original creation anchor, rejects unchanged refresh tokens.
  Backend remains authoritative for expiry, reuse detection and revocation.
- Mutex serializes credential mutations and coalesces queued refreshes through the
  newly valid access token. Old credentials are durably removed **before rotation**:
  lost response, crash or cancellation cannot replay a consumed refresh token.
  This deliberately trades automatic recovery for requiring a new Google sign-in.
- AES/GCM encrypted AtomicFile in noBackupFilesDir, key in Android Keystore;
  read/atomic replace/clear outside Room. Version/format/content checked; failures
  fail closed. No plaintext fallback. Durable persistence precedes account publication.
- Logout clears credentials before network; offline/unconfigured failure gives
  NOT_CONFIRMED, only backend success gives CONFIRMED. Corrupt credentials can still
  be cleared; failed deletion reports STORAGE. Retires old unscoped credential file
  rather than guessing ownership. Cancellation propagates.
- Auth has no business-cache/draft/outbox deletion dependency. AccountScope is a
  backend-issued local partition key, never authorization. No account inference from
  email or legacy Room. Existing Room/drafts/outbox are retained, never auto-submitted.
- Hilt composition supplies secure store/clock/repository/use cases and explicitly
  unconfigured backend. Existing Login code is untouched; its compatibility binding
  returns NOT_CONFIGURED. Removed executable Retrofit annotations for unapproved
  legacy paths. Legacy DTO/repository/store source and characterization tests remain
  unbound compatibility material, not the production foundation or wire approval.

## Blocked production integration

No approved deployed endpoints, complete request/response/account/timestamp mapping,
Google OAuth client/package/certificate configuration, or environment evidence was
available. No production auth HTTP adapter is bound and no bypass/fake login exists.
App UI/navigation is not connected to the new session/use cases in this phase.
Server account + default PAY_NOW transaction, Google cryptographic checks, refresh
reuse revocation and authoritative authorization require backend implementation
and integration acceptance. Local tests cannot establish these properties.

Room v2 still lacks account ownership. Account-partitioned cache/draft adapters,
legacy ownership/migration and durable draft-image acceptance remain blocked as
reported in Phase 3.2. Retention here is structural: auth never deletes those data;
this does not claim the existing legacy screens implement multi-account isolation.
A future adapter must enforce scope in every query. Keystore restart, corruption,
backup/device transfer and failure behavior require device tests before production
acceptance. APK compilation and in-memory test doubles do not prove device durability.

## Validation and review

Final command: `assembleDebug testDebugUnitTest lint assembleDebugAndroidTest` plus
four core dependency reports, JDK17/existing Android SDK. PASS: BUILD SUCCESSFUL in
12s; 335 tasks, 53 executed/282 up-to-date. An initial sandbox cache-lock failure
required an authorized run with normal Gradle cache access. An intermediate compile
failure from an unavailable AtomicFile helper was fixed before final verification.

67 unit tests, zero failures/errors/skips, including 19 new foundation tests covering
nonce/expiry, rejection, persistence failure, restore/idle/absolute boundaries,
single-flight refresh, account/absolute-anchor validation, lost response/cancellation,
offline/confirmed/corrupt-storage logout, account switch, explicit operation states
and unconfigured fail-closed behavior. Existing tests retained.

Lint: zero errors, 40 app warnings (GradleDependency15, UnusedResources11,
NewerVersionAvailable10, MonochromeLauncherIcon2, AndroidGradlePluginVersion1,
PluralsCandidate1); data/database/network reports empty. No suppressions/upgrades.
No assertion that every warning is historical. No device/instrumentation execution.

Dependency reports PASS; no Gradle changes. Existing edges data -> domain/database/
network and database -> domain retained; network has no project imports; domain is
Kotlin/coroutines only. Static diff review confirms no UI/assets/Splash/mascot,
Room/schema/migration, business repository, dependency version or agent-skill changes.
Reviewed cancellation/rotation ordering, account validation, secret redaction and
logout semantics; this self-review is not independent approval. `git diff --check` PASS.

## Publication / merge policy

Target only `dev-chanko-1x`; never main. CI, independent review and acceptance must
be evidenced for the published head before merge. Missing evidence means leave PR
pending. Stop after this foundation; no Phase 5 or full auth UI/backend work.

### Published evidence

Implementation commit: `5ca72e76b7636b615ef4b7dc96c0980d3992466e`, pushed to
`origin/feature/phase-4-auth-foundation`.
PR: https://github.com/buihuyhoaf/chanko/pull/18, draft, base `dev-chanko-1x`.
For implementation head `5ca72e7`, remote checks/statuses/annotations, PR-triggered
workflow runs and submitted reviews are all empty. CI and independent review are
NOT evidenced PASS; backend/device/contract acceptance remains pending above.
PR retained as draft; no merge or auto-merge performed. This publication note is
a documentation-only follow-up; tested implementation remains unchanged.
