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

/** A [Logger] implementation that delegates to an underlying [org.slf4j.Logger]. */
class Slf4jLogger(private val slf4jLogger: org.slf4j.Logger) : SafeLogger() {
  override val name: String
    get() = slf4jLogger.name

  override fun doLog(level: Level, cause: Throwable?, msg: () -> String) {
    val slf4jLevel = level.toSlf4jLevel()
    if (slf4jLogger.isEnabledForLevel(slf4jLevel)) {
      slf4jLogger.atLevel(slf4jLevel).run { cause?.let { setCause(it) } ?: this }.log(msg())
    }
  }
}

private fun Level.toSlf4jLevel(): org.slf4j.event.Level =
  when (this) {
    Level.TRACE -> org.slf4j.event.Level.TRACE
    Level.DEBUG -> org.slf4j.event.Level.DEBUG
    Level.INFO -> org.slf4j.event.Level.INFO
    Level.WARN -> org.slf4j.event.Level.WARN
    Level.ERROR -> org.slf4j.event.Level.ERROR
  }
