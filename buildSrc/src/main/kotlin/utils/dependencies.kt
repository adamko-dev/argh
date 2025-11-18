package buildsrc.utils

/**
 * Define a dependency using Gradle's 'single-string notation'.
 */
fun createDependencyNotation(
  group: String?,
  name: String,
  version: String?,
  classifier: String?,
  extension: String?,
): String =
  buildString {
    if (group != null) append(group)
    append(":")
    append(name)
    append(":")
    if (version != null) append(version)
    if (classifier != null) {
      append(":")
      append(classifier)
    }
    if (extension != null) {
      append("@")
      append(extension)
    }
  }
