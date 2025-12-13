package buildsrc

import org.gradle.api.provider.Provider

abstract class MavenRepositoryMirrorExtension
internal constructor(
  val serviceProvider: Provider<MavenRepositoryMirrorService>,
)
