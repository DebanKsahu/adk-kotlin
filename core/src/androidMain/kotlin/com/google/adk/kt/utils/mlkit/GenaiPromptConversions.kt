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
package com.google.adk.kt.utils.mlkit

import androidx.core.net.toUri
import com.google.adk.kt.logging.LoggerFactory
import com.google.adk.kt.models.LlmRequest
import com.google.adk.kt.models.LlmResponse
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.FinishReason
import com.google.adk.kt.types.Part
import com.google.adk.kt.types.Role
import com.google.mlkit.genai.prompt.Candidate
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.GenerateContentResponse
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.PromptPrefix
import com.google.mlkit.genai.prompt.TextPart

/** Utility functions for converting between ADK and ML Kit request and response formats. */
internal object GenaiPromptConversions {
  private val logger = LoggerFactory.getLogger(GenaiPromptConversions::class)

  private fun String?.isImageMimeType(): Boolean {
    return this?.startsWith("image/") == true
  }

  private val instructionSeparator = "\n\n"

  /**
   * Converts an [LlmRequest] to a [GenerateContentRequest].
   *
   * The ML Kit request can only contain one text part and one image part. If multiple text parts
   * are present, they are concatenated together separated by "\n\n". Image parts except for the
   * first one are ignored.
   *
   * Certain parameters from the [LlmRequest.config] are used to configure the ML Kit request.
   *
   * @return The [GenerateContentRequest] to be used for the ML Kit API call.
   */
  internal fun LlmRequest.toGenerateContentRequest(): GenerateContentRequest {
    val allParts = contents.flatMap { it.parts }
    val allText = allParts.mapNotNull { it.text }.joinToString(instructionSeparator)
    val imageCount = allParts.count {
      it.inlineData?.mimeType.isImageMimeType() || it.fileData?.mimeType.isImageMimeType()
    }

    if (imageCount > 1) {
      logger.warn {
        "Multiple images found in the LlmRequest. Only the first image will be used in the GenerateContentRequest."
      }
    }

    val imagePart = allParts.firstNotNullOfOrNull { part ->
      val inlineData = part.inlineData
      val fileData = part.fileData
      when {
        inlineData?.mimeType.isImageMimeType() -> {
          inlineData?.data?.let { ImagePart(it) }
        }
        fileData?.mimeType.isImageMimeType() -> {
          fileData?.fileUri?.let { ImagePart(it.toUri()) }
        }
        else -> null
      }
    }

    val systemText =
      config.systemInstruction?.parts?.mapNotNull { it.text }?.joinToString(instructionSeparator)
        ?: ""

    // ML Kit GenerateContentRequest doesn't support promptPrefix with image input.
    // Prepend system text to the main text if image is present, otherwise use promptPrefix
    // for caching benefits.
    val shouldUsePromptPrefix = systemText.isNotEmpty() && imagePart == null
    val promptPrefix = if (shouldUsePromptPrefix) PromptPrefix(systemText) else null
    val textPart =
      TextPart(
        if (shouldUsePromptPrefix || systemText.isEmpty()) {
          allText
        } else {
          "$systemText$instructionSeparator$allText".trim()
        }
      )

    val builder =
      if (imagePart != null) {
        GenerateContentRequest.builder(imagePart, textPart)
      } else {
        GenerateContentRequest.builder(textPart)
      }

    builder.apply {
      config.temperature?.let { temperature = it }
      config.topK?.let { topK = it }
      config.candidateCount?.let { candidateCount = it }
      config.maxOutputTokens?.let { maxOutputTokens = it }
      promptPrefix?.let { this.promptPrefix = it }
    }

    return builder.build()
  }

  /**
   * Converts a [GenerateContentResponse] to an [LlmResponse].
   *
   * Only the first candidate is used. If no candidate is returned, an error message is set.
   *
   * Error message is also set in case a finish reason is present and it is not STOP.
   *
   * @return The [LlmResponse] containing the text from the first candidate and the finish reason if
   *   present.
   */
  internal fun GenerateContentResponse.toLlmResponse(): LlmResponse {
    return this.toAggregatedResponse().toLlmResponse()
  }

  /**
   * Converts a [AggregatedResponse] to an [LlmResponse].
   *
   * Only the first candidate is used. If no candidate is returned, an error message is set.
   *
   * Error message is also set in case a finish reason is present and it is not STOP.
   *
   * @return The [LlmResponse] containing the text from the first candidate and the finish reason if
   *   present.
   */
  internal fun AggregatedResponse.toLlmResponse(): LlmResponse {
    if (candidates.size > 1) {
      logger.warn {
        "Multiple candidates present in GenerateContentResponse. Only the first one will be used in the LlmResponse."
      }
    }

    val candidate = candidates.firstOrNull()
    val finishReason =
      candidate?.finishReason?.let {
        when (it) {
          Candidate.FinishReason.STOP -> FinishReason.STOP
          Candidate.FinishReason.MAX_TOKENS -> FinishReason.MAX_TOKENS
          else -> FinishReason.OTHER
        }
      }

    return LlmResponse(
      content = candidate?.let { Content(role = Role.MODEL, parts = listOf(Part(text = it.text))) },
      finishReason = finishReason,
      errorMessage =
        when {
          candidate == null -> "No candidates returned."
          finishReason != null && finishReason != FinishReason.STOP ->
            "Generation finished with reason: $finishReason"
          else -> null
        },
    )
  }
}
