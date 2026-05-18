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

import kotlin.coroutines.CoroutineContext

/**
 * A [CoroutineContext.Element] that carries a [TelemetryContext] across coroutine suspension
 * points.
 */
interface TelemetryContextElement : CoroutineContext.Element {
  /** The underlying telemetry context instance. */
  val context: TelemetryContext

  /** The default key for telemetry context elements. */
  companion object Key : CoroutineContext.Key<TelemetryContextElement>
}
