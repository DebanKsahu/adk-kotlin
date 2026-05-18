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

import io.modelcontextprotocol.client.McpAsyncClient

/**
 * Manages MCP client sessions.
 *
 * This interface provides methods for creating and initializing MCP client sessions, handling
 * different connection parameters and transport builders.
 */
internal interface SessionManager {
  /** Creates an asynchronous session. */
  fun createAsyncSession(headers: Map<String, String> = emptyMap()): McpAsyncClient
}
