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

package com.google.adk.kt.types

import kotlinx.serialization.Serializable

/** A grounding chunk sourced from a retrieval (RAG) context. */
@Serializable
data class GroundingChunkRetrievedContext(
  /** The URI of the source. */
  val uri: String? = null,
  /** The title of the source. */
  val title: String? = null,
  /** The retrieved text content of the source. */
  val text: String? = null,
)
