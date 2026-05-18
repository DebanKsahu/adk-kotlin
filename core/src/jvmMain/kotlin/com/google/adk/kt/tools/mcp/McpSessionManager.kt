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

package com.google.adk.kt.tools.mcp

import com.google.adk.kt.logging.LoggerFactory
import io.modelcontextprotocol.client.McpAsyncClient
import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.spec.McpSchema
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities
import java.time.Duration
import kotlin.jvm.JvmStatic
import kotlinx.coroutines.reactor.mono

/**
 * Manages MCP client sessions.
 *
 * This class provides methods for creating and initializing MCP client sessions, handling different
 * connection parameters and transport builders.
 */
internal class McpSessionManager(
  private val connectionParams: McpConnectionParameters,
  private val transportBuilder: McpTransportBuilder = DefaultMcpTransportBuilder(),
  private val progressConsumers: List<(McpSchema.ProgressNotification) -> Unit> = emptyList(),
) : SessionManager {

  /** Creates an asynchronous session. */
  override fun createAsyncSession(headers: Map<String, String>): McpAsyncClient {
    val params =
      if (headers.isNotEmpty()) {
        when (connectionParams) {
          is McpConnectionParameters.Sse ->
            connectionParams.copy(headers = connectionParams.headers + headers)
          is McpConnectionParameters.StreamableHttp ->
            connectionParams.copy(headers = connectionParams.headers + headers)
          else -> connectionParams
        }
      } else {
        connectionParams
      }
    return initializeAsyncSession(params, transportBuilder, progressConsumers)
  }

  companion object {
    /**
     * Initializes an asynchronous MCP client session.
     *
     * @param connectionParams The parameters for the MCP connection.
     * @param transportBuilder The builder for the MCP transport.
     * @param progressConsumers The progress consumers for the MCP client.
     * @return An initialized McpAsyncClient.
     */
    @JvmStatic
    fun initializeAsyncSession(
      connectionParams: McpConnectionParameters,
      transportBuilder: McpTransportBuilder = DefaultMcpTransportBuilder(),
      progressConsumers: List<(McpSchema.ProgressNotification) -> Unit> = emptyList(),
    ): McpAsyncClient {
      val (initializationTimeout: Duration?, requestTimeout: Duration?) =
        when (connectionParams) {
          is McpConnectionParameters.Stdio -> null to connectionParams.timeoutDuration
          is McpConnectionParameters.Sse ->
            connectionParams.timeout to connectionParams.sseReadTimeout
          is McpConnectionParameters.StreamableHttp ->
            connectionParams.timeout to connectionParams.readTimeout
        }

      val transport = transportBuilder.build(connectionParams)
      val builder =
        McpClient.async(transport)
          .initializationTimeout(initializationTimeout ?: Duration.ofMinutes(5))
          .requestTimeout(requestTimeout ?: Duration.ofMinutes(5))
          .capabilities(ClientCapabilities.builder().build())
          .loggingConsumer { notification ->
            mono {
                val data = notification.data()
                when (notification.level()) {
                  McpSchema.LoggingLevel.DEBUG -> logger.debug { data.toString() }
                  McpSchema.LoggingLevel.INFO,
                  McpSchema.LoggingLevel.NOTICE -> logger.info { data.toString() }
                  McpSchema.LoggingLevel.WARNING -> logger.warn { data.toString() }
                  McpSchema.LoggingLevel.ERROR,
                  McpSchema.LoggingLevel.CRITICAL,
                  McpSchema.LoggingLevel.ALERT,
                  McpSchema.LoggingLevel.EMERGENCY -> logger.error { data.toString() }
                  null -> logger.info { data.toString() }
                }
                null
              }
              .then()
          }

      for (consumer in progressConsumers) {
        builder.progressConsumer { notification -> mono { consumer(notification) }.then() }
      }
      return builder.build()
    }

    private val logger = LoggerFactory.getLogger(McpSessionManager::class)
  }
}
