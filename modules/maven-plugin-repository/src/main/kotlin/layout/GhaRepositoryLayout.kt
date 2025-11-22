package dev.adamko.githubassetpublish.maven.layout

import java.io.Closeable
import java.net.URI
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.metadata.Metadata
import org.eclipse.aether.spi.connector.checksum.ChecksumAlgorithmFactory
import org.eclipse.aether.spi.connector.checksum.ChecksumAlgorithmFactorySelector
import org.eclipse.aether.spi.connector.layout.RepositoryLayout
import org.eclipse.aether.spi.connector.layout.RepositoryLayout.ChecksumLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GhaRepositoryLayout(
//  private val id: String,
//  url: String,
  private val checksumAlgorithmFactorySelector: ChecksumAlgorithmFactorySelector
) : RepositoryLayout, Closeable {
  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  override fun getChecksumAlgorithmFactories(): List<ChecksumAlgorithmFactory> {
    checksumAlgorithmFactorySelector.selectList(
      listOf(
        "SHA-512",
        "SHA-256",
        //"SHA-1",
        //"MD5",
      )
    )
    return emptyList()
  }

  override fun hasChecksums(artifact: Artifact): Boolean {
    return true
  }

  override fun getLocation(artifact: Artifact, upload: Boolean): URI {
    val uri = releaseAssetUri(
      groupId = artifact.groupId,
      version = artifact.version,
      file = buildString {
        append(artifact.artifactId)
        append("-")
        append(artifact.version)
        artifact.classifier.ifEmpty { null }?.let {
          append("-")
          append(it)
        }
        append(".")
        append(artifact.extension)
      },
    )
    logger.info("getLocation artifact: $uri")
    return uri
  }

  override fun getLocation(metadata: Metadata, upload: Boolean): URI {
    val uri = releaseAssetUri(
      groupId = metadata.groupId,
      version = metadata.version,
      file = buildString {
        append(metadata.artifactId)
        append("-")
        append(metadata.version)
        append("-")
        append(metadata.type)
      },
    )

    logger.info("getLocation metadata: $uri")

    return uri
  }

  override fun getChecksumLocations(
    artifact: Artifact,
    upload: Boolean,
    location: URI
  ): List<ChecksumLocation> {
//    if (artifact.extension == "pom") {
//
//    }
    return emptyList()
  }

  override fun getChecksumLocations(
    metadata: Metadata,
    upload: Boolean,
    location: URI
  ): List<ChecksumLocation> {
    return emptyList()
//
//
//    metadata.type
//
//    ChecksumLocation(
//      location = TODO(),
//      checksumAlgorithmFactory = TODO(),
//    )
  }

  override fun close() {
  }
}

private fun releaseAssetUri(
  groupId: String,
  version: String,
  file: String,
): URI {
  return URI.create(buildString {
    append(groupId.replace(".", "/"))
    append("/releases/download/")
    append(version)
    append("/")
    append(file)
  })
}
