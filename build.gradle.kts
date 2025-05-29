plugins {
  base
  idea
  `kotlin-dsl` apply false
}

group = "adamko-dev.github-asset-publish"
version = "0.0.1"

tasks.updateDaemonJvm {
  @Suppress("UnstableApiUsage")
  languageVersion = JavaLanguageVersion.of(21)
}

idea {
  module {
    excludeDirs.addAll(
      files(
        ".idea/",
        ".kotlin/",
        "buildSrc/.kotlin/",
        "gradle/wrapper/",
      )
    )
  }
}
