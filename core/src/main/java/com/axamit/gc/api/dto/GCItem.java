/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The <code>GCItem</code> class represents items (pages) in GatherContent.
 *
 * @see <a href="https://gathercontent.com/developers/items/get-items-by-id/">Item</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCItem {
    private String id;
    private String projectId;
    private String parentId;
    private String templateId;
    private String customStateId;
    private Integer position;
    private String name;
    private List<GCConfig> config;
    private String notes;
    private Boolean overdue;
    private String archivedBy;
    private String archivedAt;
    private GCTime createdAt;
    private GCTime updatedAt;
    private GCStatus status;

    /**
     * @return Item ID.
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

    /**
     * @return Item ID of parent item.
     */
    @JsonProperty("parent_id")
    public String getParentId() {
        return parentId;
    }

    @JsonProperty("parent_id")
    public void setParentId(final String parentId) {
        this.parentId = parentId;
    }

    /**
     * @return Template ID.
     */
    @JsonProperty("template_id")
    public String getTemplateId() {
        return templateId;
    }

    @JsonProperty("template_id")
    public void setTemplateId(final String templateId) {
        this.templateId = templateId;
    }

    @JsonProperty("custom_state_id")
    public String getCustomStateId() {
        return customStateId;
    }

    @JsonProperty("custom_state_id")
    public void setCustomStateId(final String customStateId) {
        this.customStateId = customStateId;
    }

    /**
     * @return Position in list of items.
     */
    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    /**
     * @return Item name.
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return List of item field configs.
     */
    public List<GCConfig> getConfig() {
        return config;
    }

    public void setConfig(final List<GCConfig> config) {
        this.config = config;
    }

    /**
     * @return Item notes.
     */
    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    /**
     * @return A boolean governing is this item overdue or not.
     */
    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(final Boolean overdue) {
        this.overdue = overdue;
    }

    @JsonProperty("archived_by")
    public String getArchivedBy() {
        return archivedBy;
    }

    @JsonProperty("archived_by")
    public void setArchivedBy(final String archivedBy) {
        this.archivedBy = archivedBy;
    }

    @JsonProperty("archived_at")
    public String getArchivedAt() {
        return archivedAt;
    }

    @JsonProperty("archived_at")
    public void setArchivedAt(final String archivedAt) {
        this.archivedAt = archivedAt;
    }

    /**
     * @return Live status.
     */
    public GCStatus getStatus() {
        return status;
    }

    public void setStatus(final GCStatus status) {
        this.status = status;
    }

    /**
     * @return Creation date of item in GatherContent.
     */
    @JsonProperty("created_at")
    public GCTime getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(final GCTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return Last update date of item in GatherContent.
     */
    @JsonProperty("updated_at")
    public GCTime getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(final GCTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
