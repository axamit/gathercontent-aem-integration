/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.axamit.gc.api.dto.helpers.GCContentMapSerializer;
import com.axamit.gc.core.util.GCUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

/**
 * The <code>GCItem</code> class represents items (pages) in GatherContent.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class GCItem {

    private Integer id;
    private Integer projectId;
    private String folderUuid;
    private Integer templateId;
    private String structureUuid;
    private Integer position;
    private String name;
    private String archivedBy;
    private String archivedAt;
    private String createdAt;
    private String updatedAt;
    private String nextDueAt;
    private String completedAt;
    private Integer statusId;

    @JsonSerialize(using = GCContentMapSerializer.class)
    private Map<String, GCContent> content;

    /**
     * @return Item ID.
     */
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * @return Project ID.
     */
    @JsonProperty("project_id")
    public Integer getProjectId() {
        return projectId;
    }

    @JsonProperty("project_id")
    public void setProjectId(final Integer projectId) {
        this.projectId = projectId;
    }

    /**
     * @return Parent folder uuid.
     */
    @JsonProperty("folder_uuid")
    public String getFolderUuid() {
        return folderUuid;
    }

    @JsonProperty("folder_uuid")
    public void setFolderUuid(String folderUuid) {
        this.folderUuid = folderUuid;
    }

    /**
     * @return Template ID.
     */
    @JsonProperty("template_id")
    public Integer getTemplateId() {
        return templateId;
    }

    @JsonProperty("template_id")
    public void setTemplateId(final Integer templateId) {
        this.templateId = templateId;
    }

    /**
     * @return Structure Uuid.
     */
    @JsonProperty("structure_uuid")
    public String getStructureUuid() {
        return structureUuid;
    }

    @JsonProperty("structure_uuid")
    public void setStructureUuid(String structureUuid) {
        this.structureUuid = structureUuid;
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

    @JsonProperty("name")
    public void setEscapedName(final String escapedName) {
        this.name = GCUtil.unescapeGCString(escapedName);
    }

    public void setName(final String name) {
        this.name = name;
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
     * @return Creation date of item in GatherContent.
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
     * @return Last update date of item in GatherContent.
     */
    @JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(final String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("next_due_at")
    public String getNextDueAt() {
        return nextDueAt;
    }

    @JsonProperty("next_due_at")
    public void setNextDueAt(String nextDueAt) {
        this.nextDueAt = nextDueAt;
    }

    @JsonProperty("completed_at")
    public String getCompletedAt() {
        return completedAt;
    }

    @JsonProperty("completed_at")
    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    @JsonProperty("status_id")
    public Integer getStatusId() {
        return statusId;
    }

    @JsonProperty("status_id")
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Map<String, GCContent> getContent() {
        return ImmutableMap.copyOf(content);
    }

    public void setContent(Map<String, GCContent> content) {
        this.content = content;
    }
}
