package dev.adamko.githubassetpublish.model

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

abstract class GitHubReleasePublishSpec internal constructor() {
  /** `OWNER/REPO` */
  abstract val repository: Property<String>
  abstract val version: Property<String>
  abstract val releaseDir: DirectoryProperty
  abstract val createNewReleaseIfMissing: Property<Boolean>
}
