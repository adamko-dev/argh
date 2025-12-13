plugins {
  buildsrc.`kotlin-gradle-plugin`
}

description = "Support for resolving artifacts from GitHub Assets."

gradlePlugin {
  plugins {
    register("githubAssetRepository") {
      id = "dev.adamko.argh.repository"
      implementationClass = "dev.adamko.argh.repository.GitHubAssetRepositoryPlugin"
    }
  }
}
