package dev.adamko.githubassetpublish

import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

abstract class GitHubAssetPublishExtension
@Inject
internal constructor() {

  /**
   * GitHub repository identifier, e.g. `my-org/my-repo`.
   */
  abstract val githubRepo: Property<String>

  internal abstract val gapBuildDir: DirectoryProperty
  internal abstract val stagingRepoDir: DirectoryProperty

  val stagingRepoName: String = "GitHubAssetPublishStaging"
}
