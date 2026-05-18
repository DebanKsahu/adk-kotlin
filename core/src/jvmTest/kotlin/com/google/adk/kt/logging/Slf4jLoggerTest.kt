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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.slf4j.Logger as Slf4jLoggerIface
import org.slf4j.event.Level as Slf4jLevel
import org.slf4j.spi.LoggingEventBuilder

class Slf4jLoggerTest {

  @Test
  fun log_disabledLevel_doesNotLog() {
    val mockLogger: Slf4jLoggerIface = mock()
    val logger = Slf4jLogger(mockLogger)
    whenever(mockLogger.isEnabledForLevel(Slf4jLevel.INFO)).thenReturn(false)

    logger.log(Level.INFO, null) { "test message" }

    verify(mockLogger).isEnabledForLevel(Slf4jLevel.INFO)
    verifyNoMoreInteractions(mockLogger)
  }

  @Test
  fun log_enabledLevelWithoutCause_logsMessage() {
    val mockLogger: Slf4jLoggerIface = mock()
    val mockBuilder: LoggingEventBuilder = mock()
    val logger = Slf4jLogger(mockLogger)
    whenever(mockLogger.isEnabledForLevel(Slf4jLevel.INFO)).thenReturn(true)
    whenever(mockLogger.atLevel(Slf4jLevel.INFO)).thenReturn(mockBuilder)

    logger.log(Level.INFO, null) { "test message" }

    verify(mockLogger).isEnabledForLevel(Slf4jLevel.INFO)
    verify(mockLogger).atLevel(Slf4jLevel.INFO)
    verify(mockBuilder).log("test message")
    verifyNoMoreInteractions(mockLogger, mockBuilder)
  }

  @Test
  fun log_enabledLevelWithCause_logsMessageAndCause() {
    val mockLogger: Slf4jLoggerIface = mock()
    val mockBuilder: LoggingEventBuilder = mock()
    val logger = Slf4jLogger(mockLogger)
    val cause = RuntimeException("test exception")
    whenever(mockLogger.isEnabledForLevel(Slf4jLevel.ERROR)).thenReturn(true)
    whenever(mockLogger.atLevel(Slf4jLevel.ERROR)).thenReturn(mockBuilder)
    whenever(mockBuilder.setCause(cause)).thenReturn(mockBuilder)

    logger.log(Level.ERROR, cause) { "test error message" }

    verify(mockLogger).isEnabledForLevel(Slf4jLevel.ERROR)
    verify(mockLogger).atLevel(Slf4jLevel.ERROR)
    verify(mockBuilder).setCause(cause)
    verify(mockBuilder).log("test error message")
    verifyNoMoreInteractions(mockLogger, mockBuilder)
  }
}
