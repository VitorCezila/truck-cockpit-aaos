plugins {
    alias(libs.plugins.truckcockpit.android.feature)
}

dependencies {
    testImplementation(testFixtures(project(":core:domain")))
}
