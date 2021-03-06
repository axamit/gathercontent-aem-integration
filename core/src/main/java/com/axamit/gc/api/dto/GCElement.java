/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.axamit.gc.core.util.GCUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The <code>GCElement</code> class represents element of config.
 *
 * @author Axamit, gc.support@axamit.com
 * @see <a href="https://gathercontent.com/developers/the-config-field/">Elements</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCElement {
    private GCElementType type;
    private String name;
    private Boolean required;
    private String label;
    private String value;
    private String microcopy;
    private Boolean otherOption;
    private String limitType;
    private Integer limit;
    private String title;
    private String subtitle;
    private Boolean plainText;
    private List<GCOption> options;

    /**
     * @return Element type like 'text', 'files', 'section', 'choice_radio', 'choice_checkbox'.
     */
    public GCElementType getType() {
        return type;
    }

    public void setType(final GCElementType type) {
        this.type = type;
    }

    /**
     * @return Element name - string, not empty, unique.
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return Element name - string, not empty like 'Blog post'.
     */
    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    @JsonProperty("label")
    public void setEscapedLabel(final String escapedLabel) {
        label = GCUtil.unescapeGCString(escapedLabel);
    }

    /**
     * @return Element value - string like '<p>Hello world</p>'.
     */
    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @JsonProperty("value")
    public void setEscapedValue(final String escapedValue) {
        value = GCUtil.unescapeGCString(escapedValue);
    }

    /**
     * @return Element microcopy - string.
     */
    public String getMicrocopy() {
        return microcopy;
    }

    public void setMicrocopy(final String microcopy) {
        this.microcopy = microcopy;
    }

    @JsonProperty("other_option")
    public Boolean getOtherOption() {
        return otherOption;
    }

    @JsonProperty("other_option")
    public void setOtherOption(final Boolean otherOption) {
        this.otherOption = otherOption;
    }

    /**
     * @return Element limit type - string, either "words" or "chars".
     */
    @JsonProperty("limit_type")
    public String getLimitType() {
        return limitType;
    }

    @JsonProperty("limit_type")
    public void setLimitType(final String limitType) {
        this.limitType = limitType;
    }

    /**
     * @return Element limit - integer, non-negative.
     */
    public Integer getLimit() {
        return limit;
    }

    public void setLimit(final Integer limit) {
        this.limit = limit;
    }

    /**
     * @return A boolean governing is this plain text element or not.
     */
    @JsonProperty("plain_text")
    public Boolean getPlainText() {
        return plainText;
    }

    @JsonProperty("plain_text")
    public void setPlainText(final Boolean plainText) {
        this.plainText = plainText;
    }

    /**
     * @return A boolean governing is this element required or not.
     */
    public Boolean getRequired() {
        return required;
    }

    public void setRequired(final Boolean required) {
        this.required = required;
    }

    /**
     * @return Element options used in 'choice_checkbox' and 'choice_radio' type - array, must have at least one option.
     */
    public List<GCOption> getOptions() {
        return options;
    }

    public void setOptions(final List<GCOption> options) {
        this.options = options;
    }

    /**
     * @return Element title used in 'section' type - string, not empty.
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @JsonProperty("title")
    public void setEscapedTitle(final String escapedTitle) {
        title = GCUtil.unescapeGCString(escapedTitle);
    }

    /**
     * @return Element subtitle used in 'section' type - string.
     */
    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(final String subtitle) {
        this.subtitle = subtitle;
    }

    @JsonProperty("subtitle")
    public void setEscapedSubTitle(final String escapedSubTitle) {
        subtitle = GCUtil.unescapeGCString(escapedSubTitle);
    }

    @Override
    public String toString() {
        return "GCElement{"
            + "type=" + type
            + ", name='" + name + '\''
            + ", required=" + required
            + ", label='" + label + '\''
            + ", value='" + value + '\''
            + ", microcopy='" + microcopy + '\''
            + ", otherOption=" + otherOption
            + ", limitType='" + limitType + '\''
            + ", limit=" + limit
            + ", title='" + title + '\''
            + ", subtitle='" + subtitle + '\''
            + ", plainText=" + plainText
            + ", options=" + options
            + '}';
    }
}
