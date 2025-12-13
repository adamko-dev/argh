package dev.adamko.argh.maven.repository.metadata

//import dev.adamko.githubassetpublish.lib.internal.model.GradleModuleMetadata
//
//import java.io.StringWriter
//import org.apache.maven.model.Dependency
//import org.apache.maven.model.Exclusion
//import org.apache.maven.model.Model
//import org.apache.maven.model.io.xpp3.MavenXpp3Writer
//
///**
// * Convert a Gradle Module Metadata file into a basic Maven POM XML string.
// *
// * Assumptions:
// * - Java consumer
// * - We pick a variant that best approximates the Maven "runtime" view, usually:
// *   - org.gradle.usage = java-runtime
// *   - or falls back to the first variant if none match.
// */
//internal fun GradleModuleMetadata.convertToPomXml(): String {
//  return toPomXml(this)
//}
//
//
//private fun toPomXml(metadata: GradleModuleMetadata): String {
//  val component = metadata.component
//  val variant = selectJavaRuntimeVariant(metadata)
//
//  val model = createModel(component)
//
//  if (variant != null) {
//    addDependencies(model, variant)
//  }
//
//  StringWriter().use { sink ->
//    MavenXpp3Writer().write(sink, model)
//    return sink.toString()
//  }
//}
//
//private fun createModel(
//  component: GradleModuleMetadata.Component,
//): Model = Model().apply {
//  modelVersion = "4.0.0"
//  groupId = component.group
//  artifactId = component.module
//  version = component.version
//
//  // TODO 'pom' packaging if no variants have files?
//  packaging = "jar"
//
//  // A minimal <name> can be helpful but is optional.
//  name = "${component.group}:${component.module}"
//}
//
//
///**
// * Heuristically select a Java runtime variant:
// * - Prefer `org.gradle.usage=java-runtime`
// * - Otherwise fall back to the first variant, if any.
// */
//private fun selectJavaRuntimeVariant(
//  metadata: GradleModuleMetadata
//): GradleModuleMetadata.Variant? {
//  // Prefer usage=java-runtime
//  val runtimeVariant = metadata.variants.firstOrNull { v ->
//    val usage = v.attributes["org.gradle.usage"] as? GradleModuleMetadata.StringAttribute
//    usage?.value in setOf(
//      "java-runtime",
//      "java-api",
//    )
//  }
//  if (runtimeVariant != null) return runtimeVariant
//
//  // Fallback: any variant that looks like JVM/Java
//  val jvmLikeVariant = metadata.variants.firstOrNull { v ->
//    val usage = v.attributes["org.gradle.usage"]
//    val targetJvm = v.attributes["org.gradle.jvm.version"]
//    usage != null || targetJvm != null
//  }
//  if (jvmLikeVariant != null) {
//    return jvmLikeVariant
//  }
//
//  return null
//}
//
//private fun addDependencies(
//  model: Model,
//  variant: GradleModuleMetadata.Variant,
//) {
//  val availableAt = variant.availableAt
//
//  if (availableAt != null) {
//    val dep = Dependency()
//    dep.groupId = availableAt.group
//    dep.artifactId = availableAt.module
//    dep.version = availableAt.version
//    model.addDependency(dep)
//  }
//
//  for (depData in variant.dependencies) {
//    val version = depData.version?.run {
//      // Gradle metadata may have several version fields: pick the closest to Maven's idea
//      strictly ?: requires ?: prefers
//    }
//
//    val dep = Dependency()
//    dep.groupId = depData.group
//    dep.artifactId = depData.module
//
//    if (version != null && version.isNotBlank()) {
//      dep.version = version
//    }
//
//    // assume a Java consumer using the runtime view, use 'compile' as a safe default scope
//    // TODO determine scope by inspecting attributes or variants
//    dep.scope = "compile"
//
//    // Exclusions
//    if (depData.excludes.isNotEmpty()) {
//      for (excludeData in depData.excludes) {
//        val exclusion = Exclusion()
//        exclusion.groupId = excludeData.group
//        exclusion.artifactId = excludeData.module
//        dep.addExclusion(exclusion)
//      }
//    }
//
//    model.addDependency(dep)
//  }
//}
