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

import com.google.adk.kt.tools.mcp.McpSchemaConverter.toAdkFunctionDeclaration
import com.google.adk.kt.tools.mcp.McpSchemaConverter.toAdkSchema
import com.google.adk.kt.types.Schema
import com.google.adk.kt.types.Type
import io.modelcontextprotocol.spec.McpSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class McpSchemaConverterTest {

  @Test
  fun parseTypeString_knownTypeName_returnsMatchingType() {
    assertEquals(Type.STRING, McpSchemaConverter.parseTypeString("string"))
    assertEquals(Type.INTEGER, McpSchemaConverter.parseTypeString("integer"))
    assertEquals(Type.NUMBER, McpSchemaConverter.parseTypeString("number"))
    assertEquals(Type.BOOLEAN, McpSchemaConverter.parseTypeString("boolean"))
    assertEquals(Type.ARRAY, McpSchemaConverter.parseTypeString("array"))
    assertEquals(Type.OBJECT, McpSchemaConverter.parseTypeString("object"))
  }

  @Test
  fun parseTypeString_nullTypeName_returnsNullType() {
    assertEquals(Type.NULL, McpSchemaConverter.parseTypeString("null"))
  }

  @Test
  fun parseTypeString_absentType_returnsTypeUnspecified() {
    assertEquals(Type.TYPE_UNSPECIFIED, McpSchemaConverter.parseTypeString(null))
  }

  @Test
  fun parseTypeString_unknownTypeName_throwsIllegalArgument() {
    assertFailsWith<IllegalArgumentException> { McpSchemaConverter.parseTypeString("unknown") }
  }

  // Type unions.

  @Test
  fun parsePropertyMap_typeUnionStartingWithNull_usesTheNonNullType() {
    val property = mapOf<String, Any>("type" to listOf("null", "string"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.STRING, converted.type)
  }

  @Test
  fun parsePropertyMap_typeUnionEndingWithNull_usesTheNonNullType() {
    val property = mapOf<String, Any>("type" to listOf("integer", "null"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.INTEGER, converted.type)
  }

  @Test
  fun parsePropertyMap_splitUnionBranch_carriesTheKeywordsThatDescribeIt() {
    // Each branch has to take the keywords for its own type; without that the split would hand the
    // model two bare types and lose everything the server said about them.
    val property =
      mapOf<String, Any>(
        "type" to listOf("string", "object"),
        "description" to "an id",
        "enum" to listOf("EAST", "WEST"),
        "properties" to mapOf("inner" to mapOf("type" to "string")),
        "required" to listOf("inner"),
      )

    val converted = McpSchemaConverter.parsePropertyMap(property)

    val branches = requireNotNull(converted.anyOf)
    val stringBranch = branches.single { it.type == Type.STRING }
    assertEquals("an id", stringBranch.description)
    assertEquals(listOf("EAST", "WEST"), stringBranch.enum)
    val objectBranch = branches.single { it.type == Type.OBJECT }
    assertEquals(Type.STRING, objectBranch.properties?.get("inner")?.type)
    assertEquals(listOf("inner"), objectBranch.required)
    // `enum` describes a string, not an object, so it must not ride along onto the object branch.
    assertNull(objectBranch.enum)
  }

  @Test
  fun parsePropertyMap_booleanFalseSubSchema_becomesAnOpenObject() {
    // Python maps `false` to an object too; dropping it would take the argument off the contract.
    val property = mapOf<String, Any>("type" to "object", "properties" to mapOf("never" to false))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.OBJECT, converted.properties?.get("never")?.type)
  }

  @Test
  fun parsePropertyMap_booleanItems_isNotTreatedAsAnArrayOfStrings() {
    // A boolean sub-schema is legal wherever a schema is, not only under `properties`.
    val property = mapOf<String, Any>("type" to "array", "items" to true)

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.OBJECT, converted.items?.type)
  }

  @Test
  fun parsePropertyMap_splitUnionArrayBranch_keepsDeclaredItems() {
    // The array branch has to take the declared `items`, not fall back to the string default.
    val property =
      mapOf<String, Any>("type" to listOf("string", "array"), "items" to mapOf("type" to "integer"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    val arrayBranch = requireNotNull(converted.anyOf).single { it.type == Type.ARRAY }
    assertEquals(Type.INTEGER, arrayBranch.items?.type)
  }

  @Test
  fun parsePropertyMap_booleanTrueSubSchema_becomesAnOpenObject() {
    // `true` is legal JSON Schema meaning "anything goes"; the argument must stay on the contract.
    val property =
      mapOf<String, Any>(
        "type" to "object",
        "properties" to mapOf("free" to true),
        "required" to listOf("free"),
      )

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.OBJECT, converted.properties?.get("free")?.type)
    assertEquals(listOf("free"), converted.required)
  }

  @Test
  fun parsePropertyMap_typeUnionContainingArray_splitsIntoBranches() {
    val property = mapOf<String, Any>("type" to listOf("string", "array"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(listOf(Type.STRING, Type.ARRAY), converted.anyOf?.map { it.type })
    assertNull(converted.type)
  }

  @Test
  fun parsePropertyMap_typeUnionOfScalars_splitsIntoBranches() {
    val property = mapOf<String, Any>("type" to listOf("string", "integer"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(listOf(Type.STRING, Type.INTEGER), converted.anyOf?.map { it.type })
  }

  @Test
  fun parsePropertyMap_nullableUnionOfSeveralTypes_splitsAndStaysNullable() {
    val property = mapOf<String, Any>("type" to listOf("null", "string", "integer"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(listOf(Type.STRING, Type.INTEGER), converted.anyOf?.map { it.type })
    assertEquals(true, converted.nullable)
  }

  @Test
  fun parsePropertyMap_unionWithNullableKeyword_splitsAndStaysNullable() {
    // Nullability spelled as the keyword rather than as a `"null"` member of the union.
    val property = mapOf<String, Any>("type" to listOf("string", "integer"), "nullable" to true)

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(listOf(Type.STRING, Type.INTEGER), converted.anyOf?.map { it.type })
    assertEquals(true, converted.nullable)
  }

  @Test
  fun parsePropertyMap_typeUnionOfOnlyNull_returnsNullType() {
    val property = mapOf<String, Any>("type" to listOf("null"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.NULL, converted.type)
  }

  @Test
  fun parsePropertyMap_typeUnionWithAnUnknownMember_keepsTheMemberItKnows() {
    // Splitting the union must not make a schema fail that used to convert: picking the first
    // member gave `string` here, and ADK Python still does exactly that.
    val property = mapOf<String, Any>("type" to listOf("string", "temperature"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.STRING, converted.type)
    assertNull(converted.anyOf)
  }

  @Test
  fun parsePropertyMap_typeUnionWithAnUnknownMemberFirst_keepsTheMemberItKnows() {
    // The surviving member does not depend on where the unknown one sits, unlike picking the first.
    val property = mapOf<String, Any>("type" to listOf("temperature", "string"))

    assertEquals(Type.STRING, McpSchemaConverter.parsePropertyMap(property).type)
  }

  @Test
  fun parsePropertyMap_typeUnionWithAnUnknownMemberAmongSeveral_splitsTheRest() {
    val property = mapOf<String, Any>("type" to listOf("string", "temperature", "integer"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(listOf(Type.STRING, Type.INTEGER), converted.anyOf?.map { it.type })
  }

  @Test
  fun parsePropertyMap_nullableTypeUnionWithAnUnknownMember_staysNullable() {
    val property = mapOf<String, Any>("type" to listOf("null", "string", "temperature"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.STRING, converted.type)
    assertEquals(true, converted.nullable)
  }

  @Test
  fun parsePropertyMap_typeUnionOfOnlyUnknownMembers_throwsIllegalArgument() {
    // Nothing is left to describe the value, so this fails the way a single unknown type does
    // rather than quietly becoming an unconstrained argument.
    val property = mapOf<String, Any>("type" to listOf("temperature", "pressure"))

    assertFailsWith<IllegalArgumentException> { McpSchemaConverter.parsePropertyMap(property) }
  }

  @Test
  fun parsePropertyMap_singleUnknownType_throwsIllegalArgument() {
    // Deliberate, and what ADK Python does: a parameter contract nothing could convert would leave
    // the model calling the tool against a description that was never validated.
    val property = mapOf<String, Any>("type" to "temperature")

    assertFailsWith<IllegalArgumentException> { McpSchemaConverter.parsePropertyMap(property) }
  }

  @Test
  fun parsePropertyMap_emptyTypeUnion_returnsTypeUnspecified() {
    val property = mapOf<String, Any>("type" to emptyList<String>())

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.TYPE_UNSPECIFIED, converted.type)
  }

  @Test
  fun parsePropertyMap_absentType_returnsTypeUnspecified() {
    val converted = McpSchemaConverter.parsePropertyMap(mapOf("description" to "no type here"))

    assertEquals(Type.TYPE_UNSPECIFIED, converted.type)
  }

  @Test
  fun parsePropertyMap_scalarType_usesThatType() {
    val converted = McpSchemaConverter.parsePropertyMap(mapOf("type" to "boolean"))

    assertEquals(Type.BOOLEAN, converted.type)
  }

  @Test
  fun parsePropertyMap_singleElementTypeUnion_usesThatType() {
    val converted = McpSchemaConverter.parsePropertyMap(mapOf("type" to listOf("integer")))

    assertEquals(Type.INTEGER, converted.type)
  }

  // enum.

  @Test
  fun parsePropertyMap_stringEnum_preservesTheAllowedValues() {
    val property = mapOf<String, Any>("type" to "string", "enum" to listOf("EAST", "WEST"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(listOf("EAST", "WEST"), converted.enum)
  }

  @Test
  fun parsePropertyMap_numericEnum_rendersTheValuesAsStrings() {
    val property = mapOf<String, Any>("type" to "integer", "enum" to listOf(101, 201))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(listOf("101", "201"), converted.enum)
  }

  @Test
  fun parsePropertyMap_enumContainingNull_dropsTheNullMember() {
    val property = mapOf<String, Any>("type" to "string", "enum" to listOf("a", null, "b"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(listOf("a", "b"), converted.enum)
  }

  @Test
  fun parsePropertyMap_emptyEnum_leavesEnumUnset() {
    val property = mapOf<String, Any>("type" to "string", "enum" to emptyList<String>())

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertNull(converted.enum)
  }

  @Test
  fun parsePropertyMap_absentEnum_leavesEnumUnset() {
    val converted = McpSchemaConverter.parsePropertyMap(mapOf("type" to "string"))

    assertNull(converted.enum)
  }

  // Structure.

  @Test
  fun parsePropertyMap_nestedObject_convertsTheNestedProperties() {
    val property =
      mapOf<String, Any>(
        "type" to "object",
        "properties" to mapOf("inner" to mapOf("type" to "boolean")),
        "required" to listOf("inner"),
      )

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.BOOLEAN, converted.properties?.get("inner")?.type)
    assertEquals(listOf("inner"), converted.required)
  }

  @Test
  fun parsePropertyMap_arraySchema_convertsTheItemSchema() {
    val property = mapOf<String, Any>("type" to "array", "items" to mapOf("type" to "string"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.STRING, converted.items?.type)
  }

  @Test
  fun parsePropertyMap_arrayWithoutItems_defaultsItemsToString() {
    val property = mapOf<String, Any>("type" to "array")

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.STRING, converted.items?.type)
  }

  @Test
  fun parsePropertyMap_typeUnionWithArrayBranchWithoutItems_defaultsItemsToString() {
    val property = mapOf<String, Any>("type" to listOf("string", "array"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    // The array branch is a schema like any other, so it gets the same `items` default.
    val arrayBranch = converted.anyOf?.single { it.type == Type.ARRAY }
    assertEquals(Type.STRING, arrayBranch?.items?.type)
  }

  @Test
  fun parsePropertyMap_nullableArrayWithoutItems_defaultsItemsToString() {
    val property = mapOf<String, Any>("type" to listOf("null", "array"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(Type.ARRAY, converted.type)
    assertEquals(Type.STRING, converted.items?.type)
  }

  @Test
  fun parsePropertyMap_nonArrayWithoutItems_leavesItemsUnset() {
    val property = mapOf<String, Any>("type" to "string")

    assertNull(McpSchemaConverter.parsePropertyMap(property).items)
  }

  @Test
  fun parsePropertyMap_descriptionPresent_preservesTheDescription() {
    val property = mapOf<String, Any>("type" to "string", "description" to "the message")

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals("the message", converted.description)
  }

  @Test
  fun parsePropertyMap_requiredNamesADroppedProperty_removesItFromRequired() {
    // A property whose sub-schema is not a schema at all is dropped, and the backend rejects a
    // `required` entry naming a property the schema does not define.
    val property =
      mapOf<String, Any>(
        "type" to "object",
        "properties" to mapOf("kept" to mapOf("type" to "string"), "dropped" to "not-a-schema"),
        "required" to listOf("kept", "dropped"),
      )

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(listOf("kept"), converted.required)
  }

  @Test
  fun parsePropertyMap_requiredWithoutAnyProperties_clearsRequired() {
    val property = mapOf<String, Any>("type" to "object", "required" to listOf("missing"))

    // Unset rather than empty: an empty `required` says nothing, so there is no reason to send it.
    assertNull(McpSchemaConverter.parsePropertyMap(property).required)
  }

  @Test
  fun parsePropertyMap_propertyThatIsNotAnObject_dropsThatProperty() {
    val property =
      mapOf<String, Any>("type" to "object", "properties" to mapOf("bogus" to "not a schema"))

    val converted = McpSchemaConverter.parsePropertyMap(property)

    assertEquals(emptyMap(), converted.properties)
  }

  // Recursion guard.

  @Test
  fun parsePropertyMap_schemaWithinMaxDepth_keepsEveryLevel() {
    val schema = nestedObjectSchema(levels = 10)

    val converted = McpSchemaConverter.parsePropertyMap(schema)

    assertEquals(10, schemaChainLength(converted))
  }

  @Test
  fun parsePropertyMap_schemaDeeperThanMaxDepth_truncatesInsteadOfRecursing() {
    val schema = nestedObjectSchema(levels = 500)

    val converted = McpSchemaConverter.parsePropertyMap(schema)

    // Level n is parsed at depth n-1, and the guard truncates at depth MAX_SCHEMA_DEPTH (32), so
    // the chain stops at level 33. Asserted exactly so that lowering the limit fails the test.
    assertEquals(33, schemaChainLength(converted))
  }

  // JsonSchema (a tool's top-level inputSchema).

  @Test
  fun toAdkSchema_objectSchema_convertsTypePropertiesAndRequired() {
    val inputSchema =
      jsonSchema(properties = mapOf("a" to mapOf("type" to "integer")), required = listOf("a"))

    val converted = inputSchema.toAdkSchema()

    assertEquals(Type.OBJECT, converted.type)
    assertEquals(Type.INTEGER, converted.properties?.get("a")?.type)
    assertEquals(listOf("a"), converted.required)
  }

  @Test
  fun toAdkSchema_propertyWithNullTypeUnion_doesNotThrow() {
    val inputSchema =
      jsonSchema(properties = mapOf("maybe" to mapOf("type" to listOf("null", "string"))))

    val converted = inputSchema.toAdkSchema()

    assertEquals(Type.STRING, converted.properties?.get("maybe")?.type)
  }

  @Test
  fun toAdkSchema_requiredNamesAnUndeclaredProperty_removesItFromRequired() {
    val inputSchema =
      jsonSchema(
        properties = mapOf("a" to mapOf("type" to "integer")),
        required = listOf("a", "missing"),
      )

    assertEquals(listOf("a"), inputSchema.toAdkSchema().required)
  }

  @Test
  fun toAdkSchema_arrayTypedInputSchema_defaultsItemsToString() {
    // The JsonSchema record has no `items` component, so an array can only take the default.
    val inputSchema =
      McpSchema.JsonSchema(
        /* type= */ "array",
        /* properties= */ null,
        /* required= */ null,
        /* additionalProperties= */ null,
        /* defs= */ null,
        /* definitions= */ null,
      )

    assertEquals(Type.STRING, inputSchema.toAdkSchema().items?.type)
  }

  // FunctionDeclaration.

  @Test
  fun toAdkFunctionDeclaration_toolWithInputSchema_setsNameDescriptionAndParameters() {
    val tool =
      McpSchema.Tool.builder()
        .name("add")
        .description("Adds two numbers.")
        .inputSchema(jsonSchema(properties = mapOf("a" to mapOf("type" to "integer"))))
        .build()

    val declaration = tool.toAdkFunctionDeclaration()

    assertEquals("add", declaration.name)
    assertEquals("Adds two numbers.", declaration.description)
    assertEquals(Type.INTEGER, declaration.parameters?.properties?.get("a")?.type)
  }

  @Test
  fun toAdkFunctionDeclaration_toolWithoutDescription_usesAnEmptyDescription() {
    val tool = McpSchema.Tool.builder().name("bare").build()

    val declaration = tool.toAdkFunctionDeclaration()

    assertEquals("", declaration.description)
  }

  @Test
  fun toAdkFunctionDeclaration_toolWithoutInputSchema_leavesParametersUnset() {
    val tool = McpSchema.Tool.builder().name("bare").build()

    val declaration = tool.toAdkFunctionDeclaration()

    assertNull(declaration.parameters)
  }

  @Test
  fun toAdkFunctionDeclaration_toolWithNullTypedProperty_doesNotThrow() {
    val tool =
      McpSchema.Tool.builder()
        .name("nullable")
        .inputSchema(jsonSchema(properties = mapOf("maybe" to mapOf("type" to "null"))))
        .build()

    val declaration = tool.toAdkFunctionDeclaration()

    val parameters = assertNotNull(declaration.parameters)
    assertEquals(Type.NULL, parameters.properties?.get("maybe")?.type)
  }

  private companion object {
    /** Property name linking one level of a [nestedObjectSchema] chain to the next. */
    const val NESTED_KEY = "child"

    /**
     * Builds an `object` schema nested [levels] deep, each level holding the next under
     * [NESTED_KEY].
     */
    fun nestedObjectSchema(levels: Int): Map<String, Any> {
      var schema = mapOf<String, Any>("type" to "object")
      repeat(levels - 1) {
        schema = mapOf("type" to "object", "properties" to mapOf(NESTED_KEY to schema))
      }
      return schema
    }

    /** Counts the levels of a converted [nestedObjectSchema] chain. */
    fun schemaChainLength(schema: Schema): Int {
      var levels = 1
      var current = schema
      while (true) {
        current = current.properties?.get(NESTED_KEY) ?: return levels
        levels++
      }
    }

    /**
     * Builds an `{"type": "object", ...}` [McpSchema.JsonSchema]; the record is a Java record, so
     * its components have to be passed positionally.
     */
    fun jsonSchema(
      properties: Map<String, Any> = emptyMap(),
      required: List<String> = emptyList(),
    ): McpSchema.JsonSchema =
      McpSchema.JsonSchema(
        /* type= */ "object",
        /* properties= */ properties,
        /* required= */ required,
        /* additionalProperties= */ null,
        /* defs= */ null,
        /* definitions= */ null,
      )
  }
}
