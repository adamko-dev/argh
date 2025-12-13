package dev.adamko.argh.lib.utils

import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.security.NoSuchAlgorithmException
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.writeText
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Unit tests for the `checksum` function in the `ChecksumKt` class.
 *
 * The `checksum` function computes the checksum of a file using the specified algorithm
 * and returns the result as a Base64 encoded string.
 */
@OptIn(InternalUtilsApi::class)
class PathComputeChecksumTest {

  @ParameterizedTest(name = "algorithm {0}")
  @CsvSource(
    "MD5,     65a8e27d8879283831b664bd8b7f0ad4",
    "SHA-256, dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f",
    "SHA-512, 374d794a95cdcfd8b35993185fef9ba368f160d8daf432d08ba9f1ed1e5abe6cc69291e0fa2fe0006a52570ef18c19def4e617c33ce52ef0a6e5fbe318cb0387",
  )
  fun `test checksum with valid algorithm and valid file`(
    algorithm: String,
    expectedChecksum: String,
    @TempDir
    tempDir: Path,
  ) {
    val content = "Hello, World!"
    val testFile = tempDir.resolve("test-file.txt").apply {
      writeText(content)
    }

    val actualChecksum = testFile.computeChecksum(algorithm)

    Assertions.assertEquals(expectedChecksum, actualChecksum)
  }

  @Test
  fun `test checksum with empty file`(
    @TempDir
    tempDir: Path,
  ) {
    val tempFile = tempDir.resolve("empty-file.txt").apply {
      createFile()
    }

    val actualChecksum = tempFile.computeChecksum("SHA-256")

    Assertions.assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", actualChecksum)
  }

  @Test
  fun `test checksum with unsupported algorithm`(
    @TempDir
    tempDir: Path,
  ) {
    val tempFile = tempDir.resolve("test-file.txt").apply {
      writeText("Some content")
    }
    val invalidAlgorithm = "INVALID-ALGO"

    assertThrows<NoSuchAlgorithmException> {
      tempFile.computeChecksum(invalidAlgorithm)
    }
  }

  @Test
  fun `test checksum with non-existent file`() {
    val nonExistentPath = Path("non-existent-file.txt")

    assertThrows<NoSuchFileException> {
      nonExistentPath.computeChecksum("SHA-256")
    }
  }

//  @Test
//  fun `test checksum with null output stream`(
//    @TempDir
//    tempDir: Path,
//  ) {
//    val tempFile = tempDir.resolve("test-file.txt")
//    val content = "Testing null output stream"
//    tempFile.writeText(content)
//
//    val expectedChecksum = MessageDigest.getInstance("MD5")
//      .digest(content.toByteArray())
//      .let { Base64.encode(it) }
//
//    val actualChecksum = tempFile.computeChecksum("MD5")
//
//    assertEquals(expectedChecksum, actualChecksum)
//  }
}
