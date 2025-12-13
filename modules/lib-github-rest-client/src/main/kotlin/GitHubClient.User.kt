package dev.adamko.githubapiclient

import dev.adamko.githubapiclient.endpoints.user.GetUser
import io.ktor.client.plugins.resources.*


suspend fun GitHubClient.User.getAuthenticatedUserScopes(): List<String>? {
  val resource = GetUser.Route()
  val response = httpClient.get(resource = resource)
  return response.headers["x-oauth-scopes"]
    ?.split(",")
    ?.map { it.trim() }
}
