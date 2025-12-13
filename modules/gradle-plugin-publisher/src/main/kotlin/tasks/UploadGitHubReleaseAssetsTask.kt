package dev.adamko.argh.gradle.publisher.tasks

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
    // TODO
    //   - Check number of files is <1000 (GitHub has a limit of 1k files)
    //     (Note: must also count checksum files and `.asc` files)
    //   - Check all files are under 2GB (GitHub limits each asset to under 2GB)
    //   - Check if publication `group` can be mapped to `github-org/github-repo-name`.
    //   - Check gh api token is valid and has `repo` scope
    //   - Check the release version is valid and consistent across all GMM module files.
    //   - Check the release group is valid and consistent across all GMM module files.
    //   - Check if repo already has release.
    //   - Check if preparedFiles does not contain subdirectories.
    //   - Check if GitHub token is present and valid,
    //     otherwise, direct user to run 'init GitHub auth token' task, or set GITHUB_TOKEN env var.
  }

  private fun upload() {
    if (skipGitHubUpload.get()) {
      logger.lifecycle("$path skipping upload")
      return
    }
    execOps.javaexec { spec ->
      spec.mainClass.set("dev.adamko.argh.lib.uploader.Uploader")
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
