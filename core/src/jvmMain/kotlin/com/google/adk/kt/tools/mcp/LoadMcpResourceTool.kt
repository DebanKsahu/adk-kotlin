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

package com.google.adk.kt.tools.mcp

import com.google.adk.kt.agents.toReadonlyContext
import com.google.adk.kt.tools.BaseTool
import com.google.adk.kt.tools.ToolContext
import com.google.adk.kt.tools.mcp.McpToolException.McpToolExecutionException
import com.google.adk.kt.types.FunctionDeclaration
import com.google.adk.kt.types.Schema
import com.google.adk.kt.types.Type
import io.modelcontextprotocol.spec.McpSchema
import kotlinx.coroutines.CancellationException

/**
 * A built-in tool that allows the ADK agents to load resources exposed by the MCP server.
 *
 * Requires `useMcpResources = true` in the `McpToolset` configuration.
 */
internal class LoadMcpResourceTool(
  private val mcpToolset: McpToolset,
  private val maxMcpResourceLength: Int,
) : BaseTool("load_mcp_resource", "Load a resource from the MCP server by URI.") {
  override suspend fun run(context: ToolContext, args: Map<String, Any>): Any {
    try {
      val uri = args[URI] as? String ?: throw IllegalArgumentException("Resource URI is required.")
      val contents =
        mcpToolset.readResource(uri, context.invocationContext.toReadonlyContext()) as? List<*>
          ?: throw IllegalArgumentException(
            "MCP server returned an unexpected response structure for URI: $uri"
          )

      if (contents.isEmpty()) {
        return ""
      }
      return contents
        .map { content ->
          when (content) {
            is McpSchema.TextResourceContents -> {
              val text = content.text() ?: ""
              if (text.length > maxMcpResourceLength) {
                text.take(maxMcpResourceLength) + "... [Content truncated due to size limit]"
              } else {
                text
              }
            }
            is McpSchema.BlobResourceContents -> {
              "[Warning: Binary data found at this URI, cannot display raw content]"
            }
            else -> content.toString()
          }
        }
        .joinToString("\n\n")
    } catch (e: CancellationException) {
      throw e // Re-throw cancellation exceptions as they are not indicative of a tool failure.
    } catch (e: Exception) {
      throw McpToolExecutionException("Failed to load MCP resource: ${e.message}", cause = e)
    }
  }

  override fun declaration(): FunctionDeclaration {
    return FunctionDeclaration(
      name = name,
      description = description,
      parameters =
        Schema(
          type = Type.OBJECT,
          properties =
            mapOf(
              URI to Schema(type = Type.STRING, description = "The URI of the resource to load.")
            ),
          required = listOf(URI),
        ),
    )
  }

  companion object {
    private const val URI = "uri"
  }
}
