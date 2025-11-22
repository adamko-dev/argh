package dev.adamko.githubassetpublish.maven.connector

import dev.adamko.githubassetpublish.maven.GitHubAssetsContentType
import dev.adamko.githubassetpublish.maven.githubAssetsRepo
import javax.inject.Inject
import javax.inject.Named
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.spi.connector.RepositoryConnector
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.checksum.ChecksumAlgorithmFactorySelector
import org.eclipse.aether.transfer.NoRepositoryConnectorException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Named(githubAssetsRepo)
class GhaRepositoryConnectorFactory
@Inject
constructor(
  private val checksumAlgorithmFactorySelector: ChecksumAlgorithmFactorySelector
) : RepositoryConnectorFactory {
  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  override fun newInstance(
    session: RepositorySystemSession,
    repository: RemoteRepository,
  ): RepositoryConnector {
    logger.warn("GhaRepositoryConnectorFactory newInstance: ${repository.contentType}")
    if (repository.contentType != GitHubAssetsContentType) {
      throw NoRepositoryConnectorException(repository)
    }
    return GhaRepositoryConnector(
      session,
      repository,
      checksumAlgorithmFactorySelector,
    )
  }

  override fun getPriority(): Float = 1f
}
