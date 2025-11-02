package buildsrc

import gradle.kotlin.dsl.accessors._4d97a60640e93d1989ec646a8b95a07d.publishing
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

plugins {
  id("buildsrc.kotlin-base")
  id("buildsrc.publishing")
}

publishing {
  publications {
    register<MavenPublication>("mavenJava") {
      from(components["java"])
    }
  }
}
