package dev.adamko.githubapiclient.endpoints.repos

import dev.adamko.githubapiclient.endpoints.PaginatedRoute
import dev.adamko.githubapiclient.model.RepoRelease
import io.ktor.resources.*
import kotlinx.serialization.SerialName

/**
 * https://docs.github.com/en/rest/releases/assets?apiVersion=2022-11-28#list-release-assets
 */
data object ListReleases {
  @Resource("/repos/{owner}/{repo}/releases")
  class Route internal constructor(
    val owner: String,
    val repo: String,
    @SerialName("per_page")
    override val perPage: Int? = null,
    override val page: Int? = null,
  ) : PaginatedRoute

  typealias ResponseBody = List<RepoRelease>
}
