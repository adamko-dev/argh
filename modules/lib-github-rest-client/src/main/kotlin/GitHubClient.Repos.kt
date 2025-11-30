import io.ktor.client.plugins.resources.*


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
): java.lang.Object {
  val resource = GitHubClient.Repos.GetRelease.Route(
    owner = owner,
    repo = repo,
    releaseId = releaseId,
  )
  val result = httpClient.get(resource = resource) {}
  TODO()
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
  assetId: Int,
): java.lang.Object {
  val resource = GitHubClient.Repos.GetReleaseAsset.Route(
    owner = owner,
    repo = repo,
    assetId = assetId,
  )
  val result = httpClient.get(resource = resource) {}
  TODO()
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
): java.lang.Object {
  val resource = GitHubClient.Repos.GetReleaseByTag.Route(
    owner = owner,
    repo = repo,
    tag = tag,
  )
  val result = httpClient.get(resource = resource) {}
  TODO()
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
 * *   GitHub renames asset filenames that have special characters, non-alphanumeric characters, and leading or trailing periods.
 * The "[List release assets](https://docs.github.com/rest/releases/assets#list-release-assets)"
 * endpoint lists the renamed filenames.
 * For more information and help, contact [GitHub Support](https://support.github.com/contact?tags=dotcom-rest-api).
 * *   To find the `release_id` query the [`GET /repos/{owner}/{repo}/releases/latest` endpoint](https://docs.github.com/rest/releases/releases#get-the-latest-release).
 *
 * *   If you upload an asset with the same filename as another uploaded asset, you'll receive an error and must delete the old file before you can re-upload the new asset.
 *
 * `repos/upload-release-asset`
 */
suspend fun GitHubClient.Repos.uploadReleaseAsset(
  owner: String,
  repo: String,
  releaseId: Int,
  name: String,
  label: String? = null,
) {
  val resource = GitHubClient.Repos.UploadReleaseAsset.Route(
    owner = owner,
    repo = repo,
    releaseId = releaseId,
  )
  val result = httpClient.post(resource = resource) {}
  TODO()
}
