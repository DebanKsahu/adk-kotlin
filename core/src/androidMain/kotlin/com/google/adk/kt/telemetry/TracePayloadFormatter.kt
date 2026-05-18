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

package com.google.adk.kt.telemetry

import org.json.JSONArray
import org.json.JSONObject

internal actual fun getTracePayloadFormatter(): TracePayloadFormatter = AndroidTracePayloadFormatter

/** Android actual implementation of trace payload formatting using org.json. */
private object AndroidTracePayloadFormatter : TracePayloadFormatter {
  override fun format(payload: Any?): String {
    return try {
      val wrapped = wrap(payload)
      when (wrapped) {
        JSONObject.NULL -> "null"
        is String -> JSONObject.quote(wrapped)
        else -> wrapped.toString()
      }
    } catch (e: Throwable) {
      "{\"error\": \"serialization failed\"}"
    }
  }

  private fun wrap(obj: Any?): Any? {
    return when (obj) {
      // wrap(null) is needed for recursive calls on null values in maps/lists.
      null -> JSONObject.NULL
      is String,
      is Number,
      is Boolean -> obj
      is Map<*, *> -> {
        val json = JSONObject()
        for ((key, value) in obj) {
          json.put(key.toString(), wrap(value))
        }
        json
      }
      is Collection<*> -> {
        val json = JSONArray()
        for (value in obj) {
          json.put(wrap(value))
        }
        json
      }
      is Array<*> -> {
        val json = JSONArray()
        for (value in obj) {
          json.put(wrap(value))
        }
        json
      }
      is BooleanArray -> JSONArray().apply { obj.forEach { put(it) } }
      is ByteArray -> JSONArray().apply { obj.forEach { put(it.toInt()) } }
      is CharArray -> JSONArray().apply { obj.forEach { put(it.toString()) } }
      is ShortArray -> JSONArray().apply { obj.forEach { put(it.toInt()) } }
      is IntArray -> JSONArray().apply { obj.forEach { put(it) } }
      is LongArray -> JSONArray().apply { obj.forEach { put(it) } }
      is FloatArray -> JSONArray().apply { obj.forEach { put(it.toDouble()) } }
      is DoubleArray -> JSONArray().apply { obj.forEach { put(it) } }
      else -> JSONObject().put("value", obj.toString())
    }
  }
}
