import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.resources.*
import kotlin.time.Instant
import kotlinx.serialization.*
import kotlinx.serialization.json.*

typealias Composed = String

typealias Object = String

typealias Email = String

@Serializable
@JvmInline
value class Date(val date: String)
