package dev.adamko.githubassetpublish.mavenPublish

import java.io.File
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

/**
 * A Maven Mojo that collects all artifacts attached to the project (Main JAR, POM, Sources, Javadoc, etc.)
 * and packages them into a single ZIP file using the standard Maven Repository layout.
 */
@Mojo(
  name = "gha-publish",
  defaultPhase = LifecyclePhase.DEPLOY,
)
class GhaPublishMojo : AbstractMojo() {

  @Parameter(defaultValue = $$"${project}", required = true, readonly = true)
  lateinit var project: MavenProject

  @Parameter(property = "localPublishDir", defaultValue = $$"${project.build.directory}/local-repo")
  lateinit var localPublishDir: File


  override fun execute() {
    // At this point, the 'deploy' phase is running.
    // If this Mojo runs AFTER the maven-deploy-plugin, the files will be in 'localPublishDir'.

    log.info("Post-processing files in: ${localPublishDir.absolutePath}")

    if (localPublishDir.exists()) {
      localPublishDir.walk().forEach { file ->
        if (file.isFile && file.extension == "jar") {
          log.info("Processing jar: ${file.name}")
          // Logic to modify/analyze the published files
        }
      }
    } else {
      log.warn("Publish directory does not exist.")
    }
  }
}
