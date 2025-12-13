# Argh (Asset Release for GitHub)

Publish Gradle/Maven libraries to GitHub Release Assets.

- Quick and easy publishing and resolving.
- Kotlin Multiplatform friendly (can publish all Kotlin targets).

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
  id("dev.adamko.argh.resolver") version "0.0.1"
}
```

OR in a `build.gradle(.kts)`

```kotlin
// build.gradle.kts
plugins {
  id("dev.adamko.argh.resolver") version "0.0.1"
}
```

#### Maven

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
      <id>github-assets</id>
      <url>https://github.com/</url>
      <layout>argh</layout>
      <releases>
        <enabled>true</enabled>
      </releases>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
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

### Guide for Publishers

#### Gradle

1. Add the Argh Gradle Publisher plugin to your `build.gradle(.kts)`.
2. Set the `gitHubRepo` property to the GitHub repository you want to publish to.

```kotlin
// build.gradle.kts

plugins {
  id("dev.adamko.argh.publisher") version "0.0.1"
}

arghPublisher {
  gitHubRepo = "my-org/my-repo"
}
```

#### Maven

The Argh Maven Publisher is not yet implemented.
Contributions are welcome!
