package com.axamit.gc.core.util;

import com.axamit.gc.api.dto.GCElement;
import com.axamit.gc.api.dto.GCElementType;
import com.axamit.gc.api.dto.GCOption;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.FieldMappingProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Vasily Lazerko
 */
public class JSONUtilTest {

    private static final String ELEMENT_JSON = "{\"type\":\"text\",\"name\":\"name\",\"label\":\"label\"," +
        "\"value\":\"value\",\"microcopy\":\"microcopy\",\"limit\":1,\"title\":\"title\"," +
        "\"options\":[{},{}],\"other_option\":true,\"limit_type\":\"limitType\",\"plain_text\":false}";

    @Test
    public void fromJsonToJSonNode() throws GCException {
        final JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(ELEMENT_JSON);
        Assert.assertEquals("text", jsonNode.get("type").asText());
        Assert.assertEquals("name", jsonNode.get("name").asText());
        Assert.assertNull(jsonNode.get("subtitle"));
    }

    @Test
    public void fromJsonToObject() throws GCException {

        final GCElement element = JSONUtil.fromJsonToObject(ELEMENT_JSON, GCElement.class);
        Assert.assertEquals(GCElementType.TEXT, element.getType());
    }

    @Test
    public void fromJsonToJSonObject() throws JSONException, GCException {
        final JSONObject jsonObject = JSONUtil.fromJsonToJSonObject(ELEMENT_JSON);
        Assert.assertTrue(jsonObject.getBoolean("other_option"));
        Assert.assertEquals(1, jsonObject.getInt("limit"));
    }

    @Test
    public void fromJsonToListObject() throws GCException {
        final List<GCElement> elements = JSONUtil.fromJsonToListObject('[' + ELEMENT_JSON + ']', GCElement.class);
        Assert.assertEquals(1, elements.size());
    }

    @Test
    public void fromJsonToMapObject() throws GCException {
        final Map<String, GCElement> map =
            JSONUtil.fromJsonToMapObject("{\"key\":" + ELEMENT_JSON + '}', String.class, GCElement.class);
        Assert.assertNotNull(map.get("key"));
    }

    @Test
    public void fromObjectToJsonString() throws GCException {
        final GCElement element = new GCElement();
        element.setType(GCElementType.TEXT);
        element.setName("name");
        element.setLabel("label");
        element.setValue("value");
        element.setMicrocopy("microcopy");
        element.setOtherOption(Boolean.TRUE);
        element.setLimitType("limitType");
        element.setLimit(NumberUtils.INTEGER_ONE);
        element.setPlainText(Boolean.FALSE);
        element.setRequired(null);
        element.setOptions(ImmutableList.of(new GCOption(), new GCOption()));
        element.setTitle("title");
        element.setSubtitle(null);
        final String s = JSONUtil.fromObjectToJsonString(element);
        Assert.assertEquals(ELEMENT_JSON, s);
    }

    @Test
    public void fromJsonToMappingMap() throws GCException {
        final Map<String, FieldMappingProperties> map =
            JSONUtil.fromJsonToMappingMap(ELEMENT_JSON);
        Assert.assertEquals("name", map.get("name").getPath().get(0));
    }

}
