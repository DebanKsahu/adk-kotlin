/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.adk.kt.webserver

import com.google.adk.kt.artifacts.ArtifactService
import com.google.adk.kt.runners.Runner
import com.google.adk.kt.sessions.SessionService
import com.google.adk.kt.webserver.AdkWebServer.StatusAwareLogger
import com.google.adk.kt.webserver.loaders.AgentLoader
import com.google.adk.kt.webserver.models.RunResponse
import com.google.adk.kt.webserver.routes.appRoutes
import com.google.adk.kt.webserver.routes.artifactRoutes
import com.google.adk.kt.webserver.routes.debugRoutes
import com.google.adk.kt.webserver.routes.evalRoutes
import com.google.adk.kt.webserver.routes.graphRoutes
import com.google.adk.kt.webserver.routes.runRoutes
import com.google.adk.kt.webserver.routes.sessionRoutes
import com.google.adk.kt.webserver.routes.staticRoutes
import com.google.adk.kt.webserver.telemetry.ApiServerSpanExporter
import com.google.adk.kt.webserver.telemetry.OpenTelemetryConfig
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import kotlinx.datetime.Instant
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

class AdkWebServer(
  private val port: Int = 8080,
  private val sessionService: SessionService,
  private val artifactService: ArtifactService,
  private val runner: Runner,
  private val agentLoader: AgentLoader,
  private val apiServerSpanExporter: ApiServerSpanExporter,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(AdkWebServer::class.java)
  }

  private var server: EmbeddedServer<*, *>? = null

  fun start(wait: Boolean = false) {
    if (server != null) return

    server =
      embeddedServer(Netty, port = port) {
          adkModule(sessionService, artifactService, runner, agentLoader, apiServerSpanExporter)
        }
        .start(wait = wait)
    logger.info("Ktor server started on port $port")
  }

  fun stop() {
    server?.stop(1000, 5000)
    server = null
    logger.info("Ktor server stopped")
  }

  class InstantTypeAdapter : TypeAdapter<Instant>() {
    override fun write(out: JsonWriter, value: Instant?) {
      if (value == null) {
        out.nullValue()
      } else {
        out.value(value.toEpochMilliseconds())
      }
    }

    override fun read(reader: JsonReader): Instant? {
      if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
        reader.nextNull()
        return null
      }
      return Instant.fromEpochMilliseconds(reader.nextLong())
    }
  }

  public class StatusAwareLogger(private val delegate: Logger) : Logger by delegate {
    override fun info(msg: String?) {
      if (msg != null && msg.contains("Status: 5")) {
        delegate.warn(msg)
      } else {
        delegate.info(msg)
      }
    }
  }
}

fun Application.adkModule(
  sessionService: SessionService,
  artifactService: ArtifactService,
  runner: Runner,
  agentLoader: AgentLoader,
  apiServerSpanExporter: ApiServerSpanExporter,
) {
  install(CallLogging) {
    level = Level.INFO
    logger = StatusAwareLogger(LoggerFactory.getLogger(CallLogging::class.java))
    format { call ->
      val status = call.response.status()
      val httpMethod = call.request.httpMethod.value
      val uri = call.request.uri
      "Status: $status, HTTP method: $httpMethod, URI: $uri"
    }
  }
  install(SSE)
  install(ContentNegotiation) {
    gson {
      setPrettyPrinting()
      registerTypeAdapter(Instant::class.java, AdkWebServer.InstantTypeAdapter())
    }
  }

  val otelConfig = OpenTelemetryConfig(apiServerSpanExporter)
  val sdkTracerProvider = otelConfig.sdkTracerProvider()
  otelConfig.openTelemetrySdk(sdkTracerProvider)

  routing {
    get("/api/health") { call.respondText("OK") }
    appRoutes(agentLoader)
    artifactRoutes(artifactService)
    debugRoutes(apiServerSpanExporter)
    evalRoutes()
    graphRoutes(agentLoader, sessionService)
    runRoutes(agentLoader, sessionService, artifactService)
    sessionRoutes(sessionService)
    staticRoutes(this@adkModule)
  }
}
