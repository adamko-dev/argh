package dev.adamko.githubapiclient.endpoints

import kotlinx.serialization.SerialName

interface PaginatedRoute {
  /**
   *
   * The number of results per page (max 100).
   * For more information, see
   * ["Using pagination in the REST API."](https://docs.github.com/en/rest/using-the-rest-api/using-pagination-in-the-rest-api?apiVersion=2022-11-28)
   */
  @SerialName("per_page")
  val perPage: Int?
  /**
   * The page number of the results to fetch.
   * ["Using pagination in the REST API."](https://docs.github.com/en/rest/using-the-rest-api/using-pagination-in-the-rest-api?apiVersion=2022-11-28)
   */
  val page: Int?
}
