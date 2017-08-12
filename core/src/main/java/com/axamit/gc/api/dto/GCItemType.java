/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The <code>GCItemType</code> class represents item type.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum GCItemType {
    ITEM("item"),
    ENTRY_PARENT("entry_parent"),
    ENTRY_CHILD("entry_child");
    private final String type;

    /**
     * Constructor.
     *
     * @param type String representation of type.
     */
    GCItemType(final String type) {
        this.type = type;
    }

    /**
     * Get enum element by string representation.
     *
     * @param code String representation of type.
     * @return enum element.
     */
    @JsonCreator
    public static GCItemType of(final String code) {
        for (GCItemType argument : GCItemType.values()) {
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
