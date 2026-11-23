plugins {
    alias(libs.plugins.truckcockpit.android.library)
    alias(libs.plugins.truckcockpit.android.compose)
    alias(libs.plugins.truckcockpit.android.roborazzi)
}

dependencies {
    implementation(project(":core:domain"))
}
