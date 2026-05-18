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

package com.google.adk.kt.agents

import com.google.adk.kt.artifacts.ArtifactService
import com.google.adk.kt.events.Event
import com.google.adk.kt.memory.MemoryService
import com.google.adk.kt.sessions.Session
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.Part

/**
 * Internal implementation of the [ReadonlyContext] interface.
 *
 * @property invocationContext The invocation context to wrap.
 */
internal open class ReadonlyContextImpl(protected val invocationContext: InvocationContext) :
  ReadonlyContext {
  override val session: Session
    get() = invocationContext.session.copy()

  override val runConfig: RunConfig?
    get() = invocationContext.runConfig?.copy()

  override val invocationId: String
    get() = invocationContext.invocationId

  override val agentName: String
    get() = invocationContext.agent.name

  override val state: Map<String, Any>
    get() = invocationContext.session.state.toMap()

  override val userId: String
    get() = invocationContext.session.key.userId

  override val userContent: Content?
    get() =
      invocationContext.userContent?.let { content ->
        Content(content.role, content.parts.map { Part(it.text, it.inlineData, it.fileData) })
      }

  override val branch: String?
    get() = invocationContext.branch

  override val artifactService: ArtifactService?
    get() = invocationContext.artifactService

  override val memoryService: MemoryService?
    get() = invocationContext.memoryService

  override suspend fun getEvents(currentInvocation: Boolean, currentBranch: Boolean): List<Event> {
    return invocationContext.getEvents(currentInvocation, currentBranch)
  }
}
