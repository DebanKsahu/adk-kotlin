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

package com.google.adk.firebase.utils

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AnySerializationsTest {

  @Serializable data class TestData(val id: Int, val name: String)

  @Serializable data class Tree(val l: Tree? = null, val r: Tree? = null)

  @Test
  fun encodeAnyToJsonElement_null() {
    val result = AnySerializations.encodeAnyToJsonElement(null)
    assertThat(result).isEqualTo(JsonNull)
  }

  @Test
  fun encodeAnyToJsonElement_primitives() {
    assertThat(AnySerializations.encodeAnyToJsonElement("test")).isEqualTo(JsonPrimitive("test"))
    assertThat(AnySerializations.encodeAnyToJsonElement(123)).isEqualTo(JsonPrimitive(123))
    assertThat(AnySerializations.encodeAnyToJsonElement(123L)).isEqualTo(JsonPrimitive(123L))
    assertThat(AnySerializations.encodeAnyToJsonElement(12.3)).isEqualTo(JsonPrimitive(12.3))
    assertThat(AnySerializations.encodeAnyToJsonElement(true)).isEqualTo(JsonPrimitive(true))
    assertThat(AnySerializations.encodeAnyToJsonElement("true")).isEqualTo(JsonPrimitive("true"))
    assertThat(AnySerializations.encodeAnyToJsonElement("false")).isEqualTo(JsonPrimitive("false"))
    assertThat(AnySerializations.encodeAnyToJsonElement("TrUe")).isEqualTo(JsonPrimitive("TrUe"))
    assertThat(AnySerializations.encodeAnyToJsonElement("FaLsE")).isEqualTo(JsonPrimitive("FaLsE"))
  }

  @Test
  fun encodeAnyToJsonElement_map() {
    val map =
      mapOf(
        "key1" to "value1",
        "key2" to 123,
        "key3" to null,
        "key4" to TestData(id = 234, name = "test"),
        "key5" to Tree(Tree(r = Tree()), Tree()),
      )
    val result = AnySerializations.encodeAnyToJsonElement(map)
    assertThat(result).isInstanceOf(JsonObject::class.java)
    val jsonObject = result as JsonObject
    assertThat(jsonObject["key1"]).isEqualTo(JsonPrimitive("value1"))
    assertThat(jsonObject["key2"]).isEqualTo(JsonPrimitive(123))
    assertThat(jsonObject["key3"]).isEqualTo(JsonNull)
    assertThat(jsonObject["key4"]).isInstanceOf(JsonObject::class.java)
    val testDataJson = jsonObject["key4"] as JsonObject
    assertThat(testDataJson["id"]).isEqualTo(JsonPrimitive(234))
    assertThat(testDataJson["name"]).isEqualTo(JsonPrimitive("test"))
    assertThat(jsonObject["key5"]).isInstanceOf(JsonObject::class.java)
    val treeJson = jsonObject["key5"] as JsonObject
    assertThat(treeJson["l"]).isInstanceOf(JsonObject::class.java)
    assertThat((treeJson["l"] as JsonObject)["l"]).isNull()
    assertThat((treeJson["l"] as JsonObject)["r"]).isInstanceOf(JsonObject::class.java)
    assertThat(((treeJson["l"] as JsonObject)["r"] as JsonObject)["l"]).isNull()
    assertThat(((treeJson["l"] as JsonObject)["r"] as JsonObject)["r"]).isNull()
    assertThat(treeJson["r"]).isInstanceOf(JsonObject::class.java)
    assertThat((treeJson["r"] as JsonObject)["l"]).isNull()
    assertThat((treeJson["r"] as JsonObject)["r"]).isNull()
  }

  @Test
  fun encodeAnyToJsonElement_list() {
    val list =
      listOf("value1", 123, null, TestData(id = 234, name = "test"), Tree(Tree(r = Tree()), Tree()))
    val result = AnySerializations.encodeAnyToJsonElement(list)
    assertThat(result).isInstanceOf(JsonArray::class.java)
    val jsonArray = result as JsonArray
    assertThat(jsonArray[0]).isEqualTo(JsonPrimitive("value1"))
    assertThat(jsonArray[1]).isEqualTo(JsonPrimitive(123))
    assertThat(jsonArray[2]).isEqualTo(JsonNull)
    assertThat(jsonArray[3]).isInstanceOf(JsonObject::class.java)
    val testDataJson = jsonArray[3] as JsonObject
    assertThat(testDataJson["id"]).isEqualTo(JsonPrimitive(234))
    assertThat(testDataJson["name"]).isEqualTo(JsonPrimitive("test"))
    assertThat(jsonArray[4]).isInstanceOf(JsonObject::class.java)
    val treeJson = jsonArray[4] as JsonObject
    assertThat(treeJson["l"]).isInstanceOf(JsonObject::class.java)
    assertThat((treeJson["l"] as JsonObject)["l"]).isNull()
    assertThat((treeJson["l"] as JsonObject)["r"]).isInstanceOf(JsonObject::class.java)
    assertThat(((treeJson["l"] as JsonObject)["r"] as JsonObject)["l"]).isNull()
    assertThat(((treeJson["l"] as JsonObject)["r"] as JsonObject)["r"]).isNull()
    assertThat(treeJson["r"]).isInstanceOf(JsonObject::class.java)
    assertThat((treeJson["r"] as JsonObject)["l"]).isNull()
    assertThat((treeJson["r"] as JsonObject)["r"]).isNull()
  }

  @Test
  fun encodeAnyToJsonElement_customObject() {
    val testData = TestData(1, "test")
    val result = AnySerializations.encodeAnyToJsonElement(testData)
    assertThat(result).isInstanceOf(JsonObject::class.java)
    val jsonObject = result as JsonObject
    assertThat(jsonObject["id"]).isEqualTo(JsonPrimitive(1))
    assertThat(jsonObject["name"]).isEqualTo(JsonPrimitive("test"))
  }

  @Test
  fun encodeAnyToJsonElement_treeObject() {
    val tree = Tree(Tree(r = Tree()), Tree())
    val result = AnySerializations.encodeAnyToJsonElement(tree)
    assertThat(result).isInstanceOf(JsonObject::class.java)
    val jsonObject = result as JsonObject
    assertThat(jsonObject["l"]).isInstanceOf(JsonObject::class.java)
    assertThat((jsonObject["l"] as JsonObject)["l"]).isNull()
    assertThat((jsonObject["l"] as JsonObject)["r"]).isInstanceOf(JsonObject::class.java)
    assertThat(((jsonObject["l"] as JsonObject)["r"] as JsonObject)["l"]).isNull()
    assertThat(((jsonObject["l"] as JsonObject)["r"] as JsonObject)["r"]).isNull()
    assertThat(jsonObject["r"]).isInstanceOf(JsonObject::class.java)
    assertThat((jsonObject["r"] as JsonObject)["l"]).isNull()
    assertThat((jsonObject["r"] as JsonObject)["r"]).isNull()
  }

  @Test
  fun decodeJsonElementToAny_null() {
    val result = AnySerializations.decodeJsonElementToAny(JsonNull)
    assertThat(result).isNull()
  }

  @Test
  fun decodeJsonElementToAny_primitives() {
    assertThat(AnySerializations.decodeJsonElementToAny(JsonPrimitive("test"))).isEqualTo("test")
    assertThat(AnySerializations.decodeJsonElementToAny(JsonPrimitive(123))).isEqualTo(123)
    assertThat(AnySerializations.decodeJsonElementToAny(JsonPrimitive(12.3))).isEqualTo(12.3)
    assertThat(AnySerializations.decodeJsonElementToAny(JsonPrimitive(true))).isEqualTo(true)
    assertThat(AnySerializations.decodeJsonElementToAny(JsonPrimitive("true"))).isEqualTo("true")
    assertThat(AnySerializations.decodeJsonElementToAny(JsonPrimitive("false"))).isEqualTo("false")
    assertThat(AnySerializations.decodeJsonElementToAny(JsonPrimitive("TrUe"))).isEqualTo("TrUe")
    assertThat(AnySerializations.decodeJsonElementToAny(JsonPrimitive("FaLsE"))).isEqualTo("FaLsE")
  }

  @Test
  fun decodeJsonElementToAny_object() {
    val jsonObject =
      JsonObject(
        mapOf(
          "key1" to JsonPrimitive("value1"),
          "key2" to JsonPrimitive(123),
          "innerList" to JsonArray(listOf(JsonPrimitive(456))),
          "innerObject" to JsonObject(mapOf("k" to JsonPrimitive("v"))),
        )
      )
    val result = AnySerializations.decodeJsonElementToAny(jsonObject)
    assertThat(result).isInstanceOf(Map::class.java)
    val map = result as Map<*, *>
    assertThat(map["key1"]).isEqualTo("value1")
    assertThat(map["key2"]).isEqualTo(123)
    assertThat(map["innerList"]).isInstanceOf(List::class.java)
    val innerList = map["innerList"] as List<*>
    assertThat(innerList.size).isEqualTo(1)
    assertThat(innerList[0]).isEqualTo(456)
    assertThat(map["innerObject"]).isInstanceOf(Map::class.java)
    val innerObject = map["innerObject"] as Map<*, *>
    assertThat(innerObject.size).isEqualTo(1)
    assertThat(innerObject["k"]).isEqualTo("v")
  }

  @Test
  fun decodeJsonElementToAny_array() {
    val jsonArray =
      JsonArray(
        listOf(
          JsonPrimitive("value1"),
          JsonPrimitive(123),
          JsonArray(listOf(JsonPrimitive(456))),
          JsonObject(mapOf("k" to JsonPrimitive("v"))),
        )
      )
    val result = AnySerializations.decodeJsonElementToAny(jsonArray)
    assertThat(result).isInstanceOf(List::class.java)
    val list = result as List<*>
    assertThat(list.size).isEqualTo(4)
    assertThat(list[0]).isEqualTo("value1")
    assertThat(list[1]).isEqualTo(123)
    assertThat(list[2]).isInstanceOf(List::class.java)
    val innerList = list[2] as List<*>
    assertThat(innerList.size).isEqualTo(1)
    assertThat(innerList[0]).isEqualTo(456)
    assertThat(list[3]).isInstanceOf(Map::class.java)
    val innerMap = list[3] as Map<*, *>
    assertThat(innerMap.size).isEqualTo(1)
    assertThat(innerMap["k"]).isEqualTo("v")
  }

  @Test
  fun roundTrip_complexStructure() {
    val original =
      mapOf(
        "string" to "test",
        "int" to 123,
        "double" to 12.3,
        "boolean" to true,
        "null" to null,
        "list" to listOf(1, 2, 3),
        "nestedMap" to mapOf("inner" to "value"),
        "trickyQuasiBoolean" to "true",
      )
    val encoded = AnySerializations.encodeAnyToJsonElement(original)
    val decoded = AnySerializations.decodeJsonElementToAny(encoded)
    assertThat(decoded).isEqualTo(original)
  }
}
