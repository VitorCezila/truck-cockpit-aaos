import com.android.build.api.dsl.LibraryExtension
import com.cezila.truckcockpit.buildlogic.BuildConfig
import com.cezila.truckcockpit.buildlogic.configureKotlinJvmTarget
import com.cezila.truckcockpit.buildlogic.defaultNamespace
import com.cezila.truckcockpit.buildlogic.lib
import com.cezila.truckcockpit.buildlogic.libs
import com.cezila.truckcockpit.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("android-library"))

        extensions.configure<LibraryExtension> {
            namespace = defaultNamespace
            compileSdk {
                version = release(BuildConfig.COMPILE_SDK) { minorApiLevel = BuildConfig.COMPILE_SDK_MINOR }
            }
            defaultConfig {
                minSdk = BuildConfig.MIN_SDK
            }
            compileOptions {
                sourceCompatibility = BuildConfig.JAVA_VERSION
                targetCompatibility = BuildConfig.JAVA_VERSION
            }
        }
        configureKotlinJvmTarget()

        dependencies {
            add("testImplementation", libs.lib("junit"))
            add("testImplementation", libs.lib("turbine"))
            add("testImplementation", libs.lib("kotlinx-coroutines-test"))
        }
    }
}
