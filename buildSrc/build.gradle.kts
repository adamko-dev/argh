plugins {
  `kotlin-dsl`
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${embeddedKotlinVersion}")
  implementation("org.jetbrains.kotlin:kotlin-serialization:${embeddedKotlinVersion}")

  implementation("dev.adamko.gradle:dev-publish-plugin:0.4.2")

  implementation("io.github.hfhbd.kfx:gradle-plugin:0.1.5")
}
