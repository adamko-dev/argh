package dev.adamko.argh.maven.publisher

//import org.apache.maven.AbstractMavenLifecycleParticipant
//import org.apache.maven.MavenExecutionException
//import org.apache.maven.artifact.repository.ArtifactRepositoryFactory
//import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout
//import org.apache.maven.execution.MavenSession
//import javax.inject.Inject
//import javax.inject.Named
//import javax.inject.Singleton
//
//@Named
//@Singleton
//class GhaLocalPublishParticipant @Inject constructor(
//    private val artifactRepositoryFactory: ArtifactRepositoryFactory
//) : AbstractMavenLifecycleParticipant() {
//
//    override fun afterProjectsRead(session: MavenSession) {
//        val localDir = "\${project.build.directory}/local-repo"
//
//        // Check if our specific property or goal is active to avoid hijacking every build
//        if (System.getProperty("custom.local.publish") != "true") {
//            return
//        }
//
//        session.projects.forEach { project ->
//            // Calculate the absolute path based on the project's build directory
//            val path = project.build.directory + "/local-repo"
//            val repoUrl = "file://$path"
//
//            // Create a new deployment repository pointing to the local folder
//            val localRepo = artifactRepositoryFactory.createDeploymentArtifactRepository(
//                "local-custom-repo",
//                repoUrl,
//                DefaultRepositoryLayout(),
//                true // unique version
//            )
//
//            // Override the project's distribution management
//            project.distributionManagementArtifactRepository = localRepo
//
//            // Often good to override snapshot repo as well
//            // project.setSnapshotArtifactRepository(localRepo)
//        }
//    }
//}
