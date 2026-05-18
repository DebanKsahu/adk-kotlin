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
import com.google.adk.kt.events.EventActions
import com.google.adk.kt.types.Content

/** Callback invoked before an agent starts running. */
interface BeforeAgentCallback : Callback {
  /**
   * Callback executed before an agent's primary logic is invoked.
   *
   * This callback can be used for logging, setup, or short-circuiting the agent's execution.
   *
   * @param context The context of the current agent call.
   * @return A [CallbackChoice] where returning [CallbackChoice.Break] with custom [Content]
   *   bypasses the agent's regular execution entirely and directly yields the provided content.
   *   Returning [CallbackChoice.Continue] with [EventActions] allows normal execution to proceed,
   *   merging any actions into the running context.
   */
  suspend fun call(context: CallbackContext): CallbackChoice<EventActions, Content>

  companion object {
    // Workaround for problems when automatic SAM conversion code generated with gradle kotlin
    // plugin introduces an invalid method name in class files for functional interfaces with
    // abstract suspend methods.
    // This manual override permits the class to be used as if it were a functional interface with
    // SAM conversion (eg. BeforeAgentCallback { context -> ... }).

    operator fun invoke(
      block: suspend (context: CallbackContext) -> CallbackChoice<EventActions, Content>
    ): BeforeAgentCallback =
      object : BeforeAgentCallback {
        override suspend fun call(context: CallbackContext) = block(context)
      }
  }
}
