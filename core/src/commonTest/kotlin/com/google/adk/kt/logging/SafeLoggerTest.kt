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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SafeLoggerTest {

  @Test
  fun log_whenLevelDisabled_doesNotEvaluateLambda() {

    val logger = TestLogger(enabledLevels = setOf(Level.INFO))
    var lambdaEvaluated = false

    logger.debug {
      lambdaEvaluated = true
      "Debug message"
    }

    assertFalse(lambdaEvaluated, "Debug lambda should not be evaluated when debug is disabled")
    assertTrue(logger.getLoggedMessages(Level.DEBUG).isEmpty())
  }

  @Test
  fun log_whenLevelEnabled_evaluatesLambdaAndLogsMessage() {

    val logger = TestLogger(enabledLevels = setOf(Level.INFO))
    var lambdaEvaluated = false

    logger.info {
      lambdaEvaluated = true
      "Info message"
    }

    assertTrue(lambdaEvaluated, "Info lambda should be evaluated when info is enabled")
    assertEquals(listOf("Info message"), logger.getLoggedMessages(Level.INFO))
  }

  @Test
  fun log_whenLambdaThrowsException_logsFallbackMessageAtSameLevel() {

    val logger = TestLogger(enabledLevels = setOf(Level.INFO))

    logger.info { throw RuntimeException("Simulated formatting error") }

    // The info message was not successfully generated but the error fallback was triggered.
    // The fallback is logged at the same level (INFO).
    val infoMessages = logger.getLoggedMessages(Level.INFO)
    assertTrue(infoMessages.isNotEmpty(), "A fallback message should be logged")
    assertTrue(
      infoMessages[0].startsWith(
        "Log message evaluation failed: java.lang.RuntimeException: Simulated formatting error"
      ),
      "Message should indicate evaluation failure",
    )
  }
}

private class TestLogger(
  val enabledLevels: Set<Level> = setOf(Level.INFO, Level.WARN, Level.ERROR)
) : SafeLogger() {
  override val name: String = "TestLogger"

  private val messages = mutableMapOf<Level, MutableList<String>>()

  override fun doLog(level: Level, cause: Throwable?, msg: () -> String) {
    if (enabledLevels.contains(level)) {
      messages.getOrPut(level) { mutableListOf() }.add(msg())
    }
  }

  fun getLoggedMessages(level: Level): List<String> {
    return messages[level] ?: emptyList()
  }
}
