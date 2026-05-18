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

package com.google.adk.kt.memory

import com.google.adk.kt.sessions.Session

/**
 * Base contract for memory services.
 *
 * The service provides functionalities to ingest sessions into memory so that the memory can be
 * used for user queries.
 */
interface MemoryService {

  /**
   * Adds a session to the memory service.
   *
   * A session may be added multiple times during its lifetime.
   *
   * @param session The session to add.
   */
  suspend fun addSessionToMemory(session: Session)

  /**
   * Searches for sessions that match the query asynchronously.
   *
   * @param appName The name of the application.
   * @param userId The id of the user.
   * @param query The query to search for.
   * @return A [SearchMemoryResponse] containing the matching memories.
   */
  suspend fun searchMemory(appName: String, userId: String, query: String): SearchMemoryResponse
}
