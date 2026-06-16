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
package com.google.adk.kt.telemetry.otel

import com.google.adk.kt.telemetry.Scope
import com.google.adk.kt.telemetry.TelemetryContextElement
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ThreadContextElement

/**
 * OpenTelemetry implementation of [TelemetryContextElement] that acts as a Coroutine
 * ThreadContextElement to propagate the [OtelTelemetryContext] across threads.
 */
internal class OtelTelemetryContextElement(override val context: OtelTelemetryContext) :
  TelemetryContextElement, ThreadContextElement<Scope> {

  override val key: CoroutineContext.Key<TelemetryContextElement>
    get() = TelemetryContextElement.Key

  override fun updateThreadContext(context: CoroutineContext): Scope {
    return this.context.attach()
  }

  override fun restoreThreadContext(context: CoroutineContext, oldState: Scope) {
    this.context.detach(oldState)
  }
}
