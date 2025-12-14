@file:Suppress("UnstableApiUsage")

plugins {
  buildsrc.base
  idea
  `kotlin-dsl` apply false
  id("com.gradleup.nmcp.aggregation")
  buildsrc.`maven-publishing-settings`
}

tasks.updateDaemonJvm {
  languageVersion = JavaLanguageVersion.of(21)
}

idea {
  module {
    excludeDirs.addAll(
      files(
        ".idea/",
        ".kotlin/",
        "buildSrc/.kotlin/",
        "gradle/wrapper/",
      )
    )
  }
}

nmcpAggregation {
  centralPortal {
    username = mavenPublishing.mavenCentralUsername
    password = mavenPublishing.mavenCentralPassword

    // publish manually from the portal
    publishingType = "USER_MANAGED"
  }
}

dependencies {
  nmcpAggregation(projects.modules.gradlePluginPublisher)
  nmcpAggregation(projects.modules.gradlePluginRepository)
  nmcpAggregation(projects.modules.libAssetUploader)
  nmcpAggregation(projects.modules.libAssetUploaderApi)
  nmcpAggregation(projects.modules.libGithubRestClient)
  nmcpAggregation(projects.modules.libGmm)
  nmcpAggregation(projects.modules.libUtils)
  nmcpAggregation(projects.modules.mavenPluginPublisher)
  nmcpAggregation(projects.modules.mavenPluginRepository)
}

val publishAggregationToCentralPortal: TaskProvider<Task> =
  tasks.named("publishAggregationToCentralPortal").apply {
    configure {
      val isReleaseVersion = mavenPublishing.isReleaseVersion
      onlyIf("is release version") { isReleaseVersion.get() }
    }
  }
val publishAggregationToCentralPortalSnapshots: TaskProvider<Task> =
  tasks.named("publishAggregationToCentralPortalSnapshots").apply {
    configure {
      val isReleaseVersion = mavenPublishing.isReleaseVersion
      onlyIf("is snapshot version") { !isReleaseVersion.get() }
    }
  }

tasks.register("nmcpPublish") {
  group = PublishingPlugin.PUBLISH_TASK_GROUP
  dependsOn(publishAggregationToCentralPortal)
  dependsOn(publishAggregationToCentralPortalSnapshots)
}
