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

package com.google.adk.kt.examples.android.litertlmchat

import android.content.Context
import com.google.adk.kt.agents.Instruction
import com.google.adk.kt.agents.LlmAgent
import com.google.adk.kt.litertlm.LiteRtLmModel
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.EngineConfig
import java.io.File

/**
 * Builds the on-device model and the [LlmAgent] used by [LiteRtLmChatActivity]. The model is
 * created separately because it owns a native engine, whose lifetime the activity ties to its own.
 */
internal object LiteRtLmChatAgent {
  const val NAME: String = "litertlm_chat_agent"

  /**
   * Opens [modelFile] on the CPU backend; the returned model owns a native engine and must be
   * closed. [cacheDir] puts the compiled-model cache where the system can reclaim it.
   */
  fun createModel(modelFile: File, cacheDir: File): LiteRtLmModel =
    LiteRtLmModel.create(
      EngineConfig(
        modelPath = modelFile.absolutePath,
        backend = Backend.CPU(),
        cacheDir = cacheDir.absolutePath,
      ),
      name = modelFile.name,
    )

  /** Builds the agent around an already-created [model], with [context]'s device tools. */
  fun create(model: LiteRtLmModel, context: Context): LlmAgent =
    LlmAgent(
      name = NAME,
      model = model,
      instruction =
        Instruction(
          """
          You are a helpful assistant running entirely on this device. Keep replies to one or two
          short sentences. Call get_battery_level when the user asks about the battery, and
          get_device_info when they ask what device this is, then state the exact value the tool
          returned.
          """
            .trimIndent()
        ),
      tools = DeviceTools(context).generatedTools(),
    )
}
