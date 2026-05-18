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

/**
 * An abstraction for a telemetry context, capable of bridging into ThreadLocal storage via its
 * [attach] and [detach] methods for synchronous tracing, or being carried across coroutine
 * boundaries via [asContextElement].
 */
interface TelemetryContext {
  /** Converts this context into a CoroutineContext.Element for propagation in coroutines. */
  fun asContextElement(): TelemetryContextElement

  /**
   * Attaches this context to the current thread synchronously. Return the scope token needed to
   * detach it.
   */
  fun attach(): Scope

  /**
   * Detaches this context from the current thread using the token returned by [attach].
   *
   * @param scopeToken the token returned by [attach]
   */
  fun detach(scopeToken: Scope)
}
