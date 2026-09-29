# Android wrapper

This module provides a Material 3 Android UI around an **Android-compatible arm64 executable** named `depotdownloader`.

Before releasing an APK, copy the executable to `app/src/main/assets/depotdownloader` and ensure it is an `ELF 64-bit LSB executable, ARM aarch64` built against the Android NDK/API level 26 or later. The app copies it into private app storage and executes it with `ProcessBuilder`; the binary is never executed from shared storage.

Build with Android Studio (JDK 17) or:

```bash
cd android
./gradlew assembleRelease
```

The source repository's `linux-arm64` artifact is **not** automatically suitable for Android. Android uses a different libc/linker ABI, so release builds must package an Android-targeted binary.
