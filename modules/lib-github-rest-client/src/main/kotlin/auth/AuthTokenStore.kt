package dev.adamko.githubapiclient.auth

import dev.adamko.githubapiclient.auth.StoredTokenData.Companion.saveTo
import java.nio.file.Path
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
