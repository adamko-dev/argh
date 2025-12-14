package dev.adamko.argh.gradle.repository

import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginAware

abstract class ArghRepositoryPlugin
@Inject
internal constructor() : Plugin<PluginAware> {

  override fun apply(target: PluginAware) {
    when (target) {
      is Settings -> configureSettings(target)
      is Project  -> configureProject(target)
    }
  }

  private fun configureSettings(settings: Settings) {
    settings.pluginManagement.repositories {
      it.arghGitHubReleaseAssets()
    }

    settings.pluginManagement.resolutionStrategy { strategy ->
      strategy.eachPlugin { details ->
        val namespace = details.requested.id.namespace.orEmpty()
        if (namespace.startsWith(ARGH_PLUGIN_ID_PREFIX)) {
          val groupModule = namespace.removePrefix(ARGH_PLUGIN_ID_PREFIX)
          if ("." in groupModule) {
            val (group, module) = groupModule.split(".", limit = 2)
            logger.info("Overriding plugin ${details.requested.id} module: $group:$module")
            details.useModule("$group:$module:${details.requested.version}")
          }
        }
      }
    }
  }

  private fun configureProject(project: Project) {
    project.repositories.arghGitHubReleaseAssets()
  }

  companion object {
    private val logger: Logger = Logging.getLogger(ArghRepositoryPlugin::class.java)

    private const val ARGH_PLUGIN_ID_PREFIX = "argh."
  }
}
