/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The <code>GCFolder</code> class represents items (pages) in GatherContent.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCFolder {

    private String uuid;
    private String name;
    private Integer position;
    private String parentUuid;
    private Integer projectId;
    private String type;
    private String archivedAt;
    private List<GCFolder> folders;
    private List<GCItem> items;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @JsonProperty("parent_uuid")
    public String getParentUuid() {
        return parentUuid;
    }

    @JsonProperty("parent_uuid")
    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    @JsonProperty("project_id")
    public Integer getProjectId() {
        return projectId;
    }

    @JsonProperty("project_id")
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("archived_at")
    public String getArchivedAt() {
        return archivedAt;
    }

    @JsonProperty("archived_at")
    public void setArchivedAt(String archivedAt) {
        this.archivedAt = archivedAt;
    }

    public List<GCFolder> getFolders() {
        return folders;
    }

    public void setFolders(List<GCFolder> folders) {
        this.folders = folders;
    }

    public List<GCItem> getItems() {
        return items;
    }

    public void setItems(List<GCItem> items) {
        this.items = items;
    }
}
