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
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertIs

class DefaultMcpTransportBuilderTest {

  private val transportBuilder = DefaultMcpTransportBuilder()

  @Test
  fun build_withSseServerParameters_returnsSseTransport() {
    val params = McpConnectionParameters.Sse(url = "http://localhost:1234")
    val transport = transportBuilder.build(params)

    assertIs<HttpClientSseClientTransport>(transport)
  }

  @Test
  fun build_withStreamableHttpServerParameters_returnsStreamableHttpTransport() {
    val params = McpConnectionParameters.StreamableHttp(url = "http://localhost:1234")

    val transport = transportBuilder.build(params)

    assertIs<HttpClientStreamableHttpTransport>(transport)
  }

  @Test
  fun build_withStdioServerParameters_returnsStdioTransport() {
    val params =
      McpConnectionParameters.Stdio(
        io.modelcontextprotocol.client.transport.ServerParameters.builder("cmd").build()
      )

    val transport = transportBuilder.build(params)

    assertIs<StdioClientTransport>(transport)
  }

  @Test
  fun build_withSseServerParametersAndHeaders_returnsSseTransport() {
    val params =
      McpConnectionParameters.Sse(
        url = "http://localhost:1234",
        headers = mapOf("header1" to "value1"),
      )

    val transport = transportBuilder.build(params)

    assertIs<HttpClientSseClientTransport>(transport)
  }

  @Test
  fun build_withStreamableHttpServerParametersAndHeaders_returnsStreamableHttpTransport() {
    val params =
      McpConnectionParameters.StreamableHttp(
        url = "http://localhost:1234",
        headers = mapOf("header1" to "value1"),
      )

    val transport = transportBuilder.build(params)

    assertIs<HttpClientStreamableHttpTransport>(transport)
  }

  @Test
  fun build_withSseServerParametersAndEmptyHeaders_returnsSseTransport() {
    val params = McpConnectionParameters.Sse(url = "http://localhost:1234", headers = emptyMap())

    val transport = transportBuilder.build(params)

    assertIs<HttpClientSseClientTransport>(transport)
  }

  @Test
  fun build_withStreamableHttpServerParametersAndEmptyHeaders_returnsStreamableHttpTransport() {
    val params =
      McpConnectionParameters.StreamableHttp(url = "http://localhost:1234", headers = emptyMap())

    val transport = transportBuilder.build(params)

    assertIs<HttpClientStreamableHttpTransport>(transport)
  }

  @Test
  fun build_withStreamableHttpWithHeadersAndTimeout_returnsStreamableHttpTransport() {
    val params =
      McpConnectionParameters.StreamableHttp(
        url = "http://localhost:1234",
        headers = mapOf("header1" to "value1"),
        timeout = Duration.ofSeconds(10),
      )

    val transport = transportBuilder.build(params)

    assertIs<HttpClientStreamableHttpTransport>(transport)
  }

  @Test
  fun build_withStdioServerParametersAndTimeout_returnsStdioTransport() {
    val params =
      McpConnectionParameters.Stdio(
        io.modelcontextprotocol.client.transport.ServerParameters.builder("cmd").build(),
        Duration.ofSeconds(10),
      )

    val transport = transportBuilder.build(params)

    assertIs<StdioClientTransport>(transport)
  }
}
