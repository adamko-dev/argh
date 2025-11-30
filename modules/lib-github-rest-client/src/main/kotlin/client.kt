import io.ktor.client.HttpClient


suspend fun demo(
  gh: GitHubClient,
//  httpClient: HttpClient,
) {
//  httpClient.get
  gh.repos.getRelease(
    owner = "",
    repo = "",
    releaseId = 1,
  )
}
