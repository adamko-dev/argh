@file:OptIn(ExperimentalSerializationApi::class)

package dev.adamko.githubassetpublish.tasks

import dev.adamko.githubassetpublish.internal.computeChecksum
import dev.adamko.githubassetpublish.internal.model.GradleModuleMetadata
import dev.adamko.githubassetpublish.internal.model.MutableGradleModuleMetadata
import java.io.File
import javax.inject.Inject
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.*

abstract class PrepareGitHubReleaseFilesTask
@Inject
internal constructor(
  private val fs: FileSystemOperations,
) : DefaultTask() {

  @get:OutputDirectory
  abstract val destinationDirectory: DirectoryProperty

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val buildDirMavenDirectory: DirectoryProperty

  @TaskAction
  protected fun taskAction() {
    val repoDir = buildDirMavenDirectory.get().asFile
    val destinationDir = destinationDirectory.get().asFile

    logger.info("[$path] processing buildDirMavenRepo: $repoDir")

    syncFiles(
      sourceDir = repoDir,
      destinationDir = destinationDir,
    )

    val syncedModuleMetadataFiles = destinationDir.moduleMetadataFiles()
    updateGradleModuleMetadata(syncedModuleMetadataFiles)
    createChecksumFiles(syncedModuleMetadataFiles)

    logger.lifecycle("[$path] outputDir:${buildDirMavenDirectory.get().asFile.invariantSeparatorsPath}")
  }

  private fun File.moduleMetadataFiles(): List<File> {
    return walk()
      .filter { it.isFile && it.extension == "module" }
      .filter { moduleFile ->
        try {
          moduleFile.inputStream().use { source ->
            json.decodeFromStream(MutableGradleModuleMetadata.serializer(), source)
          }
          true
        } catch (ex: Exception) {
          logger.warn("[$path] Failed to decode Gradle Module Metadata file ${moduleFile.invariantSeparatorsPath}, $ex")
          false
        }
      }
      .toList()
  }

  private fun syncFiles(
    sourceDir: File,
    destinationDir: File,
  ) {
    fs.sync {
      into(destinationDir)
      include(
        "**/*.jar",
        "**/*.module",
      )
      exclude(
        "**/*.md5",
        "**/*.sha1",
        "**/*.sha256",
        "**/*.sha512",
        "**/maven-metadata.xml",
        "**/*.pom",
      )
      includeEmptyDirs = false

      sourceDir.moduleMetadataFiles().forEach { moduleFile ->
        val moduleMetadata = moduleFile.inputStream().use { stream ->
          json.decodeFromStream(GradleModuleMetadata.serializer(), stream)
        }
        val moduleVersion = moduleMetadata.component.version
        val moduleName = moduleMetadata.component.module

        from(moduleFile.parentFile)

        val snapshotVersion = moduleFile.nameWithoutExtension
          .substringAfter("$moduleName-", "")

        val isSnapshot = moduleVersion.endsWith("-SNAPSHOT")
            && snapshotVersion != moduleVersion

        // Update filenames:
        // - If a snapshot version, replace the timestamp with 'SNAPSHOT'.
        // - Remove directories (can't attach directories to GitHub Release).
        eachFile {

          val newFileName = if (isSnapshot) {
            sourceName.replace("-$snapshotVersion", "-${moduleVersion}")
          } else {
            sourceName
          }

          relativePath = RelativePath(true, newFileName)
        }
      }
    }
  }

  private fun updateGradleModuleMetadata(
    moduleMetadataFiles: List<File>,
  ) {
    moduleMetadataFiles.forEach { moduleFile ->
      val moduleMetadata = moduleFile.inputStream().use { source ->
        json.decodeFromStream(MutableGradleModuleMetadata.serializer(), source)
      }
      if (moduleMetadata.component.url?.startsWith("../../") == true) {
        moduleMetadata.component.url = moduleMetadata.component.url?.substringAfterLast("/")
      }

      moduleMetadata.variants.forEach { variant ->
        variant.availableAt?.let { availableAt ->
          if (availableAt.url.startsWith("../../")) {
            availableAt.url = availableAt.url.substringAfterLast("/")
          }
        }
      }
      moduleFile.outputStream().use { sink ->
        json.encodeToStream(MutableGradleModuleMetadata.serializer(), moduleMetadata, sink)
      }
    }
  }

  private fun createChecksumFiles(
    moduleMetadataFiles: List<File>,
  ) {
    moduleMetadataFiles.forEach { file ->
      setOf(
        "256",
        "512",
      ).forEach {
        val checksum = file.computeChecksum("SHA-$it")
        file.resolveSibling(file.name + ".sha$it").writeText(checksum)
      }
    }
  }

  companion object {
    private val json: Json =
      Json {
        prettyPrint = true
        prettyPrintIndent = "  "
      }
  }
}
