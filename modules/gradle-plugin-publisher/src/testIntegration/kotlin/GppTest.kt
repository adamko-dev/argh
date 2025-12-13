package dev.adamko.argh.gradle.publisher

import java.nio.file.Path
import kotlin.io.path.*
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertLinesMatch
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class GppTest {

  @Test
  fun testJavaLibraryProject(
    @TempDir
    projectDir: Path
  ) {
    projectDir.resolve("gradle.properties").writeText(
      """
      |org.gradle.jvmargs=-Dfile.encoding=UTF-8
      |org.gradle.configuration-cache=true
      |org.gradle.parallel=true
      |org.gradle.caching=true
      |""".trimIndent()
    )

    projectDir.resolve("settings.gradle.kts").writeText(
      """
      |rootProject.name = "test-project"
      |
      |pluginManagement {
      |  repositories {
      |    maven(file("${devMavenRepo.invariantSeparatorsPathString}"))
      |    mavenCentral()
      |    gradlePluginPortal()
      |  }
      |}
      |
      |dependencyResolutionManagement {
      |  repositoriesMode = RepositoriesMode.PREFER_SETTINGS
      |  repositories {
      |    maven(file("${devMavenRepo.invariantSeparatorsPathString}"))
      |    mavenCentral()
      |    gradlePluginPortal()
      |  }
      |}
      |""".trimMargin()
    )

    projectDir.resolve("build.gradle.kts").writeText(
      """
      |plugins {
      |  `java-library`
      |  id("dev.adamko.argh.publisher") version "+"
      |  `maven-publish`
      |}
      |
      |group = "aSemy.demo-github-asset-publish-repo"
      |version = "1.0.0"
      |
      |publishing {
      |  publications {
      |    register<MavenPublication>("maven") {
      |      from(components["java"])
      |    }
      |  }
      |}
      |
      |java {
      |  withJavadocJar()
      |  withSourcesJar()
      |}
      |
      |tasks.withType<dev.adamko.argh.gradle.publisher.tasks.UploadGitHubReleaseAssetsTask>().configureEach {
      |  githubRepo.set("aSemy/demo-github-asset-publish-repo")
      |}
      |""".trimMargin()
    )

    projectDir.resolve("src/main/java/com/example/Demo.java")
      .createParentDirectories()
      .writeText(
        """
        |package com.example;
        |
        |/** Demonstration class. */
        |public class Demo {}
        |""".trimMargin()
      )

    GradleRunner.create()
      .withProjectDir(projectDir.toFile())
      .withArguments(
        "uploadGitHubReleaseAssets",
        "--skipGitHubUpload",
        "--stacktrace",
      )
      .build()

    val githubReleaseFilesDir = projectDir.resolve("build/tmp/prepareAssetsTask/")

    val githubReleaseFiles = githubReleaseFilesDir
      .walk()
      .sorted()
      .map { f ->
        val path = f.relativeTo(githubReleaseFilesDir).invariantSeparatorsPathString
        buildString {
          append(path)
          if (!f.isRegularFile()) {
            append(" ${f.describeType()}")
          }
        }
      }
      .toList()

    assertLinesMatch(
      listOf(
        "test-project-1.0.0-sources.jar",
        "test-project-1.0.0-sources.jar.sha256",
        "test-project-1.0.0.ivy.xml",
        "test-project-1.0.0.ivy.xml.sha256",
        "test-project-1.0.0.jar",
        "test-project-1.0.0.jar.sha256",
        "test-project-1.0.0.module",
        "test-project-1.0.0.module.sha256",
        "test-project-1.0.0.pom",
        "test-project-1.0.0.pom.sha256",
      ),
      githubReleaseFiles,
    )
  }

  @Test
  fun testKotlinMultiplatformProject(
    @TempDir
    projectDir: Path
  ) {
    val projectVersion = "1.0.0"

    projectDir.resolve("gradle.properties").writeText(
      """
      |org.gradle.jvmargs=-Dfile.encoding=UTF-8
      |org.gradle.configuration-cache=true
      |org.gradle.parallel=true
      |org.gradle.caching=true
      |""".trimIndent()
    )

    projectDir.resolve("settings.gradle.kts").writeText(
      """
      |rootProject.name = "test-project"
      |
      |pluginManagement {
      |  repositories {
      |    maven(file("${devMavenRepo.invariantSeparatorsPathString}"))
      |    mavenCentral()
      |    gradlePluginPortal()
      |  }
      |}
      |
      |dependencyResolutionManagement {
      |  repositoriesMode = RepositoriesMode.PREFER_SETTINGS
      |  repositories {
      |    maven(file("${devMavenRepo.invariantSeparatorsPathString}"))
      |    mavenCentral()
      |    gradlePluginPortal()
      |  }
      |}
      |""".trimMargin()
    )

    projectDir.resolve("build.gradle.kts").writeText(
      """
      |plugins {
      |  kotlin("multiplatform") version "2.2.21"
      |  id("dev.adamko.argh.publisher") version "+"
      |  `maven-publish`
      |}
      |
      |group = "aSemy.demo-github-asset-publish-repo"
      |version = "$projectVersion"
      |
      |kotlin {
      |  jvm()
      |  js { browser() }
      |  linuxX64()
      |}
      |
      |tasks.withType<dev.adamko.argh.gradle.publisher.tasks.UploadGitHubReleaseAssetsTask>().configureEach {
      |  githubRepo.set("aSemy/demo-github-asset-publish-repo")
      |}
      |
      |val javadocJarStub by tasks.registering(Jar::class) {
      |  group = org.gradle.api.plugins.JavaBasePlugin.DOCUMENTATION_GROUP
      |  description = "Empty Javadoc Jar (required by Maven Central)"
      |  archiveClassifier.set("javadoc")
      |}
      |
      |publishing {
      |  publications.withType<MavenPublication>().configureEach {
      |      artifact(javadocJarStub)
      |  }
      |}
      |""".trimMargin()
    )

    projectDir.resolve("src/commonMain/kotlin/Demo.kt")
      .createParentDirectories()
      .writeText(
        """
        |package com.example
        |
        |val demo = "demo"
        |""".trimMargin()
      )

    GradleRunner.create()
      .withProjectDir(projectDir.toFile())
      .withArguments(
        "uploadGitHubReleaseAssets",
        "--skipGitHubUpload",
        "--stacktrace",
      )
      .forwardOutput()
      .build()

    val githubReleaseFilesDir = projectDir.resolve("build/tmp/prepareAssetsTask/")

    val githubReleaseFiles = githubReleaseFilesDir
      .walk()
      .sorted()
      .map { f ->
        val path = f.relativeTo(githubReleaseFilesDir).invariantSeparatorsPathString
        buildString {
          append(path)
          if (!f.isRegularFile()) {
            append(" ${f.describeType()}")
          }
        }
      }
      .toList()

    assertLinesMatch(
      listOf(
        "test-project-$projectVersion-sources.jar",
        "test-project-$projectVersion-sources.jar.sha256",
        "test-project-$projectVersion.ivy.xml",
        "test-project-$projectVersion.ivy.xml.sha256",
        "test-project-$projectVersion.jar",
        "test-project-$projectVersion.jar.sha256",
        "test-project-$projectVersion.module",
        "test-project-$projectVersion.module.sha256",
        "test-project-$projectVersion.pom",
        "test-project-$projectVersion.pom.sha256",
        "test-project-js-$projectVersion-sources.jar",
        "test-project-js-$projectVersion-sources.jar.sha256",
        "test-project-js-$projectVersion.klib",
        "test-project-js-$projectVersion.klib.sha256",
        "test-project-js-$projectVersion.module",
        "test-project-js-$projectVersion.module.sha256",
        "test-project-js-$projectVersion.pom",
        "test-project-js-$projectVersion.pom.sha256",
        "test-project-jvm-$projectVersion-sources.jar",
        "test-project-jvm-$projectVersion-sources.jar.sha256",
        "test-project-jvm-$projectVersion.jar",
        "test-project-jvm-$projectVersion.jar.sha256",
        "test-project-jvm-$projectVersion.module",
        "test-project-jvm-$projectVersion.module.sha256",
        "test-project-jvm-$projectVersion.pom",
        "test-project-jvm-$projectVersion.pom.sha256",
        "test-project-linuxx64-$projectVersion-sources.jar",
        "test-project-linuxx64-$projectVersion-sources.jar.sha256",
        "test-project-linuxx64-$projectVersion.klib",
        "test-project-linuxx64-$projectVersion.klib.sha256",
        "test-project-linuxx64-$projectVersion.module",
        "test-project-linuxx64-$projectVersion.module.sha256",
        "test-project-linuxx64-$projectVersion.pom",
        "test-project-linuxx64-$projectVersion.pom.sha256",
      ),
      githubReleaseFiles,
    )

    GradleRunner.create()
      .withProjectDir(projectDir.toFile())
      .withArguments(
        "uploadGitHubReleaseAssets",
        "--skipGitHubUpload",
        "--stacktrace",
      )
      .forwardOutput()
      .build()
      .let { result ->
        println(result.output)
      }
  }

  companion object {
    private val devMavenRepo: Path by lazy {
      System.getProperty("devMavenRepo")
        ?.let { Path(it) }
        ?: error("System property 'devMavenRepo' not set")
    }

    /**
     * Returns the type of file (file, directory, symlink, etc.).
     */
    internal fun Path.describeType(): String =
      buildString {
        append(
          when {
            !exists()       -> "<non-existent>"
            isRegularFile() -> "file"
            isDirectory()   -> "directory"
            else            -> "<unknown>"
          }
        )

        if (isSymbolicLink()) {
          append(" (symbolic link)")
        }
      }
  }
}
