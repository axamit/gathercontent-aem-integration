/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>ImportItem</code> represents information about list of items need to be imported.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ImportData {
    private List<ImportItem> items = new ArrayList<>();
    private Integer newStatusId;
    private String newStatusColor;
    private String newStatusName;
    private String projectName;
    private Integer projectId;

    /**
     * @return List of items need to be imported.
     */
    public List<ImportItem> getItems() {
        return items;
    }

    public void setItems(final List<ImportItem> items) {
        this.items = items;
    }

    /**
     * @return New live status ID to be applied on an GatherContent items.
     */
    public Integer getNewStatusId() {
        return newStatusId;
    }

    public void setNewStatusId(final Integer newStatusId) {
        this.newStatusId = newStatusId;
    }

    /**
     * @return New live status color to be applied on an GatherContent items.
     */
    public String getNewStatusColor() {
        return newStatusColor;
    }
    public void setNewStatusColor(final String newStatusColor) {
        this.newStatusColor = newStatusColor;
    }

    /**
     * @return New live status name to be applied on an GatherContent items.
     */

    public String getNewStatusName() {
        return newStatusName;
    }

    public void setNewStatusName(final String newStatusName) {
        this.newStatusName = newStatusName;
    }

    /**
     * @return Project name.
     */
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    /**
     * @return Project id.
     */
    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(final Integer projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return "ImportData{"
            + "items=" + items
            + ", newStatusId='" + newStatusId + '\''
            + ", newStatusColor='" + newStatusColor + '\''
            + ", newStatusName='" + newStatusName + '\''
            + ", projectName='" + projectName + '\''
            + ", projectId='" + projectId + '\''
            + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ImportData that = (ImportData) o;

        return new EqualsBuilder().append(getItems(), that.getItems()).append(getNewStatusId(), that.getNewStatusId()).append(getNewStatusColor(), that.getNewStatusColor()).append(getNewStatusName(), that.getNewStatusName()).append(getProjectName(), that.getProjectName()).append(getProjectId(), that.getProjectId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getItems()).append(getNewStatusId()).append(getNewStatusColor()).append(getNewStatusName()).append(getProjectName()).append(getProjectId()).toHashCode();
    }
}
