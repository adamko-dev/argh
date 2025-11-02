plugins {
  `kotlin-dsl`
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${embeddedKotlinVersion}")
  implementation("org.jetbrains.kotlin:kotlin-serialization:${embeddedKotlinVersion}")

  implementation("org.gradlex:maven-plugin-development:1.0.3")
}
