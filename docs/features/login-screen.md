# Login — The Little Door

## Temporary login bypass — 18/10/2026

At the user’s explicit request, tapping the Google button now opens Dashboard
directly while API/OAuth configuration is unavailable. A `TODO(auth)` beside the
button callback records the original authentication call to restore. This bypass
creates no Google credential or stored token. The real authentication implementation
remains in place, but is not called by this temporary button behavior.
The server-verification requirements and previous acceptance results below describe
the original authentication flow, not this temporary navigation override.


Branch: `feature/login-screen`. Base: `origin/dev-chanko-1x` at `ad69984`.
The existing three feature commits were rebased onto the latest fetched development
branch. The implementation preserves the development Splash timing, shared overview
ViewModel and cache error handling while inserting Login before Dashboard.

## UI and localization

Native Jetpack Compose content over a separate 3D illustration. Warm cream, purple
door, white welcoming cat, Nunito, outlined white Google button. Every new visible
string is Vietnamese, including the app wordmark: “Nên mua / không nhỉ?”. All text
is accessible to accessibility services; artwork and Google icon are decorative.
Google button typeface uses the system sans-serif to follow Google branding.

The scene occupies about 55% of the viewport, with a cream fade. Interaction content
has a 440dp maximum width. The whole page scrolls on compact, landscape and 200%
font configurations. Buttons have Material's minimum 48dp touch target and grow
with text. Loading disables repeated submission; progress and errors are live
regions. Legal actions are separate accessible buttons and use configured HTTPS
URLs, or explain that the content is unavailable. No legal text/URLs are invented.

## Functional traceability

- TECH-01 §6 / TECH-03 §3.1: Google only. No email, password, signup form or local
  success shortcut. A fresh backend challenge precedes each user attempt.
- The backend nonce is passed unchanged to `GetSignInWithGoogleOption.setNonce`.
  Credential Manager presents account selection, including adding an account.
- POST `auth/google` receives the Google ID token and corresponding challenge ID.
  The backend must validate signature, issuer, audience, expiry, nonce and replay;
  the client never substitutes a locally decoded identity for server acceptance.
- Navigation occurs only after a successful backend response with both nonempty
  tokens and a durable encrypted session write. Cancellation, network loss, HTTP
  rejection, invalid response and secure-storage failure remain on Login.
- A single attempt runs at a time. Route disposal cancels it. Retry requests a new
  challenge; network mutations and lost responses are never replayed automatically.
- TECH-03 §3.2: tokens are encrypted with AES/GCM under an Android Keystore key in
  `noBackupFilesDir`, outside Room, cloud backup and device transfer. Sensitive
  DTOs redact `toString`; exceptions and backend message text are not surfaced.
- AUTH-05: account creation and default PAY_NOW initialization belong to the backend
  transaction. This screen creates no payment method, item or purchase locally.

## Configuration and integration boundary

The user confirmed on 18/10/2026 that API base URL, Google Web Client ID and legal
URLs are not available. Defaults remain empty; tapping Google shows a Vietnamese
unavailable message and neither opens Google nor attempts a network request.

Supply approved build properties (for example via untracked Gradle properties):

- `CHANKO_API_BASE_URL`: HTTPS API root ending in `/api/v1/`.
- `CHANKO_GOOGLE_WEB_CLIENT_ID`: OAuth **Web** client ID, with the Android package
  `com.hoabh.chanko` and signing certificate registered in Google Cloud.
- `CHANKO_TERMS_URL`, `CHANKO_PRIVACY_URL`: published HTTPS legal documents.

Endpoint paths and challenge fields come from TECH-03. Since no OpenAPI/backend
exists in this checkout, the exchange wire mapping is a documented integration
assumption: request `{challenge_id, id_token}`, response
`{data: {access_token, refresh_token}, meta?: {...}}`. Confirm this mapping with the
backend before enabling an environment. No live end-to-end Google/server success
is claimed. The standard response envelope comes from `:core:network`.

This change implements the Login entry flow. It does not migrate existing local
business screens/repositories, implement logout/account switching, token refresh,
or restore an authenticated route from a persisted token. A cold launch asks for
Google Sign-In again. An Activity-scoped in-memory gate prevents a restored protected
navigation destination from bypassing Login after process death; it survives rotation
without saving an authentication flag into navigation state. Full session lifecycle, authenticated API calls, account
cache partitioning and refresh rotation require the following auth/data phase;
a stored token alone is never treated as a verified session by this entry flow.

Official implementation references:
- https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation
- https://developer.android.com/jetpack/androidx/releases/credentials (stable 1.6.0)
- https://developers.google.com/identity/branding-guidelines

## Artwork provenance

Source: the image attached by the user in this task. Prepared using the built-in
ImageGen tool, preserving the approved cat, door, wall, window and warm lighting.
Asset: `app/src/main/res/drawable-nodpi/chanko_login_door.png` (about 1.9 MiB).
Native strings, wordmark, legal actions and Google button are not baked into it.

Final prompt:

> Use case: precise-object-edit. Asset type: production Android login hero background.
> Input image is edit target. Extract/recompose ONLY the upper illustration as a
> portrait 4:5 background illustration filling the canvas edge to edge: identical
> white kitten waving beside the open lavender arched door, cream stucco house,
> arched window on left, pink flowerpot and warm sunlit interior. Preserve character
> identity, scene, perspective, materials and lighting from the input. Remove ALL
> text, lettering, app logo, stickers, status bar, phone indicators, headline,
> buttons, legal footer and gesture bar. Reconstruct cream wall where logo used to
> be; keep quiet empty cream wall in top quarter so a native app wordmark can be
> overlaid later. Cat feet and door base fully visible in lower part. Scene floor
> fades softly into solid warm cream #FAF8F5 in bottom 8% of canvas. No new subjects.
> No UI or text anywhere. This is a hero artwork asset, not a screenshot.

## Validation

Final verification on 18/10/2026:

- `assembleDebug testDebugUnitTest lint assembleDebugAndroidTest`: PASS.
- Unit tests: 38 total, 0 failures (18 app, 9 auth repository, 11 network).
- Direct AndroidJUnitRunner execution on Android 16/API 36 arm64 emulator:
  8 tests PASS (5 Login UI, 2 Splash/navigation, 1 Keystore persistence).
- Initial Gradle connected execution selected only the 5 UI tests; the direct
  runner was then used to prove that all three requested classes executed.
- New login/auth source files: no lint issues. Overall lint: 0 errors, 40 warnings
  (dependency update notices and existing resources/plurals/launcher icons).
- `git diff --check`: PASS. Development head was re-fetched and remains `ad69984`.
- A stale Kotlin incremental compilation attempt could not resolve unchanged
  top-level declarations; cleaning `:app` and rerunning with
  `-Pkotlin.incremental=false` passed. No build setting was changed for this.
- Visual review: native Compose screenshot, Google button/links visible, cream
  transition clean, Vietnamese diacritics rendered correctly, no bitmap UI text.
- Additional 320×568dp and 640×360dp / 200% font action reachability: PASS.
- Real MainActivity smoke check: tapping Google with empty configuration displays
  the unavailable message and remains on Login; native screenshot retained.
- Real Google/backend acceptance cannot be exercised until approved configuration
  and compatible endpoint implementations are supplied. Session refresh/logout
  and authenticated business API migration are outside this Login change.
