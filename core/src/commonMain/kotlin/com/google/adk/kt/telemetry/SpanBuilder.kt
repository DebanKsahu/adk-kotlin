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

import com.google.errorprone.annotations.CanIgnoreReturnValue

/** A builder for creating and starting a [Span]. */
interface SpanBuilder {
  /**
   * Sets a string attribute on the span.
   *
   * @param key the attribute key.
   * @param value the attribute value.
   */
  @CanIgnoreReturnValue operator fun set(key: String, value: String): SpanBuilder

  /**
   * Sets a long attribute on the span.
   *
   * @param key the attribute key.
   * @param value the attribute value.
   */
  @CanIgnoreReturnValue operator fun set(key: String, value: Long): SpanBuilder

  /**
   * Sets a double attribute on the span.
   *
   * @param key the attribute key.
   * @param value the attribute value.
   */
  @CanIgnoreReturnValue operator fun set(key: String, value: Double): SpanBuilder

  /**
   * Sets a boolean attribute on the span.
   *
   * @param key the attribute key.
   * @param value the attribute value.
   */
  @CanIgnoreReturnValue operator fun set(key: String, value: Boolean): SpanBuilder

  /**
   * Sets the parent context for this span.
   *
   * @param context the parent telemetry context.
   */
  @CanIgnoreReturnValue fun setParent(context: TelemetryContext): SpanBuilder

  /**
   * Starts and returns a new [Span].
   *
   * **WARNING:** Direct usage of this method is heavily discouraged because it requires the caller
   * to safely manage the span's lifecycle and context propagation manually. If misused, it can
   * easily lead to orphaned spans or memory leaks. Please rely on the structural scopes created by
   * `withSpan` and `inSpan` instead.
   */
  fun startSpan(): Span
}
