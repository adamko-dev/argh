package dev.adamko.githubassetpublish.tasks

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.options.Option
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Performs remote operations")
abstract class UploadGitHubReleaseAssetsTask
@Inject
internal constructor(
  private val execOps: ExecOperations,
) : DefaultTask() {

  @get:Input
  abstract val githubRepo: Property<String>

  @get:Input
  abstract val createNewReleaseIfMissing: Property<Boolean>

  @get:Classpath
  abstract val runtimeClasspath: ConfigurableFileCollection

  /**
   * Output of the Maven Publish tasks.
   */
  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  @get:IgnoreEmptyDirectories
  abstract val preparedAssetsDir: DirectoryProperty

  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  @get:IgnoreEmptyDirectories
  abstract val pluginCacheDir: DirectoryProperty

  @get:Input
  @get:Option(option = "skipGitHubUpload", description = "Skip uploading to GitHub, just prepares the files.")
  abstract val skipGitHubUpload: Property<Boolean>

  @get:Input
  abstract val releaseVersion: Property<String>

  @TaskAction
  protected fun taskAction() {
    preflightChecks()

    upload()
  }

  private fun preflightChecks() {
    // - check if publication `group` can be mapped to `github-org/github-repo-name`
    // - check if gh cli is installed
    // - check if gh cli is authenticated
    // - check if gh cli has access to GitHub repo
    // - check the release version is valid and consistent
    //   across all GMM module files
    // - check the release group is valid and consistent
    //   across all GMM module files
    // - check if repo already has release
    // - check if preparedFiles does not contain subdirectories
  }

  private fun upload() {
    if (skipGitHubUpload.get()) {
      logger.lifecycle("$path skipping upload")
      return
    }
    execOps.javaexec { spec ->
      spec.mainClass.set("dev.adamko.githubassetpublish.lib.Uploader")
      spec.classpath(runtimeClasspath.files)
      spec.args(
        buildList {
          add("githubRepo=${githubRepo.get()}")
          add("releaseVersion=${releaseVersion.get()}")
          add("createNewReleaseIfMissing=${createNewReleaseIfMissing.get()}")
          add("releaseDir=${preparedAssetsDir.get().asFile.invariantSeparatorsPath}")
          add("pluginCacheDir=${pluginCacheDir.get().asFile.invariantSeparatorsPath}")
        }
      )
    }
  }

  companion object
}
