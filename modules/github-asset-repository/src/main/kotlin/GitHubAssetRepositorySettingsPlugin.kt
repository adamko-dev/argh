package dev.adamko.githubassetpublish

import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

abstract class GitHubAssetRepositorySettingsPlugin
@Inject
internal constructor() : Plugin<Settings> {

  override fun apply(project: Settings) {}
}
