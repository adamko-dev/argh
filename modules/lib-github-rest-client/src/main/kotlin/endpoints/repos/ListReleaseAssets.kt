package dev.adamko.githubapiclient.endpoints.repos

import dev.adamko.githubapiclient.endpoints.PaginatedRoute
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
    @SerialName("per_page")
    override val perPage: Int? = null,
    override val page: Int? = null,
  ) : PaginatedRoute

  typealias ResponseBody = List<RepoReleaseAsset>
}
