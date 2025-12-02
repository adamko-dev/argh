package dev.adamko.githubapiclient.endpoints.repos

import dev.adamko.githubapiclient.model.RepoRelease
import io.ktor.resources.*

data object GetRelease {
  @Resource("/repos/{owner}/{repo}/releases/{releaseId}")
  class Route internal constructor(
    val owner: String,
    val repo: String,
    val releaseId: Int,
  )
  typealias ResponseBody = RepoRelease
}
