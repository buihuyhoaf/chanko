# Phase 2.4a — Splash UI (review / merge hold)

18 October 2026, Asia/Ho_Chi_Minh. Scope ends at Splash; Phase 2.4b is not started.

## Baseline and approval

Read project/repository AGENTS, preserved UI-01–09 snapshot and Phase 2.3 foundation report.
Fetched dev-chanko-1x and branched feature/phase-2-4a-splash from b378a52a65e0db0c9923c27333c91069ee54ae5c.
PR #13 is confirmed merged, with foundation only; no separate 2.3b report was found.
User explicitly approved the local source image for Splash, requested its rename to
chanko_splash_mascot_source.png, and then authorized continuing Phase 2.4.
This resolves source selection for this screen; it does not approve eight state assets.

## Implementation

- Warm cream theme background, purple hero using existing primary, hero radius, medium elevation and spacing tokens.
- Existing Vietnamese copy rendered as actual Nunito theme text, heading semantics, centered wrapping text.
- Decorative transparent mascot uses Fit with padding and no TalkBack announcement. Short windows shrink art to 120dp; normal placement 220dp is Reference.
- Safe drawing insets and vertical scrolling preserve access to text at large scales/landscape.
- Static presentation for all motion settings: no looping decoration, fabricated progress or new delay.
- Existing 1800ms navigation callback delay retained independently of content; rememberUpdatedState avoids stale callback without restarting timing. NavGraph and destination/back stack unchanged.
- Four previews: compact, 150%, 200%, landscape 200%. Two instrumentation tests cover timing/latest callback/single completion and accessible large text.
- No business, Room, auth, network, cooling-off, dependencies or other screens changed.

## Asset derivation and limits

Source: user-selected local chanko_splash_mascot_source.png in the original Android repo.
Tool: built-in image_gen, transparent background enabled. Output copied into drawable-nodpi/chanko_splash_mascot.png.
1151 × 1367 RGBA, alpha verified by sips, approximately 1.3 MiB.
SHA-256: a380cbe683a4506625b72d8399556fdb154138fc879423dceb474ae15957c686.
Inspected generated image: complete white cat, no mockup text/background/shopping bag; source identity retained approximately.
Derivative has visible fur detail from the approved source; alignment with the original clay-only design intent requires user visual acceptance. Source rights are user-provided and not independently established.
This derivative is for review and is deliberately NOT registered in the shared approved-state mascot catalog. No eight-state or emotion mapping approval is inferred.

Generation prompt:

> Use case: background-extraction / precise-object-edit. Edit target is the supplied approved Chanko splash reference. Produce ONLY its white cute round cat mascot, full body centered, front facing friendly welcome pose with relaxed empty paws. Preserve recognizable round body, white fur/clay softness, round black eyes, pink inner ears/nose/paw pads and proportions of this exact reference cat. Remove the shopping bag and all products from its hands; reconstruct natural empty paws. Remove ALL text, UI, status bar, progress indicator, background, floor, objects, floating symbols, shopping imagery. Real alpha transparent background, no checkerboard, no colored backdrop. Soft premium 3D material and gentle lighting; no new accessories, no extra characters. Full figure including ears and tail with transparent padding, no cropping. Static welcome mascot for a mindful spending app. This is a derivative for review, not a new expression set.

## Verification

Final assembleDebug, testDebugUnitTest (11/11), lint and assembleDebugAndroidTest PASS.
Lint: 0 errors, unchanged 31 warnings. Diff and whitespace author review PASS; no out-of-scope tracked paths.
Previews compiled, not rendered; generated art inspected separately. No rendered Compose/device visual approval is claimed.
Connected test task misleadingly returned BUILD SUCCESSFUL while device installation failed with SecurityException for INSTALL_GRANT_RUNTIME_PERMISSIONS. Do not count that Gradle invocation as executed tests.
Workaround succeeded without changing security settings: installed test APK and final app Debug APK with adb install -r (no grant-all flag), then ran only SplashScreenTest through AndroidJUnitRunner. Actual device result: OK (2 tests), including the existing-delay/latest-callback and 200% accessible-text tests. Device: 25113PN0EG, Android 16. No database tests or data-clearing commands were run.
Independent review, CI and manual acceptance remain pending. No auto-merge or main changes.

## User smoke checklist (after installing this branch build)

1. Cold/warm launch: Splash leads to Dashboard after the existing approximately 1.8-second Compose delay; Back does not return to Splash.
2. Check mascot face/proportions and transparent edges on purple; approve or request revision of the derivative.
3. At 100%, 150%, 200% fonts and portrait/landscape, text is readable and scrollable without clipping or system-bar overlap.
4. TalkBack reads Chanko and copy without a duplicate decorative mascot announcement.
5. Disable system animations: content remains static and navigation timing/destination unchanged.

Stop after Phase 2.4a. Merge only after CI, independent review and all acceptance/guardrails PASS.
