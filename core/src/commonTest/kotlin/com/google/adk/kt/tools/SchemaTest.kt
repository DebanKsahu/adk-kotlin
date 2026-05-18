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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SchemaTest {

  @Schema(name = "testName", description = "testDesc", optional = true) fun annotatedFunction() {}

  fun annotatedParameter(
    @Schema(name = "paramName", description = "paramDesc", optional = false) param: String
  ) {}

  @Test
  fun schemaAnnotation_onFunction_isReadCorrectly() {
    val method = SchemaTest::class.members.find { it.name == "annotatedFunction" }
    val annotation = method?.annotations?.filterIsInstance<Schema>()?.firstOrNull()

    assertEquals("testName", annotation?.name)
    assertEquals("testDesc", annotation?.description)
    assertTrue(annotation?.optional == true)
  }

  @Test
  fun schemaAnnotation_onParameter_isReadCorrectly() {
    val method = SchemaTest::class.members.find { it.name == "annotatedParameter" }
    val parameter = method?.parameters?.find { it.name == "param" }
    val annotation = parameter?.annotations?.filterIsInstance<Schema>()?.firstOrNull()

    assertEquals("paramName", annotation?.name)
    assertEquals("paramDesc", annotation?.description)
    assertFalse(annotation?.optional == true)
  }
}
