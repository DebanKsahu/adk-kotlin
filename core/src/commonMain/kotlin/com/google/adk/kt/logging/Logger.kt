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

/** An interface representing a logger. Allows logging messages at various severity [Level]s. */
interface Logger {
  /** The name of this logger instance. */
  val name: String

  /**
   * Logs a message at the specified [level].
   *
   * @param level The severity level of the log message.
   * @param cause An optional [Throwable] to log along with the message.
   * @param msg A lambda returning the string message to log. It is only evaluated if the given
   *   [level] is enabled.
   */
  fun log(level: Level, cause: Throwable? = null, msg: () -> String)

  /**
   * Logs a message at the [Level.TRACE] level.
   *
   * @param cause An optional [Throwable] to log along with the message.
   * @param msg A lambda returning the string message to log. It is only evaluated if the given
   *   level is enabled.
   */
  fun trace(cause: Throwable? = null, msg: () -> String) = log(Level.TRACE, cause, msg)

  /**
   * Logs a message at the [Level.DEBUG] level.
   *
   * @param cause An optional [Throwable] to log along with the message.
   * @param msg A lambda returning the string message to log. It is only evaluated if the given
   *   level is enabled.
   */
  fun debug(cause: Throwable? = null, msg: () -> String) = log(Level.DEBUG, cause, msg)

  /**
   * Logs a message at the [Level.INFO] level.
   *
   * @param cause An optional [Throwable] to log along with the message.
   * @param msg A lambda returning the string message to log. It is only evaluated if the given
   *   level is enabled.
   */
  fun info(cause: Throwable? = null, msg: () -> String) = log(Level.INFO, cause, msg)

  /**
   * Logs a message at the [Level.WARN] level.
   *
   * @param cause An optional [Throwable] to log along with the message.
   * @param msg A lambda returning the string message to log. It is only evaluated if the given
   *   level is enabled.
   */
  fun warn(cause: Throwable? = null, msg: () -> String) = log(Level.WARN, cause, msg)

  /**
   * Logs a message at the [Level.ERROR] level.
   *
   * @param cause An optional [Throwable] to log along with the message.
   * @param msg A lambda returning the string message to log. It is only evaluated if the given
   *   level is enabled.
   */
  fun error(cause: Throwable? = null, msg: () -> String) = log(Level.ERROR, cause, msg)
}
