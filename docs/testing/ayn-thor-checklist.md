# AYN Thor Beta Checklist

Record the diagnostics report and WebView version with every test run.

## Displays

- [ ] Cold launch places the primary web surface on the upper display.
- [ ] The lower display opens the control center without a duplicate task in Recents.
- [ ] Dual view opens the selected tab on the lower display.
- [ ] Controls returns the lower display to the control center.
- [ ] Closing or disabling the lower display returns to the single-display layout.

## Navigation And Input

- [ ] Address entry resolves hosts and search terms correctly.
- [ ] Back, forward, reload, new tab, close tab, and private tab work by touch.
- [ ] Creating or focusing a tab scrolls its preview fully into view.
- [ ] B navigates back; L1 and R1 switch tabs.
- [ ] D-pad focus and A activation work through standard Android focus navigation.

## Lower-Screen Browser UI

- [ ] The 1240 x 1080 lower capture uses the navy palette with readable text and no clipped primary controls.
- [ ] Every loaded tab shows a real page thumbnail; private thumbnails remain memory-only.
- [ ] Tapping a thumbnail focuses that tab and its close button removes only that tab.
- [ ] The bookmark bar remains visible below the tab carousel.
- [ ] Tapping a bookmark navigates the focused tab without opening another tab.

## Web Compatibility

- [ ] A common account can sign in and remain signed in after process restart.
- [ ] Fullscreen video enters and exits with B/Back.
- [ ] File input opens Android's picker and returns the chosen file.
- [ ] Downloads appear in Android Downloads with the expected filename.
- [ ] Camera, microphone, and location requests show Android permission prompts.
- [ ] External schemes open their installed app or show a readable failure.

## State And Privacy

- [ ] Normal tabs survive background/foreground, sleep/wake, and process recreation.
- [ ] Private tabs and visits are absent after the private session closes.
- [ ] Renderer recovery reloads only the affected tab.
- [ ] Diagnostics remain local until Export is pressed.

## Installation

- [ ] `adb install -r app/build/outputs/apk/debug/app-debug.apk` succeeds.
- [ ] The app launches on the current Thor firmware without a crash loop.

