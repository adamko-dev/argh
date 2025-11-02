plugins {
  buildsrc.`kotlin-lib`
  kotlin("plugin.serialization")
}

dependencies {
  implementation(libs.kotlinx.serialization.json)
}
