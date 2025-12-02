package dev.adamko.githubapiclient.endpoints.repos

import dev.adamko.githubapiclient.model.RepoReleaseAsset
import io.ktor.resources.*

data object UploadReleaseAsset {
  @Resource("/repos/{owner}/{repo}/releases/{releaseId}/assets")
  data class Route internal constructor(
    val owner: String,
    val repo: String,
    val releaseId: Int,
    val name: String,
    val label: String? = null,
  )

  typealias ResponseBody = RepoReleaseAsset
}
