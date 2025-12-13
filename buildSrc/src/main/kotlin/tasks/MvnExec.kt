package buildsrc.tasks

import java.util.*
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.process.ExecOperations
import org.gradle.work.NormalizeLineEndings

/**
 * Runs a Maven task.
 *
 * See `maven-cli-setup.gradle.kts` for details on the Maven CLI installation.
 */
@CacheableTask
abstract class MvnExec
@Inject
constructor(
  private val exec: ExecOperations,
  private val fs: FileSystemOperations,
  private val javaToolchains: JavaToolchainService,
) : DefaultTask() {

  /**
   * Work directory.
   *
   * Be aware that any existing content will be replaced by [filteredClasses] and [resources].
   */
  @get:OutputDirectory
  abstract val workDirectory: DirectoryProperty

  /** Input classes - will be synced to [workDirectory]. */
  @get:Internal
  abstract val classes: ConfigurableFileCollection

  @get:Classpath
  protected val filteredClasses: FileCollection
    get() = classes.asFileTree.matching { include("**/*.class") }

  /** Input resource files - will be synced to [workDirectory]. */
  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  @get:NormalizeLineEndings
  abstract val resources: ConfigurableFileCollection

  @get:InputFile
  @get:PathSensitive(NONE)
  abstract val mvnCli: RegularFileProperty

  /**
   * Optional Maven `settings.xml` file.
   *
   * When `mvn` is invoked, it will be set using the `--settings` argument.
   */
  @get:InputFile
  @get:PathSensitive(NONE)
  @get:NormalizeLineEndings
  @get:Optional
  abstract val settingsXml: RegularFileProperty

  @get:Input
  abstract val arguments: ListProperty<String>

  /** `-e` - Produce execution error messages. */
  @get:Input
  @get:Optional
  abstract val showErrors: Property<Boolean>

  /** `-B` - Run in non-interactive (batch) mode. */
  @get:Input
  @get:Optional
  abstract val batchMode: Property<Boolean>

  @get:Input
  @get:Optional
  abstract val javaVersion: Property<JavaLanguageVersion>

  @TaskAction
  fun exec() {
    fs.sync {
      from(filteredClasses) {
        into("classes/java/main")
      }
      from(resources)
      into(workDirectory)
    }

    val arguments = buildList {
      addAll(arguments.get())
      if (showErrors.orNull == true) add("--errors")
      if (batchMode.orNull == true) add("--batch-mode")
      if (settingsXml.orNull?.asFile?.exists() == true) {
        add("--settings")
        add(settingsXml.get().asFile.invariantSeparatorsPath)
      }
    }

    val javaToolchain: JavaLauncher? =
      javaVersion.orNull?.let { javaVersion ->
        javaToolchains.launcherFor { languageVersion.set(javaVersion) }.get()
      }

    exec.exec {
      workingDir(workDirectory)
      executable(mvnCli.get())
      args(arguments)
      javaToolchain?.metadata?.installationPath?.asFile?.invariantSeparatorsPath?.let {
        environment("JAVA_HOME", it)
      }
    }

    makePropertiesFilesReproducible()
  }

  /**
   * Remove non-reproducible timestamps from any generated [Properties] files.
   */
  private fun makePropertiesFilesReproducible() {
    workDirectory.get().asFile.walk()
      .filter { it.isFile && it.extension == "properties" }
      .forEach { file ->
        logger.info("[MvnExec $path] removing timestamp from $file")
        // drop the last comment - java.util.Properties always adds a timestamp, which is not-reproducible.
        val comments = file.readLines()
          .takeWhile { it.startsWith('#') }
          .dropLast(1)

        val properties = file.readLines().dropWhile { it.startsWith('#') }

        val updatedProperties = (comments + properties).joinToString("\n")
        file.writeText(updatedProperties)
      }
  }
}
