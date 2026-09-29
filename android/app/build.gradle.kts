plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
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

    buildTypes {
        getByName("release") {
            // CI artifacts are downloaded and installed directly, so they must
            // be APK-signed. A production pipeline can replace this with its
            // own signing config through the normal Gradle configuration.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

// The executable is supplied by the release-packaging process rather than
// checked into source control. Development APKs can still exercise the UI, but
// a distributable release must not be assembled without it.
val bundledDownloader = layout.projectDirectory.file("src/main/assets/depotdownloader/depotdownloader")
val verifyBundledDownloader by tasks.registering {
    inputs.file(bundledDownloader).optional()

    doLast {
        check(bundledDownloader.asFile.isFile) {
            "Missing bundled downloader: add an Android arm64 executable at " +
                "${bundledDownloader.asFile}. Refusing to assemble an APK that will fail at runtime."
        }
    }
}

tasks.configureEach {
    if (name == "preReleaseBuild") {
        dependsOn(verifyBundledDownloader)
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
