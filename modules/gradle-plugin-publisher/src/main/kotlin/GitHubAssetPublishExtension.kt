package dev.adamko.githubassetpublish

import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

abstract class GitHubAssetPublishExtension
@Inject
internal constructor(
//  private val objects: ObjectFactory,
////  private val providers: ProviderFactory,
) {

  /**
   * GitHub repository identifier, e.g. `my-org/my-repo`.
   */
  abstract val githubRepo: Property<String>

  internal abstract val gapBuildDir: DirectoryProperty
  internal abstract val stagingRepoDir: DirectoryProperty
//  internal abstract val assertsDir: DirectoryProperty

  val stagingRepoName: String = "GitHubAssetPublishStaging"

//  val publications: NamedDomainObjectContainer<PublicationSpec> =
//    objects.domainObjectContainer(PublicationSpec::class) { name ->
//      objects.newPublicationSpec(
//        name = name,
////        groupId = providers.provider { }
////            artifactId = providers.provider { }
////            version = providers.provider { }
//      )
////      objects.newInstance(PublicationSpec::class, it).also { spec ->
////        spec.enabled.convention(true)
////      }
//    }.also { container ->
//      extensions.add("publications", container)
//    }

//  private val extensions: ExtensionContainer
//    get() = (this as ExtensionAware).extensions
}
