/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo;

import com.axamit.gc.api.dto.GCData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>ImportItem</code> represents information about item need to be imported.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ImportItem {
    private String itemId;
    private String mappingPath;
    private String parentId;
    private String importPath;
    private String title;
    private String template;
    private List<ImportItem> children = new ArrayList<>();
    private String slug;
    private GCData newStatusData;

    /**
     * @return ID of GatherContent item.
     */
    public String getItemId() {
        return itemId;
    }

    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    /**
     * @return JCR path to mapping in AEM like
     * '/etc/cloudservices/gathercontent/gathercontent-importer/jcr:content/mapping-list/mapping_1'.
     */
    public String getMappingPath() {
        return mappingPath;
    }

    public void setMappingPath(final String mappingPath) {
        this.mappingPath = mappingPath;
    }

    /**
     * @return ID of GatherContent parent item.
     */
    public String getParentId() {
        return parentId;
    }

    public void setParentId(final String parentId) {
        this.parentId = parentId;
    }

    /**
     * @return Target JCR path for import items in AEM like '/content/geometrixx/en/targetforimport'.
     */
    public String getImportPath() {
        return importPath;
    }

    public void setImportPath(final String importPath) {
        this.importPath = importPath;
    }

    /**
     * @return The title of item in GatherContent.
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * @return GatherContent template name.
     */
    public String getTemplate() {
        return template;
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    /**
     * @return List of children items.
     */
    public List<ImportItem> getChildren() {
        return children;
    }

    public void setChildren(final List<ImportItem> children) {
        this.children = children;
    }

    /**
     * @return Subdomain of account on GatherContent.
     */
    public String getSlug() {
        return slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    /**
     * @return New live status data to be applied on an GatherContent item.
     */
    public GCData getNewStatusData() {
        return newStatusData;
    }

    public void setNewStatusData(final GCData newStatusData) {
        this.newStatusData = newStatusData;
    }

    @Override
    public String toString() {
        return "ImportItem{"
                + "itemId='" + itemId + '\''
                + ", title='" + title + '\''
                + '}';
    }
}
