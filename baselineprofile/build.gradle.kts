plugins {
    alias(libs.plugins.truckcockpit.android.test)
    alias(libs.plugins.baselineprofile)
}

android {
    targetProjectPath = ":app"

    defaultConfig {
        // AAOS only runs on an emulator here; its numbers are for before/after comparison only.
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }
}

// Runs on the connected AAOS emulator: there is no automotive Gradle Managed Device image.
baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
