plugins {
    alias(libs.plugins.truckcockpit.android.library)
    alias(libs.plugins.truckcockpit.android.hilt)
}

android {
    useLibrary("android.car")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(testFixtures(project(":core:domain")))
}
