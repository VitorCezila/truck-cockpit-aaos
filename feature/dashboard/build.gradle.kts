plugins {
    alias(libs.plugins.truckcockpit.android.feature)
}

// The module has no tests yet. Remove this when the Dashboard tests land (#2).
tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}
