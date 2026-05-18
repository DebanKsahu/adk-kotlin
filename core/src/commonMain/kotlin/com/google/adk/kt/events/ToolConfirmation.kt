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
package com.google.adk.kt.events

/** Represents a tool confirmation configuration. */
data class ToolConfirmation(
  /** Whether the tool execution is confirmed. */
  val confirmed: Boolean,
  /** The confirmation payload. */
  val payload: Any? = null,
  /** The hint for the confirmation. */
  val hint: String? = null,
) {
  companion object {
    /** Key for the 'confirmed' field in the serialized map. */
    const val CONFIRMED_KEY = "confirmed"

    /** Key for the 'payload' field in the serialized map. */
    const val PAYLOAD_KEY = "payload"

    /** Key for the 'hint' field in the serialized map. */
    const val HINT_KEY = "hint"
  }
}
