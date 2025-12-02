package dev.adamko.githubapiclient.endpoints.repos

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

data object CreateAttestation {
  @Resource("/repos/{owner}/{repo}/attestations")
  class Route internal constructor(
    val owner: String,
    val repo: String,
  )
  @Serializable
  data class RequestBody(
    val bundle: JsonElement,
  )
}
