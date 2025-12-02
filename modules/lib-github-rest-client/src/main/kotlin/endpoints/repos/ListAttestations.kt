package dev.adamko.githubapiclient.endpoints.repos

import io.ktor.resources.Resource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

data object ListAttestations {
  @Resource("/repos/{owner}/{repo}/attestations/{subject_digest}")
  class Route internal constructor(
    val owner: String,
    val repo: String,
    @SerialName("subject_digest")
    val subjectDigest: String,
  )
  @Serializable
  data class ResponseBody(
    val attestations: List<JsonElement>?,
  )
}
