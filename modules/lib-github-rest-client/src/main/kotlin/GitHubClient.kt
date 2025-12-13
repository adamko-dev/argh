package dev.adamko.githubapiclient

import dev.adamko.githubapiclient.auth.AuthTokenStore
import dev.adamko.githubapiclient.auth.ObtainAuthToken
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.nio.file.Path

class GitHubClient(
  tokenDataFile: Path,
  internal val httpClient: HttpClient = defaultHttpClient(),
) : AutoCloseable {

  private val authTokenStore = AuthTokenStore(tokenDataFile)
  private val obtainAuthToken = ObtainAuthToken(authTokenStore)

  init {
    httpClient.plugin(HttpSend).intercept { request ->
      val authToken = obtainAuthToken.action()
      request.headers {
        append(HttpHeaders.Authorization, "Bearer ${authToken.token}")
      }
      execute(request)
    }
  }

  override fun close() {
    httpClient.close()
  }

  val repos: Repos = Repos(httpClient = httpClient)
  val user: User = User(httpClient = httpClient)

  class Repos internal constructor(
    internal val httpClient: HttpClient,
  )

  class User internal constructor(
    internal val httpClient: HttpClient,
  )
}
