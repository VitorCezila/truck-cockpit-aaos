import com.cezila.truckcockpit.buildlogic.BuildConfig
import com.cezila.truckcockpit.buildlogic.configureKotlinJvmTarget
import com.cezila.truckcockpit.buildlogic.lib
import com.cezila.truckcockpit.buildlogic.libs
import com.cezila.truckcockpit.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Pure Kotlin/JVM module with no Android dependencies (`:core:domain`). */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("kotlin-jvm"))

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = BuildConfig.JAVA_VERSION
            targetCompatibility = BuildConfig.JAVA_VERSION
        }
        configureKotlinJvmTarget()

        dependencies {
            add("testImplementation", libs.lib("junit"))
            add("testImplementation", libs.lib("turbine"))
            add("testImplementation", libs.lib("kotlinx-coroutines-test"))
        }
    }
}
