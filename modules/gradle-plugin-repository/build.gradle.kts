plugins {
  buildsrc.`kotlin-gradle-plugin`
}

description =
  "This Gradle plugin resolves artifacts published to GitHub Release Assets by the Argh publisher plugins."

gradlePlugin {
  plugins {
    register("githubAssetRepository") {
      id = "dev.adamko.argh.repository"
      implementationClass = "dev.adamko.argh.gradle.repository.ArghRepositoryPlugin"
    }
  }
}
