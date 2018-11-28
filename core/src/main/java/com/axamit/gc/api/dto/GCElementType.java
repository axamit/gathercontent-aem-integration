/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The <code>GCElementType</code> class represents element type of config.
 *
 * @author Axamit, gc.support@axamit.com
 * @see <a href="https://gathercontent.com/developers/the-config-field/">Element types</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum GCElementType {
    @JsonProperty("text")
    TEXT("text"),
    @JsonProperty("files")
    FILES("files"),
    @JsonProperty("choice_radio")
    CHOICE_RADIO("choice_radio"),
    @JsonProperty("choice_checkbox")
    CHOICE_CHECKBOX("choice_checkbox"),
    @JsonProperty("section")
    SECTION("section"),
    MULTIVALUE_NEW_EDITOR("multivalue_new_editor");
    private final String type;

    /**
     * Constructor.
     *
     * @param type String representation of type.
     */
    GCElementType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
