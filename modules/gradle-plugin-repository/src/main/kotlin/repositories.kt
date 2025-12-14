package dev.adamko.argh.gradle.repository

import org.gradle.api.Action
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor
import org.gradle.kotlin.dsl.*

/**
 * Add a repository that will use GitHub Release Assets as a repository.
 */
fun RepositoryHandler.arghGitHubReleaseAssets(
  contentConfig: Action<RepositoryContentDescriptor> = Action {},
) {
  ivy("https://github.com/") {
    name = "Argh GitHub Release Assets"
    patternLayout { layout ->
      layout.ivy("[orgPath]/releases/download/v[revision]/[module]-[revision].ivy.xml")
      layout.ivy("[orgPath]/releases/download/v[revision]/[module]-[revision].module")

      layout.artifact("[orgPath]/releases/download/v[revision]/[artifact]-[revision](-[classifier]).[ext]")
    }
    metadataSources.gradleMetadata()
    content {
      contentConfig.execute(it)
    }
  }
}
