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
  private val id: String,
//  url: String,
//  checksumAlgorithmFactorySelector: ChecksumAlgorithmFactorySelector
) : RepositoryLayout, Closeable {
  private val logger: Logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.warn("new GhaRepositoryLayout($id)")
    }

  override fun getChecksumAlgorithmFactories(): List<ChecksumAlgorithmFactory> {
    return emptyList()
  }

  override fun hasChecksums(artifact: Artifact): Boolean {
    return false
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
    return emptyList()
  }

  override fun getChecksumLocations(
    metadata: Metadata,
    upload: Boolean,
    location: URI
  ): List<ChecksumLocation> {
    return emptyList()
  }

  override fun close() {
  }
}

// https://github.com/aSemy/demo-github-asset-publish-repo/releases/download/1.0.0/test-project-1.0.0-sources.jar
private fun releaseAssetUri(
  groupId: String,
//  artifactId : String,
  version: String,
  file: String,
): URI {
  return URI.create(buildString {
    append(groupId.replace(".", "/"))
    append("/releases/download/")
    append(version)
    append("/")
//    append( artifactId)
//    append("-")
//    append(version)
//        metadata.classifier.ifEmpty { null }?.let {
//          append("-")
//          append(it)
//        }
//        append(".")
    append(file)
  })
}
