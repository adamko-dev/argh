package dev.adamko.githubassetpublish.maven.layout

import dev.adamko.githubassetpublish.maven.GitHubAssetsContentType
import javax.inject.Inject
import javax.inject.Named
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.spi.connector.checksum.ChecksumAlgorithmFactorySelector
import org.eclipse.aether.spi.connector.layout.RepositoryLayout
import org.eclipse.aether.spi.connector.layout.RepositoryLayoutFactory
import org.eclipse.aether.transfer.NoRepositoryLayoutException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Named(GitHubAssetsContentType)
class GhaRepositoryLayoutFactory
@Inject
internal constructor(
  private val checksumAlgorithmFactorySelector: ChecksumAlgorithmFactorySelector
) : RepositoryLayoutFactory {
  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  init {
    logger.warn("new GitHubAssetsRepositoryLayoutFactory")
  }

  override fun newInstance(
    session: RepositorySystemSession,
    repository: RemoteRepository,
  ): RepositoryLayout {
//    log.warn("newInstance($session, $repository)")
    if (repository.contentType != GitHubAssetsContentType) {
      throw NoRepositoryLayoutException(repository)
    }

    return GhaRepositoryLayout(
      repository.id,
//      repository.url,
//      checksumAlgorithmFactorySelector,
    )
  }

  override fun getPriority(): Float = 1f
}
