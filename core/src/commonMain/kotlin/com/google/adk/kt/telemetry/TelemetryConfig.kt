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

package com.google.adk.kt.telemetry

/**
 * Global configuration for ADK telemetry behavior.
 *
 * This is a singleton to maintain architectural parity with the Java and Python ADKs.
 *
 * @property captureMessageContent Whether to capture raw prompts and payloads into traces. Defaults
 *   to false to prevent PII leakage and OOM errors. This is marked as @Volatile to ensure
 *   visibility across concurrent agent executions.
 */
object TelemetryConfig {
  @Volatile var captureMessageContent: Boolean = false
}
