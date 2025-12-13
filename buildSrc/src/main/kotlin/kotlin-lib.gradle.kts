package buildsrc

plugins {
  id("buildsrc.kotlin-base")
  id("buildsrc.maven-publishing")
}

publishing {
  publications {
    register<MavenPublication>("mavenJava") {
      from(components["java"])
    }
  }
}
