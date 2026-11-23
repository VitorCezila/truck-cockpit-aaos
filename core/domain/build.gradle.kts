plugins {
    alias(libs.plugins.truckcockpit.jvm.library)
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
