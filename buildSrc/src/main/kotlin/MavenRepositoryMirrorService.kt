package buildsrc

import java.io.File
import java.io.OutputStream
import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlin.io.encoding.Base64
import kotlin.io.path.createDirectories
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import org.gradle.api.NamedDomainObjectProvider
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
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
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

  private val credentials = "admin:secret"
  private val credentialsEncoded = Base64.encode(credentials.encodeToByteArray())

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
      try {
        execOps.javaexec {
          classpath = parameters.reposiliteJar
          workingDir(reposiliteDir.toFile())
          args = listOf(
            "--port=$port",
            "--hostname=127.0.0.1",
            "--no-color",
            "--token", credentials,
          )
          standardOutput = OutputStream.nullOutputStream()
          errorOutput = OutputStream.nullOutputStream()
          maxHeapSize = "32m"
          isIgnoreExitValue = true
        }
      } catch (_: Exception) {
        // ignore
      }
    }

  @Synchronized
  fun launch(): Int {
    // --working-directory
    // --plugin-directory
    // --local-config
    // --port

    if (thread.state != Thread.State.NEW) {
      // already started
      return port
    }

    try {
      thread.start()
      logger.info("Launching maven repository mirror at http://localhost:$port/")

      waitUntilServerUp()

      updateMirrors()
    } catch (_: IllegalThreadStateException) {
      // already started
    }

    return port
  }

  private fun waitUntilServerUp() {
    val timeMark = TimeSource.Monotonic.markNow()
    val client = HttpClient.newHttpClient()
    val request = HttpRequest
      .newBuilder(URI("http://localhost:$port/api/status/instance"))
      .header("Authorization", "xBasic $credentialsEncoded")
      .GET()
      .build()
    while (true) {
      if (timeMark.elapsedNow() > 10.0.seconds) {
        error("Reposilite server didn't start in 10 seconds")
      }
      val response = try {
        client.send(
          request,
          HttpResponse.BodyHandlers.ofString(),
        )
      } catch (_: Exception) {
        null
      }
      if (response?.statusCode() == 200) {
        return
      } else {
        logger.info("Waiting for reposilite server to start ... ${response?.run { "${statusCode()} - ${body()}" }}")
      }
      Thread.sleep(1000)
    }
  }

  private fun updateMirrors() {

    val request = HttpRequest.newBuilder(URI("http://localhost:$port/api/settings/domain/maven"))
      .header("Authorization", "xBasic $credentialsEncoded")
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
                  "maxResourceLockLifetimeInSeconds": 60,
                  "allowedExtensions": [
                    ".klib",
                    ".jar",
                    ".war",
                    ".pom",
                    ".xml",
                    ".module",
                    ".md5",
                    ".sha1",
                    ".sha256",
                    ".sha512",
                    ".asc"
                  ]
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

    logger.info("Updated mirrors: ${response.body()}")
  }

  override fun close() {
    thread.interrupt()
  }

  companion object {
    private val logger: Logger = Logging.getLogger(MavenRepositoryMirrorService::class.java)

    internal fun register(project: Project): Provider<MavenRepositoryMirrorService> {
      val projectPathAsFileName: String =
        project.path.map { if (it.isLetterOrDigit()) it else "_" }.joinToString("")

      val reposiliteDir: Provider<Directory> =
        project.objects.directoryProperty()
          .fileProvider(
            project.providers.environmentVariable("REPOSILITE_DIR")
              .map { File(it).resolve(projectPathAsFileName) }
          )
          .orElse(project.layout.buildDirectory.dir("reposilite"))

      val reposiliteJarResolver = reposiliteJarResolver(project)

      return project.gradle.sharedServices.registerIfAbsent(
        "mavenRepositoryMirrorService_${project.path}",
        MavenRepositoryMirrorService::class
      ) {
        val reposiliteDir = reposiliteDir
        parameters.reposiliteDir.set(reposiliteDir)
        parameters.reposiliteJar.from(reposiliteJarResolver)
      }
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
}
