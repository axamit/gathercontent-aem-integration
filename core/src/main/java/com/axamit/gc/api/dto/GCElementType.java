/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The <code>GCElementType</code> class represents element type of config.
 *
 * @see <a href="https://gathercontent.com/developers/the-config-field/">Element types</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum GCElementType {
    TEXT("text"),
    FILES("files"),
    CHOICE_RADIO("choice_radio"),
    CHOICE_CHECKBOX("choice_checkbox"),
    SECTION("section");
    private final String type;

    /**
     * Constructor.
     *
     * @param type String representation of type.
     */
    GCElementType(final String type) {
        this.type = type;
    }

    /**
     * Get enum element by string representation.
     *
     * @param code String representation of type.
     * @return enum element.
     */
    @JsonCreator
    public static GCElementType of(final String code) {
        for (GCElementType argument : GCElementType.values()) {
            if (argument.getType().equalsIgnoreCase(code)) {
                return argument;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }
}
