package buildsrc

plugins {
  `maven-publish`
}

publishing {
  repositories {
//    maven(file("${rootProject.layout.buildDirectory.get()}/repo")) {
//      name = "BuildDir"
//    }
    maven(rootProject.layout.buildDirectory.dir("project-repo")) {
      name = "ProjectRootBuild"
    }
  }
}
