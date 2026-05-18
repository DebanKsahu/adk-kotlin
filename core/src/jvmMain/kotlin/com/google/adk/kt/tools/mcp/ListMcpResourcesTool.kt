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

import com.google.adk.kt.tools.BaseTool
import com.google.adk.kt.tools.ToolContext
import com.google.adk.kt.tools.mcp.McpToolException.McpToolExecutionException
import com.google.adk.kt.types.FunctionDeclaration
import com.google.adk.kt.types.Schema
import com.google.adk.kt.types.Type
import io.modelcontextprotocol.client.McpAsyncClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.reactor.awaitSingle

/** A built-in tool that allows the ADK agents to list resources exposed by the MCP server. */
internal class ListMcpResourcesTool(private val mcpSession: McpAsyncClient) :
  BaseTool("list_mcp_resources", "List resources available on the MCP server.") {

  override suspend fun run(context: ToolContext, args: Map<String, Any>): Any {
    try {
      val cursor = args["cursor"] as? String

      val result =
        if (cursor != null) {
            mcpSession.listResources(cursor)
          } else {
            mcpSession.listResources()
          }
          .awaitSingle()

      val resources =
        result.resources().map { resource ->
          buildMap {
            put(RESOURCE_NAME, resource.name())
            put(RESOURCE_URI, resource.uri())
            resource.description()?.let { description -> put(RESOURCE_DESCRIPTION, description) }
            resource.mimeType()?.let { mimeType -> put(RESOURCE_MIME_TYPE, mimeType) }
          }
        }

      val response = mutableMapOf<String, Any>("resources" to resources)
      result.nextCursor()?.let { response["nextCursor"] = it }
      return response
    } catch (e: CancellationException) {
      throw e // Re-throw cancellation exceptions as they are not indicative of a tool failure.
    } catch (e: Exception) {
      throw McpToolExecutionException("Failed to list MCP resources: ${e.message}", cause = e)
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
              "cursor" to
                Schema(
                  type = Type.STRING,
                  description = "Optional pagination cursor for listing the next page.",
                )
            ),
          required = emptyList(),
        ),
    )
  }

  companion object {
    const val RESOURCE_NAME = "name"
    const val RESOURCE_URI = "uri"
    const val RESOURCE_DESCRIPTION = "description"
    const val RESOURCE_MIME_TYPE = "mimeType"
  }
}
