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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Shown under the transcript until the weights are present: a download button, then its progress.
 */
@Composable
internal fun ModelSetupBar(progress: Float?, onDownload: () -> Unit) {
  Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
    if (progress == null) {
      Button(onClick = onDownload, modifier = Modifier.fillMaxWidth()) {
        Text("Download model (${LiteRtLmModelStore.DOWNLOAD_SIZE_LABEL})")
      }
    } else {
      Text(
        "Downloading model… ${(progress * 100).roundToInt()}%",
        style = MaterialTheme.typography.bodyMedium,
      )
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
      )
    }
  }
}
