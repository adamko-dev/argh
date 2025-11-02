package dev.adamko.githubassetpublish

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.*

/**
 * Add a repository that will use GitHub Release Assets as a repository.
 */
fun RepositoryHandler.githubReleaseAssets() {
  ivy("https://github.com/") {
    name = "GitHubReleaseAssets"
    patternLayout { layout ->
      layout.ivy("[orgPath]/releases/download/v[revision]/[module]-[revision].ivy.xml")
      layout.ivy("[orgPath]/releases/download/v[revision]/[module]-[revision].module")

      layout.artifact("[orgPath]/releases/download/v[revision]/[artifact]-[revision](-[classifier]).[ext]")
    }
    metadataSources.gradleMetadata()
  }
}
