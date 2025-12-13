package dev.adamko.argh.gradle.repository

import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginAware

abstract class GitHubAssetRepositoryPlugin
@Inject
internal constructor() : Plugin<PluginAware> {

  override fun apply(target: PluginAware) {
    if (target is Settings) {
      configureSettings(target)
    }
  }

  private fun configureSettings(settings: Settings) {
    settings.pluginManagement.repositories {
      it.githubReleaseAssets()
    }

    settings.pluginManagement.resolutionStrategy { strategy ->
      strategy.eachPlugin { details ->
        val namespace = details.requested.id.namespace.orEmpty()
        if (namespace.startsWith(GH_ASSET_PLUGIN_PREFIX)) {
          val (group, module) = namespace.removePrefix(GH_ASSET_PLUGIN_PREFIX).run {
            substringBefore(".") to substringAfter(".")
          }
          logger.info("Overriding plugin ${details.requested.id} module: $group:$module")
          details.useModule("$group:$module:${details.requested.version}")
        }
      }
    }
  }

  companion object {
    private val logger: Logger = Logging.getLogger(GitHubAssetRepositoryPlugin::class.java)

    private const val GH_ASSET_PLUGIN_PREFIX = "com.github-assets."
  }
}
