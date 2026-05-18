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

package com.google.adk.kt.logging

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FloggerLoggerTest {

  private val logger = FloggerLogger(com.google.common.flogger.GoogleLogger.forEnclosingClass())

  @Test
  fun log_allLevels_noCrash() {
    for (level in Level.values()) {
      logger.log(level) { "Testing level $level" }
      logger.log(level, Exception("test")) { "Testing level $level with exception" }
    }
  }

  @Test
  fun shorthandMethods_noCrash() {
    logger.trace { "trace" }
    logger.debug { "debug" }
    logger.info { "info" }
    logger.warn { "warn" }
    logger.error { "error" }
    logger.error(Exception("err")) { "error with cause" }
  }
}
