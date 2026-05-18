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

package com.google.adk.kt.models

import com.google.adk.kt.types.Candidate
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.FunctionCall
import com.google.adk.kt.types.GenerateContentResponse
import com.google.adk.kt.types.Part
import com.google.adk.kt.types.PartialArg
import com.google.adk.kt.types.PartialArgValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest

class StreamingResponseAggregatorTest {

  @Test
  fun testTextMerging() = runBlocking {
    val aggregator = StreamingResponseAggregator()

    val unused1 = aggregator.processResponse(createResp("Hello "))
    val unused2 = aggregator.processResponse(createResp("world!"))
    val finalResp = aggregator.aggregate()

    assertNotNull(finalResp)
    assertEquals(1, finalResp.content?.parts?.size)
    assertEquals("Hello world!", finalResp.content?.parts?.get(0)?.text)
  }

  @Test
  fun testThoughtMerging() = runBlocking {
    val aggregator = StreamingResponseAggregator()

    val unused1 = aggregator.processResponse(createResp("Thinking...", thought = true))
    val unused2 = aggregator.processResponse(createResp(" Done.", thought = true))
    val finalResp = aggregator.aggregate()

    assertNotNull(finalResp)
    assertEquals(1, finalResp.content?.parts?.size)
    assertEquals("Thinking... Done.", finalResp.content?.parts?.get(0)?.text)
    assertTrue(finalResp.content?.parts?.get(0)?.thought == true)
  }

  @Test
  fun testMixedTextAndThought() = runBlocking {
    val aggregator = StreamingResponseAggregator()

    val unused1 = aggregator.processResponse(createResp("Think", thought = true))
    val unused2 = aggregator.processResponse(createResp("ing", thought = true))
    val unused3 = aggregator.processResponse(createResp("Hello"))
    val finalResp = aggregator.aggregate()

    assertNotNull(finalResp)
    assertEquals(2, finalResp.content?.parts?.size)
    assertEquals("Thinking", finalResp.content?.parts?.get(0)?.text)
    assertEquals(true, finalResp.content?.parts?.get(0)?.thought)
    assertEquals("Hello", finalResp.content?.parts?.get(1)?.text)
    assertEquals(null, finalResp.content?.parts?.get(1)?.thought)
  }

  @Test
  fun testPartialFunctionCallAggregation() = runBlocking {
    val aggregator = StreamingResponseAggregator()

    val unused1 =
      aggregator.processResponse(
        createFcResp(createPartialFc("get_weather", "$.location", "San ", willContinue = true))
      )
    val unused2 =
      aggregator.processResponse(
        createFcResp(createPartialFc(null, "$.location", "Francisco", willContinue = false))
      )
    val finalResp = aggregator.aggregate()

    assertNotNull(finalResp)
    assertEquals(1, finalResp.content?.parts?.size)
    val fc = finalResp.content?.parts?.get(0)?.functionCall
    assertNotNull(fc)
    assertEquals("get_weather", fc.name)
    assertEquals("San Francisco", fc.args["location"])
  }

  @Test
  fun testNestedPartialFunctionCallAggregation() = runBlocking {
    val aggregator = StreamingResponseAggregator()

    val unused1 =
      aggregator.processResponse(
        createFcResp(
          createPartialFc("find_place", "$.location.city", "Mountain ", willContinue = true)
        )
      )
    val unused2 =
      aggregator.processResponse(
        createFcResp(createPartialFc(null, "$.location.city", "View", willContinue = true))
      )
    val unused3 =
      aggregator.processResponse(
        createFcResp(createPartialFc(null, "$.location.state", "CA", willContinue = false))
      )
    val finalResp = aggregator.aggregate()

    assertNotNull(finalResp)
    assertEquals(1, finalResp.content?.parts?.size)
    val fc = finalResp.content?.parts?.get(0)?.functionCall
    assertNotNull(fc)
    assertEquals("find_place", fc.name)
    val location = fc.args["location"] as Map<*, *>
    assertEquals("Mountain View", location["city"])
    assertEquals("CA", location["state"])
  }

  @Test
  fun processResponse_concurrentCalls_isThreadSafe() = runTest {
    val aggregator = StreamingResponseAggregator()
    val jobCount = 100
    val chunks = (0 until jobCount).map { "$it;" }

    coroutineScope {
      for (chunk in chunks) {
        launch(Dispatchers.Default) {
          val unused = aggregator.processResponse(createResp(chunk))
        }
      }
    }

    val finalResponse = aggregator.aggregate()

    assertNotNull(finalResponse)
    assertEquals(1, finalResponse.content?.parts?.size)
    val resultText = finalResponse.content?.parts?.get(0)?.text

    // The result should contain all chunks, but potentially in a jumbled order
    val resultNumbers =
      resultText?.split(';')?.filter { it.isNotBlank() }?.map { it.toInt() }?.sorted()
    assertEquals((0 until jobCount).toList(), resultNumbers)
  }

  private fun createResp(text: String, thought: Boolean? = null): GenerateContentResponse {
    return GenerateContentResponse(
      candidates =
        listOf(Candidate(content = Content(parts = listOf(Part(text = text, thought = thought)))))
    )
  }

  private fun createFcResp(fc: FunctionCall): GenerateContentResponse {
    return GenerateContentResponse(
      candidates = listOf(Candidate(content = Content(parts = listOf(Part(functionCall = fc)))))
    )
  }

  private fun createPartialFc(
    name: String? = null,
    jsonPath: String,
    stringValue: String,
    willContinue: Boolean,
  ): FunctionCall {
    return FunctionCall(
      name = name ?: "",
      partialArgs =
        listOf(PartialArg(jsonPath = jsonPath, value = PartialArgValue.StringValue(stringValue))),
      willContinue = willContinue,
    )
  }
}
