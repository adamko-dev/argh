package dev.adamko.githubapiclient.endpoints.user

import io.ktor.resources.*
import kotlinx.serialization.json.JsonElement

data object GetUser {
  @Resource("/user")
  class Route

  //  typealias ResponseBody = GitHubUser2
  typealias ResponseBody = JsonElement
}
