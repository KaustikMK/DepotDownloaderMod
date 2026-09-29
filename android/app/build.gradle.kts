plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

/**
 * Do not allow an APK that can reach the download screen without its downloader
 * to be assembled.  Assets are intentionally kept out of the source tree: the
 * executable is a release-specific Android arm64 artifact.
 */
val verifyBundledDownloader by tasks.registering {
    group = "verification"
    description = "Verifies that the Android arm64 depotdownloader asset is packaged."

    val downloader = layout.projectDirectory.file("src/main/assets/depotdownloader")
    doLast {
        val binary = downloader.asFile
        check(binary.isFile && binary.length() > 0) {
            "Missing bundled downloader: add an Android arm64 executable at ${binary.path}. " +
                "Refusing to assemble an APK that will fail at runtime."
        }

        val header = binary.inputStream().use { input -> input.readNBytes(20) }
        check(header.size == 20 && header.copyOfRange(0, 4).contentEquals(byteArrayOf(0x7F.toByte(), 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte()))) {
            "Bundled downloader must be an ELF executable: ${binary.path}"
        }
        check(header[4].toInt() == 2 && header[5].toInt() == 1 && (header[18].toInt() and 0xFF) == 0xB7 && header[19].toInt() == 0) {
            "Bundled downloader must be a 64-bit little-endian Android arm64 executable: ${binary.path}"
        }
    }
}

android {
    namespace = "com.depotdownloadermod.android"
    compileSdk = 35

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    defaultConfig {
        applicationId = "com.depotdownloadermod.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

// merge*Assets is on the path of every APK-producing variant, including debug.
// Hooking here prevents a release build from silently omitting the executable.
tasks.configureEach {
    if (name.startsWith("merge") && name.endsWith("Assets")) {
        dependsOn(verifyBundledDownloader)
    }
}
