package dev.adamko.githubassetpublish.maven.metadata

import dev.adamko.githubassetpublish.lib.internal.model.GradleModuleMetadata

/**
 * Convert a Gradle Module Metadata file into a basic Maven POM XML string.
 *
 * Assumptions:
 * - Java consumer
 * - We pick a variant that best approximates the Maven "runtime" view, usually:
 *   - org.gradle.usage = java-runtime
 *   - or falls back to the first variant if none match.
 */
internal fun GradleModuleMetadata.convertToPomXml(): String {
  return toPomXml(this)
}


private fun toPomXml(metadata: GradleModuleMetadata): String {
  val component = metadata.component
  val variant = selectJavaRuntimeVariant(metadata)
  val dependenciesXml = buildDependenciesXml(variant)

  return buildString {
    appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
    appendLine("""<project xmlns="http://maven.apache.org/POM/4.0.0"""")
    appendLine("""         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"""")
    appendLine("""         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 """)
    appendLine("""         http://maven.apache.org/xsd/maven-4.0.0.xsd">""")
    appendLine("  <modelVersion>4.0.0</modelVersion>")
    appendLine("  <groupId>${component.group}</groupId>")
    appendLine("  <artifactId>${component.module}</artifactId>")
    appendLine("  <version>${component.version}</version>")
    appendLine("  <packaging>jar</packaging>")

    // A minimal <name> can be helpful but is optional.
//    appendLine("  <name>${xmlEscape(component.group + ":" + component.module)}</name>")

    if (dependenciesXml.isNotEmpty()) {
      appendLine("  <dependencies>")
      append(dependenciesXml)
      appendLine("  </dependencies>")
    }

    appendLine("</project>")
  }
}


/**
 * Heuristically select a Java runtime variant:
 * - Prefer org.gradle.usage=java-runtime
 * - Otherwise fall back to the first variant, if any.
 */
private fun selectJavaRuntimeVariant(
  metadata: GradleModuleMetadata
): GradleModuleMetadata.Variant? {
  // Prefer usage=java-runtime
  val runtimeVariant = metadata.variants.firstOrNull { v ->
    val usage = v.attributes["org.gradle.usage"] as? GradleModuleMetadata.StringAttribute
    usage?.value in setOf(
      "java-runtime",
      "java-api",
    )
  }
  if (runtimeVariant != null) return runtimeVariant

  // Fallback: any variant that looks like JVM/Java
  val jvmLikeVariant = metadata.variants.firstOrNull { v ->
    val usage = v.attributes["org.gradle.usage"]
    val targetJvm = v.attributes["org.gradle.jvm.version"]
    usage != null || targetJvm != null
  }
  if (jvmLikeVariant != null) {
    return jvmLikeVariant
  }

  return null
}

private fun buildDependenciesXml(
  variant: GradleModuleMetadata.Variant?
): String {
  if (variant == null) return ""

  return buildString {
    val availableAt = variant.availableAt

    if (availableAt != null) {
      appendLine("    <dependency>")
      appendLine("      <groupId>${availableAt.group}</groupId>")
      appendLine("      <artifactId>${availableAt.module}</artifactId>")
      appendLine("      <version>${availableAt.version}</version>")
      appendLine("    </dependency>")
    }

    for (dep in variant.dependencies) {

      val version = dep.version?.let { vc ->
        // Gradle metadata may have several version fields; pick the closest to Maven's idea.
        vc.strictly ?: vc.requires ?: vc.prefers
      }

      appendLine("    <dependency>")
      appendLine("      <groupId>${dep.group}</groupId>")
      appendLine("      <artifactId>${dep.module}</artifactId>")

      if (version != null && version.isNotBlank()) {
        appendLine("      <version>${version}</version>")
      }

      // Assume a Java consumer using the runtime view; use 'compile' as a safe default scope.
      // (You can refine this later by inspecting attributes or variants.)
      appendLine("      <scope>compile</scope>")

      // Exclusions
      if (dep.excludes.isNotEmpty()) {
        appendLine("      <exclusions>")
        for (exclude in dep.excludes) {
          appendLine("        <exclusion>")
          appendLine("          <groupId>${exclude.group}</groupId>")
          appendLine("          <artifactId>${exclude.module}</artifactId>")
          appendLine("        </exclusion>")
        }
        appendLine("      </exclusions>")
      }

      appendLine("    </dependency>")
    }
  }
}
