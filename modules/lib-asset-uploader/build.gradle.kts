plugins {
  buildsrc.`kotlin-lib`
}

dependencies {
  implementation(projects.modules.libGmm)

  implementation(libs.kotlinx.serialization.json)
}
