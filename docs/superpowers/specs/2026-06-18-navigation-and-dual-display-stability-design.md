# Navigation and Dual-Display Stability Design

## Objective

Make tab creation, address-bar navigation, and repeated use reliable on the AYN Thor before publishing the first public beta.

## New Tab Behavior

- The initial session, every normal new tab, every private new tab, and the replacement created after closing the last tab open `https://www.google.com/`.
- A new tab becomes focused immediately and its URL is reflected in the address field.
- Restored existing tabs retain their saved URLs.

## Address Bar

- The address field remains editable after tab creation and after switching tabs.
- Submitting with the Android IME action or a visible `Go` button dispatches the same navigation command.
- Plain text is resolved as a Google search; valid host names and HTTP(S) URLs are resolved as addresses by the existing URL resolver.
- Empty input does not navigate.
- Page completion updates the displayed address without overwriting text while the user is actively editing it.

## Dual-Display Lifecycle

- `MainActivity` owns display assignment and requests the secondary activity only when the expected lower display is not already active.
- Display callbacks are idempotent: repeated callbacks, tab creation, focus changes, and relaunching the launcher intent do not create or refocus another secondary task.
- `SecondaryDisplayActivity` reports its actual display while started and clears that report only when the same instance stops.
- The upper screen removes its controls only after the lower activity is confirmed active on the assigned lower display. If that confirmation disappears, controls return to the upper screen.

## Error Handling

- A failed secondary launch is logged and leaves a usable single-screen browser on the upper display.
- Navigation with blank input is ignored rather than loading an invalid page.
- Renderer recovery behavior remains unchanged.

## Verification

- Unit tests cover all new-tab creation paths, blank navigation, search resolution, URL resolution, and idempotent secondary-launch decisions.
- Compose tests cover editing and submitting the address field from a newly created tab.
- The complete unit test, lint, and debug assembly tasks must pass.
- On the connected AYN Thor, verify: cold launch, repeated launcher intent, multiple new tabs, Google loading, text search, direct URL navigation, tab switching, closing the last tab, and stable activity placement on displays 0 and 4.

## Publication

- Publish only the verified source and build instructions to a new repository under the authenticated GitHub account `Blulyk`.
- Do not commit signing secrets, local SDK paths, generated APKs, build output, or device logs.
- Tag the verified beta and attach its APK only after the physical-device checks pass.
