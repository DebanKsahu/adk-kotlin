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

/** Callback invoked when a model encounters an error. */
interface OnModelErrorCallback : Callback {
  /**
   * Callback executed when a model call encounters an error.
   *
   * Provides an opportunity to handle model errors gracefully, potentially providing alternative
   * responses or recovery mechanisms.
   *
   * @param context The context of the current agent call.
   * @param request The request that was sent to the model when the error occurred.
   * @param error The exception that was raised during model execution.
   * @return A [CallbackChoice] where returning [CallbackChoice.Break] using a fallback
   *   [LlmResponse] intercepts the error and resolves execution utilizing that fallback response.
   *   Returning [CallbackChoice.Continue] with [Unit] permits the original error to be propagated.
   */
  suspend fun call(
    context: CallbackContext,
    request: LlmRequest,
    error: Throwable,
  ): CallbackChoice<Unit, LlmResponse>

  companion object {
    // Workaround for problems when automatic SAM conversion code generated with gradle kotlin
    // plugin introduces an invalid method name in class files for functional interfaces with
    // abstract suspend methods.
    // This manual override permits the class to be used as if it were a functional interface with
    // SAM conversion (eg. OnModelErrorCallback { context, request, error -> ... }).

    operator fun invoke(
      block:
        suspend (context: CallbackContext, request: LlmRequest, error: Throwable) -> CallbackChoice<
            Unit,
            LlmResponse,
          >
    ): OnModelErrorCallback =
      object : OnModelErrorCallback {
        override suspend fun call(context: CallbackContext, request: LlmRequest, error: Throwable) =
          block(context, request, error)
      }
  }
}
