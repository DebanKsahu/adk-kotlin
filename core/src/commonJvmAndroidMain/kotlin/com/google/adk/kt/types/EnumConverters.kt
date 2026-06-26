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
package com.google.adk.kt.types

import com.google.genai.types.BlockedReason as GenAiBlockedReason
import com.google.genai.types.FinishReason as GenAiFinishReason
import com.google.genai.types.HarmBlockThreshold as GenAiHarmBlockThreshold
import com.google.genai.types.HarmCategory as GenAiHarmCategory
import com.google.genai.types.MediaModality as GenAiMediaModality
import com.google.genai.types.MediaResolution as GenAiMediaResolution
import com.google.genai.types.ServiceTier as GenAiServiceTier
import com.google.genai.types.ThinkingLevel as GenAiThinkingLevel

/** Converts a [GenAiBlockedReason] from the GenAI SDK to an ADK [BlockedReason]. */
internal fun GenAiBlockedReason.toKt(): BlockedReason =
  when (this.knownEnum()) {
    GenAiBlockedReason.Known.BLOCKED_REASON_UNSPECIFIED -> BlockedReason.BLOCKED_REASON_UNSPECIFIED
    GenAiBlockedReason.Known.SAFETY -> BlockedReason.SAFETY
    GenAiBlockedReason.Known.OTHER -> BlockedReason.OTHER
    GenAiBlockedReason.Known.BLOCKLIST -> BlockedReason.BLOCKLIST
    GenAiBlockedReason.Known.PROHIBITED_CONTENT -> BlockedReason.PROHIBITED_CONTENT
    GenAiBlockedReason.Known.IMAGE_SAFETY -> BlockedReason.IMAGE_SAFETY
    GenAiBlockedReason.Known.MODEL_ARMOR -> BlockedReason.MODEL_ARMOR
    GenAiBlockedReason.Known.JAILBREAK -> BlockedReason.JAILBREAK
  }

/** Converts an ADK [BlockedReason] to a [GenAiBlockedReason] for the GenAI SDK. */
internal fun BlockedReason.toJava(): GenAiBlockedReason = GenAiBlockedReason(this.name)

/** Converts a [GenAiFinishReason] from the GenAI SDK to an ADK [FinishReason]. */
internal fun GenAiFinishReason.toKt(): FinishReason =
  runCatching { FinishReason.valueOf(this.toString()) }.getOrDefault(FinishReason.OTHER)

/** Converts an ADK [FinishReason] to a [GenAiFinishReason] for the GenAI SDK. */
internal fun FinishReason.toJava(): GenAiFinishReason = GenAiFinishReason(this.name)

/** Converts an ADK [BlockedReason] to its equivalent [FinishReason]. */
internal fun GenAiBlockedReason.toFinishReason(): FinishReason =
  when (this.knownEnum()) {
    GenAiBlockedReason.Known.SAFETY -> FinishReason.SAFETY
    else -> FinishReason.OTHER
  }

/** Converts a [GenAiThinkingLevel] from the GenAI SDK to an ADK [ThinkingLevel]. */
internal fun GenAiThinkingLevel.toKt(): ThinkingLevel =
  runCatching { ThinkingLevel.valueOf(this.toString()) }
    .getOrDefault(ThinkingLevel.THINKING_LEVEL_UNSPECIFIED)

/** Converts an ADK [ThinkingLevel] to a [GenAiThinkingLevel] for the GenAI SDK. */
internal fun ThinkingLevel.toJava(): GenAiThinkingLevel = GenAiThinkingLevel(this.name)

/** Converts a [GenAiMediaModality] from the GenAI SDK to an ADK [MediaModality]. */
internal fun GenAiMediaModality.toKt(): MediaModality =
  runCatching { MediaModality.valueOf(this.toString()) }
    .getOrDefault(MediaModality.MODALITY_UNSPECIFIED)

/** Converts an ADK [MediaModality] to a [GenAiMediaModality] for the GenAI SDK. */
internal fun MediaModality.toJava(): GenAiMediaModality = GenAiMediaModality(this.name)

/** Converts a [GenAiHarmCategory] from the GenAI SDK to an ADK [HarmCategory]. */
internal fun GenAiHarmCategory.toKt(): HarmCategory =
  runCatching { HarmCategory.valueOf(this.toString()) }
    .getOrDefault(HarmCategory.HARM_CATEGORY_UNSPECIFIED)

/** Converts an ADK [HarmCategory] to a [GenAiHarmCategory] for the GenAI SDK. */
internal fun HarmCategory.toJava(): GenAiHarmCategory = GenAiHarmCategory(this.name)

/** Converts a [GenAiHarmBlockThreshold] from the GenAI SDK to an ADK [HarmBlockThreshold]. */
internal fun GenAiHarmBlockThreshold.toKt(): HarmBlockThreshold =
  runCatching { HarmBlockThreshold.valueOf(this.toString()) }
    .getOrDefault(HarmBlockThreshold.HARM_BLOCK_THRESHOLD_UNSPECIFIED)

/** Converts an ADK [HarmBlockThreshold] to a [GenAiHarmBlockThreshold] for the GenAI SDK. */
internal fun HarmBlockThreshold.toJava(): GenAiHarmBlockThreshold =
  GenAiHarmBlockThreshold(this.name)

/** Converts a [GenAiMediaResolution] from the GenAI SDK to an ADK [MediaResolution]. */
internal fun GenAiMediaResolution.toKt(): MediaResolution =
  runCatching { MediaResolution.valueOf(this.toString()) }
    .getOrDefault(MediaResolution.MEDIA_RESOLUTION_UNSPECIFIED)

/** Converts an ADK [MediaResolution] to a [GenAiMediaResolution] for the GenAI SDK. */
internal fun MediaResolution.toJava(): GenAiMediaResolution = GenAiMediaResolution(this.name)

/** Converts a [GenAiServiceTier] from the GenAI SDK to an ADK [ServiceTier]. */
internal fun GenAiServiceTier.toKt(): ServiceTier =
  runCatching { ServiceTier.valueOf(this.toString()) }.getOrDefault(ServiceTier.UNSPECIFIED)

/** Converts an ADK [ServiceTier] to a [GenAiServiceTier] for the GenAI SDK. */
internal fun ServiceTier.toJava(): GenAiServiceTier = GenAiServiceTier(this.name)
