package dev.adamko.githubassetpublish

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.*

fun RepositoryHandler.githubReleaseAssets() {
  ivy("https://github.com/") {
    name = "GitHubReleaseAssets"
    patternLayout {
      ivy("[orgPath]/releases/download/v[revision]/[module]-[revision].ivy.xml")
      ivy("[orgPath]/releases/download/v[revision]/[module]-[revision].module")

      artifact("[orgPath]/releases/download/v[revision]/[artifact]-[revision](-[classifier]).[ext]")
    }
    metadataSources {
      gradleMetadata()
    }
  }
}
