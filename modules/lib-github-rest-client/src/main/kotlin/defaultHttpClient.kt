import io.ktor.client.*
import io.ktor.client.engine.cio.*

internal val defaultHttpClient: HttpClient by lazy {
  HttpClient(CIO) {
    expectSuccess = false
  }
}
