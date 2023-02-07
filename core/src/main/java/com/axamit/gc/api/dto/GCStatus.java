/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The <code>GCData</code> class represents data structure of live status.
 *
 * @author Axamit, gc.support@axamit.com
 * @see <a href="https://gathercontent.com/developers/projects/get-projects-statuses/">Statuses</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCStatus {

    private Integer id;
    private Boolean isDefault;
    private Integer position;
    private String color;
    private String display_name;
    private String description;
    private Boolean canEdit;

    /**
     * @return Status ID.
     */
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
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
    @JsonProperty("display_name")
    public String getDisplayName() {
        return display_name;
    }
    @JsonProperty("display_name")
    public void setDisplayName(final String display_name) {
        this.display_name = display_name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GCStatus gcStatus = (GCStatus) o;

        return new EqualsBuilder().append(getId(), gcStatus.getId()).append(getIsDefault(), gcStatus.getIsDefault()).append(getPosition(), gcStatus.getPosition()).append(getColor(), gcStatus.getColor()).append(display_name, gcStatus.display_name).append(getDescription(), gcStatus.getDescription()).append(getCanEdit(), gcStatus.getCanEdit()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).append(getIsDefault()).append(getPosition()).append(getColor()).append(display_name).append(getDescription()).append(getCanEdit()).toHashCode();
    }
}
