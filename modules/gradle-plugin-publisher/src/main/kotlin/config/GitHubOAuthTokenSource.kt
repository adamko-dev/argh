package dev.adamko.argh.gradle.publisher.config

sealed class GitHubOAuthTokenSource {

  /**
   * Use the `GITHUB_TOKEN` environment variable to get the GitHub Token.
   */
  data object EnvVar : GitHubOAuthTokenSource()

}
