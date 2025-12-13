package dev.adamko.githubapiclient

import dev.adamko.githubapiclient.endpoints.user.GetUser
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*

/**
 * OAuth app tokens and personal access tokens (classic) need the
 * `user` scope in order for the response to include private profile information.
 */
suspend fun GitHubClient.User.getAuthenticatedUser(): GetUser.ResponseBody {
  val resource = GetUser.Route()
  val response = httpClient.get(resource = resource)
  return response.body()
}


suspend fun GitHubClient.User.getAuthenticatedUserScopes(): List<String>? {
  val resource = GetUser.Route()
  val response = httpClient.get(resource = resource)
  return response.headers["x-oauth-scopes"]
    ?.split(",")
    ?.map { it.trim() }
}
