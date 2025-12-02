package dev.adamko.githubapiclient.endpoints.repos

import io.ktor.resources.*
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

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
    val uploader: JsonObject,
  )
}
