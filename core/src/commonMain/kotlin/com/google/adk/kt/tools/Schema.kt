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

package com.google.adk.kt.tools

/** The annotation for binding the 'Schema' input. */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Schema(
  /**
   * Specifies a custom name for the schema input, defaulting to an empty string.
   *
   * This can be used to identify or label the specific schema being injected or used.
   */
  val name: String = "",
  /**
   * Provides a human-readable description of the schema input, defaulting to an empty string.
   *
   * This helps document the purpose or nature of the schema.
   */
  val description: String = "",
  /**
   * Indicates whether the schema input is optional, defaulting to false.
   *
   * If true, the annotated element can function even if the schema input is not provided. If false,
   * the schema input is considered mandatory.
   */
  val optional: Boolean = false,
)
