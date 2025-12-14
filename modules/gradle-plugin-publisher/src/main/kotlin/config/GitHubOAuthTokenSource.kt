package dev.adamko.argh.gradle.publisher.config

import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.invariantSeparatorsPathString

sealed class GitHubOAuthTokenSource {

  internal fun encodeAsArg(): String {
    return when (this) {
      is EnvVar -> "EnvVar"
      is File   -> "File:${file.absolute().invariantSeparatorsPathString}"
    }
  }

  /**
   * Use the `GITHUB_TOKEN` environment variable to get the GitHub Token.
   */
  data object EnvVar : GitHubOAuthTokenSource() {

  }

  class File(
    val file: Path,
  ) : GitHubOAuthTokenSource()

}
