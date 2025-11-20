package buildsrc

import buildsrc.tasks.MvnExec
import org.gradle.internal.extensions.core.serviceOf

plugins {
  id("buildsrc.kotlin-lib")
  id("buildsrc.maven-cli-setup")
  id("buildsrc.maven-repository-mirror")
}

val mavenCliSetup :  MavenCliSetupExtension = extensions.getByType()

val mavenPluginTaskGroup = "maven plugin"

val generatePom by tasks.registering {
  description = "Generate pom.xml for Maven Plugin Plugin"
  group = mavenPluginTaskGroup

  val fs = serviceOf<FileSystemOperations>()

  val projectVersion = provider { project.version.toString() }
  inputs.property("projectVersion", projectVersion)

  val groupId = provider { project.group.toString() }
  inputs.property("groupId", groupId)

  val artifactId = provider { project.name }
  inputs.property("artifactId", artifactId)

  val mavenVersion = mavenCliSetup.mavenVersion
  inputs.property("mavenVersion", mavenVersion)

  val mavenPluginToolsVersion = mavenCliSetup.mavenPluginToolsVersion
  inputs.property("mavenPluginToolsVersion", mavenPluginToolsVersion)

  val pomTemplateFile = layout.projectDirectory.file("pom.template.xml")
  inputs.file(pomTemplateFile)
    .withPropertyName("pomTemplateFile")
    .normalizeLineEndings()
    .withPathSensitivity(PathSensitivity.NAME_ONLY)

  outputs.dir(temporaryDir)

  doLast {
    fs.sync {
      from(pomTemplateFile) {
        rename { it.replace(".template.xml", ".xml") }

        expand(
          "mavenVersion" to mavenVersion.get(),
          "projectVersion" to projectVersion.get(),
          "artifactId" to artifactId.get(),
          "groupId" to groupId.get(),
          "mavenPluginToolsVersion" to mavenPluginToolsVersion.get(),
        )
      }

      into(temporaryDir)
    }
  }
}

val generateHelpMojo by tasks.registering(MvnExec::class) {
  description = "Generate the Maven Plugin HelpMojo"
  group = mavenPluginTaskGroup

  resources.from(generatePom)
  arguments.addAll(
    "org.apache.maven.plugins:maven-plugin-plugin:helpmojo",
    "-DhelpPackageName=dev.adamko",
    "-DgoalPrefix=githubAssetPublish",
  )
}

//val helpMojoSources by tasks.registering(Sync::class) {
//  description = "Sync the HelpMojo source files into a SourceSet SrcDir"
//  group = mavenPluginTaskGroup
//
//  from(generateHelpMojo) {
//    eachFile {
//      // Maven generates sources into `generated-sources/plugin/`,
//      // so drop 2 leading directories:
//      relativePath = RelativePath(true, *relativePath.segments.drop(2).toTypedArray())
//    }
//  }
//  includeEmptyDirs = false
//
//  into(temporaryDir)
//
//  // this task prepares generated helpmojo _sources_, so only include source files
//  include("**/*.java")
//}

val helpMojoResources by tasks.registering(Sync::class) {
  description = "Sync the HelpMojo resource files into a SourceSet SrcDir"
  group = mavenPluginTaskGroup

  from(generateHelpMojo)

  into(temporaryDir)

  // this task prepares generated helpmojo _resources_, so...
  include("**/**")       // include everything by default
  exclude("**/*.java")   // don't include source files
  // `maven-plugin-help.properties` contains an absolute path: destinationDirectory.
  // Exclude it, so that Build Cache is relocatable.
  exclude("**/maven-plugin-help.properties")

  includeEmptyDirs = false
}

sourceSets.main {
  // use the generated HelpMojo tasks as compilation input: Gradle will automatically trigger the tasks when required
//  java.srcDirs(helpMojoSources)
  resources.srcDirs(helpMojoResources)
}

val generatePluginDescriptor by tasks.registering(MvnExec::class) {
  description = "Generate the Maven Plugin descriptor"
  group = mavenPluginTaskGroup

  classes.from(tasks.compileKotlin)
  classes.from(tasks.compileJava)

  resources.from(helpMojoResources)

  arguments.addAll(
    "org.apache.maven.plugins:maven-plugin-plugin:descriptor"
  )
}

val pluginDescriptorMetaInf: Provider<RegularFile> =
  generatePluginDescriptor.flatMap {
    it.workDirectory.file("classes/java/main/META-INF/maven")
  }

tasks.jar {
  metaInf {
    from(pluginDescriptorMetaInf) {
      into("maven")
    }
  }
  manifest {
    attributes("Class-Path" to configurations.runtimeClasspath.map { configuration ->
      configuration.resolve().joinToString(" ") { it.name }
    })
  }
}
