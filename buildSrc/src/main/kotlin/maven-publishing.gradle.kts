package buildsrc

import buildsrc.settings.MavenPublishingSettings.Companion.mavenPublishing

plugins {
  `maven-publish`
  signing
  id("com.gradleup.nmcp")
  id("buildsrc.maven-publishing-settings")
}


//region POM convention
publishing {
  publications.withType<MavenPublication>().configureEach {
    pom {
      name.convention(mavenPublishing.pomName)
      description.convention(mavenPublishing.pomDescription)
      url.convention("https://github.com/adamko-dev/argh")

      scm {
        connection.convention("scm:git:https://github.com/adamko-dev/argh")
        developerConnection.convention("scm:git:https://github.com/adamko-dev/argh")
        url.convention("https://github.com/adamko-dev/argh")
      }

      licenses {
        license {
          name.convention("Apache-2.0")
          url.convention("https://www.apache.org/licenses/LICENSE-2.0.txt")
        }
      }

      developers {
        developer {
          email.set("adam@adamko.dev")
        }
      }
    }
  }
}
//endregion


publishing {
  repositories {
    maven(rootProject.layout.buildDirectory.dir("project-repo")) {
      name = "ProjectRootBuild"
    }
  }
}


signing {
  logger.info("maven-publishing.gradle.kts enabled signing for ${project.path}")

  val keyId = mavenPublishing.signingKeyId.orNull
  val key = mavenPublishing.signingKey.orNull
  val password = mavenPublishing.signingPassword.orNull

  if (!keyId.isNullOrBlank() && !key.isNullOrBlank() && !password.isNullOrBlank()) {
    useInMemoryPgpKeys(keyId, key, password)
  }

  setRequired({
    gradle.taskGraph.allTasks
      .filterIsInstance<PublishToMavenRepository>()
      .any {
        it.repository.name in setOf(
          "SonatypeRelease",
        )
      }
  })
}


//region Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// https://youtrack.jetbrains.com/issue/KT-46466 https://github.com/gradle/gradle/issues/26091
tasks.withType<AbstractPublishToMaven>().configureEach {
  val signingTasks = tasks.withType<Sign>()
  mustRunAfter(signingTasks)
}
//endregion


//region publishing logging
tasks.withType<AbstractPublishToMaven>().configureEach {
  val publicationGAV = provider { publication?.run { "$group:$artifactId:$version" } }
  doLast("log publication GAV") {
    if (publicationGAV.isPresent) {
      logger.info("[task: ${path}] ${publicationGAV.get()}")
    }
  }
}
//endregion


////region Maven Central can't handle parallel uploads, so limit parallel uploads with a service.
//abstract class MavenPublishLimiter : BuildService<BuildServiceParameters.None>
//
//val mavenPublishLimiter =
//  gradle.sharedServices.registerIfAbsent("mavenPublishLimiter", MavenPublishLimiter::class) {
//    maxParallelUsages = 1
//  }
//
//tasks.withType<PublishToMavenRepository>().configureEach {
//  usesService(mavenPublishLimiter)
//}
////endregion
