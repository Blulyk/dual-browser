# Lower-Screen Browser UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a polished navy browser control surface for the AYN Thor lower display with real in-memory tab thumbnails and persistent one-tap bookmarks.

**Architecture:** Keep browser commands in `BrowserSessionManager`, expose bookmarks and previews through `BrowserViewModel`, and split the lower UI into a toolbar, preview carousel, and bookmark bar. Capture bounded WebView thumbnails only after visible content commits; keep every preview in process memory so private content never reaches disk.

**Tech Stack:** Kotlin, Android SDK 36, Jetpack Compose Material 3, Android WebView, Room Flow, StateFlow, JUnit, Robolectric, Compose UI tests, ADB.

---

### Task 1: Navy design system

**Files:**
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/DualBrowserTheme.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/MainActivity.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/SecondaryDisplayActivity.kt`
- Create: `app/src/test/java/com/blulyk/dualbrowser/ui/DualBrowserThemeTest.kt`

- [ ] **Step 1: Write the failing palette test**

```kotlin
@Test
fun darkPaletteUsesNavyPrimary() {
    assertEquals(Color(0xFF163A63), DualBrowserColors.primary)
    assertEquals(Color(0xFF0B111A), DualBrowserColors.background)
}
```

- [ ] **Step 2: Run the test and verify RED**

Run:
```powershell
.\gradlew.bat testDebugUnitTest --tests "com.blulyk.dualbrowser.ui.DualBrowserThemeTest"
```
Expected: compilation failure because `DualBrowserColors` does not exist.

- [ ] **Step 3: Implement the theme**

Create immutable color constants and `DualBrowserTheme` using `darkColorScheme` with navy `#163A63`, bright navy `#2F6EA8`, background `#0B111A`, surface `#111B27`, elevated surface `#192636`, and off-white text `#EAF2FA`. Replace the root `MaterialTheme` calls in both display activities with `DualBrowserTheme`.

- [ ] **Step 4: Verify GREEN**

Run the focused test and existing UI tests; expect `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/blulyk/dualbrowser/ui app/src/test/java/com/blulyk/dualbrowser/ui/DualBrowserThemeTest.kt
git commit -m "feat: add navy browser design system"
```

### Task 2: Bounded in-memory tab previews

**Files:**
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/TabPreviewStore.kt`
- Create: `app/src/test/java/com/blulyk/dualbrowser/ui/TabPreviewStoreTest.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/BrowserViewModel.kt`
- Modify: `app/src/test/java/com/blulyk/dualbrowser/ui/BrowserViewModelTest.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/DualBrowserApplication.kt`

- [ ] **Step 1: Write failing store tests**

Test the wished-for API with three 1 x 1 bitmaps:

```kotlin
val store = TabPreviewStore(maxEntries = 2)
store.put("one", first)
store.put("two", second)
store.put("three", third)
assertEquals(setOf("two", "three"), store.previews.value.keys)
store.remove("two")
assertEquals(setOf("three"), store.previews.value.keys)
```

Also verify `retain(setOf("three"))` removes every stale tab ID.

- [ ] **Step 2: Run tests and verify RED**

Run `TabPreviewStoreTest`; expect compilation failure because the class is missing.

- [ ] **Step 3: Implement the store and view-model exposure**

Use an access-ordered `LinkedHashMap<String, Bitmap>` and publish immutable copies through `StateFlow<Map<String, Bitmap>>`. Add one application-scoped `TabPreviewStore(maxEntries = 12)`. Inject it into `BrowserViewModel`, expose `previews`, add `updatePreview(tabId, bitmap)`, and after every browser command call `retain(sessionManager.state.value.tabs.mapTo(mutableSetOf(), BrowserTab::id))`.

- [ ] **Step 4: Verify GREEN**

Run store and view-model tests; expect all to pass.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/blulyk/dualbrowser app/src/test/java/com/blulyk/dualbrowser/ui
git commit -m "feat: add bounded tab preview state"
```

### Task 3: Capture visible WebView thumbnails

**Files:**
- Modify: `app/src/main/java/com/blulyk/dualbrowser/engine/WebViewCallbacks.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/engine/WebViewFactory.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/WebSurface.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/BrowserApp.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/MainActivity.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/SecondaryDisplayActivity.kt`
- Modify: `app/src/test/java/com/blulyk/dualbrowser/engine/WebViewFactoryTest.kt`
- Modify: `app/src/test/java/com/blulyk/dualbrowser/ui/WebSurfaceTest.kt`

- [ ] **Step 1: Write failing callback and capture tests**

Extend the test callback to record `onPageCommitVisible(url)`, call the WebView client callback, and assert the URL. Add a WebSurface test that supplies a laid-out fake WebView engine and asserts `onPreviewCaptured` receives the active tab ID and a non-empty bitmap.

```kotlin
assertEquals("https://example.com", committedUrl)
assertEquals("tab-1", capturedTabId)
assertTrue(capturedBitmap.width in 1..480)
```

- [ ] **Step 2: Run focused tests and verify RED**

Expected: missing callback and missing `onPreviewCaptured` parameter.

- [ ] **Step 3: Implement visual-commit capture**

Forward `WebViewClient.onPageCommitVisible` into `WebViewCallbacks`. In `WebSurface`, add `onPreviewCaptured: (String, Bitmap) -> Unit`; after commit, call `webView.post`, reject zero-sized views, create a 480-pixel-wide bitmap preserving aspect ratio, scale the canvas, draw the WebView, and emit the bitmap. Thread the callback through `BrowserApp` and both activities to `viewModel::updatePreview`.

- [ ] **Step 4: Verify GREEN**

Run `WebViewFactoryTest` and `WebSurfaceTest`; expect `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/blulyk/dualbrowser/engine app/src/main/java/com/blulyk/dualbrowser/ui app/src/test/java/com/blulyk/dualbrowser
git commit -m "feat: capture visual tab previews"
```

### Task 4: Expose live bookmarks

**Files:**
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/BrowserViewModel.kt`
- Modify: `app/src/test/java/com/blulyk/dualbrowser/ui/BrowserViewModelTest.kt`

- [ ] **Step 1: Write the failing bookmark test**

Construct the view model with `flowOf(listOf(BookmarkEntity("https://example.com", "Example", 1)))` and assert `bookmarks.first()` returns that entry.

- [ ] **Step 2: Verify RED**

Run `BrowserViewModelTest`; expect a missing `bookmarks` constructor/property failure.

- [ ] **Step 3: Implement bookmark injection**

Accept `bookmarks: Flow<List<BookmarkEntity>> = emptyFlow()` in `BrowserViewModel`, expose it unchanged, and pass `application.repository.bookmarks` from the factory.

- [ ] **Step 4: Verify GREEN**

Run `BrowserViewModelTest`; expect all tests to pass.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/blulyk/dualbrowser/ui/BrowserViewModel.kt app/src/test/java/com/blulyk/dualbrowser/ui/BrowserViewModelTest.kt
git commit -m "feat: expose bookmarks to browser chrome"
```

### Task 5: Brave-inspired lower browser chrome

**Files:**
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/BrowserToolbar.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/TabPreviewCarousel.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/BookmarkBar.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/ControlCenter.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/BrowserApp.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/MainActivity.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/SecondaryDisplayActivity.kt`
- Modify: `app/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Create: `app/src/test/java/com/blulyk/dualbrowser/ui/TabPreviewCarouselTest.kt`
- Create: `app/src/test/java/com/blulyk/dualbrowser/ui/BookmarkBarTest.kt`
- Modify: `app/src/test/java/com/blulyk/dualbrowser/ui/ControlCenterTest.kt`

- [ ] **Step 1: Add failing Compose tests**

Use test tags to assert:

```kotlin
onNodeWithTag("browser-toolbar").assertIsDisplayed()
onNodeWithTag("tab-preview-${tab.id}").assertIsDisplayed().performClick()
onNodeWithTag("close-preview-${tab.id}").performClick()
onNodeWithTag("bookmark-https://example.com").performClick()
```

Assert preview selection dispatches `Focus`, close dispatches `Close`, and bookmark tapping dispatches `Navigate(focusedTabId, bookmark.url)`. Assert the empty bookmark hint is visible for an empty list.

- [ ] **Step 2: Run focused Compose tests and verify RED**

Expected: missing composables and test tags.

- [ ] **Step 3: Implement focused UI components**

Add Compose Material icons through the BOM. Build:

- `BrowserToolbar`: compact icon buttons, rounded navy address field, Go action, new/private actions, and overflow menu for bookmark, library, diagnostics, and dual-view.
- `TabPreviewCarousel`: `LazyRow` of 320 x 190 dp cards with `Image` thumbnails using `asImageBitmap`, navy placeholders, title/domain, private badge, selected border, and close icon.
- `BookmarkBar`: 56 dp high `LazyRow` of compact bookmark chips; click dispatches `Navigate` for the current tab.
- `ControlCenter`: full-height graphite `Column` that composes toolbar, weighted preview carousel, and persistent bookmark bar without horizontal control overflow.

Pass `previews` and `bookmarks` from both activities through `BrowserApp` and `ControlCenter`. Keep existing test tags for address, Go, new tab, private tab, back, forward, reload, and dual-view.

- [ ] **Step 4: Verify GREEN**

Run all three focused Compose test classes and existing `BrowserAppTest`; expect all tests to pass.

- [ ] **Step 5: Commit**

```powershell
git add gradle app/build.gradle.kts app/src/main/java/com/blulyk/dualbrowser/ui app/src/test/java/com/blulyk/dualbrowser/ui
git commit -m "feat: redesign Thor lower browser controls"
```

### Task 6: Automated and physical UI verification

**Files:**
- Verify: `app/build/outputs/apk/debug/app-debug.apk`
- Update: `docs/testing/ayn-thor-checklist.md`

- [ ] **Step 1: Run complete checks**

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```
Expected: `BUILD SUCCESSFUL` and no lint errors.

- [ ] **Step 2: Install and launch on the connected Thor**

Install with `adb install -r`, wake the device, clear logcat, and cold-launch `MainActivity`.

- [ ] **Step 3: Exercise the redesigned lower display**

Open three normal tabs and one private tab, load distinct pages, verify each real thumbnail, switch and close tabs through preview cards, add a bookmark, and navigate through the bookmark bar. Repeat with touch and L1/R1 controller tab switching.

- [ ] **Step 4: Capture visual evidence**

Capture physical display IDs `4630946441858561667` and `4630946482288158084`. Confirm the lower screenshot is 1240 x 1080, uses navy accents, shows no clipped primary action, and retains the bookmark bar.

- [ ] **Step 5: Freeze the UI baseline**

Update the Thor checklist with thumbnail and bookmark checks, run `git diff --check`, and commit the verified checklist.

```powershell
git add docs/testing/ayn-thor-checklist.md
git commit -m "docs: add lower-screen UI verification"
```

### Task 7: Launch-stability diagnostic handoff

**Files:**
- Create after evidence: `docs/superpowers/plans/2026-06-18-secondary-display-launch-fix.md`

- [ ] **Step 1: Establish a clean post-redesign baseline**

Record the exact verified UI commit SHA and rerun full tests before changing lifecycle code.

- [ ] **Step 2: Capture each failing launch transition three times**

For cold launch, warm launcher intent, Home-to-app, sleep/wake, and force-stop relaunch, collect `logcat`, `dumpsys display`, `dumpsys activity activities`, and screenshots for both physical displays.

- [ ] **Step 3: Trace the first incorrect transition**

Compare display power state, `MainActivity`/`SecondaryDisplayActivity` lifecycle, tracker owner, assignment callbacks, and secondary START count. State one root-cause hypothesis supported by timestamps.

- [ ] **Step 4: Write the dedicated fix plan**

Create the named plan with the confirmed failing transition, an automated regression test that fails before production edits, the minimal lifecycle change, and the exact ADB replay used for verification. Do not alter display production code during this diagnostic task.
