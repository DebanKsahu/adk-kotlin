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

package com.google.adk.kt.testing

import com.google.adk.kt.artifacts.ArtifactService
import com.google.adk.kt.sessions.SessionKey
import com.google.adk.kt.types.Part

/**
 * A configurable dummy implementation of [ArtifactService] for testing scenarios.
 *
 * This class provides non-functional no-ops (e.g., returning 0, empty lists, or null) unless
 * configured otherwise via its lambda properties.
 *
 * @param onListArtifactKeys The lambda to execute when [listArtifactKeys] is called, allowing you
 *   to simulate the retrieval of artifact catalogs.
 * @param onLoadArtifact The lambda to execute when [loadArtifact] is called, allowing you to
 *   simulate the loading of existing artifacts into parts.
 */
class DummyArtifactService(
  val onListArtifactKeys: suspend (sessionKey: SessionKey) -> List<String> = { emptyList() },
  val onLoadArtifact: suspend (sessionKey: SessionKey, filename: String, version: Int?) -> Part? =
    { _, _, _ ->
      null
    },
) : ArtifactService {
  override suspend fun saveArtifact(sessionKey: SessionKey, filename: String, artifact: Part): Int =
    0

  override suspend fun saveAndReloadArtifact(
    sessionKey: SessionKey,
    filename: String,
    artifact: Part,
  ): Part = artifact

  override suspend fun loadArtifact(
    sessionKey: SessionKey,
    filename: String,
    version: Int?,
  ): Part? = onLoadArtifact(sessionKey, filename, version)

  override suspend fun listArtifactKeys(sessionKey: SessionKey): List<String> =
    onListArtifactKeys(sessionKey)

  override suspend fun deleteArtifact(sessionKey: SessionKey, filename: String) {}

  override suspend fun listVersions(sessionKey: SessionKey, filename: String): List<Int> =
    emptyList()
}
