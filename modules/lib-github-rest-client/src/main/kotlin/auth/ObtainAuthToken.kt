package dev.adamko.githubapiclient.auth

import dev.adamko.githubapiclient.auth.AccessStatusResponse.Error.Code.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

internal class ObtainAuthToken(
  pluginCacheDir: Path,
  private val httpClient: HttpClient = HttpClient(CIO) {
    defaultRequest {
      accept(ContentType.Application.Json)
    }
  }
) {
  private val tokenDataFile: Path = pluginCacheDir.resolve("gh-token-data.json")

  /**
   * Is there a `GITHUB_TOKEN` environment variable? If so, use it.
   *
   * Otherwise, check the config dir for a stored token.
   *
   * Is the stored token expired? Request a new one using the refresh token.
   *
   * Otherwise, request a new token (and refresh token) using the device code flow.
   */
  suspend fun action(): GitHubAuthToken {

    val envToken = System.getenv("GITHUB_TOKEN")
    if (envToken != null) {
      return GitHubAuthToken(envToken)
    }

    val tokenData = if (tokenDataFile.exists()) {
      try {
        Json.decodeFromString(StoredTokenData.serializer(), tokenDataFile.readText())
      } catch (ex: IllegalArgumentException) {
        println("Failed to read token data from ${tokenDataFile.toAbsolutePath()}.")
        ex.printStackTrace()
        null
      }
    } else {
      null
    }

    if (
      tokenData != null
      && tokenData.accessTokenExpiresAt < Clock.System.now().minus(30.seconds)
      && checkToken(tokenData.accessToken)
    ) {
      return tokenData.accessToken
    }

    if (
      tokenData != null
      && tokenData.refreshTokenExpiresAt < Clock.System.now().minus(30.seconds)
    ) {
      when (val t = getTokenFromRefreshToken(tokenData.refreshToken)) {
        is AccessStatusResponse.Error   -> {
          println("Refresh token not valid: ${t.description}")
        }

        is AccessStatusResponse.Success -> {
          val updatedTokenData =
            tokenData.copy(
              accessToken = t.accessToken,
            )
          tokenDataFile.writeText(
            Json.encodeToString(StoredTokenData.serializer(), updatedTokenData)
          )
          return updatedTokenData.accessToken
        }
      }
    }

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
      accessTokenExpiresAt = Clock.System.now() + auth.expiresIn.seconds,
      refreshToken = auth.refreshToken,
      refreshTokenExpiresAt = Clock.System.now() + auth.refreshTokenExpiresIn.seconds,
    )

    tokenDataFile.writeText(Json.encodeToString(StoredTokenData.serializer(), updatedTokenData))

    return updatedTokenData.accessToken
  }

  private suspend fun checkToken(token: GitHubAuthToken): Boolean {
    val response = httpClient.get("https://api.github.com/user") {
      header("Authorization", "token $token")
    }
    return response.status.isSuccess()
  }

  private suspend fun getTokenFromRefreshToken(refreshToken: String): AccessStatusResponse {
    val response = httpClient.post("https://github.com/login/oauth/access_token") {
      formData {
        append("client_id", CLIENT_ID)
        append("refresh_token", refreshToken)
        append("grant_type", "refresh_token")
      }
    }

    return response.body()
  }

  private suspend fun initiateAuth(): DeviceCodeResponse {
    val response = httpClient.post("https://github.com/login/device/code") {
      formData {
        append("client_id", CLIENT_ID)
      }
    }

    return response.body()
  }


  private suspend fun waitForAuth(
    deviceCodeResponse: DeviceCodeResponse,
  ): AccessStatusResponse.Success {
    print("Waiting for auth to complete...")
    while (true) {
      delay(deviceCodeResponse.interval * 1.1)
      print(".")

      when (val auth = checkForAuth(deviceCodeResponse)) {
        is AccessStatusResponse.Success -> {
          println()
          println("Received auth token.")
          return auth
        }

        is AccessStatusResponse.Error   -> {
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
    deviceCodeResponse: DeviceCodeResponse,
  ): AccessStatusResponse {
    val response = httpClient.post("https://github.com/login/oauth/access_token") {
      formData {
        append("client_id", CLIENT_ID)
        append("device_code", deviceCodeResponse.deviceCode)
        append("grant_type", ACCESS_TOKEN_GRANT_TYPE)
      }
    }

    return response.body()
  }
}

@Serializable
@JvmInline
internal value class GitHubAuthToken(val token: CharSequence)

private const val ACCESS_TOKEN_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code"
private const val CLIENT_ID = "Iv23liiYfbuggYH28j7O"

/**
 *
 */
@Serializable
internal data class DeviceCodeResponse internal constructor(
  /**
   * The device verification code is 40 characters and used to verify the device.
   */
  @SerialName("device_code")
  val deviceCode: String,
  @SerialName("user_code")
  /**
   * The user verification code is displayed on the device so the user can enter the code in a browser.
   * This code is 8 characters with a hyphen in the middle.
   */
  val userCode: String,
  @SerialName("verification_uri")
  /**
   * The verification URL where users need to enter the user_code: https://github.com/login/device.
   */
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
) {
  val expires: Duration get() = expiresInSeconds.seconds
  val interval: Duration get() = intervalSeconds.seconds
}


@Serializable
internal sealed class AccessStatusResponse() {
  @Serializable
  internal data class Success internal constructor(
    @SerialName("access_token")
    val accessToken: GitHubAuthToken,
    @SerialName("expires_in")
    val expiresIn: Long,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("refresh_token_expires_in")
    val refreshTokenExpiresIn: Long,
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
        else                                        -> Success.serializer()
      }
    }
  }
}


internal data class asd(
  /**
   * The user access token. The token starts with `ghu_`.
   */
  @SerialName("access_token")
  val accessToken: String,
  /**
   * The number of seconds until [accessToken] expires. If you disabled expiration of user access tokens, this parameter will be omitted. The value will always be 28800 (8 hours). */
  @SerialName("expires_in")
  val expiresIn: Int,
  /**
   * The refresh token.
   * If you disabled expiration of user access tokens, this parameter will be omitted. The token starts with `ghr_`. */
  @SerialName("refresh_token")
  val refreshToken: String,
  /** The number of seconds until refresh_token expires. If you disabled expiration of user access tokens, this parameter will be omitted. The value will always be 15897600 (6 months). */
  @SerialName("refresh_token_expires_in")
  val refreshTokenExpiresIn: Integer,
  /** The scopes that the token has. This value will always be an empty string. Unlike a traditional OAuth token, the user access token is limited to the permissions that both your app and the user have. */
  val scope: String,
  /** The type of token. The value will always be `bearer`. */
  @SerialName("token_type")
  val tokenType: String,
)

@Serializable
internal data class StoredTokenData internal constructor(
  val accessToken: GitHubAuthToken,
  val accessTokenExpiresAt: Instant,
  val refreshToken: String,
  val refreshTokenExpiresAt: Instant,
)
