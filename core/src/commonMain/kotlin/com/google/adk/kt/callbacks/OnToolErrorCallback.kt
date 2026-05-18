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

import com.google.adk.kt.tools.BaseTool
import com.google.adk.kt.tools.ToolContext

/** Callback invoked when a tool encounters an error. */
interface OnToolErrorCallback : Callback {
  /**
   * Callback executed when a tool call encounters an error.
   *
   * Provides an opportunity to handle tool errors gracefully, potentially providing alternative
   * responses or recovery mechanisms.
   *
   * @param context The context of the current tool execution.
   * @param tool The tool instance that encountered an error.
   * @param args The arguments that were passed to the tool.
   * @param error The exception that was raised.
   * @return A [CallbackChoice] where returning [CallbackChoice.Break] with a custom map intercepts
   *   the error and resolves execution using that fallback value. Returning
   *   [CallbackChoice.Continue] with [Unit] permits the error to be propagated naturally.
   */
  suspend fun call(
    context: ToolContext,
    tool: BaseTool,
    args: Map<String, Any>,
    error: Throwable,
  ): CallbackChoice<Unit, Map<String, Any>>

  companion object {
    // Workaround for problems when automatic SAM conversion code generated with gradle kotlin
    // plugin introduces an invalid method name in class files for functional interfaces with
    // abstract suspend methods.
    // This manual override permits the class to be used as if it were a functional interface with
    // SAM conversion (eg. OnToolErrorCallback { context, tool, args, error -> ... }).

    operator fun invoke(
      block:
        suspend (
          context: ToolContext, tool: BaseTool, args: Map<String, Any>, error: Throwable,
        ) -> CallbackChoice<Unit, Map<String, Any>>
    ): OnToolErrorCallback =
      object : OnToolErrorCallback {
        override suspend fun call(
          context: ToolContext,
          tool: BaseTool,
          args: Map<String, Any>,
          error: Throwable,
        ) = block(context, tool, args, error)
      }
  }
}
