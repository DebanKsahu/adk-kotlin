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

package com.google.adk.kt.tools.mcp

/**
 * The intended consumer of a resource, from an MCP `annotations.audience` entry.
 *
 * Modeled as a string rather than an enum: the MCP spec defines `user` and `assistant` today, and
 * an unrecognized role from a newer server must survive the round trip rather than fail parsing.
 */
@JvmInline internal value class McpRole(val value: String)

/**
 * Optional hints an MCP server attaches to a resource or template to say who it is for and how
 * important it is.
 *
 * @property audience Who the object is intended for; empty when the server said nothing.
 * @property priority How important the object is, between 0.0 (least) and 1.0 (most), or `null` if
 *   the server did not rank it.
 * @property lastModified The server-declared modification time (ISO 8601), if any.
 */
internal data class McpAnnotations(
  val audience: List<McpRole> = emptyList(),
  val priority: Double? = null,
  val lastModified: String? = null,
)

/**
 * A resource advertised by an MCP server, as returned by [McpToolset.listResources].
 *
 * This is the ADK-owned view of a `resources/list` entry; it does not leak the MCP SDK type. Every
 * field the MCP schema defines for a resource is carried, so nothing the server sent is lost.
 *
 * @property name The programmatic identifier for the resource. Not guaranteed to be unique across
 *   the server, so [uri] (not [name]) is the identifier to pass to [McpToolset.readResource].
 * @property uri The canonical, unambiguous identifier for the resource. MCP treats this as an
 *   opaque, server-interpreted token, so it is kept as the exact string the server returned.
 * @property title The display name for the resource. Per the MCP spec this, not [name], is the
 *   human-readable label; fall back to [name] when it is `null`.
 * @property description An optional human-readable description of the resource.
 * @property mimeType The MIME type of the resource, if the server declared one.
 * @property size The size of the raw resource content in bytes, before base64 encoding, if the
 *   server declared one.
 * @property annotations Server hints about audience and priority, or `null` if it sent none.
 * @property meta The resource's `_meta` object verbatim, or `null` if the server sent none.
 */
internal data class McpResourceInfo(
  val name: String,
  val uri: String,
  val title: String? = null,
  val description: String? = null,
  val mimeType: String? = null,
  val size: Long? = null,
  val annotations: McpAnnotations? = null,
  val meta: Map<String, Any?>? = null,
)

/**
 * A single page of resources returned by [McpToolset.listResources].
 *
 * @property resources The resources on this page.
 * @property nextCursor An opaque cursor for fetching the next page, or `null` if this is the last
 *   page. Pass it back to [McpToolset.listResources] to continue paginating.
 */
internal data class McpResourceListing(
  val resources: List<McpResourceInfo>,
  val nextCursor: String? = null,
)

/**
 * A resource template advertised by an MCP server, as returned by
 * [McpToolset.listResourceTemplates].
 *
 * Unlike [McpResourceInfo], a template carries a [uriTemplate] (an RFC 6570 URI template such as
 * `file:///{path}`) rather than a concrete URI: it must be expanded with variables before it names
 * a readable resource, so it is intentionally *not* interchangeable with [McpResourceInfo.uri].
 *
 * @property name The programmatic identifier for the template.
 * @property uriTemplate The RFC 6570 URI template used to construct concrete resource URIs.
 * @property title The display name for the template; fall back to [name] when it is `null`.
 * @property description An optional human-readable description of the template.
 * @property mimeType The MIME type shared by resources matching this template, if declared.
 * @property annotations Server hints about audience and priority, or `null` if it sent none.
 * @property meta The template's `_meta` object verbatim, or `null` if the server sent none.
 */
internal data class McpResourceTemplateInfo(
  val name: String,
  val uriTemplate: String,
  val title: String? = null,
  val description: String? = null,
  val mimeType: String? = null,
  val annotations: McpAnnotations? = null,
  val meta: Map<String, Any?>? = null,
)

/**
 * A single page of resource templates returned by [McpToolset.listResourceTemplates].
 *
 * @property resourceTemplates The templates on this page.
 * @property nextCursor An opaque cursor for fetching the next page, or `null` if this is the last
 *   page. Pass it back to [McpToolset.listResourceTemplates] to continue paginating.
 */
internal data class McpResourceTemplateListing(
  val resourceTemplates: List<McpResourceTemplateInfo>,
  val nextCursor: String? = null,
)

/**
 * The contents of a resource read from an MCP server, as returned by [McpToolset.readResource].
 *
 * A resource resolves to either text ([Text]) or binary data ([Blob]); this sealed type models both
 * so callers handle them exhaustively without leaking the MCP SDK type.
 */
internal sealed interface McpResourceContent {
  /** The URI of the resource these contents came from. */
  val uri: String

  /** The MIME type of the contents, if the server declared one. */
  val mimeType: String?

  /** The contents' `_meta` object verbatim, or `null` if the server sent none. */
  val meta: Map<String, Any?>?

  /**
   * Text contents of a resource.
   *
   * @property text The text of the resource.
   */
  data class Text(
    override val uri: String,
    override val mimeType: String?,
    val text: String,
    override val meta: Map<String, Any?>? = null,
  ) : McpResourceContent

  /**
   * Binary contents of a resource.
   *
   * @property blobBase64 The binary data of the resource, base64-encoded exactly as the server
   *   returned it.
   */
  data class Blob(
    override val uri: String,
    override val mimeType: String?,
    val blobBase64: String,
    override val meta: Map<String, Any?>? = null,
  ) : McpResourceContent
}
