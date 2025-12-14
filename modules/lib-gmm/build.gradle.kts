plugins {
  buildsrc.`kotlin-lib`
  kotlin("plugin.serialization")
}

description = "Internal Argh library. Gradle Module Metadata data model."
mavenPublishing.pomName = "Argh | Gradle Module Metadata"

dependencies {
  implementation(libs.kotlinx.serialization.json)
}
