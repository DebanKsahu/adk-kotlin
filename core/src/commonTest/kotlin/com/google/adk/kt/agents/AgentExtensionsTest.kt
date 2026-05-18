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

import com.google.adk.kt.events.Event
import com.google.adk.kt.logging.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class AgentExtensionsTest {

  private val logger = LoggerFactory.getLogger(AgentExtensionsTest::class)

  private class DummyAgent(name: String) : BaseAgent(name = name) {
    override fun runAsyncImpl(context: InvocationContext): Flow<Event> = emptyFlow()
  }

  @Test
  fun testFindIndexForResumption() {
    val subAgents = listOf(DummyAgent("agent1"), DummyAgent("agent2"), DummyAgent("agent3"))

    assertEquals(0, subAgents.findIndexForResumption(null, logger))
    assertEquals(0, subAgents.findIndexForResumption("agent1", logger))
    assertEquals(1, subAgents.findIndexForResumption("agent2", logger))
    assertEquals(2, subAgents.findIndexForResumption("agent3", logger))
    assertEquals(0, subAgents.findIndexForResumption("missing_agent", logger))
  }
}
