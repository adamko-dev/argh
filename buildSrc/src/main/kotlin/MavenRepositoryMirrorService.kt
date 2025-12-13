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
import kotlin.io.encoding.Base64
import kotlin.io.path.createDirectories
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
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


  fun launch(): Int {
    // --working-directory
    // --plugin-directory
    // --local-config
    // --port

    thread.start()
    logger.info("Launching maven repository mirror at http://localhost:$port/")

    waitUntilServerUp()

    updateMirrors()

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
  }
}
