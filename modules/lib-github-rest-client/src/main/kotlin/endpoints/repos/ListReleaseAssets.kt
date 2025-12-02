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
//  @Serializable
//  @JvmInline
//  value class ResponseBody(
//    private val content: List<RepoReleaseAsset>,
//  ) : List<RepoReleaseAsset> {
//
//    override val size: Int
//      get() = content.size
//
//    override fun isEmpty(): Boolean =
//      content.isEmpty()
//
//
//    override fun contains(element: RepoReleaseAsset): Boolean =
//      content.contains(element)
//
//
//    override fun iterator(): Iterator<RepoReleaseAsset> =
//      content.iterator()
//
//
//    override fun containsAll(elements: Collection<RepoReleaseAsset>): Boolean =
//      content.containsAll(elements)
//
//
//    override fun get(index: Int): RepoReleaseAsset =
//      content[index]
//
//
//    override fun indexOf(element: RepoReleaseAsset): Int =
//      content.indexOf(element)
//
//
//    override fun lastIndexOf(element: RepoReleaseAsset): Int =
//      content.lastIndexOf(element)
//
//
//    override fun listIterator(): ListIterator<RepoReleaseAsset> =
//      content.listIterator()
//
//
//    override fun listIterator(index: Int): ListIterator<RepoReleaseAsset> =
//      content.listIterator()
//
//
//    override fun subList(
//      fromIndex: Int,
//      toIndex: Int
//    ): List<RepoReleaseAsset> =
//      content.subList(fromIndex, toIndex)
//  }
}
