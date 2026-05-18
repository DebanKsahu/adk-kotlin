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

import com.google.adk.kt.telemetry.Span
import com.google.adk.kt.telemetry.SpanBuilder
import com.google.adk.kt.telemetry.TelemetryContext
import com.google.errorprone.annotations.CanIgnoreReturnValue
import io.opentelemetry.api.trace.SpanBuilder as OpenTelemetrySpanBuilder

/** OpenTelemetry implementation of [SpanBuilder]. */
internal class OtelSpanBuilder(private val builder: OpenTelemetrySpanBuilder) : SpanBuilder {

  @CanIgnoreReturnValue
  override fun set(key: String, value: String): SpanBuilder {
    builder.setAttribute(key, value)
    return this
  }

  @CanIgnoreReturnValue
  override fun set(key: String, value: Long): SpanBuilder {
    builder.setAttribute(key, value)
    return this
  }

  @CanIgnoreReturnValue
  override fun set(key: String, value: Double): SpanBuilder {
    builder.setAttribute(key, value)
    return this
  }

  @CanIgnoreReturnValue
  override fun set(key: String, value: Boolean): SpanBuilder {
    builder.setAttribute(key, value)
    return this
  }

  @CanIgnoreReturnValue
  override fun setParent(context: TelemetryContext): SpanBuilder {
    if (context is OtelTelemetryContext) {
      builder.setParent(context.otelContext)
    }
    // If the context is unrecognized, we intentionally do not call `setNoParent()`.
    // This allows OpenTelemetry to fall back to its native default behavior,
    // which is to inherit the ambient threaded Context.current().
    return this
  }

  override fun startSpan(): Span {
    return OtelSpan(builder.startSpan())
  }
}
