plugins {
  buildsrc.`kotlin-lib`
  kotlin("plugin.serialization")
}

description = "Internal Argh library. Gradle Module Metadata data model."

dependencies {
  implementation(libs.kotlinx.serialization.json)
}
