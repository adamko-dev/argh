package dev.adamko.githubapiclient.endpoints.repos

import io.ktor.resources.Resource

data object ListReleases {
  @Resource("/repos/{owner}/{repo}/releases")
  class Route internal constructor(
    val owner: String,
    val repo: String,
  )
}
