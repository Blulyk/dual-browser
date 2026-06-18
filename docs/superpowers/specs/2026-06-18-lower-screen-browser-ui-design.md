# Lower-Screen Browser UI and Launch Stability Design

## Objective

Turn the AYN Thor lower display into a polished browser control surface with real tab previews and persistent bookmark access. After the redesign is complete and verified, separately diagnose and correct the remaining secondary-display launch instability.

## Required Order

1. Implement and verify the lower-screen redesign.
2. Freeze the UI behavior and establish a passing baseline.
3. Reproduce the launch failure on the connected AYN Thor.
4. Fix display lifecycle behavior without changing the approved UI.

## Visual Direction

- Use a dark Brave-inspired interface without copying Brave branding or assets.
- Use deep navy blue as the primary accent instead of orange.
- Use graphite backgrounds, subtle borders, rounded surfaces, compact spacing, and high-contrast focus states.
- Keep touch targets comfortable on the 1240 x 1080 lower display and expose clear controller focus.

## Lower-Screen Layout

The control surface is organized vertically:

1. A compact navigation row with back, forward, reload, address/search field, Go, new tab, private tab, and overflow actions.
2. A horizontally scrollable tab carousel using large visual preview cards.
3. A persistent horizontally scrollable bookmark bar at the bottom.

The selected tab receives the strongest navy highlight. Controls use icons plus accessible labels where space permits. Diagnostics and library move into the overflow area so primary browsing actions remain visible without horizontal scrolling.

## Tab Previews

- Each tab card shows a real page thumbnail, title, domain, close action, private indicator, and selected state.
- Capture a thumbnail after the page becomes visually committed and laid out.
- Store normal and private thumbnails in an in-memory bounded cache keyed by tab ID.
- Never persist private thumbnails to disk.
- Remove cached thumbnails when a tab closes and clear all thumbnails when the process ends.
- Show a navy placeholder containing the domain or tab type until a capture is available.
- Selecting a card focuses that tab in the upper WebView; closing it preserves the existing last-tab replacement behavior.

## Bookmark Bar

- Observe bookmarks from the existing Room repository through the browser view model.
- Display bookmarks persistently as compact chips with title/domain and a fallback monogram.
- Tapping a bookmark navigates the currently focused tab.
- The bar shows an empty-state hint when there are no bookmarks.
- Bookmark creation and removal continue to use the existing repository and library behavior.

## Data and Component Boundaries

- `BrowserViewModel` exposes bookmark state and thumbnail state alongside the existing browser session.
- A dedicated thumbnail store owns bounded bitmap memory, capture updates, and removal.
- `WebSurface` emits a capture-ready view event only after visible content is committed.
- The lower-screen UI is split into focused composables for the navigation toolbar, preview carousel, preview card, and bookmark bar.
- The domain session manager remains responsible for tab focus, navigation, creation, and closing.

## Error Handling

- Thumbnail capture failure leaves the placeholder visible and does not affect navigation.
- Bookmark observation failure leaves the bar empty without crashing the browser.
- Missing or stale thumbnails are discarded when their tab no longer exists.
- The upper screen remains usable if the lower display is unavailable.

## Secondary-Display Investigation

Only after the redesigned UI passes tests and runs correctly:

- Capture ADB logs and display/activity state for cold launch, warm launch, Home-to-app relaunch, screen sleep/wake, and process recreation.
- Record display power state, activity lifecycle, task placement, launch requests, and tracker ownership at each boundary.
- Form one hypothesis from the evidence before changing lifecycle code.
- Add a failing regression test for the confirmed state-transition error.
- Preserve exactly one main activity on display 0 and one secondary surface on display 4.
- Fall back to the complete single-screen UI on display 0 if display 4 cannot become active.

## Verification

- Unit tests cover thumbnail ownership, eviction/removal, bookmark navigation, and launch-state decisions.
- Compose tests cover toolbar actions, selected preview state, preview closing, bookmark navigation, empty states, and controller focus semantics.
- Full unit tests, lint, and debug assembly pass before device testing.
- Physical Thor verification checks the exact 1240 x 1080 layout, real thumbnails, multiple tabs, bookmark navigation, and controller/touch operation.
- Launch verification repeats each launch scenario at least three times and confirms activity placement and absence of lifecycle timeouts or duplicate launch requests.

## Publication

Publish a new prerelease only after both ordered phases pass automated and physical verification. Do not replace the existing beta asset; use a new version and release notes describing the redesign and launch fix.
