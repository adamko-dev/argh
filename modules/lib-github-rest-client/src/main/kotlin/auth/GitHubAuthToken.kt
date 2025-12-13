package dev.adamko.githubapiclient.auth

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
internal value class GitHubAuthToken(val token: String) {
  override fun toString(): String = "GitHubAuthToken(${token.hashCode()})"
}
