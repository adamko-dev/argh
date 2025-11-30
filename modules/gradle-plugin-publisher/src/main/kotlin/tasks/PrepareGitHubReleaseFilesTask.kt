package dev.adamko.githubassetpublish.tasks

import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.invariantSeparatorsPathString
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.process.ExecOperations

@CacheableTask // Performs simple file operations, but it uses JavaExec every time
abstract class PrepareGitHubReleaseFilesTask
@Inject
internal constructor(
  private val execOps: ExecOperations,
) : DefaultTask() {

  /**
   * The output directory for this task.
   *
   * Will contain all files to be attached as assets to a GitHub Release.
   */
  @get:OutputDirectory
  abstract val destinationDirectory: DirectoryProperty

  /**
   * Output of the Maven Publish tasks.
   */
  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  @get:IgnoreEmptyDirectories
  abstract val stagingMavenRepo: DirectoryProperty

  @get:Input
  abstract val artifactMetadataExtensions: SetProperty<String>

  @get:Classpath
  abstract val runtimeClasspath: ConfigurableFileCollection

  @TaskAction
  protected fun taskAction() {
    prepareDestinationDirectory()

    prepare()
  }

  private fun prepareDestinationDirectory() {
    destinationDirectory.get().asFile.toPath().apply {
      deleteRecursively()
      createDirectories()
    }
  }

  private fun prepare() {
    val stagingMavenRepo: Path = stagingMavenRepo.get().asFile.toPath()
    val destinationDirectory = destinationDirectory.get().asFile.toPath()
    val artifactMetadataExtensions = artifactMetadataExtensions.get()

    val arguments = mapOf(
      "stagingMavenRepo" to stagingMavenRepo.invariantSeparatorsPathString,
      "destinationDir" to destinationDirectory.invariantSeparatorsPathString,
      "artifactMetadataExtensions" to artifactMetadataExtensions.joinToString(",")
    ).map { (key, value) -> "$key=$value" }

    execOps.javaexec { spec ->
      spec.mainClass.set("dev.adamko.githubassetpublish.lib.PrepareGitHubAssetsAction")
      spec.classpath(runtimeClasspath)
      spec.args(arguments)
    }
  }

  companion object
}
