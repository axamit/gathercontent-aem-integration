/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo;

import com.axamit.gc.api.dto.GCData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>ImportItem</code> represents information about item need to be imported or exported.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ImportItem {
    private String itemId;
    private String mappingPath;
    private String parentId;
    private String importPath;
    private String title;
    private String template;
    private String gcTemplateId;
    private List<ImportItem> children = new ArrayList<>();
    private String slug; //! May items have different slugs?
    private GCData newStatusData;
    private String gcTargetItemName = "";
    private String gcTargetItemId = "0";
    private Boolean isProceed = false;
    private String aemTitle;
    private Integer importIndex;
    private String mappingName;

    /**
     * @return Index number as it came to be processed.
     */
    public Integer getImportIndex() {
        return importIndex;
    }

    public void setImportIndex(Integer importIndex) {
        this.importIndex = importIndex;
    }

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

    /**
     * @return Name of item in GatherContent which will act as parent item for export.
     */
    public String getGcTargetItemName() {
        return gcTargetItemName;
    }

    public void setGcTargetItemName(final String gcTargetItemName) {
        this.gcTargetItemName = gcTargetItemName;
    }

    /**
     * @return ID of item in GatherContent which will act as parent item for export.
     */
    public String getGcTargetItemId() {
        return gcTargetItemId;
    }

    public void setGcTargetItemId(final String gcTargetItemId) {
        this.gcTargetItemId = gcTargetItemId;
    }

    /**
     * @return ID of GatherContent template.
     */
    public String getGcTemplateId() {
        return gcTemplateId;
    }

    public void setGcTemplateId(final String gcTemplateId) {
        this.gcTemplateId = gcTemplateId;
    }

    /**
     * @return AEM title for export.
     */
    public String getAemTitle() {
        return aemTitle;
    }

    public void setAemTitle(final String aemTitle) {
        this.aemTitle = aemTitle;
    }

    /**
     * @return True if item was already proceed, false otherwise.
     */
    public Boolean getProceed() {
        return isProceed;
    }

    public void setProceed(final Boolean proceed) {
        isProceed = proceed;
    }

    /**
     * @return Mapping Name.
     */
    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImportItem that = (ImportItem) o;

        return importPath != null ? importPath.equals(that.importPath) : that.importPath == null;

    }

    @Override
    public int hashCode() {
        return importPath != null ? importPath.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ImportItem{"
                + "itemId='" + itemId + '\''
                + ", title='" + title + '\''
                + '}';
    }
}
