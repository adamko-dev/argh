package dev.adamko.githubassetpublish.lib

import dev.adamko.githubassetpublish.lib.Logger.Companion.warn
import dev.adamko.githubassetpublish.lib.internal.model.GradleModuleMetadata
import dev.adamko.githubassetpublish.lib.internal.model.GradleModuleMetadata.Companion.hasAttributes
import dev.adamko.githubassetpublish.lib.internal.model.GradleModuleMetadata.StringAttribute
import dev.adamko.githubassetpublish.lib.internal.model.MutableGradleModuleMetadata
import dev.adamko.githubassetpublish.lib.internal.model.MutableGradleModuleMetadata.Companion.saveTo
import dev.adamko.githubassetpublish.lib.utils.computeChecksum
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

/**
 * Converts files published to a local directory in a Maven layout to files that can be attached to a GitHub Release.
 *
 * Will contain all files that should be attached as assets to a GitHub Release.
 */
class PrepareGitHubAssetsAction(
  private val logger: Logger = Logger.Default,
) {

  /**
   * @param[stagingMavenRepo] Maven repo directory, in M2 layout.
   * @param[destinationDir] The output directory with the relocated and updated files.
   */
  fun run(
    stagingMavenRepo: Path,
    destinationDir: Path,
    artifactMetadataExtensions: Set<String>,
  ) {
    logger.debug(
      "Running PrepareGitHubAssetsAction." +
          "\n  stagingMavenRepo: ${stagingMavenRepo.invariantSeparatorsPathString}" +
          "\n  destinationDir: ${destinationDir.invariantSeparatorsPathString}"
    )

    val allModules = findModuleMetadataFiles(stagingMavenRepo)
      .onEach(::excludeJavadocVariants)

    val mapRootModuleToVariants = allModules
      .filter {
        it.gmm.component.url == null
      }
      .associateWith { rootModule ->
        allModules.filter { module ->
          module.isAssociatedComponentOf(rootModule)
        }.toList()
      }

    mapRootModuleToVariants.forEach { (rootModule, variants) ->
      // TODO move check to _after_ files are relocated? Or check both before and after?
      checkModules(
        root = rootModule,
        variants = variants,
      )
    }

    val mapRelocatedRootModuleToVariants =
      mapRootModuleToVariants
        .mapKeys { (rootModule) ->
          relocateModule(
            module = rootModule,
            destinationDir = destinationDir,
            artifactMetadataExtensions = artifactMetadataExtensions,
          )
        }
        .mapValues { (_, modules) ->
          modules.map { module ->
            relocateModule(
              module = module,
              destinationDir = destinationDir,
              artifactMetadataExtensions = artifactMetadataExtensions,
            )
          }
        }

    mapRelocatedRootModuleToVariants.forEach { (module, variants) ->
      updateModuleMetadata(module)
      createModuleChecksums(module)
      variants.forEach { variant ->
        updateModuleMetadata(variant)
        createModuleChecksums(variant)
      }
      createIvyModuleFile(
        metadata = module.gmm,
        destinationDir = destinationDir,
      )
    }

    logger.info("outputDir:${destinationDir.invariantSeparatorsPathString}")
  }

  private data class GradleModule(
    val gmmFile: Path,
    val pomFile: Path,
    val gmm: MutableGradleModuleMetadata,
  ) {

    /**
     * The files attached to the module's variants.
     *
     * All files must be siblings of [gmmFile].
     *
     * Use [Set] because some files might be available in multiple variants.
     */
    fun allArtifacts(): Set<Path> =
      gmm.variants.flatMap { variant ->
        variant.files.map { file ->
          gmmFile.resolveSibling(file.url)
        }
      }.toSet()
  }

  private fun checkModules(
    root: GradleModule,
    variants: List<GradleModule>,
  ) {
    val errors = mutableListOf<String>()

    val invalidGroups = variants.filter { (_, _, moduleGmm) ->
      moduleGmm.component.group != root.gmm.component.group
    }
    if (invalidGroups.isNotEmpty()) {
      errors.add("The group of all variants must be '${root.gmm.component.group}', but found: $invalidGroups")
    }
    val invalidVersions = variants.filter { (_, _, moduleGmm) ->
      moduleGmm.component.version != root.gmm.component.version
    }
    if (invalidVersions.isNotEmpty()) {
      errors.add("The version of all variants must be '${root.gmm.component.version}', but found: $invalidVersions")
    }

    variants.forEach { variant ->
      val invalidFiles = variant.allArtifacts().filter { f -> variant.gmmFile.parent != f.parent }
      if (invalidFiles.isNotEmpty()) {
        val invalidFilesStrings =
          invalidFiles.map { it.relativeTo(variant.gmmFile.parent).invariantSeparatorsPathString }
        errors.add("${variant.gmm.component.module} has artifacts in invalid location: $invalidFilesStrings")
      }
    }

    if (errors.isNotEmpty()) {
      error(errors.joinToString("\n"))
    }
  }

  /**
   * Relocate publishable files on disk to be in a single directory, [destinationDir],
   * without nesting.
   */
  private fun relocateModule(
    module: GradleModule,
    destinationDir: Path,
    artifactMetadataExtensions: Set<String>,
  ): GradleModule {
    val relocatedGmmFile = destinationDir.resolve(module.gmmFile.name)
    module.gmm.saveTo(relocatedGmmFile)

    module.allArtifacts().forEach { src ->
      val projectDir =
        Path(src.invariantSeparatorsPathString.commonPrefixWith(destinationDir.invariantSeparatorsPathString))
      logger.info(
        "relocating file: ${src.relativeTo(projectDir)} to ${
          destinationDir.resolve(src.name).relativeTo(projectDir)
        }"
      )
      src.copyTo(destinationDir.resolve(src.name), overwrite = false)


      val artifactMetadataFiles =
        artifactMetadataExtensions
          .map { ext -> src.resolveSibling("${src.name}.$ext") }

      logger.info("artifact ${src.name} has metadata files: ${artifactMetadataFiles.joinToString { it.name + if (it.isRegularFile()) "" else " (not found)" }}")

      artifactMetadataFiles.forEach { metadata ->
        if (metadata.isRegularFile()) {
          metadata.copyTo(destinationDir.resolve(metadata.name), overwrite = false)
        }
      }
    }

    val relocatedPomFile = module.pomFile.copyTo(destinationDir.resolve(module.pomFile.name))

    return GradleModule(
      gmmFile = relocatedGmmFile,
      gmm = MutableGradleModuleMetadata.loadFrom(relocatedGmmFile),
      pomFile = relocatedPomFile,
    )
  }

  private fun findModuleMetadataFiles(
    stagingMavenRepo: Path
  ): Sequence<GradleModule> =
    stagingMavenRepo.walk()
      .filter { it.isRegularFile() && it.extension == "module" }
      .mapNotNull { moduleFile ->

        val pomFile = moduleFile.run {
          resolveSibling("$nameWithoutExtension.pom")
        }

        try {
          val metadata = MutableGradleModuleMetadata.loadFrom(moduleFile)
          GradleModule(
            gmmFile = moduleFile,
            gmm = metadata,
            pomFile = pomFile,
          )
        } catch (ex: Exception) {
          logger.warn("failed to load moduleFile ${moduleFile.invariantSeparatorsPathString}", ex)
          null
        }
      }

  /**
   * There's no point in publishing `javadoc.jar`s.
   * The `sources.jar` is more useful.
   * We must avoid attaching too many files as GitHub Assets to avoid hitting the 1k file limit.
   */
  private fun excludeJavadocVariants(
    module: GradleModule
  ) {
    val before = module.gmm.variants.map { it.name }
    module.gmm.variants.removeAll { variant ->
      variant.hasAttributes(
        "org.gradle.category" to StringAttribute("documentation"),
        "org.gradle.docstype" to StringAttribute("javadoc"),
      )
    }
    val after = module.gmm.variants.map { it.name }.toSet()

    if (before.size != after.size) {
      println("Removed variants from ${module.gmmFile.name}: ${before - after}")
    } else {
      println("No variants removed from ${module.gmmFile.name}. $before")
    }
  }

  /**
   * Gradle is hardcoded to publish modules to a Maven layout,
   * but we relocate all files to be in the same directory.
   * So, we must update the GMM to remove the relative paths
   * of artifacts attached to the module.
   */
  private fun updateModuleMetadata(
    module: GradleModule,
  ) {
    if (module.gmm.component.url?.startsWith("../../") == true) {
      module.gmm.component.url = module.gmm.component.url?.substringAfterLast("/")
    }

    module.gmm.variants.forEach { variant ->
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

    module.gmm.saveTo(module.gmmFile)
  }

  /**
   * Create a dummy Ivy file (otherwise Gradle can't find the Module Metadata file).
   * Workaround for https://github.com/gradle/gradle/issues/33674
   *
   * Only one Ivy file is required, pointing to the root module.
   * (i.e. KMP libraries have multiple secondary variant modules, but only one root module.)
   */
  private fun createIvyModuleFile(
    metadata: GradleModuleMetadata,
    destinationDir: Path,
  ) {
    val rootModuleName = metadata.component.module + "-" + metadata.component.version

    val ivyModuleFile = destinationDir.resolve("$rootModuleName.ivy.xml")
    ivyModuleFile.writeText(
      // language=xml
      """
        |<?xml version="1.0"?>
        |<ivy-module version="2.0"
        |            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        |            xsi:noNamespaceSchemaLocation="https://ant.apache.org/ivy/schemas/ivy.xsd">
        |    <!-- do_not_remove: published-with-gradle-metadata -->
        |    <info organisation="${metadata.component.group}" module="${metadata.component.module}" revision="${metadata.component.version}" />
        |</ivy-module>
        |""".trimMargin(),
      options = arrayOf(StandardOpenOption.CREATE_NEW),
    )

    ivyModuleFile.createShaChecksumFile("256")
  }

  private fun createModuleChecksums(module: GradleModule) {
    module.gmmFile.createShaChecksumFile("256")
    module.pomFile.createShaChecksumFile("256")
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      fun getArg(name: String): String =
        args.firstOrNull { it.startsWith("$name=") }
          ?.substringAfter("$name=")
          ?: error("missing required argument '$name'")

      val stagingMavenRepo = Path(getArg("stagingMavenRepo"))
      val destinationDir = Path(getArg("destinationDir"))
      val artifactMetadataExtensions = getArg("artifactMetadataExtensions")
        .split(",")
        .map(String::trim)
        .filter(String::isNotBlank)
        .toSet()

      val action = PrepareGitHubAssetsAction()

      action.run(
        stagingMavenRepo = stagingMavenRepo,
        destinationDir = destinationDir,
        artifactMetadataExtensions = artifactMetadataExtensions,
      )
    }

    private fun GradleModule.isAssociatedComponentOf(rootModule: GradleModule): Boolean {
      return gmm.component.url != null &&
          gmm.component.group == rootModule.gmm.component.group &&
          gmm.component.module == rootModule.gmm.component.module &&
          gmm.component.version == rootModule.gmm.component.version
    }

    private fun Path.createShaChecksumFile(bits: String) {
      val checksum = computeChecksum("SHA-$bits")
      resolveSibling("$name.sha$bits")
        .writeText(checksum, options = arrayOf(StandardOpenOption.CREATE_NEW))
    }
  }
}
