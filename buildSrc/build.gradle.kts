plugins {
  `kotlin-dsl`
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${embeddedKotlinVersion}")
  implementation("org.jetbrains.kotlin:kotlin-serialization:${embeddedKotlinVersion}")

  implementation(libs.gradlePlugin.devPublish)
  implementation(libs.gradlePlugin.dokka)
  implementation(libs.gradlePlugin.nmcp)
}
