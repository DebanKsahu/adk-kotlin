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

package com.google.adk.kt.tools

import com.google.adk.kt.models.LlmRequest
import com.google.adk.kt.types.FunctionDeclaration

/**
 * Abstract base class for defining and executing tools.
 *
 * @property name The name of the tool.
 * @property description The description of the tool.
 * @property isLongRunning Whether the tool's final result will be delivered out-of-band. When
 *   `true`, the framework marks the call as long-running and uses the tool's return value as the
 *   function-response payload (or suppresses the response entirely if the tool returns `Unit`).
 * @property customMetadata The custom metadata of the tool.
 */
abstract class BaseTool(
  val name: String,
  val description: String,
  val isLongRunning: Boolean = false,
  val customMetadata: Map<String, Any> = emptyMap(),
) : AutoCloseable {
  /** Returns the underlying function declaration. */
  abstract fun declaration(): FunctionDeclaration?

  /** Executes the tool. */
  abstract suspend fun run(context: ToolContext, args: Map<String, Any>): Any

  /**
   * Processes the LLM request before it is sent.
   *
   * Tools can override this to attach instructions, artifacts, or other data to the request. By
   * default, this implementation appends the tool itself to the [LlmRequest], making it available
   * for use by the LLM.
   */
  open suspend fun processLlmRequest(toolContext: ToolContext, llmRequest: LlmRequest): LlmRequest {
    return llmRequest.appendTools(listOf(this))
  }

  override fun close() {}

  companion object {
    const val RESULT_KEY = "result"
  }
}
