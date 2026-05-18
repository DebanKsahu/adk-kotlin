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

import com.google.adk.kt.events.EventActions
import com.google.adk.kt.sessions.State

/**
 * The context provided to agents and tools during a callback, such as when a tool is run.
 *
 * It provides access to the current invocation context, event actions, and state.
 */
class CallbackContext(
  internal val invocationContext: InvocationContext,
  eventActions: EventActions? = null,
) : ReadonlyContext by ReadonlyContextImpl(invocationContext) {
  val agent: BaseAgent = invocationContext.agent

  var eventActions: EventActions = eventActions ?: EventActions()
    private set

  override val state: Map<String, Any>
    get() =
      (invocationContext.session.state.toMap() + eventActions.stateDelta).filterValues {
        it != State.REMOVED
      }

  /** Updates the state delta in the event actions. */
  fun updateState(key: String, value: Any) {
    val newDelta = (eventActions.stateDelta + (key to value)).toMutableMap()
    eventActions = eventActions.copy(stateDelta = newDelta)
  }

  /** Merges the given event actions into the current event actions. */
  fun mergeEventActions(actions: EventActions) {
    eventActions = eventActions.mergeWith(actions)
  }

  /**
   * Triggers memory generation for the current session.
   *
   * This method saves the current session's events to the memory service.
   */
  suspend fun addSessionToMemory() {
    val memoryService =
      invocationContext.memoryService
        ?: throw IllegalStateException(
          "Cannot add session to memory: memory service is not available."
        )
    memoryService.addSessionToMemory(invocationContext.session)
  }
}

/**
 * Creates a callback context for the current invocation.
 *
 * @param eventActions Optional initial event actions.
 */
internal fun InvocationContext.toCallbackContext(
  eventActions: EventActions? = null
): CallbackContext = CallbackContext(this, eventActions)
