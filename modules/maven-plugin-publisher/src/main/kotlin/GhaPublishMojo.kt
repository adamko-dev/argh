import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
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

  /**
   * The output path for the generated zip file.
   * Defaults to the target directory.
   */
  @Parameter(
    defaultValue = $$"${project.build.directory}/${project.artifactId}-${project.version}-bundle.zip",
    required = true
  )
  lateinit var outputZipFile: File

  override fun execute() {
    log.info("Starting creation of artifact zip bundle...")

    // Map of Source File -> Zip Entry Path
    val artifactsToZip = mutableMapOf<File, String>()

    // 1. Add the Project POM (renamed to artifactId-version.pom)
    if (project.file != null && project.file.exists()) {
      val pomPath = buildRepoPath(isPom = true)
      artifactsToZip[project.file] = pomPath
    } else {
      log.warn("Project POM file is missing.")
    }

    // 2. Add the Main Artifact
    if (project.artifact.file != null && project.artifact.file.exists()) {
      // If packaging is 'pom', the artifact file IS the pom, which we handled above (usually)
      // But if it's a jar/war, add it.
      if (project.artifact.file != project.file) {
        artifactsToZip[project.artifact.file] = buildRepoPath(file = project.artifact.file)
      }
    } else if ("pom" != project.packaging) {
      log.warn("Main artifact file is missing or null.")
    }

    // 3. Add Attached Artifacts
    if (project.attachedArtifacts != null) {
      for (artifact in project.attachedArtifacts) {
        if (artifact.file != null && artifact.file.exists()) {
          artifactsToZip[artifact.file] = buildRepoPath(file = artifact.file)
        }
      }
    }

    if (artifactsToZip.isEmpty()) {
      log.warn("No artifacts found to zip.")
      return
    }

    log.info("Found ${artifactsToZip.size} files to zip.")
    createZip(artifactsToZip)
  }

  private fun buildRepoPath(file: File? = null, isPom: Boolean = false): String {
    val groupIdPath = project.groupId.replace('.', '/')
    val artifactId = project.artifactId
    val version = project.version

    val fileName = if (isPom) {
      "$artifactId-$version.pom"
    } else {
      file?.name ?: throw IllegalArgumentException("File cannot be null if not POM")
    }

    // Standard Maven Layout: group/id/artifact/id/version/filename
    return "$groupIdPath/$artifactId/$version/$fileName"
  }

  private fun createZip(filesMap: Map<File, String>) {
    try {
      if (outputZipFile.parentFile != null && !outputZipFile.parentFile.exists()) {
        outputZipFile.parentFile.mkdirs()
      }

      outputZipFile.outputStream().use { fos ->
        ZipOutputStream(fos).use { zos ->
          for ((file, zipPath) in filesMap) {
            val entry = ZipEntry(zipPath)
            zos.putNextEntry(entry)

            FileInputStream(file).use { fis ->
              fis.copyTo(zos)
            }
            zos.closeEntry()
          }
        }
      }
      log.info("Successfully created zip bundle at: ${outputZipFile.absolutePath}")
    } catch (e: Exception) {
      throw MojoExecutionException("Error creating zip file", e)
    }
  }
}
