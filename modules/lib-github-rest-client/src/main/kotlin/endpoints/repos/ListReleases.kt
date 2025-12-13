package dev.adamko.githubapiclient.endpoints.repos

import dev.adamko.githubapiclient.model.RepoRelease
import io.ktor.resources.Resource
import kotlinx.serialization.SerialName

/**
 * https://docs.github.com/en/rest/releases/assets?apiVersion=2022-11-28#list-release-assets
 */
data object ListReleases {
  @Resource("/repos/{owner}/{repo}/releases")
  class Route internal constructor(
    val owner: String,
    val repo: String,
    /**
     *
     * The number of results per page (max 100).
     * For more information, see
     * ["Using pagination in the REST API."](https://docs.github.com/en/rest/using-the-rest-api/using-pagination-in-the-rest-api?apiVersion=2022-11-28)
     */
    @SerialName("per_page")
    val perPage: Int? = null,
    /**
     * The page number of the results to fetch.
     * ["Using pagination in the REST API."](https://docs.github.com/en/rest/using-the-rest-api/using-pagination-in-the-rest-api?apiVersion=2022-11-28)
     */
    val page: Int? = null,
  )

  typealias ResponseBody = List<RepoRelease>
}
