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
import com.google.adk.kt.types.Content

/** Callback invoked after an agent runs. */
interface AfterAgentCallback : Callback {
  /**
   * Callback executed after an agent's primary logic has completed.
   *
   * Allows plugins/callbacks to inspect invocation state or override the agent's final response.
   *
   * @param context The context of the current agent call.
   * @return A [CallbackChoice] where returning [CallbackChoice.Break] with a custom [Content]
   *   overrides the agent's original response and appends it to the event history, mimicking Python
   *   ADK's behavior when a truthy content is returned. Returning [CallbackChoice.Continue] with
   *   [Unit] allows execution to proceed utilizing the original response naturally.
   */
  suspend fun call(context: CallbackContext): CallbackChoice<Unit, Content>

  companion object {
    // Workaround for problems when automatic SAM conversion code generated with gradle kotlin
    // plugin introduces an invalid method name in class files for functional interfaces with
    // abstract suspend methods.
    // This manual override permits the class to be used as if it were a functional interface with
    // SAM conversion (eg. AfterAgentCallback { context -> ... }).

    operator fun invoke(
      block: suspend (context: CallbackContext) -> CallbackChoice<Unit, Content>
    ): AfterAgentCallback =
      object : AfterAgentCallback {
        override suspend fun call(context: CallbackContext) = block(context)
      }
  }
}
