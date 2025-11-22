import dev.adamko.githubassetpublish.lib.utils.StringBlockBuilder
import dev.adamko.githubassetpublish.lib.utils.buildStringBlock
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.*
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.SwaggerParseResult
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*


fun main(args: Array<String>) {

  fun getArg(name: String): String {
    return args.firstOrNull { it.startsWith("$name=") }
      ?.substringAfter("$name=")
      ?: error("missing required argument '$name'")
  }

  val specFile = Path(getArg("specFile"))
  val outputDir = Path(getArg("outputDir"))

  val openApi = OpenAPIV3Parser().readContents(
    specFile.readText(),
//    emptyList(),
//    ParseOptions().apply {
//      this
//    }
  )

  if ("mode=v1" in args) {
    v1(openApi, outputDir.resolve("OpenApiClient.kt"))
  } else {
    v2(openApi, outputDir)
  }

//  val releases = openApi.openAPI.paths.getValue("/repos/{owner}/{repo}/releases")
//  println(releases)


//  val resources = mutableMapOf<ResourceSpec, MutableSet<ResourceSpec>>()


}

private fun v2(
  openApi: SwaggerParseResult,
  outputFile: Path
) {
  with(GenScope(outputFile, openApi)) {

    buildGhClient()

    buildPaths()

    openApi.openAPI.paths.forEach { (id, data) ->
      data.readOperationsMap().forEach { (method, operation) ->
        when (method) {
          PathItem.HttpMethod.POST    -> buildPostFn(operation)
          PathItem.HttpMethod.GET     -> buildGetFn(operation)
          PathItem.HttpMethod.PUT     -> buildPutFn(operation)
          PathItem.HttpMethod.PATCH   -> buildPatchFn(operation)
          PathItem.HttpMethod.DELETE  -> buildDeleteFn(operation)
          PathItem.HttpMethod.HEAD    -> buildHeadFn(operation)
          PathItem.HttpMethod.OPTIONS -> buildOptionsFn(operation)
          PathItem.HttpMethod.TRACE   -> buildTraceFn(operation)
        }
      }
    }
  }
}


private class GenScope(
  val outputDir: Path,
  val apiSpec: SwaggerParseResult,
) {
//  val clientFile: Path = outputDir.resolve("GitHubClient.kt")
//  val getRequests: Path = outputDir.resolve("get.kt")

  private fun getFile(name: String): Path {
    val outputFile = outputDir.resolve(name)
    require(outputFile.absolute().normalize().parent == outputDir.absolute().normalize()) {
      "file $outputFile must be a child of the output directory: $outputFile"
    }
    outputFile.apply {
      if (!exists()) {
        writeText(
          buildStringBlock {
            line("package dev.adamko.github")
            line("")
            line("import io.ktor.client.*")
            line("import io.ktor.client.engine.cio.*")
            line("import io.ktor.client.request.*")
            line("import io.ktor.client.statement.*")
            line("import io.ktor.http.*")
            line("import io.ktor.client.plugins.logging.*")
            line("import io.ktor.client.plugins.resources.*")
            line("import kotlin.time.Instant")
            line("")
          }
        )
      }
    }
    return outputFile
  }

  fun build(
    file: String,
    content: context(GenScope) StringBlockBuilder.() -> Unit,
  ) {
    val outputFile = getFile(file)

    outputFile.bufferedWriter(options = arrayOf(StandardOpenOption.APPEND)).use { writer ->
      writer.appendLine(
        buildStringBlock {
          line()
          content()
        }
      )
    }
  }
}


context(g: GenScope)
private fun buildPaths() {
//  val pathsToChildren = pathsToChildren(g.apiSpec)


//  val content = mutableMapOf<String, MutableList<String>>()
  val containerToFns = mutableMapOf<String, MutableList<String>>()
  val containerToSubContainers = mutableMapOf<String, MutableSet<String>>()

  g.apiSpec.openAPI.paths.forEach { (_, data) ->
    data.readOperationsMap().forEach { (_, operation) ->

      val opContainer = operation.operationId.substringBeforeLast("/")

      val opContainerParent = opContainer.substringBeforeLast("/", "")
      if (opContainerParent.isNotEmpty()) {
        containerToSubContainers.getOrPut(opContainerParent) { mutableSetOf() }.add(opContainer)
      } else {
        containerToSubContainers.getOrPut("GitHubClient") { mutableSetOf() }.add(opContainer)
      }

      val opName = operation.operationId.substringAfterLast("/").toCamelCase().lowercaseFirstChar()

      containerToFns.getOrPut(opContainer) { mutableListOf() }.add(
        buildFn(opName, operation)
      )
    }
  }

  fun StringBlockBuilder.addContainer(container: String) {
    block("sealed interface ${container.toCamelCase()} {", "}") {
      val subContainers = containerToSubContainers[container]?.sorted()

      subContainers?.forEach { subContainer ->
        line("")
        line("val ${subContainer.toCamelCase().lowercaseFirstChar()}: ${subContainer.toCamelCase()}")
      }

      subContainers?.forEach {
        line("")
        addContainer(it)
      }
      containerToFns[container]
        ?.sorted()
        ?.forEach { fn ->
          line("")
          line(fn)
        }
      line()
    }
  }

  g.build("Paths.kt") {
    containerToSubContainers.forEach { (container, subContainers) ->
      addContainer(container)
    }
  }
}

context(g: GenScope)
private fun buildGhClient() {
  g.build("GitHubClient.new.kt") {
    block("fun GitHubClient(", "): GitHubClient {") {
      line("httpClient: HttpClient = defaultHttpClient")
    }
    block("", "}") {
      line("TODO()")
      line("//return GitHubClient(client)")
    }
    line()
    block("private val defaultHttpClient: HttpClient by lazy {", "}") {
      block("HttpClient(CIO) {", "}") {
        line("expectSuccess = false")
      }
    }
  }
}


context(g: GenScope)
private fun buildPostFn(operation: Operation) {
  g.build("Post.kt") {
    line(buildKdoc(operation))
    block("suspend fun HttpClient.${operation.operationId.toCamelCase().lowercaseFirstChar()}() {", "}") {
      block("post(\"${operation.operationId}\") {", "}") {}
    }
  }
}

context(g: GenScope)
private fun buildGetFn(operation: Operation) {
  g.build("Get.kt") {
    line(buildKdoc(operation))
    block("suspend fun HttpClient.${operation.operationId.toCamelCase().lowercaseFirstChar()}() {", "}") {
      block("get(\"${operation.operationId}\") {", "}") {}
    }
  }
}

context(g: GenScope)
private fun buildPutFn(operation: Operation) {
  g.build("Put.kt") {
    line(buildKdoc(operation))
    block("suspend fun HttpClient.${operation.operationId.toCamelCase().lowercaseFirstChar()}() {", "}") {
      block("put(\"${operation.operationId}\") {", "}") {}
    }
  }
}

context(g: GenScope)
private fun buildPatchFn(operation: Operation) {
  g.build("Patch.kt") {}
}

context(g: GenScope)
private fun buildDeleteFn(operation: Operation) {
  g.build("Delete.kt") {}
}

context(g: GenScope)
private fun buildHeadFn(operation: Operation) {
  g.build("Head.kt") {}
}

context(g: GenScope)
private fun buildOptionsFn(operation: Operation) {
  g.build("Options.kt") {}
}

context(g: GenScope)
private fun buildTraceFn(operation: Operation) {
  g.build("Trace.kt") {}
}

private fun buildKdoc(operation: Operation): String {
  return buildString {
    if (operation.summary != null) {
      appendLine(operation.summary.trim())
      appendLine()
    }
    if (operation.description != null) {
      appendLine(
        operation.description
          .replace(". ", ".\n")
          .replace("see \"[", "see \n\"[")
      )
      appendLine()
    }
    appendLine("`${operation.operationId}`")
  }.trimEnd()
    .lines()
    .joinToString(prefix = "/**\n", separator = "\n", postfix = "\n */") { " * $it".trimEnd() }
}


private fun pathsToChildren(
  openApi: SwaggerParseResult,
): Map<String, Set<String>> {
  val pathsToChildren = mutableMapOf<String, MutableSet<String>>()

  openApi.openAPI.paths.forEach block@{ (id, data) ->
    val segments = id.split("/")

    val paths = segments.runningReduce { acc, segment -> "$acc/$segment" }

    paths.windowed(2).forEach { (parent, child) ->
      val parentPaths = pathsToChildren.getOrPut(parent) { mutableSetOf() }
      parentPaths.add(child)
    }
  }

  return pathsToChildren
}


private fun v1(
  openApi: SwaggerParseResult,
  outputFile: Path,
) {
  val pathsToChildren = pathsToChildren(openApi)
  val pathsToResource = mutableMapOf<String, MutableList<String>>()

  openApi.openAPI.paths.forEach block@{ (id, data) ->
    val segments = id.removePrefix("/").split("/")

//    val paths = segments.runningReduce { acc, segment -> "$acc/$segment" }

//    paths.windowed(2).forEach { (parent, child) ->
//      val parentPaths = pathsToChildren.getOrPut(parent) { mutableSetOf() }
//      parentPaths.add(child)
//    }


    val className = segments.last().toCamelCase()
      .ifBlank { "GitHub" }

    pathsToResource.getOrPut(id, ::mutableListOf).add(
      buildStringBlock(defaultIndent = "  ") {
        block("class $className {", "}") {

          data.readOperationsMap().forEach { (method, operation) ->
            line("""/**""")
            if (operation.summary != null) {
              line(""" * ${operation.summary}""")
              line(""" *""")
            }
            if (operation.description != null) {
              operation.description.lines().forEach { description ->
                line(""" * $description""".trimEnd())
              }
              line(""" *""")
            }
            line(""" */""")
            line("""@Resource("${segments.last()}")""")
            if (segments.last().run { startsWith("{") && endsWith("}") }) {
              block("data class ${method.name.lowercase().uppercaseFirstChar()}(", ")") {
                line("val ${segments.last().removePrefix("{").removeSuffix("}")}: String,")
              }
            } else {
              line("class ${method.name.lowercase().uppercaseFirstChar()}")
            }
          }
        }
      }
    )
  }

  println("pathsToChildren:")
  println(pathsToChildren)
  println(pathsToChildren.entries.joinToString("\n") { "${it.key} -> ${it.value}" })

  println("pathsToResource:")
  println(pathsToResource.entries.joinToString("\n\n") { "${it.key} ${it.value}" })


  outputFile.bufferedWriter().use { writer ->

    writer.appendLine(
      """
      import io.ktor.client.*
      import io.ktor.client.request.*
      import io.ktor.client.statement.*
      import io.ktor.http.*
      """.trimIndent()
    )

    pathsToChildren.forEach { (path, children) ->
      writer.appendLine(
        buildStringBlock {
          line("""/**""")
          line(""" * `$path`""")
          line(""" */""")
          block("data class ${path.toCamelCase()}() {", "}") {

          }
        }
      )
    }

    pathsToResource.forEach { (path, resource) ->
      resource.forEach { r ->
        writer.appendLine(r)
      }
    }
  }
}


private fun String.toCamelCase(): String {
  return split { !it.isLetterOrDigit() }
    .joinToString("") { it.uppercaseFirstChar() }
}

private fun String.uppercaseFirstChar(): String =
  replaceFirstChar { it.titlecaseChar() }

private fun String.lowercaseFirstChar(): String =
  replaceFirstChar { it.lowercaseChar() }

private fun String.split(predicate: (c: Char) -> Boolean): List<String> {
  return fold(mutableListOf(StringBuilder())) { acc, c ->
    if (predicate(c)) {
      acc.add(StringBuilder())
    } else {
      acc.last().append(c)
    }
    acc
  }.map { it.toString() }
}

context(g: GenScope)
private val Schema<*>.kotlinType: String
  get() {
    return when (this) {
      is ArbitrarySchema -> "Arbitrary"
      is ArraySchema     -> "List<${items.kotlinType}>"
      is BinarySchema    -> "Binary"
      is BooleanSchema   -> "Boolean"
      is ByteArraySchema -> "ByteArray"
      is ComposedSchema  -> {
        anyOf?.firstNotNullOfOrNull { it.kotlinType }
          ?: oneOf?.firstNotNullOfOrNull { it.kotlinType }
          ?: "Composed"
      }

      is DateSchema      -> "Date"
      is DateTimeSchema  -> "Instant"
      is EmailSchema     -> "Email"
      is FileSchema      -> "File"
      is IntegerSchema   -> "Int"
      is JsonSchema      -> "Json"
      is MapSchema       -> "Map"
      is NumberSchema    -> "Number"
      is ObjectSchema    -> "Object"
      is PasswordSchema  -> "Password"
      is StringSchema    -> "String"
      is UUIDSchema      -> "Uuid"
      else               -> {
        val ref = resolveRefDeep()
        if (this == ref) {
          println("unknown type: $this")
          "Nothing??"
        } else {
          ref.kotlinType
        }
      }
    }
  }

context(g: GenScope)
private fun Schema<*>.resolveRefDeep(
  visited: MutableSet<String> = mutableSetOf()
): Schema<*> {
  val ref = this.`$ref` ?: return this
  val refName = ref.substringAfterLast('/')

  if (!visited.add(refName)) return this // avoid cycles

  val resolved = g.apiSpec.openAPI.components?.schemas?.get(refName) ?: return this
  return if (resolved.`$ref` != null) {
    resolved.resolveRefDeep(visited)
  } else {
    resolved
  }
}

context(g: GenScope)
private fun buildFn(
  opName: String,
  operation: Operation,
): String {

  val parameters =
    operation.parameters.orEmpty().map { parameter ->
      val parameterName = run {
        val base = parameter.name.toCamelCase().lowercaseFirstChar()
        when (base) {
          "in"      -> "input"
          "package" -> {
            if ("comma-separated list of package names" in parameter.description.orEmpty())
              "packages"
            else "`package`"
          }

          else      -> base
        }
      }
      "${parameterName}: ${parameter.schema.kotlinType},"
    }

  return buildStringBlock {
    line(buildKdoc(operation))
    block("suspend fun ${opName}(", ")") {
      parameters.forEach { line(it) }
    }
  }
}
