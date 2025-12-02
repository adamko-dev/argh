package dev.adamko.githubapiclient.endpoints.repos

import io.ktor.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Check if immutable releases are enabled for a repository
 *
 * Shows whether immutable releases are enabled or disabled.
 * Also identifies whether immutability is being enforced by the repository owner.
 * The authenticated user must have admin read access to the repository.
 */
data object CheckImmutableReleases {
  @Resource("/repos/{owner}/{repo}/immutable-releases")
  class Route internal constructor(
    val owner: String,
    val repo: String,
  )
  @Serializable
  data class ResponseBody(
    val enabled: Boolean,
    @SerialName("enforced_by_owner")
    val enforcedByOwner: Boolean,
  )
}
