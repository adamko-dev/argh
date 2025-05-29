@file:Suppress("UnstableApiUsage")

rootProject.name = "github-asset-publish"

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.PREFER_SETTINGS
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

include(
  ":modules:github-asset-publish",
  ":modules:github-asset-repository",
)
