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

import com.google.common.truth.Truth.assertThat
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NewFileSystemSourceTest {

  private lateinit var tempDir: Path
  private lateinit var source: NewFileSystemSource

  @Before
  fun setUp() {
    tempDir = Files.createTempDirectory("new_file_system_source_test")
    source = NewFileSystemSource(tempDir.toString())
  }

  @After
  fun tearDown() {
    tempDir.toFile().deleteRecursively()
  }

  @Test
  fun listFrontmatters_emptyDirectory_returnsEmptyList() = runTest {
    val result = source.listFrontmatters()

    assertThat(result.isSuccess).isTrue()
    assertThat(result.getOrThrow()).isEmpty()
  }

  @Test
  fun listFrontmatters_nonexistentBaseDir_returnsFailure() = runTest {
    val missing = tempDir.resolve("does-not-exist")
    val brokenSource = NewFileSystemSource(missing.toString())

    val result = brokenSource.listFrontmatters()

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("Configured skills base directory does not exist")
    // The absolute filesystem path must not leak into the user-facing message.
    assertThat(exception.message).doesNotContain(missing.toString())
  }

  @Test
  fun listFrontmatters_baseDirIsFile_returnsFailure() = runTest {
    val filePath = tempDir.resolve("not-a-directory")
    Files.writeString(filePath, "some content")
    val brokenSource = NewFileSystemSource(filePath.toString())

    val result = brokenSource.listFrontmatters()

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("Configured skills base path is not a directory")
    // The absolute filesystem path must not leak into the user-facing message.
    assertThat(exception.message).doesNotContain(filePath.toString())
  }

  @Test
  fun listFrontmatters_validSkills_returnsFrontmatters() = runTest {
    val skillDir1 = tempDir.resolve("skill1")
    Files.createDirectory(skillDir1)
    Files.writeString(
      skillDir1.resolve("SKILL.md"),
      """
      ---
      name: skill1
      description: Description 1
      ---
      Instructions 1
      """
        .trimIndent(),
    )

    val skillDir2 = tempDir.resolve("skill2")
    Files.createDirectory(skillDir2)
    Files.writeString(
      skillDir2.resolve("SKILL.md"),
      """
      ---
      name: skill2
      description: Description 2
      ---
      Instructions 2
      """
        .trimIndent(),
    )

    val result = source.listFrontmatters()

    assertThat(result.isSuccess).isTrue()
    val frontmatters = result.getOrThrow()
    assertThat(frontmatters).hasSize(2)
    assertThat(frontmatters.map { it.name }).containsExactly("skill1", "skill2")
  }

  @Test
  fun listFrontmatters_missingSkillMd_returnsFailure() = runTest {
    val skillDir1 = tempDir.resolve("skill1")
    Files.createDirectory(skillDir1)
    Files.writeString(
      skillDir1.resolve("SKILL.md"),
      """
      ---
      name: skill1
      description: Description 1
      ---
      Instructions 1
      """
        .trimIndent(),
    )

    // Invalid: missing SKILL.md
    Files.createDirectory(tempDir.resolve("invalid1"))

    val result = source.listFrontmatters()

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("One of the skills is invalid")
    assertThat(exception.message).contains("missing SKILL.md")
  }

  @Test
  fun listFrontmatters_mismatchedName_returnsFailure() = runTest {
    val invalidDir = tempDir.resolve("invalid2")
    Files.createDirectory(invalidDir)
    Files.writeString(
      invalidDir.resolve("SKILL.md"),
      """
      ---
      name: mismatched
      description: Description
      ---
      """
        .trimIndent(),
    )

    val result = source.listFrontmatters()

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("One of the skills is invalid")
    assertThat(exception.message).contains("does not match directory name")
  }

  @Test
  fun loadFrontmatter_invalidFrontmatterName_returnsFailure() = runTest {
    val skillDir = tempDir.resolve("invalid_name")
    Files.createDirectory(skillDir)
    Files.writeString(
      skillDir.resolve("SKILL.md"),
      """
      ---
      name: invalid_name
      description: Description
      ---
      """
        .trimIndent(),
    )

    val result = source.loadFrontmatter("invalid_name")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message)
      .contains("name may only contain lowercase alphanumeric characters")
  }

  @Test
  fun listResources_returnsPathsRelativeToSkillRoot() = runTest {
    val skillDir = tempDir.resolve("skill1")
    Files.createDirectory(skillDir)
    Files.writeString(skillDir.resolve("SKILL.md"), "---\nname: skill1\ndescription: d\n---\n")

    val referencesDir = skillDir.resolve("references")
    Files.createDirectory(referencesDir)
    Files.writeString(referencesDir.resolve("file1.txt"), "content1")
    Files.writeString(referencesDir.resolve("file2.txt"), "content2")

    val assetsDir = skillDir.resolve("assets")
    Files.createDirectory(assetsDir)
    Files.writeString(assetsDir.resolve("root_file.txt"), "content_root")

    val scriptsDir = skillDir.resolve("scripts")
    Files.createDirectory(scriptsDir)
    Files.writeString(scriptsDir.resolve("run.sh"), "echo hello")

    assertThat(source.listResources("skill1", ".").getOrThrow())
      .containsExactly(
        "assets/root_file.txt",
        "references/file1.txt",
        "references/file2.txt",
        "scripts/run.sh",
      )
    assertThat(source.listResources("skill1", "references").getOrThrow())
      .containsExactly("references/file1.txt", "references/file2.txt")
    assertThat(source.listResources("skill1", "assets").getOrThrow())
      .containsExactly("assets/root_file.txt")
    assertThat(source.listResources("skill1", "scripts").getOrThrow())
      .containsExactly("scripts/run.sh")
  }

  @Test
  fun loadFrontmatter_validSkill_returnsFrontmatter() = runTest {
    val skillDir = tempDir.resolve("skill1")
    Files.createDirectory(skillDir)
    Files.writeString(
      skillDir.resolve("SKILL.md"),
      """
      ---
      name: skill1
      description: Description 1
      ---
      """
        .trimIndent(),
    )

    val result = source.loadFrontmatter("skill1")

    assertThat(result.isSuccess).isTrue()
    assertThat(result.getOrThrow().name).isEqualTo("skill1")
  }

  @Test
  fun loadFrontmatter_nonexistentSkill_returnsFailure() = runTest {
    val result = source.loadFrontmatter("nonexistent")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("Skill nonexistent not found")
  }

  @Test
  fun loadFrontmatter_misconfiguredBaseDir_attributesErrorToBaseDirNotSkill() = runTest {
    val missing = tempDir.resolve("does-not-exist")
    val brokenSource = NewFileSystemSource(missing.toString())

    val result = brokenSource.loadFrontmatter("any-skill")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    val message = exception!!.message
    assertThat(message).contains("Configured skills base directory does not exist")
    // Must not blame the skill: the source itself is misconfigured.
    assertThat(message).doesNotContain("any-skill")
    // Must not leak the absolute path.
    assertThat(message).doesNotContain(missing.toString())
  }

  @Test
  fun loadInstructions_validSkill_returnsInstructions() = runTest {
    val skillDir = tempDir.resolve("skill1")
    Files.createDirectory(skillDir)
    Files.writeString(
      skillDir.resolve("SKILL.md"),
      """
      ---
      name: skill1
      description: Description 1
      ---
      Instructions for skill1
      """
        .trimIndent(),
    )

    val result = source.loadInstructions("skill1")

    assertThat(result.isSuccess).isTrue()
    assertThat(result.getOrThrow()).isEqualTo("Instructions for skill1")
  }

  @Test
  fun loadInstructions_nonexistentSkill_returnsFailure() = runTest {
    val result = source.loadInstructions("nonexistent")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("Skill nonexistent not found")
  }

  @Test
  fun loadResource_validResource_returnsBytes() = runTest {
    val skillDir = tempDir.resolve("skill1")
    Files.createDirectory(skillDir)
    Files.writeString(skillDir.resolve("SKILL.md"), "---\nname: skill1\ndescription: d\n---\n")

    val assetsDir = skillDir.resolve("assets")
    Files.createDirectory(assetsDir)
    Files.writeString(assetsDir.resolve("resource.txt"), "hello world")

    val result = source.loadResource("skill1", "assets/resource.txt")

    assertThat(result.isSuccess).isTrue()
    assertThat(result.getOrThrow().decodeToString()).isEqualTo("hello world")
  }

  @Test
  fun loadResource_nonexistentResource_returnsFailure() = runTest {
    val skillDir = tempDir.resolve("skill1")
    Files.createDirectory(skillDir)
    Files.writeString(skillDir.resolve("SKILL.md"), "---\nname: skill1\ndescription: d\n---\n")

    val result = source.loadResource("skill1", "assets/nonexistent.txt")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message)
      .contains("Resource assets/nonexistent.txt not found in skill skill1")
  }

  @Test
  fun loadResource_unauthorizedDirectory_returnsFailure() = runTest {
    val skillDir = tempDir.resolve("skill1")
    Files.createDirectory(skillDir)
    Files.writeString(skillDir.resolve("SKILL.md"), "---\nname: skill1\ndescription: d\n---\n")

    val result = source.loadResource("skill1", "unauthorized/file.txt")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message)
      .contains("must be within 'references/', 'assets/', or 'scripts/'")
  }

  @Test
  fun loadResource_nonexistentSkill_returnsFailure() = runTest {
    val result = source.loadResource("nonexistent", "assets/resource.txt")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("Skill nonexistent not found")
  }

  @Test
  fun listResources_emptySearchPath_treatsAsRoot() = runTest {
    val skillDir = tempDir.resolve("skill1")
    Files.createDirectory(skillDir)
    Files.writeString(skillDir.resolve("SKILL.md"), "---\nname: skill1\ndescription: d\n---\n")
    val referencesDir = skillDir.resolve("references")
    Files.createDirectory(referencesDir)
    Files.writeString(referencesDir.resolve("file1.txt"), "content1")

    val result = source.listResources("skill1", "")

    assertThat(result.isSuccess).isTrue()
    assertThat(result.getOrThrow()).containsExactly("references/file1.txt")
  }

  @Test
  fun listResources_skillNotFound_returnsFailure() = runTest {
    val result = source.listResources("nonexistent", ".")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("Skill nonexistent not found")
  }

  @Test
  fun listResources_skillNameMismatch_returnsFailure() = runTest {
    val skillDir = tempDir.resolve("test-skill")
    Files.createDirectory(skillDir)
    Files.writeString(skillDir.resolve("SKILL.md"), "---\nname: wrong-skill\ndescription: d\n---\n")

    val result = source.listResources("test-skill", ".")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("does not match directory name")
  }

  @Test
  fun listResources_invalidFrontmatter_returnsFailure() = runTest {
    val skillDir = tempDir.resolve("test-skill")
    Files.createDirectory(skillDir)
    Files.writeString(skillDir.resolve("SKILL.md"), "---\ninvalid: [yaml\n---\n")

    val result = source.listResources("test-skill", ".")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("invalid frontmatter")
  }

  @Test
  fun loadFrontmatter_emptyFrontmatter_returnsFailure() = runTest {
    val skillDir = tempDir.resolve("test-skill")
    Files.createDirectory(skillDir)
    Files.writeString(skillDir.resolve("SKILL.md"), "---\n---\n")

    val result = source.loadFrontmatter("test-skill")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("Frontmatter must not be empty")
  }

  @Test
  fun loadFrontmatter_nonMapFrontmatterRoot_returnsFailure() = runTest {
    val skillDir = tempDir.resolve("test-skill")
    Files.createDirectory(skillDir)
    Files.writeString(skillDir.resolve("SKILL.md"), "---\n- a\n- b\n---\n")

    val result = source.loadFrontmatter("test-skill")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("Frontmatter must be a YAML mapping")
  }

  @Test
  fun listResources_unauthorizedDirectory_returnsFailure() = runTest {
    val skillDir = tempDir.resolve("skill1")
    Files.createDirectory(skillDir)
    Files.writeString(skillDir.resolve("SKILL.md"), "---\nname: skill1\ndescription: d\n---\n")
    val unauthorizedDir = skillDir.resolve("unauthorized")
    Files.createDirectory(unauthorizedDir)

    val result = source.listResources("skill1", "unauthorized")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message)
      .contains("must be empty, root (.), or within 'references/', 'assets/', or 'scripts/'")
  }

  @Test
  fun listResources_directoryNotFound_returnsFailure() = runTest {
    val skillDir = tempDir.resolve("skill1")
    Files.createDirectory(skillDir)
    Files.writeString(skillDir.resolve("SKILL.md"), "---\nname: skill1\ndescription: d\n---\n")

    val result = source.listResources("skill1", "references/missing")

    assertThat(result.isFailure).isTrue()
    val exception = result.exceptionOrNull()
    assertThat(exception).isInstanceOf(SkillSourceException::class.java)
    assertThat(exception!!.message).contains("Resource not found")
  }
}
