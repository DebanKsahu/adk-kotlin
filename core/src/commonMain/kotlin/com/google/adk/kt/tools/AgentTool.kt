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

import com.google.adk.kt.agents.BaseAgent
import com.google.adk.kt.agents.LlmAgent
import com.google.adk.kt.runners.InMemoryRunner
import com.google.adk.kt.serialization.Json
import com.google.adk.kt.sessions.InMemorySessionService
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.FunctionDeclaration
import com.google.adk.kt.types.Part
import com.google.adk.kt.types.Schema
import com.google.adk.kt.types.Type
import kotlinx.coroutines.flow.lastOrNull

/**
 * A tool that wraps a [BaseAgent].
 *
 * This tool allows an agent to be called as a tool within a larger application. The agent's input
 * schema is used to define the tool's input parameters, and the agent's output is returned as the
 * tool's result.
 *
 * @property agent The agent to wrap.
 * @property skipSummarization Whether to skip summarization of the agent output in the parent
 *   agent.
 */
class AgentTool(val agent: BaseAgent, val skipSummarization: Boolean = false) :
  BaseTool(name = agent.name, description = agent.description) {

  private val inputSchema: Schema? by lazy { getInputSchema(agent) }

  /**
   * Recursively finds the input schema from the first [LlmAgent] encountered in a depth-first
   * traversal of the agent's sub-agents. Only the first sub-agent is considered at each level.
   */
  private fun getInputSchema(agent: BaseAgent): Schema? =
    when {
      agent is LlmAgent -> agent.inputSchema
      agent.subAgents.isNotEmpty() -> getInputSchema(agent.subAgents.first())
      else -> null
    }

  override fun declaration(): FunctionDeclaration {
    return FunctionDeclaration(
      name = name,
      description = description,
      parameters =
        inputSchema
          ?: Schema(
            type = Type.OBJECT,
            properties = mapOf(REQUEST_KEY to Schema(type = Type.STRING)),
            required = listOf(REQUEST_KEY),
          ),
    )
  }

  override suspend fun run(context: ToolContext, args: Map<String, Any>): Any {
    if (skipSummarization) {
      context.actions.skipSummarization = true
    }

    val content =
      if (inputSchema != null) {
        Content(parts = listOf(Part(text = Json.toJsonString(args))))
      } else {
        val request =
          args[REQUEST_KEY] ?: throw IllegalArgumentException("Missing '$REQUEST_KEY' argument")
        Content(parts = listOf(Part(text = request.toString())))
      }

    val runner =
      InMemoryRunner(
        agent = agent,
        appName = context.invocationContext.session.key.appName,
        sessionService = context.invocationContext.sessionService ?: InMemorySessionService(),
      )

    // Pass non-internal state from the parent context to the child agent.
    val parentState =
      context.actions.stateDelta
        .filterKeys { key -> !key.startsWith("_adk") }
        .takeIf { it.isNotEmpty() }

    val lastEvent =
      runner
        .runAsync(
          userId = context.invocationContext.session.key.userId,
          sessionId = context.invocationContext.session.key.id!!,
          newMessage = content,
          stateDelta = parentState,
        )
        .lastOrNull()

    if (lastEvent != null) {
      // Propagate actions back to the parent context
      context.actions.stateDelta.putAll(lastEvent.actions.stateDelta)
      context.actions.artifactDelta.putAll(lastEvent.actions.artifactDelta)

      val text =
        lastEvent.content
          ?.parts
          ?.filter { it.thought != true }
          ?.joinToString("\n") { it.text ?: "" } ?: ""
      return text
    }

    return ""
  }

  companion object {
    private const val REQUEST_KEY = "request"
  }
}
