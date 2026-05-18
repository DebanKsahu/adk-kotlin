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

package com.google.adk.kt.callbacks

import com.google.adk.kt.agents.CallbackContext
import com.google.adk.kt.models.LlmRequest
import com.google.adk.kt.models.LlmResponse

/** Callback invoked immediately before a model (LLM) call is made. */
interface BeforeModelCallback : Callback {
  /**
   * Callback executed before a request is sent to the model.
   *
   * Provides an opportunity to inspect, log, or modify the [LlmRequest] object. It can also be used
   * to implement caching by returning a cached [LlmResponse], which skips the actual model call.
   *
   * @param context The context of the current agent call.
   * @param request The prepared request object to be sent to the model.
   * @return A [CallbackChoice] where returning [CallbackChoice.Break] with a custom [LlmResponse]
   *   triggers an early exit, returning the response immediately and bypassing the model call.
   *   Returning [CallbackChoice.Continue] with a potentially modified [LlmRequest] propagates the
   *   request to the model normally.
   */
  suspend fun call(
    context: CallbackContext,
    request: LlmRequest,
  ): CallbackChoice<LlmRequest, LlmResponse>

  companion object {
    // Workaround for problems when automatic SAM conversion code generated with gradle kotlin
    // plugin introduces an invalid method name in class files for functional interfaces with
    // abstract suspend methods.
    // This manual override permits the class to be used as if it were a functional interface with
    // SAM conversion (eg. BeforeModelCallback { context, request -> ... }).

    operator fun invoke(
      block:
        suspend (context: CallbackContext, request: LlmRequest) -> CallbackChoice<
            LlmRequest,
            LlmResponse,
          >
    ): BeforeModelCallback =
      object : BeforeModelCallback {
        override suspend fun call(context: CallbackContext, request: LlmRequest) =
          block(context, request)
      }
  }
}
