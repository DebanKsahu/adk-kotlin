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
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class OtelTracerTest {

  @Test
  fun currentContext_fetchesOpenTelemetryContext() = runBlocking {
    val otelTracer = OtelTracer(GlobalOpenTelemetry.getTracer("test"))

    val dummyOtelSpan = GlobalOpenTelemetry.getTracer("test2").spanBuilder("dummy").startSpan()
    val dummyContext = Context.current().with(dummyOtelSpan)

    dummyContext.makeCurrent().use {
      val telemetryContext = otelTracer.currentContext()

      assertThat(telemetryContext).isInstanceOf(OtelTelemetryContext::class.java)
      val otelTelemetryContext = telemetryContext as OtelTelemetryContext

      assertThat(otelTelemetryContext.otelContext).isEqualTo(dummyContext)
    }
  }

  @Test
  fun contextWithSpan_wrapsCorrectly() {
    val otelTracer = OtelTracer(GlobalOpenTelemetry.getTracer("test"))
    val standardSpan = otelTracer.spanBuilder("test-span").startSpan()

    val context = otelTracer.contextWithSpan(standardSpan)
    assertThat(context).isInstanceOf(OtelTelemetryContext::class.java)

    val scope = context.attach()
    try {
      val otelSpan = (standardSpan as OtelSpan).otelSpan
      assertThat(OpenTelemetrySpan.current()).isEqualTo(otelSpan)
    } finally {
      context.detach(scope)
    }
  }
}
