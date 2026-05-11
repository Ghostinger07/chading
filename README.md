# Chading

A local-only, Pixel-style photo gallery for Android.
Nothing syncs, nothing uploads — it just reads what's already on your device via `MediaStore`.

- **Material You** with dynamic colors on Android 12+
- Edge-to-edge Compose UI, dark mode
- Date-grouped grid with **pinch to change column count** (2–5)
- Full-screen viewer with **pinch-to-zoom**, **double-tap zoom**, **tap-to-toggle chrome**, and **swipe down to dismiss**
- Albums tab grouped by MediaStore bucket (Camera, Screenshots, WhatsApp, etc.)
- No internet permission, no analytics, no cloud — truly local

## Requirements

- Android 8.0 (API 26) or newer
- `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` on Android 13+, or `READ_EXTERNAL_STORAGE` below that

## Download

APKs are published on every tagged release.

1. Go to the [Releases](../../releases) page
2. Pick the latest version
3. Download `app-release-unsigned-signed.apk` (or `app-debug.apk`) and install on your phone
4. You may need to enable **"Install unknown apps"** for your browser or file manager — this is an Android system setting, not something the app controls

For every push to `main` you can also grab the latest APK from the **Actions** tab → choose the latest "Android Build" run → scroll to **Artifacts**.

### Creating a release

Push a tag that starts with `v`, and the workflow will attach the APK(s) to a new GitHub Release:

```bash
git tag v0.1.0
git push origin v0.1.0
```

## Build from source

```bash
git clone https://github.com/Ghostinger07/chading.git
cd chading
./gradlew :app:assembleDebug
# APK lands in app/build/outputs/apk/debug/app-debug.apk
```

Requirements: JDK 17, Android SDK (API 34), Android Studio **Koala** or newer if you want to open it in an IDE.

## Project layout

```
app/src/main/kotlin/com/chading/photos/
  ChadingApp.kt              # Application, configures Coil image loader
  MainActivity.kt            # Single activity, hosts Compose navigation
  data/
    MediaItem.kt             # Photo/video model + Album model
    MediaStoreRepository.kt  # Reactive MediaStore queries (Flow)
  ui/
    PhotosViewModel.kt       # Holds the reactive media list
    theme/                   # Material 3 / Material You theme
    nav/ChadingNav.kt        # NavHost + bottom bar + permission gate
    photos/PhotosScreen.kt   # Date-grouped grid with pinch-to-zoom cols
    albums/AlbumsScreen.kt   # Grid of buckets
    albums/AlbumDetailScreen.kt
    viewer/ViewerScreen.kt   # Fullscreen pager with zoom + dismiss
```

## Signing note

The release APK is currently signed with Android's built-in debug key so it installs without extra setup.
To ship a proper release build with a stable app signature:

1. Generate a keystore (`keytool -genkeypair …`)
2. Add it as GitHub Secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`)
3. Wire a `signingConfigs.release` block in `app/build.gradle.kts` and the workflow

Happy to help with that step when you're ready.

## License

Do whatever you want with it.
