package dev.adamko.githubassetpublish

import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class GitHubAssetRepositoryPlugin
@Inject
internal constructor() : Plugin<Project> {

  override fun apply(project: Project) {}
}
