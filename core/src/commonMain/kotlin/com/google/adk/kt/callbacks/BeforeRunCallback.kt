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

import com.google.adk.kt.agents.InvocationContext
import com.google.adk.kt.types.Content

/** Callback executed before the ADK runner runs. */
interface BeforeRunCallback : Callback {
  /**
   * Callback executed before the ADK runner runs.
   *
   * This is the first callback called in the lifecycle, ideal for global setup or initialization
   * tasks.
   *
   * @param invocationContext The context for the entire invocation, containing session information,
   *   the root agent, etc.
   * @return A [CallbackChoice] where returning [CallbackChoice.Break] with custom [Content] halts
   *   execution of the runner and resolves the invocation with that content directly. Returning
   *   [CallbackChoice.Continue] with [Unit] allows execution to proceed normally.
   */
  suspend fun call(invocationContext: InvocationContext): CallbackChoice<Unit, Content>

  companion object {
    // Workaround for problems when automatic SAM conversion code generated with gradle kotlin
    // plugin introduces an invalid method name in class files for functional interfaces with
    // abstract suspend methods.
    // This manual override permits the class to be used as if it were a functional interface with
    // SAM conversion (eg. BeforeRunCallback { invocationContext -> ... }).

    operator fun invoke(
      block: suspend (invocationContext: InvocationContext) -> CallbackChoice<Unit, Content>
    ): BeforeRunCallback =
      object : BeforeRunCallback {
        override suspend fun call(invocationContext: InvocationContext) = block(invocationContext)
      }
  }
}
