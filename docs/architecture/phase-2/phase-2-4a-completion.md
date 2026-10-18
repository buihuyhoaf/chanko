# Phase 2.4a — Layered reference splash

18 October 2026, Asia/Ho_Chi_Minh. Branch: feature/phase-2-4a-splash.

## Current implementation

This revision supersedes the earlier fixed-delay splash described in commit 8c7bf05. The user explicitly requested the attached pastel-room design on this branch after the earlier revert.

- AndroidX SplashScreen installed before super.onCreate, dedicated launcher theme, brand Purple #8965F6 and padded static cat icon. The platform releases it normally on the first frame; no keep-on-screen timer.
- Compose renders a full-bleed pre-rendered room, a separate transparent cat holding the coral bag, and native Nunito title/tagline/footer. Title is “Should I Buy It?”; launcher app label remains Chanko. “Pause. Think. Decide.” retains the mindful-shopping message agreed in the conversation.
- Uses the existing theme, spacing and shapes. Local brown/coral logo colors reproduce the supplied visual reference; they are Reference artwork colors, not new Approved design-system tokens.
- No fake percentage, partial progress bar or minimum display time. Native loading text appears only while the existing OverviewViewModel stream is Loading. Content and Error both navigate immediately to the existing Dashboard, where the existing error UI is available.
- NavGraph and Dashboard share the same OverviewViewModel instance from the existing Activity owner, so the first query result is retained across the handoff. No startup module, repository, database change or new architecture.
- Existing Splash route is removed from the back stack. Splash-to-Dashboard uses no transition animation. Navigation restoration retains the user's actual destination.
- Art and loading text remain static in all motion settings. No decorative loop, bobbing, flashing, countdown, simulated progress or animation-dependent navigation.
- Background extends behind system bars; actual content uses safeDrawingPadding. Text wraps and the entire composition scrolls; the art shrinks with available height. Dark system-bar icons match the app's existing Light-only theme even in device dark mode.

## Assets and generation

Reference: user attachment “Mèo đáng yêu và lựa chọn mua sắm sáng suốt.png”, also already present as the local untracked chanko_splash_mascot_source.png. The source file was not changed or staged.

Built-in image_gen was used for two independent edits. The generated room and mascot were visually inspected. Their pose/environment are derived approximations rather than a pixel-identical extraction. Native typography intentionally replaces the baked lettering and the original fake-looking partial loading bar. Floating purchase decorations are omitted to keep the composition focused.

Saved runtime assets:
- app/src/main/res/drawable-nodpi/chanko_splash_room.png
- app/src/main/res/drawable-nodpi/chanko_splash_cat_bag.png

The earlier chanko_splash_mascot.png and shared mascot catalog are preserved. This splash derivative is not registered as an approved shared emotion/state asset.

### Background prompt

Use case: precise-object-edit. Asset type: Android splash background plate only. Edit target: attached reference image. Reconstruct its warm pastel 3D room as an EMPTY background layer. Remove ALL text, logo, tagline, cat, shopping bags, products, floating icons, status bar, loading bar, and footer. Keep the arched blue window, softly blurred pastel skyline, shelves at far edges, plant upper right, rounded furniture and rug at lower center, warm cream floor, peach sunlight and premium soft clay 3D rendering. Simplify peripheral clutter. Keep center airy and light for separate native title in upper third and separate white cat in middle. Same portrait framing and perspective as original. No characters, words, logo, UI or symbols. Full-bleed room without device frame.

### Mascot prompt

Use case: background-extraction / identity-preserve. Edit target: attached splash reference. Extract ONLY the exact white fluffy soft 3D CAT HUGGING THE CORAL PINK SHOPPING BAG WITH HEART from the middle of this reference. Preserve the same cat identity, round black eyes, pink cheeks, small smile, pose, raised foot, curved tail, ear shapes, pink paw pads, soft texture and peach lighting. Keep its held shopping bag and contents as one cohesive cutout. Full body including ears tail and paws with generous transparent padding; no cropping. Remove ALL room, floor, rug, shadows, background, text, logo, status bar, loading indicator, footer, floating icons and peripheral objects. Real alpha transparent background, no checkerboard or colored matte. This is a single separate pre-rendered hero asset for native Android Compose layout. Match the original closely, do not redesign as another character.

## Verification

Final local verification PASS: :app:assembleDebug, :app:testDebugUnitTest (11/11), :app:lintDebug (0 errors, 34 warnings), :app:assembleDebugAndroidTest, and git diff --check. Existing unit-test XML reports 11 tests, zero failures/errors. Four Compose previews compile; no rendered Compose preview or successful device screenshot is claimed. The warning count includes retained unused earlier splash resources and dependency-update suggestions; no warning checks were suppressed. Six focused instrumentation tests compile and cover readiness without elapsed-time dependency, latest callback/single completion, already-ready launch, accessible 200% text, image capture, successful first query, failed first query, and Back stack removal.

Device tests are NOT reported as passed: the Android 16 phone was locked, the attempted run could not find resumed Compose hierarchies, and it was stopped after the user explicitly declined device installation/testing. A debug APK and test APK had already been installed before that instruction; no further installation or device testing is authorized. The app was force-stopped to end the run; no app data was cleared and the main app was not uninstalled. Further verification must stay local unless the user changes that constraint.

The first lint invocation stalled in Kotlin PSI traversal and was cancelled. An incremental retry then failed to resolve existing top-level Kotlin functions. A clean app build in a single-use Gradle process passed all requested tasks in 44 seconds, using --max-workers=2 -Pkotlin.incremental=false for that invocation only. No persistent Gradle settings were changed, and no lint checks were disabled.

No merge or auto-merge. No Phase 2.4b work.

## Manual acceptance

Inspect the Compose previews and derivative mascot for visual fidelity. No completed device screenshot is claimed. Test cold/warm launch on Android 11 and 12+; on a fast local database, the branded frame may be brief or skipped, intentionally. Check landscape, split screen, cutouts, font scaling, TalkBack, system animation scale zero and recreation. No artificial delay should be added for visual review; SplashContent previews and the focused screenshot test provide stable review surfaces.

## Runtime asset checks

- chanko_splash_room.png: SHA-256 2db36f9de9fe33716cc747bce3fa64cfe266716a7a44f7f279f3fd51f6db4976
- chanko_splash_cat_bag.png: SHA-256 6238e5f586395502b0e7439c4a92a4349b1478d85c35b5066dc3c6418d7a42f1

Room: 1024 × 1536 RGB; cat: 1247 × 1261 RGBA with alpha range 0–255. Both inspected visually after generation.
