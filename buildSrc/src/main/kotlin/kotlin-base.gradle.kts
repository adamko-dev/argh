@file:Suppress("UnstableApiUsage")

package buildsrc

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("buildsrc.base")
  kotlin("jvm")
  id("dev.adamko.dev-publish")
  id("org.jetbrains.dokka")
}

kotlin {
  jvmToolchain(21)

  compilerOptions {
    jvmTarget = JvmTarget.JVM_17

    optIn.add("kotlin.io.path.ExperimentalPathApi")
    optIn.add("kotlin.time.ExperimentalTime")
    optIn.add("kotlin.ExperimentalStdlibApi")

    freeCompilerArgs.addAll(
      "-Xconsistent-data-class-copy-visibility",
      "-Xcontext-parameters",
      "-Xwhen-guards",
      "-Xnon-local-break-continue",
      "-Xmulti-dollar-interpolation",
      "-Xnested-type-aliases",
      "-Xcontext-sensitive-resolution",
    )

    freeCompilerArgs.add(jvmTarget.map { "-Xjdk-release=${it.target}" })
  }
}

tasks.withType<JavaCompile>().configureEach {
  targetCompatibility = kotlin.compilerOptions.jvmTarget.get().target
  sourceCompatibility = kotlin.compilerOptions.jvmTarget.get().target
}

testing.suites.withType<JvmTestSuite>().configureEach {
  useJUnitJupiter("6.0.1")
}

sourceSets.configureEach {
  java.setSrcDirs(emptyList<String>())
}

tasks.withType<Test>().configureEach {
  testLogging {
    setEvents(TestLogEvent.entries)
  }
}

plugins.withType<MavenPublishPlugin>().configureEach {
  val dokkaHtmlJar by tasks.registering(Jar::class) {
    from(tasks.dokkaGeneratePublicationHtml)
    archiveClassifier.set("javadoc")
  }

  extensions.configure<PublishingExtension> {
    publications {
      withType<MavenPublication>().configureEach {
        artifact(dokkaHtmlJar)
      }
    }
  }
}
