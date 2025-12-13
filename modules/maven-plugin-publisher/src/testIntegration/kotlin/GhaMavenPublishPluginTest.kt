package dev.adamko.argh.maven.publisher

import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.*
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class GhaMavenPublishPluginTest {

  @Test
  fun `test publish`(
    @TempDir
    projectDir: Path
  ) {
    createProject(projectDir)
    val logFile = projectDir.resolve("maven_build.log")

    val result = ProcessBuilder(
      buildList {
        add(mvnBinary.invariantSeparatorsPathString)
        add("--batch-mode")
        add("--errors")
        add("-X")
//        add("--settings")
//        add("settings.xml")
        add("package")
      }
    )
      .redirectOutput(logFile.toFile())
      .redirectError(logFile.toFile())
      .directory(projectDir.toFile())
      .apply {
        environment()["JAVA_HOME"] = System.getProperty("java.home")
      }
      .start()

    result.waitFor(1, TimeUnit.MINUTES)
    println(logFile.readText())

    val stdout = result.inputStream.bufferedReader().use { it.readText() }
    println(stdout)

    assertEquals(0, result.exitValue())
  }
}

private fun createProject(projectDir: Path) {

  projectDir.resolve("pom.xml")
    .writeText(pomXml())

  projectDir.resolve("settings.xml").writeText(settingsXml())

//  projectDir.resolve(".mvn/extensions.xml").apply {
//    parent.createDirectories()
//    writeText(
//      """
//      <extensions>
//        <extension>
//          <groupId>adamko-dev.argh</groupId>
//          <artifactId>maven-plugin-repository</artifactId>
//          <version>0.0.1</version>
//        </extension>
//      </extensions>
//      """.trimIndent()
//    )
//  }
  projectDir.resolve(".mvn/maven.config").apply {
    parent.createDirectories()
    writeText(
      """
      -Dmaven.repo.local=.mvn/repository
      -s
      settings.xml
      """.trimIndent()
    )
  }

  projectDir.resolve("src/main/java/com/example/Main.java").apply {
    parent.createDirectories()
    writeText(
      """
      public class Main {
        public static void main(String[] args) {
          System.out.println("demo");
        }
      }
      """.trimIndent()
    )
  }
}


@Language("XML")
private fun pomXml(): String {
  return """
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>github-assets-publish</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>
    <build>
      <plugins>
        <plugin>
          <groupId>adamko-dev.argh</groupId>
          <artifactId>maven-plugin-publisher</artifactId>
          <version>0.0.1</version>
        </plugin>
      </plugins>
    </build>
</project>
""".trimIndent()
}

@Language("XML")
private fun settingsXml(): String =
  """
<settings>
  <profiles>
    <profile>
      <id>dev-maven</id>
      <repositories>
        <repository>
          <id>dev-maven-repo</id>
          <url>${devMavenRepo.absolute().toUri()}</url>
          <releases>
            <updatePolicy>always</updatePolicy>
          </releases>
          <snapshots>
            <updatePolicy>always</updatePolicy>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>dev-maven-repo</id>
          <url>${devMavenRepo.absolute().toUri()}</url>
          <releases>
            <updatePolicy>always</updatePolicy>
          </releases>
          <snapshots>
            <updatePolicy>always</updatePolicy>
          </snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>dev-maven</activeProfile>
  </activeProfiles>
  <mirrors>
    <mirror>
      <id>local-mirror</id>
      <url>http://localhost:$localMavenMirrorPort/releases/</url>
      <mirrorOf>external:*</mirrorOf>
    </mirror>
  </mirrors>
</settings>
"""
