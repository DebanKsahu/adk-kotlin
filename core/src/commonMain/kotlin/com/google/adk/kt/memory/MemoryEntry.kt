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

package com.google.adk.kt.memory

import com.google.adk.kt.types.Content

/**
 * Represents one memory entry in the Vertex AI Memory Bank.
 *
 * @property content The main content of the memory.
 * @property id The unique identifier of the memory entry, or null if not yet persisted.
 * @property author The author of the memory, or null if not set.
 * @property timestamp The timestamp when the original content of this memory happened, or null if
 *   not set. Preferred format is ISO 8601 format.
 * @property customMetadata Optional key-value metadata associated with this memory.
 */
data class MemoryEntry(
  val content: Content,
  val id: String? = null,
  val author: String? = null,
  val timestamp: String? = null,
  val customMetadata: Map<String, Any> = emptyMap(),
)
