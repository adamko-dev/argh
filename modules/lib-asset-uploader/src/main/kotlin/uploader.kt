@file:JvmName("Uploader")

package dev.adamko.githubassetpublish.lib

import dev.adamko.githubapiclient.*
import dev.adamko.githubapiclient.model.RepoReleaseAsset
import java.nio.file.Path
import java.util.concurrent.TimeUnit.MINUTES
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.name
import kotlin.io.path.useDirectoryEntries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

suspend fun main(args: Array<String>) {
  /*

  Requires `gh` cli is installed and authenticated.

  1. Get the version of the release.
  2. Check if the GitHub repo has a compatible release.
  3. Check if the release already has files.
     - FAIL: if not snapshot version and files already exist
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

  val release = FilesToUploadWithMetadata(
    repo = githubRepo,
    version = releaseVersion,
    releaseDir = releaseDir,
  )

  upload(
    srcRelease = release,
    createNewReleaseIfMissing = createNewReleaseIfMissing,
    tokenDataFile = pluginCacheDir.resolve("gh-token.json"),
  )
}

private val uploadReleaseAssetDispatcher: CoroutineDispatcher =
  Dispatchers.IO.limitedParallelism(10)

private data class FilesToUploadWithMetadata(
  /** Repository in the form of "owner/repo" */
  val repo: String,
  /** Version of the release */
  val version: String,
//  /** Whether this is a snapshot version */
//  val isSnapshot: Boolean,
  /** Directory containing all files to upload. */
  val releaseDir: Path,
) {
//  val isSnapshot: Boolean
//    get() = version.endsWith("-SNAPSHOT")
}

private suspend fun upload(
  srcRelease: FilesToUploadWithMetadata,
  createNewReleaseIfMissing: Boolean,
  tokenDataFile: Path,
): Unit = coroutineScope {
  val (owner, repo) = srcRelease.repo.split("/", limit = 2)
  val version = srcRelease.version
  val isSnapshot = srcRelease.version.endsWith("-SNAPSHOT")
//  val filesToUpload = release.filesToUpload

  val gh = GitHubClient(
    tokenDataFile = tokenDataFile
  )
//  val gh = GitHub(
//    repo = repo,
//    workDir = release.releaseDir,
//  )

//  val ghVersion = gh.version() ?: error("gh not installed")
//  println("using gh version: $ghVersion")

  println("Starting upload process for version: $version, repo: $repo")

  // Check if the release exists
  val ghRelease = run {
    val result =
      gh.repos.listAllReleases(
        owner = owner,
        repo = repo,
        perPage = 100,
      ).firstOrNull { it.tagName == version }
//    val result = gh.repos.getReleaseByTagOrNull(owner = owner, repo = repo, tag = version)
    if (result != null) {
      return@run result
    } else {
      if (createNewReleaseIfMissing) {
//        val created =
        gh.repos.createRelease(
          owner = owner,
          repo = repo,
          tagName = version,
          draft = true,
          prerelease = isSnapshot,
          name = version,
        )
//        check(created.content.size == 1) {
//          "Failed to create release for $owner/$repo/$version. Got ${created.content.size}\n" +
//              created.content.joinToString("\n") { "\t$it" }
//        }
//        created.content.single()
//        gh.repos.getReleaseByTagOrNull(owner = owner, repo = repo, tag = version)
//          ?: error("Release $version was created but cannot be retrieved.")
      } else {
        error("Release $version does not exist and createNewReleaseIfMissing is false.")
      }
    }
  }
//  if (gh.repos.getReleaseByTagOrNull(owner = owner, repo = repo, tag = version) == null) {
//    if (createNewReleaseIfMissing) {
//      println("Creating new release $version...")
//      val result = gh.repos.createRelease(
//        owner = owner,
//        repo = repo,
//        tagName = version,
//      )
//      println(result)
//    } else {
//      error("Release $version does not exist and createNewReleaseIfMissing is false.")
//    }
//  }


  // Check if the release already has files
  val existingFiles: List<RepoReleaseAsset> =
    gh.repos.listAllReleaseAssets(
      owner = owner,
      repo = repo,
      releaseId = ghRelease.id,
      perPage = 100,
    ).toList()


  // TODO if the release is a draft or prerelease then...
  //      1. fetch the attached module files (if any)
  //      2. extract the artifacts (and metadata files).
  //      3. delete the attached files.
  //      Until then, just abort early.
  if (existingFiles.isNotEmpty()) {
    error("Release $version already has ${existingFiles.size} file(s) $existingFiles. Exiting.")
  }

  // Upload new files
  println("Uploading files to release ${ghRelease.id}, $version...")

  srcRelease.releaseDir.useDirectoryEntries { srcFiles ->
    srcFiles.forEach { srcFile ->
      async(uploadReleaseAssetDispatcher) {
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


private abstract class CliTool(
  private val workDir: Path,
) {

  protected fun runCommandOrNull(
    cmd: String,
    logOutput: Boolean = true,
  ): String? {
    return try {
      runCommand(cmd, logOutput)
    } catch (_: ProcessException) {
      null
    }
  }

  protected fun runCommand(
    cmd: String,
    logOutput: Boolean = true,
  ): String {

    val args = parseSpaceSeparatedArgs(cmd)

    val process = ProcessBuilder(args).apply {
      redirectOutput(ProcessBuilder.Redirect.PIPE)
      redirectInput(ProcessBuilder.Redirect.PIPE)
      redirectErrorStream(true)
      directory(workDir.toFile())
    }.start()

    val processOutput = process.inputStream
      .bufferedReader()
      .lineSequence()
      .onEach { if (logOutput) println("\t$it") }
      .joinToString("\n")
      .trim()

    process.waitFor(10, MINUTES)

    val exitCode = process.exitValue()

    return if (exitCode == 0) {
      processOutput
    } else {
      throw ProcessException(
        cmd = args.joinToString(" "),
        exitCode = exitCode,
        processOutput = processOutput,
      )
    }
  }

  private class ProcessException(
    cmd: String,
    exitCode: Int,
    processOutput: String,
  ) : Exception(
    buildString {
      appendLine("Command '$cmd' failed with exit code $exitCode")
      appendLine(processOutput.prependIndent())
    }
  )

  companion object {
    private fun parseSpaceSeparatedArgs(argsString: String): List<String> {
      val parsedArgs = mutableListOf<String>()
      var inQuotes = false
      var currentCharSequence = StringBuilder()
      fun saveArg(wasInQuotes: Boolean) {
        if (wasInQuotes || currentCharSequence.isNotBlank()) {
          parsedArgs.add(currentCharSequence.toString())
          currentCharSequence = StringBuilder()
        }
      }
      argsString.forEach { char ->
        if (char == '"') {
          inQuotes = !inQuotes
          // Save value which was in quotes.
          if (!inQuotes) {
            saveArg(true)
          }
        } else if (char.isWhitespace() && !inQuotes) {
          // Space is separator
          saveArg(false)
        } else {
          currentCharSequence.append(char)
        }
      }
      if (inQuotes) {
        error("No close-quote was found in $currentCharSequence.")
      }
      saveArg(false)
      return parsedArgs
    }
  }
}

/** GitHub commands */
private class GitHub(
  private val repo: String,
  workDir: Path,
) : CliTool(workDir = workDir) {


//  fun releaseList(tag: String): String? =
//    runCommandOrNull("gh release view $tag --repo $repo")

  fun releaseCreate(tag: String): String =
    runCommand(
      buildString {
        appendLine("gh release create $tag")
//        appendLine("--verify-tag")
        appendLine("--draft")
        appendLine("--title $tag")
        appendLine("--repo $repo")
      }
//        append(" --prerelease ")
//        append(" --fail-on-no-commits ")
    )

  fun releaseUpload(tag: String, files: Iterable<String>): String =
    runCommand(
      buildString {
        appendLine("gh release upload $tag")
        appendLine("--repo $repo")
        appendLine(files.joinToString(" "))
      }
    )

  fun releaseView(tag: String): String? =
    runCommandOrNull("gh release view $tag --repo $repo")

  fun releaseListAssets(tag: String): String =
    runCommand("gh release view $tag --repo $repo --json assets --jq .assets[].name")
//    runCommand("gh release view $tag --repo $repo --json assets")

  fun releaseDeleteAsset(tag: String, assetName: String): String? =
    runCommandOrNull("gh release delete-asset $tag $assetName --repo $repo --yes")

  fun releaseDownload(
    tag: String,
    pattern: String,
    destination: Path? = null,
  ): String? =
    runCommandOrNull(
      buildString {
        appendLine("gh release download $tag")
        appendLine("--repo $repo")
        if (destination != null) {
          appendLine("--dest ${destination.invariantSeparatorsPathString}")
        }
        appendLine("--pattern $pattern")
      }
    )

  fun version(): String? =
    runCommandOrNull("gh version", logOutput = false)
}
