package buildsrc

import buildsrc.tasks.MvnExec
import buildsrc.utils.createDependencyNotation
import org.gradle.kotlin.dsl.support.serviceOf

/**
 * Utility for downloading and installing a Maven binary.
 *
 * Provides the `setupMavenProperties` extension that contains the default versions and locations
 * of the Maven binary.
 *
 * The task [installMavenBinary] will download and unzip the Maven binary.
 */

plugins {
  base
}

val mavenCliSetupExtension =
  extensions.create("mavenCliSetup", MavenCliSetupExtension::class).apply {
//    mavenVersion.convention(libs.versions.apacheMaven.core)
//    mavenPluginToolsVersion.convention(libs.versions.apacheMaven.pluginTools)

    mavenInstallDir.convention(layout.buildDirectory.dir("apache-maven"))

    val isWindowsProvider =
      providers.systemProperty("os.name").map { "win" in it.lowercase() }

    mvn.convention(
      providers.zip(mavenInstallDir, isWindowsProvider) { mavenInstallDir, isWindows ->
        mavenInstallDir.file(
          when {
            isWindows -> "bin/mvn.cmd"
            else      -> "bin/mvn"
          }
        )
      }
    )
  }

val mavenBinary = configurations.dependencyScope("mavenBinary") {
  description = "used to download the Maven binary"

  defaultDependencies {
    addLater(mavenCliSetupExtension.mavenVersion.map { mavenVersion ->
      project.dependencies.create(
        createDependencyNotation(
          group = "org.apache.maven",
          name = "apache-maven",
          version = mavenVersion,
          classifier = "bin",
          extension = "zip",
        )
      )
    })
  }
}
val mavenBinaryResolver = configurations.resolvable("mavenBinaryResolver") {
  description = "Resolves ${mavenBinary.name}."
  extendsFrom(mavenBinary.get())
}

tasks.clean {
  delete(mavenCliSetupExtension.mavenInstallDir)
}

val installMavenBinary by tasks.registering {
  val archives = serviceOf<ArchiveOperations>()
  val fs = serviceOf<FileSystemOperations>()

  val mavenBinaryFiles: Provider<Set<File>> = mavenBinaryResolver.map { it.files }
  inputs.files(mavenBinaryFiles)
    .withNormalizer(ClasspathNormalizer::class)

  val mavenInstallDir = mavenCliSetupExtension.mavenInstallDir
  outputs.dir(mavenInstallDir)

  doLast {
    fs.sync {
      from(
        mavenBinaryFiles
          .map { artifacts ->
            artifacts.map { archives.zipTree(it) }
          }
      ) {
        eachFile {
          // drop the first directory inside the zipped Maven bin (apache-maven-$version)
          relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
        }
        includeEmptyDirs = false
      }
      into(mavenInstallDir)
    }
  }
}

tasks.withType<MvnExec>().configureEach {
  group = MavenCliSetupExtension.MAVEN_PLUGIN_TASK_GROUP
  dependsOn(installMavenBinary)
  mvnCli.convention(mavenCliSetupExtension.mvn)
  workDirectory.convention(layout.dir(provider { temporaryDir }))
  showErrors.convention(true)
  batchMode.convention(true)
//  settingsXml.convention(layout.projectDirectory.file("settings.xml"))
}
