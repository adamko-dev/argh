package dev.adamko.githubassetpublish.maven

import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.*
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir

class MavenRepoPluginTest {

  @Test
  @Disabled
  @Timeout(value = 1, unit = TimeUnit.MINUTES)
  fun `testJavaLibraryProject - validate`(
    @TempDir
    projectDir: Path
  ) {
    createProject(projectDir)
    val logFile = projectDir.resolve("maven_build.log")

    val result = ProcessBuilder(
      buildList {
        add(mvnBinary.invariantSeparatorsPathString)
//        add("-Dmaven.repo.local=${testMavenLocalDir.invariantSeparatorsPathString}")
        add("--batch-mode")
        add("--errors")
        add("-X")
        add("--settings")
        add("settings.xml")
        add("validate")
      }
    )
      .redirectOutput(logFile.toFile())
      .redirectError(logFile.toFile())
      .directory(projectDir.toFile())
      .start()

    result.waitFor(1, TimeUnit.MINUTES)
    println(logFile.readText())

    val stdout = result.inputStream.bufferedReader().use { it.readText() }
    println(stdout)

    assertEquals(0, result.exitValue())
  }

  @Test
  @Timeout(value = 1, unit = TimeUnit.MINUTES)
  fun `testJavaLibraryProject - compile`(
    @TempDir
    projectDir: Path
  ) {
    createProject(projectDir)
    val logFile = projectDir.resolve("maven_build.log")

    val result = ProcessBuilder(
      buildList {
        add(mvnBinary.invariantSeparatorsPathString)
//        add("-Dmaven.repo.local=${testMavenLocalDir.invariantSeparatorsPathString}")
        add("--batch-mode")
        add("--errors")
        add("-X")
        add("--settings")
        add("settings.xml")
        add("compile")
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

//    val mvnLocalDir = prepareTestMavenLocal(projectDir.resolve("mvn-local"))

  projectDir.resolve("pom.xml")
    .writeText(pomXml())

  projectDir.resolve("settings.xml").writeText(settingsXml())

  projectDir.resolve(".mvn/extensions.xml").apply {
    parent.createDirectories()
    writeText(
      """
      <extensions>
        <extension>
          <groupId>adamko-dev.github-asset-publish</groupId>
          <artifactId>maven-plugin-repository</artifactId>
          <version>0.0.1</version>
        </extension>
      </extensions>
      """.trimIndent()
    )
  }
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
        import com.example.*;

        public class Main {
          public static void main(String[] args) {
            String demoValue = DemoKt.getDemo();
            System.out.println(demoValue);
          }
        }
      """.trimIndent()
    )
  }
}


//private fun prepareTestMavenLocal(
//  destinationDir: Path,
//): Path {
//  destinationDir.deleteRecursively()
//  destinationDir.createDirectories()
//  devMavenRepo.copyToRecursively(
//    destinationDir,
//    overwrite = false,
//    followLinks = false,
//  )
//  return destinationDir
//}

@Language("XML")
private fun pomXml(): String {
  return """
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>github-assets-resolution.example</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>

    <repositories>
        <repository>
            <id>github-assets</id>
            <url>https://github.com/</url>
            <layout>githubAssets</layout>
            <releases><enabled>true</enabled></releases>
            <snapshots><enabled>true</enabled></snapshots>
        </repository>
    </repositories>
    
    <dependencies>
      <dependency>
        <groupId>asemy.demo-github-asset-publish-repo</groupId>
        <artifactId>test-project</artifactId>
        <version>1.0.0</version>
      </dependency>
    </dependencies>

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
