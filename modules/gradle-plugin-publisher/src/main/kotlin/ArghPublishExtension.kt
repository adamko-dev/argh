package dev.adamko.argh.gradle.publisher

import dev.adamko.argh.gradle.publisher.config.GitHubOAuthTokenSource
import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

abstract class ArghPublishExtension
@Inject
internal constructor() {

  /**
   * GitHub repository identifier, e.g. `my-org/my-repo`.
   */
  abstract val gitHubRepo: Property<String>

  /**
   * The extensions of additional artifact metadata files to upload.
   * For example, checksums and `.asc` signatures.
   */
  abstract val artifactMetadataExtensions: SetProperty<String>

  internal abstract val baseBuildDir: DirectoryProperty
  internal abstract val stagingRepoDir: DirectoryProperty
  internal abstract val pluginCacheDir: DirectoryProperty

  val stagingRepoName: String = "ArghPublishStaging"

  /**
   * The source of the GitHub OAuth token
   * Argh uses to authenticate with the GitHub REST API.
   *
   * The GitHub API Token must have the `repo` scope.
   *
   * Access to the GitHub API is necessary for attaching publications as release assets.
   */
  abstract val gitHubOAuthToken: Property<GitHubOAuthTokenSource>
}
