/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.helpers;

import com.axamit.gc.core.util.Constants;

/**
 * Enum represents page renderer names.
 *
 * @author Axamit, gc.support@axamit.com
 */
public enum Renderer {
    DEFAULT("default"),
    UPDATE("update"),
    EXPORT("export"),
    MULTILOCATION("multilocation"),
    CONFIG("config"),
    JOBS("jobs"),
    MAPPING(Constants.MAPPING_IMPORT_SELECTOR),
    MAPPING_EXPORT(Constants.MAPPING_EXPORT_SELECTOR),
    CREDENTIALS("credentials");

    private final String type;

    /**
     * Constructor.
     *
     * @param type String representation of renderer.
     */
    Renderer(final String type) {
        this.type = type;
    }

    /**
     * Get enum element by string representation.
     *
     * @param code String representation of renderer.
     * @return enum element.
     */
    public static Renderer of(final String code) {
        for (Renderer argument : Renderer.values()) {
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
