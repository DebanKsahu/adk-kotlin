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

package com.google.adk.kt.agents

import com.google.adk.kt.logging.Logger

/**
 * Finds the index of the sub-agent to resume from based on its name. If the agent is not found,
 * logs a warning and returns 0.
 *
 * @param agentName The name of the agent to find.
 * @param logger The logger to use for warnings.
 * @return The index of the sub-agent, or 0 if not found.
 */
internal fun List<BaseAgent>.findIndexForResumption(agentName: String?, logger: Logger): Int {
  if (agentName == null) return 0
  return this.indexOfFirst { it.name == agentName }
    .let {
      if (it == -1) {
        logger.warn {
          "Restored sub-agent '$agentName' not found in current sub-agents list. Falling back to index 0."
        }
        0
      } else {
        it
      }
    }
}
