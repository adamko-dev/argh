package dev.adamko.githubapiclient.model

import kotlinx.serialization.Serializable

typealias Composed = String

typealias Object = String

typealias Email = String

@Serializable
@JvmInline
value class Date(val date: String)
