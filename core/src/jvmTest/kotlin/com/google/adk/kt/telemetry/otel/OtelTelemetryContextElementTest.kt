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
class OtelTelemetryContextElementTest {

  @Test
  fun updateAndRestoreThreadContext_managesOtelContext() {
    val dummyOtelSpan = GlobalOpenTelemetry.getTracer("test").spanBuilder("dummy").startSpan()
    val dummyContext = Context.current().with(dummyOtelSpan)

    val telemetryContext = OtelTelemetryContext(dummyContext)
    val contextElement = OtelTelemetryContextElement(telemetryContext)

    // Verify initial state
    val initialSpan = OpenTelemetrySpan.current()

    // Simulate Coroutine mounting the context onto the thread
    val scope = contextElement.updateThreadContext(kotlin.coroutines.EmptyCoroutineContext)

    // Within the coroutine execution boundary, the OpenTelemetry span should be the dummy one
    assertThat(OpenTelemetrySpan.current()).isEqualTo(dummyOtelSpan)

    // Simulate Coroutine dismounting
    contextElement.restoreThreadContext(kotlin.coroutines.EmptyCoroutineContext, scope)

    // The thread should revert back to original state
    assertThat(OpenTelemetrySpan.current()).isEqualTo(initialSpan)
  }
}
