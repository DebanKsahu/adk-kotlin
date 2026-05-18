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

import com.google.gson.Gson

internal actual fun getTracePayloadFormatter(): TracePayloadFormatter = JvmTracePayloadFormatter

/** JVM actual implementation of trace payload formatting using Gson. */
private object JvmTracePayloadFormatter : TracePayloadFormatter {
  private val gson = Gson()

  override fun format(payload: Any?): String {
    return if (payload == null) {
      "null"
    } else {
      try {
        gson.toJson(payload)
      } catch (e: Throwable) {
        // Parity with Java ADK error handling
        "{\"error\": \"serialization failed\"}"
      }
    }
  }
}
