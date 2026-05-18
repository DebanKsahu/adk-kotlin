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

package com.google.adk.kt.sessions

import kotlin.test.Test
import kotlin.test.assertEquals

class LockTest {
  @Test
  fun read_executesActionAndReturnsResult() {
    val lock = Lock()
    val result = lock.read { 123 }
    assertEquals(123, result)
  }

  @Test
  fun write_executesAction() {
    val lock = Lock()
    var value = 0
    lock.write { value = 123 }
    assertEquals(123, value)
  }

  @Test
  fun readAndWrite_provideConsistentView() {
    val lock = Lock()
    var value = 0
    lock.write { value = 1 }
    val read1 = lock.read { value }
    lock.write { value = 2 }
    val read2 = lock.read { value }
    assertEquals(1, read1)
    assertEquals(2, read2)
  }
}
