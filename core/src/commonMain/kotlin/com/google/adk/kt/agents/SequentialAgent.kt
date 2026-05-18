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

import com.google.adk.kt.callbacks.AfterAgentCallback
import com.google.adk.kt.callbacks.BeforeAgentCallback
import com.google.adk.kt.events.Event
import com.google.adk.kt.logging.LoggerFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Persistent state of a [SequentialAgent].
 *
 * @property currentSubAgent Name of the current or next sub-agent.
 */
data class SequentialAgentState(val currentSubAgent: String) : AgentState {
  override fun toStateValue(): TypedData.MapValue {
    return TypedData.MapValue(
      mapOf(KEY_CURRENT_SUB_AGENT to TypedData.StringValue(currentSubAgent))
    )
  }

  companion object {
    private const val KEY_CURRENT_SUB_AGENT = "current_sub_agent"

    fun fromValue(node: TypedData.MapValue): SequentialAgentState? {
      val currentSubAgent = node.getString(KEY_CURRENT_SUB_AGENT) ?: return null
      return SequentialAgentState(currentSubAgent)
    }
  }
}

/** A shell agent that runs its sub-agents in sequence. */
class SequentialAgent(
  name: String,
  description: String = "",
  subAgents: List<BaseAgent> = emptyList(),
  beforeAgentCallbacks: List<BeforeAgentCallback> = emptyList(),
  afterAgentCallbacks: List<AfterAgentCallback> = emptyList(),
) :
  BaseAgent(
    name = name,
    description = description,
    subAgents = subAgents,
    beforeAgentCallbacks = beforeAgentCallbacks,
    afterAgentCallbacks = afterAgentCallbacks,
  ) {

  override fun runAsyncImpl(context: InvocationContext): Flow<Event> = flow {
    if (subAgents.isEmpty()) return@flow

    val agentState = context.agentStates[name] as? TypedData.MapValue
    val sequentialState = agentState?.let { SequentialAgentState.fromValue(it) }

    val startSubAgentName = sequentialState?.currentSubAgent
    val startIndex = subAgents.findIndexForResumption(startSubAgentName, logger)

    var isResuming = startSubAgentName != null

    for (subAgent in subAgents.subList(startIndex, subAgents.size)) {

      if (context.isResumable && !isResuming) {
        saveAndEmitState(context, SequentialAgentState(subAgent.name))
      }

      isResuming = false

      var pauseInvocation = false
      subAgent.runAsync(context).collect { event ->
        emit(event)
        if (context.shouldPauseInvocation(event)) {
          pauseInvocation = true
        }
      }

      if (pauseInvocation) {
        return@flow // Return from flow to pause
      }
    }

    if (context.isResumable) {
      emitEndOfAgent(context)
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger(SequentialAgent::class)
  }
}
