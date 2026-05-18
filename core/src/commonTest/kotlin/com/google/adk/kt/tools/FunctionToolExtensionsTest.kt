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

    val result = tools.toPromptDescription(PromptFormat.JSON)
    assertTrue(result.contains("\"name\":\"get_weather\""))
    assertTrue(result.contains("\"description\":\"Gets the weather for a location\""))
    assertTrue(result.contains("\"location\":{"))
    assertTrue(result.contains("\"type\":\"string\""))
  }
}
