# Phase 2.3 — Mascot foundation (review required)

18/10/2026, Asia/Ho_Chi_Minh. Scope is foundation only, not completed final asset integration.
Branch: `feature/phase-2-3-mascot-foundation`, created from fetched `origin/dev-chanko-1x`
at `634b39573bd91b820d04f5aa4ae50dcf61268a64`.

## Phase 2.2b readback and authorization

- Read root AGENTS, Phase 0 README/inventory/decisions, preserved S5 UI-05/UI-07 source snapshot,
  Phase 2.2b completion report, and all four added source/test files. This is not fresh canonical Drive readback.
- GitHub PR #12 already merged into dev at `634b395` on 18/10/2026 at 15:31:37 local time.
  This session did not merge it. The clean feature checkout tree equals the merge commit tree
  (`35e17d5ba786e8a75436e875226f003834e4c9f5`); checked all five changed paths and whitespace.
  Diff stays within UI primitives, previews, focused instrumentation and completion documentation.
  No business, data, network, navigation, Gradle or palette changes found in that diff.
- Smoke PASS is user-reported. PV-01, TalkBack, font scaling and other manual accessibility checks
  remain Pending; no independent runtime/instrumentation PASS is inferred from smoke.
- GitHub head and merge commit: zero check runs/workflow runs; head statuses empty, merge combined
  status Pending with zero statuses. No review/comments returned. CI is unverified, not PASS.
- Branch API reports `protected: false`, required checks off; repository rulesets returns `[]`.
  Dedicated protection read returns 403 (integration lacks administrative access).
  An already merged PR does not establish that acceptance gates were satisfied.
- After this finding, user explicitly allowed foundation work for Phase 2.3 while CI remains Pending,
  with **no merge**. No branch protection/CI configuration changes were authorized or made.
- Rechecked Phase 2.2b: assembleDebug, testDebugUnitTest, lintDebug, assembleDebugAndroidTest
  succeed; then forced a fresh unit-test run (8/8). Lint 0 errors / 31 warnings.

## Contract and state mapping

`MascotPresentationState` is presentation only. It is not an API enum or a replacement for SPEC-07.
The nine business-emotion adapter remains Pending Verification; no inferred mapping is shipped.

| Presentation state (UI-05 Approved) | Registered final asset | Current output |
| --- | --- | --- |
| Welcome | None | Empty placeholder slot |
| Thinking | None | Empty placeholder slot |
| Sleeping | None | Empty placeholder slot |
| Curious | None | Empty placeholder slot |
| Concerned | None | Empty placeholder slot |
| Relaxed | None | Empty placeholder slot |
| Encouraging | None | Empty placeholder slot |
| Neutral | None | Empty placeholder slot |

Resolver policy is a Reference implementation: requested registered state, then registered static
Neutral, otherwise an empty slot. Never borrow another expression. Current catalog is deliberately
empty, including Neutral. This does not claim fallback art is approved.

An eventual catalog entry records packaged drawable, version, source, usage-rights evidence,
design-approval evidence, SHA-256 and source dimensions. Reviewer must verify these values against
the actual file; the metadata model does not prove ownership or approval. Additional handoff checks:

- Consistent white clay cat, round black minimal eyes, proportions/material/soft lighting across states.
- Pre-rendered transparent static raster, centered anchor, full figure, transparent outer padding;
  wrapper uses Fit without cropping or tint. Source/export sizes and compressed budgets need asset review.
- Check actual alpha edges on cream and pastel backgrounds; verify crop/anchor visually for all states.
- No shopping encouragement, Purchase celebration, shame, income ridicule or ungrounded dialogue.
- Static images are the reduced-motion representation; no animation or tap interaction is added here.

`ChankoMascot` is decorative with no TalkBack description or action. Caller must retain accessible
text for meaningful state, warnings and instructions. Missing assets are invisible Spacer placeholders,
not generated art. The 120dp default is a Reference implementation choice; callers may size, shrink or
omit the slot for narrow layouts/font scaling. No screens adopt this wrapper in this PR.
Previews cover all eight states at 100/200%, with explicit preview-only placeholder labels.

## Existing assets and provenance audit

| File | Observation | Disposition |
| --- | --- | --- |
| `drawable-nodpi/cat_mascot.webp` | 2048×2048; hasAlpha=no; visually white cat with shopping bags on purple background; SHA-256 `b6324fa53cb12dae8b0ee95ae5c61a20f000933f80fadff69afc4dfb51a06d41` | Not registered; opaque and shopping imagery needs design review |
| `drawable-nodpi/ic_logo.png` | 2048×2048 RGBA; hasAlpha=yes; SHA-256 `20411e809b9e9907b0588df6de84e24ecd0d7324a51da15ecebb775620bbdb5c` | Existing splash logo; not verified as a presentation-state asset |

Both traced to repository commit `c38dae5`. No state-specific design approval, originating source or
usage-rights evidence found in inspected repository documentation. Commit history is provenance of
repository introduction, not licensing evidence. Existing Dashboard/Splash usage remains untouched.
Nunito OFL covers the font only. No final image was created, modified, copied or guessed.

## Validation and acceptance

Final foundation run: `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug`,
`:app:assembleDebugAndroidTest` BUILD SUCCESSFUL. Tests 11/11 (existing 8 + resolver 3),
zero failures/errors/skips. Lint 0 errors / unchanged 31 warnings. Whitespace review passed.
Reviewed all five new files: three mascot source/preview files, resolver unit tests, this report.
No dependencies, screens or business/data/navigation files changed.
Instrumentation APK compilation is not runtime evidence. Previews compile but have not been rendered;
no visual/device PASS is claimed. Remote CI remains unverified pending PR head inspection.

Merge hold: user explicitly requested no merge; CI evidence is missing; final asset design, provenance,
rights, alpha/crop review and the business-emotion mapping remain Pending. Review the foundation
separately from final asset acceptance. No later phase is authorized.
