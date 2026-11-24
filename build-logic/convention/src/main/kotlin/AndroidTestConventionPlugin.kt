import com.android.build.api.dsl.TestExtension
import com.cezila.truckcockpit.buildlogic.BuildConfig
import com.cezila.truckcockpit.buildlogic.configureKotlinJvmTarget
import com.cezila.truckcockpit.buildlogic.defaultNamespace
import com.cezila.truckcockpit.buildlogic.libs
import com.cezila.truckcockpit.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** `com.android.test` modules (e.g. `:baselineprofile`) with the same SDK and Java setup as the app. */
class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("android-test"))

        extensions.configure<TestExtension> {
            namespace = defaultNamespace
            compileSdk {
                version = release(BuildConfig.COMPILE_SDK) { minorApiLevel = BuildConfig.COMPILE_SDK_MINOR }
            }
            defaultConfig {
                minSdk = BuildConfig.MIN_SDK
                targetSdk = BuildConfig.TARGET_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = BuildConfig.JAVA_VERSION
                targetCompatibility = BuildConfig.JAVA_VERSION
            }
        }
        configureKotlinJvmTarget()
    }
}
