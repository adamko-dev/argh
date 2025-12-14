plugins {
  buildsrc.`kotlin-gradle-plugin`
}

description =
  "This Gradle plugin resolves artifacts published to GitHub Release Assets by the Argh publisher plugins."
mavenPublishing.pomName = "Argh | Gradle Repository Plugin"

gradlePlugin {
  plugins {
    register("githubAssetRepository") {
      id = "dev.adamko.argh.repository"
      implementationClass = "dev.adamko.argh.gradle.repository.ArghRepositoryPlugin"
    }
  }
}
