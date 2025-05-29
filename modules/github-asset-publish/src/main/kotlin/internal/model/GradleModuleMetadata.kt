@file:OptIn(ExperimentalSerializationApi::class)

package dev.adamko.githubassetpublish.internal.model

import dev.adamko.githubassetpublish.internal.model.GradleModuleMetadataSpec.AttributeValue
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GradleModuleMetadata(
  @EncodeDefault
  override val formatVersion: String = "1.1",
  override val component: Component,
  override val createdBy: CreatedBy,
  @EncodeDefault
  override val variants: List<Variant> = emptyList(),
) : GradleModuleMetadataSpec {

  @Serializable
  data class Component(
    override val group: String,
    override val module: String,
    override val version: String,
    override val url: String? = null,
    override val attributes: Map<String, AttributeValue> = emptyMap(),
  ) : GradleModuleMetadataSpec.Component

  @Serializable
  data class CreatedBy(
    override val gradle: Gradle? = null
  ) : GradleModuleMetadataSpec.CreatedBy {
    @Serializable
    data class Gradle(
      override val version: String,
      override val buildId: String? = null,
    ) : GradleModuleMetadataSpec.CreatedBy.Gradle
  }

  @Serializable
  data class Variant(
    override val name: String,
    override val attributes: Map<String, AttributeValue> = emptyMap(),
    @SerialName("available-at")
    override val availableAt: AvailableAt? = null,
    override val dependencies: List<Dependency> = emptyList(),
    override val dependencyConstraints: List<DependencyConstraint> = emptyList(),
    override val files: List<VariantFile> = emptyList(),
    override val capabilities: List<Capability> = emptyList(),
  ) : GradleModuleMetadataSpec.Variant

  @Serializable
  data class Dependency(
    override val group: String,
    override val module: String,
    override val version: VersionConstraint? = null,
    override val excludes: List<Exclude> = emptyList(),
    override val reason: String? = null,
    override val attributes: Map<String, AttributeValue> = emptyMap(),
    override val requestedCapabilities: List<Capability> = emptyList(),
    override val endorseStrictVersions: Boolean? = null,
    override val thirdPartyCompatibility: ThirdPartyCompatibility? = null
  ) : GradleModuleMetadataSpec.Dependency

  @Serializable
  data class DependencyConstraint(
    override val group: String,
    override val module: String,
    override val version: VersionConstraint? = null,
    override val reason: String? = null,
    override val attributes: Map<String, AttributeValue> = emptyMap(),
  ) : GradleModuleMetadataSpec.DependencyConstraint

  @Serializable
  data class VersionConstraint(
    override val requires: String? = null,
    override val prefers: String? = null,
    override val strictly: String? = null,
    override val rejects: List<String> = emptyList(),
  ) : GradleModuleMetadataSpec.VersionConstraint

  @Serializable
  data class Exclude(
    override val group: String,
    override val module: String,
  ) : GradleModuleMetadataSpec.Exclude

  @Serializable
  data class Capability(
    override val group: String,
    override val name: String,
    override val version: String,
  ) : GradleModuleMetadataSpec.Capability

  @Serializable
  data class VariantFile(
    override val name: String,
    override val url: String,
    override val size: Int,
    override val sha512: String,
    override val sha256: String,
    override val sha1: String,
    override val md5: String,
  ) : GradleModuleMetadataSpec.VariantFile

  @Serializable
  data class AvailableAt(
    override val url: String,
    override val group: String,
    override val module: String,
    override val version: String,
  ) : GradleModuleMetadataSpec.AvailableAt

  @Serializable
  data class ThirdPartyCompatibility(
    override val artifactSelector: ArtifactSelector? = null
  ) : GradleModuleMetadataSpec.ThirdPartyCompatibility

  @Serializable
  data class ArtifactSelector(
    override val name: String? = null,
    override val type: String? = null,
    override val extension: String? = null,
    override val classifier: String? = null,
  ) : GradleModuleMetadataSpec.ArtifactSelector
}
