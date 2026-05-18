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

/**
 * A generic callback choice type for callbacks.
 *
 * @param ContinueT The type of data passed forward when continuing execution.
 * @param BreakT The type of data returned when breaking/short-circuiting execution.
 */
sealed interface CallbackChoice<out ContinueT, out BreakT> {
  /** Proceed with the execution, passing the original or modified data to the next stage. */
  data class Continue<out ContinueT>(val value: ContinueT) : CallbackChoice<ContinueT, Nothing>

  /** Halt or short-circuit execution, returning a final or replacement value. */
  data class Break<out BreakT>(val value: BreakT) : CallbackChoice<Nothing, BreakT>
}
