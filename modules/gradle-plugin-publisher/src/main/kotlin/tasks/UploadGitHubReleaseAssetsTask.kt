package dev.adamko.githubassetpublish.tasks

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.NONE
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
  abstract val version: Property<String>

  @get:Input
  abstract val createNewReleaseIfMissing: Property<Boolean>

  @get:InputDirectory
  @get:PathSensitive(NONE)
  abstract val releaseDir: DirectoryProperty

  @get:Classpath
  abstract val runtimeClasspath: ConfigurableFileCollection

  @TaskAction
  protected fun taskAction() {
    execOps.javaexec { spec ->
      spec.mainClass.set("dev.adamko.githubassetpublish.lib.upload.Uploader")
      spec.classpath(runtimeClasspath.files)
      spec.args(
        buildList {
          add("githubRepo=${githubRepo.get()}")
          add("version=${version.get()}")
          add("createNewReleaseIfMissing=${createNewReleaseIfMissing.get()}")
          add("releaseDir=${releaseDir.get().asFile.absolutePath}")
        }
      )
    }
  }

  companion object
}
