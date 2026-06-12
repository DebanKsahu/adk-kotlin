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

package com.google.adk.kt.examples.a2a

import com.google.adk.kt.a2a.agent.JvmA2AAgent
import io.a2a.client.Client
import io.a2a.client.transport.jsonrpc.JSONRPCTransport
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig
import io.a2a.spec.AgentCapabilities
import io.a2a.spec.AgentCard

/**
 * Example agent demonstrating how to use [RemoteA2AAgent] to communicate with a remote
 * A2A-compliant agent.
 *
 * This demo showcases:
 * 1. Initializing an [io.a2a.client.Client] with a specific transport (REST).
 * 2. Using [ServiceLoader] (implicit via A2ACardResolver) for platform-specific HTTP client
 *    resolution (JDK vs Android).
 * 3. Wrapping the remote agent as a standard ADK [com.google.adk.kt.agents.Agent].
 */
object JvmA2AAgentDemo {

  @JvmField
  val rootAgent = run {
    println("Starting JvmA2AAgentDemo...")

    val agentUrl = System.getenv("A2A_AGENT_URL") ?: "http://localhost:8080/a2a"
    val agentName = System.getenv("A2A_AGENT_NAME") ?: "remote-agent"

    val agentCard =
      AgentCard.Builder()
        .name(agentName)
        .url(agentUrl)
        .description("A remote A2A agent")
        .version("1.0.0")
        .protocolVersion("0.3.0")
        .preferredTransport("JSONRPC")
        .defaultInputModes(listOf("text"))
        .defaultOutputModes(listOf("text"))
        .capabilities(
          // Advertise non-streaming so the a2a Client uses `message/send` instead
          // of SSE `message/stream`. This is what selects the transport in
          // io.a2a.client.Client (it checks agentCard.capabilities().streaming()).
          AgentCapabilities.Builder()
            .streaming(false)
            .pushNotifications(false)
            .stateTransitionHistory(false)
            .build()
        )
        .skills(emptyList())
        .build()

    val a2aClient =
      Client.builder(agentCard)
        .withTransport(JSONRPCTransport::class.java, JSONRPCTransportConfig())
        .build()

    // Use non-streaming (message/send) so the demo works against any A2A server
    // regardless of its streaming support.
    JvmA2AAgent(name = agentName, client = a2aClient, agentCard = agentCard, streaming = false)
  }
}
