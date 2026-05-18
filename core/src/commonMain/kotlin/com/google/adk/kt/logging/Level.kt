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

/** Represents the severity level of a log message. */
enum class Level {
  /** Trace level logging, used for very detailed debugging. */
  TRACE,
  /** Debug level logging, used for general debugging information. */
  DEBUG,
  /** Info level logging, used for informational messages. */
  INFO,
  /** Warning level logging, used to indicate potential issues. */
  WARN,
  /** Error level logging, used to indicate failures or errors. */
  ERROR,
}
