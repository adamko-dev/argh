package dev.adamko.githubapiclient

import dev.adamko.githubapiclient.endpoints.PaginatedRoute
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
 * ### Get a release asset
 *
 * To download the asset's binary content use [downloadReleaseAsset].
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
  page: Int? = null,
): Flow<RepoReleaseAsset> =
  paginatedResponses(page = page) { page ->
    ListReleaseAssets.Route(
      owner = owner,
      repo = repo,
      releaseId = releaseId,
      perPage = perPage,
      page = page,
    )
  }


fun GitHubClient.Repos.listAllReleases(
  owner: String,
  repo: String,
  perPage: Int? = null,
  page: Int? = null,
): Flow<RepoRelease> =
  paginatedResponses(page = page) { page ->
    ListReleases.Route(
      owner = owner,
      repo = repo,
      perPage = perPage,
      page = page,
    )
  }


private inline fun <reified T : Any, reified R : PaginatedRoute> GitHubClient.Repos.paginatedResponses(
  page: Int? = null,
  crossinline routeProvider: (page: Int) -> R,
): Flow<T> = flow {
  var page = page ?: 1
  do {
    val route: R = routeProvider(page++)
    val response = httpClient.get(resource = route)

    val elements: List<T> = response.body()

    if (elements.isEmpty()) break

    elements.forEach { element ->
      emit(element)
    }

    val linkHeader = response.headers["link"].orEmpty()
    val hasNext = "rel=\"next\"" in linkHeader
    if (!hasNext) break

  } while (currentCoroutineContext().isActive)
}
