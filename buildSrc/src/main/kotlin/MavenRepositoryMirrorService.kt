package buildsrc

import java.io.OutputStream
import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlin.io.path.createDirectories
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.attributes.Bundling.BUNDLING_ATTRIBUTE
import org.gradle.api.attributes.Bundling.SHADOWED
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.LIBRARY
import org.gradle.api.attributes.LibraryElements.JAR
import org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.kotlin.dsl.*
import org.gradle.process.ExecOperations

abstract class MavenRepositoryMirrorService @Inject constructor(
  private val execOps: ExecOperations,
) : BuildService<MavenRepositoryMirrorService.Parameters>,
  AutoCloseable {

  interface Parameters : BuildServiceParameters {
    val reposiliteJar: ConfigurableFileCollection
    val reposiliteDir: DirectoryProperty
  }

  private val reposiliteDir: Path by lazy {
    parameters.reposiliteDir.get().asFile.toPath()
      .createDirectories()
  }

  private val port: Int by lazy {
    ServerSocket(0).use { serverSocket ->
      serverSocket.localPort
    }
  }

  private val thread: Thread =
    thread(
      name = "maven-repository-mirror-service",
      isDaemon = true,
      start = false,
    ) {
      execOps.javaexec {
        classpath = parameters.reposiliteJar
        workingDir(reposiliteDir.toFile())
        args = listOf(
          "--port=$port",
          "--hostname=127.0.0.1",
          "--no-color",
          "--token", "admin:secret",
        )
        standardOutput = OutputStream.nullOutputStream()
        errorOutput = OutputStream.nullOutputStream()
        maxHeapSize = "32m"
      }
    }


  fun launch(): Int {
    // --working-directory
    // --plugin-directory
    // --local-config
    // --port

    thread.start()
    println("Launching maven repository mirror at http://localhost:$port/")

    Thread.sleep(5000)

    updateMirrors()

    return port
  }

  private fun updateMirrors() {

    val request = HttpRequest.newBuilder(URI("http://localhost:$port/api/settings/domain/maven"))
      .header("Authorization", "xBasic YWRtaW46c2VjcmV0")
      .PUT(
        HttpRequest.BodyPublishers.ofString(
          """
           {
            "repositories": [
              {
                "id": "releases",
                "visibility": "PUBLIC",
                "redeployment": false,
                "preserveSnapshots": false,
                "storageProvider": {
                  "type": "fs",
                  "quota": "100%",
                  "mount": "",
                  "maxResourceLockLifetimeInSeconds": 60
                },
                "storagePolicy": "PRIORITIZE_UPSTREAM_METADATA",
                "metadataMaxAge": 0,
                "proxied": [
                  {
                    "reference": "https://oss.sonatype.org/content/repositories/releases/",
                    "store": true
                  }
                ]
              }
            ]
          }
        """.trimIndent()
        )
      )
      .build()

    val client = HttpClient.newHttpClient()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    check(response.statusCode() == 200) {
      "Failed to update reposilite mirrors: ${response.body()}"
    }

    println("Updated mirrors: ${response.body()}")
  }

  override fun close() {
    thread.interrupt()
  }
}


abstract class MavenRepositoryMirrorPlugin
@Inject
internal constructor(
  private val layout: ProjectLayout,
) : Plugin<Project> {

  override fun apply(project: Project) {

    val serviceProvider: Provider<MavenRepositoryMirrorService> = project.gradle.sharedServices.registerIfAbsent(
      "mavenRepositoryMirrorService_${project.path}",
      MavenRepositoryMirrorService::class
    ) {
      parameters.reposiliteJar.from(reposiliteJarResolver(project))
      parameters.reposiliteDir.set(layout.buildDirectory.dir("reposilite"))
    }

    project.extensions.create("mavenRepositoryMirror", MavenRepositoryMirrorExtension::class, serviceProvider)
  }

  private fun reposiliteJarResolver(project: Project): NamedDomainObjectProvider<ResolvableConfiguration> {
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

}

abstract class MavenRepositoryMirrorExtension
internal constructor(
  val serviceProvider: Provider<MavenRepositoryMirrorService>,
)


//
///*
//# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ #
//#       Reposilite :: Local       #
//# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ #
//
//# Local configuration contains init params for current Reposilite instance.
//# For more options, shared between instances, login to the dashboard with management token and visit 'Configuration' tab.
//
//# Hostname
//# The hostname can be used to limit which connections are accepted.
//# Use 0.0.0.0 to accept connections from anywhere.
//# 127.0.0.1 will only allow connections from localhost.
//hostname: 0.0.0.0
//# Port to bind
//port: 8080
//# Database configuration. Supported storage providers:
//# - mysql localhost:3306 database user password
//# - sqlite reposilite.db
//# - sqlite --temporary
//# Experimental providers (not covered with tests):
//# - postgresql localhost:5432 database user password
//# - h2 reposilite
//database: sqlite reposilite.db
//
//# Support encrypted connections
//sslEnabled: true
//# SSL port to bind
//sslPort: 443
//# Key file to use.
//# You can specify absolute path to the given file or use ${WORKING_DIRECTORY} variable.
//# If you want to use .pem certificate you need to specify its path next to the key path.
//# Example .pem paths setup:
//# keyPath: ${WORKING_DIRECTORY}/cert.pem ${WORKING_DIRECTORY}/key.pem
//# Example .jks path setup:
//# keyPath: ${WORKING_DIRECTORY}/keystore.jks
//keyPath: ${WORKING_DIRECTORY}/cert.pem ${WORKING_DIRECTORY}/key.pem
//# Key password to use
//keyPassword: reposilite
//# Redirect http traffic to https
//enforceSsl: false
//
//# Max amount of threads used by core thread pool (min: 5)
//# The web thread pool handles first few steps of incoming http connections, as soon as possible all tasks are redirected to IO thread pool.
//webThreadPool: 16
//# IO thread pool handles all tasks that may benefit from non-blocking IO (min: 2)
//# Because most of tasks are redirected to IO thread pool, it might be a good idea to keep it at least equal to web thread pool.
//ioThreadPool: 8
//# Database thread pool manages open connections to database (min: 1)
//# Embedded databases such as SQLite or H2 don't support truly concurrent connections, so the value will be always 1 for them if selected.
//databaseThreadPool: 1
//# Select compression strategy used by this instance.
//# Using 'none' reduces usage of CPU & memory, but ends up with higher transfer usage.
//# GZIP is better option if you're not limiting resources that much to increase overall request times.
//# Available strategies: none, gzip
//compressionStrategy: none
//# Default idle timeout used by Jetty
//idleTimeout: 30000
//
//# Adds cache bypass headers to each request from /api/* scope served by this instance.
//# Helps to avoid various random issues caused by proxy provides (e.g. Cloudflare) and browsers.
//bypassExternalCache: true
//# Amount of messages stored in cached logger.
//cachedLogSize: 50
//# Enable default frontend with dashboard
//defaultFrontend: true
//# Set custom base path for Reposilite instance.
//# It's not recommended to mount Reposilite under custom base path
//# and you should always prioritize subdomain over this option.
//basePath: /
//# Debug mode
//debugEnabled: false
//
// */
