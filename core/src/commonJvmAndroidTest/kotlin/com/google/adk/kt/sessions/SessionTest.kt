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

package com.google.adk.kt.sessions

import com.google.adk.kt.events.Event
import com.google.common.truth.Truth.assertThat
import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SessionTest {

  @Test
  fun constructor_initializesFieldsCorrectly() {
    val sessionId = "session-123"
    val appName = "test-app"
    val userId = "user-456"
    val state = State()
    val events = mutableListOf<Event>()
    val lastUpdateTime = Clock.System.now()

    val session = Session(SessionKey(appName, userId, sessionId), state, events, lastUpdateTime)

    assertThat(session.key.id).isEqualTo(sessionId)
    assertThat(session.key.appName).isEqualTo(appName)
    assertThat(session.key.userId).isEqualTo(userId)
    assertThat(session.state).isEqualTo(state)
    assertThat(session.events).isEqualTo(events)
    assertThat(session.lastUpdateTime).isEqualTo(lastUpdateTime)
  }

  @Test
  fun constructor_withMinimalArgs_initializesDefaults() {
    val sessionId = "session-123"
    val appName = "test-app"
    val userId = "user-456"

    val session = Session(SessionKey(appName, userId, sessionId))

    assertThat(session.state).isEmpty()
    assertThat(session.events).isEmpty()
    assertThat(session.lastUpdateTime).isEqualTo(Instant.fromEpochMilliseconds(0))
  }
}
