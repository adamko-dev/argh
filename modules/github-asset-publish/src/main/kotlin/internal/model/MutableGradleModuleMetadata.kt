@file:OptIn(ExperimentalSerializationApi::class)

package dev.adamko.githubassetpublish.internal.model

import dev.adamko.githubassetpublish.internal.model.GradleModuleMetadataSpec.AttributeValue
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MutableGradleModuleMetadata(
  @EncodeDefault
  override var formatVersion: String = "1.1",
  override var component: Component,
  override var createdBy: CreatedBy,
  @EncodeDefault
  override var variants: MutableList<Variant> = mutableListOf(),
) : GradleModuleMetadataSpec {

  @Serializable
  data class Component(
    override var group: String,
    override var module: String,
    override var version: String,
    override var url: String? = null,
    override var attributes: MutableMap<String, AttributeValue> = mutableMapOf(),
  ) : GradleModuleMetadataSpec.Component

  @Serializable
  data class CreatedBy(
    override var gradle: Gradle? = null
  ) : GradleModuleMetadataSpec.CreatedBy {
    @Serializable
    data class Gradle(
      override var version: String,
      override var buildId: String? = null,
    ) : GradleModuleMetadataSpec.CreatedBy.Gradle
  }

  @Serializable
  data class Variant(
    override var name: String,
    override var attributes: MutableMap<String, AttributeValue> = mutableMapOf(),
    @SerialName("available-at")
    override var availableAt: AvailableAt? = null,
    override var dependencies: MutableList<Dependency> = mutableListOf(),
    override var dependencyConstraints: MutableList<DependencyConstraint> = mutableListOf(),
    override var files: MutableList<VariantFile> = mutableListOf(),
    override var capabilities: MutableList<Capability> = mutableListOf(),
  ) : GradleModuleMetadataSpec.Variant

  @Serializable
  data class Dependency(
    override var group: String,
    override var module: String,
    override var version: VersionConstraint? = null,
    override var excludes: MutableList<Exclude> = mutableListOf(),
    override var reason: String? = null,
    override var attributes: MutableMap<String, AttributeValue> = mutableMapOf(),
    override var requestedCapabilities: MutableList<Capability> = mutableListOf(),
    override var endorseStrictVersions: Boolean? = null,
    override var thirdPartyCompatibility: ThirdPartyCompatibility? = null
  ) : GradleModuleMetadataSpec.Dependency

  @Serializable
  data class DependencyConstraint(
    override var group: String,
    override var module: String,
    override var version: VersionConstraint? = null,
    override var reason: String? = null,
    override var attributes: MutableMap<String, AttributeValue> = mutableMapOf(),
  ) : GradleModuleMetadataSpec.DependencyConstraint

  @Serializable
  data class VersionConstraint(
    override var requires: String? = null,
    override var prefers: String? = null,
    override var strictly: String? = null,
    override var rejects: MutableList<String> = mutableListOf(),
  ) : GradleModuleMetadataSpec.VersionConstraint

  @Serializable
  data class Exclude(
    override var group: String,
    override var module: String,
  ) : GradleModuleMetadataSpec.Exclude

  @Serializable
  data class Capability(
    override var group: String,
    override var name: String,
    override var version: String,
  ) : GradleModuleMetadataSpec.Capability

  @Serializable
  data class VariantFile(
    override var name: String,
    override var url: String,
    override var size: Int,
    override var sha512: String,
    override var sha256: String,
    override var sha1: String,
    override var md5: String,
  ) : GradleModuleMetadataSpec.VariantFile

  @Serializable
  data class AvailableAt(
    override var url: String,
    override var group: String,
    override var module: String,
    override var version: String,
  ) : GradleModuleMetadataSpec.AvailableAt

  @Serializable
  data class ThirdPartyCompatibility(
    override var artifactSelector: ArtifactSelector? = null
  ) : GradleModuleMetadataSpec.ThirdPartyCompatibility

  @Serializable
  data class ArtifactSelector(
    override var name: String? = null,
    override var type: String? = null,
    override var extension: String? = null,
    override var classifier: String? = null,
  ) : GradleModuleMetadataSpec.ArtifactSelector
}
