package dev.adamko.githubapiclient.endpoints.repos

import dev.adamko.githubapiclient.model.RepoRelease
import io.ktor.resources.*
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#create-a-release
data object CreateRelease {

  @Resource("/repos/{owner}/{repo}/releases")
  class Route internal constructor(
    val owner: String,
    val repo: String,
  )

  @Serializable
  data class RequestBody internal constructor(
    /** The name of the tag. */
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("target_commitish")
    val targetCommitish: String? = null,
    val name: String? = null,
    val body: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    @SerialName("discussion_category_name")
    val discussionCategoryName: String? = null,
    @SerialName("generate_release_notes")
    val generateReleaseNotes: Boolean = false,
    @SerialName("make_latest")
    val makeLatest: MakeLatest? = null
  ) {

    @Serializable
    enum class MakeLatest {
      @SerialName("true")
      True,
      @SerialName("false")
      False,
      @SerialName("legacy")
      Legacy,
    }
  }

  typealias ResponseBody = RepoRelease

//  @Serializable
//  @JvmInline
//  value class ResponseBody(
//    val content: List<RepoRelease>
//  )
}
