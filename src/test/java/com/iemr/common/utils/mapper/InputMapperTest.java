/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.common.utils.mapper;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class InputMapperTest {

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Reset the static builder field before each test to ensure isolation.
        // The InputMapper constructor and gson() method only initialize 'builder' if it's null.
        // Setting it to null ensures each test starts with a clean slate for the static builder.
        Field builderField = InputMapper.class.getDeclaredField("builder");
        builderField.setAccessible(true);
        builderField.set(null, null); // Set static field to null
    }

    @Test
    void testInputMapperConstructor_InitializesStaticBuilder() throws NoSuchFieldException, IllegalAccessException {
        // Calling the constructor should initialize the static 'builder' field if it's null.
        new InputMapper();

        // Verify that the static 'builder' field is no longer null after constructor call.
        Field builderField = InputMapper.class.getDeclaredField("builder");
        builderField.setAccessible(true);
        GsonBuilder builder = (GsonBuilder) builderField.get(null);

        assertNotNull(builder, "GsonBuilder should be initialized by the constructor.");
    }

    @Test
    void testGsonStaticMethod_ReturnsNewInputMapperInstanceAndInitializesBuilder() throws NoSuchFieldException, IllegalAccessException {
        // First call to gson() should initialize the static builder and return a new instance.
        InputMapper mapper1 = InputMapper.gson();
        assertNotNull(mapper1, "gson() should return a non-null InputMapper instance.");

        // Verify that the static 'builder' field is initialized after the first call to gson().
        Field builderField = InputMapper.class.getDeclaredField("builder");
        builderField.setAccessible(true);
        GsonBuilder builder = (GsonBuilder) builderField.get(null);
        assertNotNull(builder, "GsonBuilder should be initialized when gson() is called for the first time.");

        // Second call to gson() should return another new instance.
        InputMapper mapper2 = InputMapper.gson();
        assertNotNull(mapper2, "gson() should return a non-null InputMapper instance on subsequent calls.");

        // Verify that different instances are returned by consecutive calls to gson().
        assertNotSame(mapper1, mapper2, "gson() should return a new InputMapper instance each time it's called.");
    }

    @Test
    void testFromJson_ValidJsonObjectStringToJsonElement() {
        InputMapper mapper = new InputMapper(); // Ensure builder is initialized for fromJson to work
        String json = "{\"name\":\"test\", \"age\":30}";
        try {
            JsonElement element = mapper.fromJson(json, JsonElement.class);
            assertNotNull(element, "JsonElement should not be null for valid JSON object.");
            assertTrue(element.isJsonObject(), "Parsed element should be a JsonObject.");
            assertEquals("test", element.getAsJsonObject().get("name").getAsString());
            assertEquals(30, element.getAsJsonObject().get("age").getAsInt());
        } catch (Exception e) {
            fail("fromJson should not throw an exception for valid JSON object: " + e.getMessage());
        }
    }

    @Test
    void testFromJson_ValidJsonArrayStringToJsonElement() {
        InputMapper mapper = new InputMapper();
        String json = "[1, 2, \"three\"]";
        try {
            JsonElement element = mapper.fromJson(json, JsonElement.class);
            assertNotNull(element, "JsonElement should not be null for valid JSON array.");
            assertTrue(element.isJsonArray(), "Parsed element should be a JsonArray.");
            assertEquals(3, element.getAsJsonArray().size());
            assertEquals(1, element.getAsJsonArray().get(0).getAsInt());
            assertEquals("three", element.getAsJsonArray().get(2).getAsString());
        } catch (Exception e) {
            fail("fromJson should not throw an exception for valid JSON array: " + e.getMessage());
        }
    }

    @Test
    void testFromJson_ValidJsonPrimitiveStringToString() {
        InputMapper mapper = new InputMapper();
        String json = "\"hello world\""; // A JSON string literal
        try {
            String result = mapper.fromJson(json, String.class);
            assertNotNull(result, "Result should not be null for valid JSON string literal.");
            assertEquals("hello world", result);
        } catch (Exception e) {
            fail("fromJson should not throw an exception for valid JSON string literal: " + e.getMessage());
        }
    }

    @Test
    void testFromJson_ValidJsonPrimitiveNumberToInteger() {
        InputMapper mapper = new InputMapper();
        String json = "123"; // A JSON number literal
        try {
            Integer result = mapper.fromJson(json, Integer.class);
            assertNotNull(result, "Result should not be null for valid JSON number literal.");
            assertEquals(123, result);
        } catch (Exception e) {
            fail("fromJson should not throw an exception for valid JSON number literal: " + e.getMessage());
        }
    }

    @Test
    void testFromJson_NullJsonStringReturnsNullForJsonElement() {
        InputMapper mapper = new InputMapper();
        String json = null;
        try {
            JsonElement element = mapper.fromJson(json, JsonElement.class);
            assertNull(element, "fromJson should return null for null JSON string when target is JsonElement.");
        } catch (Exception e) {
            fail("fromJson should not throw an exception for null JSON string when target is JsonElement: " + e.getMessage());
        }
    }

    @Test
    void testFromJson_InvalidJsonStringThrowsJsonSyntaxException() {
        InputMapper mapper = new InputMapper();
        String invalidJson = "{this is not valid json"; // Malformed JSON string
        assertThrows(JsonSyntaxException.class, () -> {
            mapper.fromJson(invalidJson, JsonElement.class);
        }, "fromJson should throw JsonSyntaxException for malformed JSON.");
    }
}