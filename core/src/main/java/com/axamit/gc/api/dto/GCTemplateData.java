/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * The <code>GCTemplateData</code> class represents template's data.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCTemplateData {

    private Integer id;
    private String name;
    private Integer numberOfItemsUsing;
    private String structureUuid;
    private Integer projectId;
    private String updatedAt;
    private Integer updatedBy;

    /**
     * @return Template ID.
     */
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
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

    @JsonProperty("number_of_items_using")
    public Integer getNumberOfItemsUsing() {
        return numberOfItemsUsing;
    }

    @JsonProperty("number_of_items_using")
    public void setNumberOfItemsUsing(Integer numberOfItemsUsing) {
        this.numberOfItemsUsing = numberOfItemsUsing;
    }

    @JsonProperty("structure_uuid")
    public String getStructureUuid() {
        return structureUuid;
    }

    @JsonProperty("structure_uuid")
    public void setStructureUuid(String structureUuid) {
        this.structureUuid = structureUuid;
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

    @JsonProperty("updated_by")
    public Integer getUpdatedBy() {
        return updatedBy;
    }

    @JsonProperty("updated_by")
    public void setUpdatedBy(final Integer updatedBy) {
        this.updatedBy = updatedBy;
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
