/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The <code>GCData</code> class represents data structure of live status.
 *
 * @author Axamit, gc.support@axamit.com
 * @see <a href="https://gathercontent.com/developers/projects/get-projects-statuses/">Statuses</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCData {

    private String id;
    private Boolean isDefault;
    private Integer position;
    private String color;
    private String name;
    private String description;
    private Boolean canEdit;

    /**
     * @return Status ID.
     */
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return A boolean governing is this live status default or not.
     */
    @JsonProperty("is_default")
    public Boolean getIsDefault() {
        return isDefault;
    }

    @JsonProperty("is_default")
    public void setIsDefault(final Boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * @return Position in list of statuses.
     */
    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    /**
     * @return Status color in HEX e.g. #FAA732.
     */
    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    /**
     * @return Status name.
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return Status description.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @JsonProperty("can_edit")
    public Boolean getCanEdit() {
        return canEdit;
    }

    @JsonProperty("can_edit")
    public void setCanEdit(final Boolean canEdit) {
        this.canEdit = canEdit;
    }
}
