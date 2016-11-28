/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.util;

import com.axamit.gc.core.exception.GCException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * The <code>JSONUtil</code> is an utility class for transformation various objects and JSON representations.
 */
public final class JSONUtil {

    private JSONUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONUtil.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Deserialize JSON content as tree expressed using set of <code>{@link JsonNode}</code> instances.
     *
     * @param stringObject JSON content represented in <code>{@link String}</code> to parse.
     * @return <code>{@link JsonNode}</code> instance.
     * @throws GCException If any error occurred during transformation.
     */
    public static JsonNode fromJsonToJSonNode(final String stringObject) throws GCException {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(stringObject);
            return jsonNode;
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
            T value = OBJECT_MAPPER.readValue(stringObject, clazz);
            return value;
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
            JSONObject jsonNode = new JSONObject(stringObject);
            return jsonNode;
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
     * @return Created <code>{@link List}</code> of objects.
     * @throws GCException If any error occurred during transformation.
     */
    public static <T> List<T> fromJsonToListObject(final String stringObject, final Class<T> clazz) throws GCException {
        try {
            List<T> value = OBJECT_MAPPER.readValue(stringObject,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
            return value;
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

}
