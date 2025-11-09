package dev.adamko.githubassetpublish.maven.layout

import java.io.Closeable
import java.net.URI
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.metadata.Metadata
import org.eclipse.aether.spi.connector.checksum.ChecksumAlgorithmFactory
import org.eclipse.aether.spi.connector.checksum.ChecksumAlgorithmFactorySelector
import org.eclipse.aether.spi.connector.layout.RepositoryLayout
import org.eclipse.aether.spi.connector.layout.RepositoryLayout.ChecksumLocation

class GitHubAssetsRepositoryLayout(
  private val id: String,
  url: String,
  checksumAlgorithmFactorySelector: ChecksumAlgorithmFactorySelector
) : RepositoryLayout, Closeable {

  override fun getChecksumAlgorithmFactories(): List<ChecksumAlgorithmFactory> {
    TODO("Not yet implemented")
  }

  override fun hasChecksums(artifact: Artifact): Boolean {
    TODO("Not yet implemented")
  }

  override fun getLocation(artifact: Artifact, upload: Boolean): URI {
    TODO("Not yet implemented")
  }

  override fun getLocation(metadata: Metadata, upload: Boolean): URI {
    TODO("Not yet implemented")
  }

  override fun getChecksumLocations(
    artifact: Artifact,
    upload: Boolean,
    location: URI
  ): List<ChecksumLocation> {
    TODO("Not yet implemented")
  }

  override fun getChecksumLocations(
    metadata: Metadata,
    upload: Boolean,
    location: URI
  ): List<ChecksumLocation> {
    TODO("Not yet implemented")
  }

  override fun close() {
    TODO("Not yet implemented")
  }
}
