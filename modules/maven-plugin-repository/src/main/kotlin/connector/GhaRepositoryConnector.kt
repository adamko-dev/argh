package dev.adamko.githubassetpublish.maven.connector

import dev.adamko.githubassetpublish.lib.internal.model.GradleModuleMetadata
import dev.adamko.githubassetpublish.maven.layout.GhaRepositoryLayout
import dev.adamko.githubassetpublish.maven.metadata.convertToPomXml
import dev.adamko.githubassetpublish.maven.modify
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.*
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.spi.connector.*
import org.eclipse.aether.spi.connector.checksum.ChecksumAlgorithmFactorySelector
import org.eclipse.aether.spi.connector.layout.RepositoryLayout
import org.eclipse.aether.transfer.ArtifactNotFoundException
import org.eclipse.aether.transfer.ArtifactTransferException
import org.eclipse.aether.transfer.TransferEvent
import org.eclipse.aether.transfer.TransferResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class GhaRepositoryConnector(
  private val session: RepositorySystemSession,
  private val repository: RemoteRepository,
  private val checksumAlgorithmFactorySelector: ChecksumAlgorithmFactorySelector
) : RepositoryConnector {
  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  private val layout: RepositoryLayout = GhaRepositoryLayout(
//    "gh-assets"
    checksumAlgorithmFactorySelector = checksumAlgorithmFactorySelector,
  )

  private val client: HttpClient = HttpClient.newBuilder()
    .followRedirects(HttpClient.Redirect.NORMAL)
    .build()

  override fun get(
    artifactDownloads: Collection<ArtifactDownload>?,
    metadataDownloads: Collection<MetadataDownload>?,
  ) {
    logger.warn("GhaRepositoryConnector get")
    artifactDownloads?.forEach { download ->
      if (download.artifact.extension == "pom") {
        downloadPom(download)
      } else {
        downloadArtifact(download)
      }
    }
    metadataDownloads?.forEach { metadataDownload ->
      logger.warn("GhaRepositoryConnector get metadata: $metadataDownload")
    }
  }

  override fun put(
    artifactUploads: Collection<ArtifactUpload>?,
    metadataUploads: Collection<MetadataUpload>?,
  ) {
    logger.warn("GhaRepositoryConnector put")
    artifactUploads?.forEach { artifactUpload ->
      logger.warn("GhaRepositoryConnector put artifact: $artifactUpload")
    }
    metadataUploads?.forEach { metadataUpload ->
      logger.warn("GhaRepositoryConnector put metadata: $metadataUpload")
    }
  }

  private fun downloadPom(download: ArtifactDownload) {

    val pomDest = download.file.toPath()
    val moduleDest: Path =
      pomDest.resolveSibling(pomDest.name.removeSuffix(".pom") + ".module")

    val moduleUri = layout.getLocation(download.artifact, false)
      .toString()
      .removeSuffix(".pom")
      .plus(".module")
      .let(::URI)

    downloadGradleMetadata(
      download = download,
      moduleUri = moduleUri,
      moduleDest = moduleDest,
    )

    validateGradleMetadataChecksum(
      module = moduleDest,
      moduleUri = moduleUri,
    )

    convertGradleMetadataToPom(
      pom = pomDest,
      module = moduleDest,
    )
  }

  private fun downloadGradleMetadata(
    download: ArtifactDownload,
    moduleUri: URI,
    moduleDest: Path,
  ) {
    val transferEvent = TransferEvent.Builder(
      session,
      TransferResource(
        repository.id,
        repository.url,
        download.file.getName(),
        download.file,
        download.trace,
      )
    ).build()

    download.listener.transferInitiated(transferEvent)

    try {
      download.listener.transferStarted(transferEvent)

      download(moduleUri, moduleDest)

      download.listener.transferSucceeded(transferEvent)
    } catch (e: Exception) {
      download.listener.transferFailed(transferEvent)
      download.setException(
        ArtifactNotFoundException(
          download.artifact,
          repository,
          "Error transferring artifact",
          e,
        )
      )
    }
  }

  private fun validateGradleMetadataChecksum(
    module: Path,
    moduleUri: URI,
  ) {
    val sha512 = downloadGradleMetadataChecksum(module, moduleUri, "sha512")
    val sha256 = downloadGradleMetadataChecksum(module, moduleUri, "sha256")


  }

  private fun downloadGradleMetadataChecksum(
    module: Path,
    moduleUri: URI,
    checksum: String,
  ): String {
    val checksumUri = moduleUri.modify {
      path = "$path.$checksum"
    }
    val checksumFile = module.resolveSibling(module.name + ".$checksum")
    download(checksumUri, checksumFile)
    return checksumFile.readText().trim()
  }

  private fun convertGradleMetadataToPom(
    pom: Path,
    module: Path
  ) {
    val gmm = GradleModuleMetadata.loadFrom(module)
    pom.writeText(gmm.convertToPomXml())
  }

  private fun downloadArtifact(download: ArtifactDownload) {
    val transferEvent = TransferEvent.Builder(
      session,
      TransferResource(
        repository.id,
        repository.url,
        download.file.getName(),
        download.file,
        download.trace
      )
    ).build()
    download.listener.transferInitiated(transferEvent)

    val dest: Path = download.file.toPath()

    val sourceUri = layout.getLocation(download.artifact, false)

//    download.listener.transferStarted(transferEvent);

    try {
      download.listener.transferStarted(transferEvent)
      download(sourceUri, dest)

//      for (checksum in layout.getChecksumLocations(download.artifact, false, sourceUri)) {
////        val ext = checksum.checksumAlgorithmFactory.fileExtension
////        val checksumPath = dest.parent.resolve(dest.fileName.toString() + "." + ext)
////        download(sourceUri.resolve(checksum.location), checksumPath)
//
////        verifyChecksum(dest, checksumPath, checksum.checksumAlgorithmFactory)
//        download.listener.transferSucceeded(transferEvent)
//      }
      download.listener.transferSucceeded(transferEvent)
    } catch (e: FileNotFoundException) {
      download.listener.transferFailed(transferEvent)
      download.setException(
        ArtifactNotFoundException(
          download.artifact,
          repository,
          "Artifact not found in repository",
          e
        )
      )
    } catch (e: Exception) {
      download.listener.transferFailed(transferEvent)
      download.setException(
        ArtifactTransferException(
          download.artifact,
          repository,
          "Error transferring artifact",
          e
        )
      )
    }
  }

  override fun close() {
    logger.warn("GhaRepositoryConnector close")
//    client.close()
  }

  /**
   * @param[resourcePath] Path to the resource, without the repo URI.
   */
  private fun download(resourcePath: URI, dest: Path) {
    val request = HttpRequest.newBuilder()
      .uri(URI(repository.url).resolve(resourcePath.toString()))
      .build()

    val tempDownloadFile = createTempFile("gha-download-${dest.name}", ".tmp")

    val response = client.send(
      request,
      HttpResponse.BodyHandlers.ofInputStream(),
    )

    if (response.statusCode() != 200) {
      throw IOException("HTTP error occurred: " + response.statusCode())
    }

    response.body().use { source ->
      tempDownloadFile.outputStream().use { sink ->
        source.transferTo(sink)
      }
    }

    tempDownloadFile.moveTo(dest, StandardCopyOption.REPLACE_EXISTING)

    logger.warn("downloaded $resourcePath to ${dest.toUri()}")
  }
}
