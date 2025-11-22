package dev.adamko.githubassetpublish.lib.gh

//import io.ktor.client.*
//import io.ktor.client.engine.cio.*
//import io.ktor.client.plugins.logging.*
//import io.ktor.client.plugins.resources.*
import java.nio.file.Path

internal class GitHubClient {

//  private val client: HttpClient =
//    HttpClient(CIO) {
//      expectSuccess = true
//      install(Logging) {
//      }
//
//      install(Resources)
//    }


  fun releaseCreate(tag: String): String {
//    runCommand(
//      buildString {
//        appendLine("gh release create $tag")
//        appendLine("--draft")
//        appendLine("--title $tag")
//        appendLine("--repo $repo")
//      }
//    )
    TODO()
  }

  fun releaseUpload(tag: String, files: Iterable<String>): String {
//    runCommand(
//      buildString {
//        appendLine("gh release upload $tag")
//        appendLine("--repo $repo")
//        appendLine(files.joinToString(" "))
//      }
//    )
    TODO()
  }

  fun releaseView(tag: String): String? {
//    runCommandOrNull("gh release view $tag --repo $repo")
    TODO()
  }

  fun releaseListAssets(tag: String): String {
//    runCommand("gh release view $tag --repo $repo --json assets --jq .assets[].name")
    TODO()
  }

  fun releaseDeleteAsset(tag: String, assetName: String): String? {
//    runCommandOrNull("gh release delete-asset $tag $assetName --repo $repo --yes")
    TODO()
  }

  fun releaseDownload(
    tag: String,
    pattern: String,
    destination: Path? = null,
  ): String? {
//    runCommandOrNull(
//      buildString {
//        appendLine("gh release download $tag")
//        appendLine("--repo $repo")
//        if (destination != null) {
//          appendLine("--dest ${destination.invariantSeparatorsPathString}")
//        }
//        appendLine("--pattern $pattern")
//      }
//    )
    TODO()
  }
}
