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
import com.google.adk.kt.telemetry.TelemetryContext
import com.google.adk.kt.telemetry.TelemetryContextElement
import io.opentelemetry.context.Context
import io.opentelemetry.context.Scope as OpenTelemetryScope

/** OpenTelemetry implementation of [TelemetryContext]. */
internal class OtelTelemetryContext(val otelContext: Context) : TelemetryContext {

  override fun asContextElement(): TelemetryContextElement {
    return OtelTelemetryContextElement(this)
  }

  @Suppress("MustBeClosedChecker", "MustBeClosed")
  override fun attach(): Scope {
    val otelScope = otelContext.makeCurrent()
    return OtelScope(otelScope)
  }

  override fun detach(scopeToken: Scope) {
    scopeToken.close()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is OtelTelemetryContext) return false

    return otelContext == other.otelContext
  }

  override fun hashCode(): Int {
    return otelContext.hashCode()
  }
}

/** OpenTelemetry implementation of [Scope]. */
internal class OtelScope(private val otelScope: OpenTelemetryScope) : Scope {
  override fun close() {
    otelScope.close()
  }
}
