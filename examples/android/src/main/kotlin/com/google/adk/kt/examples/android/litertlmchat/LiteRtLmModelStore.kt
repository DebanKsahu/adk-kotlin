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
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Supplies the `.litertlm` weights [LiteRtLmChatActivity] runs, kept out of the activity so it
 * shows only how ADK is used. LiteRT-LM takes a file path and does not fetch models itself, so this
 * downloads one on first use; a file already in the directory is used as is, so `adb push` works
 * too. See the app README.md, which also covers what a shipping app should do instead.
 */
internal object LiteRtLmModelStore {

  /**
   * The model to fetch; any tool-capable `.litertlm` model works. Repos behind a license need a
   * token this sample does not implement, so push those with `adb` instead.
   */
  private const val REPO = "litert-community/gemma-4-E2B-it-litert-lm"
  private const val FILE_NAME = "gemma-4-E2B-it.litertlm"
  private const val REVISION = "main"

  /** Approximate download size, so the prompt can say what it is about to cost. */
  const val DOWNLOAD_SIZE_LABEL: String = "2.5 GB"

  private const val EXTENSION = ".litertlm"
  private const val PARTIAL_SUFFIX = ".part"

  /** Emit at most this many progress updates, rather than one per buffer. */
  private const val PROGRESS_STEPS = 200

  private const val TIMEOUT_MILLIS = 30_000

  /**
   * The model to run, or null if none has been downloaded or pushed yet. A model you supplied
   * yourself wins over the downloaded one, so pushing a file overrides it without having to delete
   * anything; between several of your own, the first by name wins.
   */
  fun find(context: Context): File? {
    val candidates =
      directory(context)
        .listFiles { file -> file.isFile && file.name.endsWith(EXTENSION) }
        .orEmpty()
        .sortedBy { it.name }
    return candidates.firstOrNull { it.name != FILE_NAME } ?: candidates.firstOrNull()
  }

  /**
   * Downloads [FILE_NAME], emitting the fraction completed. The bytes go to a temporary file that
   * is renamed only once the transfer finishes, so an interrupted download is never mistaken for a
   * usable model; leaving the screen cancels it, and retrying starts over.
   */
  fun download(context: Context): Flow<Float> =
    flow {
        emit(0f)
        val partial = File(directory(context), FILE_NAME + PARTIAL_SUFFIX)
        try {
          val connection = URL(downloadUrl()).openConnection() as HttpURLConnection
          connection.connectTimeout = TIMEOUT_MILLIS
          connection.readTimeout = TIMEOUT_MILLIS
          try {
            // Without this an error page would be written out as if it were the model.
            check(connection.responseCode == HttpURLConnection.HTTP_OK) {
              "Model download failed with HTTP ${connection.responseCode}."
            }
            val total = connection.contentLengthLong
            val step = if (total > 0) total / PROGRESS_STEPS else Long.MAX_VALUE
            var copied = 0L
            var reported = 0L
            connection.inputStream.use { input ->
              partial.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                  coroutineContext.ensureActive()
                  val read = input.read(buffer)
                  if (read < 0) break
                  output.write(buffer, 0, read)
                  copied += read
                  if (copied - reported >= step) {
                    reported = copied
                    emit(copied.toFloat() / total)
                  }
                }
              }
            }
          } finally {
            connection.disconnect()
          }
          check(partial.renameTo(File(directory(context), FILE_NAME))) {
            "Downloaded model could not be moved into place."
          }
        } catch (t: Throwable) {
          // Gigabytes of a half-finished model are not worth keeping for a retry that restarts.
          val unused = partial.delete()
          throw t
        }
        emit(1f)
      }
      .flowOn(Dispatchers.IO)

  /** The `adb push` destination, for the hint shown alongside the download prompt. */
  fun pushDirectory(context: Context): String = directory(context).absolutePath

  private fun directory(context: Context): File =
    context.getExternalFilesDir(null) ?: context.filesDir

  private fun downloadUrl(): String = "https://huggingface.co/$REPO/resolve/$REVISION/$FILE_NAME"
}
