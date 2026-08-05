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

import com.google.adk.kt.types.FunctionDeclaration
import com.google.adk.kt.types.Schema
import com.google.adk.kt.types.Type
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FunctionToolExtensionsTest {

  class DummyFunctionTool(name: String, description: String, private val schema: Schema?) :
    FunctionTool(name, description) {
    override suspend fun execute(context: ToolContext, args: Map<String, Any>): Any {
      return mapOf("result" to "Success")
    }

    override fun declaration(): FunctionDeclaration {
      return FunctionDeclaration(name, description, schema)
    }
  }

  @Test
  fun toPromptDescription_xmlFormat_generatesCorrectXml() {
    val tools =
      listOf(
        DummyFunctionTool(
          name = "get_weather",
          description = "Gets the weather for a location",
          schema =
            Schema(
              type = Type.OBJECT,
              properties =
                mapOf(
                  "location" to
                    Schema(
                      type = Type.STRING,
                      description = "The city and state, e.g. San Francisco, CA",
                    )
                ),
              required = listOf("location"),
            ),
        )
      )

    val result = tools.toPromptDescription(PromptFormat.XML)
    assertTrue(result.contains("<tools>"))
    assertTrue(result.contains("<name>get_weather</name>"))
    assertTrue(result.contains("<description>Gets the weather for a location</description>"))
    assertTrue(result.contains("<name>location</name>"))
    assertTrue(result.contains("<type>string</type>"))
  }

  @Test
  fun toPromptDescription_jsonFormat_generatesCorrectJson() {
    val tools =
      listOf(
        DummyFunctionTool(
          name = "get_weather",
          description = "Gets the weather for a location",
          schema =
            Schema(
              type = Type.OBJECT,
              properties =
                mapOf(
                  "location" to
                    Schema(
                      type = Type.STRING,
                      description = "The city and state, e.g. San Francisco, CA",
                    )
                ),
              required = listOf("location"),
            ),
        )
      )

    // The whole document is pinned, not a few substrings: this renderer's contract is the exact
    // bytes it produces, so key order and the set of keys have to be part of the assertion.
    assertEquals(
      """[{"name":"get_weather","description":"Gets the weather for a location",""" +
        """"parameters":{"type":"object","properties":{"location":{"type":"string",""" +
        """"description":"The city and state, e.g. San Francisco, CA"}},"required":["location"]}}]""",
      tools.toPromptDescription(PromptFormat.JSON),
    )
  }

  @Test
  fun toPromptDescription_jsonFormat_arrayPropertyCarriesItsItems() {
    val tools =
      listOf(
        DummyFunctionTool(
          name = "add_labels",
          description = "Adds labels",
          schema =
            Schema(
              type = Type.OBJECT,
              properties =
                mapOf("labels" to Schema(type = Type.ARRAY, items = Schema(type = Type.STRING))),
            ),
        )
      )

    assertEquals(
      """[{"name":"add_labels","description":"Adds labels","parameters":{"type":"object",""" +
        """"properties":{"labels":{"type":"array","items":{"type":"string"}}}}}]""",
      tools.toPromptDescription(PromptFormat.JSON),
    )
  }

  @Test
  fun toPromptDescription_jsonFormat_arrayWithoutItemsAndObjectWithoutProperties_writeOnlyTheType() {
    val tools =
      listOf(
        DummyFunctionTool(
          name = "describe",
          description = "Describes a thing",
          schema =
            Schema(
              type = Type.OBJECT,
              properties =
                mapOf("tags" to Schema(type = Type.ARRAY), "extras" to Schema(type = Type.OBJECT)),
            ),
        )
      )

    // Neither sub-schema says what it contains, so neither gets an `items` or a `properties` key.
    // The drop is silent, so it is pinned here rather than left to be noticed in a prompt.
    assertEquals(
      """[{"name":"describe","description":"Describes a thing","parameters":{"type":"object",""" +
        """"properties":{"tags":{"type":"array"},"extras":{"type":"object"}}}}]""",
      tools.toPromptDescription(PromptFormat.JSON),
    )
  }

  @Test
  fun toPromptDescription_jsonFormat_lineSeparatorIsWrittenLiterally() {
    val tools =
      listOf(DummyFunctionTool(name = "a\u2028b", description = "line\u2029break", schema = null))

    // The one place the output is not what gson produced. gson escaped U+2028 and U+2029 so that
    // its output could be handed to JavaScript's `eval`; kotlinx writes them as themselves. Both
    // are valid JSON, and this string goes into a prompt rather than into a script.
    assertEquals(
      "[{\"name\":\"a\u2028b\",\"description\":\"line\u2029break\"}]",
      tools.toPromptDescription(PromptFormat.JSON),
    )
  }
}
