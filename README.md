# Infinity Note

A lightning-fast Android Markdown note editor built with Kotlin, Jetpack Compose, Material 3, and local Room storage.

## Features

- **Instant Local Auto-Save**: Notes save directly to on-device Room SQLite storage as you write, with lifecycle flush protection.
- **Clutter-Free Main Screen**: Notes appear as tinted cards with rendered Markdown previews, a black-and-purple `+` add button, and long-press actions for **Heart / Unheart** and **Delete**.
- **Focused Markdown Editor**: Clean title and body fields with live inline Markdown syntax styling (`#` headings, `**bold**`, `*italic*`, `` `code` ``, `> quotes`, lists, and task checkboxes).
- **Infinity Menu**: Upper-left purple infinity circle expands to provide **Back**, **Color** (7 adaptive note themes), **Undo**, and **Redo**.

## APK

The prebuilt debug APK is located in:

```text
test/infinity-note.apk
```

## Building from Source

1. Ensure JDK 17+ and Android SDK (API 36) are installed.
2. Build the debug APK:
   ```bash
   gradle :app:assembleDebug
   ```
3. Run unit and Robolectric tests:
   ```bash
   gradle :app:testDebugUnitTest
   ```
