/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.axamit.gc.core.util.GCUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The <code>GCConfig</code> is a POJO class represent item config info.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCConfig {
    private String label;

    private Boolean hidden;

    private List<GCElement> elements;

    private String name;

    /**
     * @return Label of GatherContent page.
     */
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setEscapedLabel(final String escapedLabel) {
        this.label = GCUtil.unescapeGCString(escapedLabel);
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    /**
     * @return Hidden of GatherContent page.
     */
    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(final Boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * @return Collection of elements of GatherContent page.
     */
    public List<GCElement> getElements() {
        return elements;
    }

    public void setElements(final List<GCElement> elements) {
        this.elements = elements;
    }

    /**
     * @return Name of GatherContent page.
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
