package dev.adamko.githubapiclient.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A GitHub user. */
@Serializable
data class GitHubUser internal constructor(
  //region required properties
  val id: Long,
  val type: String,
  val login: String,
  @SerialName("node_id")
  val nodeId: String,

  @SerialName("gravatar_id")
  val gravatarId: String?,
  @SerialName("site_admin")
  val siteAdmin: Boolean,

  val url: String,
  @SerialName("html_url")
  val htmlUrl: String,
  @SerialName("avatar_url")
  val avatarUrl: String,
  @SerialName("repos_url")
  val reposUrl: String,
  @SerialName("subscriptions_url")
  val subscriptionsUrl: String,
  @SerialName("starred_url")
  val starredUrl: String,
  @SerialName("organizations_url")
  val organizationsUrl: String,
  @SerialName("received_events_url")
  val receivedEventsUrl: String,
  @SerialName("events_url")
  val eventsUrl: String,
  @SerialName("followers_url")
  val followersUrl: String,
  @SerialName("following_url")
  val followingUrl: String,
  @SerialName("gists_url")
  val gistsUrl: String,
  //endregion

  val name: String? = null,
  val email: String? = null,
  @SerialName("starred_at")
  val starredAt: String? = null,
  @SerialName("user_view_type")
  val userViewType: String? = null,
)
