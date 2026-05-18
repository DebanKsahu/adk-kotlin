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
import com.google.adk.kt.types.Part
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** A thread-safe in-memory implementation of the [ArtifactService]. */
class InMemoryArtifactService : ArtifactService {

  private val mutex = Mutex()
  private val artifacts: MutableMap<SessionKey, MutableMap<String, MutableList<Part>>> =
    mutableMapOf()

  override suspend fun saveArtifact(sessionKey: SessionKey, filename: String, artifact: Part): Int =
    mutex.withLock {
      val versions =
        artifacts.getOrPut(sessionKey) { mutableMapOf() }.getOrPut(filename) { mutableListOf() }
      versions.add(artifact)
      versions.size - 1
    }

  override suspend fun loadArtifact(
    sessionKey: SessionKey,
    filename: String,
    version: Int?,
  ): Part? = mutex.withLock {
    val versions = artifacts[sessionKey]?.get(filename) ?: return@withLock null

    if (versions.isEmpty()) {
      return null
    }

    if (version == null) {
      return versions.lastOrNull()
    }

    if (version >= 0 && version < versions.size) {
      versions[version]
    } else {
      null
    }
  }

  override suspend fun listArtifactKeys(sessionKey: SessionKey): List<String> = mutex.withLock {
    artifacts[sessionKey]?.keys?.toList() ?: emptyList()
  }

  override suspend fun deleteArtifact(sessionKey: SessionKey, filename: String) {
    mutex.withLock { artifacts[sessionKey]?.remove(filename) }
  }

  override suspend fun saveAndReloadArtifact(
    sessionKey: SessionKey,
    filename: String,
    artifact: Part,
  ): Part {
    val unused = saveArtifact(sessionKey, filename, artifact)
    return artifact
  }

  override suspend fun listVersions(sessionKey: SessionKey, filename: String): List<Int> =
    mutex.withLock {
      artifacts[sessionKey]?.get(filename)?.indices?.toList() ?: emptyList()
    }
}
