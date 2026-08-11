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

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.adk.kt.agents.RunConfig
import com.google.adk.kt.agents.StreamingMode
import com.google.adk.kt.events.Event
import com.google.adk.kt.examples.android.common.ScopedExampleActivity
import com.google.adk.kt.examples.android.common.foldTextParts
import com.google.adk.kt.examples.android.common.ui.AdkExamplesTheme
import com.google.adk.kt.examples.android.common.ui.ChatAuthor
import com.google.adk.kt.examples.android.common.ui.ChatMessage
import com.google.adk.kt.examples.android.common.ui.ChatScreen
import com.google.adk.kt.litertlm.LiteRtLmModel
import com.google.adk.kt.runners.InMemoryRunner
import com.google.adk.kt.sessions.InMemorySessionService
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.Part
import com.google.adk.kt.types.Role
import java.io.File
import kotlin.concurrent.thread
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

/**
 * Minimal Android example: a multi-turn chat with an on-device ADK agent backed by LiteRT-LM
 * ([LiteRtLmModel]), calling [DeviceTools] to answer from real device state. The "Stream" toggle
 * picks the [RunConfig.streamingMode]: [StreamingMode.SSE] grows the reply bubble from partial
 * chunks, [StreamingMode.NONE] appends the aggregated turn.
 *
 * The weights are a file the app supplies, so the first run downloads one (see [LiteRtLmModelStore]
 * and the app README.md). Nothing leaves the device and no API key is needed.
 */
class LiteRtLmChatActivity : ScopedExampleActivity() {

  private val sessionService = InMemorySessionService()

  // Built off the main thread: loading the weights takes seconds.
  private var runner: InMemoryRunner? = null

  private val messages = mutableStateListOf<ChatMessage>()
  private var inputEnabled by mutableStateOf(false)
  private var streaming by mutableStateOf(true)
  private var modelMissing by mutableStateOf(false)
  private var downloadProgress by mutableStateOf<Float?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AdkExamplesTheme {
        ChatScreen(
          title = "LiteRT-LM chat",
          messages = messages,
          inputEnabled = inputEnabled,
          onSend = ::sendToAgent,
          onBack = ::finish,
          streaming = streaming,
          onStreamingChange = { streaming = it },
          footer = {
            if (modelMissing) ModelSetupBar(downloadProgress, onDownload = ::downloadModel)
          },
        )
      }
    }

    // Off the main thread: looking for the model touches the filesystem.
    scope.launch(Dispatchers.IO) {
      val modelFile = LiteRtLmModelStore.find(applicationContext)
      if (modelFile == null) {
        addSystem(
          "This example needs a LiteRT-LM model (about " +
            "${LiteRtLmModelStore.DOWNLOAD_SIZE_LABEL}). Download it below, or push your own:\n\n" +
            "adb push your-model.litertlm ${LiteRtLmModelStore.pushDirectory(applicationContext)}/"
        )
        runOnUiThread { modelMissing = true }
      } else {
        loadModel(modelFile)
      }
    }
  }

  /** Fetches the weights, then loads them; the button that calls this is hidden while it runs. */
  private fun downloadModel() {
    if (downloadProgress != null) return
    downloadProgress = 0f
    scope.launch(Dispatchers.IO) {
      try {
        LiteRtLmModelStore.download(applicationContext).collect { fraction ->
          runOnUiThread { downloadProgress = fraction }
        }
        val modelFile = checkNotNull(LiteRtLmModelStore.find(applicationContext))
        runOnUiThread {
          modelMissing = false
          downloadProgress = null
        }
        loadModel(modelFile)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        runOnUiThread { downloadProgress = null }
        addSystem("Download failed: ${e.message ?: e::class.simpleName}")
      }
    }
  }

  private fun loadModel(modelFile: File) {
    // Dispatchers.IO: reading the weights blocks this thread for seconds.
    scope.launch(Dispatchers.IO) {
      addSystem("Loading ${modelFile.name} (this takes a few seconds)…")
      initRunner(modelFile)
    }
  }

  private fun initRunner(modelFile: File) {
    try {
      val litertLmModel = LiteRtLmChatAgent.createModel(modelFile, cacheDir)
      // Load now, not on the first message, so a broken file is reported as such.
      litertLmModel.engine.initialize()
      // Only now is there a native engine to release; closing one that failed to open throws.
      releaseWithScope(litertLmModel)
      runner =
        InMemoryRunner(
          agent = LiteRtLmChatAgent.create(litertLmModel, applicationContext),
          appName = APP_NAME,
          sessionService = sessionService,
        )
      addSystem(
        "Model ready. Try: \"What is my battery level?\" or \"What phone is this?\" — the agent " +
          "answers those by calling a tool."
      )
      runOnUiThread { inputEnabled = true }
    } catch (e: Throwable) {
      // Throwable: a device with no native binary fails with UnsatisfiedLinkError.
      addSystem("Could not load the model: ${e.message ?: e::class.simpleName}")
      // Offer setup again; a pushed model wins, so that one must be deleted by hand.
      runOnUiThread { modelMissing = true }
    }
  }

  /**
   * Releases [model]'s native engine once the activity scope completes, which is after `onDestroy`
   * cancelled it and no turn is still running. Its own thread, because releasing is slow; failures
   * are swallowed, because an uncaught one here would take the process down.
   */
  private fun releaseWithScope(model: LiteRtLmModel) {
    scope.coroutineContext.job.invokeOnCompletion {
      thread(name = "litertlm-close") {
        try {
          model.close()
        } catch (_: Throwable) {
          // Nothing to report to: the screen this belonged to is already gone.
        }
      }
    }
  }

  private fun sendToAgent(text: String) {
    val activeRunner = runner ?: return
    val useStreaming = streaming
    add(ChatAuthor.USER, text)
    // Lock the input for the duration of the turn: the engine holds one conversation at a time.
    runOnUiThread { inputEnabled = false }

    // Dispatchers.IO: generating blocks this thread until the turn ends, longer than loading does.
    scope.launch(Dispatchers.IO) {
      try {
        val events =
          activeRunner.runAsync(
            userId = USER_ID,
            sessionId = SESSION_ID,
            newMessage = Content(role = Role.USER, parts = listOf(Part(text = text))),
            runConfig =
              RunConfig(
                streamingMode = if (useStreaming) StreamingMode.SSE else StreamingMode.NONE,
                // A small model can loop on a tool, and the input stays locked for the turn.
                maxLlmCalls = MAX_LLM_CALLS,
              ),
          )

        val partial = StringBuilder()
        val bubble = AgentBubble()
        events.collect { event ->
          val isAgent = event.author == AGENT_NAME
          val chunk = if (isAgent) event.foldTextParts() else ""
          val isPartial = event.partial
          runOnUiThread {
            if (isAgent) {
              if (isPartial) {
                // SSE mode: grow one bubble from the partial deltas as they arrive.
                if (chunk.isNotEmpty()) {
                  partial.append(chunk)
                  bubble.show(partial.toString())
                }
              } else {
                // The aggregated event ends the turn: prefer its authoritative text.
                val finalText = chunk.ifBlank { partial.toString() }.trim()
                if (finalText.isNotEmpty()) bubble.show(finalText)
                partial.setLength(0)
                bubble.endTurn()
              }
            }
            // After the bubble, so the tool line follows the text the model emitted alongside it.
            reportAgentActivity(event)
          }
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        addSystem("Error: ${e.message ?: e::class.simpleName}")
      } finally {
        runOnUiThread { inputEnabled = true }
      }
    }
  }

  /**
   * Surfaces which tool the agent called and any error it reported instead of an answer. Partial
   * events are skipped, because they repeat the same call.
   */
  private fun reportAgentActivity(event: Event) {
    if (event.partial) return
    event.errorMessage?.let { addSystem("Model error: $it") }
    for (part in event.content?.parts.orEmpty()) {
      part.functionCall?.name?.let { addSystem("Calling tool: $it") }
    }
  }

  /**
   * The reply bubble for the turn in progress: added on the first text, updated in place after. A
   * tool call splits a reply into two turns, so [endTurn] gives the next one its own bubble. UI
   * thread only.
   */
  private inner class AgentBubble {
    private var index = -1

    fun show(text: String) {
      if (index < 0) {
        messages.add(ChatMessage(ChatAuthor.AGENT, text, AGENT_NAME))
        index = messages.lastIndex
      } else {
        messages[index] = messages[index].copy(text = text)
      }
    }

    fun endTurn() {
      index = -1
    }
  }

  private fun add(author: ChatAuthor, text: String) {
    val label = if (author == ChatAuthor.AGENT) AGENT_NAME else ""
    runOnUiThread { messages.add(ChatMessage(author, text, label)) }
  }

  private fun addSystem(text: String) {
    runOnUiThread { messages.add(ChatMessage(ChatAuthor.SYSTEM, text)) }
  }

  private companion object {
    const val APP_NAME = "LiteRtLmChatExample"
    const val USER_ID = "local-user"
    const val SESSION_ID = "local-session"
    const val AGENT_NAME = LiteRtLmChatAgent.NAME
    const val MAX_LLM_CALLS = 8
  }
}
