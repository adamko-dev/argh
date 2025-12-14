package buildsrc

plugins {
  id("buildsrc.kotlin-base")
  id("buildsrc.maven-publishing")
}

java {
  withSourcesJar()
}

publishing {
  publications {
    register<MavenPublication>("mavenJava") {
      from(components["java"])
    }
  }
}
