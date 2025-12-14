@file:OptIn(ExperimentalSerializationApi::class)

package dev.adamko.githubapiclient.auth

import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

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
      } catch (_: IOException) {
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
