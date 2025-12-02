package dev.adamko.githubapiclient.endpoints.repos

import io.ktor.resources.*


data object DeleteRelease {
  @Resource("/repos/{owner}/{repo}/releases/{releaseId}")
  class Route internal constructor(
    val owner: String,
    val repo: String,
    val releaseId: Int,
  )
}
