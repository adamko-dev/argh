@file:Suppress("UnstableApiUsage")

import buildsrc.tasks.MvnExec


plugins {
  buildsrc.`kotlin-maven-plugin`
}

dependencies {
  implementation(projects.modules.libGmm)
  devPublication(projects.modules.libGmm)

  implementation(libs.apacheMaven.pluginApi)
  implementation(libs.apacheMaven.core)
  //implementation(libs.apacheMaven.resolverSpi)
  compileOnly(libs.apacheMaven.pluginAnnotations)

  implementation(libs.apacheMaven.model)
  implementation(libs.apacheMaven.modelBuilder)


//  implementation("org.apache.maven:maven-plugin-api:${libs.versions.apacheMaven.core.get()}")
//  implementation("org.apache.maven:maven-core:${libs.versions.apacheMaven.core.get()}")
//  compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:${libs.versions.apacheMaven.pluginTools.get()}")

  compileOnly(libs.slf4j.api)
}

mavenCliSetup {
  mavenVersion.set(libs.versions.apacheMaven.core)
  mavenPluginToolsVersion.set(libs.versions.apacheMaven.pluginTools)
}

tasks.withType<Test>().configureEach {
  dependsOn(tasks.installMavenBinary)
  val mvnBinary = mavenCliSetup.mvn
  jvmArgumentProviders.add {
    mvnBinary.get().asFile.absolutePath.let { listOf("-DmvnBinary=$it") }
  }

  val testMavenLocalDir = temporaryDir.resolve("testMavenRepo")
  systemProperty("testMavenLocalDir", testMavenLocalDir)
}

testing {
  suites {
    withType<JvmTestSuite>().configureEach {
      targets.configureEach {
        testTask.configure {
          systemProperty("junit.jupiter.tempdir.cleanup.mode.default", "ON_SUCCESS")
        }
      }
    }

    val test by getting(JvmTestSuite::class) {
      dependencies {
        implementation(platform("org.junit:junit-bom:6.0.1"))
        implementation("org.junit.jupiter:junit-jupiter")
        runtimeOnly("org.junit.platform:junit-platform-launcher")
      }
    }

    val testIntegration by registering(JvmTestSuite::class) {
      dependencies {
        implementation(platform("org.junit:junit-bom:6.0.1"))
        implementation("org.junit.jupiter:junit-jupiter")
        runtimeOnly("org.junit.platform:junit-platform-launcher")
      }

      targets.configureEach {
        testTask.configure {
          val devMavenRepo = devPublish.devMavenRepo
          dependsOn(tasks.updateDevRepo)
          jvmArgumentProviders.add {
            listOf(
              "-DdevMavenRepo=${devMavenRepo.get().asFile.invariantSeparatorsPath}"
            )
          }

          val mavenRepositoryMirrorService = project.mavenRepositoryMirror.serviceProvider
          usesService(mavenRepositoryMirrorService)

          jvmArgumentProviders.add {
            listOf(
              "-DlocalMavenMirrorPort=${mavenRepositoryMirrorService.get().launch()}"
            )
          }

          javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) })
        }
      }
    }
    tasks.check {
      dependsOn(test)
      dependsOn(testIntegration)
    }
  }
}


tasks.withType<MvnExec>().configureEach {
  javaVersion.set(JavaLanguageVersion.of(21))
}
