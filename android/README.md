# Android wrapper

This module provides a Material 3 Android UI around an **Android-compatible arm64 executable** named `depotdownloader`.

The Android workflow publishes this repository's .NET downloader for `android-arm64`, compiles a native Android host, and packages the self-contained .NET runtime under `app/src/main/assets/depotdownloader/`. `DepotDownloaderMod.dll` is managed application code, not a Windows executable. Android installs the native host as `libdepotdownloader.so` in its native-library directory, which has the required executable permission and SELinux label; the app copies only the managed runtime into private app storage and launches that host with `ProcessBuilder`.

In the app, choose either one dumped manifest named `<depotId>_<manifestId>.manifest` or a `.zip` archive containing those files. Archives may contain folders and unrelated files; the app imports every matching manifest, then invokes the downloader once per manifest with its depot and manifest IDs. Select a depot keys file separately.

Build with Android Studio (JDK 17) or:

```bash
cd android
./gradlew assembleRelease
```

The GitHub Actions artifact is an installable release APK, signed with the Android debug certificate, with the downloader and its Android runtime bundled. It is suitable for direct installation and testing, but it is not signed for Play Store distribution. The source repository's `linux-arm64` artifact is not used because Android requires its own runtime ABI.
