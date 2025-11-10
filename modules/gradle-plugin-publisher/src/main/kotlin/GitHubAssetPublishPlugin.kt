@file:Suppress("UnstableApiUsage")

package dev.adamko.githubassetpublish

import dev.adamko.githubassetpublish.internal.UploadReleaseDependencies
import dev.adamko.githubassetpublish.tasks.PrepareGitHubReleaseFilesTask
import dev.adamko.githubassetpublish.tasks.UploadGitHubReleaseAssetsTask
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.*

abstract class GitHubAssetPublishPlugin
@Inject
internal constructor(
  private val providers: ProviderFactory,
  private val layout: ProjectLayout,
) : Plugin<Project> {

  override fun apply(project: Project) {

    val gapExtension = createExtension(project)

    project.pluginManager.apply(MavenPublishPlugin::class)

    val publishing = project.extensions.getByType<PublishingExtension>()

    configureTaskConventions(project, gapExtension)

    publishing.repositories.maven(gapExtension.stagingRepoDir) {
      name = gapExtension.stagingRepoName
    }

    val cleanBuildDirMavenRepoDir by project.tasks.registering(Delete::class) {
      delete(gapExtension.stagingRepoDir)
    }

    val buildDirPublishTasks =
      project.tasks
        .withType<PublishToMavenRepository>()
        .matching { task ->
          task.name.endsWith("PublicationToGitHubAssetPublishStagingRepository")
        }

    buildDirPublishTasks.configureEach { task ->
      task.dependsOn(cleanBuildDirMavenRepoDir)
    }

    val prepareAssetsTask by project.tasks.registering(PrepareGitHubReleaseFilesTask::class) {
      group = PublishingPlugin.PUBLISH_TASK_GROUP
      dependsOn(buildDirPublishTasks)
      stagingMavenRepo.convention(gapExtension.stagingRepoDir)
    }

    val uploadGitHubReleaseAssets by project.tasks.registering(UploadGitHubReleaseAssetsTask::class) {
      group = PublishingPlugin.PUBLISH_TASK_GROUP

      dependsOn(buildDirPublishTasks)
      preparedAssetsDir.convention(prepareAssetsTask.flatMap { it.destinationDirectory })
    }
  }

  private fun createExtension(project: Project): GitHubAssetPublishExtension {
    return project.extensions.create<GitHubAssetPublishExtension>("gitHubAssetPublish").apply {
      gapBuildDir.convention(project.layout.buildDirectory.dir("github-asset-publish"))
      stagingRepoDir.convention(gapBuildDir.dir("staging-repo"))
    }
  }

  private fun configureTaskConventions(
    project: Project,
    gapExtension: GitHubAssetPublishExtension,
  ) {
    val uploadReleaseDependencies = UploadReleaseDependencies(project)

    project.tasks.withType<PrepareGitHubReleaseFilesTask>().configureEach { task ->
      task.runtimeClasspath.from(uploadReleaseDependencies.resolver)
      task.destinationDirectory.convention(
        layout.dir(providers.provider { task.temporaryDir })
      )
    }

    project.tasks.withType<UploadGitHubReleaseAssetsTask>().configureEach { task ->
      task.createNewReleaseIfMissing.convention(true)
      task.runtimeClasspath.from(uploadReleaseDependencies.resolver)
      task.githubRepo.convention(gapExtension.githubRepo)
      task.skipUpload.convention(false)
    }
  }
}
