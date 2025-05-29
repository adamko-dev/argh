package dev.adamko.githubassetpublish.internal

import java.io.File
import java.io.OutputStream.nullOutputStream
import java.math.BigInteger
import java.security.DigestOutputStream
import java.security.MessageDigest

internal fun File.computeChecksum(algorithm: String): String {
  val md = MessageDigest.getInstance(algorithm)
  DigestOutputStream(nullOutputStream(), md).use { os ->
    inputStream().use { it.transferTo(os) }
  }
  return BigInteger(1, md.digest()).toString(16)
    .padStart(md.digestLength * 2, '0')
}
