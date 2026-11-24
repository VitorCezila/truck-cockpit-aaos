plugins {
    alias(libs.plugins.truckcockpit.android.application)
    alias(libs.plugins.truckcockpit.android.compose)
    alias(libs.plugins.truckcockpit.android.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.cezila.truckcockpit"

    defaultConfig {
        applicationId = "com.cezila.truckcockpit"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            // R8: code shrinking, optimization and resource shrinking. The libraries ship their own
            // keep rules; the Car API is a platform library (useLibrary), so it is not in the APK.
            optimization {
                enable = true
            }
        }
    }
}

// The Baseline Profile plugin copies release into nonMinifiedRelease (the build the generator runs
// on) but does not turn the AGP 9 optimization block off there, and a build type setting is
// overridden. Without this, the generated profile would list obfuscated names that match no other
// build. Seen with androidx.baselineprofile 1.5.0 and AGP 9.5.2; CI checks it still holds, so this
// can go once the plugin handles `optimization` itself.
androidComponents {
    beforeVariants(selector().withBuildType("nonMinifiedRelease")) { variant ->
        variant.isMinifyEnabled = false
        variant.shrinkResources = false
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:trip"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.profileinstaller)

    baselineProfile(project(":baselineprofile"))

    debugImplementation(libs.leakcanary.android)
}
