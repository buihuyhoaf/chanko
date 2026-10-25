# Reference rendering harness

Optional reproducibility tool for the supplied HTML/CSS visual documents. Requires Node.js, Playwright and a locally installed compatible Chromium/Chrome; dependencies are not bundled. Set CHANKO_CHROME_EXECUTABLE if needed. Run the script from any directory; it resolves the handoff root from its own location.

It renders each source at390/320 CSS px and100/200% text scale, waits for Nunito/images, selects the large-font wrapping layout, records text color pairs, target boxes, image slots and overflow. Screenshots and layout_checks.json are overwritten only inside this extracted handoff copy. Original canonical/expression masters and baseline snapshots are never edited.

This is document rendering/measurement, not Android source or a test of backend, TalkBack, IME or native focus. The source HTML has no business actions. CSS px correspond only to proposed reference dp at scale1. Measurements are evidence for this exact source revision, not blanket WCAG conformance.
