package dev.adamko.githubapiclient.endpoints.repos

import dev.adamko.githubapiclient.model.RepoRelease
import io.ktor.resources.*

/**
 * Get a published release with the specified tag.
 *
 * https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#get-a-release-by-tag-name
 */
data object GetReleaseByTag {
  @Resource("/repos/{owner}/{repo}/releases/tags/{tag}")
  data class Route internal constructor(
    val owner: String,
    val repo: String,
    val tag: String,
  )

  /** A release. */
  typealias ResponseBody = RepoRelease
}
