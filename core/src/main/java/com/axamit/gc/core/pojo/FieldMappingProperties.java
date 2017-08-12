/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo;

import java.util.List;

/**
 * The <code>FieldMappingProperties</code> represents information about field mapping properties.
 *
 * @author Axamit, gc.support@axamit.com
 */
public class FieldMappingProperties {
    public static final String MAPPING_FIELD_PROPERTY_PATH = "path";
    private List<String> path;

    public static final String MAPPING_FIELD_PLUGIN = "plugin";
    private String plugin;

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    @Override
    public String toString() {
        return "FieldMappingProperties{"
            + "path=" + path
            + ", plugin='" + plugin + '\''
            + '}';
    }
}
