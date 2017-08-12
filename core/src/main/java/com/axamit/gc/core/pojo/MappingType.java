/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The <code>MappingType</code> class represents mapping type.
 *
 * @author Axamit, gc.support@axamit.com
 * @see <a href="https://gathercontent.com/developers/the-config-field/">Element types</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum MappingType {
    TEMPLATE("1"),
    ENTRY_PARENT("2"),
    CUSTOM_ITEM("3");
    private final String type;

    /**
     * Constructor.
     *
     * @param type String representation of type.
     */
    MappingType(final String type) {
        this.type = type;
    }

    /**
     * Get enum element by string representation.
     *
     * @param code String representation of type.
     * @return enum element.
     */
    @JsonCreator
    public static MappingType of(final String code) {
        for (MappingType argument : MappingType.values()) {
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
