package dev.adamko.githubapiclient

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.accept
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal val defaultHttpClient: HttpClient by lazy {
  HttpClient(CIO) {

    install(ContentNegotiation) {
      json(
        // Ktor's default Json sets encodeDefaults=true :(
        json = Json,
//        {
//          encodeDefaults = true
//          isLenient = true
//          allowSpecialFloatingPointValues = true
//          allowStructuredMapKeys = true
//          prettyPrint = false
//          useArrayPolymorphism = false
//        }

      )
    }

    install(Resources)

//    install(Auth) {
//      bearer {
//        loadTokens {
//          bearerTokenStorage.last()
//        }
//        refreshTokens {
//          getGitHubAuthToken()
//          val refreshTokenInfo: TokenInfo = client.post(
//            url = "https://accounts.google.com/o/oauth2/token",
////            formParameters = parameters {
////              append("grant_type", "refresh_token")
////              append("client_id", System.getenv("GOOGLE_CLIENT_ID"))
////              append("refresh_token", oldTokens?.refreshToken ?: "")
////            }
//          ) { markAsRefreshTokenRequest() }.body()
//          bearerTokenStorage.add(BearerTokens(refreshTokenInfo.accessToken, oldTokens?.refreshToken!!))
//          bearerTokenStorage.last()
//        }
//        sendWithoutRequest { request ->
//          request.url.host == "www.googleapis.com"
//        }
//      }
//    }
//
//    install(Auth) {
//      bearer {
//        loadTokens {
//          obtainAuthToken.action().let {
//            BearerTokens(
//              it.token.toString(),
//              ""
//            )
//          }
//        }
//        // Configure bearer authentication
//        refreshTokens {
//          this.client
//          // Refresh tokens and return them as the 'BearerTokens' instance
//          BearerTokens("def456", "xyz111")
//        }
//      }
//    }


    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.BODY
//      filter { request ->
//        request.url.host.contains("ktor.io")
//      }
      sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }

    defaultRequest {
      url("https://api.github.com/")
      headers {
        userAgent("dev.adamko.githubapiclient")
        accept(GitHubClientAcceptHeader)
        contentType(ContentType.Application.Json)
//        append(HttpHeaders.UserAgent, "dev.adamko.githubapiclient")
//        append(HttpHeaders.Accept, )
//        append(HttpHeaders.Authorization, "Bearer ${token.token}")
        gitHubApiVersion()
      }
    }
  }
}

@Suppress("UnusedReceiverParameter")
internal val HeadersBuilder.GitHubClientAcceptHeader: ContentType
  get() = ContentType("application", "vnd.github+json")

internal fun HeadersBuilder.gitHubApiVersion() {
  append("X-GitHub-Api-Version", "2022-11-28")
}
