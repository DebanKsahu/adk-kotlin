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

package com.google.adk.kt.events

/**
 * Returns [events] with rewound invocations removed.
 *
 * Iterates backward. When an event carries `actions.rewindBeforeInvocationId == X`, drops that
 * event together with every event between it and the earliest event of invocation `X` (inclusive),
 * then resumes the backward walk from there.
 *
 * This is the single source of truth for "which events are live" after rewinds. Both LLM prompt
 * building ([com.google.adk.kt.processors.HistoryRewriterProcessor]) and context compaction must
 * agree on it, otherwise rewound content can leak back into prompts through a compaction summary.
 */
internal fun applyRewinds(events: List<Event>): List<Event> {
  val kept = mutableListOf<Event>()
  var i = events.size - 1
  while (i >= 0) {
    val event = events[i]
    val rewindInvocationId = event.actions.rewindBeforeInvocationId
    if (!rewindInvocationId.isNullOrEmpty()) {
      for (j in 0 until i) {
        if (events[j].invocationId == rewindInvocationId) {
          i = j
          break
        }
      }
    } else {
      kept.add(event)
    }
    i--
  }
  return kept.asReversed()
}
