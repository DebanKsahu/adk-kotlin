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

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.serializer

internal object AnySerializations {
  fun encodeAnyToJsonElement(o: Any?): JsonElement {
    return when (o) {
      null -> JsonNull
      is String -> Json.encodeToJsonElement(o)
      is Int -> Json.encodeToJsonElement(o)
      is Boolean -> Json.encodeToJsonElement(o)
      is Double -> Json.encodeToJsonElement(o)
      is Float -> Json.encodeToJsonElement(o)
      is Long -> Json.encodeToJsonElement(o)
      is Short -> Json.encodeToJsonElement(o)
      is Byte -> Json.encodeToJsonElement(o)
      is Char -> Json.encodeToJsonElement(o)
      is Map<*, *> ->
        buildJsonObject {
          for ((key, value) in o) {
            put(key.toString(), encodeAnyToJsonElement(value))
          }
        }

      is List<*> -> {
        buildJsonArray {
          for (value in o) {
            add(encodeAnyToJsonElement(value))
          }
        }
      }

      else -> Json.encodeToJsonElement(serializer(o::class.java), o)
    }
  }

  fun decodeJsonElementToAny(json: JsonElement): Any? {
    return when (json) {
      is JsonNull -> null
      is JsonPrimitive ->
        if (json.isString) json.content
        else {
          when (json.content) {
            "true" -> true
            "false" -> false
            else ->
              try {
                Json.decodeFromJsonElement<Int>(json)
              } catch (_: SerializationException) {
                Json.decodeFromJsonElement<Double>(json)
              }
          }
        }

      is JsonObject -> json.mapValues { decodeJsonElementToAny(it.value) }
      is JsonArray -> json.map { decodeJsonElementToAny(it) }
    }
  }
}
