plugins {
  id("org.gradlex.maven-plugin-development")
  buildsrc.`kotlin-lib`
}

// https://github.com/OpenNTF/p2-layout-provider

dependencies {
  implementation("org.apache.maven:maven-plugin-api:3.9.11")
  implementation("org.apache.maven:maven-core:3.9.11")
  implementation("org.codehaus.plexus:plexus-utils:4.0.2")
  implementation("org.codehaus.plexus:plexus-container-default:2.1.1")
//  testImplementation("junit:junit:4.13.2")
//  testImplementation("org.assertj:assertj-core:3.24.2")

  implementation("org.apache.maven.resolver:maven-resolver-spi:1.9.18")
//  compileOnly("javax.inject:javax.inject:1")
//  compileOnly("org.eclipse.sisu:org.eclipse.sisu.inject:0.3.5")
}
