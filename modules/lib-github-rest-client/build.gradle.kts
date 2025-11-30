plugins {
  buildsrc.`kotlin-lib`
  kotlin("plugin.serialization")
}

dependencies {
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.ktorClient.core)
  implementation(libs.ktorClient.cio)
  implementation(libs.ktorClient.logging)
  implementation(libs.ktorClient.resources)
  implementation(libs.ktorClient.contentNegotiation)

  implementation(libs.ktorSerialization.kotlinxJson)
}
