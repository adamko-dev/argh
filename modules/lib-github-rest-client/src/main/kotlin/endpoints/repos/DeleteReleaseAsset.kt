package dev.adamko.githubapiclient.endpoints.repos

import io.ktor.resources.*


data object DeleteReleaseAsset {
  @Resource("/repos/{owner}/{repo}/releases/assets/{assetId}")
  class Route internal constructor(
    val owner: String,
    val repo: String,
    val assetId: Int,
  )
}
