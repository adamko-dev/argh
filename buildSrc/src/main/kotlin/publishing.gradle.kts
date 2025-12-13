package buildsrc

plugins {
  `maven-publish`
}

publishing {
  repositories {
    maven(rootProject.layout.buildDirectory.dir("project-repo")) {
      name = "ProjectRootBuild"
    }
  }
}
