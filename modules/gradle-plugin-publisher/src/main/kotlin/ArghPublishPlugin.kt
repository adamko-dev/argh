@file:Suppress("UnstableApiUsage")

package dev.adamko.argh.gradle.publisher

import dev.adamko.argh.gradle.publisher.config.GitHubOAuthTokenSource
import dev.adamko.argh.gradle.publisher.internal.PluginCacheDirSource.Companion.pluginCacheDirSource
import dev.adamko.argh.gradle.publisher.internal.UploadReleaseDependencies
import dev.adamko.argh.gradle.publisher.tasks.PrepareGitHubReleaseFilesTask
import dev.adamko.argh.gradle.publisher.tasks.UploadGitHubReleaseAssetsTask
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.kotlin.dsl.*

abstract class ArghPublishPlugin
@Inject
internal constructor(
  private val providers: ProviderFactory,
  private val layout: ProjectLayout,
  private val objects: ObjectFactory,
) : Plugin<Project> {

  override fun apply(project: Project) {

    val arghExtension = createExtension(project)

    project.pluginManager.apply(MavenPublishPlugin::class)

    val publishing = project.extensions.getByType<PublishingExtension>()

    configureTaskConventions(project, arghExtension)

    publishing.repositories.maven(arghExtension.stagingRepoDir) {
      name = arghExtension.stagingRepoName
    }

    val cleanBuildDirMavenRepoDir by project.tasks.registering {
      val stagingRepoDir = arghExtension.stagingRepoDir
      destroyables.register(stagingRepoDir)
      doLast {
        stagingRepoDir.get().asFile.deleteRecursively()
      }
    }

    val buildDirPublishTasks =
      project.tasks
        .withType<PublishToMavenRepository>()
        .matching { task ->
          task.name.endsWith("PublicationTo${arghExtension.stagingRepoName}Repository")
        }

    buildDirPublishTasks.configureEach { task ->
      task.dependsOn(cleanBuildDirMavenRepoDir)
    }

    val prepareAssetsTask by project.tasks.registering(PrepareGitHubReleaseFilesTask::class) {
      group = PublishingPlugin.PUBLISH_TASK_GROUP
      dependsOn(buildDirPublishTasks)
      stagingMavenRepo.convention(arghExtension.stagingRepoDir)
    }

    val uploadGitHubReleaseAssets by project.tasks.registering(UploadGitHubReleaseAssetsTask::class) {
      group = PublishingPlugin.PUBLISH_TASK_GROUP

      dependsOn(buildDirPublishTasks)
      preparedAssetsDir.convention(prepareAssetsTask.flatMap { it.destinationDirectory })
    }
  }

  private fun createExtension(project: Project): ArghPublishExtension {
    return project.extensions.create<ArghPublishExtension>("arghPublisher").apply {
      baseBuildDir.convention(project.layout.buildDirectory.dir("argh-publish"))
      stagingRepoDir.convention(baseBuildDir.dir("staging-repo"))
      artifactMetadataExtensions.convention(
        setOf(
          "sha256",
          "asc",
        )
      )
      pluginCacheDir.convention(
        objects.directoryProperty()
          .fileProvider(
            providers.pluginCacheDirSource().map { it.toFile() }
          )
      )
      gitHubOAuthToken.convention(GitHubOAuthTokenSource.EnvVar)
    }
  }

  private fun configureTaskConventions(
    project: Project,
    arghExtension: ArghPublishExtension,
  ) {
    val uploadReleaseDependencies = UploadReleaseDependencies(project)

    project.tasks.withType<PrepareGitHubReleaseFilesTask>().configureEach { task ->
      task.runtimeClasspath.from(uploadReleaseDependencies.resolver)
      task.destinationDirectory.convention(
        layout.dir(providers.provider { task.temporaryDir })
      )
      task.artifactMetadataExtensions.convention(arghExtension.artifactMetadataExtensions)
    }

    project.tasks.withType<UploadGitHubReleaseAssetsTask>().configureEach { task ->
      task.createNewReleaseIfMissing.convention(true)
      task.runtimeClasspath.from(uploadReleaseDependencies.resolver)
      task.gitHubRepo.convention(arghExtension.gitHubRepo)
      task.skipGitHubUpload.convention(false)
      task.releaseVersion.convention(providers.provider { project.version.toString() })
      task.pluginCacheDir.convention(arghExtension.pluginCacheDir)
      task.gitHubOAuthToken.convention(arghExtension.gitHubOAuthToken)
    }
  }
}
