import dev.adamko.github.GitHubClient


suspend fun demo(
  gh: GitHubClient
) {
  gh.repos.getRelease(
    owner = "",
    repo = "",
    releaseId = 1,
  )
}
