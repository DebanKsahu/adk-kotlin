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

package com.google.adk.kt.memory.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Wire model for `IngestEventsRequest` (the `memories:ingestEvents` method). `parent` is bound to
 * the URL path, so only these fields travel in the body. [generationTriggerConfig] is passed
 * through as opaque JSON (a `MemoryGenerationTriggerConfig`).
 */
@Serializable
internal data class IngestEventsRequestDto(
  val scope: Map<String, String>,
  val directContentsSource: IngestionDirectContentsSourceDto? = null,
  val streamId: String? = null,
  val forceFlush: Boolean? = null,
  val generationTriggerConfig: JsonElement? = null,
)

/** Source content (chat history) to ingest into a stream. */
@Serializable
internal data class IngestionDirectContentsSourceDto(val events: List<IngestionEventDto>)

/**
 * A single event to ingest. [content] is the genai `Content` JSON (role + parts), pre-encoded by
 * the service. [eventId] de-duplicates repeated ingests; [eventTime] (RFC3339) orders events.
 */
@Serializable
internal data class IngestionEventDto(
  val content: JsonElement,
  val eventId: String? = null,
  val eventTime: String? = null,
)
