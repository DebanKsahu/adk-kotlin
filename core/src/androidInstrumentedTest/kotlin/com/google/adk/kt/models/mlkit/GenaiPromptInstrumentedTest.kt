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

import android.util.Log
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.adk.kt.agents.Instruction
import com.google.adk.kt.agents.LlmAgent
import com.google.adk.kt.runners.InMemoryRunner
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.Part
import com.google.adk.kt.types.Role
import com.google.adk.kt.utils.mlkit.GenerativeModelHelpers
import com.google.common.truth.Truth.assertThat
import com.google.mlkit.genai.prompt.ModelConfig
import com.google.mlkit.genai.prompt.ModelPreference
import com.google.mlkit.genai.prompt.ModelReleaseStage
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenaiPromptInstrumentedTest {

  @get:Rule val rule = createComposeRule()

  @Test
  fun llmAgent_respondsToEarthQuestion_withoutErrors() {
    runTest {
      // 1) Build an ML Kit GenerativeModel via GenerativeModelHelpers.
      val generativeModel = GenerativeModelHelpers.initGenerativeModel {
        modelConfig =
          ModelConfig.builder()
            .apply {
              releaseStage = ModelReleaseStage.STABLE
              preference = ModelPreference.FAST
            }
            .build()
      }

      // 2) Wrap it in the ADK Model adapter and build a sample LlmAgent.
      val agent =
        LlmAgent(
          name = "earth_agent",
          model = GenaiPrompt.create(generativeModel, name = "gemini-nano"),
          instruction =
            Instruction(
              "You are a helpful assistant. Answer the user's question in one or two sentences."
            ),
        )

      // 3) Run the agent through an InMemoryRunner and collect every event.
      val runner = InMemoryRunner(agent = agent, appName = "GenaiPromptInstrumentedTestApp")
      val userQuestion = "Tell me something about planet Earth."
      val events =
        runner
          .runAsync(
            userId = "test-user",
            sessionId = "test-session",
            newMessage = Content(role = Role.USER, parts = listOf(Part(text = userQuestion))),
          )
          .toList()

      for (event in events) {
        Log.d(
          TAG,
          "event author=${event.author} text=${event.content?.parts?.joinToString { it.text ?: "" }} " +
            "errorCode=${event.errorCode} errorMessage=${event.errorMessage}",
        )
      }

      // 4) Verify no errors were reported on any event.
      assertThat(events).isNotEmpty()
      assertThat(events.mapNotNull { it.errorCode }).isEmpty()
      assertThat(events.mapNotNull { it.errorMessage }).isEmpty()

      // 5) Verify the agent produced a sensible final, model-authored response.
      val finalText =
        events
          .filter { it.isFinalResponse && it.author == agent.name }
          .flatMap { it.content?.parts.orEmpty() }
          .mapNotNull { it.text }
          .joinToString(separator = " ")
          .trim()

      Log.d(TAG, "final response text: $finalText")

      assertThat(finalText).isNotEmpty()
      // Sanity-check the answer is on-topic: it should mention "Earth" somewhere.
      assertThat(finalText.lowercase()).contains("earth")
    }
  }

  private companion object {
    const val TAG = "GenaiPromptInstrTest"
  }
}
