# Android wrapper

This module provides a Material 3 Android UI around an **Android-compatible arm64 executable** named `depotdownloader`.

Before releasing an APK, copy the executable to `app/src/main/assets/depotdownloader` and ensure it is an `ELF 64-bit LSB executable, ARM aarch64` built against the Android NDK/API level 26 or later. The app copies it into private app storage and executes it with `ProcessBuilder`; the binary is never executed from shared storage.

In the app, choose either one dumped manifest named `<depotId>_<manifestId>.manifest` or a `.zip` archive containing those files. Archives may contain folders and unrelated files; the app imports every matching manifest, then invokes the downloader once per manifest with its depot and manifest IDs. Select a depot keys file separately.

Build with Android Studio (JDK 17) or:

```bash
cd android
./gradlew assembleRelease
```

The GitHub Actions debug artifact is a **development UI build**. It intentionally excludes the `verifyBundledDownloader` check because this repository does not contain a distributable Android executable; it will report that the downloader is missing when an import is run. Do not distribute it as a functional downloader.

The source repository's `linux-arm64` artifact is **not** automatically suitable for Android. Android uses a different libc/linker ABI, so release builds must package an Android-targeted binary.
