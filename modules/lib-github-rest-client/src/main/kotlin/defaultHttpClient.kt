package dev.adamko.githubapiclient

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.json.Json

internal val defaultHttpClient: HttpClient by lazy {
  HttpClient(CIO) {

    install(ContentNegotiation) {
      json(
        // Ktor's default Json sets encodeDefaults=true :(
        json = Json,
      )
    }

    install(Resources)

    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.ALL
      sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }

    defaultRequest {
      url("https://api.github.com/")
      headers {
        appendIfNameAbsent(HttpHeaders.Accept, GitHubClientAcceptHeader.toString())
//        set(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        appendIfNameAbsent(HttpHeaders.UserAgent, "dev.adamko.githubapiclient")
        set("X-GitHub-Api-Version", "2022-11-28")
      }
    }

    install(HttpCache) {
//      val cacheFile = Path("build/cache").createDirectories().toFile()
//      publicStorage(FileStorage(cacheFile))
    }

    install(HttpRedirect) {
      //checkHttpMethod = false // allow redirect on non-GET too (optional)
    }

//    install(OutgoingHeadersLogger)
  }
}

@Suppress("UnusedReceiverParameter")
internal val HeadersBuilder.GitHubClientAcceptHeader: ContentType
  get() = ContentType("application", "vnd.github+json")

internal fun HeadersBuilder.gitHubApiVersion() {
  append("X-GitHub-Api-Version", "2022-11-28")
}

//val OutgoingHeadersLogger = io.ktor.client.plugins.api.createClientPlugin("OutgoingHeadersLogger") {
//  onRequest { request, _ ->
//    println(">>> ${request.method.value} ${request.url}")
//    println(">>> Accept: ${request.headers[HttpHeaders.Accept]}")
//    println(">>> Content-Type: ${request.headers[HttpHeaders.ContentType]}")
//  }
//}
