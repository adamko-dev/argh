package dev.adamko.githubapiclient.endpoints.repos

import dev.adamko.githubapiclient.model.RepoReleaseAsset
import io.ktor.resources.*
import kotlinx.serialization.SerialName

/**
 * https://docs.github.com/en/rest/releases/assets?apiVersion=2022-11-28#list-release-assets
 */
data object ListReleaseAssets {

  @Resource("/repos/{owner}/{repo}/releases/{releaseId}/assets")
  data class Route internal constructor(
    /**
     * The account owner of the repository. The name is not case-sensitive.
     */
    val owner: String,
    /**
     * The name of the repository without the `.git` extension. The name is not case-sensitive.
     */
    val repo: String,
    /**
     * The unique identifier of the release.
     */
    val releaseId: Int,
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

  typealias ResponseBody = List<RepoReleaseAsset>
}
