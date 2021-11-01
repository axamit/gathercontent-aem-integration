/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The <code>GCElement</code> class represents field of group.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCTemplateField {

    private String uuid;
    private String label;
    private String instructions;
    private GCElementType type;
    private GCTemplateGroup component;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    @JsonProperty("field_type")
    public GCElementType getType() {
        return type;
    }

    @JsonProperty("field_type")
    public void setType(GCElementType type) {
        this.type = type;
    }

    public GCTemplateGroup getComponent() {
        return component;
    }

    public void setComponent(GCTemplateGroup component) {
        this.component = component;
    }
}
