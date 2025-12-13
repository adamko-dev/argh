package dev.adamko.argh.lib.utils

import java.io.OutputStream.nullOutputStream
import java.nio.file.Path
import java.security.DigestOutputStream
import java.security.MessageDigest
import kotlin.io.path.inputStream

@InternalUtilsApi
fun Path.computeChecksum(algorithm: String): String {
  val md = MessageDigest.getInstance(algorithm)
  DigestOutputStream(nullOutputStream(), md).use { sink ->
    inputStream().use { source ->
      source.copyTo(sink)
    }
  }
  return md.digest().toHexString()
}
