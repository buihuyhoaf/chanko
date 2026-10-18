# Phase 2.4a — Layered reference splash

18 October 2026, Asia/Ho_Chi_Minh. Branch: feature/phase-2-4a-splash.

## Current implementation

This revision supersedes the earlier fixed-delay splash described in commit 8c7bf05. The user explicitly requested the attached pastel-room design on this branch after the earlier revert.

- AndroidX SplashScreen installed before super.onCreate, dedicated launcher theme, brand Purple #8965F6 and padded static cat icon. The platform releases it normally on the first frame; no keep-on-screen timer.
- Compose renders a full-bleed pre-rendered room, a separate transparent cat holding the coral bag, and native Nunito title/tagline/footer. Title is “Should I Buy It?”; launcher app label remains Chanko. “Pause. Think. Decide.” retains the mindful-shopping message agreed in the conversation.
- Uses the existing theme, spacing and shapes. Local brown/coral logo colors reproduce the supplied visual reference; they are Reference artwork colors, not new Approved design-system tokens.
- User-approved one-second brand introduction, measured after the scene first draws. Native loading text appears only while OverviewViewModel is Loading. After the introduction, Content and Error both navigate to the existing Dashboard; slow data continues to wait for a real result. No fake percentage or partial progress bar.
- NavGraph and Dashboard share the same OverviewViewModel instance from the existing Activity owner, so the first query result is retained across the handoff. No startup module, repository, database change or new architecture.
- Existing Splash route is removed from the back stack. Splash entrance and the transition to Dashboard use no transition animation. The platform exit callback removes its overlay immediately. Navigation restoration retains the user's actual destination.
- Art and loading text remain static in all motion settings. No decorative loop, bobbing, flashing, countdown, simulated progress or animation-dependent navigation.
- Background extends behind system bars; actual content uses safeDrawingPadding. Text wraps and the entire composition scrolls; the art shrinks with available height. Dark system-bar icons match the app's existing Light-only theme even in device dark mode.

## Assets and generation

Reference: user attachment “Mèo đáng yêu và lựa chọn mua sắm sáng suốt.png”, also already present as the local untracked chanko_splash_mascot_source.png. The source file was not changed or staged.

Built-in image_gen was used for two independent edits. The generated room and mascot were visually inspected. Their pose/environment are derived approximations rather than a pixel-identical extraction. Native typography intentionally replaces the baked lettering and the original fake-looking partial loading bar. A separate static decoration layer now restores the cart, heart, calendar and receipt from the reference; it is omitted for large fonts and landscape.

Saved runtime assets:
- app/src/main/res/drawable-nodpi/chanko_splash_room.png
- app/src/main/res/drawable-nodpi/chanko_splash_cat_bag.png

The earlier chanko_splash_mascot.png and shared mascot catalog are preserved. This splash derivative is not registered as an approved shared emotion/state asset.

### Background prompt

Use case: precise-object-edit. Asset type: Android splash background plate only. Edit target: attached reference image. Reconstruct its warm pastel 3D room as an EMPTY background layer. Remove ALL text, logo, tagline, cat, shopping bags, products, floating icons, status bar, loading bar, and footer. Keep the arched blue window, softly blurred pastel skyline, shelves at far edges, plant upper right, rounded furniture and rug at lower center, warm cream floor, peach sunlight and premium soft clay 3D rendering. Simplify peripheral clutter. Keep center airy and light for separate native title in upper third and separate white cat in middle. Same portrait framing and perspective as original. No characters, words, logo, UI or symbols. Full-bleed room without device frame.

### Mascot prompt

Use case: background-extraction / identity-preserve. Edit target: attached splash reference. Extract ONLY the exact white fluffy soft 3D CAT HUGGING THE CORAL PINK SHOPPING BAG WITH HEART from the middle of this reference. Preserve the same cat identity, round black eyes, pink cheeks, small smile, pose, raised foot, curved tail, ear shapes, pink paw pads, soft texture and peach lighting. Keep its held shopping bag and contents as one cohesive cutout. Full body including ears tail and paws with generous transparent padding; no cropping. Remove ALL room, floor, rug, shadows, background, text, logo, status bar, loading indicator, footer, floating icons and peripheral objects. Real alpha transparent background, no checkerboard or colored matte. This is a single separate pre-rendered hero asset for native Android Compose layout. Match the original closely, do not redesign as another character.

## Verification

Final local verification PASS: :app:assembleDebug, :app:testDebugUnitTest (11/11), :app:lintDebug (0 errors, 34 warnings), :app:assembleDebugAndroidTest, and git diff --check. Existing unit-test XML reports 11 tests, zero failures/errors. Four actual Compose previews now render successfully on the host via an isolated screenshot-test harness; no successful device screenshot is claimed. See qa/splash/README.md and the four saved PNGs. The warning count includes retained unused earlier splash resources and dependency-update suggestions; no warning checks were suppressed. Six focused instrumentation tests compile and cover readiness without elapsed-time dependency, latest callback/single completion, already-ready launch, accessible 200% text, image capture, successful first query, failed first query, and Back stack removal.

Device tests are NOT reported as passed: the Android 16 phone was locked, the attempted run could not find resumed Compose hierarchies, and it was stopped after the user explicitly declined device installation/testing. A debug APK and test APK had already been installed before that instruction; no further installation or device testing is authorized. The app was force-stopped to end the run; no app data was cleared and the main app was not uninstalled. Further verification must stay local unless the user changes that constraint.

The first lint invocation stalled in Kotlin PSI traversal and was cancelled. An incremental retry then failed to resolve existing top-level Kotlin functions. A clean app build in a single-use Gradle process passed all requested tasks in 44 seconds, using --max-workers=2 -Pkotlin.incremental=false for that invocation only. No persistent Gradle settings were changed, and no lint checks were disabled.

No merge or auto-merge. No Phase 2.4b work.

## Manual acceptance

Inspect the Compose previews and derivative mascot for visual fidelity. No completed device screenshot is claimed. Test cold/warm launch on Android 11 and 12+; on a fast local database, the brand introduction still remains visible for approximately one second with no loading text. Check landscape, split screen, cutouts, font scaling, TalkBack, system animation scale zero and recreation. The one-second introduction is explicitly approved branding, not simulated loading. SplashContent previews and the focused screenshot test also provide stable review surfaces.

## Runtime asset checks

- chanko_splash_room.png: SHA-256 2db36f9de9fe33716cc747bce3fa64cfe266716a7a44f7f279f3fd51f6db4976
- chanko_splash_cat_bag.png: SHA-256 6238e5f586395502b0e7439c4a92a4349b1478d85c35b5066dc3c6418d7a42f1

Room: 1024 × 1536 RGB; cat: 1247 × 1261 RGBA with alpha range 0–255. Both inspected visually after generation.

## Visual correction after user review

The user reported not seeing the room and that the result differed from the reference. Rendering the previous code on the host confirmed the room asset was wired correctly, but ContentScale.Crop cut off much of the side scenery on tall phones, the global cream overlay washed out the lower scene, and cream cards replaced the reference sticker logo/plain native copy. This does not establish exactly which frame/build the user saw. The system splash remains a solid purple platform screen, and a fast first data result can still make the Compose scene very brief; no timer was added to hide that distinction.

Corrected on the same branch:
- Full room plate visible in portrait; landscape uses crop to avoid extreme stretch. Removed the global cream overlay and the logo/tagline/footer cards.
- Larger Nunito lettering rendered in two native passes for a cream sticker outline, brown first line and coral second line, matching the supplied hierarchy more closely.
- Restored static pre-rendered floating cart, heart, calendar and receipt in a separate transparent layer. Suppressed these decorations above 130% font scale and in landscape so they do not compete with accessible copy.
- Retained separate Fit-scaled cat, native copy/loading, safe insets, scrolling, static reduced-motion behavior and data-based exit without any delay.
- Added public SplashRoomPreview with persistent room previews for local inspection; production startup timing is unchanged.

Saved render evidence: qa/splash/portrait-393x852.png, compact-360x640.png, font200-320x568-scroll-top.png and landscape-font200-640x360-scroll-top.png. These are actual Compose host renders, not generated screenshots. The temporary screenshot harness is not part of the production modules or Gradle configuration.

### Decoration asset

Saved: app/src/main/res/drawable-nodpi/chanko_splash_decorations.png. Built-in image_gen edit, transparent background. Inspected visually and verified RGBA with transparent center.

Prompt:

Use case: background-extraction / precise-object-edit. Input is the supplied original splash reference. Create a transparent FOREGROUND DECORATION layer aligned to the same portrait 2:3 canvas. Extract ONLY the original four floating 3D clay objects with their original colors: coral square shopping-cart tile near x=16%, y=32%; coral square heart tile near x=87%, y=40%; small pastel calendar with red check mark near x=11%, y=56%; cream receipt near x=84%, y=55%. Preserve the softly rounded clay rendering and rotations, same relative positions and sizes. Keep the center and most of the canvas completely transparent so an independently rendered cat and native title can fit. Remove the cat, bag, room, floor, ALL typography, logo, tagline, footer, status bar, loading bar and every other object. Real alpha background. No text or numbers on the calendar or receipt, just simple non-text marks. No devices or frames. This is an overlay layer, not a new full screen mockup.

SHA-256: d1f31e95f457adba3343912b067653282cce47f6813d91ff2b6db4dd311f0706

Visual-correction verification: final assembleDebug, testDebugUnitTest (11/11), lintDebug (0 errors, 34 warnings), assembleDebugAndroidTest and diff --check PASS. Four actual host renders (portrait, compact, 200% font, landscape 200%) completed successfully. No device operation was performed in this correction.

## Startup visibility fix — one-second brand introduction

After another report of only seeing the cat on purple, source inspection confirmed the missing runtime guarantee: the first data result could navigate away while the platform splash still covered the window or while NavHost's default entrance fade had not exposed the room. Earlier room renders verified art/layout only; they did not prove that real startup kept the room visible.

The user explicitly approved: always display the room for about one second, with loading copy only while actual data work remains. This supersedes the previous immediate-exit policy, without simulating loading.

- MainActivity removes the system splash overlay immediately in setOnExitAnimationListener; no extra platform fade covers the room.
- The Splash route has no entrance fade. The scene's first draw starts a frame-synchronized one-second presentation interval. Asset decode/composition time is not counted as visible presentation time.
- A separate readiness effect exits only after BOTH the interval and the actual data result. Readiness changes never restart the interval. The latest completion callback is used, once only.
- Loading copy disappears immediately when data is ready, including throughout a fast-start brand introduction. If data takes longer than one second, the real loading state remains until Content or Error.
- The presentation remains static under reduced motion. The completed presentation flag is saveable across configuration recreation.
- Instrumentation coverage is updated for fast ready-at-entry (scene present, no loading, no completion before one second), slow data beyond the presentation, latest callback and single completion. Instrumentation is compiled only, not executed on a phone.

No device installation or execution was performed. The installed APK is not automatically updated by Git commits; the newly built app-debug.apk must be explicitly run/installed by the user if they choose to update their device.

One-second introduction verification: assembleDebug, 11/11 unit tests, lintDebug (0 errors, 34 warnings), and assembleDebugAndroidTest PASS. Additional host render of SplashScreen(isReady=true) PASS, saved as qa/splash/fast-data-introduction.png; the room is present and loading copy absent. The timer assertions are compiled instrumentation coverage, not an executed device result.
