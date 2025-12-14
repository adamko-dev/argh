package dev.adamko.githubapiclient.auth

import dev.adamko.githubapiclient.GitHubClientAcceptHeader
import dev.adamko.githubapiclient.auth.AccessStatusResponse.Error.Code.*
import dev.adamko.githubapiclient.gitHubApiVersion
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

internal class ObtainAuthToken(
  private val authTokenStore: AuthTokenStore,
  private val gitHubOAuthToken: String,
  private val httpClient: HttpClient = HttpClient(CIO) {
    defaultRequest {
      accept(ContentType.Application.Json)
    }
    install(ContentNegotiation) {
      json()
    }
  },
//  private val clock: Clock = Clock.System,
) {
  private val lock: Mutex = Mutex()
//  private val expirationFudgeFactor = 60.seconds

  private val statusCache = ConcurrentHashMap<String, Boolean>()

  /**
   * Try fetching a token from `gitHubOAuthToken`
   *
   * Otherwise, check the config dir for a stored token.
   *
   * Is the stored token expired? Request a new one using the refresh token.
   *
   * Otherwise, request a new token (and refresh token) using the device code flow.
   */
  suspend fun action(): GitHubAuthToken = lock.withLock {

    val envToken = when {
      gitHubOAuthToken == "EnvVar"         -> System.getenv("GITHUB_TOKEN")
      gitHubOAuthToken.startsWith("File:") -> Path(gitHubOAuthToken.removePrefix("File:")).readText().trim()
      else                                 -> null
    }

    if (envToken != null) {
      return GitHubAuthToken(envToken)
    }

    val tokenData = authTokenStore.load()

    when {
      tokenData == null                  ->
        println("No token found")

//      (tokenData.accessTokenExpiresAt + expirationFudgeFactor) < clock.now() ->
//        println("Token ${tokenData.accessToken} expired ${tokenData.accessTokenExpiresAt} vs now:${clock.now() - 60.seconds}.")

      !checkToken(tokenData.accessToken) -> {
        println("Token ${tokenData.accessToken} is not valid")
      }
//      statusCache[tokenData.accessToken.token] != true                       -> {
//        println("Token ${tokenData.accessToken} is not valid")
//      }

      else                               -> {
        println("Token ${tokenData.accessToken} is valid")
        return tokenData.accessToken
      }
    }

//    when {
//      tokenData == null
//          || (tokenData.refreshTokenExpiresAt + expirationFudgeFactor) < clock.now() -> {
//        println("Refresh token expired.")
//      }
//
//      else                                                                           -> {
//        when (val auth = getTokenFromRefreshToken(tokenData.refreshToken)) {
//          is AccessStatusResponse.Error   -> {
//            println("Refresh token not valid: ${auth.description}")
//          }
//
//          is AccessStatusResponse.OAuthSuccess -> {
//            println("Created new token using refresh token: ${auth.refreshToken}")
//            authTokenStore.save(
//              StoredTokenData(
//                accessToken = auth.accessToken,
//                accessTokenExpiresAt = clock.now() + auth.expiresIn.seconds,
//                refreshToken = auth.refreshToken,
//                refreshTokenExpiresAt = clock.now() + auth.refreshTokenExpiresIn.seconds,
//              )
//            )
//            return auth.accessToken
//          }
//        }
//      }
//    }

    val deviceCodeResponse = initiateAuth()

    println(
      """
      Requested auth.
      Visit ${deviceCodeResponse.verificationUri} and enter code ${deviceCodeResponse.userCode}
      """.trimIndent()
    )

    val auth = waitForAuth(deviceCodeResponse)

    val updatedTokenData = StoredTokenData(
      accessToken = auth.accessToken,
//      accessTokenExpiresAt = clock.now() + auth.expiresIn.seconds,
//      refreshToken = auth.refreshToken,
//      refreshTokenExpiresAt = clock.now() + auth.refreshTokenExpiresIn.seconds,
    )

    authTokenStore.save(updatedTokenData)

    check(checkToken(updatedTokenData.accessToken)) {
      "Token ${updatedTokenData.accessToken} is not valid"
    }

    return updatedTokenData.accessToken
  }

  private suspend fun checkToken(token: GitHubAuthToken): Boolean {
    if (statusCache[token.token] == true) {
      return true
    }

    val response = httpClient.get("https://api.github.com/user") {
      headers {
        bearerAuth(token.token)
        accept(GitHubClientAcceptHeader)
        gitHubApiVersion()
      }
    }
    if (response.status.isSuccess()) {
      statusCache[token.token] = true
    } else {
      println("Checked token $token, response ${response.status} ${response.bodyAsText()}")
    }
    return response.status.isSuccess()
  }

  private suspend fun getTokenFromRefreshToken(refreshToken: String): AccessStatusResponse {
    val response = httpClient.submitForm(
      "https://github.com/login/oauth/access_token",
      formParameters = parameters {
        append("client_id", CLIENT_ID)
        append("refresh_token", refreshToken)
        append("grant_type", "refresh_token")
      }
    )

    return response.body()
  }

  private suspend fun initiateAuth(): DeviceCodeResponse.Success {
    val response = httpClient.submitForm(
      "https://github.com/login/device/code",
      formParameters = parameters {
        append("client_id", CLIENT_ID)
        append("scope", "repo")
//        append("scope", "repo,workflow")
      }
    )

    val data = response.body<DeviceCodeResponse>()
    return when (data) {
      is DeviceCodeResponse.Error   -> error("Failed to initiate auth: ${data.content}")
      is DeviceCodeResponse.Success -> data
    }
  }


  private suspend fun waitForAuth(
    deviceCodeResponse: DeviceCodeResponse.Success,
  ): AccessStatusResponse.OAuthSuccess {
    print("Waiting for auth to complete...")
    while (true) {
      delay(deviceCodeResponse.interval * 1.1)
      print(".")

      when (val auth = checkForAuth(deviceCodeResponse)) {
        is AccessStatusResponse.OAuthSuccess -> {
          println()
          println("Received auth token.")
          return auth
        }

        is AccessStatusResponse.Error        -> {
          when (auth.error) {
            AuthorizationPending ->
              continue

            SlowDown,
            ExpiredToken,
            UnsupportedGrantType,
            IncorrectClientCredentials,
            IncorrectDeviceCode,
            AccessDenied,
            DeviceFlowDisabled   ->
              error("Failed to get auth token: ${auth.description}")
          }
        }
      }
    }
  }


  private suspend fun checkForAuth(
    deviceCodeResponse: DeviceCodeResponse.Success,
  ): AccessStatusResponse {
    val response = httpClient.submitForm(
      "https://github.com/login/oauth/access_token",
      formParameters = parameters {
        append("client_id", CLIENT_ID)
        append("device_code", deviceCodeResponse.deviceCode)
        append("grant_type", ACCESS_TOKEN_GRANT_TYPE)
      }
    )

    return response.body()
  }
}


private const val ACCESS_TOKEN_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code"
private const val CLIENT_ID = "Ov23liVvsLA8nHywWI8e"
//private const val CLIENT_ID = "Iv23liiYfbuggYH28j7O"

/**
 *
 */
@Serializable(with = DeviceCodeResponse.Serializer::class)
internal sealed interface DeviceCodeResponse {

  @Serializable
  data class Success internal constructor(
    /**
     * The device verification code is 40 characters and used to verify the device.
     */
    @SerialName("device_code")
    val deviceCode: String,

    /**
     * The user verification code is displayed on the device so the user can enter the code in a browser.
     * This code is 8 characters with a hyphen in the middle.
     */
    @SerialName("user_code")
    val userCode: String,

    /**
     * The verification URL where users need to enter the user_code: https://github.com/login/device.
     */
    @SerialName("verification_uri")
    val verificationUri: String = "https://github.com/login/device",

    /**
     * The number of seconds before the [deviceCode] and [userCode] expire.
     * The default is 900 seconds or 15 minutes.
     */
    @SerialName("expires_in")
    val expiresInSeconds: Long,

    /**
     * The minimum number of seconds that must pass before you can make a new access token request
     * (`POST https://github.com/login/oauth/access_token`)
     * to complete the device authorization.
     *
     * For example, if the interval is 5, then you cannot make a new request until 5 seconds pass.
     * If you make more than one request over 5 seconds, then you will hit the rate limit and receive a `slow_down` error.
     */
    @SerialName("interval")
    val intervalSeconds: Long,
  ) : DeviceCodeResponse {
    val expires: Duration get() = expiresInSeconds.seconds
    val interval: Duration get() = intervalSeconds.seconds
  }

  @JvmInline
  @Serializable
  value class Error internal constructor(
    val content: JsonObject,
  ) : DeviceCodeResponse

  object Serializer : JsonContentPolymorphicSerializer<DeviceCodeResponse>(DeviceCodeResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<DeviceCodeResponse> {
      return if (element !is JsonObject || "error" in element) {
        Error.serializer()
      } else {
        Success.serializer()
      }
    }
  }
}


@Serializable(with = AccessStatusResponse.Serializer::class)
internal sealed class AccessStatusResponse() {

//  @Serializable
//  internal data class AppSuccess internal constructor(
//    @SerialName("access_token")
//    val accessToken: GitHubAuthToken,
//    @SerialName("expires_in")
//    val expiresIn: Long,
//    @SerialName("refresh_token")
//    val refreshToken: String,
//    @SerialName("refresh_token_expires_in")
//    val refreshTokenExpiresIn: Long,
//    @SerialName("token_type")
//    val tokenType: String,
//    val scope: String,
//  ) : AccessStatusResponse()

  @Serializable
  internal data class OAuthSuccess internal constructor(
    @SerialName("access_token")
    val accessToken: GitHubAuthToken,
//    @SerialName("expires_in")
//    val expiresIn: Long,
//    @SerialName("refresh_token")
//    val refreshToken: String,
//    @SerialName("refresh_token_expires_in")
//    val refreshTokenExpiresIn: Long,
    @SerialName("token_type")
    val tokenType: String,
    val scope: String,
  ) : AccessStatusResponse()

  @Serializable
  internal data class Error internal constructor(
    val error: Code,
    @SerialName("error_description")
    val description: String,
    @SerialName("error_uri")
    val uri: String,
  ) : AccessStatusResponse() {
    @Serializable
    enum class Code {
      /**
       * This error occurs when the authorization request is pending and the user hasn't entered the user code yet.
       * The app is expected to keep polling the `POST https://github.com/login/oauth/access_token`
       * request without exceeding the interval,
       * which requires a minimum number of seconds between each request.
       */
      @SerialName("authorization_pending")
      AuthorizationPending,
      /**
       * When you receive the `slow_down` error, 5 extra seconds are added to the minimum interval or timeframe
       * required between your requests using `POST https://github.com/login/oauth/access_token`.
       *
       * For example, if the starting interval required at least 5 seconds between requests,
       * and you get a `slow_down` error response, you must now wait a minimum of 10 seconds
       * before making a new request for an OAuth access token.
       *
       * The error response includes the new interval that you must use.
       */
      @SerialName("slow_down")
      SlowDown,
      /**
       * If the device code expired, then you will see the `token_expired` error.
       * You must make a new request for a device code.
       */
      @SerialName("expired_token")
      ExpiredToken,
      /**
       * The grant type must be `urn:ietf:params:oauth:grant-type:device_code`
       * and included as an input parameter when you poll the OAuth token request
       * `POST https://github.com/login/oauth/access_token`.
       */
      @SerialName("unsupported_grant_type")
      UnsupportedGrantType,
      /**
       * For the device flow, you must pass your app's client ID, which you can find on your app settings page.
       * The `client_secret` is not needed for the device flow.
       */
      @SerialName("incorrect_client_credentials")
      IncorrectClientCredentials,
      /** The `device_code` provided is not valid. */
      @SerialName("incorrect_device_code")
      IncorrectDeviceCode,
      /**
       * When a user clicks cancel during the authorization process,
       * you'll receive a `access_denied` error and the user won't be able to use the verification code again.
       */
      @SerialName("access_denied")
      AccessDenied,
      /**
       * Device flow has not been enabled in the app's settings.
       * For more information, see [Device flow](https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/authorizing-oauth-apps#device-flow).
       */
      @SerialName("device_flow_disabled")
      DeviceFlowDisabled,
    }
  }

  object Serializer : JsonContentPolymorphicSerializer<AccessStatusResponse>(
    AccessStatusResponse::class
  ) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AccessStatusResponse> {
      return when {
        element is JsonObject && "error" in element -> Error.serializer()
//        else                                        -> Success.serializer()
        else                                        -> OAuthSuccess.serializer()
      }
    }
  }
}
