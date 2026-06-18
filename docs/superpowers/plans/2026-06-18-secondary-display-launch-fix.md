# Secondary Display Launch Stability Fix

**Verified UI baseline:** `c0ce87277a496f5713b65b185414bc93b31d4033`

## Confirmed failing transition

1. Cold-launch `MainActivity` on display 0.
2. The coordinator launches `SecondaryDisplayActivity` on display 4.
3. Send the launcher intent again with display 4 focused.
4. Because `MainActivity` uses the default `standard` launch mode, Android creates another
   `MainActivity` in the lower task.

The captured failure has two `MainActivity` records on display 4, no Dual Browser activity on
display 0, the Thor launcher on the upper panel, and the single-display browser layout on the
lower panel. This matches the reported failure after opening the app a second time.

## Regression test

Add a Robolectric package-manager test that resolves `MainActivity` from the installed manifest
and asserts `ActivityInfo.LAUNCH_SINGLE_TASK`. Run it first against the baseline and verify that it
fails because the manifest currently reports `LAUNCH_MULTIPLE`.

## First hypothesis result

`android:launchMode="singleTask"` prevented a second instance, but the Thor moved the existing
primary task from display 0 to display 4 to honor the new launch display. The upper panel still
returned to the launcher, so launch mode alone is insufficient.

## Final lifecycle change

Keep `MainActivity` as the exported `MAIN/LAUNCHER` activity so Dual Browser remains a normal app
in Android's app drawer. Keep it `singleTask` to prevent duplicate primary instances. If Android
starts or moves that task onto the lower display, `AndroidDisplayCoordinator` immediately starts
the same task on the assigned upper display with `ActivityOptions`; no intermediary launcher
activity is used.

The manifest test verifies that the launcher intent resolves directly to `MainActivity`. The
display coordinator test verifies that restoration is requested only when the primary task is not
on the assigned upper display.

## Home-to-app race found during replay

After both activities go Home, `MainActivity` can receive its display assignment while the old
secondary owner still reports display 4. The one-shot launch check therefore does nothing. The
secondary owner then stops and changes the tracker to `null`; Compose notices and shows the
single-display layout above, but the coordinator is not called again, leaving the lower launcher
visible.

Add a tested `SecondaryLaunchReconciler` that re-evaluates the same assignment whenever either the
assignment or active-secondary ID changes. It must deduplicate identical pairs so the initial
StateFlow emission cannot launch twice, while still launching when the sequence changes from
`(assignment, 4)` to `(assignment, null)`.

## Verification replay

For each of three repetitions:

```powershell
adb shell am force-stop com.blulyk.dualbrowser
adb shell am start --display 0 -W -n com.blulyk.dualbrowser/.ui.MainActivity
adb shell am start --display 4 -W -a android.intent.action.MAIN `
  -c android.intent.category.LAUNCHER `
  -n com.blulyk.dualbrowser/.ui.MainActivity
```

Then capture both physical panels and `dumpsys activity activities`. Passing requires exactly one
`MainActivity` on display 0, one `SecondaryDisplayActivity` on display 4, the web surface visible
above, and the navy control center visible below. Also replay cold launch, warm launch, Home-to-app,
and sleep/wake once after the focused regression passes.
