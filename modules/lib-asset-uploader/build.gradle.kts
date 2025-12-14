plugins {
  buildsrc.`kotlin-lib`
}

description = "Internal Argh library. A shared library for handling GitHub Asset publications."
mavenPublishing.pomName = "Argh | GitHub uploader"

dependencies {
  implementation(projects.modules.libGmm)
  implementation(projects.modules.libAssetUploaderApi)
  implementation(projects.modules.libGithubRestClient)

  implementation(projects.modules.libUtils)

  implementation(libs.kotlinx.serialization.json)

  implementation(libs.kotlinx.coroutines)
}

kotlin {
  compilerOptions {
    optIn.add("dev.adamko.argh.lib.utils.InternalUtilsApi")
  }
}
