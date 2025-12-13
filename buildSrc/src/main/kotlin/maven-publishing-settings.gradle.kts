package buildsrc

import buildsrc.settings.MavenPublishingSettings

val mavenPublishing: MavenPublishingSettings =
  extensions.create<MavenPublishingSettings>(MavenPublishingSettings.EXTENSION_NAME, project)


mavenPublishing.pomName.convention("Argh | ${project.name}")
mavenPublishing.pomDescription.convention(provider { project.description })
