import com.android.build.api.dsl.ApplicationExtension
import com.cezila.truckcockpit.buildlogic.BuildConfig
import com.cezila.truckcockpit.buildlogic.configureKotlinJvmTarget
import com.cezila.truckcockpit.buildlogic.libs
import com.cezila.truckcockpit.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("android-application"))

        extensions.configure<ApplicationExtension> {
            compileSdk {
                version = release(BuildConfig.COMPILE_SDK) { minorApiLevel = BuildConfig.COMPILE_SDK_MINOR }
            }
            defaultConfig {
                minSdk = BuildConfig.MIN_SDK
                targetSdk = BuildConfig.TARGET_SDK
            }
            compileOptions {
                sourceCompatibility = BuildConfig.JAVA_VERSION
                targetCompatibility = BuildConfig.JAVA_VERSION
            }
        }
        configureKotlinJvmTarget()
    }
}
