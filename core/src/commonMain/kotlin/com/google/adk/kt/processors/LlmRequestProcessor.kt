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

package com.google.adk.kt.processors

import com.google.adk.kt.agents.InvocationContext
import com.google.adk.kt.events.Event
import com.google.adk.kt.models.LlmRequest

/**
 * Defines a step in the pipeline that executes before an LLM request is sent.
 *
 * Implementations of this interface can modify the provided [LlmRequest] to add instructions,
 * contents, or tools, or configure model generation settings prior to execution. They can also emit
 * [Event]s to the caller flow, representing thought processes, intermediate actions, or history
 * rewrites.
 */
internal interface LlmRequestProcessor {
  /**
   * Executes the processor logic.
   *
   * @param context The [InvocationContext] providing current execution state, history, and agent
   *   info.
   * @param request The [LlmRequest] being built. Processors can modify this object by returning a
   *   new instance.
   * @param emitEvent A callback to emit [Event]s to the caller during this processing step.
   * @return The potentially modified [LlmRequest].
   */
  suspend fun process(
    context: InvocationContext,
    request: LlmRequest,
    emitEvent: suspend (Event) -> Unit = {},
  ): LlmRequest
}
