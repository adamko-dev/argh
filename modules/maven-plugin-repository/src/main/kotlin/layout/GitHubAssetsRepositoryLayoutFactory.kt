package dev.adamko.githubassetpublish.maven.layout

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

@Named("githubAssets")
class GitHubAssetsRepositoryLayoutFactory
@Inject
internal constructor(
  private val checksumAlgorithmFactorySelector: ChecksumAlgorithmFactorySelector
) : RepositoryLayoutFactory {
  val log: Logger = LoggerFactory.getLogger(javaClass)

  //  @Throws(NoRepositoryLayoutException::class)
  override fun newInstance(
    session: RepositorySystemSession,
    repository: RemoteRepository,
  ): RepositoryLayout {
    if (repository.contentType != "githubAssets") {
      throw NoRepositoryLayoutException(repository)
    }

    return GitHubAssetsRepositoryLayout(
      repository.id,
      repository.url,
      checksumAlgorithmFactorySelector,
    )
  }

  override fun getPriority(): Float = 10f
}
