package buildsrc

import org.gradle.api.attributes.Bundling.BUNDLING_ATTRIBUTE
import org.gradle.api.attributes.Bundling.SHADOWED
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.LIBRARY
import org.gradle.api.attributes.LibraryElements.JAR
import org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE

fun reposiliteJarResolver(): NamedDomainObjectProvider<ResolvableConfiguration> {
  val dependencyScope = project.configurations.dependencyScope("reposiliteClasspath") {
    defaultDependencies {
      add(project.dependencies.create("com.reposilite:reposilite:3.5.26"))
    }
  }

  return project.configurations.resolvable(dependencyScope.name + "Resolver") {
    extendsFrom(dependencyScope.get())
    attributes {
      attribute(CATEGORY_ATTRIBUTE, project.objects.named(LIBRARY))
      attribute(BUNDLING_ATTRIBUTE, project.objects.named(SHADOWED))
      attribute(LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(JAR))
      attribute(USAGE_ATTRIBUTE, project.objects.named(JAVA_RUNTIME))
    }
  }
}

val projectPathAsFileName: String =
  project.path.map { if (it.isLetterOrDigit()) it else "_" }.joinToString("")


val reposiliteDir: Provider<Directory> =
  objects.directoryProperty()
    .fileProvider(
      providers.environmentVariable("REPOSILITE_DIR")
        .map { File(it).resolve(projectPathAsFileName) }
    )
    .orElse(layout.buildDirectory.dir("reposilite"))


val serviceProvider: Provider<MavenRepositoryMirrorService> = project.gradle.sharedServices.registerIfAbsent(
  "mavenRepositoryMirrorService_${project.path}",
  MavenRepositoryMirrorService::class
) {
  val layout = project.layout
  parameters.reposiliteJar.from(reposiliteJarResolver())

  parameters.reposiliteDir.set(reposiliteDir)
}

project.extensions.create("mavenRepositoryMirror", MavenRepositoryMirrorExtension::class, serviceProvider)
