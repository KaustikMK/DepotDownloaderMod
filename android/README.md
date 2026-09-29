# Android wrapper

This module provides a Material 3 Android UI around an **Android-compatible arm64 executable** named `depotdownloader`.

The Android workflow publishes this repository's .NET downloader for `android-arm64`, compiles a native Android executable named `depotdownloader`, and packages it with the self-contained .NET runtime under `app/src/main/assets/depotdownloader/`. `DepotDownloaderMod.dll` is managed application code, not a Windows executable; the bundled `depotdownloader` ELF is the Android executable that starts it. The app copies the runtime into private app storage and executes that host with `ProcessBuilder`; it is never executed from shared storage.

In the app, choose either one dumped manifest named `<depotId>_<manifestId>.manifest` or a `.zip` archive containing those files. Archives may contain folders and unrelated files; the app imports every matching manifest, then invokes the downloader once per manifest with its depot and manifest IDs. Select a depot keys file separately.

Build with Android Studio (JDK 17) or:

```bash
cd android
./gradlew assembleRelease
```

The GitHub Actions artifact is a release APK with the downloader and its Android runtime bundled. The source repository's `linux-arm64` artifact is not used because Android requires its own runtime ABI.
