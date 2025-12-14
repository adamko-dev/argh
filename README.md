# Argh (Asset Release for GitHub)

Publish Gradle or Maven libraries to GitHub Release Assets.

- Quick and easy publishing and resolving.
- Consumers can access the publications without authentication.
- Control your own requirements: POM details, GPG signatures, checksums, Javadoc are all optional.
- Kotlin Multiplatform friendly (can handle all Kotlin targets).

Argh is perfect for prototyping, or sharing internal libraries.

### Status

Argh is an experimental prototype. Please try it out, feedback is welcome!

### Guide for Publishers

##### Requirements

- A GitHub OAuth token with `repo` scope.
  (Run the run publishing follow the instructions to allow access.)
- The `group` of all subprojects must match the GitHub repository.

#### Gradle

1. Add the Argh Gradle Publisher plugin to your `build.gradle(.kts)`.
2. Set the `gitHubRepo` property to the GitHub repository you want to publish to.
3. Run `./gradlew uploadGitHubReleaseAssets`.
4. Argh will create a draft GitHub Release and attach the assets.
5. Navigate to the Release page and publish the release.

```kotlin
// build.gradle.kts

plugins {
  id("dev.adamko.argh.publisher") version "0.0.1"
}

group = "my-org.my-repo" // must match arghPublisher.gitHubRepo

arghPublisher {
  gitHubRepo = "my-org/my-repo"
}
```

#### Maven

The Argh Maven Publisher is not yet implemented.
Contributions are welcome!

### Guide for Consumers

Once a library has been published using one of the Argh publisher plugins,
it can be resolved using the Argh resolver plugin.

Resolving Argh publications does not require any authentication.
There are no limitations.
However, due to the potential of GitHub Release Assets being mutable,
it is recommended to enable dependency verification.

- https://docs.gradle.org/current/userguide/dependency_verification.html
- https://maven.apache.org/resolver/expected-checksums.html

#### Gradle

In a `settings.gradle(.kts)`

```kotlin
// settings.gradle.kts
plugins {
  id("dev.adamko.argh.repository") version "0.0.1"
}
```

OR in a `build.gradle(.kts)`

```kotlin
// build.gradle.kts
plugins {
  id("dev.adamko.argh.repository") version "0.0.1"
}
```

#### Maven

To resolve Argh publications from Maven the Argh Maven Repository plugin must be added to the `extensions.xml`.
Once added,

###### `.mvn/extensions.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<extensions>
  <extension>
    <groupId>dev.adamko.argh</groupId>
    <artifactId>maven-plugin-repository</artifactId>
    <version>0.0.1</version>
  </extension>
</extensions>
```

###### `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.example</groupId>
  <artifactId>github-assets-resolution.example</artifactId>
  <version>1.2.3</version>

  <repositories>
    <repository>
      <!-- Define the GitHub repository, using the `argh` layout provided by the Argh maven-plugin-repository extension. -->
      <id>github-assets</id>
      <url>https://github.com/</url>
      <layout>argh</layout>
    </repository>
  </repositories>

  <dependencies>
    <dependency>
      <groupId>asemy.demo-github-asset-publish-repo</groupId>
      <artifactId>test-project-jvm</artifactId>
      <version>1.0.0</version>
    </dependency>
  </dependencies>
</project>
```
