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

package com.google.adk.kt.serialization

import com.google.common.truth.Truth.assertThat
import com.google.genai.JsonSerializable
import com.google.genai.types.Content
import com.google.genai.types.Part
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class JsonTest {

  @Before fun setUp() {}

  class TestJsonSerializable(
    val name: String,
    val number: Int,
    val props: Map<String, Any>,
    val items: List<String>,
  ) : JsonSerializable() {}

  @Test
  fun toJsonString_withJsonSerializable() {
    val obj =
      TestJsonSerializable(
        "test",
        123,
        mapOf("a" to 1, "b" to "two", "c" to 3.0),
        listOf("a", "b", "c"),
      )
    val json = Json.toJsonString(obj)
    val abstractJson = Gson().fromJson(json, Map::class.java)
    assertThat(abstractJson["name"]).isEqualTo("test")
    assertThat(abstractJson["number"]).isEqualTo(123)
    assertThat(abstractJson["props"]).isEqualTo(mapOf("a" to 1.0, "b" to "two", "c" to 3.0))
    assertThat(abstractJson["items"]).isEqualTo(listOf("a", "b", "c"))
  }

  @Test
  fun toJsonString_withContent() {
    val content =
      Content.builder().role("USER").parts(listOf(Part.builder().text("hello").build())).build()
    val json = Json.toJsonString(content)
    val abstractJson = Gson().fromJson(json, Map::class.java)
    assertThat(abstractJson["role"]).isEqualTo("USER")
    assertThat(abstractJson["parts"]).isEqualTo(listOf(mapOf("text" to "hello")))
  }

  @Test
  fun toJsonString_withNullJsonSerializable() {
    data class TestJsonSerializable(val jsonSerializable: JsonSerializable?)
    val obj = TestJsonSerializable(null)
    val json = Json.toJsonString(obj)
    val abstractJson = Gson().fromJson(json, Map::class.java)
    assertThat(abstractJson["jsonSerializable"]).isNull()
  }
}
