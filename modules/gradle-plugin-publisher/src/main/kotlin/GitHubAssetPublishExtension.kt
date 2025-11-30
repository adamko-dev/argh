package dev.adamko.githubassetpublish

import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

abstract class GitHubAssetPublishExtension
@Inject
internal constructor() {

  /**
   * GitHub repository identifier, e.g. `my-org/my-repo`.
   */
  abstract val githubRepo: Property<String>

  /**
   * The extensions of additional artifact metadata files to upload.
   * For example, checksums and `.asc` signatures.
   */
  abstract val artifactMetadataExtensions: SetProperty<String>

  internal abstract val gapBuildDir: DirectoryProperty
  internal abstract val stagingRepoDir: DirectoryProperty

  val stagingRepoName: String = "GitHubAssetPublishStaging"
}
