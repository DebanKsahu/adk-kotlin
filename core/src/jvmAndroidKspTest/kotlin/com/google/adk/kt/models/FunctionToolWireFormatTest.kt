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

package com.google.adk.kt.models

import com.google.adk.kt.agents.BaseAgent
import com.google.adk.kt.agents.InvocationContext
import com.google.adk.kt.events.Event
import com.google.adk.kt.sessions.Session
import com.google.adk.kt.sessions.SessionKey
import com.google.adk.kt.tools.FunctionTool
import com.google.adk.kt.tools.ToolContext
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.FunctionResponse
import com.google.adk.kt.types.Part
import com.google.adk.kt.types.toGenaiSdk
import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonParser
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest

/**
 * Verifies the JSON wire format of the `functionResponse` Gemini receives for a [FunctionTool]'s
 * return value, using real `@Tool` functions whose `FunctionTool` subclasses are KSP-generated.
 * Each runs `execute`, wraps the result, converts via [Content.toGenaiSdk], and asserts the
 * payload.
 *
 * Asserts via the Java SDK's `toJson()` for now; the SDK-swap CL switches to the Kotlin SDK.
 */
class FunctionToolWireFormatTest {

  @Test
  fun toolReturningInt_emitsJsonNumberInResponseMap() = runTest {
    assertWireResponse(ReturnsIntTool(), expected = """{"result":42}""")
  }

  @Test
  fun toolReturningString_emitsJsonStringInResponseMap() = runTest {
    assertWireResponse(ReturnsStringTool(), expected = """{"result":"hello"}""")
  }

  @Test
  fun toolReturningBoolean_emitsJsonBooleanInResponseMap() = runTest {
    assertWireResponse(ReturnsBooleanTool(), expected = """{"result":true}""")
  }

  @Test
  fun toolReturningDouble_emitsJsonNumberInResponseMap() = runTest {
    assertWireResponse(ReturnsDoubleTool(), expected = """{"result":3.14}""")
  }

  @Test
  fun toolReturningListOfInts_emitsJsonArrayOfNumbersInResponseMap() = runTest {
    assertWireResponse(ReturnsListOfIntsTool(), expected = """{"result":[1,2,3]}""")
  }

  @Test
  fun toolReturningMapOfStringToInt_emitsNestedJsonObjectOfNumbers() = runTest {
    assertWireResponse(
      ReturnsScoreboardTool(),
      expected = """{"result":{"alice":100,"bob":87,"carol":42}}""",
    )
  }

  @Test
  fun toolReturningUnit_emitsEmptyResponseMap() = runTest {
    // The generated tool returns the `Unit` singleton; for non-long-running tools the framework
    // coerces it to an empty function-response payload.
    assertWireResponse(ReturnsUnitTool(), expected = """{}""")
  }

  @Test
  fun toolCalledWithMissingRequiredArg_emitsErrorMap() = runTest {
    // The generated tool validates required parameters; when the LLM omits `name` it returns an
    // `error` payload directly (no `result` wrapper).
    assertWireResponse(
      RequiresNameTool(),
      args = emptyMap(),
      expected = """{"error":"Missing required parameter name"}""",
    )
  }

  @Test
  fun toolCalledWithRequiredArg_emitsResultInResponseMap() = runTest {
    // Supplying the required `name` exercises the tool body and yields a normal `result` payload.
    assertWireResponse(
      RequiresNameTool(),
      args = mapOf("name" to "world"),
      expected = """{"result":"hello, world"}""",
    )
  }

  @Test
  fun toolReturningMapStringAny_mixedValueTypes_emitsJsonObjectPreservingNativeTypes() = runTest {
    assertWireResponse(
      ReturnsStockPriceTool(),
      expected = """{"result":{"symbol":"GOOG","price":123.45,"volume":1000}}""",
    )
  }

  @Test
  fun toolReturningListAny_mixedElementTypes_emitsJsonArrayPreservingNativeTypes() = runTest {
    assertWireResponse(ReturnsStockHistoryTool(), expected = """{"result":["GOOG",123.45,1000]}""")
  }

  @Test
  fun toolReturningDeeplyNestedMapStringAny_mapListMap_emitsNestedJson() = runTest {
    assertWireResponse(
      ReturnsNestedDataTool(),
      expected =
        """
        {
          "result": {
            "outer": {
              "middle": [
                {"leaf": 1, "label": "first"},
                {"leaf": 2, "label": "second"}
              ],
              "scalar": "value"
            }
          }
        }
        """
          .trimIndent(),
    )
  }

  @Test
  fun toolReturningNestedMapWithNulls_emitsRecursivelyFlattenedJsonWithNullsStripped() = runTest {
    // `null` entries (here: `label`) are dropped by the converter, mirroring the SDK's
    // `explicitNulls = false` wire behavior, so Gemini never sees a `label: null` entry.
    assertWireResponse(
      ReturnsKitchenSinkTool(),
      expected =
        """
        {
          "result": {
            "name": "the answer",
            "value": 42,
            "ratio": 0.5,
            "active": true,
            "status": "READY",
            "tags": ["alpha", "beta"],
            "items": [
              {"id": 1, "label": "first"},
              {"id": 2, "label": "second"}
            ],
            "scores": {"alice": 100, "bob": 87}
          }
        }
        """
          .trimIndent(),
    )
  }

  // -- Helpers -----------------------------------------------------------------------------------

  /**
   * Runs [tool] with [args], converts the resulting function response through the real ADK -> GenAI
   * SDK converter, and asserts the converted `functionResponse` (name, id, and the serialized
   * `response` object) matches [expected].
   */
  private suspend fun assertWireResponse(
    tool: FunctionTool,
    args: Map<String, Any> = emptyMap(),
    expected: String,
  ) {
    @Suppress("UNCHECKED_CAST")
    val responseMap: Map<String, Any?> =
      (tool.execute(dummyToolContext(), args) as? Map<String, Any?>) ?: emptyMap()

    val genaiContent =
      Content(
          role = "user",
          parts =
            listOf(
              Part(
                functionResponse =
                  FunctionResponse(
                    name = tool.name,
                    response = responseMap,
                    id = "${tool.name}-call",
                  )
              )
            ),
        )
        .toGenaiSdk()

    // This module still runs on the Java GenAI SDK, so assert via the SDK's own JSON serialization.
    val functionResponse =
      JsonParser.parseString(genaiContent.toJson())
        .asJsonObject
        .getAsJsonArray("parts")
        .get(0)
        .asJsonObject
        .getAsJsonObject("functionResponse")

    assertThat(functionResponse.get("name").asString).isEqualTo(tool.name)
    assertThat(functionResponse.get("id").asString).isEqualTo("${tool.name}-call")
    assertThat(JsonParser.parseString(functionResponse.getAsJsonObject("response").toString()))
      .isEqualTo(JsonParser.parseString(expected))
  }

  private fun dummyToolContext(): ToolContext =
    ToolContext(
      invocationContext =
        InvocationContext(
          session = Session(key = SessionKey("app", "user", "session")),
          runConfig = null,
          agent = NoOpAgent(),
        )
    )

  private class NoOpAgent : BaseAgent(name = "wire-format-test-agent") {
    override fun runAsyncImpl(context: InvocationContext): Flow<Event> = flow {}
  }
}
