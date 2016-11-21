/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

import java.util.List;

/**
 * Sling model class which represents table with items on Import History page.
 * @author Axamit, gc.support@axamit.com
 */
@Model(adaptables = Resource.class)
public final class ImportListModel {

    private Resource resource;

    private List<ImportModel> importList;

    public Resource getResource() {
        return resource;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    public List<ImportModel> getImportList() {
        return importList;
    }

    public void setImportList(final List<ImportModel> importList) {
        this.importList = importList;
    }
}
