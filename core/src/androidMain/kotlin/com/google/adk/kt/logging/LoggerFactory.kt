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

import android.util.Log
import kotlin.reflect.KClass

/** A factory object for obtaining [Logger] instances on the Android platform. */
private object AndroidLoggerFactory : LoggerFactory {
  /** Returns a [Logger] backed by [android.util.Log] for the given [kClass]. */
  override fun getLogger(kClass: KClass<*>): Logger =
    object : SafeLogger() {
      override val name: String = kClass.simpleName ?: "Unknown"

      override fun doLog(level: Level, cause: Throwable?, msg: () -> String) {
        when (level) {
          Level.TRACE -> Log.v(name, msg(), cause)
          Level.DEBUG -> Log.d(name, msg(), cause)
          Level.INFO -> Log.i(name, msg(), cause)
          Level.WARN -> Log.w(name, msg(), cause)
          Level.ERROR -> Log.e(name, msg(), cause)
        }
      }
    }
}

internal actual fun getLoggerFactory(): LoggerFactory = AndroidLoggerFactory
