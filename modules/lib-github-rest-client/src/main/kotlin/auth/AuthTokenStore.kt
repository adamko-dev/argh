@file:OptIn(ExperimentalSerializationApi::class)

package dev.adamko.githubapiclient.auth

import dev.adamko.githubapiclient.auth.StoredTokenData.Companion.saveTo
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission.OWNER_READ
import java.nio.file.attribute.PosixFilePermission.OWNER_WRITE
import kotlin.io.path.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

internal class AuthTokenStore(
  private val tokenDataFile: Path,
) {
  private val mutex: Mutex = Mutex(locked = false)

  suspend fun load(): StoredTokenData? = mutex.withLock {
    return StoredTokenData.loadFrom(tokenDataFile)
  }

  suspend fun save(token: StoredTokenData): Unit = mutex.withLock {
    token.saveTo(tokenDataFile)
  }
}


@Serializable
@JvmInline
internal value class GitHubAuthToken(val token: String) {
  override fun toString(): String = "GitHubAuthToken(${token.hashCode()})"
}

@Serializable
internal data class StoredTokenData internal constructor(
  val accessToken: GitHubAuthToken,
//  val accessTokenExpiresAt: Instant,
//  val refreshToken: String,
//  val refreshTokenExpiresAt: Instant,
) {
  companion object {
    private val json: Json = Json {
      prettyPrint = true
      prettyPrintIndent = "  "
    }

    fun loadFrom(file: Path): StoredTokenData? {
      return try {
        file.inputStream().use { source ->
          json.decodeFromStream(serializer(), source)
        }
      } catch (_: IllegalArgumentException) {
        null
      } catch (_: java.io.IOException) {
        null
      }
    }

    fun StoredTokenData.saveTo(file: Path) {
      if (!file.exists()) {
        file.parent.createDirectories()
        file.createFile()
        // set permissions to 600 (rw-------)
        file.setPosixFilePermissions(setOf(OWNER_READ, OWNER_WRITE))
      }

      file.outputStream().use { sink ->
        json.encodeToStream(serializer(), this, sink)
      }
    }
  }
}
