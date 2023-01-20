/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.axamit.gc.api.dto.helpers.GCContentDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
        return ImmutableList.copyOf(files);
    }

    public GCContent setFiles(List<GCFile> files) {
        this.files = files;
        return this;
    }

    public List<GCOption> getOptions() {
        return ImmutableList.copyOf(options);
    }

    public GCContent setOptions(List<GCOption> options) {
        this.options = options;
        return this;
    }

    public Map<String, GCContent> getComponent() {
        return ImmutableMap.copyOf(component);
    }

    public GCContent setComponent(Map<String, GCContent> component) {
        this.component = component;
        return this;
    }

    public boolean isEmpty() {
        return type == null || StringUtils.isBlank(text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GCContent gcContent = (GCContent) o;

        return new EqualsBuilder().append(getType(), gcContent.getType()).append(getText(), gcContent.getText()).append(getFiles(), gcContent.getFiles()).append(getOptions(), gcContent.getOptions()).append(getComponent(), gcContent.getComponent()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getType()).append(getText()).append(getFiles()).append(getOptions()).append(getComponent()).toHashCode();
    }
}
