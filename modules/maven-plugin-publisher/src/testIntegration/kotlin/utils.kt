package dev.adamko.argh.maven.publisher

import java.nio.file.Path
import kotlin.io.path.Path


internal val mvnBinary: Path by lazy {
  Path(System.getProperty("mvnBinary"))
}

internal val devMavenRepo: Path by lazy {
  Path(System.getProperty("devMavenRepo"))
}

internal val testMavenLocalDir: Path by lazy {
  Path(System.getProperty("testMavenLocalDir"))
}

internal val localMavenMirrorPort: Int by lazy {
  System.getProperty("localMavenMirrorPort").toInt()
}
