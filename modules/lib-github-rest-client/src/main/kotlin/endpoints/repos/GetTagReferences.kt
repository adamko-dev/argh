package dev.adamko.githubapiclient.endpoints.repos

import dev.adamko.githubapiclient.model.RepoReleaseAsset
import io.ktor.resources.*

data object GetTagReferences {
  @Resource("/repos/{owner}/{repo}/git/refs/tags")
  data class Route internal constructor(
    val owner: String,
    val repo: String,
    val releaseId: Int,
    val name: String,
    val label: String? = null,
  )

  typealias ResponseBody = RepoReleaseAsset
}
