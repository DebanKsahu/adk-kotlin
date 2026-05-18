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

/**
 * Represents the response from a search operation in the memory service.
 *
 * @property memories The list of memory entries matching the search criteria.
 * @property nextPageToken A token to retrieve the next page of results, or null if there are no
 *   more results.
 */
data class SearchMemoryResponse(val memories: List<MemoryEntry>, val nextPageToken: String? = null)
