plugins {
  buildsrc.`kotlin-lib`
//  kotlin("plugin.serialization")
}

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
    optIn.add("dev.adamko.githubassetpublish.lib.utils.InternalUtilsApi")
  }
}
