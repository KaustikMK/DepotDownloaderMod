# Android wrapper

This module provides a Material 3 Android UI around an **Android-compatible arm64 executable** named `depotdownloader`.

Before building an APK, copy the executable to `app/src/main/assets/depotdownloader` and ensure it is an `ELF 64-bit LSB executable, ARM aarch64` built against the Android NDK/API level 26 or later. The app copies it into private app storage and executes it with `ProcessBuilder`; the binary is never executed from shared storage.

The Gradle build now refuses to produce either a debug or release APK if that asset is absent, empty, or not a 64-bit little-endian ARM executable. This prevents publishing an APK that reaches the download screen and then fails with a missing-binary error. Check a candidate before building:

```bash
file app/src/main/assets/depotdownloader
# Expected: ELF 64-bit LSB executable, ARM aarch64
```

In the app, choose either one dumped manifest named `<depotId>_<manifestId>.manifest` or a `.zip` archive containing those files. Archives may contain folders and unrelated files; the app imports every matching manifest, then invokes the downloader once per manifest with its depot and manifest IDs. Select a depot keys file separately.

Build with Android Studio (JDK 17) or:

```bash
cd android
./gradlew assembleRelease
```

The source repository's `linux-arm64` artifact is **not** automatically suitable for Android. Android uses a different libc/linker ABI, so builds must package an Android-targeted binary.
