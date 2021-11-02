/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The <code>GCProject</code> class represents project.
 *
 * @author Axamit, gc.support@axamit.com
 * @see <a href="https://gathercontent.com/developers/projects/get-projects-by-id/">Project</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCProject {
    private Integer id;
    private String name;
    private String type;
    private Boolean example;
    private Integer accountId;
    private Boolean active;
    private String textDirection;
    private String allowedTags;
    private String createdAt;
    private String updatedAt;
    private Boolean overdue;

    /**
     * @return Project ID.
     */
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * @return Project name.
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Boolean getExample() {
        return example;
    }

    public void setExample(final Boolean example) {
        this.example = example;
    }

    /**
     * @return Account ID of project owner.
     */
    @JsonProperty("account_id")
    public Integer getAccountId() {
        return accountId;
    }

    @JsonProperty("account_id")
    public void setAccountId(final Integer accountId) {
        this.accountId = accountId;
    }

    /**
     * @return A boolean governing is this project active or not.
     */
    public Boolean getActive() {
        return active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }

    /**
     * @return Text direction like 'rtl'.
     */
    @JsonProperty("text_direction")
    public String getTextDirection() {
        return textDirection;
    }

    @JsonProperty("text_direction")
    public void setTextDirection(final String textDirection) {
        this.textDirection = textDirection;
    }

    @JsonProperty("allowed_tags")
    public String getAllowedTags() {
        return allowedTags;
    }

    @JsonProperty("allowed_tags")
    public void setAllowedTags(final String allowedTags) {
        this.allowedTags = allowedTags;
    }

    /**
     * @return Creation date of project in GatherContent.
     */
    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return Last update date of project in GatherContent.
     */
    @JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(final String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return A boolean governing is this project overdue or not.
     */
    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(final Boolean overdue) {
        this.overdue = overdue;
    }
}
