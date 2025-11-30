import io.ktor.client.*
import io.ktor.resources.*
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GitHubClient internal constructor(
  internal val httpClient: HttpClient = defaultHttpClient,
) {

  val repos: Repos =
    Repos(httpClient)

  class Repos internal constructor(
    internal val httpClient: HttpClient,
  ) {

    /**
     * Check if immutable releases are enabled for a repository
     *
     * Shows whether immutable releases are enabled or disabled.
     * Also identifies whether immutability is being enforced by the repository owner.
     * The authenticated user must have admin read access to the repository.
     */
    data object CheckImmutableReleases {
      @Resource("/repos/{owner}/{repo}/immutable-releases")
      class Route internal constructor(
        val owner: String,
        val repo: String,
      )
      @Serializable
      data class ResponseBody(
        val enabled: Boolean,
        @SerialName("enforced_by_owner")
        val enforcedByOwner: Boolean,
      )
    }
    //    data object CreateAttestation {
//      @Resource("/repos/{owner}/{repo}/attestations")
//      class Route internal constructor(
//        val owner: String,
//        val repo: String,
//      )
//      @Serializable
//      data class RequestBody(
//        val bundle: Object,
//      )
//    }
    data object CreateRelease {
      @Resource("/repos/{owner}/{repo}/releases")
      class Route internal constructor(
        val owner: String,
        val repo: String,
      )
      @Serializable
      data class RequestBody(
        @SerialName("tag_name")
        val tagName: String,
        @SerialName("target_commitish")
        val targetCommitish: String?,
        val name: String?,
        val body: String?,
        val draft: Boolean?,
        val prerelease: Boolean?,
        @SerialName("discussion_category_name")
        val discussionCategoryName: String?,
        @SerialName("generate_release_notes")
        val generateReleaseNotes: Boolean?,
        @SerialName("make_latest")
        val makeLatest: String?,
      )
    }

    data object DeleteRelease {
      @Resource("/repos/{owner}/{repo}/releases/{release_id}")
      class Route internal constructor(
        val owner: String,
        val repo: String,
        @SerialName("release_id")
        val releaseId: Int,
      )
    }

    data object DeleteReleaseAsset {
      @Resource("/repos/{owner}/{repo}/releases/assets/{asset_id}")
      class Route internal constructor(
        val owner: String,
        val repo: String,
        @SerialName("asset_id")
        val assetId: Int,
      )
    }

    data object EnableImmutableReleases {
      @Resource("/repos/{owner}/{repo}/immutable-releases")
      class Route internal constructor(
        val owner: String,
        val repo: String,
      )
    }

    data object GenerateReleaseNotes {
      @Resource("/repos/{owner}/{repo}/releases/generate-notes")
      class Route internal constructor(
        val owner: String,
        val repo: String,
      )
      @Serializable
      data class RequestBody(
        @SerialName("tag_name")
        val tagName: String,
        @SerialName("target_commitish")
        val targetCommitish: String?,
        @SerialName("previous_tag_name")
        val previousTagName: String?,
        @SerialName("configuration_file_path")
        val configurationFilePath: String?,
      )
      @Serializable
      data class ResponseBody(
        val name: String,
        val body: String,
      )
    }

    data object GetLatestRelease {
      @Resource("/repos/{owner}/{repo}/releases/latest")
      class Route internal constructor(
        val owner: String,
        val repo: String,
      )
      @Serializable
      data class ResponseBody(
        val url: String,
        @SerialName("html_url")
        val htmlUrl: String,
        @SerialName("assets_url")
        val assetsUrl: String,
        @SerialName("upload_url")
        val uploadUrl: String,
        @SerialName("tarball_url")
        val tarballUrl: String,
        @SerialName("zipball_url")
        val zipballUrl: String,
        val id: Int,
        @SerialName("node_id")
        val nodeId: String,
        @SerialName("tag_name")
        val tagName: String,
        @SerialName("target_commitish")
        val targetCommitish: String,
        val name: String,
        val body: String?,
        val draft: Boolean,
        val prerelease: Boolean,
        val immutable: Boolean?,
        @SerialName("created_at")
        val createdAt: Instant,
        @SerialName("published_at")
        val publishedAt: Instant,
        @SerialName("updated_at")
        val updatedAt: Instant?,
        val author: Object,
        val assets: List<Object>,
        @SerialName("body_html")
        val bodyHtml: String?,
        @SerialName("body_text")
        val bodyText: String?,
        @SerialName("mentions_count")
        val mentionsCount: Int?,
        @SerialName("discussion_url")
        val discussionUrl: String?,
        val reactions: Object?,
      )
    }

    data object GetRelease {
      @Resource("/repos/{owner}/{repo}/releases/{releaseId}")
      class Route internal constructor(
        val owner: String,
        val repo: String,
        val releaseId: Int,
      )
      @Serializable
      data class ResponseBody(
        val url: String,
        @SerialName("html_url")
        val htmlUrl: String,
        @SerialName("assets_url")
        val assetsUrl: String,
        @SerialName("upload_url")
        val uploadUrl: String,
        @SerialName("tarball_url")
        val tarballUrl: String,
        @SerialName("zipball_url")
        val zipballUrl: String,
        val id: Int,
        @SerialName("node_id")
        val nodeId: String,
        @SerialName("tag_name")
        val tagName: String,
        @SerialName("target_commitish")
        val targetCommitish: String,
        val name: String,
        val body: String?,
        val draft: Boolean,
        val prerelease: Boolean,
        val immutable: Boolean?,
        @SerialName("created_at")
        val createdAt: Instant,
        @SerialName("published_at")
        val publishedAt: Instant,
        @SerialName("updated_at")
        val updatedAt: Instant?,
        val author: Object,
        val assets: List<Object>,
        @SerialName("body_html")
        val bodyHtml: String?,
        @SerialName("body_text")
        val bodyText: String?,
        @SerialName("mentions_count")
        val mentionsCount: Int?,
        @SerialName("discussion_url")
        val discussionUrl: String?,
        val reactions: Object?,
      )
    }

    data object GetReleaseAsset {
      @Resource("/repos/{owner}/{repo}/releases/assets/{assetId}")
      class Route internal constructor(
        val owner: String,
        val repo: String,
        val assetId: Int,
      )
      @Serializable
      data class ResponseBody(
        val url: String,
        @SerialName("browser_download_url")
        val browserDownloadUrl: String,
        val id: Int,
        @SerialName("node_id")
        val nodeId: String,
        val name: String,
        val label: String,
        val state: String,
        @SerialName("content_type")
        val contentType: String,
        val size: Int,
        val digest: String,
        @SerialName("download_count")
        val downloadCount: Int,
        @SerialName("created_at")
        val createdAt: Instant,
        @SerialName("updated_at")
        val updatedAt: Instant,
        val uploader: Object,
      )
    }

    data object GetReleaseByTag {
      @Resource("/repos/{owner}/{repo}/releases/tags/{tag}")
      class Route internal constructor(
        val owner: String,
        val repo: String,
        val tag: String,
      )
      @Serializable
      data class ResponseBody(
        val url: String,
        @SerialName("html_url")
        val htmlUrl: String,
        @SerialName("assets_url")
        val assetsUrl: String,
        @SerialName("upload_url")
        val uploadUrl: String,
        @SerialName("tarball_url")
        val tarballUrl: String,
        @SerialName("zipball_url")
        val zipballUrl: String,
        val id: Int,
        @SerialName("node_id")
        val nodeId: String,
        @SerialName("tag_name")
        val tagName: String,
        @SerialName("target_commitish")
        val targetCommitish: String,
        val name: String,
        val body: String?,
        val draft: Boolean,
        val prerelease: Boolean,
        val immutable: Boolean?,
        @SerialName("created_at")
        val createdAt: Instant,
        @SerialName("published_at")
        val publishedAt: Instant,
        @SerialName("updated_at")
        val updatedAt: Instant?,
        val author: Object,
        val assets: List<Object>,
        @SerialName("body_html")
        val bodyHtml: String?,
        @SerialName("body_text")
        val bodyText: String?,
        @SerialName("mentions_count")
        val mentionsCount: Int?,
        @SerialName("discussion_url")
        val discussionUrl: String?,
        val reactions: Object?,
      )
    }

    data object ListAttestations {
      @Resource("/repos/{owner}/{repo}/attestations/{subject_digest}")
      class Route internal constructor(
        val owner: String,
        val repo: String,
        @SerialName("subject_digest")
        val subjectDigest: String,
      )
      @Serializable
      data class ResponseBody(
        val attestations: List<Object>?,
      )
    }

    data object ListReleaseAssets {
      @Resource("/repos/{owner}/{repo}/releases/{release_id}/assets")
      class Route internal constructor(
        val owner: String,
        val repo: String,
        @SerialName("release_id")
        val releaseId: Int,
      )
    }

    data object ListReleases {
      @Resource("/repos/{owner}/{repo}/releases")
      class Route internal constructor(
        val owner: String,
        val repo: String,
      )
    }

    data object UpdateRelease {
      @Resource("/repos/{owner}/{repo}/releases/{release_id}")
      class Route internal constructor(
        val owner: String,
        val repo: String,
        @SerialName("release_id")
        val releaseId: Int,
      )
      @Serializable
      data class RequestBody(
        @SerialName("tag_name")
        val tagName: String?,
        @SerialName("target_commitish")
        val targetCommitish: String?,
        val name: String?,
        val body: String?,
        val draft: Boolean?,
        val prerelease: Boolean?,
        @SerialName("make_latest")
        val makeLatest: String?,
        @SerialName("discussion_category_name")
        val discussionCategoryName: String?,
      )
      @Serializable
      data class ResponseBody(
        val url: String,
        @SerialName("html_url")
        val htmlUrl: String,
        @SerialName("assets_url")
        val assetsUrl: String,
        @SerialName("upload_url")
        val uploadUrl: String,
        @SerialName("tarball_url")
        val tarballUrl: String,
        @SerialName("zipball_url")
        val zipballUrl: String,
        val id: Int,
        @SerialName("node_id")
        val nodeId: String,
        @SerialName("tag_name")
        val tagName: String,
        @SerialName("target_commitish")
        val targetCommitish: String,
        val name: String,
        val body: String?,
        val draft: Boolean,
        val prerelease: Boolean,
        val immutable: Boolean?,
        @SerialName("created_at")
        val createdAt: Instant,
        @SerialName("published_at")
        val publishedAt: Instant,
        @SerialName("updated_at")
        val updatedAt: Instant?,
        val author: Object,
        val assets: List<Object>,
        @SerialName("body_html")
        val bodyHtml: String?,
        @SerialName("body_text")
        val bodyText: String?,
        @SerialName("mentions_count")
        val mentionsCount: Int?,
        @SerialName("discussion_url")
        val discussionUrl: String?,
        val reactions: Object?,
      )
    }

    data object UpdateReleaseAsset {
      @Resource("/repos/{owner}/{repo}/releases/assets/{asset_id}")
      class Route internal constructor(
        val owner: String,
        val repo: String,
        @SerialName("asset_id")
        val assetId: Int,
      )
      @Serializable
      data class RequestBody(
        val name: String?,
        val label: String?,
        val state: String?,
      )
      @Serializable
      data class ResponseBody(
        val url: String,
        @SerialName("browser_download_url")
        val browserDownloadUrl: String,
        val id: Int,
        @SerialName("node_id")
        val nodeId: String,
        val name: String,
        val label: String,
        val state: String,
        @SerialName("content_type")
        val contentType: String,
        val size: Int,
        val digest: String,
        @SerialName("download_count")
        val downloadCount: Int,
        @SerialName("created_at")
        val createdAt: Instant,
        @SerialName("updated_at")
        val updatedAt: Instant,
        val uploader: Object,
      )
    }

    data object UploadReleaseAsset {
      @Resource("/repos/{owner}/{repo}/releases/{release_id}/assets")
      class Route internal constructor(
        val owner: String,
        val repo: String,
        @SerialName("release_id")
        val releaseId: Int,
      )
    }
  }

}
