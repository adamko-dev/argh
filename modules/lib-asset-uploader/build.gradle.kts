plugins {
  buildsrc.`kotlin-lib`
//  kotlin("plugin.serialization")
}

dependencies {
  implementation(projects.modules.libGmm)
  implementation(projects.modules.libAssetUploaderApi)

  implementation(projects.modules.libUtils)

  implementation(libs.kotlinx.serialization.json)
}

kotlin {
  compilerOptions {
    optIn.add("dev.adamko.githubassetpublish.lib.utils.InternalUtilsApi")
  }
}
