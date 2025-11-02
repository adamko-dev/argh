package dev.adamko.githubassetpublish.lib

import dev.adamko.githubassetpublish.lib.Logger.Companion.warn
import dev.adamko.githubassetpublish.lib.internal.computeChecksum
import dev.adamko.githubassetpublish.lib.internal.model.GradleModuleMetadata
import dev.adamko.githubassetpublish.lib.internal.model.MutableGradleModuleMetadata
import dev.adamko.githubassetpublish.lib.internal.model.MutableGradleModuleMetadata.Companion.saveTo
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Converts files published to a local directory in a Maven layout to files that can be attached to a GitHub Release.
 *
 * @param[sourceMavenRepositoryDir] Output of the Maven Publish task.
 * @param[destinationDir] The output directory for this task.
 * Will contain all files that should be attached as assets to a GitHub Release.
 */
class PrepareGitHubAssetsAction(
  private val sourceMavenRepositoryDir: Path,
  private val destinationDir: Path,
  private val logger: Logger = Logger.Default,
) {

  fun run() {
    logger.debug(
      "Running PrepareGitHubAssetsAction." +
          "\n  sourceMavenRepositoryDir: ${sourceMavenRepositoryDir.invariantSeparatorsPathString}" +
          "\n  destinationDir: $destinationDir"
    )

    relocateFiles(
      sourceDir = sourceMavenRepositoryDir,
      destinationDir = destinationDir,
    )

    updateRelocatedFiles(destinationDir)

    logger.info("outputDir:${destinationDir.invariantSeparatorsPathString}")
  }

  /**
   * Relocate publishable files on disk to be in a single directory,
   * without nesting.
   */
  private fun relocateFiles(
    sourceDir: Path,
    destinationDir: Path,
  ) {
    val publishableFileExtensions = setOf("jar", "module", "klib")

    sourceDir.walk()
      .filter { file ->
        file.isRegularFile()
            && publishableFileExtensions.any { validExt -> file.name.endsWith(".$validExt") }
      }
      .forEach { src ->
        logger.info("relocating file: $src to ${destinationDir.resolve(src.name)}")
        src.moveTo(destinationDir.resolve(src.name))
      }
  }

  private fun updateRelocatedFiles(
    destinationDir: Path,
  ) {
    destinationDir.findModuleMetadataFiles().forEach { (moduleFile, metadata) ->
      updateModuleMetadata(moduleFile, metadata)
      createIvyModuleFile(moduleFile, metadata)
      createModuleChecksums(moduleFile)
    }
  }

  private fun Path.findModuleMetadataFiles(): Sequence<Pair<Path, MutableGradleModuleMetadata>> =
    walk()
      .filter { it.isRegularFile() && it.extension == "module" }
      .mapNotNull { moduleFile ->
        try {
          val metadata = MutableGradleModuleMetadata.loadFrom(moduleFile)
          moduleFile to metadata
        } catch (ex: Exception) {
          logger.warn("failed to load moduleFile ${moduleFile.invariantSeparatorsPathString}", ex)
          null
        }
      }

  private fun updateModuleMetadata(
    moduleFile: Path,
    metadata: MutableGradleModuleMetadata,
  ) {
    // Gradle is hardcoded to publish modules to a Maven layout,
    // but we relocate all files to be in the same directory.
    // So, we must update the GMM to remove the relative paths.
    if (metadata.component.url?.startsWith("../../") == true) {
      metadata.component.url = metadata.component.url?.substringAfterLast("/")
    }

    metadata.variants.forEach { variant ->
      variant.files.forEach { file ->
        if (file.url.startsWith("../../")) {
          file.url = file.url.substringAfterLast("/")
        }
      }

      variant.availableAt?.let { aa ->
        if (aa.url.startsWith("../../")) {
          aa.url = aa.url.substringAfterLast("/")
        }
      }
    }

    metadata.saveTo(moduleFile)
  }

  private fun createIvyModuleFile(
    moduleFile: Path,
    metadata: GradleModuleMetadata,
  ) {
    // Create a dummy Ivy file (otherwise Gradle can't find the Module Metadata file).
    // Only one Ivy file is required, pointing to the root module.
    // (i.e. KMP libraries have multiple modules, but only one root module.)
    val rootModuleName = metadata.component.module + "-" + metadata.component.version
    if (moduleFile.nameWithoutExtension == rootModuleName) {
      moduleFile
        .resolveSibling("$rootModuleName.ivy.xml")
        .writeText(
          // language=xml
          """
          |<?xml version="1.0"?>
          |<ivy-module version="2.0"
          |            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          |            xsi:noNamespaceSchemaLocation="https://ant.apache.org/ivy/schemas/ivy.xsd">
          |    <!-- do_not_remove: published-with-gradle-metadata -->
          |    <info organisation="${metadata.component.group}" module="${metadata.component.module}" revision="${metadata.component.version}" />
          |</ivy-module>
          |""".trimMargin()
        )
    }
  }

  private fun createModuleChecksums(
    moduleFile: Path,
  ) {
    setOf(
      "256",
      "512",
    ).forEach {
      val checksum = moduleFile.computeChecksum("SHA-$it")
      moduleFile.resolveSibling(moduleFile.name + ".sha$it").writeText(checksum)
    }
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      fun getArg(name: String): String =
        args.firstOrNull { it.startsWith("$name=") }
          ?.substringAfter("$name=")
          ?: error("missing required argument '$name'")

      val sourceMavenRepositoryDir = Path(getArg("sourceMavenRepositoryDir"))
      val destinationDir = Path(getArg("destinationDir"))

      val action = PrepareGitHubAssetsAction(
        sourceMavenRepositoryDir = sourceMavenRepositoryDir,
        destinationDir = destinationDir,
      )

      action.run()
    }
  }
}
