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

package com.google.adk.kt.ids

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class UuidTest {
  @Test
  fun random_generatesNonNullUuid() {
    val uuid = Uuid.random()
    assertNotNull(uuid)
  }

  @Test
  fun random_generatesDifferentUuids() {
    val uuid1 = Uuid.random()
    val uuid2 = Uuid.random()
    assertNotEquals(uuid1, uuid2)
  }
}
