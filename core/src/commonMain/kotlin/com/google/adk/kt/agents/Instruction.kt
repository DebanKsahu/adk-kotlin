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

import com.google.adk.kt.types.Content
import com.google.adk.kt.types.Part

/**
 * A unit of instruction provided to an [LlmAgent].
 *
 * Use one of the variants:
 * - [Text] for a single literal string (most common).
 * - [Structured] for a pre-built, possibly multimodal [Content].
 * - [Provider] for instructions that must be resolved per turn from a [ReadonlyContext].
 *
 * Convenience factories on the companion object allow the call sites `Instruction("text")`,
 * `Instruction(content)`, and `Instruction { ctx -> ... }`.
 */
sealed interface Instruction {

  /** A literal text instruction. Wrapped into a single [Part] at runtime. */
  @JvmInline value class Text(val text: String) : Instruction

  /** A pre-built structured instruction, e.g. multimodal content. */
  @JvmInline value class Structured(val content: Content) : Instruction

  /**
   * A function that produces the instruction [Content] at turn time, given a [ReadonlyContext].
   *
   * Returning `null` indicates "no instruction this turn".
   */
  fun interface Provider : Instruction {
    suspend fun provide(context: ReadonlyContext): Content?
  }

  companion object {
    /** Shortcut for [Text]. */
    operator fun invoke(text: String): Instruction = Text(text)

    /** Shortcut for [Structured]. */
    operator fun invoke(content: Content): Instruction = Structured(content)

    /** Shortcut for [Provider]. */
    operator fun invoke(provider: suspend (ReadonlyContext) -> Content?): Instruction =
      Provider { context ->
        provider(context)
      }
  }
}

/** Materializes this [Instruction] to a [Content] for the given [context]. */
internal suspend fun Instruction.resolve(context: ReadonlyContext): Content? =
  when (this) {
    is Instruction.Text -> Content(parts = listOf(Part(text = text)))
    is Instruction.Structured -> content
    is Instruction.Provider -> provide(context)
  }
