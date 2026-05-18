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

package com.google.adk.kt.models.mlkit

import com.google.adk.kt.logging.LoggerFactory
import com.google.adk.kt.models.LlmRequest
import com.google.adk.kt.models.LlmResponse
import com.google.adk.kt.models.Model
import com.google.adk.kt.utils.mlkit.GenaiPromptConversions.toGenerateContentRequest
import com.google.adk.kt.utils.mlkit.GenaiPromptConversions.toLlmResponse
import com.google.adk.kt.utils.mlkit.GenerateContentResponseAggregator
import com.google.adk.kt.utils.mlkit.toAggregatedResponse
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.GenerateContentResponse
import com.google.mlkit.genai.prompt.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A [Model] implementation that uses the ML Kit GenAI API to generate content.
 *
 * @param generativeModel The [GenerativeModel] to use for generation.
 * @param name The name of the model.
 */
class GenaiPrompt
private constructor(val generativeModel: GenerativeModel, override val name: String) : Model {

  companion object {
    val logger = LoggerFactory.getLogger(GenaiPrompt::class)

    /**
     * Creates a [GenaiPrompt] instance with the given [generativeModel] and [name].
     *
     * @param generativeModel The [GenerativeModel] to use for generation.
     * @param name The name of the model.
     */
    fun create(generativeModel: GenerativeModel, name: String = "GenaiPrompt") =
      GenaiPrompt(generativeModel, name)

    private fun trace(request: GenerateContentRequest) = logger.trace {
      val imageTrace = request.image?.bitmap?.let { "${it.width}x${it.height}" } ?: "none"
      "generateContentRequest: text: ${request.text}, promptPrefix: ${request.promptPrefix}, image: ${imageTrace}"
    }

    private fun trace(response: GenerateContentResponse) = logger.trace {
      val candidate = response.candidates.firstOrNull()
      "generateContentResponse text: ${candidate?.text}, finishReason: ${candidate?.finishReason}"
    }
  }

  private suspend fun generateContentNonStreaming(request: LlmRequest): LlmResponse {
    return generativeModel
      .generateContent(request.toGenerateContentRequest().also { trace(it) })
      .also { trace(it) }
      .toLlmResponse()
      .also { logger.trace { "final response: ${it}" } }
  }

  private fun generateContentStreaming(request: LlmRequest): Flow<LlmResponse> = flow {
    val responseAggregator = GenerateContentResponseAggregator()
    generativeModel
      .generateContentStream(request.toGenerateContentRequest().also { trace(it) })
      .collect {
        responseAggregator.processResponse(it.toAggregatedResponse())
        emit(
          it
            .also { response -> trace(response) }
            .toLlmResponse()
            .copy(partial = true)
            .also { response -> logger.trace { "partial response: ${response}" } }
        )
      }

    emit(
      responseAggregator
        .aggregate()
        .also { response -> logger.trace { "aggregated response: ${response}" } }
        .toLlmResponse()
        .copy(partial = false)
        .also { response -> logger.trace { "final response: ${response}" } }
    )
  }

  override fun generateContent(request: LlmRequest, stream: Boolean): Flow<LlmResponse> {
    logger.trace { "request: ${request}, stream: ${stream}" }
    if (stream) {
      return generateContentStreaming(request)
    } else {
      return flow { emit(generateContentNonStreaming(request)) }
    }
  }
}
