plugins {
  buildsrc.`kotlin-lib`
  kotlin("plugin.serialization")
}

description = "Internal Argh library. GitHub REST API client."
mavenPublishing.pomName = "Argh | GitHub REST API client"

dependencies {
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.ktorClient.core)
  implementation(libs.ktorClient.cio)
  implementation(libs.ktorClient.logging)
  implementation(libs.ktorClient.resources)
  implementation(libs.ktorClient.contentNegotiation)
  implementation(libs.ktorClient.auth)

  implementation(libs.ktorSerialization.kotlinxJson)

  implementation(libs.slf4j.simple)
}
