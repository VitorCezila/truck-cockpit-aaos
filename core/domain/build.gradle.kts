plugins {
    alias(libs.plugins.truckcockpit.jvm.library)
    `java-test-fixtures`
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    // Shared test helpers (FakeVehicleDataSource, MainDispatcherRule) for every module's tests.
    testFixturesApi(libs.kotlinx.coroutines.test)
    testFixturesApi(libs.junit)
}
