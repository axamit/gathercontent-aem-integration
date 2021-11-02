/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto.helpers;

import com.axamit.gc.api.dto.GCContent;
import com.axamit.gc.api.dto.GCElementType;
import com.axamit.gc.api.dto.GCFile;
import com.axamit.gc.api.dto.GCOption;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The <code>GCContentDeserializer</code> is a deserializer for content in Items.
 *
 * @author Axamit, gc.support@axamit.com
 */
public class GCContentDeserializer extends StdDeserializer<GCContent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GCContentDeserializer.class);

    public GCContentDeserializer() {
        this(null);
    }

    public GCContentDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public GCContent deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        final JsonNode node = jp.getCodec().readTree(jp);
        final ObjectMapper mapper = new ObjectMapper();

        switch (node.getNodeType()) {
            case STRING: return new GCContent(GCElementType.TEXT).setText(node.asText());
            case ARRAY: //list of files
                final List<GCFile> files = new ArrayList<>();
                final List<GCOption> options = new ArrayList<>();
                node.elements().forEachRemaining(jsonNode -> {
                    if (jsonNode.has("filename")) {
                        final GCFile file = mapper.convertValue(jsonNode, GCFile.class);
                        files.add(file);
                    } else {
                        final GCOption option = mapper.convertValue(jsonNode, GCOption.class);
                        options.add(option);
                    }
                });
                if (!files.isEmpty()) {
                    return new GCContent(GCElementType.FILES).setFiles(files);
                }
                if (!options.isEmpty()) {
                    return new GCContent(GCElementType.OPTIONS).setOptions(options);
                }
                LOGGER.warn("Not supported type in json: {}", node.asText());
                return new GCContent(GCElementType.NOT_SUPPORTED_TYPE);
            case OBJECT: //component
                Map<String, GCContent> component = new HashMap<>();
                node.fields().forEachRemaining(entry -> {
                    final GCContent gcContent = mapper.convertValue(entry.getValue(), GCContent.class);
                    component.put(entry.getKey(), gcContent);
                });
                return new GCContent(GCElementType.COMPONENT).setComponent(component);
        }
        LOGGER.warn("Not supported type in json: {}", node.asText());
        return new GCContent(GCElementType.NOT_SUPPORTED_TYPE);
    }
}
