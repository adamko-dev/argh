package dev.adamko.githubassetpublish

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.*

fun RepositoryHandler.githubReleaseAssets() {
  ivy("https://github.com/") {
    name = "GitHub Release Assets"
    patternLayout {
      setM2compatible(true)
      artifact("[organisation]/releases/download/v[revision]/[module]-[revision](-[classifier]).[ext]")
    }
    metadataSources {
      gradleMetadata()
    }
  }
}
