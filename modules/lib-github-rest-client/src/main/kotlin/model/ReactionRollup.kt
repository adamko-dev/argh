package dev.adamko.githubapiclient.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReactionRollup internal constructor(
  val url: String,
  @SerialName("total_count")
  val totalCount: Long,
  @SerialName("+1")
  val plusOne: Long,
  @SerialName("-1")
  val minusOne: Long,
  val laugh: Long,
  val confused: Long,
  val heart: Long,
  val hooray: Long,
  val eyes: Long,
  val rocket: Long,
)
