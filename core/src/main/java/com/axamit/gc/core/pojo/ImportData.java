/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>ImportItem</code> represents information about list of items need to be imported.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ImportData {
    private List<ImportItem> items = new ArrayList<>();
    private String newStatusId;
    private String newStatusColor;
    private String newStatusName;

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
    public String getNewStatusId() {
        return newStatusId;
    }

    public void setNewStatusId(final String newStatusId) {
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
}
