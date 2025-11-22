import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

plugins {
  buildsrc.`kotlin-lib`
  application
}


val downloadGitHubApiSpec by tasks.registering {
  val version = "2022-11-28"
  val filename = "api.github.com.$version.yaml"
  val url =
    "https://raw.githubusercontent.com/github/rest-api-description/refs/heads/main/descriptions/api.github.com/$filename"
  inputs.property("url", url)
  val specFile = temporaryDir.resolve(filename)
  outputs.file(specFile)
  outputs.cacheIf { true }

  doLast {
    temporaryDir.deleteRecursively()
    temporaryDir.mkdirs()

    val request = HttpRequest.newBuilder(URI(url)).GET().build()
    HttpClient.newHttpClient().use { client ->
      val response = client.send(
        request,
        HttpResponse.BodyHandlers.ofInputStream()
      )

      response.body().use { source ->
        specFile.outputStream().use { sink ->
          source.transferTo(sink)
        }
      }
    }
  }
}


dependencies {
  implementation("io.swagger.parser.v3:swagger-parser:2.1.35")

  implementation("org.slf4j:slf4j-simple:2.0.17")

  implementation(projects.modules.libUtils)

  implementation(libs.kotlinx.serialization.json)

  implementation(libs.ktorClient.core)
  implementation(libs.ktorClient.cio)
  implementation(libs.ktorClient.logging)
  implementation(libs.ktorClient.resources)
  implementation(libs.ktorClient.contentNegotiation)

  implementation(libs.ktorSerialization.kotlinxJson)
}


application {
  mainClass.set("GeneratorKt")
}

tasks.run.configure {
  val downloadGitHubApiSpec = downloadGitHubApiSpec.map { it.outputs.files.singleFile }
  inputs.files(downloadGitHubApiSpec)
    .withPathSensitivity(PathSensitivity.NAME_ONLY)
    .normalizeLineEndings()

  val outputDir = temporaryDir
  outputs.dir(outputDir)
  outputs.cacheIf { true }

  argumentProviders.add {
    listOf(
      "specFile=${downloadGitHubApiSpec.get().absolutePath}",
      "outputDir=${outputDir.absolutePath}",
    )
  }

  systemProperty("maxYamlCodePoints", Int.MAX_VALUE)

  doFirst {
    outputDir.deleteRecursively()
    outputDir.mkdirs()
  }
}

kotlin {
  compilerOptions {
    optIn.add("dev.adamko.githubassetpublish.lib.utils.InternalUtilsApi")
  }
}


val syncGeneratedFiles by tasks.registering(Sync::class) {
  from(tasks.run)
  into(temporaryDir)
}

val generatedSrc = configurations.consumable("generatedSrc") {
  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, objects.named("ghc-src"))
  }
  outgoing.artifact(syncGeneratedFiles)
}
