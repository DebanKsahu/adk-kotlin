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

package com.google.adk.kt.testing

import com.google.adk.kt.events.Event
import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout

/** Truth extensions for ADK. */
class EventIterableSubject(metadata: FailureMetadata, private val actual: Iterable<Event>?) :
  Subject(metadata, actual) {

  /** Asserts that at least one event has a part with text matching the given predicate. */
  fun hasAnyPartWithText(predicate: (String) -> Boolean) {
    val found =
      actual?.any { event ->
        event.content?.parts?.any { part -> part.text?.let(predicate) ?: false } ?: false
      } ?: false
    if (!found) {
      failWithActual(Fact.simpleFact("expected to have any part with matching text"))
    }
  }

  companion object {
    private val FACTORY =
      Factory<EventIterableSubject, Iterable<Event>> { metadata, actual ->
        EventIterableSubject(metadata, actual)
      }

    fun events(): Factory<EventIterableSubject, Iterable<Event>> = FACTORY
  }
}

/** Provides a [EventIterableSubject] for the given events. */
fun assertThatAdk(events: Iterable<Event>?): EventIterableSubject =
  assertAbout(EventIterableSubject.events()).that(events)
