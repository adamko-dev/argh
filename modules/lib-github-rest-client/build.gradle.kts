plugins {
  buildsrc.`kotlin-lib`
  kotlin("plugin.serialization")
}

dependencies {
//  implementation(projects.modules.libGithubRestClientGenerator)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.ktorClient.core)
  implementation(libs.ktorClient.cio)
  implementation(libs.ktorClient.logging)
  implementation(libs.ktorClient.resources)
  implementation(libs.ktorClient.contentNegotiation)

  implementation(libs.ktorSerialization.kotlinxJson)
}



kotlin {
  compilerOptions {
    optIn.add("dev.adamko.githubassetpublish.lib.utils.InternalUtilsApi")
  }
}


val generatedSrc = configurations.dependencyScope("generatedSrc") {
  defaultDependencies {
    add(project.dependencies.create(projects.modules.libGithubRestClientGenerator))
  }
}

val generatedSrcResolver = configurations.resolvable("${generatedSrc.name}Resolver") {
  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, objects.named("ghc-src"))
  }
  extendsFrom(generatedSrc.get())
}

val syncGeneratedFiles by tasks.registering(Sync::class) {
  from(generatedSrcResolver)
  into(temporaryDir)
}

sourceSets.main {
  kotlin.srcDir(syncGeneratedFiles)
}
