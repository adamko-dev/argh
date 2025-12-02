package dev.adamko.githubapiclient.endpoints.repos

import io.ktor.resources.*
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

data object UpdateRelease {
  @Resource("/repos/{owner}/{repo}/releases/{release_id}")
  class Route internal constructor(
    val owner: String,
    val repo: String,
    @SerialName("release_id")
    val releaseId: Int,
  )
  @Serializable
  data class RequestBody internal constructor(
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
  data class ResponseBody internal constructor(
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
    val author: JsonElement,
    val assets: List<JsonElement>,
    @SerialName("body_html")
    val bodyHtml: String?,
    @SerialName("body_text")
    val bodyText: String?,
    @SerialName("mentions_count")
    val mentionsCount: Int?,
    @SerialName("discussion_url")
    val discussionUrl: String?,
    val reactions: JsonElement?,
  )
}
