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

/** A type-safe hierarchy for agent state variables, ensuring primitives don't collapse. */
sealed class TypedData {
  /** Represents a null value in the agent state. */
  data object NullValue : TypedData()

  /**
   * Represents an integer value in the agent state.
   *
   * @property value The integer value.
   */
  data class IntValue(val value: Int) : TypedData()

  /**
   * Represents a long value in the agent state.
   *
   * @property value The long value.
   */
  data class LongValue(val value: Long) : TypedData()

  /**
   * Represents a string value in the agent state.
   *
   * @property value The string value.
   */
  data class StringValue(val value: String) : TypedData()

  /**
   * Represents a boolean value in the agent state.
   *
   * @property value The boolean value.
   */
  data class BooleanValue(val value: Boolean) : TypedData()

  /**
   * Represents a double value in the agent state.
   *
   * @property value The double value.
   */
  data class DoubleValue(val value: Double) : TypedData()

  /**
   * Represents a list of state values.
   *
   * @property elements The list of child values.
   */
  data class ListValue(val elements: List<TypedData>) : TypedData()

  /**
   * Represents a map of state values.
   *
   * @property fields The map of child values keyed by field name.
   */
  data class MapValue(val fields: Map<String, TypedData>) : TypedData() {
    /** Gets a string value for the given [key]. */
    fun getString(key: String): String? {
      return (this.fields[key] as? StringValue)?.value
    }

    /** Gets an integer value for the given [key]. */
    fun getInt(key: String): Int? {
      return (this.fields[key] as? IntValue)?.value
    }
  }
}
