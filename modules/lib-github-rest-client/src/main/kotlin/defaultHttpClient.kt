package dev.adamko.githubapiclient

import dev.adamko.githubapiclient.auth.ObtainAuthToken
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import java.nio.file.Path

internal suspend fun defaultHttpClient(
  pluginCacheDir: Path,
): HttpClient {
  val token = ObtainAuthToken(pluginCacheDir).action()
  return HttpClient(CIO) {
    defaultRequest {
      url("https://api.github.com/")
      headers {
        append(HttpHeaders.UserAgent, "dev.adamko.githubapiclient")
        append(HttpHeaders.Accept, "application/vnd.github+json")
        append(HttpHeaders.Authorization, "Bearer ${token.token}")
        append("X-GitHub-Api-Version", "2022-11-28")
      }
    }
  }
}
