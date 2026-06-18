# Dual Browser Public Beta Design

## Goal

Build a local-first Android browser optimized for the AYN Thor. The upper display presents the primary web page at maximum usable size. The lower display is a control center by default and can switch to an independent second web page with compact controls. The app also remains usable on ordinary single-display Android devices.

The first public beta uses Android System WebView through AndroidX WebKit. This provides the system Chromium engine without the size and maintenance burden of a Chromium fork. The web engine is hidden behind an internal interface so a later engine migration does not require rewriting the UI or browser state.

## Target And Technology

- Primary hardware: AYN Thor, tested on a physical unit.
- Application: Kotlin, Jetpack Compose, Material 3, coroutines, and Gradle Kotlin DSL.
- Web engine: Android System WebView plus AndroidX WebKit feature detection.
- Persistence: Room for bookmarks and history; DataStore for preferences.
- Minimum Android version: API 28. Compile and target SDK: API 36.
- Package name: `com.blulyk.dualbrowser`.
- Product name: Dual Browser.

## Display Model

`DisplayCoordinator` observes Android `DisplayManager` and records the dimensions, density, refresh rate, and identity of connected displays. On the AYN Thor it assigns the larger upper display as the primary web surface and launches `SecondaryDisplayActivity` on the lower display with `ActivityOptions.setLaunchDisplayId`. If the firmware rejects that launch, the app uses the single-display layout and reports the capability in diagnostics. Display identity and assignment are verified with the in-app diagnostics screen on physical hardware.

If only one display is available, the app uses one responsive window: the web surface fills the window and the control center appears as a bottom sheet. Losing the secondary display moves its active web session into the primary window without discarding navigation state.

The selected hybrid behavior is:

1. The upper display shows the focused primary tab.
2. The lower display starts as the control center.
3. The user can promote a tab to the lower display as an independent second web surface.
4. When the lower web surface is active, it retains a compact address and navigation bar.
5. Either web surface can be focused; new tabs open beside the currently focused tab.

## Architecture

The application is divided into focused modules within one Android app:

- `browser-domain`: tab, window, navigation, bookmark, history, download, and privacy models. It has no Android UI dependency.
- `browser-engine`: `BrowserEngine` and `BrowserView` contracts, implemented by WebView and AndroidX WebKit.
- `browser-data`: Room repositories for bookmarks and history, DataStore preferences, and normal-session restoration.
- `browser-ui`: Compose screens for the web chrome, control center, tab switcher, settings, diagnostics, and single-display fallback.
- `browser-platform`: display coordination, downloads, file chooser, sharing, runtime permissions, and external intent handling.

`BrowserSessionManager` is the single owner of tabs and focus. UI surfaces render immutable state and send commands to it. A tab owns navigation metadata and one live engine instance while visible; background tabs may release their view and retain restorable state when memory pressure requires it.

## Public Beta Features

The beta includes:

- Multiple tabs with title, favicon, thumbnails, close, reopen-last-closed, and session restoration.
- Address and search field with direct URL detection and a configurable search provider.
- Back, forward, reload/stop, home, share, find in page, desktop-site toggle, and open in external app.
- Bookmarks, browsing history, recent tabs, and clearing local browsing data.
- Private sessions that do not write history or restore tabs after exit.
- File uploads, Android downloads, runtime site permissions, geolocation prompts, camera/microphone prompts, and full-screen video.
- JavaScript dialogs and `window.open` handling, with user-visible decisions rather than silent popups.
- Controller and keyboard navigation for focus movement, back, tab switching, address focus, and activation. Touch remains fully supported.
- Light, dark, and system themes with layouts tuned first for the Thor's two displays.
- A local diagnostics page that shows display and WebView capability information and exports it through Android's share sheet only when the user requests it.

The beta excludes accounts, cloud sync, extensions, ad blocking, password management, autofill ownership, VPN/proxy services, and remote telemetry. Password and form autofill remain the responsibility of Android and the installed WebView provider.

## Data And Privacy

No backend is required. No analytics, advertising identifier, or automatic diagnostic upload is included. Normal bookmarks, history, preferences, and restorable tabs remain on the device. Private tabs are held in memory and are destroyed when the private session closes.

The app uses WebView Safe Browsing when supported, blocks cleartext traffic by default, does not bypass TLS certificate errors, and disables file-URL access that could expose local files. Site permissions are scoped to the requesting origin and can be reviewed and cleared. External schemes require an explicit Android intent and fail with a readable message when no handler exists.

## Failure Handling

- Renderer crashes replace only the affected tab with a recovery view and offer reload.
- Process recreation restores normal tab metadata and URLs; private tabs are not restored.
- Unsupported AndroidX WebKit features degrade individually and are reported in diagnostics.
- Download, file, permission, and external-intent failures surface concise messages on the control display or current window.
- If the lower display disconnects or its activity cannot launch, the app returns to the single-display layout.
- A malformed URL is converted to a search query; an unreachable page keeps the entered URL and exposes retry.

## Testing And Acceptance

Unit tests cover URL classification, search construction, tab commands, focus rules, session persistence, privacy boundaries, and display assignment. Repository tests use an in-memory Room database. Compose tests cover control-center actions and single-display fallback. Instrumented WebView tests use local HTML fixtures for navigation, popups, downloads, file input, permissions, renderer recovery, and full-screen transitions.

Physical AYN Thor acceptance requires:

1. Cold launch places web content on the upper display and controls on the lower display.
2. A second web surface can open and close on the lower display without losing either tab.
3. Touch, keyboard, and mapped controller actions can navigate core browser controls.
4. Rotation, sleep/wake, background/foreground, and lower-display loss preserve the normal session.
5. Common sites can authenticate, play full-screen video, upload and download files, and request permissions.
6. Private-session history and tabs are absent after the private session exits.
7. The signed release APK installs through Android package installation and is published with checksums and release notes.

## Distribution

The repository will be public on GitHub. GitHub Actions will run tests and build a debug APK for pull requests. A tagged beta release will build a signed release APK when signing secrets are configured; until then, a locally signed beta APK can be attached manually. The README will explain sideloading, Android's unknown-app permission, required WebView updates, current limitations, and how to report Thor-specific issues without submitting private browsing data.
