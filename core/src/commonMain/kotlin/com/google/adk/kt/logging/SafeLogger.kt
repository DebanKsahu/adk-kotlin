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

/**
 * An abstract base class for [Logger] implementations that safely evaluates the log message lambda.
 * If the lambda throws an exception, it is caught and logged without crashing the application.
 */
abstract class SafeLogger : Logger {

  /**
   * Subclasses should implement this method to perform the actual logging.
   *
   * @param level The severity level of the log message.
   * @param cause An optional [Throwable] to log along with the message.
   * @param msg A lambda returning the string message to log.
   */
  protected abstract fun doLog(level: Level, cause: Throwable? = null, msg: () -> String)

  override fun log(level: Level, cause: Throwable?, msg: () -> String) {
    doLog(level, cause, safeMsg(msg))
  }

  private fun safeMsg(msg: () -> String): () -> String = {
    try {
      msg()
    } catch (e: Exception) {
      "Log message evaluation failed: $e"
    }
  }
}
