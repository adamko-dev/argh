package dev.adamko.githubapiclient.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoRelease internal constructor(
  //region required properties
  val id: Int,
  val name: String?,
  val url: String,
  @SerialName("node_id")
  val nodeId: String,
  /** The name of the tag. */
  @SerialName("tag_name")
  val tagName: String,
  /** Specifies the commitish value that determines where the Git tag is created from. */
  @SerialName("target_commitish")
  val targetCommitish: String,

  @SerialName("html_url")
  val htmlUrl: String,
  @SerialName("assets_url")
  val assetsUrl: String,
  @SerialName("upload_url")
  val uploadUrl: String,
  @SerialName("tarball_url")
  val tarballUrl: String?,
  @SerialName("zipball_url")
  val zipballUrl: String?,

  @SerialName("created_at")
  val createdAt: String,
  @SerialName("published_at")
  val publishedAt: String?,

  /** `true` to create a draft (unpublished) release, `false` to create a published one. */
  val draft: Boolean,
  val author: GitHubUser,
  /** Whether to identify the release as a prerelease or a full release. */
  val prerelease: Boolean,

  val assets: List<RepoReleaseAsset>,
  //endregion

  val reactions: ReactionRollup? = null,
  @SerialName("updated_at")
  val updatedAt: String? = null,

  val body: String? = null,
  /** Whether the release is immutable. */
  val immutable: Boolean? = null,
  @SerialName("body_html")
  val bodyHtml: String? = null,
  @SerialName("body_text")
  val bodyText: String? = null,
  @SerialName("mentions_count")
  val mentionsCount: Int? = null,
  /** The URL of the release discussion. */
  @SerialName("discussion_url")
  val discussionUrl: String? = null,
)

//  data class ResponseBody internal constructor(
//    @SerialName("created_at")
//    val createdAt: String,
//    @SerialName("published_at")
//    val publishedAt: String?,
//    /** `true` to create a draft (unpublished) release, `false` to create a published one. */
//    val draft: Boolean,
//    val id: Int,
//    @SerialName("node_id")
//    val nodeId: String,
//    val name: String?,
//    val body: String? = null,
//    /** Whether to identify the release as a prerelease or a full release. */
//    val prerelease: Boolean,
//    /** The name of the tag. */
//    @SerialName("tag_name")
//    val tagName: String,
//    /** Specifies the commitish value that determines where the Git tag is created from. */
//    @SerialName("target_commitish")
//    val targetCommitish: String,
//    val url: String,
//    @SerialName("html_url")
//    val htmlUrl: String,
//    @SerialName("assets_url")
//    val assetsUrl: String,
//    @SerialName("upload_url")
//    val uploadUrl: String,
//    @SerialName("tarball_url")
//    val tarballUrl: String,
//    @SerialName("zipball_url")
//    val zipballUrl: String?,
//
//    // optional properties
//    /** Whether the release is immutable. */
//    val immutable: Boolean? = null,
//    @SerialName("updated_at")
//    val updatedAt: String? = null,
//    val author: GitHubUser,
//    val assets: List<RepoReleaseAsset>,
//    @SerialName("body_html")
//    val bodyHtml: String? = null,
//    @SerialName("body_text")
//    val bodyText: String? = null,
//    @SerialName("mentions_count")
//    val mentionsCount: Int? = null,
//    /** The URL of the release discussion. */
//    @SerialName("discussion_url")
//    val discussionUrl: String? = null,
//    val reactions: ReactionRollup? = null,
//  )
