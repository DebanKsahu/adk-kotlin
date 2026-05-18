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

package com.google.adk.kt.tools

import com.google.adk.kt.types.FunctionDeclaration
import com.google.adk.kt.types.Schema
import com.google.adk.kt.types.Type
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.StringWriter
import kotlin.jvm.JvmName
import org.kxml2.io.KXmlSerializer

/**
 * Generates a text description of the function tools for use in an LLM prompt.
 *
 * @param format The format in which to render the tool descriptions (e.g., XML or JSON).
 * @return A formatted string describing the provided tools and their parameters.
 */
@JvmName("toPromptDescriptionFromDeclarations")
internal fun Iterable<FunctionDeclaration>.toPromptDescription(
  format: PromptFormat = PromptFormat.XML
): String {
  return when (format) {
    PromptFormat.XML -> toXmlPromptDescription()
    PromptFormat.JSON -> toJsonPromptDescription()
  }
}

/**
 * Generates a text description of the function tools for use in an LLM prompt.
 *
 * @param format The format in which to render the tool descriptions (e.g., XML or JSON).
 * @return A formatted string describing the provided tools and their parameters.
 */
@JvmName("toPromptDescriptionFromTools")
internal fun Iterable<FunctionTool>.toPromptDescription(
  format: PromptFormat = PromptFormat.XML
): String {
  return this.mapNotNull { it.declaration() }.toPromptDescription(format)
}

private fun Iterable<FunctionDeclaration>.toXmlPromptDescription(): String {
  val writer = StringWriter()
  val serializer = KXmlSerializer()
  serializer.setOutput(writer)
  serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)

  serializer.startTag(null, "tools")
  for (declaration in this@toXmlPromptDescription) {
    serializer.startTag(null, "tool")

    serializer.startTag(null, "name")
    serializer.text(declaration.name)
    serializer.endTag(null, "name")

    serializer.startTag(null, "description")
    serializer.text(declaration.description)
    serializer.endTag(null, "description")

    val parameters = declaration.parameters
    if (parameters?.properties?.isNotEmpty() == true) {
      serializer.startTag(null, "parameters")
      schemaToXml(parameters, serializer)
      serializer.endTag(null, "parameters")
    }

    serializer.endTag(null, "tool")
  }
  serializer.endTag(null, "tools")
  serializer.flush()

  return writer.toString()
}

private fun Iterable<FunctionDeclaration>.toJsonPromptDescription(): String {
  val jsonArray = JsonArray()
  for (declaration in this) {
    val toolObj = JsonObject()
    toolObj.addProperty("name", declaration.name)
    toolObj.addProperty("description", declaration.description)

    val parameters = declaration.parameters
    if (parameters?.properties?.isNotEmpty() == true) {
      toolObj.add("parameters", schemaToJsonObject(parameters))
    }
    jsonArray.add(toolObj)
  }
  return jsonArray.toString()
}

private fun schemaToJsonObject(schema: Schema): JsonObject {
  val jsonObj = JsonObject()
  val typeStr = schema.type?.name?.lowercase() ?: "string"
  jsonObj.addProperty("type", typeStr)

  if (schema.description != null) {
    jsonObj.addProperty("description", schema.description)
  }

  when (schema.type) {
    Type.OBJECT -> {
      if (schema.properties != null) {
        val propsObj = JsonObject()
        for ((name, propSchema) in schema.properties) {
          propsObj.add(name, schemaToJsonObject(propSchema))
        }
        jsonObj.add("properties", propsObj)

        if (schema.required != null && schema.required.isNotEmpty()) {
          val reqArray = JsonArray()
          schema.required.forEach { reqArray.add(it) }
          jsonObj.add("required", reqArray)
        }
      }
    }
    Type.ARRAY -> {
      if (schema.items != null) {
        jsonObj.add("items", schemaToJsonObject(schema.items))
      }
    }
    else -> {}
  }

  return jsonObj
}

private fun schemaToXml(schema: Schema, serializer: KXmlSerializer) {
  when (schema.type) {
    Type.OBJECT -> {
      if (schema.properties != null) {
        for ((name, propSchema) in schema.properties) {
          serializer.startTag(null, "parameter")

          serializer.startTag(null, "name")
          serializer.text(name)
          serializer.endTag(null, "name")

          val propType = propSchema.type?.name?.lowercase() ?: "string"
          serializer.startTag(null, "type")
          serializer.text(propType)
          serializer.endTag(null, "type")

          if (propSchema.description != null) {
            serializer.startTag(null, "description")
            serializer.text(propSchema.description)
            serializer.endTag(null, "description")
          }

          val required = schema.required?.contains(name) == true
          serializer.startTag(null, "required")
          serializer.text(required.toString())
          serializer.endTag(null, "required")

          if (propSchema.type == Type.OBJECT || propSchema.type == Type.ARRAY) {
            serializer.startTag(null, "schema")
            schemaToXml(propSchema, serializer)
            serializer.endTag(null, "schema")
          }

          serializer.endTag(null, "parameter")
        }
      }
    }
    Type.ARRAY -> {
      if (schema.items != null) {
        serializer.startTag(null, "items")

        val itemType = schema.items.type?.name?.lowercase() ?: "string"
        serializer.startTag(null, "type")
        serializer.text(itemType)
        serializer.endTag(null, "type")

        if (schema.items.type == Type.OBJECT || schema.items.type == Type.ARRAY) {
          serializer.startTag(null, "schema")
          schemaToXml(schema.items, serializer)
          serializer.endTag(null, "schema")
        }

        serializer.endTag(null, "items")
      }
    }
    else -> {}
  }
}
