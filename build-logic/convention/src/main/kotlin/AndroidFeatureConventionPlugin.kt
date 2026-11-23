import com.cezila.truckcockpit.buildlogic.lib
import com.cezila.truckcockpit.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * A `:feature:*` module: Android library + Compose + Hilt + Roborazzi.
 * Depends only on `:core:domain` and `:core:ui`.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("truckcockpit.android.library")
        pluginManager.apply("truckcockpit.android.compose")
        pluginManager.apply("truckcockpit.android.hilt")
        pluginManager.apply("truckcockpit.android.roborazzi")

        dependencies {
            add("implementation", project(":core:domain"))
            add("implementation", project(":core:ui"))
            add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
            add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
            add("implementation", libs.lib("androidx-hilt-lifecycle-viewmodel-compose"))
            add("implementation", libs.lib("kotlinx-coroutines-core"))
        }
    }
}
