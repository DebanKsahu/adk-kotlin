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

import com.google.common.truth.Truth.assertThat
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Span as OpenTelemetrySpan
import io.opentelemetry.context.Context
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class OtelTelemetryContextTest {

  @Test
  fun attach_setsOpenTelemetryContext() {
    val tracer = GlobalOpenTelemetry.getTracer("test")
    val otelSpan = tracer.spanBuilder("testSpan").startSpan()

    val otelContext = Context.current().with(otelSpan)
    val telemetryContext = OtelTelemetryContext(otelContext)

    val scope = telemetryContext.attach()
    try {
      assertThat(OpenTelemetrySpan.current()).isSameInstanceAs(otelSpan)
    } finally {
      telemetryContext.detach(scope)
    }
  }

  @Test
  fun asContextElement_returnsOtelTelemetryContextElement() {
    val telemetryContext = OtelTelemetryContext(Context.current())
    val element = telemetryContext.asContextElement()

    assertThat(element).isInstanceOf(OtelTelemetryContextElement::class.java)
  }
}
