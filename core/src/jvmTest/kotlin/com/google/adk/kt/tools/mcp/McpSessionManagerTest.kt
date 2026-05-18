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

import io.modelcontextprotocol.client.McpAsyncClient
import io.modelcontextprotocol.spec.McpClientTransport
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class McpSessionManagerTest {

  @Test
  fun createAsyncSession_withSseServerParameters_setsCorrectTimeouts() {
    val params =
      McpConnectionParameters.Sse(
        url = "http://localhost:1234",
        timeout = Duration.ofSeconds(10),
        sseReadTimeout = Duration.ofSeconds(20),
      )
    val sessionManager = McpSessionManager(params)

    val client = sessionManager.createAsyncSession()

    assertNotNull(client)
    assertEquals(Duration.ofSeconds(10), client.initializationTimeout)
    assertEquals(Duration.ofSeconds(20), client.requestTimeout)
  }

  @Test
  fun createAsyncSession_withSseServerParametersAndDefaultTimeouts_setsCorrectTimeouts() {
    val params = McpConnectionParameters.Sse(url = "http://localhost:1234")
    val sessionManager = McpSessionManager(params)

    val client = sessionManager.createAsyncSession()

    assertNotNull(client)
    assertEquals(Duration.ofSeconds(5), client.initializationTimeout)
    assertEquals(Duration.ofMinutes(5), client.requestTimeout)
  }

  @Test
  fun createAsyncSession_withStreamableHttpServerParameters_setsCorrectTimeouts() {
    val params =
      McpConnectionParameters.StreamableHttp(
        url = "http://localhost:1234",
        timeout = Duration.ofSeconds(10),
        readTimeout = Duration.ofSeconds(20),
      )
    val sessionManager = McpSessionManager(params)

    val client = sessionManager.createAsyncSession()

    assertNotNull(client)
    assertEquals(Duration.ofSeconds(10), client.initializationTimeout)
    assertEquals(Duration.ofSeconds(20), client.requestTimeout)
  }

  @Test
  fun createAsyncSession_withStreamableHttpServerParametersAndDefaultTimeouts_setsCorrectTimeouts() {
    val params = McpConnectionParameters.StreamableHttp(url = "http://localhost:1234")
    val sessionManager = McpSessionManager(params)

    val client = sessionManager.createAsyncSession()

    assertNotNull(client)
    assertEquals(Duration.ofSeconds(5), client.initializationTimeout)
    assertEquals(Duration.ofMinutes(5), client.requestTimeout)
  }

  @Test
  fun createAsyncSession_withStdioConnectionParameters_setsCorrectTimeouts() {
    val params =
      McpConnectionParameters.Stdio(
        io.modelcontextprotocol.client.transport.ServerParameters.builder("cmd").build(),
        timeoutDuration = Duration.ofSeconds(30),
      )
    val sessionManager = McpSessionManager(params)

    val client = sessionManager.createAsyncSession()

    assertNotNull(client)
    assertEquals(Duration.ofMinutes(5), client.initializationTimeout)
    assertEquals(Duration.ofSeconds(30), client.requestTimeout)
  }

  @Test
  fun createAsyncSession_withStdioConnectionParametersAndDefaultTimeouts_setsCorrectTimeouts() {
    val params =
      McpConnectionParameters.Stdio(
        io.modelcontextprotocol.client.transport.ServerParameters.builder("cmd").build()
      )
    val sessionManager = McpSessionManager(params)

    val client = sessionManager.createAsyncSession()

    assertNotNull(client)
    assertEquals(Duration.ofMinutes(5), client.initializationTimeout)
    assertEquals(Duration.ofSeconds(5), client.requestTimeout)
  }

  @Test
  fun createAsyncSession_withSseServerParametersAndHeaders_mergesHeaders() {
    val params =
      McpConnectionParameters.Sse(
        url = "http://localhost:1234",
        headers = mapOf("EXISTING_HEADER" to "EXISTING_VALUE"),
        timeout = Duration.ofSeconds(10),
        sseReadTimeout = Duration.ofSeconds(20),
      )
    val transport = mock<McpClientTransport>()
    whenever(transport.protocolVersions()).thenReturn(listOf("2.0"))
    val transportBuilder = mock<McpTransportBuilder>()
    whenever(transportBuilder.build(any())).thenReturn(transport)
    val sessionManager = McpSessionManager(params, transportBuilder)

    val unused = sessionManager.createAsyncSession(mapOf("NEW_HEADER" to "NEW_VALUE"))

    val expectedParams =
      params.copy(headers = params.headers + mapOf("NEW_HEADER" to "NEW_VALUE"))
    verify(transportBuilder).build(expectedParams)
  }

  @Test
  fun createAsyncSession_withStreamableHttpServerParametersAndHeaders_mergesHeaders() {
    val params =
      McpConnectionParameters.StreamableHttp(
        url = "http://localhost:1234",
        headers = mapOf("EXISTING_HEADER" to "EXISTING_VALUE"),
        timeout = Duration.ofSeconds(10),
        readTimeout = Duration.ofSeconds(20),
      )
    val transport = mock<McpClientTransport>()
    whenever(transport.protocolVersions()).thenReturn(listOf("2.0"))
    val transportBuilder = mock<McpTransportBuilder>()
    whenever(transportBuilder.build(any())).thenReturn(transport)
    val sessionManager = McpSessionManager(params, transportBuilder)

    val unused = sessionManager.createAsyncSession(mapOf("NEW_HEADER" to "NEW_VALUE"))

    val expectedParams =
      params.copy(headers = params.headers + mapOf("NEW_HEADER" to "NEW_VALUE"))
    verify(transportBuilder).build(expectedParams)
  }
}

// NOTE: Using reflection to access private timeout fields in the external McpAsyncClient library.
// Ideally, we would test timeout behavior, but that would require extensive mocking of the
// transport layer.
// This check ensures our McpSessionManager is configuring the client as expected.
private val McpAsyncClient.initializationTimeout: Duration
  get() {
    val initializer = getPrivateField<Any>(this, "initializer")
    return getPrivateField<Duration>(initializer, "initializationTimeout")
  }

private val McpAsyncClient.requestTimeout: Duration
  get() {
    val initializer = getPrivateField<Any>(this, "initializer")
    val sessionSupplier = getPrivateField<Any>(initializer, "sessionSupplier")
    // The sessionSupplier is a lambda, and its captured arguments are stored as fields.
    // The field names are not guaranteed, so we search by type.
    val requestTimeoutField =
      sessionSupplier.javaClass.declaredFields.first { it.type == Duration::class.java }
    requestTimeoutField.isAccessible = true
    return requestTimeoutField.get(sessionSupplier) as Duration
  }

private fun <T> getPrivateField(obj: Any, name: String): T {
  val field = obj.javaClass.getDeclaredField(name)
  field.isAccessible = true
  @Suppress("UNCHECKED_CAST")
  return field.get(obj) as T
}
