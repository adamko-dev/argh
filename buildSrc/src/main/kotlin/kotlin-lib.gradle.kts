package buildsrc

plugins {
  id("buildsrc.kotlin-base")
  id("buildsrc.maven-publishing")
  id("org.jetbrains.dokka")
}

java {
  withSourcesJar()
}

val dokkaHtmlJar by tasks.registering(Jar::class) {
  from(tasks.dokkaGeneratePublicationHtml)
  archiveClassifier.set("javadoc")
}

publishing {
  publications {
    register<MavenPublication>("mavenJava") {
      from(components["java"])
    }
    withType<MavenPublication>().configureEach {
      artifact(dokkaHtmlJar)
    }
  }
}
