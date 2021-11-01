/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.util;

import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.FieldMappingProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The <code>JSONUtil</code> is an utility class for transformation various objects and JSON representations.
 *
 * @author Axamit, gc.support@axamit.com
 */
public enum JSONUtil {
    /*INSTANCE*/;
    private static final Logger LOGGER = LoggerFactory.getLogger(JSONUtil.class);

    private static final ObjectMapper OBJECT_MAPPER =
        new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    /**
     * Deserialize JSON content as tree expressed using set of <code>{@link JsonNode}</code> instances.
     *
     * @param stringObject JSON content represented in <code>{@link String}</code> to parse.
     * @return <code>{@link JsonNode}</code> instance.
     * @throws GCException If any error occurred during transformation.
     */
    public static JsonNode fromJsonToJSonNode(final String stringObject) throws GCException {
        try {
            return OBJECT_MAPPER.readTree(stringObject);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new GCException(e);
        }
    }

    /**
     * Build object of specified class type from JSON.
     *
     * @param stringObject JSON content represented in <code>{@link String}</code> to parse.
     * @param clazz        Class object to build.
     * @param <T>          The type of object built in this method.
     * @return Created object of specified class type.
     * @throws GCException If any error occurred during transformation.
     */
    public static <T> T fromJsonToObject(final String stringObject, final Class<T> clazz) throws GCException {
        try {
            return OBJECT_MAPPER.readValue(stringObject, clazz);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new GCException(e);
        }
    }

    /**
     * Create <code>{@link JSONObject}</code> from JSON.
     *
     * @param stringObject JSON content represented in <code>{@link String}</code> to parse.
     * @return Created <code>{@link JSONObject}</code>.
     * @throws GCException If any error occurred during transformation.
     */
    public static JSONObject fromJsonToJSonObject(final String stringObject) throws GCException {
        try {
            return new JSONObject(stringObject);
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
            throw new GCException(e);
        }
    }

    /**
     * Build the <code>{@link List}</code> of objects with specified class type.
     *
     * @param stringObject JSON content represented in <code>{@link String}</code> to parse.
     * @param clazz        Class object to build.
     * @param <T>          The type of objects built in this method.
     * @return Created <code>List</code> of objects.
     * @throws GCException If any error occurred during transformation.
     */
    public static <T> List<T> fromJsonToListObject(final String stringObject, final Class<T> clazz) throws GCException {
        try {
            return OBJECT_MAPPER.readValue(stringObject,
                OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new GCException(e);
        }
    }

    /**
     * Build the <code>Map</code> of objects with specified class types.
     *
     * @param stringObject JSON content represented in <code>{@link String}</code> to parse.
     * @param keyClass     Class object to build Map key.
     * @param valueClass   Class object to build Map value.
     * @param <K>          The type of Map key objects built.
     * @param <V>          The type of Map value objects built.
     * @return Created <code>Map</code> of objects.
     * @throws GCException If any error occurred during transformation.
     */
    public static <K, V> Map<K, V> fromJsonToMapObject(final String stringObject, final Class<K> keyClass,
                                                       final Class<V> valueClass) throws GCException {
        try {
            return OBJECT_MAPPER.readValue(stringObject,
                OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new GCException(e);
        }
    }

    /**
     * Build JSON content represented in <code>{@link String}</code> from object.
     *
     * @param object Object to transform to JSON.
     * @return JSON content represented in <code>{@link String}</code>.
     * @throws GCException If any error occurred during transformation.
     */
    public static String fromObjectToJsonString(final Object object) throws GCException {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
            throw new GCException(e);
        }
    }

    /**
     * Read JSON and build map of mapped properties.
     *
     * @param mappingString JSON content represented in <code>{@link String}</code> to parse.
     * @return Map of mapped properties.
     * @throws GCException If any error occurred during transformation.
     */
    public static Map<String, FieldMappingProperties> fromJsonToMappingMap(String mappingString) throws GCException {
        Map<String, FieldMappingProperties> map = new LinkedHashMap<>();
        JsonNode jsonNode = fromJsonToJSonNode(mappingString);
        for (Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> fieldMapping = it.next();
            JsonNode fieldMappingValue = fieldMapping.getValue();
            FieldMappingProperties fieldMappingProperties = new FieldMappingProperties();
            List<String> paths = new ArrayList<>();
            if (fieldMappingValue.isTextual()) {
                paths.add(fieldMappingValue.textValue());
            } else if (fieldMappingValue.isArray()) {
                for (Iterator<JsonNode> pathElements = fieldMappingValue.elements(); pathElements.hasNext(); ) {
                    JsonNode pathElement = pathElements.next();
                    if (pathElement.isTextual()) {
                        paths.add(pathElement.textValue());
                    }
                }
            } else if (fieldMappingValue.isObject()) {
                if (fieldMappingValue.has(FieldMappingProperties.MAPPING_FIELD_PLUGIN)) {
                    fieldMappingProperties.setPlugin(
                        fieldMappingValue.get(FieldMappingProperties.MAPPING_FIELD_PLUGIN).textValue());
                }
                if (fieldMappingValue.has(FieldMappingProperties.MAPPING_FIELD_PROPERTY_PATH)) {
                    JsonNode pathNode = fieldMappingValue.get(FieldMappingProperties.MAPPING_FIELD_PROPERTY_PATH);
                    if (pathNode.isArray()) {
                        for (Iterator<JsonNode> pathElements = pathNode.elements(); pathElements.hasNext(); ) {
                            JsonNode pathElement = pathElements.next();
                            if (pathElement.isTextual()) {
                                paths.add(pathElement.textValue());
                            }
                        }
                    } else if (pathNode.isTextual()) {
                        paths.add(pathNode.textValue());
                    }
                }
            }
            fieldMappingProperties.setPath(paths);
            map.put(fieldMapping.getKey(), fieldMappingProperties);
        }
        return map;
    }

    public static void addMappingEntry(JSONArray jsonArray, String text, String value, String qtip) throws JSONException {
        JSONObject jsonObjectAccount = new JSONObject();
        jsonObjectAccount.put(Constants.JSON_PN_TEXT, text);
        jsonObjectAccount.put(Constants.JSON_PN_VALUE, value);
        jsonObjectAccount.put(Constants.JSON_PN_QTIP, qtip);
        jsonArray.put(jsonObjectAccount);
    }

}
