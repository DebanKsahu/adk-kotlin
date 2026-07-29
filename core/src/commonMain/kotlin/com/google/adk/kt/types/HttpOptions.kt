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

import kotlin.time.Duration

/**
 * ADK-owned HTTP transport options for calls a [com.google.adk.kt.models.Model] makes to its
 * backend.
 *
 * This is ADK's own type rather than the backend SDK's, so that configuration surfaces such as
 * [com.google.adk.kt.agents.ContextCacheConfig] stay independent of any particular backend.
 * Implementations translate it to whatever their transport expects.
 *
 * @property baseUrl Base URL of the service endpoint. `null` uses the backend's default.
 * @property apiVersion Version of the API to use. `null` uses the backend's default.
 * @property headers Additional HTTP headers to send with the request.
 * @property timeout Request timeout, e.g. `10.seconds`. `null` uses the backend's default.
 */
data class HttpOptions(
  val baseUrl: String? = null,
  val apiVersion: String? = null,
  val headers: Map<String, String>? = null,
  val timeout: Duration? = null,
)
