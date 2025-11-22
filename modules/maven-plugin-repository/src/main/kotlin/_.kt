package dev.adamko.githubassetpublish.maven

import java.net.URI

/**
 * Helper function to modify a [URI].
 * Allows changing any part of the URI:
 * [URI.scheme], [URI.authority], [URI.path], [URI.query], [URI.fragment].
 *
 * @return The modified URI.
 */
internal fun URI.modify(
  modify: URIComponents.() -> Unit,
): URI {
  val components = URIComponents(
    scheme = this.scheme,
    authority = this.authority,
    path = this.path,
    query = this.query,
    fragment = this.fragment
  )

  components.modify()

  return URI(
    components.scheme,
    components.authority,
    components.path,
    components.query,
    components.fragment,
  )
}

internal class URIComponents(
  var scheme: String?,
  var authority: String?,
  var path: String?,
  var query: String?,
  var fragment: String?,
)
