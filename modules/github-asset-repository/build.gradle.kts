plugins {
  `kotlin-dsl`
  `maven-publish`
}

group = rootProject.group
version = rootProject.version

description =
  "Utility plugin. Does nothing by itself. Just provides a utility function for adding GitHub Asset repository."

gradlePlugin {
  plugins {
    create("githubAssetRepository") {
      id = "dev.adamko.github-asset-repository"
      implementationClass = "dev.adamko.githubassetpublish.GitHubAssetRepositoryPlugin"
    }
    create("githubAssetRepositorySettings") {
      id = "dev.adamko.github-asset-repository-settings"
      implementationClass = "dev.adamko.githubassetpublish.GitHubAssetRepositorySettingsPlugin"
    }
  }
}
