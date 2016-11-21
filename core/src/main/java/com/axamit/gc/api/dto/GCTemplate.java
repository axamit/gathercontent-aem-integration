/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


/**
 * The <code>GCTemplate</code> class represents template.
 *
 * @see <a href="https://gathercontent.com/developers/templates/get-templates-by-id/">Template</a>
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCTemplate {

    private String id;
    private String projectId;
    private String createdBy;
    private String updatedBy;
    private String name;
    private String description;
    private List<GCConfig> config;
    private String usedAt;
    private String createdAt;
    private String updatedAt;

    /**
     * @return Template ID.
     */
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return Project ID.
     */
    @JsonProperty("project_id")
    public String getProjectId() {
        return projectId;
    }

    @JsonProperty("project_id")
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @JsonProperty("created_by")
    public String getCreatedBy() {
        return createdBy;
    }

    @JsonProperty("created_by")
    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    @JsonProperty("updated_by")
    public String getUpdatedBy() {
        return updatedBy;
    }

    @JsonProperty("updated_by")
    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * @return Template name.
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return Template description.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * @return List of template field configs.
     */
    public List<GCConfig> getConfig() {
        return config;
    }

    public void setConfig(final List<GCConfig> config) {
        this.config = config;
    }

    /**
     * @return Used date like '2015-08-26 17:03:20'.
     */
    @JsonProperty("used_at")
    public String getUsedAt() {
        return usedAt;
    }

    @JsonProperty("used_at")
    public void setUsedAt(final String usedAt) {
        this.usedAt = usedAt;
    }

    /**
     * @return Used date like '1440604320'.
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
     * @return Used date like '1440608600'.
     */
    @JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(final String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
