package dev.adamko.githubapiclient

import dev.adamko.githubapiclient.endpoints.repos.*
import dev.adamko.githubapiclient.endpoints.repos.CreateRelease.RequestBody.MakeLatest
import dev.adamko.githubapiclient.model.RepoRelease
import dev.adamko.githubapiclient.model.RepoReleaseAsset
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.streams.*
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.name
import kotlin.io.path.outputStream
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive


/**
 * ### Get a release
 *
 * Gets a public release with the specified release ID.
 *
 * > [!NOTE]
 * > This returns an `upload_url` key corresponding to the endpoint for uploading release assets.
 * This key is a hypermedia resource.
 * For more information, see
 * "[Getting started with the REST API](https://docs.github.com/rest/using-the-rest-api/getting-started-with-the-rest-api#hypermedia)."
 *
 * `repos/get-release`
 */
suspend fun GitHubClient.Repos.getRelease(
  owner: String,
  repo: String,
  releaseId: Int,
): GetRelease.ResponseBody {
  val resource = GetRelease.Route(
    owner = owner,
    repo = repo,
    releaseId = releaseId,
  )
  return httpClient.get(resource = resource).body()
}

/**
 * ### Get a release asset
 *
 * To download the asset's binary content:
 *
 * - If within a browser, fetch the location specified in the `browser_download_url` key provided in the response.
 * - Alternatively, set the `Accept` header of the request to
 *   [`application/octet-stream`](https://docs.github.com/rest/using-the-rest-api/getting-started-with-the-rest-api#media-types).
 *   The API will either redirect the client to the location, or stream it directly if possible.
 *   API clients should handle both a `200` or `302` response.
 *
 * `repos/get-release-asset`
 */
suspend fun GitHubClient.Repos.getReleaseAsset(
  owner: String,
  repo: String,
  assetId: Long,
): GetReleaseAsset.ResponseBody {
  val resource = GetReleaseAsset.Route(
    owner = owner,
    repo = repo,
    assetId = assetId,
  )
  val result = httpClient.get(resource = resource)
  return result.body()
}

/**
 * https://docs.github.com/en/rest/releases/assets?apiVersion=2022-11-28#get-a-release-asset
 */
suspend fun GitHubClient.Repos.downloadReleaseAsset(
  owner: String,
  repo: String,
  assetId: Long,
  destination: Path,
) {
  val resource = GetReleaseAsset.Route(
    owner = owner,
    repo = repo,
    assetId = assetId,
  )
  httpClient.prepareGet(resource) {
    headers {
      accept(ContentType.Application.OctetStream)
      exclude(ContentType.Application.Json) // automatically added by ContentNegotiation plugin
    }
  }.execute { httpResponse ->
    val channel: ByteReadChannel = httpResponse.body()
    channel.copyAndClose(destination.outputStream().asByteWriteChannel())
  }
}

/**
 * https://docs.github.com/en/rest/releases/assets?apiVersion=2022-11-28#delete-a-release-asset
 */
suspend fun GitHubClient.Repos.deleteReleaseAsset(
  owner: String,
  repo: String,
  assetId: Long,
) {
  val resource = DeleteReleaseAsset.Route(
    owner = owner,
    repo = repo,
    assetId = assetId,
  )
  httpClient.delete(resource)
}

/**
 * ### Get a release by tag name
 *
 * Get a published release with the specified tag.
 *
 * `repos/get-release-by-tag`
 */
suspend fun GitHubClient.Repos.getReleaseByTag(
  owner: String,
  repo: String,
  tag: String,
): GetReleaseByTag.ResponseBody {
  val resource = GetReleaseByTag.Route(
    owner = owner,
    repo = repo,
    tag = tag,
  )
  val response = httpClient.get(resource = resource)
  return response.body()
}

/**
 * ### Get a release by tag name
 *
 * Get a published release with the specified tag.
 *
 * Returns `null` if the release does not exist.
 *
 * `repos/get-release-by-tag`
 */
suspend fun GitHubClient.Repos.getReleaseByTagOrNull(
  owner: String,
  repo: String,
  tag: String,
): GetReleaseByTag.ResponseBody? {
  val resource = GetReleaseByTag.Route(
    owner = owner,
    repo = repo,
    tag = tag,
  )
  val response = httpClient.get(resource = resource)
  return if (response.status == HttpStatusCode.NotFound) {
    null
  } else {
    response.body()
  }
}


suspend fun GitHubClient.Repos.createRelease(
  owner: String,
  repo: String,
  tagName: String,
  targetCommitish: String? = null,
  name: String? = null,
  body: String? = null,
  draft: Boolean = false,
  prerelease: Boolean = false,
  discussionCategoryName: String? = null,
  generateReleaseNotes: Boolean = false,
  makeLatest: MakeLatest? = null,
): CreateRelease.ResponseBody {
  val resource = CreateRelease.Route(
    owner = owner,
    repo = repo,
  )
  val request = CreateRelease.RequestBody(
    tagName = tagName,
    targetCommitish = targetCommitish,
    name = name,
    body = body,
    draft = draft,
    prerelease = prerelease,
    discussionCategoryName = discussionCategoryName,
    generateReleaseNotes = generateReleaseNotes,
    makeLatest = makeLatest,
  )
  val response = httpClient.post(resource = resource) {
    setBody(request)
  }
  return response.body()
}


/**
 * ### Upload a release asset
 *
 * This endpoint makes use of a [Hypermedia relation](https://docs.github.com/rest/using-the-rest-api/getting-started-with-the-rest-api#hypermedia) to determine which URL to access.
 * The endpoint you call to upload release assets is specific to your release.
 * Use the `upload_url` returned in
 * the response of the [Create a release endpoint](https://docs.github.com/rest/releases/releases#create-a-release) to upload a release asset.
 *
 * You need to use an HTTP client which supports [SNI](http://en.wikipedia.org/wiki/Server_Name_Indication) to make calls to this endpoint.
 *
 * Most libraries will set the required `Content-Length` header automatically.
 * Use the required `Content-Type` header to provide the media type of the asset.
 * For a list of media types, see [Media Types](https://www.iana.org/assignments/media-types/media-types.xhtml).
 * For example:
 *
 * `application/zip`
 *
 * GitHub expects the asset data in its raw binary form, rather than JSON.
 * You will send the raw binary content of the asset as the request body.
 * Everything else about the endpoint is the same as the rest of the API.
 * For example,
 * you'll still need to pass your authentication to be able to upload an asset.
 *
 * When an upstream failure occurs, you will receive a `502 Bad Gateway` status.
 * This may leave an empty asset with a state of `starter`.
 * It can be safely deleted.
 *
 * **Notes:**
 * - GitHub renames asset filenames that have special characters, non-alphanumeric characters, and leading or trailing periods.
 * The "[List release assets](https://docs.github.com/rest/releases/assets#list-release-assets)"
 * endpoint lists the renamed filenames.
 * For more information and help, contact [GitHub Support](https://support.github.com/contact?tags=dotcom-rest-api).
 * - To find the `release_id` query the [`GET /repos/{owner}/{repo}/releases/latest` endpoint](https://docs.github.com/rest/releases/releases#get-the-latest-release).
 *
 * - If you upload an asset with the same filename as another uploaded asset, you'll receive an error and must delete the old file before you can re-upload the new asset.
 *
 * https://docs.github.com/en/rest/releases/assets?apiVersion=2022-11-28#upload-a-release-asset
 */
suspend fun GitHubClient.Repos.uploadReleaseAsset(
  owner: String,
  repo: String,
  releaseId: Int,
  file: Path,
  name: String = file.name,
  label: String? = null,
): UploadReleaseAsset.ResponseBody {
  val resource = UploadReleaseAsset.Route(
    owner = owner,
    repo = repo,
    releaseId = releaseId,
    name = name,
    label = label,
  )
  val result = httpClient.post(resource = resource) {
    url {
      host = "uploads.github.com"
    }
    setBody(file.readChannel())
    headers {
      append(HttpHeaders.ContentLength, file.fileSize().toString())
    }
  }
  return try {
    result.body()
  } catch (e: ContentConvertException) {
    throw RuntimeException("Failed to upload asset: ${e.message}. ${result.bodyAsText()}", e)
  }
}


///**
// * ## List release assets
// *
// * @param[perPage]
// * The number of results per page (max 100).
// * For more information, see
// * ["Using pagination in the REST API."](https://docs.github.com/en/rest/using-the-rest-api/using-pagination-in-the-rest-api?apiVersion=2022-11-28)
// */
//private suspend fun GitHubClient.Repos.listReleaseAssets(
//  owner: String,
//  repo: String,
//  releaseId: Int,
//  perPage: Int? = null,
//  page: Int? = null,
//): ListReleaseAssets.ResponseBody {
//  val resource = ListReleaseAssets.Route(
//    owner = owner,
//    repo = repo,
//    releaseId = releaseId,
//    perPage = perPage,
//    page = page,
//  )
//  val response = httpClient.get(resource = resource)
//  return response.body()
//}

/**
 * ## List release assets
 *
 * @param[perPage]
 * The number of results per page (max 100).
 * For more information, see
 * ["Using pagination in the REST API."](https://docs.github.com/en/rest/using-the-rest-api/using-pagination-in-the-rest-api?apiVersion=2022-11-28)
 */
fun GitHubClient.Repos.listAllReleaseAssets(
  owner: String,
  repo: String,
  releaseId: Int,
  perPage: Int? = null,
  page: Int = 1,
): Flow<RepoReleaseAsset> {
  var page = page
  return flow {
    do {
      val resource = ListReleaseAssets.Route(
        owner = owner,
        repo = repo,
        releaseId = releaseId,
        perPage = perPage,
        page = page++,
      )
      val response = httpClient.get(resource = resource)

      val assets: ListReleaseAssets.ResponseBody = response.body()

      if (assets.isEmpty()) break

      assets.forEach { asset ->
        emit(asset)
      }

      val linkHeader = response.headers["link"].orEmpty()
      val hasNext = "rel=\"next\"" in linkHeader
      if (!hasNext) break

    } while (currentCoroutineContext().isActive)
  }
}


suspend fun GitHubClient.Repos.listReleases(
  owner: String,
  repo: String,
  perPage: Int? = null,
  page: Int? = null,
): ListReleases.ResponseBody {
  val resource = ListReleases.Route(
    owner = owner,
    repo = repo,
    perPage = perPage,
    page = page,
  )
  val response = httpClient.get(resource = resource)
  return response.body()
}


fun GitHubClient.Repos.listAllReleases(
  owner: String,
  repo: String,
  perPage: Int? = null,
  page: Int = 1,
): Flow<RepoRelease> {
  var page = page
  return flow {
    do {
      val releases = listReleases(
        owner = owner,
        repo = repo,
        perPage = perPage,
        page = page++,
      )
      if (releases.isEmpty()) break
      releases.forEach { asset ->
        emit(asset)
      }
    } while (currentCoroutineContext().isActive)
  }
}
