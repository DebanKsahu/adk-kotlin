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

import com.google.adk.kt.models.LlmRequest
import com.google.adk.kt.testing.DummyModel
import com.google.adk.kt.testing.testToolContext
import com.google.adk.kt.types.GenerateContentConfig
import com.google.adk.kt.types.GoogleSearch
import com.google.adk.kt.types.Tool
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class UrlContextToolTest {

  @Test
  fun declaration_returnsNull() {
    val tool = UrlContextTool()
    assertNull(tool.declaration())
  }

  @Test
  fun run_throwsUnsupportedOperation() = runTest {
    val tool = UrlContextTool()
    val context = testToolContext()

    assertFailsWith<UnsupportedOperationException> { tool.run(context, emptyMap()) }
  }

  @Test
  fun processLlmRequest_addsUrlContext() = runTest {
    val tool = UrlContextTool()
    val context = testToolContext()
    var request = LlmRequest(model = DummyModel("gemini-2.0-flash"))

    request = tool.processLlmRequest(context, request)

    val tools = request.config.tools
    assertNotNull(tools)
    assertEquals(1, tools.size)
    assertNotNull(tools[0].urlContext)
  }

  @Test
  fun processLlmRequest_noModel_addsUrlContext() = runTest {
    // The model name is not checked, so the tool is added even without a model.
    val tool = UrlContextTool()
    val context = testToolContext()
    var request = LlmRequest()

    request = tool.processLlmRequest(context, request)

    val tools = request.config.tools
    assertNotNull(tools)
    assertEquals(1, tools.size)
    assertNotNull(tools[0].urlContext)
  }

  @Test
  fun processLlmRequest_nonGeminiModel_addsUrlContext() = runTest {
    // The model name is not checked, so the tool is added for any model.
    val tool = UrlContextTool()
    val context = testToolContext()
    var request = LlmRequest(model = DummyModel("gpt-4"))

    request = tool.processLlmRequest(context, request)

    val tools = request.config.tools
    assertNotNull(tools)
    assertEquals(1, tools.size)
    assertNotNull(tools[0].urlContext)
  }

  @Test
  fun processLlmRequest_withExistingTool_appendsUrlContext() = runTest {
    val tool = UrlContextTool()
    val context = testToolContext()
    val existingTool = Tool(googleSearch = GoogleSearch())
    var request =
      LlmRequest(
        model = DummyModel("gemini-2.0-flash"),
        config = GenerateContentConfig(tools = listOf(existingTool)),
      )

    request = tool.processLlmRequest(context, request)

    val tools = request.config.tools
    assertNotNull(tools)
    assertEquals(2, tools.size)
    assertNotNull(tools.find { it.googleSearch != null })
    assertNotNull(tools.find { it.urlContext != null })
  }

  @Test
  fun processLlmRequest_preservesOtherConfig() = runTest {
    val tool = UrlContextTool()
    val context = testToolContext()
    var request =
      LlmRequest(
        model = DummyModel("gemini-2.0-flash"),
        config = GenerateContentConfig(temperature = 0.5f),
      )

    request = tool.processLlmRequest(context, request)

    assertEquals(0.5f, request.config.temperature)
    val tools = request.config.tools
    assertNotNull(tools)
    assertEquals(1, tools.size)
    assertNotNull(tools[0].urlContext)
  }
}
