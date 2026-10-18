# Actual Compose renders — splash visual correction

Rendered locally with the official Android Compose Preview Screenshot Testing plugin 0.0.1-alpha16 / Layoutlib. These are actual SplashContent renders, not image mockups or screenshots from a phone. No emulator/device installation was used for this revision.

- [Fast-data SplashScreen introduction, no loading text](fast-data-introduction.png)
- [Portrait 393 × 852 dp](portrait-393x852.png)
- [Compact 360 × 640 dp](compact-360x640.png)
- [Font 200%, 320 × 568 dp](font200-320x568-scroll-top.png)
- [Landscape font 200%, 640 × 360 dp](landscape-font200-640x360-scroll-top.png)

Large-text and landscape images show the initial scroll position; lower content remains accessible by scrolling. Decoration is omitted at font scales over 130% or in landscape. Portrait has all scene layers.

The screenshot plugin and screenshotTest source set were configured ONLY in an isolated /private/tmp repo copy. No module or screenshot-plugin dependency was added to the application repository. Source equality was checked before copying the renders; only the internal/public visibility of SplashContent differs in the temporary harness, to allow its invocation from screenshotTest. Four renders completed successfully with updateDebugScreenshotTest. Existing instrumentation tests were compiled only, not run on a phone.

The room is a flexible decorative plate: portrait fits the full plate to preserve the shelves and plant; landscape crops it to avoid extreme horizontal stretching. Cat and decorative icons use Fit and retain their aspect ratios. The user subsequently approved a brand introduction after the first scene draw, now set to two seconds. Layout renders invoke SplashContent directly; an additional fast-data render invokes SplashScreen itself with isReady=true and confirms the room remains the presentation and loading text is absent. This host render is not an executed timing assertion; virtual-clock timing tests are compiled instrumentation tests.

Renderer reference: https://developer.android.com/studio/preview/compose-screenshot-testing
