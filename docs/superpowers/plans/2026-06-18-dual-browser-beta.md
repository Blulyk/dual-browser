# Dual Browser Beta Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce a tested, installable public beta of a local-first Android browser optimized for the AYN Thor's two displays.

**Architecture:** One Android application module is divided into domain, engine, data, platform, and UI packages. `BrowserSessionManager` owns browser state, WebView implements an isolated engine contract, and `DisplayCoordinator` launches a dedicated Compose activity on the Thor's lower display while preserving a responsive single-display fallback.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, AndroidX WebKit, Room, DataStore, coroutines/StateFlow, JUnit, Robolectric, Compose UI tests, Android instrumented tests, Gradle Kotlin DSL, GitHub Actions.

---

## File Map

- `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`: build and dependency definitions.
- `scripts/bootstrap-android.ps1`: reproducible Windows SDK/JDK setup.
- `app/src/main/AndroidManifest.xml`: activities, permissions, secure network policy, and file provider.
- `app/src/main/java/com/blulyk/dualbrowser/domain/*`: pure tab, URL, session, and command logic.
- `app/src/main/java/com/blulyk/dualbrowser/engine/*`: WebView ownership, callbacks, settings, and state restoration.
- `app/src/main/java/com/blulyk/dualbrowser/data/*`: Room entities/DAOs and DataStore preferences.
- `app/src/main/java/com/blulyk/dualbrowser/platform/*`: displays, downloads, chooser, sharing, and permissions.
- `app/src/main/java/com/blulyk/dualbrowser/ui/*`: activities and focused Compose screens/components.
- `app/src/test/*`: JVM domain, repository, and coordinator tests.
- `app/src/androidTest/*`: WebView fixtures, Compose behavior, and activity tests.
- `.github/workflows/android.yml`, `README.md`: CI, release build, installation, and testing instructions.

### Task 1: Bootstrap And Launchable Shell

**Files:**
- Create: `scripts/bootstrap-android.ps1`
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `gradlew`, `gradlew.bat`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/blulyk/dualbrowser/DualBrowserApplication.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/MainActivity.kt`
- Test: `app/src/test/java/com/blulyk/dualbrowser/ui/MainActivityTest.kt`

- [ ] **Step 1: Add a failing launch test**

```kotlin
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Test fun launch_showsProductName() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withText("Dual Browser")).check(matches(isDisplayed()))
        }
    }
}
```

- [ ] **Step 2: Bootstrap the toolchain and verify the test fails before the activity exists**

Run: `powershell -ExecutionPolicy Bypass -File scripts/bootstrap-android.ps1`, then `./gradlew testDebugUnitTest`

Expected: SDK 36 and build-tools 36.0.0 are installed; compilation fails because `MainActivity` is unresolved.

- [ ] **Step 3: Add the minimal Compose activity and build configuration**

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Text("Dual Browser") } }
    }
}
```

- [ ] **Step 4: Run verification**

Run: `./gradlew testDebugUnitTest assembleDebug`

Expected: tests pass and `app/build/outputs/apk/debug/app-debug.apk` exists.

- [ ] **Step 5: Commit**

Run: `git add scripts settings.gradle.kts build.gradle.kts gradle app && git commit -m "build: bootstrap Android browser app"`

### Task 2: URL Resolution And Browser State

**Files:**
- Create: `app/src/main/java/com/blulyk/dualbrowser/domain/BrowserModels.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/domain/UrlResolver.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/domain/BrowserSessionManager.kt`
- Test: `app/src/test/java/com/blulyk/dualbrowser/domain/UrlResolverTest.kt`
- Test: `app/src/test/java/com/blulyk/dualbrowser/domain/BrowserSessionManagerTest.kt`

- [ ] **Step 1: Test URL, search, tab, focus, close, and private-state rules**

```kotlin
@Test fun hostBecomesHttpsUrl() = assertEquals("https://example.com", resolver.resolve("example.com"))
@Test fun wordsBecomeEncodedSearch() = assertEquals("https://www.google.com/search?q=dual+browser", resolver.resolve("dual browser"))
@Test fun promotingTabAssignsLowerSurface() {
    val state = reducer.reduce(initial, BrowserCommand.PromoteToLower(initial.focusedTabId))
    assertEquals(initial.focusedTabId, state.lowerTabId)
}
@Test fun privateTabsAreExcludedFromRestorableSession() {
    assertTrue(manager.restorableTabs().none(BrowserTab::isPrivate))
}
```

- [ ] **Step 2: Run the focused tests and confirm unresolved symbols fail**

Run: `./gradlew testDebugUnitTest --tests "*.domain.*"`

Expected: compilation fails for `UrlResolver`, `BrowserCommand`, and `BrowserSessionManager`.

- [ ] **Step 3: Implement immutable models and a StateFlow-backed command reducer**

```kotlin
data class BrowserTab(val id: String, val url: String, val title: String = "New tab", val isPrivate: Boolean = false)
data class BrowserState(val tabs: List<BrowserTab>, val focusedTabId: String, val lowerTabId: String? = null)
sealed interface BrowserCommand {
    data class Navigate(val tabId: String, val input: String) : BrowserCommand
    data class PromoteToLower(val tabId: String) : BrowserCommand
    data class Close(val tabId: String) : BrowserCommand
}
```

- [ ] **Step 4: Run domain tests**

Run: `./gradlew testDebugUnitTest --tests "*.domain.*"`

Expected: all domain tests pass.

- [ ] **Step 5: Commit**

Run: `git add app/src/main/java/com/blulyk/dualbrowser/domain app/src/test/java/com/blulyk/dualbrowser/domain && git commit -m "feat: add browser session domain"`

### Task 3: WebView Engine

**Files:**
- Create: `app/src/main/java/com/blulyk/dualbrowser/engine/BrowserEngine.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/engine/WebViewBrowserEngine.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/engine/WebViewFactory.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/engine/WebViewCallbacks.kt`
- Test: `app/src/test/java/com/blulyk/dualbrowser/engine/WebViewFactoryTest.kt`
- Test: `app/src/androidTest/java/com/blulyk/dualbrowser/engine/WebViewBrowserEngineTest.kt`
- Test fixture: `app/src/androidTest/assets/pages/navigation.html`

- [ ] **Step 1: Test secure defaults and navigation callbacks**

```kotlin
@Test fun factoryUsesSecureSettings() {
    val webView = factory.create(context, callbacks)
    assertFalse(webView.settings.allowFileAccess)
    assertFalse(webView.settings.allowContentAccess)
    assertEquals(WebSettings.MIXED_CONTENT_NEVER_ALLOW, webView.settings.mixedContentMode)
    assertTrue(webView.settings.javaScriptEnabled)
}
```

- [ ] **Step 2: Run the engine tests and confirm they fail**

Run: `./gradlew testDebugUnitTest --tests "*.engine.*"`

Expected: compilation fails because `WebViewFactory` does not exist.

- [ ] **Step 3: Implement the engine contract and WebView adapter**

```kotlin
interface BrowserEngine {
    fun load(url: String)
    fun back(): Boolean
    fun forward(): Boolean
    fun reload()
    fun stop()
    fun find(query: String)
    fun setDesktopMode(enabled: Boolean)
    fun destroy()
}
```

`WebViewFactory` configures Safe Browsing, cookies, DOM storage, JavaScript, zoom, mixed-content blocking, file-access blocking, renderer-death callbacks, `WebChromeClient`, and `WebViewClient`. It never calls `handler.proceed()` for TLS errors.

- [ ] **Step 4: Run JVM and device engine tests**

Run: `./gradlew testDebugUnitTest connectedDebugAndroidTest`

Expected: secure-setting and local HTML navigation tests pass.

- [ ] **Step 5: Commit**

Run: `git add app/src/main/java/com/blulyk/dualbrowser/engine app/src/test app/src/androidTest && git commit -m "feat: add secure WebView engine"`

### Task 4: Single-Display Browser UI

**Files:**
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/BrowserApp.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/BrowserViewModel.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/WebSurface.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/ControlCenter.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/AddressBar.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/TabSwitcher.kt`
- Test: `app/src/androidTest/java/com/blulyk/dualbrowser/ui/ControlCenterTest.kt`

- [ ] **Step 1: Test address submission and core control semantics**

```kotlin
@Test fun submittingAddressNavigatesFocusedTab() {
    composeRule.setContent { ControlCenter(state, onCommand) }
    composeRule.onNodeWithTag("address").performTextInput("example.com")
    composeRule.onNodeWithTag("address").performImeAction()
    assertEquals(BrowserCommand.Navigate(state.focusedTabId, "example.com"), commands.single())
}
@Test fun systemThemeKeepsControlsReadable() {
    composeRule.setContent { DualBrowserTheme(darkTheme = true) { ControlCenter(state, onCommand) } }
    composeRule.onNodeWithTag("control-center").assertIsDisplayed()
}
```

- [ ] **Step 2: Run the UI test and confirm missing composables fail compilation**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.blulyk.dualbrowser.ui.ControlCenterTest`

Expected: compilation fails for `ControlCenter`.

- [ ] **Step 3: Implement the adaptive single-window UI**

```kotlin
@Composable
fun BrowserApp(state: BrowserState, onCommand: (BrowserCommand) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        WebSurface(state.focusedTabId, Modifier.fillMaxSize())
        ControlCenter(state, onCommand, Modifier.align(Alignment.BottomCenter))
    }
}
```

The control center contains tab previews, address input, back/forward/reload, new tab, private tab, bookmark, share, find, desktop mode, history, downloads, and settings. Expanded controls use a modal bottom sheet on one display. `DualBrowserTheme` supports system, light, and dark modes through a DataStore preference.

- [ ] **Step 4: Verify UI and build**

Run: `./gradlew connectedDebugAndroidTest assembleDebug`

Expected: UI tests pass and the debug APK renders a navigable WebView with bottom controls.

- [ ] **Step 5: Commit**

Run: `git add app/src/main/java/com/blulyk/dualbrowser/ui app/src/androidTest/java/com/blulyk/dualbrowser/ui && git commit -m "feat: add adaptive browser interface"`

### Task 5: AYN Thor Dual-Display Coordination

**Files:**
- Create: `app/src/main/java/com/blulyk/dualbrowser/platform/DisplayCoordinator.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/platform/DisplaySnapshot.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/SecondaryDisplayActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/MainActivity.kt`
- Test: `app/src/test/java/com/blulyk/dualbrowser/platform/DisplayCoordinatorTest.kt`

- [ ] **Step 1: Test deterministic display assignment and disconnect fallback**

```kotlin
@Test fun largestDisplayIsUpperAndOtherIsLower() {
    val assignment = coordinator.assign(listOf(display(1920, 1080, 0), display(1240, 1080, 2)))
    assertEquals(0, assignment.upperId)
    assertEquals(2, assignment.lowerId)
}
@Test fun removalClearsLowerAssignment() = assertNull(coordinator.assign(listOf(display(1920, 1080, 0))).lowerId)
```

- [ ] **Step 2: Run tests and confirm missing coordinator failure**

Run: `./gradlew testDebugUnitTest --tests "*.DisplayCoordinatorTest"`

Expected: compilation fails for `DisplayCoordinator`.

- [ ] **Step 3: Implement display observation and lower activity launch**

```kotlin
val options = ActivityOptions.makeBasic().setLaunchDisplayId(lowerDisplayId)
context.startActivity(
    Intent(context, SecondaryDisplayActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    options.toBundle()
)
```

The two activities bind to the application-scoped `BrowserSessionManager`. The primary activity hosts the upper WebView. The lower activity renders `ControlCenter`, or `WebSurface` plus `CompactControls` when `lowerTabId` is set. Launch failure records a diagnostic capability and leaves the single-display UI intact.

- [ ] **Step 4: Verify coordinator and activity launch tests**

Run: `./gradlew testDebugUnitTest connectedDebugAndroidTest`

Expected: assignment tests pass; emulator fallback remains single-window.

- [ ] **Step 5: Commit**

Run: `git add app/src/main app/src/test && git commit -m "feat: coordinate dual Android displays"`

### Task 6: Local Persistence And Privacy

**Files:**
- Create: `app/src/main/java/com/blulyk/dualbrowser/data/BrowserDatabase.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/data/BookmarkEntity.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/data/HistoryEntity.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/data/BrowserDao.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/data/BrowserRepository.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/data/BrowserPreferences.kt`
- Test: `app/src/test/java/com/blulyk/dualbrowser/data/BrowserRepositoryTest.kt`

- [ ] **Step 1: Test bookmarks, history ordering, session restore, clearing, and private exclusion**

```kotlin
@Test fun privateVisitsAreNotPersisted() = runTest {
    repository.recordVisit(tab(isPrivate = true, url = "https://private.example"))
    assertTrue(repository.history().first().isEmpty())
}
@Test fun latestNormalVisitAppearsFirst() = runTest {
    repository.recordVisit(tab(url = "https://one.example"))
    repository.recordVisit(tab(url = "https://two.example"))
    assertEquals("https://two.example", repository.history().first().first().url)
}
```

- [ ] **Step 2: Run repository tests and confirm missing database types fail**

Run: `./gradlew testDebugUnitTest --tests "*.data.*"`

Expected: compilation fails for `BrowserRepository`.

- [ ] **Step 3: Implement Room repositories and DataStore preferences**

```kotlin
suspend fun recordVisit(tab: BrowserTab) {
    if (!tab.isPrivate) dao.insertHistory(HistoryEntity(url = tab.url, title = tab.title, visitedAt = clock.millis()))
}
```

Normal tab URLs and ordering are persisted after navigation changes. Private state remains memory-only and is cleared from WebView storage when its final private tab closes.

- [ ] **Step 4: Run persistence tests**

Run: `./gradlew testDebugUnitTest --tests "*.data.*"`

Expected: repository and privacy tests pass with an in-memory Room database.

- [ ] **Step 5: Commit**

Run: `git add app/src/main/java/com/blulyk/dualbrowser/data app/src/test/java/com/blulyk/dualbrowser/data && git commit -m "feat: persist local browser data"`

### Task 7: Android Browser Integrations

**Files:**
- Create: `app/src/main/java/com/blulyk/dualbrowser/platform/DownloadHandler.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/platform/FileChooserHandler.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/platform/PermissionHandler.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/platform/ExternalIntentHandler.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/platform/FullscreenHandler.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/engine/WebViewCallbacks.kt`
- Test: `app/src/test/java/com/blulyk/dualbrowser/platform/ExternalIntentHandlerTest.kt`
- Test: `app/src/androidTest/java/com/blulyk/dualbrowser/platform/BrowserIntegrationTest.kt`

- [ ] **Step 1: Test unsupported external intents and local fixture callbacks**

```kotlin
@Test fun unsupportedSchemeReturnsReadableFailure() {
    assertEquals(ExternalResult.NoHandler("maps"), handler.open(Uri.parse("maps://place")))
}
@Test fun popupRequiresExplicitTargetTab() {
    assertEquals(PopupDecision.OpenNewTab, popupPolicy.decide(userGesture = true))
}
```

- [ ] **Step 2: Run focused tests and confirm handler types are missing**

Run: `./gradlew testDebugUnitTest --tests "*.platform.*"`

Expected: compilation fails for `ExternalIntentHandler`.

- [ ] **Step 3: Implement Android contracts and WebChromeClient routing**

Downloads use `DownloadManager`, file input uses `ActivityResultContracts.StartActivityForResult`, site camera/microphone requests require both origin approval and Android runtime permission, geolocation uses the WebChrome callback, external schemes use resolved intents, and custom views enter immersive full-screen until back is pressed. User-gesture popups open in a visible new tab; unsolicited popups are rejected. JavaScript alert, confirm, and prompt requests use explicit Material dialogs and return cancellation when their host activity is unavailable.

- [ ] **Step 4: Verify fixture-driven integrations**

Run: `./gradlew testDebugUnitTest connectedDebugAndroidTest`

Expected: external-intent unit tests and HTML fixture integration tests pass.

- [ ] **Step 5: Commit**

Run: `git add app/src/main app/src/test app/src/androidTest && git commit -m "feat: add Android browser integrations"`

### Task 8: Controller Navigation And Diagnostics

**Files:**
- Create: `app/src/main/java/com/blulyk/dualbrowser/platform/ControllerMapper.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/platform/DiagnosticsCollector.kt`
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/DiagnosticsScreen.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/MainActivity.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/SecondaryDisplayActivity.kt`
- Test: `app/src/test/java/com/blulyk/dualbrowser/platform/ControllerMapperTest.kt`

- [ ] **Step 1: Test deterministic key mappings**

```kotlin
@Test fun shoulderButtonsSwitchTabs() {
    assertEquals(BrowserAction.PreviousTab, mapper.map(KeyEvent.KEYCODE_BUTTON_L1))
    assertEquals(BrowserAction.NextTab, mapper.map(KeyEvent.KEYCODE_BUTTON_R1))
}
@Test fun buttonBMapsToBack() = assertEquals(BrowserAction.Back, mapper.map(KeyEvent.KEYCODE_BUTTON_B))
```

- [ ] **Step 2: Run tests and confirm mapper is missing**

Run: `./gradlew testDebugUnitTest --tests "*.ControllerMapperTest"`

Expected: compilation fails for `ControllerMapper`.

- [ ] **Step 3: Implement key routing and local diagnostics export**

Controller actions map D-pad to focus, A to activate, B to back, L1/R1 to adjacent tabs, and Select to focus the address field. Diagnostics report app/WebView versions, feature support, display IDs, dimensions, density, refresh rate, flags, and launch capability. Export uses `Intent.ACTION_SEND` only after a user action.

- [ ] **Step 4: Verify tests and lint**

Run: `./gradlew testDebugUnitTest lintDebug`

Expected: mappings pass and lint reports no errors.

- [ ] **Step 5: Commit**

Run: `git add app/src/main app/src/test && git commit -m "feat: add controller navigation and diagnostics"`

### Task 9: Recovery And Beta Acceptance Suite

**Files:**
- Create: `app/src/main/java/com/blulyk/dualbrowser/ui/RecoveryView.kt`
- Create: `app/src/androidTest/java/com/blulyk/dualbrowser/BetaAcceptanceTest.kt`
- Create: `docs/testing/ayn-thor-checklist.md`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/engine/WebViewCallbacks.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/platform/DisplayCoordinator.kt`

- [ ] **Step 1: Add automated recovery and privacy acceptance tests**

```kotlin
@Test fun closingLastPrivateTabLeavesNoRestorablePrivateState() {
    manager.dispatch(BrowserCommand.Close(privateTab.id))
    assertTrue(manager.restorableTabs().none(BrowserTab::isPrivate))
}
@Test fun rendererDeathMarksOnlyItsTabRecoverable() {
    callbacks.onRendererGone(firstTab.id)
    assertTrue(state.tab(firstTab.id).needsRecovery)
    assertFalse(state.tab(secondTab.id).needsRecovery)
}
```

- [ ] **Step 2: Run acceptance tests and observe the recovery test fail**

Run: `./gradlew testDebugUnitTest connectedDebugAndroidTest`

Expected: renderer recovery assertion fails before recovery state is wired.

- [ ] **Step 3: Implement recovery UI and physical checklist**

`RecoveryView` offers reload and close without affecting other tabs. The Thor checklist records cold launch placement, second web surface, touch/controller input, sleep/wake, backgrounding, display loss, login, full-screen video, uploads, downloads, permissions, private cleanup, WebView version, and diagnostic export.

- [ ] **Step 4: Run the full quality gate**

Run: `./gradlew clean testDebugUnitTest connectedDebugAndroidTest lintDebug assembleDebug`

Expected: all tasks succeed and the debug APK is produced.

- [ ] **Step 5: Commit**

Run: `git add app docs/testing && git commit -m "test: add browser beta acceptance coverage"`

### Task 10: Public Release And GitHub Publication

**Files:**
- Create: `.github/workflows/android.yml`
- Create: `README.md`
- Create: `CHANGELOG.md`
- Create: `docs/privacy.md`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add a workflow syntax and release configuration check**

Run: `./gradlew signingReport assembleRelease`

Expected before configuration: release build reports that release signing secrets are absent and uses the documented local debug-signed beta path.

- [ ] **Step 2: Add CI and tagged-release workflow**

```yaml
name: Android
on:
  push:
  pull_request:
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21
          cache: gradle
      - uses: android-actions/setup-android@v3
      - run: sdkmanager "platforms;android-36" "build-tools;36.0.0"
      - run: ./gradlew testDebugUnitTest lintDebug assembleDebug
      - uses: actions/upload-artifact@v4
        with:
          name: dual-browser-debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
```

- [ ] **Step 3: Document installation, limitations, privacy, and Thor testing**

README commands include `adb install -r app/build/outputs/apk/debug/app-debug.apk` and the GitHub Releases APK path. Release notes identify WebView dependency, local-only data, API 28 minimum, controller mappings, known OEM display limitations, and APK SHA-256.

- [ ] **Step 4: Run final verification and compute checksum**

Run: `./gradlew clean testDebugUnitTest lintDebug assembleDebug assembleRelease; Get-FileHash app/build/outputs/apk/debug/app-debug.apk -Algorithm SHA256`

Expected: builds and tests pass; PowerShell prints a SHA-256 value for the APK.

- [ ] **Step 5: Publish**

Run: `gh auth status`, create or select the public GitHub repository, push `master`, tag `v0.1.0-beta.1`, attach the APK and checksum to a prerelease, and verify the release page and downloadable asset.

- [ ] **Step 6: Commit**

Run: `git add .github README.md CHANGELOG.md docs/privacy.md app/build.gradle.kts && git commit -m "docs: prepare public beta release"`
