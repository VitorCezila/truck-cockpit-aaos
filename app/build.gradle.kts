plugins {
    alias(libs.plugins.truckcockpit.android.application)
    alias(libs.plugins.truckcockpit.android.compose)
    alias(libs.plugins.truckcockpit.android.hilt)
    alias(libs.plugins.kotlin.serialization)
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
            optimization {
                enable = false
            }
        }
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

    debugImplementation(libs.leakcanary.android)
}
