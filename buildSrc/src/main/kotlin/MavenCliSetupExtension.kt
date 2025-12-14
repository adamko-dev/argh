package buildsrc

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

abstract class MavenCliSetupExtension {

  /**
   * The version of `org.apache.maven` dependencies..
   */
  abstract val mavenVersion: Property<String>

  /**
   * The version of `org.apache.maven.plugin-tools` dependencies.
   */
  abstract val mavenPluginToolsVersion: Property<String>

  /** Directory that will contain the unpacked Apache Maven dependency */
  abstract val mavenInstallDir: DirectoryProperty

  /**
   * Path to the Maven executable.
   *
   * This should be different per OS:
   *
   * * Windows: `$mavenInstallDir/bin/mvn.cmd`
   * * Unix: `$mavenInstallDir/bin/mvn`
   */
  abstract val mvn: RegularFileProperty

  companion object {
    const val MAVEN_PLUGIN_TASK_GROUP = "maven plugin"
  }
}
