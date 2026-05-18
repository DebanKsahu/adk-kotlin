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

import kotlinx.coroutines.flow.Flow

/** Interface that provides a common interface for interacting with different LLMs. */
interface Model {
  /** The name of the model. */
  val name: String

  /**
   * Generates content for the given [LlmRequest]. This returns a [Flow] of [LlmResponse]s.
   *
   * @param request The request containing prompt and config.
   * @param stream Whether to enable streaming mode. If true, partial responses will be emitted.
   */
  fun generateContent(request: LlmRequest, stream: Boolean = false): Flow<LlmResponse>
}

private val PATH_PATTERNS =
  listOf(
    Regex("^projects/[^/]+/locations/[^/]+/publishers/[^/]+/models/(.+)$"),
    Regex("^apigee/(?:[^/]+/)?(?:[^/]+/)?(.+)$"),
  )

/**
 * The short name of the model, extracted from the full model name if it's in a path-based format.
 */
internal val Model.shortName: String
  get() =
    PATH_PATTERNS.firstNotNullOfOrNull { it.matchEntire(name)?.groupValues?.get(1) }
      ?: name.removePrefix("models/")

/** Whether the model is a Gemini 2.0 or newer model. */
internal val Model.isGemini2OrAbove: Boolean
  get() =
    shortName.startsWith("gemini-") &&
      (shortName.removePrefix("gemini-").substringBefore("-").substringBefore(".").toIntOrNull()
        ?: 0) >= 2
