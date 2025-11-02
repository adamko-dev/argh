@file:Suppress("UnstableApiUsage")

package dev.adamko.githubassetpublish

import dev.adamko.githubassetpublish.internal.BuildConstants
import dev.adamko.githubassetpublish.internal.PrepareReleaseDependencies
import dev.adamko.githubassetpublish.internal.UploadReleaseDependencies
import dev.adamko.githubassetpublish.tasks.PrepareGitHubReleaseFilesTask
import dev.adamko.githubassetpublish.tasks.UploadGitHubReleaseAssetsTask
import java.net.URI
import javax.inject.Inject
import kotlin.io.path.toPath
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
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
  private val objects: ObjectFactory,
  private val providers: ProviderFactory,
  private val layout: ProjectLayout,
) : Plugin<Project> {

  override fun apply(project: Project) {
    project.pluginManager.apply(MavenPublishPlugin::class)

    val publishing = project.extensions.getByType<PublishingExtension>()

    val buildDirMavenRepo =
      publishing.repositories.maven(layout.buildDirectory.dir("build-dir-maven")) {
        name = "BuildDir"
      }

    fun buildDirMavenDirectoryProvider(): Provider<Directory> =
      objects.directoryProperty()
        .fileProvider(providers.provider { buildDirMavenRepo }.map { it.url.toPath().toFile() })

    val cleanBuildDirMavenRepoDir by project.tasks.registering(Delete::class) {
      val buildDirMavenRepoDir = buildDirMavenDirectoryProvider()
      delete(buildDirMavenRepoDir)
    }

    val buildDirPublishTasks = project.tasks.withType<PublishToMavenRepository>()
      .matching { task ->
        try {
          task.repository?.url?.toPath() != null
        } catch (ex: IllegalArgumentException) {
          false
        }
      }

    buildDirPublishTasks.configureEach { task ->
      val repositoryUrl: Provider<URI> = providers.provider { task.repository.url }

      task.outputs.dir(repositoryUrl)
        .withPropertyName("repositoryUrl")

      task.dependsOn(cleanBuildDirMavenRepoDir)
    }

    val prepareReleaseDependencies = PrepareReleaseDependencies(project)

    val prepareGitHubReleaseFiles by project.tasks.registering(PrepareGitHubReleaseFilesTask::class) {
      group = PublishingPlugin.PUBLISH_TASK_GROUP
      dependsOn(buildDirPublishTasks)
      sourceMavenRepositoryDir.convention(buildDirMavenDirectoryProvider())
      val destinationDir = layout.buildDirectory.dir("github-release-files")
      destinationDirectory.convention(destinationDir)

      runtimeClasspath.from(prepareReleaseDependencies.resolver)
    }

    val uploadReleaseDependencies = UploadReleaseDependencies(project)

    val uploadGitHubReleaseAssets by project.tasks.registering(UploadGitHubReleaseAssetsTask::class) {
      group = PublishingPlugin.PUBLISH_TASK_GROUP

    }

  }
}
