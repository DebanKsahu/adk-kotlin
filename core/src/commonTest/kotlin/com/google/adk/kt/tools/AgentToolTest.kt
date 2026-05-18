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
import com.google.adk.kt.agents.LlmAgent
import com.google.adk.kt.agents.RunConfig
import com.google.adk.kt.collections.concurrentMutableMapOf
import com.google.adk.kt.events.Event
import com.google.adk.kt.events.EventActions
import com.google.adk.kt.models.LlmResponse
import com.google.adk.kt.runners.InMemoryRunner
import com.google.adk.kt.sessions.InMemorySessionService
import com.google.adk.kt.sessions.Session
import com.google.adk.kt.sessions.SessionKey
import com.google.adk.kt.sessions.State
import com.google.adk.kt.testing.DummyAgent
import com.google.adk.kt.testing.DummyModel
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.FunctionCall
import com.google.adk.kt.types.Part
import com.google.adk.kt.types.Schema
import com.google.adk.kt.types.Type
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class AgentToolTest {

  @Test
  fun declaration_withInputSchema_buildsCorrectParameters() {
    val inputSchema =
      Schema(
        type = Type.OBJECT,
        properties = mapOf("query" to Schema(type = Type.STRING)),
        required = listOf("query"),
      )
    val agent =
      LlmAgent(name = "inner-agent", model = DummyModel("test"), inputSchema = inputSchema)
    val tool = AgentTool(agent)

    val declaration = tool.declaration()

    assertNotNull(declaration)
    assertEquals("inner-agent", declaration.name)
    assertEquals(inputSchema, declaration.parameters)
  }

  @Test
  fun declaration_withoutInputSchema_fallsBackToRequest() {
    val agent = LlmAgent(name = "inner-agent", model = DummyModel("test"))
    val tool = AgentTool(agent)

    val declaration = tool.declaration()

    assertNotNull(declaration)
    assertEquals("inner-agent", declaration.name)
    val parameters = declaration.parameters
    assertNotNull(parameters)
    assertEquals(Type.OBJECT, parameters.type)
    assertEquals(1, parameters.properties?.size)
    assertEquals(Type.STRING, parameters.properties?.get("request")?.type)
  }

  @Test
  fun run_executesInnerAgent_returnsResult() = runTest {
    val responseContent = Content(parts = listOf(Part(text = "Response from inner agent")))
    val model = DummyModel("test") { flowOf(LlmResponse(content = responseContent)) }
    val agent = LlmAgent(name = "inner-agent", model = model)
    val tool = AgentTool(agent)
    val context = ToolContext(invocationContext = getTestInvocationContext(agent))

    val result = tool.run(context, mapOf("request" to "Hello"))

    assertEquals("Response from inner agent", result)
  }

  @Test
  fun run_executesInnerAgent_propagatesActions() = runTest {
    val responseContent = Content(parts = listOf(Part(text = "Response from inner agent")))
    val eventActions =
      EventActions(
        stateDelta = mutableMapOf("testStateKey" to "testStateValue"),
        artifactDelta = mutableMapOf("testArtifactKey" to 1),
      )
    val agent =
      DummyAgent(
        name = "inner-agent",
        onRunAsync = {
          emit(Event(author = "inner-agent", content = responseContent, actions = eventActions))
        },
      )
    val tool = AgentTool(agent)
    val context = ToolContext(invocationContext = getTestInvocationContext(agent))

    val result = tool.run(context, mapOf("request" to "Hello"))

    assertEquals("Response from inner agent", result)
    assertEquals("testStateValue", context.actions.stateDelta["testStateKey"])
    assertEquals(1, context.actions.artifactDelta["testArtifactKey"])
  }

  @Test
  fun run_withSkipSummarization_setsFlagInContext() = runTest {
    val responseContent = Content(parts = listOf(Part(text = "Response")))
    val model = DummyModel("test") { flowOf(LlmResponse(content = responseContent)) }
    val agent = LlmAgent(name = "inner-agent", model = model)
    val tool = AgentTool(agent, skipSummarization = true)
    val context = ToolContext(invocationContext = getTestInvocationContext(agent))

    val unused = tool.run(context, mapOf("request" to "Hello"))

    assertEquals(true, context.actions.skipSummarization)
  }

  @Test
  fun declaration_returnsCorrectTool() {
    val agent = LlmAgent(name = "inner-agent", model = DummyModel("test"))
    val tool = AgentTool(agent)

    val declaration = tool.declaration()

    assertNotNull(declaration)
    assertEquals("inner-agent", declaration.name)
  }

  @Test
  fun declaration_withNonLlmAgent_resolvesSubAgentSchema() {
    val inputSchema =
      Schema(
        type = Type.OBJECT,
        properties = mapOf("query" to Schema(type = Type.STRING)),
        required = listOf("query"),
      )
    val inner =
      LlmAgent(name = "inner-agent", model = DummyModel("test"), inputSchema = inputSchema)
    val wrapper =
      com.google.adk.kt.testing.DummyAgent(name = "wrapper-agent", subAgents = listOf(inner))
    val tool = AgentTool(wrapper)

    val declaration = tool.declaration()

    assertNotNull(declaration)
    assertEquals("wrapper-agent", declaration.name)
    assertEquals(inputSchema, declaration.parameters)
  }

  @Test
  fun declaration_withNonLlmAgentNoSubAgents_fallsBackToRequest() {
    val wrapper = com.google.adk.kt.testing.DummyAgent(name = "wrapper-agent")
    val tool = AgentTool(wrapper)

    val declaration = tool.declaration()

    assertNotNull(declaration)
    assertEquals("wrapper-agent", declaration.name)
    val parameters = declaration.parameters
    assertNotNull(parameters)
    assertEquals(Type.OBJECT, parameters.type)
    assertEquals(1, parameters.properties?.size)
    assertEquals(Type.STRING, parameters.properties?.get("request")?.type)
  }

  @Test
  fun run_throughInMemoryRunner_executesSuccessfully() = runTest {
    val agentTool =
      AgentTool(
        LlmAgent(
          name = "inner-agent",
          model =
            DummyModel("inner-model") {
              flowOf(
                LlmResponse(
                  content = Content(parts = listOf(Part(text = "Response from inner agent")))
                )
              )
            },
        )
      )
    val mainModel =
      DummyModel(
        name = "main-model",
        flows =
          listOf(
            flowOf(
              LlmResponse(
                content =
                  Content(
                    parts =
                      listOf(
                        Part(
                          functionCall =
                            FunctionCall(
                              name = "inner-agent",
                              args = mapOf("request" to "Hello inner"),
                            )
                        )
                      )
                  )
              )
            ),
            flowOf(LlmResponse(content = Content(parts = listOf(Part(text = "Final Answer"))))),
          ),
      )
    val mainAgent = LlmAgent(name = "main-agent", model = mainModel, tools = listOf(agentTool))
    val runner = InMemoryRunner(agent = mainAgent)

    val events =
      runner
        .runAsync(
          userId = "user1",
          sessionId = "session1",
          newMessage = Content(parts = listOf(Part(text = "hi"))),
        )
        .toList()

    assertEquals(3, events.size)

    val callEvent = events[0]
    val functionCall = callEvent.content?.parts?.first()?.functionCall
    assertNotNull(functionCall)
    assertEquals("inner-agent", functionCall.name)
    assertEquals("Hello inner", functionCall.args["request"])

    val toolResponseEvent = events[1]
    val functionResponse = toolResponseEvent.content?.parts?.first()?.functionResponse
    assertNotNull(functionResponse)
    assertEquals("inner-agent", functionResponse.name)
    assertEquals("Response from inner agent", functionResponse.response["result"])

    assertEquals("Final Answer", events[2].content?.parts?.first()?.text)
  }

  private fun getTestInvocationContext(agent: com.google.adk.kt.agents.BaseAgent) =
    InvocationContext(
      session =
        Session(
          key = SessionKey(appName = "app-name", userId = "user-id", id = "session-id"),
          state = State(concurrentMutableMapOf()),
          events = mutableListOf(),
        ),
      runConfig = RunConfig(),
      agent = agent,
      sessionService = InMemorySessionService(),
      invocationId = "test-invocation-id",
    )
}
