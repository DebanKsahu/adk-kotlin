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

import com.google.adk.kt.telemetry.noop.NoOpTracer
import com.google.adk.kt.telemetry.otel.OtelTracer
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TelemetryTest {

  @After
  fun tearDown() {
    Telemetry.resetTracer()
  }

  @Test
  fun tracer_byDefault_isOtelTracer() {
    assertThat(Telemetry.tracer).isInstanceOf(OtelTracer::class.java)
  }

  @Test
  fun setTracerForTest_withCustomTracer_overridesTracer() {
    val customTracer =
      object : Tracer {
        override fun spanBuilder(spanName: String): SpanBuilder = throw NotImplementedError()

        override suspend fun currentContext(): TelemetryContext = throw NotImplementedError()

        override fun contextWithSpan(span: Span): TelemetryContext = throw NotImplementedError()
      }

    Telemetry.setTracerForTest(customTracer)

    assertThat(Telemetry.tracer).isSameInstanceAs(customTracer)
  }

  @Test
  fun setTracerForTest_whenSetInOneThread_doesNotAffectOtherThreads() {
    val customTracer =
      object : Tracer {
        override fun spanBuilder(spanName: String): SpanBuilder = throw NotImplementedError()

        override suspend fun currentContext(): TelemetryContext = throw NotImplementedError()

        override fun contextWithSpan(span: Span): TelemetryContext = throw NotImplementedError()
      }

    Telemetry.setTracerForTest(customTracer)
    assertThat(Telemetry.tracer).isSameInstanceAs(customTracer)

    var tracerInOtherThread: Tracer? = null
    val thread = Thread { tracerInOtherThread = Telemetry.tracer }
    thread.start()
    thread.join()

    // The other thread should see the default OtelTracer, not the custom one set in the current
    // thread.
    assertThat(tracerInOtherThread).isInstanceOf(OtelTracer::class.java)
  }

  @Test
  fun noOpTracer_whenMethodsCalled_doesNotCrash() =
    kotlinx.coroutines.runBlocking {
      val tracer = NoOpTracer
      assertThat(tracer).isInstanceOf(NoOpTracer::class.java)

      // Verify we can call all methods without crashing
      val span =
        tracer
          .spanBuilder("test-span")
          .set("key", "value")
          .set("key", 1L)
          .set("key", 2.0)
          .set("key", true)
          .startSpan()

      span.addEvent("event")
      span.recordException(RuntimeException("test"))

      // Set parent
      tracer.spanBuilder("child").setParent(tracer.currentContext()).startSpan().end()

      span.end()
    }
}
