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

package com.google.adk.kt.tools

import com.google.adk.kt.agents.InvocationContext
import com.google.adk.kt.agents.ReadonlyContext
import com.google.adk.kt.agents.toReadonlyContext
import com.google.adk.kt.events.EventActions
import com.google.adk.kt.events.ToolConfirmation
import com.google.adk.kt.types.Part

/** ToolContext provides a structured context for executing tools or functions. */
class ToolContext(
  val invocationContext: InvocationContext,
  val actions: EventActions = EventActions(),
  override val functionCallId: String? = null,
  val toolConfirmation: ToolConfirmation? = null,
  override val eventId: String? = null,
) : ReadonlyToolContext {
  override val context: ReadonlyContext
    get() = invocationContext.toReadonlyContext()

  fun requestConfirmation(hint: String? = null, payload: Any? = null) {
    if (functionCallId == null) {
      throw IllegalStateException("functionCallId is not set.")
    }
    actions.requestedToolConfirmations[functionCallId] =
      ToolConfirmation(hint = hint, confirmed = false, payload = payload)
  }

  override suspend fun listArtifacts(): List<String> {
    val service = invocationContext.artifactService ?: return emptyList()
    return service.listArtifactKeys(invocationContext.session.key)
  }

  override suspend fun loadArtifact(name: String, version: Int?): Part? {
    val service = invocationContext.artifactService ?: return null
    return service.loadArtifact(invocationContext.session.key, name, version)
  }
}
