package buildsrc


val serviceProvider: Provider<MavenRepositoryMirrorService> =
  MavenRepositoryMirrorService.register(project)

project.extensions.create("mavenRepositoryMirror", MavenRepositoryMirrorExtension::class, serviceProvider)
