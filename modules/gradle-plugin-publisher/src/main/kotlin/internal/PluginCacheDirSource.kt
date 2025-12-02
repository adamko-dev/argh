package dev.adamko.githubassetpublish.internal

import dev.adamko.githubassetpublish.internal.PluginCacheDirSource.Companion.pluginCacheDirSource
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.Path
import org.gradle.api.provider.*
import org.gradle.kotlin.dsl.*

/**
 * Deduces a cache dir from the current operating system
 *
 * Use [PluginCacheDirSource.Companion.pluginCacheDirSource] to get a new instance.
 */
// A ValueSource is used to simplify combining lots of providers.
internal abstract class PluginCacheDirSource
@Inject
internal constructor() : ValueSource<Path, PluginCacheDirSource.Parameters> {

  interface Parameters : ValueSourceParameters {
    /** `systemProperty("os.name")` */
    val osName: Property<String>

    /** `systemProperty("user.home")` */
    val homeDir: Property<String>

    /** `environmentVariable("APPDATA")` */
    val appDataDir: Property<String>

    /** `environmentVariable("XDG_CACHE_HOME")` */
    val xdgCacheHome: Property<String>
  }

  override fun obtain(): Path {
    val kayrayCacheDirName = "github-asset-publish"

    return Path(userCacheDir()).resolve(kayrayCacheDirName)
  }

  private fun userCacheDir(): String {
    val osName = parameters.osName.get().lowercase()

    val xdgCacheHome = parameters.xdgCacheHome.orNull
    if (xdgCacheHome != null) return xdgCacheHome

    val homeDir = parameters.homeDir.get()
    val appDataDir = parameters.appDataDir.orNull ?: homeDir

    return when {
      "win" in osName -> "$appDataDir/Caches/"
      "mac" in osName -> "$homeDir/Library/Caches/"
      "nix" in osName -> "$homeDir/.cache/"
      else            -> "$homeDir/.cache/"
    }
  }

  companion object {
    fun ProviderFactory.pluginCacheDirSource(): Provider<Path> {
      return of(PluginCacheDirSource::class) { spec ->
        spec.parameters.osName.set(systemProperty("os.name"))
        spec.parameters.homeDir.set(systemProperty("user.home"))
        spec.parameters.appDataDir.set(environmentVariable("APPDATA"))
        spec.parameters.xdgCacheHome.set(environmentVariable("XDG_CACHE_HOME"))
      }
    }
  }
}
