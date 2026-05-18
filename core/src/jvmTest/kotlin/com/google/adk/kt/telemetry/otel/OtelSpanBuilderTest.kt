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
import io.opentelemetry.api.trace.SpanBuilder as OpenTelemetrySpanBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.RETURNS_SELF
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(JUnit4::class)
class OtelSpanBuilderTest {

  @Test
  fun set_string_delegatesToAttribute() {
    val mockBuilder = mock(OpenTelemetrySpanBuilder::class.java, RETURNS_SELF)
    val otelSpanBuilder = OtelSpanBuilder(mockBuilder)

    val result = otelSpanBuilder.set("key", "value")

    verify(mockBuilder).setAttribute("key", "value")
    assertThat(result).isSameInstanceAs(otelSpanBuilder)
  }

  @Test
  fun set_boolean_delegatesToAttribute() {
    val mockBuilder = mock(OpenTelemetrySpanBuilder::class.java, RETURNS_SELF)
    val otelSpanBuilder = OtelSpanBuilder(mockBuilder)

    otelSpanBuilder.set("key", true)

    verify(mockBuilder).setAttribute("key", true)
  }

  @Test
  fun set_long_delegatesToAttribute() {
    val mockBuilder = mock(OpenTelemetrySpanBuilder::class.java, RETURNS_SELF)
    val otelSpanBuilder = OtelSpanBuilder(mockBuilder)

    otelSpanBuilder.set("key", 10L)

    verify(mockBuilder).setAttribute("key", 10L)
  }

  @Test
  fun set_double_delegatesToAttribute() {
    val mockBuilder = mock(OpenTelemetrySpanBuilder::class.java, RETURNS_SELF)
    val otelSpanBuilder = OtelSpanBuilder(mockBuilder)

    otelSpanBuilder.set("key", 3.14)

    verify(mockBuilder).setAttribute("key", 3.14)
  }
}
