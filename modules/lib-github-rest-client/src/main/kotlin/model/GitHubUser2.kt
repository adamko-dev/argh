package dev.adamko.githubapiclient.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class GitHubUser2 {

  abstract val id: Long

  @SerialName("avatar_url")
  abstract val avatarUrl: String
  @SerialName("events_url")
  abstract val eventsUrl: String
  @SerialName("followers_url")
  abstract val followersUrl: String
  @SerialName("following_url")
  abstract val followingUrl: String
  @SerialName("gists_url")
  abstract val gistsUrl: String
  @SerialName("gravatar_id")
  abstract val gravatarId: String?
  @SerialName("html_url")
  abstract val htmlUrl: String

  @SerialName("created_at")
  abstract val createdAt: String
  @SerialName("updated_at")
  abstract val updatedAt: String

  abstract val bio: String?
  abstract val blog: String?
  abstract val collaborators: Int?
  abstract val company: String?
  @SerialName("disk_usage")
  abstract val diskUsage: Int
  abstract val email: String?
  abstract val followers: Int
  abstract val following: Int
  abstract val hireable: Boolean?
  abstract val location: String?
  abstract val login: String
  abstract val name: String?
  @SerialName("node_id")
  abstract val nodeId: String
  @SerialName("notification_email")
  abstract val notificationEmail: String?
  @SerialName("organizations_url")
  abstract val organizationsUrl: String
  @SerialName("owned_private_repos")
  abstract val ownedPrivateRepos: Int?
  abstract val plan: Plan?
  @SerialName("private_gists")
  abstract val privateGists: Int?
  @SerialName("public_gists")
  abstract val publicGists: Int
  @SerialName("public_repos")
  abstract val publicRepos: Int
  @SerialName("received_events_url")
  abstract val receivedEventsUrl: String
  @SerialName("repos_url")
  abstract val reposUrl: String
  @SerialName("site_admin")
  abstract val siteAdmin: Boolean
  @SerialName("starred_url")
  abstract val starredUrl: String
  @SerialName("subscriptions_url")
  abstract val subscriptionsUrl: String
  @SerialName("total_private_repos")
  abstract val totalPrivateRepos: Int?
  @SerialName("twitter_username")
  abstract val twitterUsername: String?
  abstract val type: String
  abstract val url: String
  @SerialName("user_view_type")
  abstract val userViewType: String?

  @Serializable
  data class PrivateUser(
    //region required properties
    @SerialName("avatar_url")
    override val avatarUrl: String,
    @SerialName("events_url")
    override val eventsUrl: String,
    @SerialName("followers_url")
    override val followersUrl: String,
    @SerialName("following_url")
    override val followingUrl: String,
    @SerialName("gists_url")
    override val gistsUrl: String,
    @SerialName("gravatar_id")
    override val gravatarId: String?,
    @SerialName("html_url")
    override val htmlUrl: String,
    override val id: Long,
    @SerialName("node_id")
    override val nodeId: String,
    override val login: String,
    @SerialName("organizations_url")
    override val organizationsUrl: String,
    @SerialName("received_events_url")
    override val receivedEventsUrl: String,
    @SerialName("repos_url")
    override val reposUrl: String,
    @SerialName("site_admin")
    override val siteAdmin: Boolean,
    @SerialName("starred_url")
    override val starredUrl: String,
    @SerialName("subscriptions_url")
    override val subscriptionsUrl: String,
    override val type: String,
    override val url: String,
    override val bio: String? = null,
    override val blog: String? = null,
    override val company: String? = null,
    override val email: String? = null,
    override val followers: Int,
    override val following: Int,
    override val hireable: Boolean? = null,
    override val location: String? = null,
    override val name: String? = null,
    @SerialName("public_gists")
    override val publicGists: Int,
    @SerialName("public_repos")
    override val publicRepos: Int,
    @SerialName("created_at")
    override val createdAt: String,
    @SerialName("updated_at")
    override val updatedAt: String,
    override val collaborators: Int,
    @SerialName("disk_usage")
    override val diskUsage: Int,
    @SerialName("owned_private_repos")
    override val ownedPrivateRepos: Int,
    @SerialName("private_gists")
    override val privateGists: Int,
    @SerialName("total_private_repos")
    override val totalPrivateRepos: Int,
    @SerialName("two_factor_authentication")
    val twoFactorAuthentication: Boolean,
    //endregion

    @SerialName("user_view_type")
    override val userViewType: String? = null,
    @SerialName("twitter_username")
    override val twitterUsername: String? = null,
    @SerialName("notification_email")
    override val notificationEmail: String? = null,
    val ldapDn: String? = null,
    override val plan: Plan? = null,
    val businessPlus: Boolean? = null,
  ) : GitHubUser2() {
  }

  @Serializable
  data class PublicUser(
    //region required properties
    @SerialName("avatar_url")
    override val avatarUrl: String,
    @SerialName("events_url")
    override val eventsUrl: String,
    @SerialName("followers_url")
    override val followersUrl: String,
    @SerialName("following_url")
    override val followingUrl: String,
    @SerialName("gists_url")
    override val gistsUrl: String,
    @SerialName("gravatar_id")
    override val gravatarId: String? = null,
    @SerialName("html_url")
    override val htmlUrl: String,
    override val id: Long,
    @SerialName("node_id")
    override val nodeId: String,
    override val login: String,
    @SerialName("organizations_url")
    override val organizationsUrl: String,
    @SerialName("received_events_url")
    override val receivedEventsUrl: String,
    @SerialName("repos_url")
    override val reposUrl: String,
    @SerialName("site_admin")
    override val siteAdmin: Boolean,
    @SerialName("starred_url")
    override val starredUrl: String,
    @SerialName("subscriptions_url")
    override val subscriptionsUrl: String,
    override val type: String,
    override val url: String,
    override val bio: String?,
    override val blog: String?,
    override val company: String?,
    override val email: String?,
    override val followers: Int,
    override val following: Int,
    override val hireable: Boolean?,
    override val location: String?,
    override val name: String?,
    @SerialName("public_gists")
    override val publicGists: Int,
    @SerialName("public_repos")
    override val publicRepos: Int,
    @SerialName("created_at")
    override val createdAt: String,
    @SerialName("updated_at")
    override val updatedAt: String,

    //endregion

    @SerialName("notification_email")
    override val notificationEmail: String? = null,
    @SerialName("user_view_type")
    override val userViewType: String? = null,
    @SerialName("twitter_username")
    override val twitterUsername: String? = null,
    override val collaborators: Int? = null,
    @SerialName("disk_usage")
    override val diskUsage: Int,
    override val plan: Plan? = null,
    @SerialName("private_gists")
    override val privateGists: Int? = null,
    @SerialName("total_private_repos")
    override val totalPrivateRepos: Int? = null,
    @SerialName("owned_private_repos")
    override val ownedPrivateRepos: Int? = null,
  ) : GitHubUser2()

  @Serializable
  data class Plan(
    val collaborators: Int,
    val name: String,
    val space: Int,
    @SerialName("private_repos")
    val privateRepos: Int,
  )
}
