package dev.adamko.githubapiclient.endpoints.repos

import dev.adamko.githubapiclient.model.GitHubUser
import dev.adamko.githubapiclient.model.ReactionRollup
import dev.adamko.githubapiclient.model.RepoReleaseAsset
import io.ktor.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#create-a-release
data object CreateRelease {

  @Resource("/repos/{owner}/{repo}/releases")
  class Route internal constructor(
    val owner: String,
    val repo: String,
  )

  @Serializable
  data class RequestBody internal constructor(
    /** The name of the tag. */
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("target_commitish")
    val targetCommitish: String? = null,
    val name: String? = null,
    val body: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    @SerialName("discussion_category_name")
    val discussionCategoryName: String? = null,
    @SerialName("generate_release_notes")
    val generateReleaseNotes: Boolean = false,
    @SerialName("make_latest")
    val makeLatest: MakeLatest? = null
  ) {

    @Serializable
    enum class MakeLatest {
      @SerialName("true")
      True,
      @SerialName("false")
      False,
      @SerialName("legacy")
      Legacy,
    }
  }

  @Serializable
  data class ResponseBody internal constructor(
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("published_at")
    val publishedAt: String?,
    /** `true` to create a draft (unpublished) release, `false` to create a published one. */
    val draft: Boolean,
    val id: Int,
    @SerialName("node_id")
    val nodeId: String,
    val name: String?,
    val body: String? = null,
    /** Whether to identify the release as a prerelease or a full release. */
    val prerelease: Boolean,
    /** The name of the tag. */
    @SerialName("tag_name")
    val tagName: String,
    /** Specifies the commitish value that determines where the Git tag is created from. */
    @SerialName("target_commitish")
    val targetCommitish: String,
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
    val zipballUrl: String?,

    // optional properties
    /** Whether the release is immutable. */
    val immutable: Boolean? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    val author: GitHubUser,
    val assets: List<RepoReleaseAsset>,
    @SerialName("body_html")
    val bodyHtml: String? = null,
    @SerialName("body_text")
    val bodyText: String? = null,
    @SerialName("mentions_count")
    val mentionsCount: Int? = null,
    /** The URL of the release discussion. */
    @SerialName("discussion_url")
    val discussionUrl: String? = null,
    val reactions: ReactionRollup? = null,
  )
}
