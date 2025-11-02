plugins {
  buildsrc.`kotlin-gradle-plugin`
}

description = "Support for resolving artifacts from GitHub Assets."

gradlePlugin {
  plugins {
    register("githubAssetRepository") {
      id = "dev.adamko.github-asset-repository"
      implementationClass = "dev.adamko.githubassetpublish.GitHubAssetRepositoryPlugin"
    }
  }
}
