package dev.adamko.argh.maven.repository.layout

import dev.adamko.argh.maven.repository.modify
import java.net.URI
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.metadata.Metadata
import org.eclipse.aether.spi.connector.checksum.ChecksumAlgorithmFactory
import org.eclipse.aether.spi.connector.checksum.ChecksumAlgorithmFactorySelector
import org.eclipse.aether.spi.connector.layout.RepositoryLayout
import org.eclipse.aether.spi.connector.layout.RepositoryLayout.ChecksumLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ArghRepositoryLayout(
  private val checksumAlgorithmFactorySelector: ChecksumAlgorithmFactorySelector
) : RepositoryLayout {
  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  override fun getChecksumAlgorithmFactories(): List<ChecksumAlgorithmFactory> {
    checksumAlgorithmFactorySelector.selectList(
      listOf(
        "SHA-256",
      )
    )
    return emptyList()
  }

  override fun hasChecksums(artifact: Artifact): Boolean {
    return true
  }

  override fun getLocation(artifact: Artifact, upload: Boolean): URI {
    val uri = artifact.toReleaseAssetUri()
    logger.debug("getLocation artifact: $uri")
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

    logger.debug("getLocation metadata: $uri")

    return uri
  }

  override fun getChecksumLocations(
    artifact: Artifact,
    upload: Boolean,
    location: URI
  ): List<ChecksumLocation> {
    if (upload) return emptyList()

    val uri = artifact.toReleaseAssetUri()

    return checksumAlgorithmFactorySelector.checksumAlgorithmFactories.map { factory ->
      uri.modify {
        path += ".${factory.algorithm}"
      }
      ChecksumLocation(uri, factory)
    }
  }

  override fun getChecksumLocations(
    metadata: Metadata,
    upload: Boolean,
    location: URI
  ): List<ChecksumLocation> {
    // Metadata, e.g. `maven-metadata.xml`, is not supported.
    // https://maven.apache.org/repositories/metadata.html
    return emptyList()
  }
}

private fun Artifact.toReleaseAssetUri(): URI {
  return releaseAssetUri(
    groupId = groupId,
    version = version,
    file = buildString {
      append(artifactId)
      append("-")
      append(version)
      classifier.ifEmpty { null }?.let {
        append("-")
      }
      append(".")
      append(extension)
    }
  )
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
