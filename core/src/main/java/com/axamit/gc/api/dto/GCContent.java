/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.axamit.gc.api.dto.helpers.GCContentDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The <code>GCContent</code> class represents element of content.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = GCContentDeserializer.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class GCContent {

    private GCElementType type;

    private String text;

    private List<GCFile> files;

    private List<GCOption> options;

    private Map<String, GCContent> component;

    public GCContent(GCElementType type) {
        this.type = type;
    }

    public GCElementType getType() {
        return type;
    }

    public void setType(GCElementType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public GCContent setText(String text) {
        this.text = text;
        return this;
    }

    public List<GCFile> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public GCContent setFiles(List<GCFile> files) {
        this.files = files;
        return this;
    }

    public List<GCOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public GCContent setOptions(List<GCOption> options) {
        this.options = options;
        return this;
    }

    public Map<String, GCContent> getComponent() {
        return Collections.unmodifiableMap(component);
    }

    public GCContent setComponent(Map<String, GCContent> component) {
        this.component = component;
        return this;
    }
}
