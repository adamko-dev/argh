package dev.adamko.githubapiclient.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Data related to a release. */
@Serializable
data class RepoReleaseAsset internal constructor(
  val id: Long,
  /** The file name of the asset. */
  val name: String,
  @SerialName("content_type")
  val contentType: String,
  val size: Long,
  val digest: String?,
  /** State of the release asset. */
  val state: State,
  val url: String,
  @SerialName("node_id")
  val nodeId: String,
  @SerialName("download_count")
  val downloadCount: Long,
  val label: String?,
  val uploader: GitHubUser?,
  @SerialName("browser_download_url")
  val browserDownloadUrl: String,
  @SerialName("created_at")
  val createdAt: String,
  @SerialName("updated_at")
  val updatedAt: String,
) {

  @Serializable
  enum class State {
    @SerialName("uploaded")
    Uploaded,
    @SerialName("open")
    Open,
  }
}
