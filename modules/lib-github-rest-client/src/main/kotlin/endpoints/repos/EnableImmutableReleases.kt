package dev.adamko.githubapiclient.endpoints.repos

import io.ktor.resources.*

data object EnableImmutableReleases {
  @Resource("/repos/{owner}/{repo}/immutable-releases")
  class Route internal constructor(
    val owner: String,
    val repo: String,
  )
}
