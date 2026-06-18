# Navigation and Dual-Display Stability Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Google-backed new tabs, address navigation, and the AYN Thor secondary display reliable, then publish the verified beta.

**Architecture:** Keep tab defaults and input resolution in the domain layer, make the Compose address field expose one shared submit path, and make secondary-display ownership explicit and idempotent. Verify pure behavior with unit tests, UI behavior with Compose tests, and lifecycle behavior with ADB on displays 0 and 4.

**Tech Stack:** Kotlin, Android SDK 36, Jetpack Compose, WebView/Chromium, StateFlow, JUnit, Robolectric, Gradle, ADB, GitHub CLI.

---

### Task 1: Google-backed tabs and safe navigation

**Files:**
- Modify: `app/src/main/java/com/blulyk/dualbrowser/domain/BrowserSessionManager.kt`
- Modify: `app/src/test/java/com/blulyk/dualbrowser/domain/BrowserSessionManagerTest.kt`
- Modify: `app/src/test/java/com/blulyk/dualbrowser/domain/UrlResolverTest.kt`

- [ ] **Step 1: Write failing domain tests**

Add assertions that normal/private new tabs and the replacement after closing the final tab use `BrowserSessionManager.HOME_URL`, and that `Navigate(tabId, "   ")` preserves the current URL.

```kotlin
assertEquals(BrowserSessionManager.HOME_URL, manager.state.value.focusedTab.url)
manager.dispatch(BrowserCommand.Navigate(firstTab.id, "   "))
assertEquals(firstTab.url, manager.state.value.focusedTab.url)
```

- [ ] **Step 2: Verify the tests fail**

Run:
```powershell
.\gradlew.bat testDebugUnitTest --tests "com.blulyk.dualbrowser.domain.BrowserSessionManagerTest"
```
Expected: failures showing `about:blank` instead of Google and blank input changing the URL.

- [ ] **Step 3: Implement the domain behavior**

Define one public constant and use it in the default state, `NewTab`, and last-tab replacement. Ignore blank navigation before calling the resolver.

```kotlin
companion object {
    const val HOME_URL = "https://www.google.com/"
}

is BrowserCommand.Navigate -> if (command.input.isBlank()) current else current.updateTab(command.tabId) {
    it.copy(url = resolver.resolve(command.input), needsRecovery = false)
}
```

- [ ] **Step 4: Verify domain tests pass**

Run the focused command from Step 2 and `UrlResolverTest`; expect `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/blulyk/dualbrowser/domain/BrowserSessionManager.kt app/src/test/java/com/blulyk/dualbrowser/domain
git commit -m "fix: make new tabs navigable Google pages"
```

### Task 2: Reliable address submission

**Files:**
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/ControlCenter.kt`
- Modify: `app/src/test/java/com/blulyk/dualbrowser/ui/ControlCenterTest.kt`

- [ ] **Step 1: Write failing Compose tests**

Add a test that clears the Google URL, enters `dual browser`, taps a node tagged `go`, and receives exactly `BrowserCommand.Navigate(tab.id, "dual browser")`. Add a recomposition test that switches focused tabs and verifies the field contains the new tab URL.

```kotlin
composeRule.onNodeWithTag("address").performTextClearance()
composeRule.onNodeWithTag("address").performTextInput("dual browser")
composeRule.onNodeWithTag("go").performClick()
assertEquals(BrowserCommand.Navigate(tab.id, "dual browser"), commands.single())
```

- [ ] **Step 2: Verify the tests fail**

Run:
```powershell
.\gradlew.bat testDebugUnitTest --tests "com.blulyk.dualbrowser.ui.ControlCenterTest"
```
Expected: no node tagged `go` and stale address content on tab change.

- [ ] **Step 3: Implement one submit path**

Key the saved address state by focused tab ID and URL, ignore blank submissions, use the same `navigate` lambda for IME and a visible `Go` button, and tag the button `go`.

```kotlin
var address by rememberSaveable(state.focusedTabId, state.focusedTab.url) {
    mutableStateOf(state.focusedTab.url)
}
val navigate = {
    if (address.isNotBlank()) onCommand(BrowserCommand.Navigate(state.focusedTabId, address))
}
```

- [ ] **Step 4: Verify UI tests pass**

Run the focused command from Step 2; expect `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/blulyk/dualbrowser/ui/ControlCenter.kt app/src/test/java/com/blulyk/dualbrowser/ui/ControlCenterTest.kt
git commit -m "fix: make address submission reliable"
```

### Task 3: Idempotent secondary-display ownership

**Files:**
- Modify: `app/src/main/java/com/blulyk/dualbrowser/platform/DisplayCoordinator.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/platform/AndroidDisplayCoordinator.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/platform/SecondaryDisplayTracker.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/MainActivity.kt`
- Modify: `app/src/main/java/com/blulyk/dualbrowser/ui/SecondaryDisplayActivity.kt`
- Modify: `app/src/test/java/com/blulyk/dualbrowser/platform/DisplayCoordinatorTest.kt`
- Modify: `app/src/test/java/com/blulyk/dualbrowser/platform/SecondaryDisplayTrackerTest.kt`

- [ ] **Step 1: Write failing lifecycle tests**

Test that launch is rejected when the expected lower ID is already active, and that stopping an older owner cannot clear a newer owner on the same display.

```kotlin
assertFalse(coordinator.shouldLaunchLower(0, assignment, activeSecondaryDisplayId = 2))
tracker.started("old", 2)
tracker.started("new", 2)
tracker.stopped("old")
assertEquals(2, tracker.activeDisplayId.value)
```

- [ ] **Step 2: Verify the tests fail**

Run both platform test classes; expect signature/ownership failures.

- [ ] **Step 3: Implement ownership and idempotence**

Extend `shouldLaunchLower` and `launchLowerIfNeeded` with `activeSecondaryDisplayId`. Store an opaque owner with the active display in `SecondaryDisplayTracker`; clear only when the matching owner stops. In `MainActivity`, pass the tracker's current value before launching; in `SecondaryDisplayActivity`, use a stable per-instance owner.

```kotlin
fun shouldLaunchLower(currentDisplayId: Int, assignment: DisplayAssignment, activeSecondaryDisplayId: Int?): Boolean =
    assignment.lowerId != null && currentDisplayId == assignment.upperId &&
        activeSecondaryDisplayId != assignment.lowerId
```

- [ ] **Step 4: Verify platform tests pass**

Run both focused platform test classes; expect `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/blulyk/dualbrowser/platform app/src/main/java/com/blulyk/dualbrowser/ui/MainActivity.kt app/src/main/java/com/blulyk/dualbrowser/ui/SecondaryDisplayActivity.kt app/src/test/java/com/blulyk/dualbrowser/platform
git commit -m "fix: stabilize secondary display ownership"
```

### Task 4: Full build and AYN Thor verification

**Files:**
- Verify: `app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 1: Run the complete local checks**

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```
Expected: `BUILD SUCCESSFUL` with no lint errors.

- [ ] **Step 2: Install cleanly on the connected Thor**

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```
Expected: `Success`.

- [ ] **Step 3: Exercise the physical workflow**

Wake the device, cold-launch the app, create normal and private tabs, submit `dual browser`, submit `example.com`, switch tabs, close the final tab, and send the launcher intent repeatedly. Capture screenshots for physical displays `4630946441858561667` and `4630946482288158084`.

- [ ] **Step 4: Check placement and logs**

Confirm one resumed `MainActivity` on display 0, one resumed `SecondaryDisplayActivity` on display 4, no duplicate secondary START after repeated display callbacks, and no `AndroidRuntime`, renderer, or activity timeout errors.

### Task 5: Publish the verified beta

**Files:**
- Verify: `.gitignore`
- Modify if needed: `README.md`

- [ ] **Step 1: Audit publish contents**

Run `git status`, `git ls-files`, and secret/path searches. Confirm `signing.properties`, keystores, `local.properties`, `.gradle`, `build`, `.worktrees`, APKs, screenshots, and logs are absent from tracked files.

- [ ] **Step 2: Create the GitHub repository**

Use authenticated account `Blulyk` and create `dual-browser` as a public repository from the current checkout, then push `master`.

```powershell
gh repo create Blulyk/dual-browser --public --source . --remote origin --push
```

- [ ] **Step 3: Tag and release**

Build the signed release using the existing local signing configuration, tag the verified beta, push the tag, and attach the signed APK with release notes describing AYN Thor support and beta limitations.

- [ ] **Step 4: Verify publication**

Open the repository and release metadata through GitHub, confirm the default branch, tag, release asset, and public clone URL, then report the repository URL and APK installation path.
