package com.cezila.truckcockpit.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

object BuildConfig {
    const val COMPILE_SDK = 37

    // Compose 1.13 (BOM 2026.11.01) requires compileSdk 37.1 or later.
    const val COMPILE_SDK_MINOR = 2
    const val MIN_SDK = 33
    const val TARGET_SDK = 37
    val JAVA_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JvmTarget.JVM_17
}

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).get()

fun VersionCatalog.pluginId(alias: String): String = findPlugin(alias).get().get().pluginId

/** `:core:ui` -> `com.cezila.truckcockpit.core.ui` */
val Project.defaultNamespace: String
    get() = "com.cezila.truckcockpit" + path.replace(':', '.')

internal fun Project.configureKotlinJvmTarget() {
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(BuildConfig.JVM_TARGET)
    }
}
