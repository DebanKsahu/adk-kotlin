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
import com.google.adk.kt.models.LlmResponse

/** Callback invoked immediately after a model call has completed. */
interface AfterModelCallback : Callback {
  /**
   * Callback executed after a response is received from the model.
   *
   * This is the ideal place to log model responses, collect metrics on token usage, or perform
   * post-processing on the raw [LlmResponse].
   *
   * @param context The context of the current agent call.
   * @param response The response object received from the model.
   * @return The potentially updated or replaced [LlmResponse] to propagate down the chain.
   */
  suspend fun call(context: CallbackContext, response: LlmResponse): LlmResponse

  companion object {
    // Workaround for problems when automatic SAM conversion code generated with gradle kotlin
    // plugin introduces an invalid method name in class files for functional interfaces with
    // abstract suspend methods.
    // This manual override permits the class to be used as if it were a functional interface with
    // SAM conversion (eg. AfterModelCallback { context, response -> ... }).

    operator fun invoke(
      block: suspend (context: CallbackContext, response: LlmResponse) -> LlmResponse
    ): AfterModelCallback =
      object : AfterModelCallback {
        override suspend fun call(context: CallbackContext, response: LlmResponse) =
          block(context, response)
      }
  }
}
