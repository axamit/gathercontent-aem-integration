/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.axamit.gc.core.util.GCUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The <code>GCOption</code> class represents options in types 'choice_radio' and 'choice_checkbox'.
 *
 * @see <a href="https://gathercontent.com/developers/the-config-field/">Option</a>
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCOption {
    private String name;
    private String label;
    private Boolean selected;
    private String value;

    /**
     * @return Option name - string, not empty, unique.
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return Option label - string, not empty.
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
     * @return A boolean governing is this option selected or not.
     */
    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(final Boolean selected) {
        this.selected = selected;
    }

    /**
     * @return Option value - for 'choice_radio' element if the other_option attribute is true.
     */
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setEscapedValue(final String escapedValue) {
        this.value = GCUtil.unescapeGCString(escapedValue);
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
