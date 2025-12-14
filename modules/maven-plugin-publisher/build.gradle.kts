@file:Suppress("UnstableApiUsage")

plugins {
  buildsrc.`kotlin-maven-plugin`
}

description = "Argh Maven plugin for attaching Maven publications as GitHub Release Assets."
mavenPublishing.pomName = "Argh | Maven Publisher Plugin"

dependencies {
  implementation(projects.modules.libGmm)
  devPublication(projects.modules.libGmm)

  implementation(libs.apacheMaven.pluginApi)
  implementation(libs.apacheMaven.core)
  compileOnly(libs.apacheMaven.pluginAnnotations)

  implementation(libs.apacheMaven.model)
  implementation(libs.apacheMaven.modelBuilder)

  compileOnly(libs.slf4j.api)
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
    tasks.check { dependsOn(testIntegration) }
  }
}
