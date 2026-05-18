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

/** Callback invoked immediately before a tool is executed. */
interface BeforeToolCallback : Callback {
  /**
   * Invoked before the tool is executed.
   *
   * @param context The context of the current tool execution.
   * @param tool The tool about to be executed.
   * @param args The arguments to be passed to the tool.
   * @return A [CallbackChoice] representing the tool response/arguments. When
   *   [CallbackChoice.Break] is returned, the value will be used as the tool response and the
   *   framework will skip calling the actual tool. When [CallbackChoice.Continue] is returned, the
   *   value will be used as the arguments to be passed to the tool.
   */
  suspend fun call(
    context: ToolContext,
    tool: BaseTool,
    args: Map<String, Any>,
  ): CallbackChoice<Map<String, Any>, Map<String, Any>>

  companion object {
    // Workaround for problems when automatic SAM conversion code generated with gradle kotlin
    // plugin introduces an invalid method name in class files for functional interfaces with
    // abstract suspend methods.
    // This manual override permits the class to be used as if it were a functional interface with
    // SAM conversion (eg. BeforeToolCallback { context, tool, args -> ... }).

    operator fun invoke(
      block:
        suspend (context: ToolContext, tool: BaseTool, args: Map<String, Any>) -> CallbackChoice<
            Map<String, Any>,
            Map<String, Any>,
          >
    ): BeforeToolCallback =
      object : BeforeToolCallback {
        override suspend fun call(context: ToolContext, tool: BaseTool, args: Map<String, Any>) =
          block(context, tool, args)
      }
  }
}
