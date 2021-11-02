/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto.helpers;

import com.axamit.gc.api.dto.GCContent;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Map;

/**
 * The <code>GCContentMapSerializer</code> is a serializer for content in Items.
 *
 * @author Axamit, gc.support@axamit.com
 */
public class GCContentMapSerializer extends StdSerializer<Map<String, GCContent>> {

    public GCContentMapSerializer() {
        this(null);
    }

    public GCContentMapSerializer(Class<Map<String, GCContent>> t) {
        super(t);
    }

    @Override
    public void serialize(Map<String, GCContent> contentMap, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        for (Map.Entry<String, GCContent> gcContentEntry : contentMap.entrySet()) {
            final String key = gcContentEntry.getKey();
            final GCContent value = gcContentEntry.getValue();
            switch (value.getType()) {
                case TEXT: {
                    jgen.writeStringField(key, value.getText());
                    break;
                }
                default:
                    break;
            }
        }
        jgen.writeEndObject();
    }
}