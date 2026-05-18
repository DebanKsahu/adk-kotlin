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

import com.google.adk.kt.agents.ReadonlyContext
import com.google.adk.kt.types.Part

/** A readonly view of the tool context. */
interface ReadonlyToolContext {
  /** The readonly invocation context. */
  val context: ReadonlyContext

  /** The unique ID of the function call. */
  val functionCallId: String?

  /** The unique ID of the event that triggered this tool call. */
  val eventId: String?

  /** Lists the artifacts available in the current session. */
  suspend fun listArtifacts(): List<String>

  /** Loads a specific artifact from the current session. */
  suspend fun loadArtifact(name: String, version: Int? = null): Part?
}
