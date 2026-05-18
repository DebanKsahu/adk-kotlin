/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.adk.kt.skills

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.streams.asSequence
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.YAMLException

/** JVM implementation of [SkillSource] using standard File I/O. */
class NewFileSystemSource(private val skillsBaseDir: String) : SkillSource {

  override suspend fun listFrontmatters(): Result<List<Frontmatter>> = sourceRunCatching {
    val baseDirPath = Path.of(skillsBaseDir)
    val baseDirStream =
      try {
        Files.list(baseDirPath)
      } catch (e: java.nio.file.NoSuchFileException) {
        throw SkillSourceException("Configured skills base directory does not exist.", e)
      } catch (e: java.nio.file.NotDirectoryException) {
        throw SkillSourceException("Configured skills base path is not a directory.", e)
      }
    baseDirStream.use { stream ->
      Sequence { stream.iterator() }
        .filter { Files.isDirectory(it) }
        .map { skillDir ->
          try {
            parseSkillFromDir(skillDir).first
          } catch (e: SkillSourceException) {
            throw SkillSourceException("One of the skills is invalid: ${e.message}", e)
          }
        }
        .toList()
    }
  }

  override suspend fun listResources(
    skillName: String,
    resourceDirectoryPath: String,
  ): Result<List<String>> = sourceRunCatching {
    validateBaseDir()
    val skillDirPath = Path.of(skillsBaseDir).resolve(skillName)
    // Validates the skill.
    val unused = parseSkillFromDir(skillDirPath)

    val normalizedPath = Path.of(resourceDirectoryPath).normalize()
    if (normalizedPath.isAbsolute || normalizedPath.startsWith(Path.of(".."))) {
      throw SkillSourceException("Invalid resource path: $resourceDirectoryPath")
    }

    val isRoot = normalizedPath.toString().isEmpty()
    if (!isRoot && normalizedPath.getName(0).toString() !in SkillSource.VALID_RESOURCE_DIRS) {
      throw SkillSourceException(
        "$resourceDirectoryPath must be empty, root (.), or within 'references/', 'assets/', or 'scripts/'"
      )
    }

    val fullResourceDirPath = if (isRoot) skillDirPath else skillDirPath.resolve(normalizedPath)
    if (!Files.exists(fullResourceDirPath)) {
      throw SkillSourceException("Resource not found: $resourceDirectoryPath")
    }

    val targets = if (isRoot) SkillSource.VALID_RESOURCE_DIRS else listOf(normalizedPath.toString())
    buildList {
      for (target in targets) {
        val targetPath = skillDirPath.resolve(target)
        if (Files.exists(targetPath)) {
          Files.walk(targetPath).use { stream ->
            stream
              .asSequence()
              .filter { Files.isRegularFile(it) }
              .map { skillDirPath.relativize(it).toString().replace(java.io.File.separator, "/") }
              .forEach { add(it) }
          }
        }
      }
    }
  }

  override suspend fun loadFrontmatter(skillName: String): Result<Frontmatter> = sourceRunCatching {
    validateBaseDir()
    parseSkillFromDir(Path.of(skillsBaseDir).resolve(skillName)).first
  }

  override suspend fun loadInstructions(skillName: String): Result<String> = sourceRunCatching {
    validateBaseDir()
    parseSkillFromDir(Path.of(skillsBaseDir).resolve(skillName)).second
  }

  override suspend fun loadResource(skillName: String, resourcePath: String): Result<ByteArray> =
    sourceRunCatching {
      validateBaseDir()
      val skillDirPath = Path.of(skillsBaseDir).resolve(skillName)
      // Validates the skill.
      val unused = parseSkillFromDir(skillDirPath)

      val normalizedPath = Path.of(resourcePath).normalize()
      if (normalizedPath.startsWith(Path.of("..")) || normalizedPath.isAbsolute) {
        throw SkillSourceException("Invalid resource path: $resourcePath")
      }
      if (
        normalizedPath.nameCount > 0 &&
          normalizedPath.getName(0).toString() !in SkillSource.VALID_RESOURCE_DIRS
      ) {
        throw SkillSourceException(
          "Invalid resource path: $resourcePath must be within 'references/', 'assets/', or 'scripts/'"
        )
      }

      val fullResourcePath = skillDirPath.resolve(normalizedPath)
      if (!Files.exists(fullResourcePath) || !Files.isRegularFile(fullResourcePath)) {
        throw SkillSourceException("Resource $resourcePath not found in skill $skillName")
      }
      try {
        Files.readAllBytes(fullResourcePath)
      } catch (e: java.io.IOException) {
        throw SkillSourceException(
          "Failed to read resource $resourcePath in skill $skillName: ${e.message}",
          e,
        )
      }
    }

  /**
   * Verifies that [skillsBaseDir] points to an existing directory, throwing [SkillSourceException]
   * otherwise.
   */
  private fun validateBaseDir() {
    val baseDirPath = Path.of(skillsBaseDir)
    if (!baseDirPath.exists()) {
      throw SkillSourceException("Configured skills base directory does not exist.")
    }
    if (!baseDirPath.isDirectory()) {
      throw SkillSourceException("Configured skills base path is not a directory.")
    }
  }
}

private const val SKILL_FILE_NAME = "SKILL.md"
private const val FRONTMATTER_SEPARATOR = "---"

/**
 * Runs [block] and wraps its outcome in a [Result].
 *
 * A successful return is wrapped in [Result.success]; a [SkillSourceException] thrown by [block] is
 * wrapped in [Result.failure] with the message preserved verbatim. Any other exception is
 * intentionally NOT caught and propagates to the caller.
 */
private inline fun <R> sourceRunCatching(block: () -> R): Result<R> =
  try {
    Result.success(block())
  } catch (e: SkillSourceException) {
    Result.failure(e)
  }

private fun parseSkillFromDir(skillDirPath: Path): Pair<Frontmatter, String> {
  val skillName = skillDirPath.name
  if (!skillDirPath.exists() || !skillDirPath.isDirectory()) {
    throw SkillSourceException("Skill $skillName not found.")
  }

  val skillMdPath = skillDirPath.resolve(SKILL_FILE_NAME)
  if (!skillMdPath.exists()) {
    throw SkillSourceException("Skill $skillName is malformed: missing $SKILL_FILE_NAME.")
  }

  val content =
    try {
      skillMdPath.readText()
    } catch (e: java.io.IOException) {
      throw SkillSourceException(
        "Failed to read $SKILL_FILE_NAME for skill $skillName: ${e.message}",
        e,
      )
    }
  val (frontmatterMap, instructions) =
    try {
      parseSkillMdContent(content)
    } catch (e: IllegalArgumentException) {
      throw SkillSourceException(
        "Skill $skillName is malformed: invalid frontmatter: ${e.message}",
        e,
      )
    }

  val name =
    frontmatterMap["name"] as? String
      ?: throw SkillSourceException(
        "Skill $skillName is malformed: 'name' field missing from frontmatter."
      )
  val description =
    frontmatterMap["description"] as? String
      ?: throw SkillSourceException(
        "Skill $skillName is malformed: 'description' field missing from frontmatter."
      )
  if (name != skillName) {
    throw SkillSourceException(
      "Skill $skillName is malformed: name '$name' in $SKILL_FILE_NAME does not match directory name '$skillName'"
    )
  }

  val frontmatter =
    try {
      Frontmatter(
        name = name,
        description = description,
        license = frontmatterMap["license"] as? String,
        compatibility = frontmatterMap["compatibility"] as? String,
        allowedTools =
          frontmatterMap["allowed-tools"] as? String ?: frontmatterMap["allowed_tools"] as? String,
        metadata =
          (frontmatterMap["metadata"] as? Map<*, *>)
            ?.mapNotNull { (k, v) -> if (k is String && v is String) k to v else null }
            ?.toMap() ?: emptyMap(),
      )
    } catch (e: IllegalArgumentException) {
      throw SkillSourceException("Skill $skillName is malformed: ${e.message}", e)
    }

  return frontmatter to instructions
}

/**
 * Parses a `SKILL.md` document into its YAML frontmatter map and the instruction body.
 *
 * @throws IllegalArgumentException if the document is malformed in any of the following ways: the
 *   leading `---` separator is missing; the trailing `---` separator is missing; the frontmatter
 *   body is empty or whitespace-only; the frontmatter is not valid YAML; the frontmatter root is
 *   not a YAML mapping (e.g. it is a list, scalar, or null).
 */
private fun parseSkillMdContent(content: String): Pair<Map<String, Any>, String> {
  if (!content.startsWith(FRONTMATTER_SEPARATOR)) {
    throw IllegalArgumentException(
      "$SKILL_FILE_NAME must start with YAML frontmatter ($FRONTMATTER_SEPARATOR)"
    )
  }
  val parts = content.split(FRONTMATTER_SEPARATOR, limit = 3)
  if (parts.size < 3) {
    throw IllegalArgumentException(
      "$SKILL_FILE_NAME frontmatter not properly closed with $FRONTMATTER_SEPARATOR"
    )
  }

  val frontmatterStr = parts[1]
  val instructions = parts[2].trim()

  if (frontmatterStr.isBlank()) {
    throw IllegalArgumentException("Frontmatter must not be empty.")
  }

  try {
    val yaml = Yaml(SafeConstructor(LoaderOptions()))
    val parsed: Map<String, Any> = yaml.load(frontmatterStr)
    return parsed to instructions
  } catch (e: YAMLException) {
    throw IllegalArgumentException("Invalid YAML in frontmatter: ${e.message}", e)
  } catch (e: ClassCastException) {
    throw IllegalArgumentException("Frontmatter must be a YAML mapping.", e)
  }
}
