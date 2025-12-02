package dev.adamko.githubapiclient

import io.ktor.client.*
import java.nio.file.Path

class GitHubClient private constructor(
  internal val httpClient: HttpClient,
) : AutoCloseable {

  override fun close() {
    httpClient.close()
  }

  val repos: Repos = Repos(httpClient)

  class Repos internal constructor(
    internal val httpClient: HttpClient,
  )

  companion object {
    suspend fun GitHubClient(
      pluginCacheDir: Path,
    ): GitHubClient {
      val httpClient = defaultHttpClient(pluginCacheDir)
      return GitHubClient(httpClient)
    }
  }
}
