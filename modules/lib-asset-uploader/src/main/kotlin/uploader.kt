@file:JvmName("Uploader")

package dev.adamko.argh.lib.uploader

import dev.adamko.githubapiclient.*
import dev.adamko.githubapiclient.model.RepoRelease
import dev.adamko.githubapiclient.model.RepoReleaseAsset
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.useDirectoryEntries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

suspend fun main(args: Array<String>) {
  /*

  1. Get the version of the release.
  2. Check if the GitHub repo has a compatible release.
  3. If a release exists, delete already attached files.
  4. Upload assets to the release.
  5. If snapshot version, delete old files.
   */


  fun getArg(name: String): String =
    args.firstOrNull { it.startsWith("$name=") }
      ?.substringAfter("$name=")
      ?: error("missing required argument '$name'")


  /** `OWNER/REPO` */
  val githubRepo = getArg("githubRepo")
  val releaseVersion = getArg("releaseVersion")
  val releaseDir = getArg("releaseDir").let(::Path)
  val pluginCacheDir = getArg("pluginCacheDir").let(::Path)
  val createNewReleaseIfMissing = getArg("createNewReleaseIfMissing").toBoolean()

  val tokenDataFile = pluginCacheDir.resolve("gh-token.json")
  val gh = GitHubClient(
    tokenDataFile = tokenDataFile
  )

  checkGitHubClientScope(gh)

  val release = FilesToUploadWithMetadata(
    repo = githubRepo,
    version = releaseVersion,
    releaseDir = releaseDir,
  )

  upload(
    gh = gh,
    srcRelease = release,
    createNewReleaseIfMissing = createNewReleaseIfMissing,
  )
}

private suspend fun checkGitHubClientScope(gh: GitHubClient) {
  val scopes = gh.user.getAuthenticatedUserScopes()
    .orEmpty()
  require("repo" in scopes) {
    "GitHub token must have 'repo' scope to upload release assets. Current scopes: $scopes"
  }
}

/**
 * Limited parallel upload and download of release assets.
 */
private val releaseAssetDispatcher: CoroutineDispatcher =
  Dispatchers.IO.limitedParallelism(10)

private data class FilesToUploadWithMetadata(
  /** Repository in the form of "owner/repo" */
  val repo: String,
  /** Version of the release */
  val version: String,
  /** Directory containing all files to upload. */
  val releaseDir: Path,
) {
  val isSnapshot: Boolean
    get() = version.endsWith("-SNAPSHOT")
}

private suspend fun upload(
  gh: GitHubClient,
  srcRelease: FilesToUploadWithMetadata,
  createNewReleaseIfMissing: Boolean,
): Unit = coroutineScope {
  val (owner, repo) = srcRelease.repo.split("/", limit = 2)
  val version = srcRelease.version

  println("Starting upload process for version: $version, repo: $repo")

  // Check if the release exists
  val ghRelease = getOrCreateRelease(
    gh = gh,
    isSnapshot = srcRelease.isSnapshot,
    version = version,
    owner = owner,
    repo = repo,
    createNewReleaseIfMissing = createNewReleaseIfMissing,
  )


  // Upload new files
  println("Uploading files to release ${ghRelease.id}, $version...")

  // TODO validate Release does not already have asset with the same name

  srcRelease.releaseDir.useDirectoryEntries { srcFiles ->
    srcFiles.forEach { srcFile ->
      launch(releaseAssetDispatcher) {
        println("Attaching asset ${srcFile.name} to release ${ghRelease.id}")
        gh.repos.uploadReleaseAsset(
          owner = owner,
          repo = repo,
          releaseId = ghRelease.id,
          file = srcFile,
        )
      }
    }
  }
//  val filesToUpload = srcRelease.releaseDir.listDirectoryEntries().map { it.name }
//  gh.releaseUpload(tag = version, files = filesToUpload)

  println("Upload process completed for version: $version")

  // TODO validate checksum of uploaded files
}

/**
 * If the release is NOT immutable and is a draft or prerelease then...
 * 1. Fetch the attached module files (if any).
 * 2. Find associated files (by module filename prefix).
 * 3. Delete the attached assets.
 */
// TODO don't delete if an attached asset is identical to an asset that should be uploaded
private suspend fun handleExistingReleaseAssets(
  gh: GitHubClient,
  owner: String,
  repo: String,
  ghRelease: RepoRelease,
  existingAssets: List<RepoReleaseAsset>
): Unit = coroutineScope block@{
  val deletedAssets = mutableListOf<RepoReleaseAsset>()

  val moduleAssets = existingAssets
    .filter { it.name.endsWith(".module") }

  val moduleGavPrefixes = moduleAssets.map { it.name.removeSuffix(".module") }

  val assetsToDelete = existingAssets.filter { asset ->
    moduleGavPrefixes.any { prefix ->
      asset.name.startsWith(prefix)
    }
  }

  assetsToDelete.forEach { assetToDelete ->
    launch(releaseAssetDispatcher) {
      try {
        gh.repos.deleteReleaseAsset(
          owner = owner,
          repo = repo,
          assetId = assetToDelete.id,
        )
        println("Deleted asset ${assetToDelete.name}")
        deletedAssets += assetToDelete
      } catch (e: Exception) {
        println("Failed to delete asset ${assetToDelete.name}. $e")
      }
    }
  }

  println("Deleted ${deletedAssets.size} assets from release ${ghRelease.name}.")
  deletedAssets.forEach { println("\t- ${it.name}") }
  if (deletedAssets.size < existingAssets.size) {
    println("There are still ${existingAssets.size - deletedAssets.size} assets remaining.")
    existingAssets.filter { it !in deletedAssets }.forEach { println("\t- ${it.name}") }
  }
}


private suspend fun getOrCreateRelease(
  gh: GitHubClient,
  owner: String,
  repo: String,
  version: String,
  isSnapshot: Boolean,
  createNewReleaseIfMissing: Boolean,
): RepoRelease {
  val ghRelease =
    gh.repos.listAllReleases(
      owner = owner,
      repo = repo,
      perPage = 100,
    ).firstOrNull { it.tagName == version }

  if (ghRelease != null) {

    // Check if the release already has files
    val existingFiles: List<RepoReleaseAsset> =
      gh.repos.listAllReleaseAssets(
        owner = owner,
        repo = repo,
        releaseId = ghRelease.id,
        perPage = 100,
      ).toList()

    if (
      (ghRelease.draft
          || ghRelease.prerelease
          || ghRelease.immutable == true)
      && existingFiles.isNotEmpty()
    ) {
      handleExistingReleaseAssets(
        gh = gh,
        owner = owner,
        repo = repo,
        ghRelease = ghRelease,
        existingAssets = existingFiles,
      )
    }

    return ghRelease
  }

  if (createNewReleaseIfMissing) {
    return gh.repos.createRelease(
      owner = owner,
      repo = repo,
      tagName = version,
      draft = true,
      prerelease = isSnapshot,
      name = version,
    )
  } else {
    error("Release $version does not exist and createNewReleaseIfMissing is false.")
  }
}
