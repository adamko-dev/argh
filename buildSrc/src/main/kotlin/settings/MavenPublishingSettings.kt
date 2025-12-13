package buildsrc.settings

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.*


/**
 * Settings for the [buildsrc.conventions.MavenPublishTestPlugin] convention plugin.
 */
abstract class MavenPublishingSettings @Inject constructor(
  private val project: Project,
  private val providers: ProviderFactory,
) {
  abstract val pomName: Property<String>
  abstract val pomDescription: Property<String>

  val isReleaseVersion: Provider<Boolean> =
    providers.provider { !project.version.toString().endsWith("-SNAPSHOT") }

  val mavenCentralUsername: Provider<String> =
    arghProp("mavenCentralUsername")
      .orElse(providers.environmentVariable("MAVEN_SONATYPE_USERNAME"))
  val mavenCentralPassword: Provider<String> =
    arghProp("mavenCentralPassword")
      .orElse(providers.environmentVariable("MAVEN_SONATYPE_PASSWORD"))


  val adamkoDevReleaseUrl: Provider<String> =
    isReleaseVersion.map { isRelease ->
      if (isRelease) {
        "https://europe-west4-maven.pkg.dev/adamko-dev/adamko-dev-releases"
      } else {
        "https://europe-west4-maven.pkg.dev/adamko-dev/adamko-dev-snapshots"
      }
    }
  val adamkoDevUsername: Provider<String> =
    arghProp("adamkoDevUsername")
      .orElse(providers.environmentVariable("MAVEN_ADAMKO_DEV_USERNAME"))
  val adamkoDevPassword: Provider<String> =
    arghProp("adamkoDevPassword")
      .orElse(providers.environmentVariable("MAVEN_ADAMKO_DEV_PASSWORD"))


  val signingKeyId: Provider<String> =
    arghProp("signing.keyId")
      .orElse(providers.environmentVariable("MAVEN_SONATYPE_SIGNING_KEY_ID"))
  val signingKey: Provider<String> =
    arghProp("signing.key")
      .orElse(providers.environmentVariable("MAVEN_SONATYPE_SIGNING_KEY"))
  val signingPassword: Provider<String> =
    arghProp("signing.password")
      .orElse(providers.environmentVariable("MAVEN_SONATYPE_SIGNING_PASSWORD"))


  private fun arghProp(name: String): Provider<String> =
    providers.gradleProperty("dev.adamko.argh.$name")

  private fun <T : Any> arghProp(name: String, convert: (String) -> T): Provider<T> =
    arghProp(name).map(convert)

  companion object {
    const val EXTENSION_NAME = "mavenPublishing"

    /** Retrieve the [MavenPublishingSettings] extension. */
    internal val Project.mavenPublishing: MavenPublishingSettings
      get() = extensions.getByType()

    /** Configure the [MavenPublishingSettings] extension. */
    internal fun Project.mavenPublishing(configure: MavenPublishingSettings.() -> Unit) =
      extensions.configure(configure)
  }
}
