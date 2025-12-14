@file:Suppress("UnstableApiUsage")

package buildsrc

import buildsrc.settings.MavenPublishingSettings.Companion.mavenPublishing

plugins {
  id("buildsrc.kotlin-base")
  id("buildsrc.maven-publishing")
  `java-gradle-plugin`
}

gradlePlugin {
  isAutomatedPublishing = true
}

tasks.validatePlugins {
  enableStricterValidation.set(true)
}

gradlePlugin.plugins.configureEach {
  description = project.description
  displayName = mavenPublishing.pomName.get()
}

kotlin {
  compilerOptions {
    // Add compiler arguments for building Gradle plugins.
    // https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin_compiler_arguments
    freeCompilerArgs.addAll(
      "-java-parameters",
      "-Xjvm-default=all",
      "-Xsam-conversions=class",
      "-Xjsr305=strict",
      "-Xjspecify-annotations=strict",
    )
  }
}

dependencies {
  compileOnly(gradleApi())
  compileOnly(gradleKotlinDsl())
  compileOnly(kotlin("stdlib"))
}


// kotlin-stdlib should be excluded for Gradle plugins because it will be provided at runtime by Gradle.
configurations
  .matching {
    it.isCanBeDeclared
        && !it.isCanBeResolved
        && !it.isCanBeConsumed
        && it.name != JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME
        && it.name in setOf(
      JavaPlugin.API_CONFIGURATION_NAME,
      JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
      JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME,
      JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME,
    )
  }
  .configureEach {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
  }
