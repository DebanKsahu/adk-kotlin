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

import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport
import io.modelcontextprotocol.client.transport.StdioClientTransport
import io.modelcontextprotocol.json.McpJsonDefaults
import io.modelcontextprotocol.spec.McpClientTransport

/**
 * The default builder for creating MCP client transports. Supports StdioClientTransport based on
 * [McpConnectionParameters.Stdio], HttpClientSseClientTransport based on
 * [McpConnectionParameters.Sse], and HttpClientStreamableHttpTransport based on
 * [McpConnectionParameters.StreamableHttp].
 */
internal class DefaultMcpTransportBuilder : McpTransportBuilder {

  override fun build(connectionParams: McpConnectionParameters): McpClientTransport =
    when (connectionParams) {
      is McpConnectionParameters.Stdio ->
        StdioClientTransport(connectionParams.serverParameters, jsonMapper)
      is McpConnectionParameters.Sse -> {
        HttpClientSseClientTransport.builder(connectionParams.url)
          .sseEndpoint(connectionParams.sseEndpoint)
          .jsonMapper(jsonMapper)
          .httpRequestCustomizer { builder, _, _, _, _ ->
            connectionParams.headers.forEach { (key, value) -> builder.header(key, value) }
          }
          .build()
      }
      is McpConnectionParameters.StreamableHttp -> {
        HttpClientStreamableHttpTransport.builder(connectionParams.url)
          .connectTimeout(connectionParams.timeout)
          .jsonMapper(jsonMapper)
          .httpRequestCustomizer { builder, _, _, _, _ ->
            connectionParams.headers.forEach { (key, value) -> builder.header(key, value) }
          }
          .build()
      }
    }

  companion object {
    private val jsonMapper = McpJsonDefaults.getMapper()
  }
}
