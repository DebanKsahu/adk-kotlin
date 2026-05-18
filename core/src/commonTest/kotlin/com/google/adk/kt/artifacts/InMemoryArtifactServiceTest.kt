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

package com.google.adk.kt.artifacts

import com.google.adk.kt.sessions.SessionKey
import com.google.adk.kt.types.Blob
import com.google.adk.kt.types.Part
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

/** Unit tests for [InMemoryArtifactService]. */
class InMemoryArtifactServiceTest {

  private lateinit var service: InMemoryArtifactService

  @BeforeTest
  fun setUp() {
    service = InMemoryArtifactService()
  }

  @Test
  fun saveArtifact_savesAndReturnsVersion() = runTest {
    val artifact =
      Part(inlineData = Blob(data = "test content".toByteArray(), mimeType = "text/plain"))
    val version = service.saveArtifact(SESSION_KEY, FILENAME, artifact)
    assertEquals(0, version)
  }

  @Test
  fun loadArtifact_loadsLatest() = runTest {
    val artifact1 =
      Part(inlineData = Blob(data = "content 1".toByteArray(), mimeType = "text/plain"))
    val artifact2 =
      Part(inlineData = Blob(data = "content 2".toByteArray(), mimeType = "text/plain"))
    val unused1 = service.saveArtifact(SESSION_KEY, FILENAME, artifact1)
    val unused2 = service.saveArtifact(SESSION_KEY, FILENAME, artifact2)

    val result = service.loadArtifact(SESSION_KEY, FILENAME)
    assertEquals(artifact2, result)
  }

  @Test
  fun loadArtifact_loadsByVersion() = runTest {
    val artifact1 =
      Part(inlineData = Blob(data = "content 1".toByteArray(), mimeType = "text/plain"))
    val artifact2 =
      Part(inlineData = Blob(data = "content 2".toByteArray(), mimeType = "text/plain"))
    val unused1 = service.saveArtifact(SESSION_KEY, FILENAME, artifact1)
    val unused2 = service.saveArtifact(SESSION_KEY, FILENAME, artifact2)

    val result = service.loadArtifact(SESSION_KEY, FILENAME, 0)
    assertEquals(artifact1, result)
  }

  @Test
  fun saveAndReloadArtifact_reloadsArtifact() = runTest {
    val artifact =
      Part(inlineData = Blob(data = "test content".toByteArray(), mimeType = "text/plain"))
    val result = service.saveAndReloadArtifact(SESSION_KEY, FILENAME, artifact)
    assertEquals(artifact, result)
  }

  @Test
  fun listArtifactKeys_returnsFilenames() = runTest {
    val artifact = Part(inlineData = Blob(data = "content".toByteArray(), mimeType = "text/plain"))
    val unused1 = service.saveArtifact(SESSION_KEY, "file1.txt", artifact)
    val unused2 = service.saveArtifact(SESSION_KEY, "file2.txt", artifact)

    val response = service.listArtifactKeys(SESSION_KEY)
    assertContentEquals(listOf("file1.txt", "file2.txt"), response)
  }

  @Test
  fun deleteArtifact_removesArtifact() = runTest {
    val artifact = Part(inlineData = Blob(data = "content".toByteArray(), mimeType = "text/plain"))
    val unused = service.saveArtifact(SESSION_KEY, FILENAME, artifact)

    service.deleteArtifact(SESSION_KEY, FILENAME)

    val result = service.loadArtifact(SESSION_KEY, FILENAME)
    assertNull(result)
  }

  companion object {
    private const val APP_NAME = "test-app"
    private const val USER_ID = "test-user"
    private const val SESSION_ID = "test-session"
    private const val FILENAME = "test-file.txt"
    private val SESSION_KEY = SessionKey(appName = APP_NAME, userId = USER_ID, id = SESSION_ID)
  }
}
